EditLine Interface for Java
===========================

## Introduction

This library provides a Java (JNI) interface to the BSD `editline` library,
which is available on BSD systems and Mac OS X, and can be installed on
most Linux systems. `editline` is a replacement for the [GNU Readline][readline]
library, with a more liberal BSD-style license. Linking `editline` into your
code (and, similarly, using this Java interface to it) will not change the
license you have assigned to your code.

[readline]: http://tiswww.case.edu/php/chet/readline/rltop.html

## Restrictions

This package is only known to work on Unix-like operating systems. I have
tested it on:

- Linux (Ubuntu 9)
- FreeBSD 8
- Mac OS X (10.4)

## Building

After unpacking the source, change your working directory to `src`. The code uses GNU `make` to build. The `make` logic is split into two pieces:

* A platform-independent `Makefile`.
* Architecture-specific build definitions. These definitions are in the
  `Makedefs.*` files. The suffix for those files is determined by the
  `uname -s` command.

How to build:

* Type `uname -s` at the command line. If there's an existing `Makedefs` file
  for your platform, you're probably fine. (You may have to edit it, if there
  are errors.)
* Install the `editline` library, if necessary. This is *not* necessary on
  FreeBSD or Mac OS X. For Linux distributions, you can usually find an
  appropriate version of `editline` in your distro's repository. For example,
  for Ubuntu: `apt-get install libedit-dev`.
* Type `make`. If it's successfully, you'll get a shared library and a jar
  file.

## License and Copyright

This software is released under a BSD license, adapted from
[http://opensource.org/licenses/bsd-license.php][opensource-bsd].

[opensource-bsd]: http://opensource.org/licenses/bsd-license.php

Copyright &copy; 2010 Brian M. Clapper
All rights reserved.

Redistribution and use in source and binary forms, with or without
modification, are permitted provided that the following conditions are met:

* Redistributions of source code must retain the above copyright notice,
  this list of conditions and the following disclaimer.

* Redistributions in binary form must reproduce the above copyright notice,
  this list of conditions and the following disclaimer in the documentation
  and/or other materials provided with the distribution.

* Neither the names "clapper.org", "Grizzled Scala Library", nor the names
  of its contributors may be used to endorse or promote products derived
  from this software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
POSSIBILITY OF SUCH DAMAGE.

---

