/****************************************************************************
Copyright (c) 2010, Colorado School of Mines and others. All rights reserved.
This program and accompanying materials are made available under the terms of
the Common Public License - v1.0, which accompanies this distribution, and is
available at http://www.eclipse.org/legal/cpl-v10.html
****************************************************************************/
package cae.util;

import static java.lang.Math.sqrt;

/**
 * Math operations for vectors.
 * @author Chris Engelsma, Colorado School of Mines
 * @version 2010.02.23
 */
public class VectorMath {

  /**
   * Computes the dot (inner) product of two vectors a x b.
   * @param a a vector.
   * @param b a vector.
   * @return the dot product of a * b.
   */
  public static float dot(float[] a, float[] b) {
    return (a[0]*b[0]+a[1]*b[1]+a[2]*b[2]);
  }

  /**
   * Computes the cross-product of two vectors a x b.
   * @param a a vector.
   * @param b a vector.
   * @param normal true, if normalized; false, otherwise.
   * @return the cross product of a x b.
   */
  public static float[] cp(float[] a, float[] b, boolean normal) {
    float mag = 1.0f;
    float[] cp = new float[3];
    cp[0] = a[1]*b[2]-b[1]*a[2];
    cp[1] = b[0]*a[2]-a[0]*b[2];
    cp[2] = a[0]*b[1]-b[0]*a[1];
    if (normal)
      mag = (float)sqrt(cp[0]*cp[0]+cp[1]*cp[1]+cp[2]*cp[2]);
    cp[0] /= mag; cp[1] /= mag; cp[2] /= mag;
    return cp;
  }

  /**
   * Determines if two vectors are equivalent a = b.
   * Note two vectors are equivalent if they have the same length and their
   * three components are the same.
   * @param a a vector.
   * @param b a vector.
   * @return true, if equivalent; false, otherwise.
   */
  public static boolean eq(float[] a, float[] b) {
    int alen = a.length;
    int blen = b.length;
    if (alen!=blen) return false;
    for (int i=0; i<alen; ++i)
      if (a[i]!=b[i]) return false;
    return true;
  }

  /**
   * Normalizes a vector with three values.
   * @param v0 the first value.
   * @param v1 the second value.
   * @param v2 the third value.
   * @return the normalized vector.
   */
  public static float[] normalize(float v0, float v1, float v2) {
    return normalize(new float[]{v0,v1,v2});
  }

  /**
   * Normalizes a vector.
   * @param v a vector
   * @return the normalized vector.
   */
  public static float[] normalize(float[] v) {
    float mag = 0.0f;
    int l = v.length;
    for (int i=0; i<l; ++i) 
      mag += v[i]*v[i];
    mag = (float)sqrt(mag);
    for (int i=0; i<l; ++i)
      v[i] /= mag;
    return v;
  }
}
