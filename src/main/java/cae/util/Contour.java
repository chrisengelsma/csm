/****************************************************************************
Copyright (c) 2009, Colorado School of Mines and others. All rights reserved.
This program and accompanying materials are made available under the terms of
the Common Public License - v1.0, which accompanies this distribution, and is
available at http://www.eclipse.org/legal/cpl-v10.html
****************************************************************************/
package cae.util;

import java.util.ArrayList;

/**
 * A 2D contour.
 * @author Chris Engelsma, Colorado School of Mines.
 * @version 2010.02.23
 */
public class Contour {
  
  public double cid; // Contour function ID
  public int cs = 0; // Number of segments
  public ArrayList<double[]> x = new ArrayList<double[]>();
  public ArrayList<double[]> y = new ArrayList<double[]>();

  /**
   * Constructs a new contour with given reference ID.
   * @param cid the contour reference ID.
   */
  public Contour(double cid) {
    this.cid = cid;
  }

  /**
   * Adds a coordinate to the contour.
   * @param x the x-coordinate.
   * @param y the y-coordinate.
   */
  public void append(DoubleList x, DoubleList y) {
    ++this.cs;
    this.x.add(x.trim());
    this.y.add(y.trim());
  }
}
