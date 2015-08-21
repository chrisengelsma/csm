#!/bin/csh
# Demos 3D interactive image-guided painting.
# Users must have the Mines Java Toolkit (http://boole.mines.edu/jtk)
# installed on their system. For more detailed information on how to install
# the JTK, please refer to http://boole.mines.edu/jtk/trunk/readme.txt
# Author: Chris Engelsma
# Version: 2010.12.14

# Where is the CAE repository? (Where is the build.xml?)
setenv CAE_HOME ${HOME}/Home/box/cae/trunk


# Where is the Mines JTK? (Where is the JTK build.xml?)
setenv MINES_JTK_HOME ${HOME}/Home/box/jtk/trunk

# Where will Java look for classes?
# Add other jars to this list as necessary.
setenv CLASSPATH ${CAE_HOME}/build/jar/cae.jar:
setenv CLASSPATH ${CLASSPATH}:${MINES_JTK_HOME}/build/jar/edu_mines_jtk.jar:
setenv CLASSPATH ${CLASSPATH}:${MINES_JTK_HOME}/jar/gluegen-rt.jar:
setenv CLASSPATH ${CLASSPATH}:${MINES_JTK_HOME}/jar/jogl.jar:
setenv CLASSPATH ${CLASSPATH}:${MINES_JTK_HOME}/jar/junit.jar:
setenv CLASSPATH ${CLASSPATH}:.

# Where are the relevant native (non-Java) code libraries?
setenv JAVA_LIBRARY_PATH \
${MINES_JTK_HOME}/lib/macosx/x64

# Run a server 64-bit VM with assertions enabled and a 1GB max Java heap.
# Modifiy these flags as necessary for your system.
java -server -d64 -ea -Xmx1g \
-Djava.library.path=${JAVA_LIBRARY_PATH} \
-Dapple.awt.graphicsUseQuartz=true \
cae.paint.TensorGuidedPainting
