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
package com.yasuenag.nativebinder.examples.getpid;

import java.lang.foreign.Linker;

import com.yasuenag.nativebinder.NativeBinder;


public class Main{

  public native int getpid();

  private void bind() throws Throwable{
    var method = this.getClass().getMethod("getpid");
    var p_getpid = Linker.nativeLinker()
                         .defaultLookup()
                         .find("getpid")
                         .get();

    var bindMethod = new NativeBinder.BindMethod(method, p_getpid);
    var bindMethods = new NativeBinder.BindMethod[]{bindMethod};
    var binder = NativeBinder.getInstance();
    binder.bind(this.getClass(), bindMethods);
  }

  public static void main(String[] args) throws Throwable{
    var inst = new Main();
    inst.bind();

    System.out.printf("PID from getpid():      %d\n", inst.getpid());
    System.out.printf("PID from ProcessHandle: %d\n", ProcessHandle.current().pid());
  }

}
