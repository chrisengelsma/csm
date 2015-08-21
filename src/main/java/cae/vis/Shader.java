/****************************************************************************
Copyright (c) 2010, Colorado School of Mines and others. All rights reserved.
This program and accompanying materials are made available under the terms of
the Common Public License - v1.0, which accompanies this distribution, and is
available at http://www.eclipse.org/legal/cpl-v10.html
****************************************************************************/
package cae.vis;

import java.io.*;

/**
 * An OpenGL Shader.
 * Typically, shaders are imported as strings. This shader object will import
 * a shader file as a proper string.
 * @author Chris Engelsma, Colorado School of Mines
 * @version 2010.03.08
 */
public class Shader {

  /**
   * Constructs a new shader with a given GLSL file.
   * @param fileName the shader file.
   */
  public Shader(String fileName) {
    try {
      read(fileName);
    } catch (FileNotFoundException fnfe) {
      System.out.println(fileName+" does not exist.");
    } catch (IOException ioe) {
      System.out.println("Error loading "+fileName);
    }
  }

  /**
   * Prints the shader file.
   */
  public void printShader() {
    System.out.println(_shader);
  }

  public String getShaderText() {
    return _shader;
  }

  ///////////////////////////////////////////////////////////////////////////
  // private

  private String _shader;

  private void read(String fileName)
    throws FileNotFoundException, IOException
  {
    FileInputStream f = new FileInputStream(fileName);
    int size = (int)(f.getChannel().size());
    byte[] source = new byte[size];
    f.read(source,0,size);
    f.close();
    _shader = new String(source,0,size);
  }
}
