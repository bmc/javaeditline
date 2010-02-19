# ---------------------------------------------------------------------------
# Master Makefile
# ---------------------------------------------------------------------------

.SUFFIXES: .md .html

%.html: %.md
	(echo "<html><head><title>Java EditLine $*</title></head><body>" ;\
	 markdown < $< ;\
	 echo "</body></html>" ) > $@
	

all: source docs

source:
	$(MAKE) -C src all

# Depends on markdown being present.
docs: html apidocs

apidocs:
	$(MAKE) -C src docs

html: README.html LICENSE.html FAQ.html

README.html: README.md
LICENSE.html: LICENSE.md
FAQ.html: FAQ.md

clean:
	$(MAKE) -C src clean
