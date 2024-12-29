/*
 * Copyright (C) 2024 Yasumasa Suenaga
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
package com.yasuenag.nativebinder.internal;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.OptionalInt;

import com.yasuenag.ffmasm.amd64.AMD64AsmBuilder;
import com.yasuenag.ffmasm.amd64.Register;

import com.yasuenag.nativebinder.NativeBinder;


public class LinuxNativeBinder extends NativeBinder{

  private static final Register[] intArgRegs = new Register[]{
                                                 Register.RDI,
                                                 Register.RSI,
                                                 Register.RDX,
                                                 Register.RCX,
                                                 Register.R8,
                                                 Register.R9
                                               };

  private static final int FP_ARGREG_LIMIT = 8; // xmm0 - xmm7

  private int calculateArgStackSize(Class<?>[] argTypes){
    int numIntParamOnStack = -intArgRegs.length;
    int numFPParamOnStack  = -FP_ARGREG_LIMIT;

    for(var type : argTypes){
      if(isIntegerClass(type)){
        numIntParamOnStack++;
      }
      else if(isFloatingPointClass(type)){
        numFPParamOnStack++;
      }
      else{
        throw new IllegalArgumentException("Unsupported argument type: " + type.getName());
      }
    }

    int numParamOnStack = ((numIntParamOnStack > 0) ? numIntParamOnStack : 0) +
                          ((numFPParamOnStack > 0) ? numFPParamOnStack : 0);
    return numParamOnStack * 8;
  }

  @Override
  protected ArgTransformRule createArgTransformRule(Method method){
    var argTypes = method.getParameterTypes();
    var argStackSize = calculateArgStackSize(argTypes);
    var alignedArgStackSize = alignTo16Bytes(argStackSize);

    int intArgs = 0;
    int fpArgs = 0;
    int fromStackOffset = 16; // RBP + (saved RBP) + (return address)
    int toStackOffset = 0;
    var transformers = new ArrayList<Transformer>();

    for(int i = 0; i < argTypes.length; i++){
      var type = argTypes[i];
      if(isIntegerClass(type)){
        if(intArgs < (intArgRegs.length - 2)){
          transformers.add(new Transformer(intArgRegs[intArgs + 2], intArgRegs[intArgs], ArgType.INT));
        }
        else{
          if(intArgs < intArgRegs.length){
            transformers.add(new Transformer(Register.RBP, OptionalInt.of(fromStackOffset), intArgRegs[intArgs], OptionalInt.empty(), ArgType.INT));
          }
          else{
            transformers.add(new Transformer(Register.RBP, OptionalInt.of(fromStackOffset), Register.RSP, OptionalInt.of(toStackOffset), ArgType.INT));
            toStackOffset += 8;
          }
          fromStackOffset += 8;
        }
        intArgs++;
      }
      else if(isFloatingPointClass(type)){
        if(fpArgs >= FP_ARGREG_LIMIT){
          transformers.add(new Transformer(Register.RBP, OptionalInt.of(fromStackOffset), Register.RSP, OptionalInt.of(toStackOffset), ArgType.FP));
          fromStackOffset += 8;
          toStackOffset += 8;
        }
        fpArgs++;
      }
      else{
        throw new IllegalArgumentException("Unsupported argument type: " + type.getName());
      }
    }

    return new ArgTransformRule(transformers.toArray(new Transformer[0]), alignedArgStackSize);
  }

  @Override
  protected void addPrologue(AMD64AsmBuilder builder){
    builder.push(Register.RBP)                                       /* push %rbp      */
           .movMR(Register.RSP, Register.RBP, OptionalInt.empty());  /* mov %rsp, %rbp */ 
  }

  @Override
  protected void addEpilogue(AMD64AsmBuilder builder){
    builder.leave()  /* leave */
           .ret();   /* ret   */
  }

  @Override
  protected Register xmmVolatileRegister(){
    return Register.XMM8;
  }

}
