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

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.OptionalInt;

import com.yasuenag.ffmasm.amd64.AMD64AsmBuilder;
import com.yasuenag.ffmasm.amd64.Register;

import com.yasuenag.nativebinder.NativeBinder;


public class WindowsNativeBinder extends NativeBinder{

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

  @Override
  protected Transformer[] createArgTransformRule(Method method, boolean isJMP){
    var argTypes = method.getParameterTypes();

    int fromStackOffset = 40; // RSP + (return address) + (reg param stack (8 bytes * 4 registers))
    int toStackOffset = 40; // RSP + (return address) + (reg param stack (8 bytes * 4 registers))
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
          transformers.add(new Transformer(Register.RSP, OptionalInt.of(fromStackOffset), toReg, OptionalInt.empty(), argType));
        }
        else{ // mem-mem
          transformers.add(new Transformer(Register.RSP, OptionalInt.of(fromStackOffset), Register.RSP, OptionalInt.of(toStackOffset), argType));
          toStackOffset += 8;
        }

        fromStackOffset += 8;
      }

    }

    return transformers.toArray(new Transformer[0]);
  }

  @Override
  protected AMD64AsmBuilder obtainErrorCode(AMD64AsmBuilder builder){
    // TODO
    return null;
  }

  @Override
  protected Register xmmVolatileRegister(){
    return Register.XMM4;
  }

}
