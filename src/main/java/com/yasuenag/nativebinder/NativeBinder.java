/*
 * Copyright (C) 2024, 2025, Yasumasa Suenaga
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
package com.yasuenag.nativebinder;

import java.lang.foreign.Arena;
import java.lang.foreign.FunctionDescriptor;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;
import java.lang.foreign.ValueLayout;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.ref.Cleaner;
import java.lang.reflect.Method;

import com.yasuenag.ffmasm.CodeSegment;
import com.yasuenag.ffmasm.PlatformException;
import com.yasuenag.ffmasm.UnsupportedPlatformException;

import com.yasuenag.nativebinder.internal.AMD64NativeBinder;


/**
 * Class for binding C functions to Java native methods.
 * JNI function needs JNIEnv and jobject or jclass, they are set in
 * 1st and 2nd argument in JNI function. They should be removed native
 * function call.
 * NativeBinder transforms the order of arguments to remove them.
 *
 * @author Yasumasa Suenaga
 */
public abstract class NativeBinder{

  /**
   * Record to store method information.
   *
   * @param method native (JNI) method to bind.
   * @param seg MemorySegment to hold C function pointer.
   */
  public static record BindMethod(Method method, MemorySegment seg){};

  /**
   * Argument type
   */
  protected static enum ArgType{
    /**
     * Integer (includes boolean and char)
     */
    INT,

    /**
     * Floating point
     */
    FP;
  }

  protected static CodeSegment seg = null;

  /**
   * Function pointer of errorCodeCallback()
   */
  protected static MemorySegment ptrErrorCodeCallback = null;

  private static final ThreadLocal<Integer> threadLocalErrorCode = new ThreadLocal<>();

  private static void errorCodeCallback(int errcode){
    threadLocalErrorCode.set(errcode);
  }

  /**
   * Get error code in previous bind method call.
   * Note that the method should be binded by bindWithErrorCode().
   *
   * @return error code in previous binded method call
   */
  public static int errorCodeInPreviousCall(){
    return threadLocalErrorCode.get();
  }

  private static void init() throws PlatformException, UnsupportedPlatformException{
    if(seg == null){
      seg = new CodeSegment();
      var action = new CodeSegment.CleanerAction(seg);
      Cleaner.create()
             .register(NativeBinder.class, action);
    }

    if(ptrErrorCodeCallback == null){
      try{
        var target = MethodHandles.lookup()
                                  .findStatic(NativeBinder.class, "errorCodeCallback", MethodType.methodType(void.class, int.class));
        var desc = FunctionDescriptor.ofVoid(ValueLayout.JAVA_INT);
        ptrErrorCodeCallback = Linker.nativeLinker()
                                     .upcallStub(target, desc, Arena.ofAuto());
      }
      catch(NoSuchMethodException | IllegalAccessException e){
        throw new RuntimeException(e);
      }
    }
  }

  /**
   * Get NativeBinder instance.
   *
   * @return NativeBinder instance
   * @throws UnsupportedPlatformException thrown when the platform is not
   *         supported by NativeBinder.
   */
  public static NativeBinder getInstance() throws PlatformException, UnsupportedPlatformException{
    init();

    var arch = System.getProperty("os.arch");
    if(arch.equals("amd64")){
      return AMD64NativeBinder.getInstance();
    }
    throw new UnsupportedPlatformException(arch);
  }

  /**
   * Check integer class or not.
   *
   * @param cls to check
   * @return true if integer class
   */
  protected boolean isIntegerClass(Class<?> cls){
    return cls.equals(boolean.class) ||
           cls.equals(byte.class) ||
           cls.equals(char.class) ||
           cls.equals(short.class) ||
           cls.equals(int.class) ||
           cls.equals(long.class);
  }

  /**
   * Check floating point class or not.
   *
   * @param cls to check
   * @return true if floating point class
   */
  protected boolean isFloatingPointClass(Class<?> cls){
    return cls.equals(float.class) || cls.equals(double.class);
  }

  /**
   * Bind C functions to JNI methods.
   *
   * @param targetClass to hold JNI methods
   * @param bindMethods array of binding information
   */
  public abstract void bind(Class<?> targetClass, BindMethod[] bindMethods) throws Throwable;

  /**
   * Bind C functions to JNI methods.
   * Error code (errno in Linux, GetLastError() in Windows) can be obtained.
   *
   * @param targetClass to hold JNI methods
   * @param bindMethods array of binding information
   */
  public abstract void bindWithErrorCode(Class<?> targetClass, BindMethod[] bindMethods) throws Throwable;

}
