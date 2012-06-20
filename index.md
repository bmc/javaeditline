---
title: Java EditLine, an EditLine Interface for Java
layout: withTOC
---

## Introduction

Java EditLine provides a Java (JNI) interface to the [Editline][] library,
which is available on BSD systems and Mac OS X, and can be installed on
most Linux systems. Editline is a replacement for the [GNU Readline][]
library, with a more liberal BSD-style license. Linking Editline into your
code (and, similarly, using this Java interface to it) will not change the
license you have assigned to your software. You can use Editline and Java
EditLine in open source software (even GNU-licensed software), as well as
commercial software.

[GNU Readline]: http://tiswww.case.edu/php/chet/readline/rltop.html
[Editline]: http://www.thrysoee.dk/editline/

## Getting Java Editline

Java EditLine relies on the presence of the [Editline][] library, which
isn't automatically present on all operating systems. In addition, Java
EditLine uses the [Java Native Interface][] (JNI) to bridge Java to the
C-language Editline library; portions of the Java EditLine are written in
C.

It is impractical to provide binaries of Java EditLine for every
combination of Unix-like operating system and operating system release. So,
currently, you must build Java EditLine from source code, as described
below.

[Java Native Interface]: http://java.sun.com/docs/books/jni/

There are two ways to get the source code:

* Download a release (zip file or tarball) from the [downloads page][].
* Clone a copy of the Git repository:

        $ git clone git://github.com/bmc/javaeditline.git
        $ git clone http://github.com/bmc/javaeditline.git

[downloads page]: http://github.com/bmc/javaeditline/downloads

## Restrictions

This package is only known to work on Unix-like operating systems. I have
tested it on:

- Ubuntu Linux 9 and 10, with the [OpenJDK 6][openjdk] and the Oracle (Sun)
  Java 6 JDKs.
- FreeBSD 8.0-RELEASE, with the Diablo 1.6.0 JDK
- Mac OS X 10.4 (Tiger) and 10.6 (Snow Leopard) with the
  [SoyLatte Java 6][soylatte], [OpenJDK 6][openjdk], and Apple Java 6 JDKs.

[soylatte]: http://landonf.bikemonkey.org/static/soylatte/
[openjdk]: http://www.openjdk.org/

This Java wrapper does not expose all the functionality of the underlying
Editline library.

* This wrapper does not support the use of alternate file descriptors. All
  Editline instances use standard input, output and error. While this
  library theoretically permits multiple Editline instances to be
  constructed, all such instances use the same file descriptors (which
  limits the utility of having multiple instances). In practice, this is
  generally not a problem.

* This class does not currently expose the Editline library's tokenizer
  functionality (e.g., the `tok_init()`, `tok_line()`, `tok_str()`,
  `tok_reset()` and `tok_end()` functions).

* Signal handling is currently omitted, as it doesn't play well with the JVM.

* Certain Editline functions are not currently exposed, among them:

  - `el_insertstr()`
  - `el_deletestr()`
  - `el_set()`
  - `el_get()`
  - `el_getc()`
  - `el_reset()`
  - `el_push()`

## Building Java EditLine

### Overview

The code uses [GNU Make][] to build. On Linux systems and Mac OS X, GNU
Make is the standard version of *make*(1). On BSD systems, such as
[FreeBSD][], you'll have to install it separately. (On most BSD systems,
GNU Make is available as the *gmake* port or package.)

The *make* logic is split into two pieces:

* A platform-independent `Makefile`.
* Architecture-specific build definitions. These definitions are in the
  `Makedefs.*` files. The suffix for those files is determined by the
  `uname -s` command. There are existing definitions files for Darwin
  (Mac OS X), FreeBSD, and Linux.

[GNU Make]: http://www.gnu.org/software/make/
[FreeBSD]: http://www.freebsd.org/

### Building

* After unpacking the source, change your working directory to
  `javareadline/src`.
* Type `uname -s` at the command line. If there's an existing `Makedefs` file
  for your platform, you're probably fine. (You may have to edit it, if there
  are errors.)
* Install the Editline library, if necessary. This is *not* necessary on
  FreeBSD or Mac OS X. For Linux distributions, you can usually find an
  appropriate version of Editline in your distro's repository. For example,
  for Ubuntu, you can install it with: `apt-get install libedit-dev`.
* On FreeBSD, make sure GNU Make is installed.
* Type `make` (`gmake` on FreeBSD). If it's successful, you'll get a
  `libjavaeditline.so` shared library (`libjavaeditline.jnilib` on the Mac)
  and a `javaeditline.jar` jar file.

## Deploying Java EditLine

Once you have succcessfully built Java EditLine:

- Ensure that the directory containing the shared library is in your
  `LD_LIBRARY_PATH`.
- Ensure that `javaeditline.jar` is in your CLASSPATH.

That should be all you need to do.

**MAC OS X only**

On Mac OS X, using `LD_LIBRARY_PATH` may not suffice. You may need to set
`DYLD_LIBRARY_PATH`, instead. For instance, suppose you copied 
`libjavaeditline.jnilib` to `$HOME/lib`. You may need to add this environment
variable, to get Java to see it:

    export DYLD_LIBRARY_PATH=$HOME/lib

This wrinkle is also noted in the [FAQ][].

## Frequently Asked Questions

Please see the [FAQ][].

[FAQ]: faq.html

## License and Copyright

This software is released under a BSD license. See the accompanying
[license file][] for complete details.

## Patches

I gladly accept patches from their original authors. Feel free to email
patches to me or to fork the [GitHub repository][] and send me a pull
request. Along with any patch you send:

* Please state that the patch is your original work.
* Please indicate that you license the work to the Java EditLine
  project under a [BSD License][license file].

[GitHub repository]: http://github.com/bmc/javaeditline
[license file]: license.html

