Native Binder
===================

![CI result](../../actions/workflows/ci.yml/badge.svg)
![CodeQL](../../actions/workflows/codeql-analysis.yml/badge.svg)

Native Binder binds native (C) function to native method in Java without any C codes.

Native Binder generates stub code dynamically to remove top of 2 arguments - `JNIEnv` and `jobject`/`jclass`. Both are required by JNI specification, but they are not needed in native function. Thus stub code by Native Binder shuffles arguments and calls native function.

> [!IMPORTANT]
> `errno` cannot be obtained.

* Javadoc: https://yasuenag.github.io/nativebinder/
* Maven package: https://github.com/YaSuenag/nativebinder/packages/

# Requirements

* Java 22

# Supported platform

* Linux AMD64
* Windows x64 (experimental)

> [!CAUTION]
> Windows x64 support is experimental. It might not work especially number of arguments are more than 12.

# How to build

```
mvn package
```

# How to use

See [Javadoc](https://yasuenag.github.io/nativebinder/) and [examples](examples).

[getpid](examples/getpid) is the most simple example.

```java
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
```

# License

The GNU Lesser General Public License, version 3.0
