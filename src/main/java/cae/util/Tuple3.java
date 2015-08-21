/****************************************************************************
Copyright (c) 2009, Colorado School of Mines and others. All rights reserved.
This program and accompanying materials are made available under the terms of
the Common Public License - v1.0, which accompanies this distribution, and is
available at http://www.eclipse.org/legal/cpl-v10.html
****************************************************************************/
package cae.util;

/**
 * Constructs a tuple with three components.
 * @author Chris Engelsma, Colorado School of Mines.
 * @version 2009.10.27
 */
public class Tuple3 {

  public double x = 0; // The first component.
  public double y = 0; // The second component.
  public double z = 0; // The third component.

  /**
   * Initializes a new tuple with all values equal to zero.
   */
  public Tuple3() {
  }

  /**
   * Initializes a new tuple with specified x, y and z values.
   * @param x the first component.
   * @param y the second component.
   * @param z the third component.
   */
  public Tuple3(double x, double y, double z) {
    this.x = x;
    this.y = y;
    this.z = z;
  }

  /**
   * Compares two tuples' values.
   * @param that another tuple.
   * @return true, if all three components are equivalent; false, otherwise.
   */
  public boolean equals(Tuple3 that) {
    if (this==that)
      return true;
    else return (this.x==that.x && this.y==that.y && this.z==that.z);
  }

  /**
   * Displays the tuple.
   */
  public void display() {
    System.out.println("("+x+","+y+","+z+")");
  }

  ////////////////////////////////////////////////////////////////////////////
  // private
}
