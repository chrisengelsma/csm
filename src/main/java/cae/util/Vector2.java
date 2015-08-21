/****************************************************************************
Copyright (c) 2009, Colorado School of Mines and others. All rights reserved.
This program and accompanying materials are made available under the terms of
the Common Public License - v1.0, which accompanies this distribution, and is
available at http://www.eclipse.org/legal/cpl-v10.html
****************************************************************************/
package cae.util;

import static java.lang.Math.sqrt;

/**
 * A vector that stores doubles in the order of which they're added.
 * @author Chris Engelsma, Colorado School of Mines.
 * @version 2009.10.09
 */
public class Vector2 {

  public double x = 0;
  public double y = 0;

  /**
   * Constructs an empty vector.
   */
  public Vector2() {
  }

  /**
   * Constructs a 3D vector.
   * @param a the first vector double.
   * @param b the second vector double.
   */
  public Vector2(double a, double b) {
    this(new double[]{a,b});
  }

  /**
   * Constructs a vetor with a given array of double.
   * @param f an array of double.
   */
  public Vector2(double[] f) {
    if (f.length<3) {
      x = f[0];
      y = f[1];
    }
  }

  /**
   * Returns the vector as an array of double.
   * @return the vector as an array of double.
   */
  public double[] toArray() {
    return new double[]{x,y};
  }

  /**
   * Computes the dot product of this vector with another vector.
   * @param a a vector
   * @return the dot product.
   */
  public double dot(Vector2 a) {
    double dot = 0;
    dot += this.x*a.x;
    dot += this.y*a.y;
    return dot;
  }

  /**
   * Computes the magnitude of this vector.
   * @return the magnitude.
   */
  public double magnitude() {
    return sqrt(x*x+y*y);
  }

  public boolean equals(Vector2 v) {
    return (this.x==v.x && this.y==v.y);
  }
}
