/*
 * Copyright (C) 2024, 2025, Yasumasa Suenaga
 *
 * This file is part of nativebinder.
 *
 * nativebinder is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * nativebinder is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with nativebinder. If not, see <http://www.gnu.org/licenses/>.
 */
package com.yasuenag.nativebinder.internal;

import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.OptionalInt;

import com.yasuenag.ffmasm.AsmBuilder;
import com.yasuenag.ffmasm.NativeRegister;
import com.yasuenag.ffmasm.PlatformException;
import com.yasuenag.ffmasm.UnsupportedPlatformException;
import com.yasuenag.ffmasm.amd64.Register;

import com.yasuenag.nativebinder.NativeBinder;


/**
 * Class for binding C functions to Java native methods.
 * JNI function needs JNIEnv and jobject or jclass, they are set in
 * 1st and 2nd argument in JNI function. They should be removed native
 * function call.
 * NativeBinder transforms the order of arguments to remove them.
 *
 * @author Yasumasa Suenaga
 */
public abstract class AMD64NativeBinder extends NativeBinder{

  private static volatile boolean initialized;
  private static boolean isAVX;

  /**
   * Record to store the rule of argument transformation.
   *
   * @param from register in JNI
   * @param fromOffset offset on stack if the argument is on stack.
   *        This value is for `from` - `from` would be RBP in this case.       * @param to register in native function
   * @param toOffset offset on stack if the argument is on stack.
   *        This value is for `to` - `to` would be RSP in this case.
   * @param type argument type
   */
  public static record Transformer(Register from, OptionalInt fromOffset, Register to, OptionalInt toOffset, ArgType type){

    public Transformer(Register from, Register to, ArgType type){
      this(from, OptionalInt.empty(), to, OptionalInt.empty(), type);
    }

    @Override
    public String toString(){
      String result = fromOffset.isEmpty() ? from.toString()
                                           : String.format("%d(%s)", fromOffset.getAsInt(), from.toString());                                                 result += " -> ";
      result += toOffset.isEmpty() ? to.toString()
                                   : String.format("%d(%s)", toOffset.getAsInt(), to.toString());
      return result;
    }
  }

  /**
   * Create transformation rule.
   *
   * @param method to create rule
   * @param isJMP true if the stub should be generated with JMP operation.
   * @return transformation ruleset
   */
  protected abstract Transformer[] createArgTransformRule(Method method, boolean isJMP);

  /**
   * Generate machine code to obtain error code (errno in Linux, GetLastError() in Windows)
   *
   * @param builder AsmBuilder instance for generating stub code.
   */
  protected abstract void obtainErrorCode(AsmBuilder.AVX builder);

  private static void init(){
    if(!initialized){
      try{
        var desc = FunctionDescriptor.of(ValueLayout.JAVA_INT);
        var cpuid = new AsmBuilder.AMD64(seg, desc)
           /* push %rbp        */ .push(Register.RBP)
           /* mov  %rsp, %rbp  */ .movMR(Register.RSP, Register.RBP, OptionalInt.empty())
           /* mov  %rax, $0x01 */ .movImm(Register.RAX, 0x01L)
           /* cpuid            */ .cpuid()
           /* mov  %rcx, %rax  */ .movMR(Register.RCX, Register.RAX, OptionalInt.empty())
           /* leave            */ .leave()
           /* ret              */ .ret()
                                  .build("cpuid");
        int ecx = (int)cpuid.invoke();
        isAVX = ((ecx >>> 28) & 1) == 1;
      }
      catch(Throwable t){
        throw new RuntimeException(t);
      }
    }
  }

  /**
   * Get NativeBinder instance.
   *
   * @return NativeBinder instance
   * @throws UnsupportedPlatformException thrown when the platform is not
   *         supported by NativeBinder.
   */
  public static AMD64NativeBinder getInstance() throws PlatformException, UnsupportedPlatformException{
    init();

    var osName = System.getProperty("os.name");
    if(osName.equals("Linux")){
      return new LinuxNativeBinder();
    }
    else if(osName.startsWith("Windows")){
      return new WindowsNativeBinder();
    }
    else{
      throw new UnsupportedPlatformException(osName);
    }
  }

  /**
   * Get XMM volatile register.
   *
   * @return volatile register (XMM)
   */
  protected abstract Register xmmVolatileRegister();

  private void bindInner(AsmBuilder.AVX builder, Transformer[] rule){
    for(var transformer : rule){
      if(transformer.fromOffset().isEmpty() && transformer.toOffset().isEmpty()){
        // reg to reg
        if(transformer.type() == ArgType.INT){
          builder.movMR(transformer.from(), transformer.to(), OptionalInt.empty());
        }
        else{ // should be FP
          builder.movdqaMR(transformer.from(), transformer.to(), OptionalInt.empty());
        }
      }
      else if(transformer.fromOffset().isPresent() && transformer.toOffset().isEmpty()){
        // mem to reg
        if(transformer.type() == ArgType.INT){
          builder.movRM(transformer.to(), transformer.from(), transformer.fromOffset());
        }
        else{ // should be FP
          builder.movqRM(transformer.to(), transformer.from(), transformer.fromOffset());
        }
      }
      else if(transformer.fromOffset().isPresent() && transformer.toOffset().isPresent()){
        // mem to mem
        if(transformer.type() == ArgType.INT){
          builder.movRM(Register.R11, transformer.from(), transformer.fromOffset())
                 .movMR(Register.R11, transformer.to(), transformer.toOffset());
        }
        else{ // should be PF
          var tmpReg = xmmVolatileRegister();
          builder.movqRM(tmpReg, transformer.from(), transformer.fromOffset())
                 .movqMR(tmpReg, transformer.to(), transformer.toOffset());
        }
      }
      else{
        throw new IllegalStateException("Should not be reg-mem");
      }
    }
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void bind(Class<?> targetClass, BindMethod[] bindMethods) throws Throwable{
    var methodMap = new HashMap<Method, MemorySegment>();

    for(var bindMethod : bindMethods){
      var rule = createArgTransformRule(bindMethod.method(), true);

      var builder = new AsmBuilder.AVX(seg);
      if(isAVX){
        builder.vzeroupper();
      }

      bindInner(builder, rule);

      builder.movImm(Register.R10, bindMethod.seg().address())
             .jmp(Register.R10);

      var stubName = "stub_" + bindMethod.method().getName();
      var stubSeg = builder.getMemorySegment(stubName);
      methodMap.put(bindMethod.method(), stubSeg);
    }

    var register = NativeRegister.create(targetClass);
    register.registerNatives(methodMap);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public void bindWithErrorCode(Class<?> targetClass, BindMethod[] bindMethods) throws Throwable{
    var methodMap = new HashMap<Method, MemorySegment>();

    for(var bindMethod : bindMethods){
      // Stack size is estimated a max value.
      int stackSize = 8 * bindMethod.method().getParameterTypes().length;
      if(stackSize < 32){  // for Windows reg param stack
        stackSize = 32;
      }
      int alignedStackSize = ((stackSize & 0xf) == 0) ? stackSize
                                                      : (stackSize + 0x10) & 0xfffffff0;

      var builder = new AsmBuilder.AVX(seg)
/* push %rbp                    */ .push(Register.RBP)
/* mov %rsp,               %rbp */ .movMR(Register.RSP, Register.RBP, OptionalInt.empty())
/* sub <alignedStackSize>, %rsp */ .sub(Register.RSP, alignedStackSize, OptionalInt.empty());

      if(isAVX){
        builder.vzeroupper();
      }

      var rule = createArgTransformRule(bindMethod.method(), false);
      bindInner(builder, rule);

      builder.movImm(Register.R10, bindMethod.seg().address())
             .call(Register.R10);

      obtainErrorCode(builder);

      builder.leave()
             .ret();

      var stubName = "stub_" + bindMethod.method().getName();
      var stubSeg = builder.getMemorySegment(stubName);
      methodMap.put(bindMethod.method(), stubSeg);
    }

    var register = NativeRegister.create(targetClass);
    register.registerNatives(methodMap);
  }

}
