package com.yasuenag.nativebinder.test.internal;

import java.lang.reflect.Method;
import java.util.Arrays;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import com.yasuenag.ffmasm.amd64.Register;

import com.yasuenag.nativebinder.NativeBinder;
import com.yasuenag.nativebinder.internal.WindowsNativeBinder;


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
  void testIntManyArgs(){
    var targetMethod = getTargetMethod("intManyArgs");
    var rule = createArgTransformRule(targetMethod);

    Assertions.assertEquals(8, rule.transformers().length);
    Assertions.assertEquals(48, rule.alignedArgStackSize());

    // arg1
    Assertions.assertEquals(Register.R8, rule.transformers()[0].from());
    Assertions.assertTrue(rule.transformers()[0].fromOffset().isEmpty());
    Assertions.assertEquals(Register.RCX, rule.transformers()[0].to());
    Assertions.assertTrue(rule.transformers()[0].toOffset().isEmpty());
    Assertions.assertEquals(NativeBinder.ArgType.INT, rule.transformers()[0].type());

    // arg2
    Assertions.assertEquals(Register.R9, rule.transformers()[2].from());
    Assertions.assertTrue(rule.transformers()[2].fromOffset().isEmpty());
    Assertions.assertEquals(Register.RDX, rule.transformers()[2].to());
    Assertions.assertTrue(rule.transformers()[2].toOffset().isEmpty());
    Assertions.assertEquals(NativeBinder.ArgType.INT, rule.transformers()[2].type());

    // arg3
    Assertions.assertEquals(Register.RBP, rule.transformers()[4].from());
    Assertions.assertEquals(48, rule.transformers()[4].fromOffset().getAsInt());
    Assertions.assertEquals(Register.R8, rule.transformers()[4].to());
    Assertions.assertTrue(rule.transformers()[4].toOffset().isEmpty());
    Assertions.assertEquals(NativeBinder.ArgType.INT, rule.transformers()[4].type());

    // arg4
    Assertions.assertEquals(Register.RBP, rule.transformers()[5].from());
    Assertions.assertEquals(56, rule.transformers()[5].fromOffset().getAsInt());
    Assertions.assertEquals(Register.R9, rule.transformers()[5].to());
    Assertions.assertTrue(rule.transformers()[5].toOffset().isEmpty());
    Assertions.assertEquals(NativeBinder.ArgType.INT, rule.transformers()[5].type());

    // arg5
    Assertions.assertEquals(Register.RBP, rule.transformers()[6].from());
    Assertions.assertEquals(64, rule.transformers()[6].fromOffset().getAsInt());
    Assertions.assertEquals(Register.RSP, rule.transformers()[6].to());
    Assertions.assertEquals(32, rule.transformers()[6].toOffset().getAsInt());
    Assertions.assertEquals(NativeBinder.ArgType.INT, rule.transformers()[6].type());

    // arg6
    Assertions.assertEquals(Register.RBP, rule.transformers()[7].from());
    Assertions.assertEquals(72, rule.transformers()[7].fromOffset().getAsInt());
    Assertions.assertEquals(Register.RSP, rule.transformers()[7].to());
    Assertions.assertEquals(40, rule.transformers()[7].toOffset().getAsInt());
    Assertions.assertEquals(NativeBinder.ArgType.INT, rule.transformers()[7].type());
  }

  @Test
  void testFPManyArgs(){
    var targetMethod = getTargetMethod("fpManyArgs");
    var rule = createArgTransformRule(targetMethod);

    Assertions.assertEquals(8, rule.transformers().length);
    Assertions.assertEquals(48, rule.alignedArgStackSize());

    // arg1
    Assertions.assertEquals(Register.XMM2, rule.transformers()[0].from());
    Assertions.assertTrue(rule.transformers()[0].fromOffset().isEmpty());
    Assertions.assertEquals(Register.XMM0, rule.transformers()[0].to());
    Assertions.assertTrue(rule.transformers()[0].toOffset().isEmpty());
    Assertions.assertEquals(NativeBinder.ArgType.FP, rule.transformers()[0].type());

    // arg2
    Assertions.assertEquals(Register.XMM3, rule.transformers()[2].from());
    Assertions.assertTrue(rule.transformers()[2].fromOffset().isEmpty());
    Assertions.assertEquals(Register.XMM1, rule.transformers()[2].to());
    Assertions.assertTrue(rule.transformers()[2].toOffset().isEmpty());
    Assertions.assertEquals(NativeBinder.ArgType.FP, rule.transformers()[2].type());

    // arg3
    Assertions.assertEquals(Register.RBP, rule.transformers()[4].from());
    Assertions.assertEquals(48, rule.transformers()[4].fromOffset().getAsInt());
    Assertions.assertEquals(Register.XMM2, rule.transformers()[4].to());
    Assertions.assertTrue(rule.transformers()[4].toOffset().isEmpty());
    Assertions.assertEquals(NativeBinder.ArgType.FP, rule.transformers()[4].type());

    // arg4
    Assertions.assertEquals(Register.RBP, rule.transformers()[5].from());
    Assertions.assertEquals(56, rule.transformers()[5].fromOffset().getAsInt());
    Assertions.assertEquals(Register.XMM3, rule.transformers()[5].to());
    Assertions.assertTrue(rule.transformers()[5].toOffset().isEmpty());
    Assertions.assertEquals(NativeBinder.ArgType.FP, rule.transformers()[5].type());

    // arg5
    Assertions.assertEquals(Register.RBP, rule.transformers()[6].from());
    Assertions.assertEquals(64, rule.transformers()[6].fromOffset().getAsInt());
    Assertions.assertEquals(Register.RSP, rule.transformers()[6].to());
    Assertions.assertEquals(32, rule.transformers()[6].toOffset().getAsInt());
    Assertions.assertEquals(NativeBinder.ArgType.FP, rule.transformers()[6].type());

    // arg6
    Assertions.assertEquals(Register.RBP, rule.transformers()[7].from());
    Assertions.assertEquals(72, rule.transformers()[7].fromOffset().getAsInt());
    Assertions.assertEquals(Register.RSP, rule.transformers()[7].to());
    Assertions.assertEquals(40, rule.transformers()[7].toOffset().getAsInt());
    Assertions.assertEquals(NativeBinder.ArgType.FP, rule.transformers()[7].type());
  }

  @Test
  void testMixManyArgs(){
    var targetMethod = getTargetMethod("mixManyArgs");
    var rule = createArgTransformRule(targetMethod);

    Assertions.assertEquals(14, rule.transformers().length);
    Assertions.assertEquals(96, rule.alignedArgStackSize());

    // arg1
    Assertions.assertEquals(Register.R8, rule.transformers()[0].from());
    Assertions.assertTrue(rule.transformers()[0].fromOffset().isEmpty());
    Assertions.assertEquals(Register.RCX, rule.transformers()[0].to());
    Assertions.assertTrue(rule.transformers()[0].toOffset().isEmpty());
    Assertions.assertEquals(NativeBinder.ArgType.INT, rule.transformers()[0].type());

    // arg2
    Assertions.assertEquals(Register.XMM3, rule.transformers()[2].from());
    Assertions.assertTrue(rule.transformers()[2].fromOffset().isEmpty());
    Assertions.assertEquals(Register.XMM1, rule.transformers()[2].to());
    Assertions.assertTrue(rule.transformers()[2].toOffset().isEmpty());
    Assertions.assertEquals(NativeBinder.ArgType.FP, rule.transformers()[2].type());

    // arg3
    Assertions.assertEquals(Register.RBP, rule.transformers()[4].from());
    Assertions.assertEquals(48, rule.transformers()[4].fromOffset().getAsInt());
    Assertions.assertEquals(Register.R8, rule.transformers()[4].to());
    Assertions.assertTrue(rule.transformers()[4].toOffset().isEmpty());
    Assertions.assertEquals(NativeBinder.ArgType.INT, rule.transformers()[4].type());

    // arg4
    Assertions.assertEquals(Register.RBP, rule.transformers()[5].from());
    Assertions.assertEquals(56, rule.transformers()[5].fromOffset().getAsInt());
    Assertions.assertEquals(Register.XMM3, rule.transformers()[5].to());
    Assertions.assertTrue(rule.transformers()[5].toOffset().isEmpty());
    Assertions.assertEquals(NativeBinder.ArgType.FP, rule.transformers()[5].type());

    // arg5
    Assertions.assertEquals(Register.RBP, rule.transformers()[6].from());
    Assertions.assertEquals(64, rule.transformers()[6].fromOffset().getAsInt());
    Assertions.assertEquals(Register.RSP, rule.transformers()[6].to());
    Assertions.assertEquals(32, rule.transformers()[6].toOffset().getAsInt());
    Assertions.assertEquals(NativeBinder.ArgType.INT, rule.transformers()[6].type());

    // arg6
    Assertions.assertEquals(Register.RBP, rule.transformers()[7].from());
    Assertions.assertEquals(72, rule.transformers()[7].fromOffset().getAsInt());
    Assertions.assertEquals(Register.RSP, rule.transformers()[7].to());
    Assertions.assertEquals(40, rule.transformers()[7].toOffset().getAsInt());
    Assertions.assertEquals(NativeBinder.ArgType.FP, rule.transformers()[7].type());

    // arg7
    Assertions.assertEquals(Register.RBP, rule.transformers()[8].from());
    Assertions.assertEquals(80, rule.transformers()[8].fromOffset().getAsInt());
    Assertions.assertEquals(Register.RSP, rule.transformers()[8].to());
    Assertions.assertEquals(48, rule.transformers()[8].toOffset().getAsInt());
    Assertions.assertEquals(NativeBinder.ArgType.INT, rule.transformers()[8].type());

    // arg8
    Assertions.assertEquals(Register.RBP, rule.transformers()[9].from());
    Assertions.assertEquals(88, rule.transformers()[9].fromOffset().getAsInt());
    Assertions.assertEquals(Register.RSP, rule.transformers()[9].to());
    Assertions.assertEquals(56, rule.transformers()[9].toOffset().getAsInt());
    Assertions.assertEquals(NativeBinder.ArgType.FP, rule.transformers()[9].type());

    // arg9
    Assertions.assertEquals(Register.RBP, rule.transformers()[10].from());
    Assertions.assertEquals(96, rule.transformers()[10].fromOffset().getAsInt());
    Assertions.assertEquals(Register.RSP, rule.transformers()[10].to());
    Assertions.assertEquals(64, rule.transformers()[10].toOffset().getAsInt());
    Assertions.assertEquals(NativeBinder.ArgType.INT, rule.transformers()[10].type());

    // arg10
    Assertions.assertEquals(Register.RBP, rule.transformers()[11].from());
    Assertions.assertEquals(104, rule.transformers()[11].fromOffset().getAsInt());
    Assertions.assertEquals(Register.RSP, rule.transformers()[11].to());
    Assertions.assertEquals(72, rule.transformers()[11].toOffset().getAsInt());
    Assertions.assertEquals(NativeBinder.ArgType.FP, rule.transformers()[11].type());

    // arg11
    Assertions.assertEquals(Register.RBP, rule.transformers()[12].from());
    Assertions.assertEquals(112, rule.transformers()[12].fromOffset().getAsInt());
    Assertions.assertEquals(Register.RSP, rule.transformers()[12].to());
    Assertions.assertEquals(80, rule.transformers()[12].toOffset().getAsInt());
    Assertions.assertEquals(NativeBinder.ArgType.INT, rule.transformers()[12].type());

    // arg12
    Assertions.assertEquals(Register.RBP, rule.transformers()[13].from());
    Assertions.assertEquals(120, rule.transformers()[13].fromOffset().getAsInt());
    Assertions.assertEquals(Register.RSP, rule.transformers()[13].to());
    Assertions.assertEquals(88, rule.transformers()[13].toOffset().getAsInt());
    Assertions.assertEquals(NativeBinder.ArgType.FP, rule.transformers()[13].type());
  }

}
