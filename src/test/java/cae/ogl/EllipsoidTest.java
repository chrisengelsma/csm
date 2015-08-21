/****************************************************************************
Copyright (c) 2009, Colorado School of Mines and others. All rights reserved.
This program and accompanying materials are made available under the terms of
the Common Public License - v1.0, which accompanies this distribution, and is
available at http://www.eclipse.org/legal/cpl-v10.html
****************************************************************************/
package cae.ogl;

import edu.mines.jtk.sgl.*;

/**
 * Tests {@link cae.ogl.Ellipsoid}
 * @author Chris Engelsma, Colorado School of Mines.
 * @version
 */
public class EllipsoidTest {

  private static java.util.Random r = new java.util.Random();

  private static float[] makeRandomEigenvector() {
    float a = r.nextFloat()-0.5f;
    float b = r.nextFloat()-0.5f;
    float c = r.nextFloat()-0.5f;
    if (c<0.0f) {
      a = -a;
      b = -b;
      c = -c;
    }
    float s = 1.0f/(float)Math.sqrt(a*a+b*b+c*c);
    return new float[]{a*s,b*s,c*s};
  }

  private static float[] makeRandomEigenvalues() {
    float a1 = r.nextFloat();
    float a2 = r.nextFloat();
    float a3 = r.nextFloat();
    float au = Math.max(Math.max(a1,a2),a3);
    float aw = Math.min(Math.min(a1,a2),a3);
    float av = a1+a2+a3-au-aw;
    return new float[]{au,av,aw};
  }

  private static float[] makeOrthogonalVector(float[] v1) {
    float a1 = v1[0];
    float b1 = v1[1];
    float c1 = v1[2];
    float a2 = r.nextFloat()-0.5f;
    float b2 = r.nextFloat()-0.5f;
    float c2 = r.nextFloat()-0.5f;
    float d11 = a1*a1+b1*b1+c1*c1;
    float d12 = a1*a2+b1*b2+c1*c2;
    float s = d12/d11;
    float a = a2-s*a1;
    float b = b2-s*b1;
    float c = c2-s*c1;
    if (c<0.0f) {
      a = -a;
      b = -b;
      c = -c;
    }
    s = 1.0f/(float)Math.sqrt(a*a+b*b+c*c);
    return new float[]{a*s,b*s,c*s};
  }

  public static void main(String[] args) {
    float[] u = {1,0,0};
    float[] w = {0,0,1};
    float[] a = {1,1,1};
//    float[] u = makeRandomEigenvector();
//    float[] w = makeOrthogonalVector(u);
//    float[] a = makeRandomEigenvalues();

    Ellipsoid el = new Ellipsoid(u,w,a);


    SimpleFrame sf = new SimpleFrame();
    float[] vertices = el.getVertices();
    float[] X = new float[1000];
    float[] Y = new float[1000];
    float[] Z = new float[1000];
    float[] rgbX = new float[X.length];
    float[] rgbY = new float[X.length];
    float[] rgbZ = new float[X.length];
    float start = -2;
    float n = 250;
    for (int i=0; i<X.length-2; i+=3)  {
      X[i] = start+i/n;
      if (X[i]<0) {
        rgbX[i] = 1;
      } else {
        rgbX[i+1] = 1;
      }
      Y[i+1] = start+i/n;
      if (Y[i+1]<0) {
        rgbY[i] = 1;
      } else {
        rgbY[i+1] = 1;
      }
      Z[i+2] = start+i/n;
      if (Z[i+2]<0) {
        rgbZ[i] = 1;
      } else {
        rgbZ[i+1] = 1;
      }
    }


    TriangleGroup tg = sf.addTriangles(vertices);
 //   PointGroup pgx = new PointGroup(0.03f,X,rgbX);
 //   PointGroup pgy = new PointGroup(0.03f,Y,rgbY);
 //   PointGroup pgz = new PointGroup(0.03f,Z,rgbZ);
//    sf.addPoints(pgx);
//    sf.addPoints(pgy);
//    sf.addPoints(pgz);

//    PointGroup pgz= sf.addPoints(0.1f,Z);
  }
}
