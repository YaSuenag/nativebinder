/*
 * Copyright (C) 2025 Yasumasa Suenaga
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
package com.yasuenag.nativebinder.examples.puts;

import java.lang.foreign.Arena;
import java.lang.foreign.Linker;
import java.lang.foreign.MemorySegment;

import com.yasuenag.nativebinder.NativeBinder;


public class Main{

  public native int puts(long s /* const char* */);

  private void bind() throws Throwable{
    var method = this.getClass().getMethod("puts", long.class);
    var p_puts = Linker.nativeLinker()
                       .defaultLookup()
                       .find("puts")
                       .get();

    var bindMethod = new NativeBinder.BindMethod(method, p_puts);
    var bindMethods = new NativeBinder.BindMethod[]{bindMethod};
    var binder = NativeBinder.getInstance();
    binder.bind(this.getClass(), bindMethods);
  }

  public static void main(String[] args) throws Throwable{
    var inst = new Main();
    inst.bind();

    try(var arena = Arena.ofConfined()){
      String str = "Call puts() from Java\n";
      MemorySegment c_str = arena.allocateFrom(str);
      inst.puts(c_str.address());
    }
  }

}
