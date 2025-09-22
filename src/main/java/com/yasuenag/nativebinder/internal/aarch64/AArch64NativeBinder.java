/*
 * Copyright (C) 2025, Yasumasa Suenaga
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
package com.yasuenag.nativebinder.internal.aarch64;

import java.lang.foreign.MemorySegment;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Optional;
import java.util.OptionalInt;

import com.yasuenag.ffmasm.AsmBuilder;
import com.yasuenag.ffmasm.NativeRegister;
import com.yasuenag.ffmasm.PlatformException;
import com.yasuenag.ffmasm.UnsupportedPlatformException;
import com.yasuenag.ffmasm.aarch64.HWShift;
import com.yasuenag.ffmasm.aarch64.IndexClass;
import com.yasuenag.ffmasm.aarch64.Register;

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
public abstract class AArch64NativeBinder extends NativeBinder{

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
  protected abstract void obtainErrorCode(AsmBuilder.AArch64 builder);

  /**
   * Get NativeBinder instance.
   *
   * @return NativeBinder instance
   * @throws UnsupportedPlatformException thrown when the platform is not
   *         supported by NativeBinder.
   */
  public static AArch64NativeBinder getInstance() throws PlatformException, UnsupportedPlatformException{
    var osName = System.getProperty("os.name");
    if(osName.equals("Linux")){
      return new LinuxNativeBinder();
    }
    else{
      throw new UnsupportedPlatformException(osName);
    }
  }

  private void bindInner(AsmBuilder.AArch64 builder, Transformer[] rule){
    for(var transformer : rule){
      if(transformer.fromOffset().isEmpty() && transformer.toOffset().isEmpty()){
        // reg to reg
        if(transformer.type() == ArgType.INT){
          builder.mov(transformer.to(), transformer.from());
        }
        else{ // should be FP
          throw new IllegalStateException("FP reg to FP reg is not supported.");
        }
      }
      else if(transformer.fromOffset().isPresent() && transformer.toOffset().isEmpty()){
        // mem to reg
        if(transformer.type() == ArgType.INT){
          builder.ldr(transformer.to(), transformer.from(), IndexClass.UnsignedOffset, transformer.fromOffset().getAsInt());
        }
        else{ // should be FP
          throw new IllegalStateException("FP mem to FP reg is not supported.");
        }
      }
      else if(transformer.fromOffset().isPresent() && transformer.toOffset().isPresent()){
        // mem to mem
        builder.ldr(Register.X9, transformer.from(), IndexClass.UnsignedOffset, transformer.fromOffset().getAsInt())
               .str(Register.X9, transformer.to(), IndexClass.UnsignedOffset, transformer.toOffset().getAsInt());
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

      var builder = new AsmBuilder.AArch64(seg);
      bindInner(builder, rule);

      final long addr = bindMethod.seg().address();
      builder
/* movz  x0, #addr[0:15]           */ .movz(Register.X9, (int)(addr & 0xffff), HWShift.None)
/* movk  x0, #addr[16:31], lsl #16 */ .movk(Register.X9, (int)((addr >> 16) & 0xffff), HWShift.HW_16)
/* movk  x0, #addr[32:47], lsl #32 */ .movk(Register.X9, (int)((addr >> 32) & 0xffff), HWShift.HW_32)
/* movk  x0, #addr[48:63], lsl #48 */ .movk(Register.X9, (int)((addr >> 48) & 0xffff), HWShift.HW_48)
                                      .br(Register.X9);

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
      int alignedStackSize = ((stackSize & 0xf) == 0) ? stackSize
                                                      : (stackSize + 0x10) & 0xfffffff0;

      var builder = new AsmBuilder.AArch64(seg)
/* stp x29, x30, [sp, #-16]!       */ .stp(Register.X29, Register.X30, Register.SP, IndexClass.PreIndex, -16)
/* mov x29,  sp                    */ .mov(Register.X29, Register.SP)
/* sub  sp,  sp, #alignedStackSize */ .subImm(Register.SP, Register.SP, alignedStackSize, false);

      var rule = createArgTransformRule(bindMethod.method(), false);
      bindInner(builder, rule);

      final long addr = bindMethod.seg().address();
      builder
/* movz  x0, #addr[0:15]           */ .movz(Register.X9, (int)(addr & 0xffff), HWShift.None)
/* movk  x0, #addr[16:31], lsl #16 */ .movk(Register.X9, (int)((addr >> 16) & 0xffff), HWShift.HW_16)
/* movk  x0, #addr[32:47], lsl #32 */ .movk(Register.X9, (int)((addr >> 32) & 0xffff), HWShift.HW_32)
/* movk  x0, #addr[48:63], lsl #48 */ .movk(Register.X9, (int)((addr >> 48) & 0xffff), HWShift.HW_48)
                                      .blr(Register.X9);

      obtainErrorCode(builder);

      builder
/* mov  sp, x29              */ .mov(Register.SP, Register.X29)
/* ldp x29, x30, [sp], #16   */ .ldp(Register.X29, Register.X30, Register.SP, IndexClass.PostIndex, 16)
/* ret                       */ .ret(Optional.empty());

      var stubName = "stub_" + bindMethod.method().getName();
      var stubSeg = builder.getMemorySegment(stubName);
      methodMap.put(bindMethod.method(), stubSeg);
    }

    var register = NativeRegister.create(targetClass);
    register.registerNatives(methodMap);
  }

}
