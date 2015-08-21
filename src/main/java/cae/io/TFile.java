/****************************************************************************
Copyright (c) 2010, Colorado School of Mines and others. All rights reserved.
This program and accompanying materials are made available under the terms of
the Common Public License - v1.0, which accompanies this distribution, and is
available at http://www.eclipse.org/legal/cpl-v10.html
****************************************************************************/
package cae.io;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Scanner;

/**
 * A file with .t format.
 * A .t file will have the following format:
 * <code>
 * nt nv
 * t1 t2 t3
 * ...
 * vx vy vz
 * </code>
 * where, nt is the number of triangles, nv is the number of vertices, t(i)
 * is a triangle index, and v(x,y,z) is a vertex coordinate.
 * @author Chris Engelsma, Colorado School of Mines
 * @version 2010.03.05
 */
public class TFile {

  /**
   * Loads a new t file.
   * @param fileName the t file.
   */
  public TFile(String fileName) {
    try {
      load(fileName);
    } catch (IOException ioe) {
      System.err.println(ioe);
      System.exit(0);
    }
  }

  /**
   * Gets the triangle table.
   * The triangle table is returned as an array of packed indices:
   * [t11, t12, t13, t21, t22, t23, ... ]
   * A triangle table has dimension 3*nt.
   * @return the triangle table.
   */
  public int[] getTriangles() {
    return t;
  }

  /**
   * Gets the vertex table.
   * The vertex table is returned as an array of packed coordinates:
   * [v1x, v1y, v1z, v2x, v2y, v2z, ... ]
   * A vertex table has dimension 3*nv.
   * @return the vertex table.
   */
  public float[] getVertices() {
    return v;
  }


  /**
   * Gets the number of triangles.
   * @return the number of triangles.
   */
  public int getTriangleCount() {
    return nt;
  }

  /**
   * Gets the number of vertices.
   * @return the number of vertices.
   */
  public int getVertexCount() {
    return nv;
  }

  ///////////////////////////////////////////////////////////////////////////
  // private

  private int nt,nv; // number of triangles, vertices.
  private int[] t;   // triangle table
  private float[] v; // vertex table

  private void load(String fileName) throws IOException {
    checkExtension(fileName);
    Scanner s = new Scanner(new FileInputStream(fileName));
    nt = s.nextInt();
    nv = s.nextInt();
    t = new int[3*nt];
    v = new float[3*nv];
    for (int n=0; n<nt; n++) {
      t[3*n+0] = s.nextInt();
      t[3*n+1] = s.nextInt();
      t[3*n+2] = s.nextInt();
    }
    for (int n=0; n<nv; n++) {
      v[3*n+0] = s.nextFloat();
      v[3*n+1] = s.nextFloat();
      v[3*n+2] = s.nextFloat();
    }
    s.close();
  }

  private static void checkExtension(String fileName) {
    int dot = fileName.lastIndexOf(".");
    String ext = fileName.substring(dot);
    if (!ext.equals(".t")) {
      System.err.println("Warning: "+ext+" possibly not proper file");
    }
  }
}
