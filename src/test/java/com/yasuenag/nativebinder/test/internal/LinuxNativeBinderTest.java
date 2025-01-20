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
package com.yasuenag.nativebinder.test.internal;

import java.lang.reflect.Method;
import java.util.Arrays;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import com.yasuenag.ffmasm.amd64.Register;

import com.yasuenag.nativebinder.NativeBinder;
import com.yasuenag.nativebinder.internal.LinuxNativeBinder;


@EnabledOnOs(OS.LINUX)
public class LinuxNativeBinderTest extends LinuxNativeBinder{

  // Skeltons for test
  public void intManyArgs(boolean a1,
                          byte a2,
                          char a3,
                          short a4,
                          int a5,
                          long a6,
                          boolean a7,
                          byte a8){}
  public void fpManyArgs(float a1,
                         double a2,
                         float a3,
                         double a4,
                         float a5,
                         double a6,
                         float a7,
                         double a8,
                         float a9,
                         double a10){}
  public void mixManyArgs(boolean a1,
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
                          float a18){}

  @Test
  public void testXmmVolatileRegister(){
    Assertions.assertEquals(Register.XMM8, xmmVolatileRegister());
  }

  private Method getTargetMethod(String name){
    return Arrays.stream(this.getClass().getMethods())
                 .filter(m -> m.getName().equals(name))
                 .findAny()
                 .get();
  }

  @Test
  void testIntManyArgsWithJMP(){
    var targetMethod = getTargetMethod("intManyArgs");
    var rule = createArgTransformRule(targetMethod, true);

    Assertions.assertEquals(8, rule.length);

    // arg1
    Assertions.assertEquals(Register.RDX, rule[0].from());
    Assertions.assertTrue(rule[0].fromOffset().isEmpty());
    Assertions.assertEquals(Register.RDI, rule[0].to());
    Assertions.assertTrue(rule[0].toOffset().isEmpty());
    Assertions.assertEquals(NativeBinder.ArgType.INT, rule[0].type());

    // arg2
    Assertions.assertEquals(Register.RCX, rule[1].from());
    Assertions.assertTrue(rule[1].fromOffset().isEmpty());
    Assertions.assertEquals(Register.RSI, rule[1].to());
    Assertions.assertTrue(rule[1].toOffset().isEmpty());
    Assertions.assertEquals(NativeBinder.ArgType.INT, rule[1].type());

    // arg3
    Assertions.assertEquals(Register.R8, rule[2].from());
    Assertions.assertTrue(rule[2].fromOffset().isEmpty());
    Assertions.assertEquals(Register.RDX, rule[2].to());
    Assertions.assertTrue(rule[2].toOffset().isEmpty());
    Assertions.assertEquals(NativeBinder.ArgType.INT, rule[2].type());

    // arg4
    Assertions.assertEquals(Register.R9, rule[3].from());
    Assertions.assertTrue(rule[3].fromOffset().isEmpty());
    Assertions.assertEquals(Register.RCX, rule[3].to());
    Assertions.assertTrue(rule[3].toOffset().isEmpty());
    Assertions.assertEquals(NativeBinder.ArgType.INT, rule[3].type());

    // arg5
    Assertions.assertEquals(Register.RSP, rule[4].from());
    Assertions.assertEquals(8, rule[4].fromOffset().getAsInt());
    Assertions.assertEquals(Register.R8, rule[4].to());
    Assertions.assertTrue(rule[4].toOffset().isEmpty());
    Assertions.assertEquals(NativeBinder.ArgType.INT, rule[4].type());

    // arg6
    Assertions.assertEquals(Register.RSP, rule[5].from());
    Assertions.assertEquals(16, rule[5].fromOffset().getAsInt());
    Assertions.assertEquals(Register.R9, rule[5].to());
    Assertions.assertTrue(rule[5].toOffset().isEmpty());
    Assertions.assertEquals(NativeBinder.ArgType.INT, rule[5].type());

    // arg7
    Assertions.assertEquals(Register.RSP, rule[6].from());
    Assertions.assertEquals(24, rule[6].fromOffset().getAsInt());
    Assertions.assertEquals(Register.RSP, rule[6].to());
    Assertions.assertEquals(8, rule[6].toOffset().getAsInt());
    Assertions.assertEquals(NativeBinder.ArgType.INT, rule[6].type());

    // arg8
    Assertions.assertEquals(Register.RSP, rule[7].from());
    Assertions.assertEquals(32, rule[7].fromOffset().getAsInt());
    Assertions.assertEquals(Register.RSP, rule[7].to());
    Assertions.assertEquals(16, rule[7].toOffset().getAsInt());
    Assertions.assertEquals(NativeBinder.ArgType.INT, rule[7].type());
  }

  @Test
  void testFPManyArgsWithJMP(){
    var targetMethod = getTargetMethod("fpManyArgs");
    var rule = createArgTransformRule(targetMethod, true);

    Assertions.assertEquals(0, rule.length);
  }

  @Test
  void testMixManyArgsWithJMP(){
    var targetMethod = getTargetMethod("mixManyArgs");
    var rule = createArgTransformRule(targetMethod, true);

    Assertions.assertEquals(10, rule.length);

    // arg1
    Assertions.assertEquals(Register.RDX, rule[0].from());
    Assertions.assertTrue(rule[0].fromOffset().isEmpty());
    Assertions.assertEquals(Register.RDI, rule[0].to());
    Assertions.assertTrue(rule[0].toOffset().isEmpty());
    Assertions.assertEquals(NativeBinder.ArgType.INT, rule[0].type());

    // arg3
    Assertions.assertEquals(Register.RCX, rule[1].from());
    Assertions.assertTrue(rule[1].fromOffset().isEmpty());
    Assertions.assertEquals(Register.RSI, rule[1].to());
    Assertions.assertTrue(rule[1].toOffset().isEmpty());
    Assertions.assertEquals(NativeBinder.ArgType.INT, rule[1].type());

    // arg5
    Assertions.assertEquals(Register.R8, rule[2].from());
    Assertions.assertTrue(rule[2].fromOffset().isEmpty());
    Assertions.assertEquals(Register.RDX, rule[2].to());
    Assertions.assertTrue(rule[2].toOffset().isEmpty());
    Assertions.assertEquals(NativeBinder.ArgType.INT, rule[1].type());

    // arg7
    Assertions.assertEquals(Register.R9, rule[3].from());
    Assertions.assertTrue(rule[3].fromOffset().isEmpty());
    Assertions.assertEquals(Register.RCX, rule[3].to());
    Assertions.assertTrue(rule[3].toOffset().isEmpty());
    Assertions.assertEquals(NativeBinder.ArgType.INT, rule[3].type());

    // arg9
    Assertions.assertEquals(Register.RSP, rule[4].from());
    Assertions.assertEquals(8, rule[4].fromOffset().getAsInt());
    Assertions.assertEquals(Register.R8, rule[4].to());
    Assertions.assertTrue(rule[4].toOffset().isEmpty());
    Assertions.assertEquals(NativeBinder.ArgType.INT, rule[4].type());

    // arg11
    Assertions.assertEquals(Register.RSP, rule[5].from());
    Assertions.assertEquals(16, rule[5].fromOffset().getAsInt());
    Assertions.assertEquals(Register.R9, rule[5].to());
    Assertions.assertTrue(rule[5].toOffset().isEmpty());
    Assertions.assertEquals(NativeBinder.ArgType.INT, rule[5].type());

    // arg13
    Assertions.assertEquals(Register.RSP, rule[6].from());
    Assertions.assertEquals(24, rule[6].fromOffset().getAsInt());
    Assertions.assertEquals(Register.RSP, rule[6].to());
    Assertions.assertEquals(8, rule[6].toOffset().getAsInt());
    Assertions.assertEquals(NativeBinder.ArgType.INT, rule[6].type());

    // arg15
    Assertions.assertEquals(Register.RSP, rule[7].from());
    Assertions.assertEquals(32, rule[7].fromOffset().getAsInt());
    Assertions.assertEquals(Register.RSP, rule[7].to());
    Assertions.assertEquals(16, rule[7].toOffset().getAsInt());
    Assertions.assertEquals(NativeBinder.ArgType.INT, rule[7].type());

    // arg17
    Assertions.assertEquals(Register.RSP, rule[8].from());
    Assertions.assertEquals(40, rule[8].fromOffset().getAsInt());
    Assertions.assertEquals(Register.RSP, rule[8].to());
    Assertions.assertEquals(24, rule[8].toOffset().getAsInt());
    Assertions.assertEquals(NativeBinder.ArgType.INT, rule[8].type());

    // arg18
    Assertions.assertEquals(Register.RSP, rule[9].from());
    Assertions.assertEquals(48, rule[9].fromOffset().getAsInt());
    Assertions.assertEquals(Register.RSP, rule[9].to());
    Assertions.assertEquals(32, rule[9].toOffset().getAsInt());
    Assertions.assertEquals(NativeBinder.ArgType.FP, rule[9].type());
  }

  @Test
  void testIntManyArgsWithErrorCode(){
    var targetMethod = getTargetMethod("intManyArgs");
    var rule = createArgTransformRule(targetMethod, false);

    Assertions.assertEquals(8, rule.length);

    // arg1
    Assertions.assertEquals(Register.RDX, rule[0].from());
    Assertions.assertTrue(rule[0].fromOffset().isEmpty());
    Assertions.assertEquals(Register.RDI, rule[0].to());
    Assertions.assertTrue(rule[0].toOffset().isEmpty());
    Assertions.assertEquals(NativeBinder.ArgType.INT, rule[0].type());

    // arg2
    Assertions.assertEquals(Register.RCX, rule[1].from());
    Assertions.assertTrue(rule[1].fromOffset().isEmpty());
    Assertions.assertEquals(Register.RSI, rule[1].to());
    Assertions.assertTrue(rule[1].toOffset().isEmpty());
    Assertions.assertEquals(NativeBinder.ArgType.INT, rule[1].type());

    // arg3
    Assertions.assertEquals(Register.R8, rule[2].from());
    Assertions.assertTrue(rule[2].fromOffset().isEmpty());
    Assertions.assertEquals(Register.RDX, rule[2].to());
    Assertions.assertTrue(rule[2].toOffset().isEmpty());
    Assertions.assertEquals(NativeBinder.ArgType.INT, rule[2].type());

    // arg4
    Assertions.assertEquals(Register.R9, rule[3].from());
    Assertions.assertTrue(rule[3].fromOffset().isEmpty());
    Assertions.assertEquals(Register.RCX, rule[3].to());
    Assertions.assertTrue(rule[3].toOffset().isEmpty());
    Assertions.assertEquals(NativeBinder.ArgType.INT, rule[3].type());

    // arg5
    Assertions.assertEquals(Register.RBP, rule[4].from());
    Assertions.assertEquals(16, rule[4].fromOffset().getAsInt());
    Assertions.assertEquals(Register.R8, rule[4].to());
    Assertions.assertTrue(rule[4].toOffset().isEmpty());
    Assertions.assertEquals(NativeBinder.ArgType.INT, rule[4].type());

    // arg6
    Assertions.assertEquals(Register.RBP, rule[5].from());
    Assertions.assertEquals(24, rule[5].fromOffset().getAsInt());
    Assertions.assertEquals(Register.R9, rule[5].to());
    Assertions.assertTrue(rule[5].toOffset().isEmpty());
    Assertions.assertEquals(NativeBinder.ArgType.INT, rule[5].type());

    // arg7
    Assertions.assertEquals(Register.RBP, rule[6].from());
    Assertions.assertEquals(32, rule[6].fromOffset().getAsInt());
    Assertions.assertEquals(Register.RSP, rule[6].to());
    Assertions.assertEquals(0, rule[6].toOffset().getAsInt());
    Assertions.assertEquals(NativeBinder.ArgType.INT, rule[6].type());

    // arg8
    Assertions.assertEquals(Register.RBP, rule[7].from());
    Assertions.assertEquals(40, rule[7].fromOffset().getAsInt());
    Assertions.assertEquals(Register.RSP, rule[7].to());
    Assertions.assertEquals(8, rule[7].toOffset().getAsInt());
    Assertions.assertEquals(NativeBinder.ArgType.INT, rule[7].type());
  }

  @Test
  void testFPManyArgsWithErrorCode(){
    var targetMethod = getTargetMethod("fpManyArgs");
    var rule = createArgTransformRule(targetMethod, false);

    Assertions.assertEquals(2, rule.length);

    // arg9
    Assertions.assertEquals(Register.RBP, rule[0].from());
    Assertions.assertEquals(16, rule[0].fromOffset().getAsInt());
    Assertions.assertEquals(Register.RSP, rule[0].to());
    Assertions.assertEquals(0, rule[0].toOffset().getAsInt());
    Assertions.assertEquals(NativeBinder.ArgType.FP, rule[0].type());

    // arg10
    Assertions.assertEquals(Register.RBP, rule[1].from());
    Assertions.assertEquals(24, rule[1].fromOffset().getAsInt());
    Assertions.assertEquals(Register.RSP, rule[1].to());
    Assertions.assertEquals(8, rule[1].toOffset().getAsInt());
    Assertions.assertEquals(NativeBinder.ArgType.FP, rule[1].type());
  }

  @Test
  void testMixManyArgsWithErrorCode(){
    var targetMethod = getTargetMethod("mixManyArgs");
    var rule = createArgTransformRule(targetMethod, false);

    Assertions.assertEquals(10, rule.length);

    // arg1
    Assertions.assertEquals(Register.RDX, rule[0].from());
    Assertions.assertTrue(rule[0].fromOffset().isEmpty());
    Assertions.assertEquals(Register.RDI, rule[0].to());
    Assertions.assertTrue(rule[0].toOffset().isEmpty());
    Assertions.assertEquals(NativeBinder.ArgType.INT, rule[0].type());

    // arg3
    Assertions.assertEquals(Register.RCX, rule[1].from());
    Assertions.assertTrue(rule[1].fromOffset().isEmpty());
    Assertions.assertEquals(Register.RSI, rule[1].to());
    Assertions.assertTrue(rule[1].toOffset().isEmpty());
    Assertions.assertEquals(NativeBinder.ArgType.INT, rule[1].type());

    // arg5
    Assertions.assertEquals(Register.R8, rule[2].from());
    Assertions.assertTrue(rule[2].fromOffset().isEmpty());
    Assertions.assertEquals(Register.RDX, rule[2].to());
    Assertions.assertTrue(rule[2].toOffset().isEmpty());
    Assertions.assertEquals(NativeBinder.ArgType.INT, rule[1].type());

    // arg7
    Assertions.assertEquals(Register.R9, rule[3].from());
    Assertions.assertTrue(rule[3].fromOffset().isEmpty());
    Assertions.assertEquals(Register.RCX, rule[3].to());
    Assertions.assertTrue(rule[3].toOffset().isEmpty());
    Assertions.assertEquals(NativeBinder.ArgType.INT, rule[3].type());

    // arg9
    Assertions.assertEquals(Register.RBP, rule[4].from());
    Assertions.assertEquals(16, rule[4].fromOffset().getAsInt());
    Assertions.assertEquals(Register.R8, rule[4].to());
    Assertions.assertTrue(rule[4].toOffset().isEmpty());
    Assertions.assertEquals(NativeBinder.ArgType.INT, rule[4].type());

    // arg11
    Assertions.assertEquals(Register.RBP, rule[5].from());
    Assertions.assertEquals(24, rule[5].fromOffset().getAsInt());
    Assertions.assertEquals(Register.R9, rule[5].to());
    Assertions.assertTrue(rule[5].toOffset().isEmpty());
    Assertions.assertEquals(NativeBinder.ArgType.INT, rule[5].type());

    // arg13
    Assertions.assertEquals(Register.RBP, rule[6].from());
    Assertions.assertEquals(32, rule[6].fromOffset().getAsInt());
    Assertions.assertEquals(Register.RSP, rule[6].to());
    Assertions.assertEquals(0, rule[6].toOffset().getAsInt());
    Assertions.assertEquals(NativeBinder.ArgType.INT, rule[6].type());

    // arg15
    Assertions.assertEquals(Register.RBP, rule[7].from());
    Assertions.assertEquals(40, rule[7].fromOffset().getAsInt());
    Assertions.assertEquals(Register.RSP, rule[7].to());
    Assertions.assertEquals(8, rule[7].toOffset().getAsInt());
    Assertions.assertEquals(NativeBinder.ArgType.INT, rule[7].type());

    // arg17
    Assertions.assertEquals(Register.RBP, rule[8].from());
    Assertions.assertEquals(48, rule[8].fromOffset().getAsInt());
    Assertions.assertEquals(Register.RSP, rule[8].to());
    Assertions.assertEquals(16, rule[8].toOffset().getAsInt());
    Assertions.assertEquals(NativeBinder.ArgType.INT, rule[8].type());

    // arg18
    Assertions.assertEquals(Register.RBP, rule[9].from());
    Assertions.assertEquals(56, rule[9].fromOffset().getAsInt());
    Assertions.assertEquals(Register.RSP, rule[9].to());
    Assertions.assertEquals(24, rule[9].toOffset().getAsInt());
    Assertions.assertEquals(NativeBinder.ArgType.FP, rule[9].type());
  }

}
