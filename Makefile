# ---------------------------------------------------------------------------
# Master Makefile
# ---------------------------------------------------------------------------

all: source docs

source:
	$(MAKE) -C src all

# Depends on markdown being present.
docs:	README.html

README.html:	README.md
	(echo "<html><head><title>Java EditLine README</title></head><body>" ;\
	 markdown <README.md ;\
	 echo "</body></html>" ) >README.html

clean:
	$(MAKE) -C src clean
	rm -f README.html
