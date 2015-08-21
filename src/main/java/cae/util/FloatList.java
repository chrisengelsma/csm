/****************************************************************************
Copyright (c) 2010, Colorado School of Mines and others. All rights reserved.
This program and accompanying materials are made available under the terms of
the Common Public License - v1.0, which accompanies this distribution, and is
available at http://www.eclipse.org/legal/cpl-v10.html
****************************************************************************/
package cae.util;

import java.io.Serializable;

/**
 * A list of floats.
 * @author Chris Engelsma, Colorado School of Mines.
 * @version 2010.02.23
 */
public class FloatList implements Serializable {

  public int n;      // the number of submitted values.
  public float[] a = new float[32]; // the array of doubles.

  /**
   * Adds a value to the list.
   * @param d a double.
   */
  public void add(float d) {
    if (n==a.length) {
      float[] t = new float[2*n];
      System.arraycopy(a,0,t,0,n);
      a = t;
    }
    a[n++] = d;
  }

  /**
   * Adds a float list to this list.
   * @param list a float list.
   */
  public void add(FloatList list) {
    if (list!=null) {
      float[] f = list.trim();
      add(f);
    }
  }

  /**
   * Adds an array floats to this float list.
   * @param f an array of floats.
   */
  public void add(float[] f) {
    if (f!=null) {
      for (int i=0; i<f.length; ++i) add(f[i]);
    }
  }
  
  /**
   * Clears the float list.
   */
  public void clear() {
    n = 0;
    a = new float[32];
  }

  /**
   * Trims the list to size.
   * @return the trimmed list of doubles.
   */
  public float[] trim() {
    float[] t = new float[n];
    System.arraycopy(a,0,t,0,n);
    return t;
  }

  /**
   * Gets the untrimmed array of floats.
   * @return the untrimmed array.
   */
  public float[] getAll() {
    return a;
  }

  /**
   * Gets the number of input values.
   * @return the number of inputted floats.
   */
  public int getN() {
    return n;
  }
}
