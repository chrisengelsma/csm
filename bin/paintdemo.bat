@echo off
rem Demos 3D interactive image-guided painting.
rem Users must have the Mines Java Toolkit (http://boole.mines.edu/jtk)
rem installed on their system. For more detailed information on how to install
rem the JTK, please refer to http://boole.mines.edu/jtk/trunk/readme.txt
rem Author: Chris Engelsma
rem Version: 2010.12.14

setlocal

rem Where is the Java Runtime Environment (JRE)?
set JRE_HOME=C:\pro\jdk\jre

rem Where is the CAE repository? (Where is the build.xml?)
set CAE_HOME=C:\cengelsm\box\cae\trunk

rem Where is the Mines JTK? (Where is the JTK build.xml?)
set MINES_JTK_HOME=C:\cengelsm\box\jtk\trunk

rem Where will Java look for classes?
rem Add other jars to this list as necessary.
set CLASSPATH=^
%CAE_HOME%\build\jar\cae.jar;^
%MINES_JTK_HOME%\build\jar\edu_mines_jtk.jar;^
%MINES_JTK_HOME%\jar\gluegen-rt.jar;^
%MINES_JTK_HOME%\jar\jogl.jar;^
%MINES_JTK_HOME%\jar\junit.jar;^
.

rem Where are the relevant native (non-Java) code libraries?
set JAVA_LIBRARY_PATH=^
%MINES_JTK_HOME%\lib\windows\x86

rem Run a server VM with assertions enabled and a 1GB max Java heap.
rem Modify these flags as necessary for your system.
java -server -ea -Xmx1g ^
-Djava.library.path=%JAVA_LIBRARY_PATH% ^
-Djava.util.logging.config.file=C:\cengelsm\etc\java_logging_config ^
cae.paint.TensorGuidedPainting

endlocal
