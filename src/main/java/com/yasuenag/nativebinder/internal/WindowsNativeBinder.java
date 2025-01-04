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
  protected ArgTransformRule createArgTransformRule(Method method){
    var argTypes = method.getParameterTypes();
    var argStackSize = (argTypes.length <= 4) ? 32 /* reg param stack */ : (argTypes.length * 8);
    var alignedArgStackSize = alignTo16Bytes(argStackSize);

    int fromStackOffset = 48; // RBP + (saved RBP) + (return address) + (reg param stack (8 bytes * 4 registers))
    int toStackOffset = 0;
    var transformers = new ArrayList<Transformer>();

    for(int i = 0; i < argTypes.length; i++){
      var type = argTypes[i];
      var argType = isIntegerClass(type) ? ArgType.INT : ArgType.FP;
      if(i < 2){ // reg-reg
        var fromReg = isIntegerClass(type) ? intArgRegs[i + 2] : fpArgRegs[i + 2];
        var toReg = isIntegerClass(type) ? intArgRegs[i] : fpArgRegs[i];
        transformers.add(new Transformer(fromReg, toReg, argType));
        transformers.add(new Transformer(toReg, OptionalInt.empty(), Register.RSP, OptionalInt.of(toStackOffset), argType)); // for reg param stack
      }
      else{
        if(i < 4){ // mem-reg
          var toReg = isIntegerClass(type) ? intArgRegs[i] : fpArgRegs[i];
          transformers.add(new Transformer(Register.RBP, OptionalInt.of(fromStackOffset), toReg, OptionalInt.empty(), argType));
          transformers.add(new Transformer(toReg, OptionalInt.empty(), Register.RSP, OptionalInt.of(toStackOffset), argType)); // for reg param stack
        }
        else{ // mem-mem
          transformers.add(new Transformer(Register.RBP, OptionalInt.of(fromStackOffset), Register.RSP, OptionalInt.of(toStackOffset), argType));
        }

        fromStackOffset += 8;
      }

      toStackOffset += 8;
    }

    return new ArgTransformRule(transformers.toArray(new Transformer[0]), alignedArgStackSize);
  }

  @Override
  protected void addPrologue(AMD64AsmBuilder builder){
    builder.push(Register.RBP)                                      /* push %rbp      */
           .movMR(Register.RSP, Register.RBP, OptionalInt.empty()); /* mov %rsp, %rbp */ 
  }

  @Override
  protected void addEpilogue(AMD64AsmBuilder builder){
    builder.leave()  /* leave */
           .ret();   /* ret   */
  }

  @Override
  protected Register xmmVolatileRegister(){
    return Register.XMM4;
  }

}
