PLATFORM     = $(shell uname -s)
CLASSDIR     = classes
CLASS_PKGDIR = $(CLASSDIR)/org/clapper/editline
CLASSES      = $(CLASS_PKGDIR)/EditLine.class

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
	rm -rf $(CLASSDIR) $(SOLIB) *.o *EditLine.h *.jar *.class

compile: java native

package: jar

jar: editline.jar

editline.jar: $(CLASSES)
	(cd $(CLASSDIR); $(JAR) cf ../editline.jar org)

java:	$(CLASSES)

native: $(SOLIB)

$(CLASS_PKGDIR)/EditLine.class: EditLine.java
	$(JAVAC) -d $(CLASSDIR) -cp $(CLASSDIR) EditLine.java

$(SOLIB): org_clapper_editline_EditLine.o
	$(CC) -o $(SOLIB) org_clapper_editline_EditLine.o $(LDFLAGS) 

org_clapper_editline_EditLine.o: org_clapper_editline_EditLine.c \
                                 org_clapper_editline_EditLine.h
	$(CC) $(INCLUDES) $(CFLAGS) -c org_clapper_editline_EditLine.c

org_clapper_editline_EditLine.h: $(CLASS_PKGDIR)/EditLine.class
	$(JAVAH) -classpath $(CLASSDIR) -jni org.clapper.editline.EditLine
