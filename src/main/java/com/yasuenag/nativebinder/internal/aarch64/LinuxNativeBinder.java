/*
 * Copyright (C) 2025, Yasumasa Suenaga
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
package com.yasuenag.nativebinder.internal.aarch64;

import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.OptionalInt;

import com.yasuenag.ffmasm.AsmBuilder;
import com.yasuenag.ffmasm.aarch64.HWShift;
import com.yasuenag.ffmasm.aarch64.IndexClass;
import com.yasuenag.ffmasm.aarch64.Register;


public class LinuxNativeBinder extends AArch64NativeBinder{

  private static final Register[] intArgRegs = new Register[]{
                                                 Register.X0,
                                                 Register.X1,
                                                 Register.X2,
                                                 Register.X3,
                                                 Register.X4,
                                                 Register.X5,
                                                 Register.X6,
                                                 Register.X7
                                               };

  private static final int FP_ARGREG_LIMIT = 8; // v0 - v7

  /* errno is defined as "*__errno_location ()" in errno.h */
  private static final MemorySegment __errno_location;

  static{
    __errno_location = Linker.nativeLinker()
                             .defaultLookup()
                             .find("__errno_location")
                             .get();
  }

  private Transformer[] createArgTransformRuleInner(Method method, Register fromBaseReg, int fromStackOffset){
    var argTypes = method.getParameterTypes();

    int toStackOffset = 0;
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
            transformers.add(new Transformer(fromBaseReg, OptionalInt.of(fromStackOffset), Register.SP, OptionalInt.of(toStackOffset), ArgType.INT));
            toStackOffset += 8;
          }
          fromStackOffset += 8;
        }
        intArgs++;
      }
      else if(isFloatingPointClass(type)){
        if(fpArgs >= FP_ARGREG_LIMIT){
          if(fromStackOffset != toStackOffset){
            transformers.add(new Transformer(fromBaseReg, OptionalInt.of(fromStackOffset), Register.SP, OptionalInt.of(toStackOffset), ArgType.FP));
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
      return createArgTransformRuleInner(method, Register.SP, 0);
    }
    else{
      return createArgTransformRuleInner(method, Register.X29, 16 /* Saved FP + LR */);
    }
  }

  @Override
  protected void obtainErrorCode(AsmBuilder.AArch64 builder){
    long errno_addr = __errno_location.address();
    long cb_addr = ptrErrorCodeCallback.address();

    builder.stp(Register.X0, Register.X0, Register.SP, IndexClass.PreIndex, -16) // evacuate original return val with 16 bytes alignment
           .movz(Register.X9, (int)(errno_addr & 0xffff), HWShift.None)
           .movk(Register.X9, (int)((errno_addr >> 16) & 0xffff), HWShift.HW_16)
           .movk(Register.X9, (int)((errno_addr >> 32) & 0xffff), HWShift.HW_32)
           .movk(Register.X9, (int)((errno_addr >> 48) & 0xffff), HWShift.HW_48)
           .blr(Register.X9) // get errno
           .ldr(Register.X0, Register.X0, IndexClass.UnsignedOffset, 0)
           .movz(Register.X9, (int)(cb_addr & 0xffff), HWShift.None)
           .movk(Register.X9, (int)((cb_addr >> 16) & 0xffff), HWShift.HW_16)
           .movk(Register.X9, (int)((cb_addr >> 32) & 0xffff), HWShift.HW_32)
           .movk(Register.X9, (int)((cb_addr >> 48) & 0xffff), HWShift.HW_48)
           .blr(Register.X9)
           .ldp(Register.X0, Register.X9 /* dummy */, Register.SP, IndexClass.PostIndex, 16); // restore original return val
  }

}
