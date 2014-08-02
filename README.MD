SCALA MANDELBROT VISUALIZER
===========================

Written as an exercise in learning Scala and experimenting with Akka, circa late 2013.

Uses Processing as the front-end library.

Also of potential interest is a minimal tweening library found in /src/leelib/tween.


PROJECT SETUP
-------------

Uses v2.10.2 of the Scala Standard Library and Scala compiler. 
It hasn't been tested against any later versions of the Scala compiler.

This is an IntelliJ IDEA project (using v13.1). If not using IntelliJ:

	- Link the jars in the libs/ 
	- Copy the 'data' folder (in the 'src' directory) to the output directory
	- The main class is mandelbrot.frontend.processing.App 

DEPLOYMENT
----------

If so inclined, the application JAR's can be built thru the IntelliJ project 
(Build > Build Artifact > All Artifacts > Build)

