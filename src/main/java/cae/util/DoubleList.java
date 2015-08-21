/****************************************************************************
Copyright (c) 2010, Colorado School of Mines and others. All rights reserved.
This program and accompanying materials are made available under the terms of
the Common Public License - v1.0, which accompanies this distribution, and is
available at http://www.eclipse.org/legal/cpl-v10.html
****************************************************************************/
package cae.util;

/**
 * A list of doubles.
 * @author Chris Engelsma, Colorado School of Mines.
 * @version 2010.02.23
 */
public class DoubleList {

  int n;      // the number of submitted values.
  double[] a; // the array of doubles.

  /**
   * Constructs a new list of doubles.
   */
  public DoubleList() {
    n = 0;
    a = new double[64];
  }

  /**
   * Adds a value to the list.
   * @param d a double.
   */
  public void add(double d) {
    if (n==a.length) {
      double[] t = new double[2*a.length];
      for (int i=0; i<n; ++i)
        t[i] = a[i];
      a = t;
    }
    a[n++] = d;
  }

  /**
   * Trims the list to size.
   * @return the trimmed list of doubles.
   */
  public double[] trim() {
    double[] t = new double[n];
    for (int i=0; i<n; ++i)
      t[i] = a[i];
    return t;
  }
}
