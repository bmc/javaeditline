PLATFORM = $(shell uname -s)

ifeq "$(PLATFORM)" "Linux"

# gcc
# Linux
CFLAGS   = -fPIC -O2 -pthread -D_REENTRANT
LDFLAGS = -z defs -Wl,-O1 -Wl,-soname=libEditLine.so -static-libgcc \
          -shared -mimpure-text -lc -ledit
SOLIB    = libEditLine.so
INCLUDES = -I$(JAVA_HOME)/include -I$(JAVA_HOME)/include/linux

else
ifeq "$(PLATFORM)" "Darwin"

JAVA_HOME = /System/Library/Frameworks/JavaVM.framework/Versions/1.6.0/Home

CFLAGS   = -fno-common
LDFLAGS  = -dynamiclib -framework JavaVM -ledit
SOLIB    = libEditLine.jnilib
INCLUDES = -I$(JAVA_HOME)/include

endif
endif

JAVAC    = $(JAVA_HOME)/bin/javac
JAVAH    = $(JAVA_HOME)/bin/javah
JAR      = $(JAVA_HOME)/bin/jar

all: compile package

clean:
	rm -rf classes $(SOLIB) *.o *EditLine.h *.jar *.class

compile: java native

package:
	(cd classes; $(JAR) cf ../editline.jar org)

java:	classes

classes: classes/org/clapper/editline/LineInfo.class \
         classes/org/clapper/editline/EditLine.class

classes/org/clapper/editline/EditLine.class: EditLine.java
	$(JAVAC) -d classes -cp classes EditLine.java
classes/org/clapper/editline/LineInfo.class: LineInfo.java
	$(JAVAC) -d classes -cp classes LineInfo.java

native: $(SOLIB)

$(SOLIB): org_clapper_editline_EditLine.c org_clapper_editline_EditLine.h
	$(CC) $(INCLUDES) $(CFLAGS) -c org_clapper_editline_EditLine.c
	$(CC) -o $(SOLIB) org_clapper_editline_EditLine.o $(LDFLAGS) 

org_clapper_editline_EditLine.h: classes
	$(JAVAH) -classpath classes -jni org.clapper.editline.EditLine
