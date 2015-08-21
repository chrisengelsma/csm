/****************************************************************************
Copyright (c) 2010, Colorado School of Mines and others. All rights reserved.
This program and accompanying materials are made available under the terms of
the Common Public License - v1.0, which accompanies this distribution, and is
available at http://www.eclipse.org/legal/cpl-v10.html
****************************************************************************/
package cae.paint;

import edu.mines.jtk.dsp.Sampling;
import edu.mines.jtk.sgl.Group;

/**
 * A group of 3D paintings.
 * @author Chris Engelsma, Colorado School of Mines
 * @version 2010.08.09
 */
public class Painting3Group extends Group {

  /**
   * Constructs a 3D painting group.
   * @param f a 3D image.
   */
  public Painting3Group(float[][][] f) {
    this(new Sampling(f[0][0].length),
         new Sampling(f[0].length),
         new Sampling(f.length),
         f);
  }

  /**
   * Constructs a 3D painting group with specified samplings.
   * @param s1 sampling in the 1st dimension.
   * @param s2 sampling in the 2nd dimension.
   * @param s3 sampling in the 3rd dimension.
   * @param f a 3D image.
   */
  public Painting3Group(
    Sampling s1, Sampling s2, Sampling s3, float[][][] f)
  {
    _f = f;
    _s1 = s1;
    _s2 = s2;
    _s3 = s3;
    _painting = new Painting3(s1,s2,s3,f);
  }

  /**
   * Sets the 3D painting for this group.
   * @param p3 a 3D painting.
   */
  public void setPainting3(Painting3 p3) {
    _painting = p3;
  }

  /**
   * Gets the 3D painting for this group.
   * @return the 3D painting.
   */
  public Painting3 getPainting3() {
    return _painting;
  }

  /**
   * Gets the painted voxels.
   * @return the painted voxels.
   */
  public float[][][] getPaint() {
    return _f;
  }
  ///////////////////////////////////////////////////////////////////////////
  // private

  private Sampling _s1,_s2,_s3;
  private float[][][] _f;
  private Painting3 _painting;

}
