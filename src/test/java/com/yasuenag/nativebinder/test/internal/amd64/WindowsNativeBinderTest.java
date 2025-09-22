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
package com.yasuenag.nativebinder.test.internal.amd64;

import java.lang.reflect.Method;
import java.util.Arrays;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import com.yasuenag.ffmasm.amd64.Register;

import com.yasuenag.nativebinder.NativeBinder;
import com.yasuenag.nativebinder.internal.amd64.WindowsNativeBinder;


@EnabledOnOs(OS.WINDOWS)
public class WindowsNativeBinderTest extends WindowsNativeBinder{

  // Skeltons for test
  public void intManyArgs(boolean a1,
                          byte a2,
                          char a3,
                          short a4,
                          int a5,
                          long a6){}
  public void fpManyArgs(float a1,
                         double a2,
                         float a3,
                         double a4,
                         float a5,
                         double a6){}
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
                          double a12){}

  @Test
  public void testXmmVolatileRegister(){
    Assertions.assertEquals(Register.XMM4, xmmVolatileRegister());
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

    Assertions.assertEquals(6, rule.length);

    // arg1
    Assertions.assertEquals(Register.R8, rule[0].from());
    Assertions.assertTrue(rule[0].fromOffset().isEmpty());
    Assertions.assertEquals(Register.RCX, rule[0].to());
    Assertions.assertTrue(rule[0].toOffset().isEmpty());
    Assertions.assertEquals(NativeBinder.ArgType.INT, rule[0].type());

    // arg2
    Assertions.assertEquals(Register.R9, rule[1].from());
    Assertions.assertTrue(rule[1].fromOffset().isEmpty());
    Assertions.assertEquals(Register.RDX, rule[1].to());
    Assertions.assertTrue(rule[1].toOffset().isEmpty());
    Assertions.assertEquals(NativeBinder.ArgType.INT, rule[1].type());

    // arg3
    Assertions.assertEquals(Register.RSP, rule[2].from());
    Assertions.assertEquals(40, rule[2].fromOffset().getAsInt());
    Assertions.assertEquals(Register.R8, rule[2].to());
    Assertions.assertTrue(rule[2].toOffset().isEmpty());
    Assertions.assertEquals(NativeBinder.ArgType.INT, rule[2].type());

    // arg4
    Assertions.assertEquals(Register.RSP, rule[3].from());
    Assertions.assertEquals(48, rule[3].fromOffset().getAsInt());
    Assertions.assertEquals(Register.R9, rule[3].to());
    Assertions.assertTrue(rule[3].toOffset().isEmpty());
    Assertions.assertEquals(NativeBinder.ArgType.INT, rule[3].type());

    // arg5
    Assertions.assertEquals(Register.RSP, rule[4].from());
    Assertions.assertEquals(56, rule[4].fromOffset().getAsInt());
    Assertions.assertEquals(Register.RSP, rule[4].to());
    Assertions.assertEquals(40, rule[4].toOffset().getAsInt());
    Assertions.assertEquals(NativeBinder.ArgType.INT, rule[4].type());

    // arg6
    Assertions.assertEquals(Register.RSP, rule[5].from());
    Assertions.assertEquals(64, rule[5].fromOffset().getAsInt());
    Assertions.assertEquals(Register.RSP, rule[5].to());
    Assertions.assertEquals(48, rule[5].toOffset().getAsInt());
    Assertions.assertEquals(NativeBinder.ArgType.INT, rule[5].type());
  }

  @Test
  void testFPManyArgsWithJMP(){
    var targetMethod = getTargetMethod("fpManyArgs");
    var rule = createArgTransformRule(targetMethod, true);

    Assertions.assertEquals(6, rule.length);

    // arg1
    Assertions.assertEquals(Register.XMM2, rule[0].from());
    Assertions.assertTrue(rule[0].fromOffset().isEmpty());
    Assertions.assertEquals(Register.XMM0, rule[0].to());
    Assertions.assertTrue(rule[0].toOffset().isEmpty());
    Assertions.assertEquals(NativeBinder.ArgType.FP, rule[0].type());

    // arg2
    Assertions.assertEquals(Register.XMM3, rule[1].from());
    Assertions.assertTrue(rule[1].fromOffset().isEmpty());
    Assertions.assertEquals(Register.XMM1, rule[1].to());
    Assertions.assertTrue(rule[1].toOffset().isEmpty());
    Assertions.assertEquals(NativeBinder.ArgType.FP, rule[1].type());

    // arg3
    Assertions.assertEquals(Register.RSP, rule[2].from());
    Assertions.assertEquals(40, rule[2].fromOffset().getAsInt());
    Assertions.assertEquals(Register.XMM2, rule[2].to());
    Assertions.assertTrue(rule[2].toOffset().isEmpty());
    Assertions.assertEquals(NativeBinder.ArgType.FP, rule[2].type());

    // arg4
    Assertions.assertEquals(Register.RSP, rule[3].from());
    Assertions.assertEquals(48, rule[3].fromOffset().getAsInt());
    Assertions.assertEquals(Register.XMM3, rule[3].to());
    Assertions.assertTrue(rule[3].toOffset().isEmpty());
    Assertions.assertEquals(NativeBinder.ArgType.FP, rule[3].type());

    // arg5
    Assertions.assertEquals(Register.RSP, rule[4].from());
    Assertions.assertEquals(56, rule[4].fromOffset().getAsInt());
    Assertions.assertEquals(Register.RSP, rule[4].to());
    Assertions.assertEquals(40, rule[4].toOffset().getAsInt());
    Assertions.assertEquals(NativeBinder.ArgType.FP, rule[4].type());

    // arg6
    Assertions.assertEquals(Register.RSP, rule[5].from());
    Assertions.assertEquals(64, rule[5].fromOffset().getAsInt());
    Assertions.assertEquals(Register.RSP, rule[5].to());
    Assertions.assertEquals(48, rule[5].toOffset().getAsInt());
    Assertions.assertEquals(NativeBinder.ArgType.FP, rule[5].type());
  }

  @Test
  void testMixManyArgsWithJMP(){
    var targetMethod = getTargetMethod("mixManyArgs");
    var rule = createArgTransformRule(targetMethod, true);

    Assertions.assertEquals(12, rule.length);

    // arg1
    Assertions.assertEquals(Register.R8, rule[0].from());
    Assertions.assertTrue(rule[0].fromOffset().isEmpty());
    Assertions.assertEquals(Register.RCX, rule[0].to());
    Assertions.assertTrue(rule[0].toOffset().isEmpty());
    Assertions.assertEquals(NativeBinder.ArgType.INT, rule[0].type());

    // arg2
    Assertions.assertEquals(Register.XMM3, rule[1].from());
    Assertions.assertTrue(rule[1].fromOffset().isEmpty());
    Assertions.assertEquals(Register.XMM1, rule[1].to());
    Assertions.assertTrue(rule[1].toOffset().isEmpty());
    Assertions.assertEquals(NativeBinder.ArgType.FP, rule[1].type());

    // arg3
    Assertions.assertEquals(Register.RSP, rule[2].from());
    Assertions.assertEquals(40, rule[2].fromOffset().getAsInt());
    Assertions.assertEquals(Register.R8, rule[2].to());
    Assertions.assertTrue(rule[2].toOffset().isEmpty());
    Assertions.assertEquals(NativeBinder.ArgType.INT, rule[2].type());

    // arg4
    Assertions.assertEquals(Register.RSP, rule[3].from());
    Assertions.assertEquals(48, rule[3].fromOffset().getAsInt());
    Assertions.assertEquals(Register.XMM3, rule[3].to());
    Assertions.assertTrue(rule[3].toOffset().isEmpty());
    Assertions.assertEquals(NativeBinder.ArgType.FP, rule[3].type());

    // arg5
    Assertions.assertEquals(Register.RSP, rule[4].from());
    Assertions.assertEquals(56, rule[4].fromOffset().getAsInt());
    Assertions.assertEquals(Register.RSP, rule[4].to());
    Assertions.assertEquals(40, rule[4].toOffset().getAsInt());
    Assertions.assertEquals(NativeBinder.ArgType.INT, rule[4].type());

    // arg6
    Assertions.assertEquals(Register.RSP, rule[5].from());
    Assertions.assertEquals(64, rule[5].fromOffset().getAsInt());
    Assertions.assertEquals(Register.RSP, rule[5].to());
    Assertions.assertEquals(48, rule[5].toOffset().getAsInt());
    Assertions.assertEquals(NativeBinder.ArgType.FP, rule[5].type());

    // arg7
    Assertions.assertEquals(Register.RSP, rule[6].from());
    Assertions.assertEquals(72, rule[6].fromOffset().getAsInt());
    Assertions.assertEquals(Register.RSP, rule[6].to());
    Assertions.assertEquals(56, rule[6].toOffset().getAsInt());
    Assertions.assertEquals(NativeBinder.ArgType.INT, rule[6].type());

    // arg8
    Assertions.assertEquals(Register.RSP, rule[7].from());
    Assertions.assertEquals(80, rule[7].fromOffset().getAsInt());
    Assertions.assertEquals(Register.RSP, rule[7].to());
    Assertions.assertEquals(64, rule[7].toOffset().getAsInt());
    Assertions.assertEquals(NativeBinder.ArgType.FP, rule[7].type());

    // arg9
    Assertions.assertEquals(Register.RSP, rule[8].from());
    Assertions.assertEquals(88, rule[8].fromOffset().getAsInt());
    Assertions.assertEquals(Register.RSP, rule[8].to());
    Assertions.assertEquals(72, rule[8].toOffset().getAsInt());
    Assertions.assertEquals(NativeBinder.ArgType.INT, rule[8].type());

    // arg10
    Assertions.assertEquals(Register.RSP, rule[9].from());
    Assertions.assertEquals(96, rule[9].fromOffset().getAsInt());
    Assertions.assertEquals(Register.RSP, rule[9].to());
    Assertions.assertEquals(80, rule[9].toOffset().getAsInt());
    Assertions.assertEquals(NativeBinder.ArgType.FP, rule[9].type());

    // arg11
    Assertions.assertEquals(Register.RSP, rule[10].from());
    Assertions.assertEquals(104, rule[10].fromOffset().getAsInt());
    Assertions.assertEquals(Register.RSP, rule[10].to());
    Assertions.assertEquals(88, rule[10].toOffset().getAsInt());
    Assertions.assertEquals(NativeBinder.ArgType.INT, rule[10].type());

    // arg12
    Assertions.assertEquals(Register.RSP, rule[11].from());
    Assertions.assertEquals(112, rule[11].fromOffset().getAsInt());
    Assertions.assertEquals(Register.RSP, rule[11].to());
    Assertions.assertEquals(96, rule[11].toOffset().getAsInt());
    Assertions.assertEquals(NativeBinder.ArgType.FP, rule[11].type());
  }

  @Test
  void testIntManyArgsWithErrorCode(){
    var targetMethod = getTargetMethod("intManyArgs");
    var rule = createArgTransformRule(targetMethod, false);

    Assertions.assertEquals(6, rule.length);

    // arg1
    Assertions.assertEquals(Register.R8, rule[0].from());
    Assertions.assertTrue(rule[0].fromOffset().isEmpty());
    Assertions.assertEquals(Register.RCX, rule[0].to());
    Assertions.assertTrue(rule[0].toOffset().isEmpty());
    Assertions.assertEquals(NativeBinder.ArgType.INT, rule[0].type());

    // arg2
    Assertions.assertEquals(Register.R9, rule[1].from());
    Assertions.assertTrue(rule[1].fromOffset().isEmpty());
    Assertions.assertEquals(Register.RDX, rule[1].to());
    Assertions.assertTrue(rule[1].toOffset().isEmpty());
    Assertions.assertEquals(NativeBinder.ArgType.INT, rule[1].type());

    // arg3
    Assertions.assertEquals(Register.RBP, rule[2].from());
    Assertions.assertEquals(48, rule[2].fromOffset().getAsInt());
    Assertions.assertEquals(Register.R8, rule[2].to());
    Assertions.assertTrue(rule[2].toOffset().isEmpty());
    Assertions.assertEquals(NativeBinder.ArgType.INT, rule[2].type());

    // arg4
    Assertions.assertEquals(Register.RBP, rule[3].from());
    Assertions.assertEquals(56, rule[3].fromOffset().getAsInt());
    Assertions.assertEquals(Register.R9, rule[3].to());
    Assertions.assertTrue(rule[3].toOffset().isEmpty());
    Assertions.assertEquals(NativeBinder.ArgType.INT, rule[3].type());

    // arg5
    Assertions.assertEquals(Register.RBP, rule[4].from());
    Assertions.assertEquals(64, rule[4].fromOffset().getAsInt());
    Assertions.assertEquals(Register.RSP, rule[4].to());
    Assertions.assertEquals(32, rule[4].toOffset().getAsInt());
    Assertions.assertEquals(NativeBinder.ArgType.INT, rule[4].type());

    // arg6
    Assertions.assertEquals(Register.RBP, rule[5].from());
    Assertions.assertEquals(72, rule[5].fromOffset().getAsInt());
    Assertions.assertEquals(Register.RSP, rule[5].to());
    Assertions.assertEquals(40, rule[5].toOffset().getAsInt());
    Assertions.assertEquals(NativeBinder.ArgType.INT, rule[5].type());
  }

  @Test
  void testFPManyArgsWithErrorCode(){
    var targetMethod = getTargetMethod("fpManyArgs");
    var rule = createArgTransformRule(targetMethod, false);

    Assertions.assertEquals(6, rule.length);

    // arg1
    Assertions.assertEquals(Register.XMM2, rule[0].from());
    Assertions.assertTrue(rule[0].fromOffset().isEmpty());
    Assertions.assertEquals(Register.XMM0, rule[0].to());
    Assertions.assertTrue(rule[0].toOffset().isEmpty());
    Assertions.assertEquals(NativeBinder.ArgType.FP, rule[0].type());

    // arg2
    Assertions.assertEquals(Register.XMM3, rule[1].from());
    Assertions.assertTrue(rule[1].fromOffset().isEmpty());
    Assertions.assertEquals(Register.XMM1, rule[1].to());
    Assertions.assertTrue(rule[1].toOffset().isEmpty());
    Assertions.assertEquals(NativeBinder.ArgType.FP, rule[1].type());

    // arg3
    Assertions.assertEquals(Register.RBP, rule[2].from());
    Assertions.assertEquals(48, rule[2].fromOffset().getAsInt());
    Assertions.assertEquals(Register.XMM2, rule[2].to());
    Assertions.assertTrue(rule[2].toOffset().isEmpty());
    Assertions.assertEquals(NativeBinder.ArgType.FP, rule[2].type());

    // arg4
    Assertions.assertEquals(Register.RBP, rule[3].from());
    Assertions.assertEquals(56, rule[3].fromOffset().getAsInt());
    Assertions.assertEquals(Register.XMM3, rule[3].to());
    Assertions.assertTrue(rule[3].toOffset().isEmpty());
    Assertions.assertEquals(NativeBinder.ArgType.FP, rule[3].type());

    // arg5
    Assertions.assertEquals(Register.RBP, rule[4].from());
    Assertions.assertEquals(64, rule[4].fromOffset().getAsInt());
    Assertions.assertEquals(Register.RSP, rule[4].to());
    Assertions.assertEquals(32, rule[4].toOffset().getAsInt());
    Assertions.assertEquals(NativeBinder.ArgType.FP, rule[4].type());

    // arg6
    Assertions.assertEquals(Register.RBP, rule[5].from());
    Assertions.assertEquals(72, rule[5].fromOffset().getAsInt());
    Assertions.assertEquals(Register.RSP, rule[5].to());
    Assertions.assertEquals(40, rule[5].toOffset().getAsInt());
    Assertions.assertEquals(NativeBinder.ArgType.FP, rule[5].type());
  }

  @Test
  void testMixManyArgsWithErrorCode(){
    var targetMethod = getTargetMethod("mixManyArgs");
    var rule = createArgTransformRule(targetMethod, false);

    Assertions.assertEquals(12, rule.length);

    // arg1
    Assertions.assertEquals(Register.R8, rule[0].from());
    Assertions.assertTrue(rule[0].fromOffset().isEmpty());
    Assertions.assertEquals(Register.RCX, rule[0].to());
    Assertions.assertTrue(rule[0].toOffset().isEmpty());
    Assertions.assertEquals(NativeBinder.ArgType.INT, rule[0].type());

    // arg2
    Assertions.assertEquals(Register.XMM3, rule[1].from());
    Assertions.assertTrue(rule[1].fromOffset().isEmpty());
    Assertions.assertEquals(Register.XMM1, rule[1].to());
    Assertions.assertTrue(rule[1].toOffset().isEmpty());
    Assertions.assertEquals(NativeBinder.ArgType.FP, rule[1].type());

    // arg3
    Assertions.assertEquals(Register.RBP, rule[2].from());
    Assertions.assertEquals(48, rule[2].fromOffset().getAsInt());
    Assertions.assertEquals(Register.R8, rule[2].to());
    Assertions.assertTrue(rule[2].toOffset().isEmpty());
    Assertions.assertEquals(NativeBinder.ArgType.INT, rule[2].type());

    // arg4
    Assertions.assertEquals(Register.RBP, rule[3].from());
    Assertions.assertEquals(56, rule[3].fromOffset().getAsInt());
    Assertions.assertEquals(Register.XMM3, rule[3].to());
    Assertions.assertTrue(rule[3].toOffset().isEmpty());
    Assertions.assertEquals(NativeBinder.ArgType.FP, rule[3].type());

    // arg5
    Assertions.assertEquals(Register.RBP, rule[4].from());
    Assertions.assertEquals(64, rule[4].fromOffset().getAsInt());
    Assertions.assertEquals(Register.RSP, rule[4].to());
    Assertions.assertEquals(32, rule[4].toOffset().getAsInt());
    Assertions.assertEquals(NativeBinder.ArgType.INT, rule[4].type());

    // arg6
    Assertions.assertEquals(Register.RBP, rule[5].from());
    Assertions.assertEquals(72, rule[5].fromOffset().getAsInt());
    Assertions.assertEquals(Register.RSP, rule[5].to());
    Assertions.assertEquals(40, rule[5].toOffset().getAsInt());
    Assertions.assertEquals(NativeBinder.ArgType.FP, rule[5].type());

    // arg7
    Assertions.assertEquals(Register.RBP, rule[6].from());
    Assertions.assertEquals(80, rule[6].fromOffset().getAsInt());
    Assertions.assertEquals(Register.RSP, rule[6].to());
    Assertions.assertEquals(48, rule[6].toOffset().getAsInt());
    Assertions.assertEquals(NativeBinder.ArgType.INT, rule[6].type());

    // arg8
    Assertions.assertEquals(Register.RBP, rule[7].from());
    Assertions.assertEquals(88, rule[7].fromOffset().getAsInt());
    Assertions.assertEquals(Register.RSP, rule[7].to());
    Assertions.assertEquals(56, rule[7].toOffset().getAsInt());
    Assertions.assertEquals(NativeBinder.ArgType.FP, rule[7].type());

    // arg9
    Assertions.assertEquals(Register.RBP, rule[8].from());
    Assertions.assertEquals(96, rule[8].fromOffset().getAsInt());
    Assertions.assertEquals(Register.RSP, rule[8].to());
    Assertions.assertEquals(64, rule[8].toOffset().getAsInt());
    Assertions.assertEquals(NativeBinder.ArgType.INT, rule[8].type());

    // arg10
    Assertions.assertEquals(Register.RBP, rule[9].from());
    Assertions.assertEquals(104, rule[9].fromOffset().getAsInt());
    Assertions.assertEquals(Register.RSP, rule[9].to());
    Assertions.assertEquals(72, rule[9].toOffset().getAsInt());
    Assertions.assertEquals(NativeBinder.ArgType.FP, rule[9].type());

    // arg11
    Assertions.assertEquals(Register.RBP, rule[10].from());
    Assertions.assertEquals(112, rule[10].fromOffset().getAsInt());
    Assertions.assertEquals(Register.RSP, rule[10].to());
    Assertions.assertEquals(80, rule[10].toOffset().getAsInt());
    Assertions.assertEquals(NativeBinder.ArgType.INT, rule[10].type());

    // arg12
    Assertions.assertEquals(Register.RBP, rule[11].from());
    Assertions.assertEquals(120, rule[11].fromOffset().getAsInt());
    Assertions.assertEquals(Register.RSP, rule[11].to());
    Assertions.assertEquals(88, rule[11].toOffset().getAsInt());
    Assertions.assertEquals(NativeBinder.ArgType.FP, rule[11].type());
  }

}
