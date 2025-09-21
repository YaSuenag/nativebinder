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
package com.yasuenag.nativebinder.internal;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.SymbolLookup;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.OptionalInt;

import com.yasuenag.ffmasm.AsmBuilder;
import com.yasuenag.ffmasm.amd64.Register;

import com.yasuenag.nativebinder.NativeBinder;


public class WindowsNativeBinder extends AMD64NativeBinder{

  private static final Register[] intArgRegs = new Register[]{
                                                 Register.RCX,
                                                 Register.RDX,
                                                 Register.R8,
                                                 Register.R9
                                               };

  private static final Register[] fpArgRegs = new Register[]{
                                                  Register.XMM0,
                                                  Register.XMM1,
                                                  Register.XMM2,
                                                  Register.XMM3
                                              };

  private static final MemorySegment getLastError;

  static{
    getLastError = SymbolLookup.libraryLookup("Kernel32", Arena.global())
                               .find("GetLastError")
                               .get();
  }

  private Transformer[] createArgTransformRuleInner(Method method, Register fromBaseReg, int fromStackOffset, int toStackOffset){
    var argTypes = method.getParameterTypes();

    var transformers = new ArrayList<Transformer>();

    for(int i = 0; i < argTypes.length; i++){
      var type = argTypes[i];
      var argType = isIntegerClass(type) ? ArgType.INT : ArgType.FP;
      if(i < 2){ // reg-reg
        var fromReg = isIntegerClass(type) ? intArgRegs[i + 2] : fpArgRegs[i + 2];
        var toReg = isIntegerClass(type) ? intArgRegs[i] : fpArgRegs[i];
        transformers.add(new Transformer(fromReg, toReg, argType));
      }
      else{
        if(i < 4){ // mem-reg
          var toReg = isIntegerClass(type) ? intArgRegs[i] : fpArgRegs[i];
          transformers.add(new Transformer(fromBaseReg, OptionalInt.of(fromStackOffset), toReg, OptionalInt.empty(), argType));
        }
        else{ // mem-mem
          transformers.add(new Transformer(fromBaseReg, OptionalInt.of(fromStackOffset), Register.RSP, OptionalInt.of(toStackOffset), argType));
          toStackOffset += 8;
        }

        fromStackOffset += 8;
      }

    }

    return transformers.toArray(new Transformer[0]);
  }

  @Override
  protected Transformer[] createArgTransformRule(Method method, boolean isJMP){
    if(isJMP){
      return createArgTransformRuleInner(method, Register.RSP, 40 /* RSP + (return address) + (reg param stack (8 bytes * 4 registers)) */, 40 /* RSP + (return address) + (reg param stack (8 bytes * 4 registers)) */);
    }
    else{
      return createArgTransformRuleInner(method, Register.RBP, 48 /* RSP + (return address) + (reg param stack (8 bytes * 4 registers)) */, 32 /* reg param stack (8 bytes * 4 registers) */);
    }

  }

  @Override
  protected void obtainErrorCode(AsmBuilder.AVX builder){
    builder.sub(Register.RSP, 48, OptionalInt.empty()) // reg param stack + aligned stack (16 bytes)
           .movMR(Register.RAX, Register.RSP, OptionalInt.of(32)) // evacuate original return val
           .movImm(Register.R10, getLastError.address())
           .call(Register.R10) // get error code
           .movRM(Register.EDI, Register.EAX, OptionalInt.empty())
           .movImm(Register.R10, ptrErrorCodeCallback.address())
           .call(Register.R10)
           .movRM(Register.RAX, Register.RSP, OptionalInt.of(32)); // restore original return val
  }

  @Override
  protected Register xmmVolatileRegister(){
    return Register.XMM4;
  }

}
