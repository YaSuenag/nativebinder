/*
 * Copyright (C) 2024 Yasumasa Suenaga
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
package com.yasuenag.nativebinder.examples.manyargs;

import java.lang.foreign.SymbolLookup;
import java.lang.reflect.Method;
import java.util.Objects;

import com.yasuenag.nativebinder.NativeBinder;


public class Main{

  public native void intManyArgs(boolean a1,
                                 byte a2,
                                 char a3,
                                 short a4,
                                 int a5,
                                 long a6,
                                 boolean a7,
                                 byte a8,
                                 char a9,
                                 short a10,
                                 int a11,
                                 long a12);

  public native void fpManyArgs(float a1,
                                double a2,
                                float a3,
                                double a4,
                                float a5,
                                double a6,
                                float a7,
                                double a8,
                                float a9,
                                double a10,
                                float a11,
                                double a12,
                                float a13,
                                double a14,
                                float a15,
                                double a16);

  public native void mixManyArgs(boolean a1,
                                 float a2,
                                 char a3,
                                 double a4,
                                 int a5,
                                 float a6,
                                 long a7,
                                 double a8,
                                 byte a9,
                                 float a10,
                                 short a11,
                                 double a12,
                                 boolean a13,
                                 float a14,
                                 short a15,
                                 double a16,
                                 int a17,
                                 float a18);

  private final SymbolLookup lookup;

  public Main(){
    System.loadLibrary("manyargs");
    lookup = SymbolLookup.loaderLookup();
  }

  private void bind() throws Throwable{
    var cls = this.getClass();
    var clsMethods = cls.getMethods();
    Method methodIntManyArgs = null;
    Method methodFPManyArgs = null;
    Method methodMixManyArgs = null;
    for(var method : clsMethods){
      if(method.getName().equals("intManyArgs")){
        methodIntManyArgs = method;
      }
      else if(method.getName().equals("fpManyArgs")){
        methodFPManyArgs = method;
      }
      else if(method.getName().equals("mixManyArgs")){
        methodMixManyArgs = method;
      }
    }
    Objects.requireNonNull(methodIntManyArgs);
    Objects.requireNonNull(methodFPManyArgs);
    Objects.requireNonNull(methodMixManyArgs);

    var bindMethods = new NativeBinder.BindMethod[]{
                        new NativeBinder.BindMethod(methodIntManyArgs, lookup.find("intManyArgs").get()),
                        new NativeBinder.BindMethod(methodFPManyArgs, lookup.find("fpManyArgs").get()),
                        new NativeBinder.BindMethod(methodMixManyArgs, lookup.find("mixManyArgs").get())
                      };

    var binder = NativeBinder.getInstance();
    binder.bind(cls, bindMethods);
  }

  public static void main(String[] args) throws Throwable{
    var inst = new Main();
    inst.bind();

    inst.intManyArgs(true, (byte)2, (char)3, (short)4, 5, 6L,
                     false, (byte)8, (char)9, (short)10, 11, 12L);

    inst.fpManyArgs(0.1f, 0.2d, 0.3f, 0.4d, 0.5f, 0.6d, 0.7f, 0.8d,
                    0.9f, 1.0d, 1.1f, 1.2d, 1.3f, 1.4d, 1.5f, 1.6d);

    inst.mixManyArgs(true, 0.2f, (char)3, 0.4d, 5, 0.6f, 7L, 0.8d, (byte)9,
                     1.0f, (short)11, 1.2d, false, 1.4f, (short)15, 1.6d, 17, 1.8f);
  }

}
