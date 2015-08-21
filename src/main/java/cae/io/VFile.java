/****************************************************************************
Copyright (c) 2009, Colorado School of Mines and others. All rights reserved.
This program and accompanying materials are made available under the terms of
the Common Public License - v1.0, which accompanies this distribution, and is
available at http://www.eclipse.org/legal/cpl-v10.html
****************************************************************************/
package cae.io;

import edu.mines.jtk.io.*;

import java.io.IOException;
import java.nio.ByteOrder;

/**
 * A file with the .v structure used by Dr. Andrzej Szymczak.
 * Files ending in a .v extension are designed to have a header file
 * containing 3 4-byte LITTLE endian integers.
 * @author Chris Engelsma, Colorado School of Mines
 * @version 2010.02.10
 */
public class VFile {

  /**
   * Constructs a new vfile
   * @param fileName the name of the .v file.
   */
  public VFile(String fileName) {
    try {
      load(fileName);
    } catch (IOException ioe) {
      System.out.println(ioe);
    }
  }

  /**
   * Gets the floats as a 3D array
   * @return the floats.
   */
  public float[][][] getFloats() {
    return data;
  }

  /**
   * Gets the number of samples in the x-direction.
   * @return the number of samples in the x-direction.
   */
  public int getX() {
    return nx;
  }

  /**
   * Gets the number of samples in the y-direction.
   * @return the number of samples in the y-direction.
   */
  public int getY() {
    return ny;
  }

  /**
   * Gets the number of samples in the z-direction.
   * @return the number of samples in the z-direction.
   */
  public int getZ() {
    return nz;
  }

  ///////////////////////////////////////////////////////////////////////////
  // private

  private float[][][] data; // The image floats.
  private int nx,ny,nz;     // Dimensions of the image.

  private void load(String fileName) throws IOException {
    checkExtension(fileName);
    ArrayInputStream ais =
      new ArrayInputStream(fileName, ByteOrder.LITTLE_ENDIAN);
    nx = ais.readInt();
    ny = ais.readInt();
    nz = ais.readInt();
    data = new float[nz][ny][nx];
    ais.readFloats(data);
  }

  private void checkExtension(String fileName) {
    int dot = fileName.lastIndexOf(".");
    String ext = fileName.substring(dot);
    if (!ext.equals(".v")) {
      System.err.println("Warning: "+ext+" possibly not proper file");
    }
  }
}
