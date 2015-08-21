/****************************************************************************
Copyright (c) 2009, Colorado School of Mines and others. All rights reserved.
This program and accompanying materials are made available under the terms of
the Common Public License - v1.0, which accompanies this distribution, and is
available at http://www.eclipse.org/legal/cpl-v10.html
****************************************************************************/
package cae.io;

import edu.mines.jtk.io.ArrayInputStream;
import edu.mines.jtk.util.Direct;

import java.awt.image.*;
import java.io.*;
import java.nio.*;
import java.util.Scanner;
import javax.imageio.*;

/**
 * Data relating to an input image.
 * @author Chris Engelsma, Colorado School of Mines.
 * @version 2010.03.22
 */
public class ImageData {

  public float[][] image = null;
  public float[][][] rgb;
  public String fileName = "";
  public float min = 0;
  public float max = 0;
  public int nx = 0;
  public int ny = 0;
  public ByteBuffer buf = null;
  public String sufx;

  public ImageData() {

  }
  
  /**
   * Constructs a new image data object.
   */
  public ImageData(String fileName) {
    sufx = fileName.substring(fileName.lastIndexOf('.')+1);
    sufx = sufx.toLowerCase();
    if (sufx.equals("ppm")) {
      loadPPM(fileName);
    } else if (sufx.equals("png")) {
      loadPNG(fileName);
    } else {
      System.err.println("Unsupported datatype: "+sufx);
    }
  }

  /**
   * Loads a PNG file.
   * TODO doesn't work yet.
   * @param fileName the file name.
   */
  public void loadPNG(String fileName) {
    this.fileName = fileName;
    try {
      BufferedImage bi = null;
      File file = new File(fileName);
      bi = ImageIO.read(file);
      int w = bi.getWidth();
      int h = bi.getHeight();
      int[] p = new int[3*w*h];

      image = new float[h][w];
      rgb = new float[3][h][w];
      int[] pixels = bi.getRGB(0,0,w,h,null,0,w);
      for (int i=0,k=0; i<w; ++i) {
        for (int j=0; j<h; ++j,++k) {
          int pix = pixels[k];
          int r = (pix>>16)&0xff;
          int g = (pix>> 8)&0xff;
          int b = (pix>> 0)&0xff;
          p[3*(j+i*w)+0] = r;
          p[3*(j+i*w)+1] = g;
          p[3*(j+i*w)+2] = b;

          rgb[0][j][i] = (float)r;
          rgb[1][j][i] = (float)g;
          rgb[2][j][i] = (float)b;
          image[j][i] = (float)pix;
        }
      }
      
      nx = h;
      ny = w;
//      scale(0,255);
    } catch (IOException ioe) {}
  }

  /**
   * Loads a binary PPM file.
   * @param fileName the file name.
   */
  public void loadPPM(String fileName) {
    this.fileName = fileName;
    byte[] p;
    try {
      File map = new File(fileName);
      FileInputStream fis = new FileInputStream(map);
      Scanner s = new Scanner(fis);
      s.nextLine();
      nx = s.nextInt();
      ny = s.nextInt();
      s.close();
      fis.close();
      fis = new FileInputStream(map);
      ArrayInputStream ais = new ArrayInputStream(fis);
      p = new byte[3*nx*ny];
      ais.readLine(); ais.readLine(); ais.readLine();
      ais.readBytes(p);
      ais.close();
      fis.close();
      buf = Direct.newByteBuffer(p);
    } catch(IOException ioe) {}
  }

  /**
   * Loads an array into the image data.
   * @param image a 2D array of doubles.
   * @param scaling true, if scaling [0,255]; false, do nothing.
   */
  public void loadArray(float[][] image, boolean scaling) {
    this.image = image;
    int nx = image.length;
    int ny = image[0].length;
    if (scaling)
      scale(0,255);
  }

  /**
   * Returns the byte buffer, if any.
   * @return the byte buffer, if any; null, if none.
   */
  public ByteBuffer getBuffer() {
    return buf;
  }

  /**
   * Returns the double array, if any.
   * @return the double array, if any; null, if none.
   */
  public float[][] getArray() {
    return image;
  }

  ////////////////////////////////////////////////////////////////////////////
  // private

  /**
   * Grabs the min/max of the image data.
   * @return an array containing the max and min [0,1]
   */
  private float[] getMinMax() {
    float[] mm = new float[2];
    mm[0] = image[0][0]; // Max
    mm[1] = image[0][0]; // Min
    for (int i=0; i<image.length; ++i)
      for (int j=0; j<image[0].length; ++j)
        if (mm[0]<=image[i][j]) {
          mm[0] = image[i][j];
        } else if (mm[1]>=image[i][j]) {
          mm[1] = image[i][j];
        }
    return mm;
  }

  /**
   * Scales the image data.
   * @param min the min.
   * @param max the max.
   */
  private void scale(float min, float max) {
    float[] mm = getMinMax();
    for (int i=0; i<image.length; ++i)
      for (int j=0; j<image[0].length; ++j)
        image[i][j] = max*(image[i][j]-mm[1])/(mm[0]-mm[1]);
    this.max = max;
    this.min = min;
  }
}

