/****************************************************************************
Copyright (c) 2010, Colorado School of Mines and others. All rights reserved.
This program and accompanying materials are made available under the terms of
the Common Public License - v1.0, which accompanies this distribution, and is
available at http://www.eclipse.org/legal/cpl-v10.html
****************************************************************************/
package cae.util;

import java.io.Serializable;

/**
 * A list of integers.
 * @author Chris Engelsma, Colorado School of Mines.
 * @version 2010.02.23
 */
public class IntList implements Serializable {

  public int n;      // the number of submitted values.
  public int[] a = new int[64]; // the array of integers.

  /**
   * Adds a value to the list.
   * @param i an integer.
   */
  public void add(int i) {
    if (n==a.length) {
      int[] t = new int[2*n];
      System.arraycopy(a,0,t,0,n);
      a = t;
    }
    a[n++] = i;
  }

  /**
   * Gets the value at the requested index.
   * @param i the index.
   * @return the integer at index i
   */
  public int get(int i) {
    return a[i];
  }

  /**
   * Clears the int list.
   */
  public void clear() {
    n = 0;
    a = new int[1024];
  }

  public boolean contains(int x) {
    for (int i=0; i<n; ++i)
      if (a[i]==x) return true;
    return false;
  }
  
  /**
   * Trims the list to size.
   * @return the trimmed list of integers.
   */
  public int[] trim() {
    int[] t = new int[n];
    System.arraycopy(a,0,t,0,n);
    return t;
  }

  /**
   * Gets the untrimmed array of integers.
   * @return the untrimmed array of integers.
   */
  public int[] getAll() {
    return a;
  }

  /**
   * Gets the number of inputted integers.
   * @return the number of inputted integers.
   */
  public int getN() {
    return n;
  }
}
