/****************************************************************************
Copyright (c) 2010, Colorado School of Mines and others. All rights reserved.
This program and accompanying materials are made available under the terms of
the Common Public License - v1.0, which accompanies this distribution, and is
available at http://www.eclipse.org/legal/cpl-v10.html
****************************************************************************/
package cae.bench;

import cae.vis.MarchingCubes;

import edu.mines.jtk.util.Stopwatch;
import edu.mines.jtk.io.*;
import edu.mines.jtk.dsp.*;

import java.io.IOException;

/**
 * Benchmarks {@link main.java.cae.vis.MarchingCubes}.
 * @author Chris Engelsma, Colorado School of Mines
 * @version 2010.07.14
 */
public class MarchingCubesBench {
  public static void main(String[] args) {
    loadData();
    MarchingCubes mc1 = new MarchingCubes(s1,s2,s3,xyz);
    MarchingCubes mc2 = new MarchingCubes(s1,s2,s3,xyz);

    mc1.setConcurrency(MarchingCubes.Concurrency.SERIAL);
    mc1.setNormals(false);
    mc1.setSwap13(true);
    mc2.setConcurrency(MarchingCubes.Concurrency.PARALLEL);
    mc2.setNormals(false);
    mc2.setSwap13(true);

    for(;;) {
      double t1 = time(mc1);
      double t2 = time(mc2);
      System.out.println("  Serial: "+t1+" sec");
      System.out.println("Parallel: "+t2+" sec");
    }
  }

  private static final String dir = "/Data/vol/volvis.org/";
  private static final String file = "skull.raw";
  private static final int n1 = 256;
  private static final int n2 = 256;
  private static final int n3 = 256;
  private static final float isoval = 55.0f;
  private static final Sampling s1 = new Sampling(n1);
  private static final Sampling s2 = new Sampling(n2);
  private static final Sampling s3 = new Sampling(n3);
  private static float[][][] xyz;

  private static double time(MarchingCubes mc) {
    Stopwatch sw = new Stopwatch();
    sw.start();
    int niter;
    for (niter=0; sw.time()<10.0; ++niter)
      mc.getContour(isoval);
    sw.stop();
    return sw.time()/niter;
  }

  /*
  private static void show(MarchingCubes mc) {
    MarchingCubes.Contour contour = mc.getContour(isoval);
    SimpleFrame sf = new SimpleFrame();
    TriangleGroup tg = new TriangleGroup(true,contour.x);
    sf.addTriangles(tg);
  }
  */

  private static void loadData() {
    byte[][][] xyzbytes = new byte[n3][n2][n1];
    xyz = new float[n3][n2][n1];
    try {
      ArrayInputStream ais = new ArrayInputStream(dir+file);
      ais.readBytes(xyzbytes);
      ais.close();
    } catch (IOException ioe) {
      System.out.println(ioe);
    }
    for (int i3=0; i3<n3; ++i3) {
      for (int i2=0; i2<n2; ++i2) {
        for (int i1=0; i1<n1; ++i1) {
          xyz[i3][i2][i1] = (float)xyzbytes[i3][i2][i1];
        }
      }
    }
  }
}
