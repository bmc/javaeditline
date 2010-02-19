# ---------------------------------------------------------------------------
# Master Makefile
# ---------------------------------------------------------------------------

all: source docs

source:
	$(MAKE) -C src all

# Depends on markdown being present.
docs: html apidocs

apidocs:
	$(MAKE) -C src docs

html: README.html LICENSE.html

README.html: README.md
	(echo "<html><head><title>Java EditLine README</title></head><body>" ;\
	 markdown <README.md ;\
	 echo "</body></html>" ) >README.html

LICENSE.html: LICENSE.md
	(echo "<html><head><title>Java EditLine License</title></head><body>" ;\
	 markdown <LICENSE.md ;\
	 echo "</body></html>" ) >LICENSE.html

clean:
	$(MAKE) -C src clean
