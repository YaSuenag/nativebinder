/*
 * Copyright (C) 2024, 2025, Yasumasa Suenaga
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
package com.yasuenag.nativebinder.test;

import java.lang.reflect.Method;
import java.util.Arrays;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;

import com.yasuenag.ffmasm.amd64.AMD64AsmBuilder;
import com.yasuenag.ffmasm.amd64.Register;

import com.yasuenag.nativebinder.NativeBinder;
import com.yasuenag.nativebinder.internal.LinuxNativeBinder;
import com.yasuenag.nativebinder.internal.WindowsNativeBinder;


public class NativeBinderTest extends NativeBinder{

  @Override
  protected Transformer[] createArgTransformRule(Method method){
    throw new RuntimeException("Not implemented");
  }

  @Override
  protected Register xmmVolatileRegister(){
    throw new RuntimeException("Not implemented");
  }

  @Test
  @EnabledOnOs(OS.LINUX)
  public void testGetInstanceOnLinux() throws Exception{
    var inst = NativeBinderTest.getInstance();
    Assertions.assertEquals(LinuxNativeBinder.class, inst.getClass());
  }

  @Test
  @EnabledOnOs(OS.WINDOWS)
  public void testGetInstanceOnWindows() throws Exception{
    var inst = NativeBinderTest.getInstance();
    Assertions.assertEquals(WindowsNativeBinder.class, inst.getClass());
  }

  @Test
  public void testIsIntegerClass(){
    Assertions.assertTrue(isIntegerClass(boolean.class));
    Assertions.assertTrue(isIntegerClass(byte.class));
    Assertions.assertTrue(isIntegerClass(char.class));
    Assertions.assertTrue(isIntegerClass(short.class));
    Assertions.assertTrue(isIntegerClass(int.class));
    Assertions.assertTrue(isIntegerClass(long.class));

    Assertions.assertFalse(isIntegerClass(float.class));
    Assertions.assertFalse(isIntegerClass(double.class));
    Assertions.assertFalse(isIntegerClass(Object.class));
  }

  @Test
  public void testIsFloatingPointClass(){
    Assertions.assertTrue(isFloatingPointClass(float.class));
    Assertions.assertTrue(isFloatingPointClass(double.class));

    Assertions.assertFalse(isFloatingPointClass(boolean.class));
    Assertions.assertFalse(isFloatingPointClass(byte.class));
    Assertions.assertFalse(isFloatingPointClass(char.class));
    Assertions.assertFalse(isFloatingPointClass(short.class));
    Assertions.assertFalse(isFloatingPointClass(int.class));
    Assertions.assertFalse(isFloatingPointClass(long.class));
    Assertions.assertFalse(isFloatingPointClass(Object.class));
  }

}
