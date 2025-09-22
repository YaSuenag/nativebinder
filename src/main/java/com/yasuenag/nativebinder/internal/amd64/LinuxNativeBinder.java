/*
 * Copyright (C) 2024, 2025, Yasumasa Suenaga
 *
 * This file is part of nativebinder.
 *
 * nativebinder is free software: you can redistribute it and/or modify  * it under the terms of the GNU Lesser General Public License as published by
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
package com.yasuenag.nativebinder.internal.amd64;

import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.OptionalInt;

import com.yasuenag.ffmasm.AsmBuilder;
import com.yasuenag.ffmasm.amd64.Register;

import com.yasuenag.nativebinder.NativeBinder;


public class LinuxNativeBinder extends AMD64NativeBinder{

  private static final Register[] intArgRegs = new Register[]{
                                                 Register.RDI,
                                                 Register.RSI,
                                                 Register.RDX,
                                                 Register.RCX,
                                                 Register.R8,
                                                 Register.R9
                                               };

  private static final int FP_ARGREG_LIMIT = 8; // xmm0 - xmm7

  /* errno is defined as "*__errno_location ()" in errno.h */
  private static final MemorySegment __errno_location;

  static{
    __errno_location = Linker.nativeLinker()
                             .defaultLookup()
                             .find("__errno_location")
                             .get();
  }

  private Transformer[] createArgTransformRuleInner(Method method, Register fromBaseReg, int fromStackOffset, int toStackOffset){
    var argTypes = method.getParameterTypes();

    int intArgs = 0;
    int fpArgs = 0;
    var transformers = new ArrayList<Transformer>();

    for(int i = 0; i < argTypes.length; i++){
      var type = argTypes[i];
      if(isIntegerClass(type)){
        if(intArgs < (intArgRegs.length - 2)){
          transformers.add(new Transformer(intArgRegs[intArgs + 2], intArgRegs[intArgs], ArgType.INT));
        }
        else{
          if(intArgs < intArgRegs.length){
            transformers.add(new Transformer(fromBaseReg, OptionalInt.of(fromStackOffset), intArgRegs[intArgs], OptionalInt.empty(), ArgType.INT));
          }
          else{
            transformers.add(new Transformer(fromBaseReg, OptionalInt.of(fromStackOffset), Register.RSP, OptionalInt.of(toStackOffset), ArgType.INT));
            toStackOffset += 8;
          }
          fromStackOffset += 8;
        }
        intArgs++;
      }
      else if(isFloatingPointClass(type)){
        if(fpArgs >= FP_ARGREG_LIMIT){
          if(fromStackOffset != toStackOffset){
            transformers.add(new Transformer(fromBaseReg, OptionalInt.of(fromStackOffset), Register.RSP, OptionalInt.of(toStackOffset), ArgType.FP));
          }
          fromStackOffset += 8;
          toStackOffset += 8;
        }
        fpArgs++;
      }
      else{
        throw new IllegalArgumentException("Unsupported argument type: " + type.getName());
      }
    }

    return transformers.toArray(new Transformer[0]);
  }

  @Override
  protected Transformer[] createArgTransformRule(Method method, boolean isJMP){
    if(isJMP){
      return createArgTransformRuleInner(method, Register.RSP, 8 /* RSP + (return address) */, 8 /* return address */);
    }
    else{
      return createArgTransformRuleInner(method, Register.RBP, 16 /* RSP + (saved RBP) + (return address) */, 0);
    }
  }

  @Override
  protected void obtainErrorCode(AsmBuilder.AVX builder){
    builder.sub(Register.RSP, 16, OptionalInt.empty()) // 16 bytes aligned
           .movMR(Register.RAX, Register.RSP, OptionalInt.of(0)) // evacuate original return val
           .movImm(Register.R10, __errno_location.address())
           .call(Register.R10) // get errno
           .movRM(Register.EDI, Register.RAX, OptionalInt.of(0))
           .movImm(Register.R10, ptrErrorCodeCallback.address())
           .call(Register.R10)
           .pop(Register.RAX, OptionalInt.empty()); // restore original return val
  }

  @Override
  protected Register xmmVolatileRegister(){
    return Register.XMM8;
  }

}
