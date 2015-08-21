/****************************************************************************
Copyright (c) 2010, Colorado School of Mines and others. All rights reserved.
This program and accompanying materials are made available under the terms of
the Common Public License - v1.0, which accompanies this distribution, and is
available at http://www.eclipse.org/legal/cpl-v10.html
****************************************************************************/
package cae.io;

import edu.mines.jtk.io.ArrayInputStream;
import edu.mines.jtk.sgl.SimpleFrame;
import static edu.mines.jtk.util.ArrayMath.*;

import java.io.IOException;

/**
 * @author Chris Engelsma, Colorado School of Mines
 * @version 2010.07.26
 */
public class MRIFileTest {
  public static final String dir = "/Data/mri/bin/";
  public static final String file = "MRbrain";
  public static final int n1 = 109;
  public static final int n2 = 256;
  public static final int n3 = 256;

  public static void main(String[] args) {
    short[][][] data = new short[n1][n2][n3];
    try {
      for (int i=0; i<n1; ++i) {
        String filename = dir+file+"."+(i+1);
        ArrayInputStream ais = new ArrayInputStream(filename);
        ais.readShorts(data[i]);
      }
      float average = 0.0f;
      float[][][] fdata = new float[n1][n2][n3];
      for (int i1=0; i1<n1; ++i1) {
        for (int i2=0; i2<n2; ++i2) {
          for (int i3=0; i3<n3; ++i3) {
            fdata[i1][i3][i2] = (float)data[i1][i2][i3];
            average+=fdata[i1][i3][i2];
          }
        }
      }
      float min = min(fdata);
      float max = max(fdata);
      SimpleFrame sf = new SimpleFrame();
      sf.addImagePanels(fdata);
      System.out.println("min: "+min+" max: "+max);
    } catch (IOException ioe) {
      System.err.println(ioe);
    }
  }
}
