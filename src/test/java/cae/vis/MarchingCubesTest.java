/****************************************************************************
Copyright (c) 2010, Colorado School of Mines and others. All rights reserved.
This program and accompanying materials are made available under the terms of
the Common Public License - v1.0, which accompanies this distribution, and is
available at http://www.eclipse.org/legal/cpl-v10.html
****************************************************************************/
package cae.vis;

import cae.vis.Contour;
import cae.vis.MarchingCubes;

import edu.mines.jtk.dsp.Sampling;
import edu.mines.jtk.sgl.*;
import static edu.mines.jtk.util.ArrayMath.*;

import javax.swing.*;
import java.util.Random;

/**
 * Tests {@link main.java.cae.vis.MarchingCubes}
 * @author Chris Engelsma, Colorado School of Mines
 * @version 2010.07.04
 */
public class MarchingCubesTest {
  public static void main(String[] args) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        initialize();
        curvedFunction();
        //plane1();
        //plane2();
        //plane3();
        march();
      }
    });
  }


  public static void initialize() {
    data = new float[n3][n2][n1];

    s1 = new Sampling(n1);
    s2 = new Sampling(n2);
    s3 = new Sampling(n3);
  }

  public static void march() {
    MarchingCubes mc = new MarchingCubes(s1,s2,s3,data);
    mc.setSwap13(true);
    plot(mc);
  }

  ///////////////////////////////////////////////////////////////////////////
  // private

  private static final int n1 = 200;
  private static final int n2 = 200;
  private static final int n3 = 200;

  private static float[][][] data;

  private static Sampling s1;
  private static Sampling s2;
  private static Sampling s3;

  private static SimpleFrame sf;

  private static void plot(MarchingCubes mc) {
    mc.setConcurrency(MarchingCubes.Concurrency.PARALLEL);
    Random r = new Random();
    float max = max(data);
    float min = min(data);
    float range = max-min;
    int step = 1;
    Contour c = mc.getContour(100);
    TriangleGroup tg = new TriangleGroup(c.i,c.x,c.u);
    setTGStates(tg);
    sf = new SimpleFrame();
    sf.addTriangles(tg);
  }

  private static void setTGStates(TriangleGroup tg) {
    StateSet states = new StateSet();
    LightModelState lms = new LightModelState();
    lms.setTwoSide(true);
    states.add(lms);
    MaterialState ms = new MaterialState();
//    ms.setColorMaterial(GL_AMBIENT_AND_DIFFUSE);
    ms.setShininess(0.0f);
    states.add(ms);
    tg.setStates(states);
  }

  private static void curvedFunction() {
    float dx = 100;
    for (int i3=0; i3<n3; ++i3) {
      for (int i2=0; i2<n2; ++i2) {
        for (int i1=0; i1<n1; ++i1) {
          data[i3][i2][i1] = sqrt(pow(i3-dx,2)+pow(i2-dx,2)+pow(i1-dx,2));
        }
      }
    }
  }

  private static void plane1() {
    for (int i3=0; i3<n3; ++i3) {
      for (int i2=0; i2<n2; ++i2) {
        for (int i1=0; i1<n1; ++i1) {
          if (i3>n3/2) data[i3][i2][i1] = 20.0f;
        }
      }
    }
  }

  private static void plane2() {
    for (int i3=0; i3<n3; ++i3) {
      for (int i2=0; i2<n2; ++i2) {
        for (int i1=0; i1<n1; ++i1) {
          if (i2>n2/2) data[i3][i2][i1] = 20.0f;
        }
      }
    }
  }
  
  private static void plane3() {
    for (int i3=0; i3<n3; ++i3) {
      for (int i2=0; i2<n2; ++i2) {
        for (int i1=0; i1<n1; ++i1) {
          if (i1>n1/2) data[i3][i2][i1] = 20.0f;
        }
      }
    }
  }
}

