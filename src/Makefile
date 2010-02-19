# ---------------------------------------------------------------------------
# Makefile for Java EditLine interface
#
# Copyright (c) 2010 Brian M. Clapper, bmc@clapper.org
# ---------------------------------------------------------------------------

PLATFORM     = $(shell uname -s)
CLASSDIR     = classes
CLASS_PKGDIR = $(CLASSDIR)/org/clapper/editline
CLASSES      = $(CLASS_PKGDIR)/EditLine.class

# ---------------------------------------------------------------------------
# Platform-specific stuff
# ---------------------------------------------------------------------------

include Makedefs.$(PLATFORM)

# ---------------------------------------------------------------------------

JAVAC    = $(JAVA_HOME)/bin/javac
JAVAH    = $(JAVA_HOME)/bin/javah
JAR      = $(JAVA_HOME)/bin/jar

# ---------------------------------------------------------------------------
# Targets
# ---------------------------------------------------------------------------

all: compile package

clean:
	rm -rf $(CLASSDIR) $(SOLIB) *.o \
        org_clapper_editline_*.h *.jar *.class

compile: java native

package: jar

jar: editline.jar

editline.jar: $(CLASSES)
	(cd $(CLASSDIR); $(JAR) cf ../editline.jar org)

java:	$(CLASSES)

native: $(SOLIB)

test: TestDriver.class

$(CLASS_PKGDIR)/EditLine.class: EditLine.java
	mkdir $(CLASSDIR)
	$(JAVAC) -d $(CLASSDIR) -cp $(CLASSDIR) EditLine.java

$(SOLIB): org_clapper_editline_EditLine.o
	$(CC) -o $(SOLIB) org_clapper_editline_EditLine.o $(LDFLAGS) 

org_clapper_editline_EditLine.o: org_clapper_editline_EditLine.c \
                                 org_clapper_editline_EditLine.h
	$(CC) $(INCLUDES) $(CFLAGS) -c org_clapper_editline_EditLine.c

org_clapper_editline_EditLine.h: $(CLASS_PKGDIR)/EditLine.class
	$(JAVAH) -classpath $(CLASSDIR) -jni org.clapper.editline.EditLine

TestDriver.class: TestDriver.java editline.jar
	$(JAVAC) -d . -cp $(CLASSDIR) TestDriver.java
