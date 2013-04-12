Eclipse plugin for scripting using javascript.

Read more at http://eclipsescript.org/.

This plug-in currently uses a re-packaged version of rhino in js.jar. This was built from rhino master git on 2013-04-11 using:
	(1) Get the source (https://developer.mozilla.org/en-US/docs/Rhino/Download_Rhino?redirectlocale=en-US&redirectslug=RhinoDownload):
		git clone https://github.com/mozilla/rhino.git
		cd rhino
	(2) Change string references to class:
		perl -p -i -e 's/org\/mozilla\//org\/eclipsescript\/rhino\//g' `find ./ -name *.java`
	(3) Build:
		ant jar
	(4) 
		cp build/rhino1_7R5pre/js.jar $ECLIPSESCRIPT/js_orig.jar
		cd $ECLIPSESCRIPT
		ant -f build_js_with_jarjar.xml
	(5) Test the re-packaged rhino from shell with:
		java -cp js.jar org.eclipsescript.rhino.javascript.tools.shell.Main
