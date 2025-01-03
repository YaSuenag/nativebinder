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
  void testIntManyArgs(){
    var targetMethod = getTargetMethod("intManyArgs");
    var rule = createArgTransformRule(targetMethod);

    Assertions.assertEquals(8, rule.transformers().length);
    Assertions.assertEquals(16, rule.alignedArgStackSize());

    // arg1
    Assertions.assertEquals(Register.RDX, rule.transformers()[0].from());
    Assertions.assertTrue(rule.transformers()[0].fromOffset().isEmpty());
    Assertions.assertEquals(Register.RDI, rule.transformers()[0].to());
    Assertions.assertTrue(rule.transformers()[0].toOffset().isEmpty());
    Assertions.assertEquals(NativeBinder.ArgType.INT, rule.transformers()[0].type());

    // arg2
    Assertions.assertEquals(Register.RCX, rule.transformers()[1].from());
    Assertions.assertTrue(rule.transformers()[1].fromOffset().isEmpty());
    Assertions.assertEquals(Register.RSI, rule.transformers()[1].to());
    Assertions.assertTrue(rule.transformers()[1].toOffset().isEmpty());
    Assertions.assertEquals(NativeBinder.ArgType.INT, rule.transformers()[1].type());

    // arg3
    Assertions.assertEquals(Register.R8, rule.transformers()[2].from());
    Assertions.assertTrue(rule.transformers()[2].fromOffset().isEmpty());
    Assertions.assertEquals(Register.RDX, rule.transformers()[2].to());
    Assertions.assertTrue(rule.transformers()[2].toOffset().isEmpty());
    Assertions.assertEquals(NativeBinder.ArgType.INT, rule.transformers()[2].type());

    // arg4
    Assertions.assertEquals(Register.R9, rule.transformers()[3].from());
    Assertions.assertTrue(rule.transformers()[3].fromOffset().isEmpty());
    Assertions.assertEquals(Register.RCX, rule.transformers()[3].to());
    Assertions.assertTrue(rule.transformers()[3].toOffset().isEmpty());
    Assertions.assertEquals(NativeBinder.ArgType.INT, rule.transformers()[3].type());

    // arg5
    Assertions.assertEquals(Register.RBP, rule.transformers()[4].from());
    Assertions.assertEquals(16, rule.transformers()[4].fromOffset().getAsInt());
    Assertions.assertEquals(Register.R8, rule.transformers()[4].to());
    Assertions.assertTrue(rule.transformers()[4].toOffset().isEmpty());
    Assertions.assertEquals(NativeBinder.ArgType.INT, rule.transformers()[4].type());

    // arg6
    Assertions.assertEquals(Register.RBP, rule.transformers()[5].from());
    Assertions.assertEquals(24, rule.transformers()[5].fromOffset().getAsInt());
    Assertions.assertEquals(Register.R9, rule.transformers()[5].to());
    Assertions.assertTrue(rule.transformers()[5].toOffset().isEmpty());
    Assertions.assertEquals(NativeBinder.ArgType.INT, rule.transformers()[5].type());

    // arg7
    Assertions.assertEquals(Register.RBP, rule.transformers()[6].from());
    Assertions.assertEquals(32, rule.transformers()[6].fromOffset().getAsInt());
    Assertions.assertEquals(Register.RSP, rule.transformers()[6].to());
    Assertions.assertEquals(0, rule.transformers()[6].toOffset().getAsInt());
    Assertions.assertEquals(NativeBinder.ArgType.INT, rule.transformers()[6].type());

    // arg8
    Assertions.assertEquals(Register.RBP, rule.transformers()[7].from());
    Assertions.assertEquals(40, rule.transformers()[7].fromOffset().getAsInt());
    Assertions.assertEquals(Register.RSP, rule.transformers()[7].to());
    Assertions.assertEquals(8, rule.transformers()[7].toOffset().getAsInt());
    Assertions.assertEquals(NativeBinder.ArgType.INT, rule.transformers()[7].type());
  }

  @Test
  void testFPManyArgs(){
    var targetMethod = getTargetMethod("fpManyArgs");
    var rule = createArgTransformRule(targetMethod);

    Assertions.assertEquals(2, rule.transformers().length);
    Assertions.assertEquals(16, rule.alignedArgStackSize());

    // arg9
    Assertions.assertEquals(Register.RBP, rule.transformers()[0].from());
    Assertions.assertEquals(16, rule.transformers()[0].fromOffset().getAsInt());
    Assertions.assertEquals(Register.RSP, rule.transformers()[0].to());
    Assertions.assertEquals(0, rule.transformers()[0].toOffset().getAsInt());
    Assertions.assertEquals(NativeBinder.ArgType.FP, rule.transformers()[0].type());

    // arg10
    Assertions.assertEquals(Register.RBP, rule.transformers()[1].from());
    Assertions.assertEquals(24, rule.transformers()[1].fromOffset().getAsInt());
    Assertions.assertEquals(Register.RSP, rule.transformers()[1].to());
    Assertions.assertEquals(8, rule.transformers()[1].toOffset().getAsInt());
    Assertions.assertEquals(NativeBinder.ArgType.FP, rule.transformers()[1].type());
  }

  @Test
  void testMixManyArgs(){
    var targetMethod = getTargetMethod("mixManyArgs");
    var rule = createArgTransformRule(targetMethod);

    Assertions.assertEquals(10, rule.transformers().length);
    Assertions.assertEquals(32, rule.alignedArgStackSize());

    // arg1
    Assertions.assertEquals(Register.RDX, rule.transformers()[0].from());
    Assertions.assertTrue(rule.transformers()[0].fromOffset().isEmpty());
    Assertions.assertEquals(Register.RDI, rule.transformers()[0].to());
    Assertions.assertTrue(rule.transformers()[0].toOffset().isEmpty());
    Assertions.assertEquals(NativeBinder.ArgType.INT, rule.transformers()[0].type());

    // arg3
    Assertions.assertEquals(Register.RCX, rule.transformers()[1].from());
    Assertions.assertTrue(rule.transformers()[1].fromOffset().isEmpty());
    Assertions.assertEquals(Register.RSI, rule.transformers()[1].to());
    Assertions.assertTrue(rule.transformers()[1].toOffset().isEmpty());
    Assertions.assertEquals(NativeBinder.ArgType.INT, rule.transformers()[1].type());

    // arg5
    Assertions.assertEquals(Register.R8, rule.transformers()[2].from());
    Assertions.assertTrue(rule.transformers()[2].fromOffset().isEmpty());
    Assertions.assertEquals(Register.RDX, rule.transformers()[2].to());
    Assertions.assertTrue(rule.transformers()[2].toOffset().isEmpty());
    Assertions.assertEquals(NativeBinder.ArgType.INT, rule.transformers()[1].type());

    // arg7
    Assertions.assertEquals(Register.R9, rule.transformers()[3].from());
    Assertions.assertTrue(rule.transformers()[3].fromOffset().isEmpty());
    Assertions.assertEquals(Register.RCX, rule.transformers()[3].to());
    Assertions.assertTrue(rule.transformers()[3].toOffset().isEmpty());
    Assertions.assertEquals(NativeBinder.ArgType.INT, rule.transformers()[3].type());

    // arg9
    Assertions.assertEquals(Register.RBP, rule.transformers()[4].from());
    Assertions.assertEquals(16, rule.transformers()[4].fromOffset().getAsInt());
    Assertions.assertEquals(Register.R8, rule.transformers()[4].to());
    Assertions.assertTrue(rule.transformers()[4].toOffset().isEmpty());
    Assertions.assertEquals(NativeBinder.ArgType.INT, rule.transformers()[4].type());

    // arg11
    Assertions.assertEquals(Register.RBP, rule.transformers()[5].from());
    Assertions.assertEquals(24, rule.transformers()[5].fromOffset().getAsInt());
    Assertions.assertEquals(Register.R9, rule.transformers()[5].to());
    Assertions.assertTrue(rule.transformers()[5].toOffset().isEmpty());
    Assertions.assertEquals(NativeBinder.ArgType.INT, rule.transformers()[5].type());

    // arg13
    Assertions.assertEquals(Register.RBP, rule.transformers()[6].from());
    Assertions.assertEquals(32, rule.transformers()[6].fromOffset().getAsInt());
    Assertions.assertEquals(Register.RSP, rule.transformers()[6].to());
    Assertions.assertEquals(0, rule.transformers()[6].toOffset().getAsInt());
    Assertions.assertEquals(NativeBinder.ArgType.INT, rule.transformers()[6].type());

    // arg15
    Assertions.assertEquals(Register.RBP, rule.transformers()[7].from());
    Assertions.assertEquals(40, rule.transformers()[7].fromOffset().getAsInt());
    Assertions.assertEquals(Register.RSP, rule.transformers()[7].to());
    Assertions.assertEquals(8, rule.transformers()[7].toOffset().getAsInt());
    Assertions.assertEquals(NativeBinder.ArgType.INT, rule.transformers()[7].type());

    // arg17
    Assertions.assertEquals(Register.RBP, rule.transformers()[8].from());
    Assertions.assertEquals(48, rule.transformers()[8].fromOffset().getAsInt());
    Assertions.assertEquals(Register.RSP, rule.transformers()[8].to());
    Assertions.assertEquals(16, rule.transformers()[8].toOffset().getAsInt());
    Assertions.assertEquals(NativeBinder.ArgType.INT, rule.transformers()[8].type());

    // arg18
    Assertions.assertEquals(Register.RBP, rule.transformers()[9].from());
    Assertions.assertEquals(56, rule.transformers()[9].fromOffset().getAsInt());
    Assertions.assertEquals(Register.RSP, rule.transformers()[9].to());
    Assertions.assertEquals(24, rule.transformers()[9].toOffset().getAsInt());
    Assertions.assertEquals(NativeBinder.ArgType.FP, rule.transformers()[9].type());
  }

}
