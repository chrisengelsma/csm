A set of Java packages created by Chris Engelsma.
Applications dwell in the realm of graphics, visualization and image
processing.

(1) Getting the source code
----------------------------

To build CAE from source, you must first use git to check the repository
out from GitHub (http://www.github.com/chrisengelsma/csm)

bin/ - platform-dependent scripts (for running demos)
src/ - source code files (e.g., main/java/cae/paint/Painting3.java)

(2) Tools for building
----------------------

To build CAE, you need these freely available tools:
* Java SE JDK 6.0 (or later):
  http://www.oracle.com/technetwork/java/javase/downloads
* Gradle 2.3
  http://gradle.org
* Mines Java Toolkit (JTK)
  http://www.github.com/dhale/jtk
    Managed by Dave Hale and others at the Colorado School of Mines.
    CAE strongly relies on packages contained within this toolkit.
    It may be checked out and built via SVN. For further instructions on how 
    to build and run the applications contained within the JTK please refer to
    http://boole.mines.edu/jtk/trunk/readme.txt

(3) Building CAE
----------------

Navigate to the top directory and run `gradlew build`.

(4) Using CAE
-------------

After you have built CAE, you should have a JAR file 
[...]/build/libs/cae.jar.
You may include this JAR file as a classpath when running Java.

Some packages (e.g. cae.paint) require Java native interface (JNI) libraries
of native (non-Java) code. These platform-specific libraries should have come
with the Mines JTK and are located in jtk/trunk/lib, such as
jtk/trunk/lib/linux/x86.

To use CAE, we must launch a Java virtual machine, specifying all of these
JAR files and the locations of our JNI libraries. Provided in cae/trunk/bin
are scripts (e.g. paintdemo.sh) that illustrate how we do this for different 
platforms. Similarly, provided with the Mines JTK in jtk/trunk/bin are similar
scripts. To enable CAE, we must add cae.jar to the classpath. This can be
done using a scripting language such as sh or csh as follows:

Using bash, sh or ksh (Unix/Mac):

```bash
export CAE_HOME=/directories/to/cae/trunk
export CLASSPATH=\
$CAE_HOME/build/jar/cae.jar:\
.
```

Using csh or tcsh (Unix/Mac):

```csh
setenv CAE_HOME /directories/to/cae/trunk
setenv CLASSPATH ${CAE_HOME}/build/jar/cae.jar:
# more jars here
setenv CLASSPATH ${CLASSPATH}:.
```

Using a batch file (Windows):

```bat
set CAE_HOME=C:\path\to\cae\trunk
set CLASSPATH=^
%CAE_HOME%\build\jar\cae.jar;^
.
```

By adding the cae.jar to the classpath this allows you to call classes that
are included in CAE. 

(5) 3-D graphics in CAE
-----------------------

The packages used for 3-D graphics are built on JOGL, a java binding for the
OpenGL API. Like the Mines JTK, JOGL provides both JAR files and JNI
libraries; and the JNI libraries are platform-specific. In the future, JOGL is
likely to become part of standard Java Runtime Environment. Until then, the
JAR files and JNI libraries for JOGL are provided with the Mines JTK for most
platforms.

(6) Demos
---------

Demos are located in cae/bin and are platform-dependent scripts. 
Parameters in these scripts must be changed to match your system in order to 
succesfully run.

Tensor Guided Painting
* bin/paintdemo.sh  <-- sh script  (Unix/Mac)
* bin/paintdemo.csh <-- csh script (Unix/Mac)
* bin/paintdemo.bat <-- batch file (Windows)
* Demos the tensor guided painting as outlined in my MS thesis (Interpretation
  of 3D seismic images using an interactive image-guided paintbrush).
