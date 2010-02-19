EditLine Interface for Java
===========================

## Introduction

This library provides a Java (JNI) interface to the [Editline][editline]
library, which is available on BSD systems and Mac OS X, and can be
installed on most Linux systems. Editline is a replacement for the [GNU
Readline][readline] library, with a more liberal BSD-style license. Linking
Editline into your code (and, similarly, using this Java interface to it)
will not change the license you have assigned to your code.

[readline]: http://tiswww.case.edu/php/chet/readline/rltop.html
[editline]: http://www.thrysoee.dk/editline/

## Using the Library

An `EditLine` object is instantiated via a factory method. The finalizer
for the returned object restores the terminal environment, if you don't do
it yourself--but you're better off doing it yourself. Here's the typical
usage pattern:

    import org.clapper.editline.EditLine

    public class MyProgram
    {
        public static void main(String[] args)
        {
            EditLine el = EditLine.init("myprogram")
            try
            {
                el.setPrompt("myprogram? ");
                el.setHistorySize(1024);

                String line;
                while ((line = el.getLine()) != null)
                {
                    // ...
                }

                System.exit(0);
            }
            finally
            {
                el.cleanup();
            }
        }
    }
        
            
## Restrictions

This package is only known to work on Unix-like operating systems. I have
tested it on:

- Linux (Ubuntu 9)
- FreeBSD 8
- Mac OS X (10.4)

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

After unpacking the source, change your working directory to `src`. The
code uses GNU `make` to build. The `make` logic is split into two pieces:

* A platform-independent `Makefile`.
* Architecture-specific build definitions. These definitions are in the
  `Makedefs.*` files. The suffix for those files is determined by the
  `uname -s` command.

How to build:

* Type `uname -s` at the command line. If there's an existing `Makedefs` file
  for your platform, you're probably fine. (You may have to edit it, if there
  are errors.)
* Install the Editline library, if necessary. This is *not* necessary on
  FreeBSD or Mac OS X. For Linux distributions, you can usually find an
  appropriate version of Editline in your distro's repository. For example,
  for Ubuntu: `apt-get install libedit-dev`.
* Type `make`. If it's successful, you'll get a shared library and a jar
  file.

## Deploying Java EditLine

Once you have succcessfully built Java EditLine:

- Ensure that the shared library (`libjavaeditline.jnilib` on the Mac,
  `libjavaeditline.so` on other systems) is in your LD_LIBRARY_PATH.
- Ensure that `javaeditline.jar` is in your CLASSPATH.

That should be all you need to do.

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

* Neither the names "clapper.org", "Java EditLine", nor the names of its
  contributors may be used to endorse or promote products derived from this
  software without specific prior written permission.

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

