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
package com.yasuenag.nativebinder.examples.getauxval;

import java.lang.foreign.Linker;

import com.yasuenag.nativebinder.NativeBinder;


public class Main{

  // from /usr/include/linux/auxvec.h
  public static final long AT_BASE = 7;

  public native long getauxval(long type);

  private void bind() throws Throwable{
    var method = this.getClass().getMethod("getauxval", long.class);
    var p_getauxval = Linker.nativeLinker()
                            .defaultLookup()
                            .find("getauxval")
                            .get();

    var bindMethod = new NativeBinder.BindMethod(method, p_getauxval);
    var bindMethods = new NativeBinder.BindMethod[]{bindMethod};
    var binder = NativeBinder.getInstance();
    binder.bind(this.getClass(), bindMethods);
  }

  public static void main(String[] args) throws Throwable{
    var inst = new Main();
    inst.bind();

    var base = inst.getauxval(AT_BASE);
    System.out.printf("Base address: 0x%x\n", base);
  }

}
