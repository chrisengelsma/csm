/****************************************************************************
Copyright (c) 2010, Colorado School of Mines and others. All rights reserved.
This program and accompanying materials are made available under the terms of
the Common Public License - v1.0, which accompanies this distribution, and is
available at http://www.eclipse.org/legal/cpl-v10.html
****************************************************************************/
package cae.vis;

import cae.paint.PaintBrush;
import cae.paint.Painting3;
import cae.paint.Painting3Group;
import edu.mines.jtk.dsp.EigenTensors3;
import edu.mines.jtk.sgl.*;
import static edu.mines.jtk.util.ArrayMath.*;

import javax.swing.*;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * @author Chris Engelsma, Colorado School of Mines
 * @version 2010.08.10
 */
public class SubPixelPrecisionTest {

  public static void main(String[] args) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        test1();
//        test2();
      }
    });
  }

  /**
   * Tests two intersecting spheres.
   */
  public static void test1() {
    int n1 = 100, n2 = 100, n3 = 100;
    float v = 1.0f, d = 20.0f;
    float[][][] paint = new float[n3][n2][n1];
    /*
    for (int i3=0; i3<n3; ++i3) {
      for (int i2=0; i2<n2; ++i2) {
        for (int i1=0; i1<n1; ++i1) {
          if (i3<n3/2) cae.paint[i3][i2][i1] = 1.0f;
        }
      }
    }
    */
    Painting3Group p3g = new Painting3Group(paint);
    SphereBrush pb = new SphereBrush();
    Painting3 p3 = p3g.getPainting3();
//    p3.paintAt(50,60,50,v,d,pb);
//    p3.paintAt(50,50,50,v,d,pb);
//    p3.paintAt(50,50,50,v,d,pb);
    p3.paintAt(50,50,50,v,d,pb);
    Contour c = p3.getContour(v);

    SimpleFrame sf = new SimpleFrame();
    sf.setTitle("Formation");
    World world = sf.getWorld();
    TriangleGroup tg = new TriangleGroup(c.i,c.x);
    world.addChild(tg);
    sf.setSize(1250,900);
  }

  /**
   * Tests on structure tensors measured from the Teapot Dome data set.
   */
  public static void test2() {
    String dataDir = "/Data/cteam/tp/csm/seismicz/subz_401_4_600/";
    String dataFile = "tpets.dat";
    int n1 = 401, n2 = 357, n3 = 161;
    float v = 1.0f, d = 30.0f;
    EigenTensors3 et = loadTensors(dataDir+dataFile);
    System.out.println(et);
    float[][][] paint = new float[n3][n2][n1];
    Painting3Group p3g = new Painting3Group(paint);
    PaintBrush pb = new PaintBrush(n1,n2,n3,et);
    pb.setSize(30);
    Painting3 p3 = p3g.getPainting3();
    for (int i3=0; i3<10; ++i3)
      for (int i2=0; i2<10; ++i2)
        for (int i1=0; i1<1; ++i1) {
          p3.paintAt(n1/2+i1,n2/2+i2,n3/2+i3,v,d,pb);
        }

    Contour c = p3.getContour(v);

    SimpleFrame sf = new SimpleFrame();
    sf.setTitle("Formation");
    World world = sf.getWorld();
    TriangleGroup tg = new TriangleGroup(c.i,c.x);
    world.addChild(tg);
  }

  ///////////////////////////////////////////////////////////////////////////
  // private

  private static class SphereBrush implements Painting3.DistanceMap {
    float[] o = new float[]{0.0f,0.0f,0.0f};
    
    public float getDistance(int i1, int i2, int i3) {
      return sqrt(pow(i1-o[0],2)+pow(i2-o[1],2)+pow(i3-o[2],2));
    }
    public void setLocation(int i1, int i2, int i3) {
      o[0] = i3;
      o[1] = i2;
      o[2] = i1;
    }
    public int getSize() {
      return 10;
    }
  }

  private static EigenTensors3 loadTensors(String file) {
    EigenTensors3 et = null;
    try {
      ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file));
      et = (EigenTensors3)ois.readObject();
      ois.close();
      return et;
    } catch (IOException ioe) {
      System.err.println(ioe);
    } catch (ClassNotFoundException cnfe) {
      System.err.println(cnfe);
    }
    return et;
  }
}
