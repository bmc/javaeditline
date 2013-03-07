Java EditLine FAQ
=================

**Why not just use [Java Readline][javareadline]?**

For several reasons.

* Java Readline doesn't expose enough information for completion. It exposes
  the token being completed and the current line, but it doesn't expose the
  *location* of the token. In applications that do context-sensitive
  completion, not knowing the location of the token is a problem.

* Java Readline has not been updated since 2003.

---

**Well, why don't you fix Java Readline, instead?**

Because I wanted to start fresh, with a new API.

---

**What about [JLine][jline] or [Jline2][jline2]?**

JLine is certainly useful, especially since it works on Unix-like systems
and on Windows. However, it isn't as functionally complete as Editline. For
instance, it isn't as configurable, and it doesn't support breadth of
editing commands that Editline supports. JLine2 is a substantial improvement
over JLine, but it's still not as feature-rich as GNU Readline or BSD's
*libedit*.

---

**Why use [Editline][editline], instead of [GNU Readline][readline]?**

Largely because of licensing. Editline is BSD-licensed, whereas GNU
Readline is GNU-licensed. The BSD license is more liberal.

[javareadline]: http://java-readline.sourceforge.net/
[jline]: http://jline.sourceforge.net/
[jline2]: https://github.com/jline/jline2
[readline]: http://tiswww.case.edu/php/chet/readline/rltop.html
[editline]: http://www.thrysoee.dk/editline/
