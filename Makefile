JAVAC    = $(JAVA_HOME)/bin/javac
JAVAH    = $(JAVA_HOME)/bin/javah
JAR      = $(JAVA_HOME)/bin/jar

# gcc
# Linux
CFLAGS   = -fPIC -O2 -pthread -D_REENTRANT
LDFLAGS = -z defs -Wl,-O1 -Wl,-soname=libEditLine.so -static-libgcc \
          -shared -mimpure-text -lc -ledit
SOLIB    = libEditLine.so
INCLUDES = -I$(JAVA_HOME)/include -I$(JAVA_HOME)/include/linux

# Darwin
#CFLAGS   = -fno-common
#LDFLAGS  = -dynamiclib -framework JavaVM \
#           -compatibility_version 1.0 -current_version 1.0 -ledit
#SOLIB    = libjeditline.1.0.dylib
#INCLUDES = -I$(JAVA_HOME)/Headers
#INCLUDES = -I$(JAVA_HOME)/include -I$(JAVA_HOME)/include/darwin

all: compile package

clean:
	rm -rf classes $(SOLIB) *.o *EditLine.h *.jar *.class

compile: java native

package:
	(cd classes; $(JAR) cf ../editline.jar org)

java:	classes

classes: EditLine.java LineInfo.java
	mkdir -p classes
	$(JAVAC) -d classes *.java

native: $(SOLIB)

$(SOLIB): org_clapper_editline_EditLine.c org_clapper_editline_EditLine.h
	$(CC) $(INCLUDES) $(CFLAGS) -c org_clapper_editline_EditLine.c
	$(CC) -o $(SOLIB) org_clapper_editline_EditLine.o $(LDFLAGS) 

org_clapper_editline_EditLine.h:
	$(JAVAH) -classpath classes -jni org.clapper.editline.EditLine
