/****************************************************************************
Copyright (c) 2009, Colorado School of Mines and others. All rights reserved.
This program and accompanying materials are made available under the terms of
the Common Public License - v1.0, which accompanies this distribution, and is
available at http://www.eclipse.org/legal/cpl-v10.html
****************************************************************************/
package cae.util;

/**
 * A vector that stores floats in the order of which they're added.
 * @author Chris Engelsma, Colorado School of Mines.
 * @version 2009.10.09
 */
public class Vector3 {

  public float x = 0;
  public float y = 0;
  public float z = 0;

  /**
   * Constructs a zero vector.
   */
  public Vector3() {
  }

  /**
   * Constructs a vetor with a given array of float.
   * @param f an array of float.
   */
  public Vector3(float[] f) {
    this(f[0],f[1],f[2]);
  }
  
  /**
   * Constructs a 3D vector.
   * @param a the first vector float.
   * @param b the second vector float.
   * @param c the third vector float.
   */
  public Vector3(float a, float b, float c) {
    x = a;
    y = b;
    z = c;
  }


  /**
   * Returns the vector as an array of float.
   * @return the vector as an array of float.
   */
  public float[] toArray() {
    return new float[]{x,y,z};
  }

  /**
   * Computes the dot product of this vector with another vector.
   * @param a a vector
   * @return the dot product.
   */
  public float dot(Vector3 a) {
    return x*a.x+y*a.y+z*a.z;
  }

  /**
   * Computes the cross product of this vector with another vector.
   * Both vectors must have at least three components or a zero vector is
   * returned.  Note that only the first three components of the vector are
   * used to compute the cross product.
   * @param a a Vector3.
   * @return the cross product
   */
  public Vector3 cross(Vector3 a) {
    Vector3 v = new Vector3(
      this.y*a.z-this.z*a.y,
      this.z*a.x-this.x*a.z,
      this.x*a.y-this.y*a.x);
    return v.normalize();
  }

  public static Vector3 cross(Vector3 a, Vector3 b) {
    Vector3 v = new Vector3(
      a.y*b.z-a.z*b.y,
      a.z*b.x-a.x*b.z,
      a.x*b.y-a.y*b.x
    );
    return v;
  }

  public Vector3 normalize() {
    float mag = this.magnitude();
    return new Vector3(x/mag,y/mag,z/mag);
  }

  /**
   * Computes the magnitude of this vector.
   * @return the magnitude.
   */
  public float magnitude() {
    return (float)Math.sqrt(x*x+y*y+z*z);
  }

  public boolean equals(Vector3 v) {
    return (x==v.x && y==v.y && z==v.z);
  }
}
