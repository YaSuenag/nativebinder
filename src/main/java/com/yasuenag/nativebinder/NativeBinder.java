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
package com.yasuenag.nativebinder;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.ref.Cleaner;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.OptionalInt;

import com.yasuenag.ffmasm.CodeSegment;
import com.yasuenag.ffmasm.NativeRegister;
import com.yasuenag.ffmasm.PlatformException;
import com.yasuenag.ffmasm.UnsupportedPlatformException;
import com.yasuenag.ffmasm.amd64.AMD64AsmBuilder;
import com.yasuenag.ffmasm.amd64.AVXAsmBuilder;
import com.yasuenag.ffmasm.amd64.Register;
import com.yasuenag.ffmasm.amd64.SSEAsmBuilder;

import com.yasuenag.nativebinder.internal.LinuxNativeBinder;
import com.yasuenag.nativebinder.internal.WindowsNativeBinder;


/**
 * Class for binding C functions to Java native methods.
 * JNI function needs JNIEnv and jobject or jclass, they are set in
 * 1st and 2nd argument in JNI function. They should be removed native
 * function call.
 * NativeBinder transforms the order of arguments to remove them.
 *
 * @author Yasumasa Suenaga
 */
public abstract class NativeBinder{

  /**
   * Record to store method information.
   *
   * @param method native (JNI) method to bind.
   * @param seg MemorySegment to hold C function pointer.
   */
  public static record BindMethod(Method method, MemorySegment seg){};

  /**
   * Argument type
   */
  protected static enum ArgType{
    /**
     * Integer (includes boolean and char)
     */
    INT,

    /**
     * Floating point
     */
    FP;
  }

  private static CodeSegment seg = null;

  private static boolean isAVX;

  /**
   * Function pointer of errorCodeCallback()
   */
  protected static MemorySegment ptrErrorCodeCallback = null;

  private static final ThreadLocal<Integer> threadLocalErrorCode = new ThreadLocal<>();

  /**
   * Record to store the rule of argument transformation.
   *
   * @param from register in JNI
   * @param fromOffset offset on stack if the argument is on stack.
   *        This value is for `from` - `from` would be RBP in this case.
   * @param to register in native function
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
                                           : String.format("%d(%s)", fromOffset.getAsInt(), from.toString());
      result += " -> ";
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
   * @param builder AMD64AsmBuilder instance for generating stub code.
   * @return builder instance
   */
  protected abstract AMD64AsmBuilder obtainErrorCode(AMD64AsmBuilder builder);

  private static void errorCodeCallback(int errcode){
    threadLocalErrorCode.set(errcode);
  }

  /**
   * Get error code in previous bind method call.
   * Note that the method should be binded by bindWithErrorCode().
   *
   * @return error code in previous binded method call
   */
  public static int errorCodeInPreviousCall(){
    return threadLocalErrorCode.get();
  }

  private static void init() throws PlatformException, UnsupportedPlatformException{
    if(seg == null){
      seg = new CodeSegment();
      var action = new CodeSegment.CleanerAction(seg);
      Cleaner.create()
             .register(NativeBinder.class, action);

      var desc = FunctionDescriptor.of(ValueLayout.JAVA_INT);
      var cpuid = AMD64AsmBuilder.create(AMD64AsmBuilder.class, seg, desc)
          /* push %rbp        */ .push(Register.RBP)
          /* mov  %rsp, %rbp  */ .movMR(Register.RSP, Register.RBP, OptionalInt.empty())
          /* mov  %rax, $0x01 */ .movImm(Register.RAX, 0x01L)
          /* cpuid            */ .cpuid()
          /* mov  %rcx, %rax  */ .movMR(Register.RCX, Register.RAX, OptionalInt.empty())
          /* leave            */ .leave()
          /* ret              */ .ret()
                                 .build("cpuid");
      try{
        int ecx = (int)cpuid.invoke();
        isAVX = ((ecx >>> 28) & 1) == 1;
      }
      catch(Throwable t){
        throw new RuntimeException(t);
      }

    }

    if(ptrErrorCodeCallback == null){
      try{
        var target = MethodHandles.lookup()
                                  .findStatic(NativeBinder.class, "errorCodeCallback", MethodType.methodType(void.class, int.class));
        var desc = FunctionDescriptor.ofVoid(ValueLayout.JAVA_INT);
        ptrErrorCodeCallback = Linker.nativeLinker()
                                     .upcallStub(target, desc, Arena.ofAuto());
      }
      catch(NoSuchMethodException | IllegalAccessException e){
        throw new RuntimeException(e);
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
  public static NativeBinder getInstance() throws PlatformException, UnsupportedPlatformException{
    init();

    var arch = System.getProperty("os.arch");
    if(arch.equals("amd64")){
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
    throw new UnsupportedPlatformException(arch);
  }

  /**
   * Get XMM volatile register.
   *
   * @return volatile register (XMM)
   */
  protected abstract Register xmmVolatileRegister();

  /**
   * Check integer class or not.
   *
   * @param cls to check
   * @return true if integer class
   */
  protected boolean isIntegerClass(Class<?> cls){
    return cls.equals(boolean.class) ||
           cls.equals(byte.class) ||
           cls.equals(char.class) ||
           cls.equals(short.class) ||
           cls.equals(int.class) ||
           cls.equals(long.class);
  }

  /**
   * Check floating point class or not.
   *
   * @param cls to check
   * @return true if floating point class
   */
  protected boolean isFloatingPointClass(Class<?> cls){
    return cls.equals(float.class) || cls.equals(double.class);
  }

  private AMD64AsmBuilder bindInner(AMD64AsmBuilder builder, Transformer[] rule){
    for(var transformer : rule){
      if(transformer.fromOffset().isEmpty() && transformer.toOffset().isEmpty()){
        // reg to reg
        if(transformer.type() == ArgType.INT){
          builder.movMR(transformer.from(), transformer.to(), OptionalInt.empty());
        }
        else{ // should be FP
          builder.cast(SSEAsmBuilder.class)
                 .movdqaMR(transformer.from(), transformer.to(), OptionalInt.empty());
        }
      }
      else if(transformer.fromOffset().isPresent() && transformer.toOffset().isEmpty()){
        // mem to reg
        if(transformer.type() == ArgType.INT){
          builder.movRM(transformer.to(), transformer.from(), transformer.fromOffset());
        }
        else{ // should be FP
          builder.cast(SSEAsmBuilder.class)
                 .movqRM(transformer.to(), transformer.from(), transformer.fromOffset());
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
          builder.cast(SSEAsmBuilder.class)
                 .movqRM(tmpReg, transformer.from(), transformer.fromOffset())
                 .movqMR(tmpReg, transformer.to(), transformer.toOffset());
        }
      }
      else{
        throw new IllegalStateException("Should not be reg-mem");
      }
    }

    return builder;
  }

  /**
   * Bind C functions to JNI methods.
   *
   * @param targetClass to hold JNI methods
   * @param bindMethods array of binding information
   */
  public void bind(Class<?> targetClass, BindMethod[] bindMethods) throws Throwable{
    var methodMap = new HashMap<Method, MemorySegment>();

    for(var bindMethod : bindMethods){
      var rule = createArgTransformRule(bindMethod.method(), true);

      AMD64AsmBuilder builder;
      if(isAVX){
        builder = AMD64AsmBuilder.create(AVXAsmBuilder.class, seg)
                                 .vzeroupper();
      }
      else{
        builder = AMD64AsmBuilder.create(SSEAsmBuilder.class, seg);
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
   * Bind C functions to JNI methods.
   * Error code (errno in Linux, GetLastError() in Windows) can be obtained.
   *
   * @param targetClass to hold JNI methods
   * @param bindMethods array of binding information
   */
  public void bindWithErrorCode(Class<?> targetClass, BindMethod[] bindMethods) throws Throwable{
    var methodMap = new HashMap<Method, MemorySegment>();

    for(var bindMethod : bindMethods){
      var klass = isAVX ? AVXAsmBuilder.class : SSEAsmBuilder.class;
      // Stack size is estimated a max value.
      int stackSize = 8 * bindMethod.method().getParameterTypes().length;
      if(stackSize < 32){  // for Windows reg param stack
        stackSize = 32;
      }
      int alignedStackSize = ((stackSize & 0xf) == 0) ? stackSize
                                                      : (stackSize + 0x10) & 0xfffffff0;

      var builder = AMD64AsmBuilder.create(klass, seg)
/* push %rbp                    */ .push(Register.RBP)
/* mov %rsp,               %rbp */ .movMR(Register.RSP, Register.RBP, OptionalInt.empty())
/* sub <alignedStackSize>, %rsp */ .sub(Register.RSP, alignedStackSize, OptionalInt.empty());

      if(isAVX){
        builder.cast(AVXAsmBuilder.class)
               .vzeroupper();
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
