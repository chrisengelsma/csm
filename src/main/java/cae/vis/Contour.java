/****************************************************************************
Copyright (c) 2010, Colorado School of Mines and others. All rights reserved.
This program and accompanying materials are made available under the terms of
the Common Public License - v1.0, which accompanies this distribution, and is
available at http://www.eclipse.org/legal/cpl-v10.html
****************************************************************************/
package cae.vis;

/**
 * A contour.
 * Contours are represented by lists of vertices and triangles.
 * @author Chris Engelsma, Colorado School of Mines
 * @version 2010.12.10
 */
public class Contour {
  /**
   * Vertex array with packed coordinates (x1,x2,x3).
   * The number of vertices equals x.length/3.
   */
  public float[] x;

  /**
   * Normal array with packed components (u1,u2,u3).
   * The number of normal vectors equals u.length/3. This number equals the
   * number of vertices.
   */
  public float[] u;

  /**
   * Triangle array of packed vertex indices (i1,i2,i3).
   * When multiplied by 3, each index references the first coordinate x1 of
   * a vertex (x1,x2,x3) stored in the packed vertex array. The number of
   * triangles equals i.length/3. A vertex may be referenced by more than
   * one triangle.
   */
  public int[] i;
}
