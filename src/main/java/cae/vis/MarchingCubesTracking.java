/****************************************************************************
Copyright (c) 2010, Colorado School of Mines and others. All rights reserved.
This program and accompanying materials are made available under the terms of
the Common Public License - v1.0, which accompanies this distribution, and is
available at http://www.eclipse.org/legal/cpl-v10.html
****************************************************************************/
package cae.vis;

import cae.util.FloatList;
import cae.util.IntList;
import edu.mines.jtk.dsp.Sampling;

import java.util.ArrayList;
import static java.lang.Math.*;

/**
 * The marching cubes algorithm (Lorenson and Cline, 1987).
 * This marching cubes is also adapted to be fast by implementing surface
 * tracking (Shekhar et al, 1996). This algorithm makes the assumption that
 * the surface contour is a single connected surface.
 * @author Dave Hale and Chris Engelsma, Colorado School of Mines
 * @version 2010.07.07
 */
public class MarchingCubesTracking {

  public enum Concurrency {
    PARALLEL, // TODO
    SERIAL
  };

  /**
   * A 3D surface contour.
   */
  public static class Contour {
    public float[] x;
    public float[] u;
    public int[] i;
  }

  public MarchingCubesTracking(int n1, int n2, int n3, float[][][] f) {
    this(new Sampling(n1),new Sampling(n2),new Sampling(n3),f);
  }

  /**
   * Constructs a new marching cubes dataset with given sampling values and
   * input data.
   * @param s1 sampling of the 1st dimension.
   * @param s2 sampling of the 2nd dimension.
   * @param s3 sampling of the 3rd dimension.
   * @param f 3D array of image values.
   */
  public MarchingCubesTracking(
    Sampling s1, Sampling s2, Sampling s3, float[][][] f)
  {
    _f = f;
    _s1 = s1;
    _s2 = s2;
    _s3 = s3;
  }

  /**
   * Sets the sampling for this marching cubes dataset.
   * @param s1 sampling of the 1st dimension.
   * @param s2 sampling of the 2nd dimension.
   * @param s3 sampling of the 3rd dimension.
   */
  public void setSampling(Sampling s1, Sampling s2, Sampling s3) {
    _s1 = s1;
    _s2 = s2;
    _s3 = s3;
  }

  /**
   * Sets whether to compute normals.
   * @param normals true, if computing normals; false, otherwise.
   */
  public void setNormals(boolean normals) {
    _normals = normals;
  }

  /**
   * Swaps the first and third dimension.
   * @param swap13 true, if swapping; false, otherwise.
   */
  public void setSwap13(boolean swap13) {
    _swap13 = swap13;
  }

  /**
   * Gets a surface contour for a specified value.
   * @param c a contour value to be extracted.
   * @return the extracted contour.
   */
  public Contour getContour(float c) {
    return getContour(c,-1,-1,-1,Concurrency.SERIAL);
  }

  public Contour getContourLong(float c) {
    IntList tlist = new IntList();
    FloatList xlist = new FloatList();
    FloatList ulist = _normals?new FloatList():null;
    march(_s1.getCount(),_s2.getCount(),_s3.getCount(),
          _s1.getDelta(),_s2.getDelta(),_s3.getDelta(),
          _s1.getFirst(),_s2.getFirst(),_s3.getFirst(),
          _f,c,tlist,xlist,ulist);
    Contour contour = new Contour();
    contour.i = tlist.trim();
    contour.x = xlist.trim();
    contour.u = _normals?ulist.trim():null;
    if (_swap13) {
      float[] x = contour.x;
      float[] u = contour.u;
      for (int i=x.length-3; i>=0; i-=3) {
        float x1 = x[i  ];
        float x3 = x[i+2];
        x[i  ] = x3;
        x[i+2] = x1;
        if (u!=null) {
          float u1 = u[i  ];
          float u3 = u[i+2];
          u[i  ] = u3;
          u[i+2] = u1;
        }
      }
    }
    return contour;
  }

  /**
   * Gets a surface contour for a specified value.
   * @param c a contour value to be extracted.
   * @param i3 coordinate of seed point in 3rd dimension.
   * @param i2 coordinate of seed point in 2nd dimension.
   * @param i1 coordinate of seed point in 1st dimension.
   * @return the extracted contour.
   */
  public Contour getContour(
    float c, int i3, int i2, int i1, Concurrency con)
  {
    nx = 0;

    _n3 = _s3.getCount();
    _n2 = _s2.getCount();
    _n1 = _s1.getCount();

    _d3 = _s3.getDelta();
    _d2 = _s2.getDelta();
    _d1 = _s1.getDelta();

    _f3 = _s3.getFirst();
    _f2 = _s2.getFirst();
    _f1 = _s1.getFirst();

    tlist = new IntList();
    xlist = new FloatList();
    ulist = _normals?new FloatList():null;

    _i = new int[_n3][_n2][_n1];

    int[] ix;
    if (isInBounds(i3,i2,i1)) ix = findFirstCube(c,i3,i2,i1);
    else ix = findFirstCube(c);

    if (ix!=null) marchFollow(ix[0],ix[1],ix[2],c);

    /*
    if (ix!=null && con==Concurrency.SERIAL)
      marchSerial(_f,c,_n1,_n2,_n3,_d1,_d2,_d3,_f1,_f2,_f3);
    if (ix!=null && con==Concurrency.PARALLEL)
      marchParallel();
      */

    Contour contour = new Contour();
    contour.i = tlist.trim();
    contour.x = xlist.trim();
    contour.u = _normals?ulist.trim():null;

    tlist.clear();
    xlist.clear();
    ulist.clear();

    if (_swap13) {
      float[] x = contour.x;
      float[] u = contour.u;
      for (int i=x.length-3; i>=0; i-=3) {
        float x1 = x[i  ];
        float x3 = x[i+2];
        x[i  ] = x3;
        x[i+2] = x1;
        if (u!=null) {
          float u1 = u[i  ];
          float u3 = u[i+2];
          u[i  ] = u3;
          u[i+2] = u1;
        }
      }
    }

    return contour;
  }

  ///////////////////////////////////////////////////////////////////////////
  // private

  private FloatList ulist = new FloatList();
  private FloatList xlist = new FloatList();
  private IntList tlist = new IntList();
  private Sampling _s1,_s2,_s3;
  private int _n1,_n2,_n3;
  private double _d1,_d2,_d3;
  private double _f1,_f2,_f3;
  private float[][][] _f;
  private int[][][] _i; // Surface following bookkeeping array
  private Index vi = new Index();
  private boolean _normals = true;
  private boolean _swap13 = true;
  private int nx = 0;

  private void marchSerial(
    float[][][] f, float c,
    int n1, int n2, int n3,
    double d1, double d2, double d3,
    double f1, double f2, double f3)
  {
    System.out.println("Solving Serial");
    int nx = 0;
    for (int i3=0; i3<_n3-1; ++i3) {
      marchSlab(i3,_f,c,n1,n2,n3,d1,d2,d3,f1,f2,f3,nx);
    }
  }

  private void marchParallel() {

  }

  private void marchSlab(
    int i3, float[][][] f, float c,
    int n1, int n2, int n3,
    double d1, double d2, double d3,
    double f1, double f2, double f3,
    int nx)
  {
    float[] u = new float[3];
    for (int i2=0; i2<n2-1; ++i2) {
      for (int i1=0; i1<n1-1; ++i1) {

      }
    }
  }


    private void march(
    int n1, int n2, int n3,
    double d1, double d2, double d3,
    double f1, double f2, double f3,
    float[][][] f, float c,
    IntList tlist, FloatList xlist, FloatList ulist)
  {
    float[] u = new float[3];

    // Arrays of indices of vertices computed only once and stored in lists.
    // The two arrays ixa and ixb contain indices for one 2D slab of cubes.
    // A non-negative index in either array points to a computed vertex, and
    // a triangle is represented by three such indices. Here we initialize
    // all of the indices to -1, since no vertices have yet been computed.
    int[][][] ixa = new int[n2][n1][3];
    int[][][] ixb = new int[n2][n1][3];
    for (int i2=0; i2<n2; ++i2) {
      for (int i1=0; i1<n1; ++i1) {
        for (int kk=0; kk<3; ++kk) {
          ixa[i2][i1][kk] = -1;
          ixb[i2][i1][kk] = -1;
        }
      }
    }

    // Numbers of vertices (and normal vectors) and triangle.
    int nx = 0;
    int nt = 0;

    // For all slabs of cubes, ...
    for (int i3=0; i3<n3-1; ++i3) {

      // For all cubes in this slab, ...
      for (int i2=0; i2<n2-1; ++i2) {
        for (int i1=0; i1<n1-1; ++i1) {

          // Eight corner values for this cube.
          float c0 = f[i3  ][i2  ][i1  ];
          float c1 = f[i3  ][i2  ][i1+1];
          float c2 = f[i3  ][i2+1][i1+1];
          float c3 = f[i3  ][i2+1][i1  ];
          float c4 = f[i3+1][i2  ][i1  ];
          float c5 = f[i3+1][i2  ][i1+1];
          float c6 = f[i3+1][i2+1][i1+1];
          float c7 = f[i3+1][i2+1][i1  ];

          // Case index for this cube.
          int caseIndex = 0;
          if (c0>c) caseIndex +=   1;
          if (c1>c) caseIndex +=   2;
          if (c2>c) caseIndex +=   4;
          if (c3>c) caseIndex +=   8;
          if (c4>c) caseIndex +=  16;
          if (c5>c) caseIndex +=  32;
          if (c6>c) caseIndex +=  64;
          if (c7>c) caseIndex += 128;

          // If at least one triangle for this case, ...
          if (caseIndex>0 && caseIndex<255) {

            // Edges intersected by contour.
            int[] edges = _edges[caseIndex];
            int ne = edges.length;

            // For all triangles (triplets of edge intersections), ...
            for (int ie=0; ie<ne; ie+=3) {

              // For each of three triangle vertices, ...
              for (int je=0; je<3; ++je) {

                // Decode edge j->k into sample indices of j and axis to k.
                int edge = edges[ie+je];
                float cj,ck;
                int j1,j2,j3,kk;
                switch(edge) {
                case 0: // 0->1
                  cj = c0;
                  ck = c1;
                  j1 = i1;
                  j2 = i2;
                  j3 = i3;
                  kk = 0;
                  break;
                case 1: // 1->2
                  cj = c1;
                  ck = c2;
                  j1 = i1+1;
                  j2 = i2;
                  j3 = i3;
                  kk = 1;
                  break;
                case 2: // 3->2
                  cj = c3;
                  ck = c2;
                  j1 = i1;
                  j2 = i2+1;
                  j3 = i3;
                  kk = 0;
                  break;
                case 3: // 0->3
                  cj = c0;
                  ck = c3;
                  j1 = i1;
                  j2 = i2;
                  j3 = i3;
                  kk = 1;
                  break;
                case 4: // 4->5
                  cj = c4;
                  ck = c5;
                  j1 = i1;
                  j2 = i2;
                  j3 = i3+1;
                  kk = 0;
                  break;
                case 5: // 5->6
                  cj = c5;
                  ck = c6;
                  j1 = i1+1;
                  j2 = i2;
                  j3 = i3+1;
                  kk = 1;
                  break;
                case 6: // 7->6
                  cj = c7;
                  ck = c6;
                  j1 = i1;
                  j2 = i2+1;
                  j3 = i3+1;
                  kk = 0;
                  break;
                case 7: // 4->7
                  cj = c4;
                  ck = c7;
                  j1 = i1;
                  j2 = i2;
                  j3 = i3+1;
                  kk = 1;
                  break;
                case 8: // 0->4
                  cj = c0;
                  ck = c4;
                  j1 = i1;
                  j2 = i2;
                  j3 = i3;
                  kk = 2;
                  break;
                case 9: // 1->5
                  cj = c1;
                  ck = c5;
                  j1 = i1+1;
                  j2 = i2;
                  j3 = i3;
                  kk = 2;
                  break;
                case 10: // 3->7
                  cj = c3;
                  ck = c7;
                  j1 = i1;
                  j2 = i2+1;
                  j3 = i3;
                  kk = 2;
                  break;
                default: // 2->6
                  cj = c2;
                  ck = c6;
                  j1 = i1+1;
                  j2 = i2+1;
                  j3 = i3;
                  kk = 2;
                }

                // Index of vertex, if already computed; or -1, if not yet.
                int[] ixjj = (j3==i3)?ixa[j2][j1]:ixb[j2][j1];
                int ix = ixjj[kk];

                // If vertex not yet computed, compute and store coordinates,
                // and optionally compute and store normal vector components.
                if (ix<0) {
                  int k1,k2,k3;
                  double x1,x2,x3;
                  float dx = (c-cj)/(ck-cj);
                  switch(kk) {
                  case 0: // edge aligned with axis 1
                    k1 = j1+1;
                    k2 = j2;
                    k3 = j3;
                    x1 = f1+d1*(j1+dx);
                    x2 = f2+d2*(j2   );
                    x3 = f3+d3*(j3   );
                    break;
                  case 1: // edge aligned with axis 2
                    k1 = j1;
                    k2 = j2+1;
                    k3 = j3;
                    x1 = f1+d1*(j1   );
                    x2 = f2+d2*(j2+dx);
                    x3 = f3+d3*(j3   );
                    break;
                  default: // edge aligned with axis 3
                    k1 = j1;
                    k2 = j2;
                    k3 = j3+1;
                    x1 = f1+d1*(j1   );
                    x2 = f2+d2*(j2   );
                    x3 = f3+d3*(j3+dx);
                  }
                  ix = ixjj[kk] = nx;
                  xlist.add((float)x1);
                  xlist.add((float)x2);
                  xlist.add((float)x3);
                  ++nx;
                  if (ulist!=null) {
                    computeNormalVector(j1,j2,j3,k1,k2,k3,
                                        n1,n2,n3,d1,d2,d3,
                                        dx,f,u);
                    ulist.add(u[0]);
                    ulist.add(u[1]);
                    ulist.add(u[2]);
                  }
                }

                // Append index of vertex to triangle list.
                tlist.add(ix);
              }

              // Number of triangles.
              ++nt;
            }
          }
        }
      }

      // Swap the index arrays ixa and ixb, and re-initialize all ixb to -1.
      int[][][] ixt = ixa;
      ixa = ixb;
      ixb = ixt;
      for (int i2=0; i2<n2; ++i2) {
        for (int i1=0; i1<n1; ++i1) {
          for (int kk=0; kk<3; ++kk) {
            ixb[i2][i1][kk] = -1;
          }
        }
      }
    }
  }

  /*
   *  Labeling convention for this code:
   *
   *  Vertices:
   *
   *          v7---------v6
   *         .|         . |
   *       .  |       .   |
   *     v3---------v2    |
   *     |    v4-----|---v5
   *     |   .       |  .
   *     | .         |.
   *     v0---------v1
   *
   *  Edges:
   *           ----e6-----
   *    e10  .|      e11. |
   *       . e7       .  e5
   *      -----e2----     |
   *     |     ----e4|----
   *    e3   .      e1  .
   *     |.e8        |.  e9
   *      ----e0-----
   *
   *  Sides:
   *           -----------  s5
   *         .          . |
   *       .    s5    .   |
   *      -----------  s1 |
   *  s3 |           |
   *     |     s0    |  .
   *     |           |.
   *      -----------
   *            s4
   */

  /**
   * Marches the cubes.
   * Cubes are marched using surface tracking. This means that the next cubes
   * in the recursion are determined by the edge intersections of the current
   * cube. This algorithm works ideally for continuous surfaces.
   * @param i3 index value in dimension three.
   * @param i2 index value in dimension two.
   * @param i1 index value in dimension one.
   * @param c contour value.
   */
  private void marchFollow(int i3, int i2, int i1, float c) {
    float[] u = new float[3];

    int[] v1 = new int[3];
    int[] v2 = new int[3];

    // Eight corner values
    float c0 = _f[i3  ][i2  ][i1  ];
    float c1 = _f[i3  ][i2  ][i1+1];
    float c2 = _f[i3  ][i2+1][i1+1];
    float c3 = _f[i3  ][i2+1][i1  ];
    float c4 = _f[i3+1][i2  ][i1  ];
    float c5 = _f[i3+1][i2  ][i1+1];
    float c6 = _f[i3+1][i2+1][i1+1];
    float c7 = _f[i3+1][i2+1][i1  ];

    boolean s0 = false, s1 = false, s2 = false;
    boolean s3 = false, s4 = false, s5 = false;

    // Case index.
    int ci = 0;
    if (c0>c) ci +=   1;
    if (c1>c) ci +=   2;
    if (c2>c) ci +=   4;
    if (c3>c) ci +=   8;
    if (c4>c) ci +=  16;
    if (c5>c) ci +=  32;
    if (c6>c) ci +=  64;
    if (c7>c) ci += 128;

    if (ci>0 && ci<255) {

      // Look up edges intersected.
      int[] e = _edges[ci];
      int ne = e.length;

      // For all triangles
      for (int ie=0; ie<ne; ie+=3) {

        // For each of three triangle vertices
        for (int je=0; je<3; ++je) {
          int edge = e[ie+je];
          float cj,ck;
          int j1,j2,j3,kk;
          switch(edge) {
            case 0: // 0->1
              cj = c0;
              ck = c1;
              j1 = i1;
              j2 = i2;
              j3 = i3;
              kk = 0;
              s0 = s4 = true;
              break;
            case 1: // 1->2
              cj = c1;
              ck = c2;
              j1 = i1+1;
              j2 = i2;
              j3 = i3;
              kk = 1;
              s0 = s1 = true;
              break;
            case 2: // 3->2
              cj = c3;
              ck = c2;
              j1 = i1;
              j2 = i2+1;
              j3 = i3;
              kk = 0;
              s0 = s5 = true;
              break;
            case 3: // 0->3
              cj = c0;
              ck = c3;
              j1 = i1;
              j2 = i2;
              j3 = i3;
              kk = 1;
              s0 = s3 = true;
              break;
            case 4: // 4->5
              cj = c4;
              ck = c5;
              j1 = i1;
              j2 = i2;
              j3 = i3+1;
              kk = 0;
              s4 = s2 = true;
              break;
            case 5: // 5->6
              cj = c5;
              ck = c6;
              j1 = i1+1;
              j2 = i2;
              j3 = i3+1;
              kk = 1;
              s1 = s2 = true;
              break;
            case 6: // 7->6
              cj = c7;
              ck = c6;
              j1 = i1;
              j2 = i2+1;
              j3 = i3+1;
              kk = 0;
              s2 = s5 = true;
              break;
            case 7: // 4->7
              cj = c4;
              ck = c7;
              j1 = i1;
              j2 = i2;
              j3 = i3+1;
              kk = 1;
              s3 = s2 = true;
              break;
            case 8: // 0-> 4
              cj = c0;
              ck = c4;
              j1 = i1;
              j2 = i2;
              j3 = i3;
              kk = 2;
              s4 = s3 = true;
              break;
            case 9: // 1->5
              cj = c1;
              ck = c5;
              j1 = i1+1;
              j2 = i2;
              j3 = i3;
              kk = 2;
              s4 = s1 = true;
              break;
            case 10: // 3->7
              cj = c3;
              ck = c7;
              j1 = i1;
              j2 = i2+1;
              j3 = i3;
              kk = 2;
              s5 = s3 = true;
              break;
            default: // 2->6
              cj = c2;
              ck = c6;
              j1 = i1+1;
              j2 = i2+1;
              j3 = i3;
              kk = 2;
              s1 = s5 = true;
          }

          v1[0] = j1; v1[1] = j2; v1[2] = j3;
          v2[0] = j1; v2[1] = j2; v2[2] = j3;
          v2[kk] += 1;
            int k1,k2,k3;
            double x1,x2,x3;
            float dx = (c-cj)/(ck-cj);
            switch(kk) {
              case 0: // edge aligned with axis 1
                k1 = j1+1;
                k2 = j2;
                k3 = j3;
                x1 = _f1+_d1*(j1+dx);
                x2 = _f2+_d2*(j2   );
                x3 = _f3+_d3*(j3   );
                break;
              case 1: // edge aligned with axis 2
                k1 = j1;
                k2 = j2+1;
                k3 = j3;
                x1 = _f1+_d1*(j1   );
                x2 = _f2+_d2*(j2+dx);
                x3 = _f3+_d3*(j3   );
                break;
              default: // edge aligned with axis 3
                k1 = j1;
                k2 = j2;
                k3 = j3+1;
                x1 = _f1+_d1*(j1   );
                x2 = _f2+_d2*(j2   );
                x3 = _f3+_d3*(j3+dx);
            }
            vi.addIndex(v1,v2,nx++);
            xlist.add((float)x1);
            xlist.add((float)x2);
            xlist.add((float)x3);
            if (ulist!=null) {
              computeNormalVector(j1,j2,j3,k1,k2,k3,
                                 _s1.getCount(),_s2.getCount(),_s3.getCount(),
                                 _s1.getDelta(),_s2.getDelta(),_s3.getDelta(),
                                 dx,_f,u);
              ulist.add(u[0]);
              ulist.add(u[1]);
              ulist.add(u[2]);
            }
          // Append index of vertex to triangle list
          tlist.add(nx);
        }
      }

      // Determines which direction to continue.
      for (int ie=0; ie<ne; ie++) {
        if (e[ie]== 0) s0 = s4 = true;
        if (e[ie]== 1) s0 = s1 = true;
        if (e[ie]== 2) s0 = s5 = true;
        if (e[ie]== 3) s0 = s3 = true;
        if (e[ie]== 4) s2 = s4 = true;
        if (e[ie]== 5) s2 = s1 = true;
        if (e[ie]== 6) s2 = s5 = true;
        if (e[ie]== 7) s2 = s3 = true;
        if (e[ie]== 8) s4 = s3 = true;
        if (e[ie]== 9) s4 = s1 = true;
        if (e[ie]==10) s5 = s3 = true;
        if (e[ie]==11) s5 = s1 = true;
      }

      // Mark this index as done.
      _i[i3][i2][i1] = 1;

      // Recursion
      if (s0 && canContinue(i3-1,i2,i1)) marchFollow(i3-1,i2,i1,c);
      if (s2 && canContinue(i3+1,i2,i1)) marchFollow(i3+1,i2,i1,c);
      if (s3 && canContinue(i3,i2-1,i1)) marchFollow(i3,i2-1,i1,c);
      if (s1 && canContinue(i3,i2+1,i1)) marchFollow(i3,i2+1,i1,c);
      if (s4 && canContinue(i3,i2,i1-1)) marchFollow(i3,i2,i1-1,c);
      if (s5 && canContinue(i3,i2,i1+1)) marchFollow(i3,i2,i1+1,c);
    }
  }

  /**
   * Searches the dataset for the marching cubes starting point the slow way.
   * This is implemented if no seed point is provided.
   * @param c the contour value.
   * @return the (z,y,x) of the starting index; null, if none.
   */
  private int[] findFirstCube(float c) {
    for (int i3=0; i3<_n3-1; ++i3) {
      for (int i2=0; i2<_n2-1; ++i2) {
        for (int i1=0; i1<_n1-1; ++i1) {
          if (isValidCube(i3,i2,i1,c))
            return new int[]{i3,i2,i1};
        }
      }
    }
    return null;
  }

  /**
   * Searches for a starting point to begin the recursion.
   * This technique works best if searching for a closed body, and can be
   * much faster than searching from zero.
   * @param c the contour value.
   * @param i3 the index in the 3rd dimension.
   * @param i2 the index in the 2nd dimension.
   * @param i1 the index in the 1st dimension.
   * @return the (z,y,x) of the starting index; null, if none.
   */
  private int[] findFirstCube(float c, int i3, int i2, int i1) {
    int ii1 = i1;
    while (isInBounds(i3,i2,ii1)) {
      if (isValidCube(i3,i2,ii1++,c))
        return new int[]{i3,i2,ii1};
    }
    ii1 = i1;
    while (isInBounds(i3,i2,ii1)) {
      if (isValidCube(i3,i2,ii1--,c))
        return new int[]{i3,i2,ii1};
    }
    return null;
  }

  /**
   * Determines whether a cube intersects the surface contour.
   * We first assign an index 0 outside contour, and index 1 inside the
   * contour. We then sum up all 8 indices -- if the added value is anything
   * but 0 or 8 it is a valid cube.
   * @param i3 the index in dimension-3.
   * @param i2 the index in dimension-2.
   * @param i1 the index in dimension-1.
   * @param c the surface contour value.
   * @return true, if valid; false, otherwise.
   */
  private boolean isValidCube(int i3, int i2, int i1, float c) {
    int ci = 0;
    if (_f[i3  ][i2  ][i1  ]>c) ci++;
    if (_f[i3  ][i2  ][i1+1]>c) ci++;
    if (_f[i3  ][i2+1][i1+1]>c) ci++;
    if (_f[i3  ][i2+1][i1  ]>c) ci++;
    if (_f[i3+1][i2  ][i1  ]>c) ci++;
    if (_f[i3+1][i2  ][i1+1]>c) ci++;
    if (_f[i3+1][i2+1][i1+1]>c) ci++;
    if (_f[i3+1][i2+1][i1  ]>c) ci++;

    if (ci==0 || ci==8) return false;
    if (!canContinue(i3,i2,i1)) return false;

    return true;
  }

  private boolean canContinue(int i3, int i2, int i1) {
    // Is this cube in bounds?
    if (!isInBounds(i3,i2,i1)) return false;

    // Has this cube been marked yet?
    if (_i[i3][i2][i1] == 1) return false;

    return true; // Both tests pass
  }

  // TODO Hashmap.
  private class Index {
    ArrayList<int[]> al = new ArrayList<int[]>(); // Contains the ub&lb
    IntList il = new IntList(); // Contains the index

    int getIndex(int[] s1, int[] s2) {
      for (int i=0; i<il.n; ++i) {
        int[] i1 = al.get(2*i+0);
        int[] i2 = al.get(2*i+1);
        if (eq(s1,i1) && eq(s2,i2)) {
          return il.get(i);
        }
      }
      return -1;
    }

    void addIndex(int[] v1, int[] v2, int i) {
      al.add(v1);
      al.add(v2);
      il.add(i);
    }

    void clear() {
      al.clear();
      il.clear();
    }

    private boolean eq(int[] a, int[] b) {
      return (a[0]==b[0] && a[1]==b[1] && a[2]==b[2])?true:false;
    }
  }


    private void computeNormalVector(
    int j1, int j2, int j3, int k1, int k2, int k3,
    int n1, int n2, int n3, double d1, double d2, double d3,
    double dx, float[][][] f, float[] u)
  {
    double u1,u2,u3;
    double v1,v2,v3;
    if (j1==0) {
      u1 = (f[j3][j2][j1+1]-f[j3][j2][j1  ]);
    } else if (j1==n1-1) {
      u1 = (f[j3][j2][j1  ]-f[j3][j2][j1-1]);
    } else {
      u1 = (f[j3][j2][j1+1]-f[j3][j2][j1-1])*0.5;
    }
    if (k1==0) {
      v1 = (f[k3][k2][k1+1]-f[k3][k2][k1  ]);
    } else if (k1==n1-1) {
      v1 = (f[k3][k2][k1  ]-f[k3][k2][k1-1]);
    } else {
      v1 = (f[k3][k2][k1+1]-f[k3][k2][k1-1])*0.5;
    }
    if (j2==0) {
      u2 = (f[j3][j2+1][j1]-f[j3][j2  ][j1]);
    } else if (j2==n2-1) {
      u2 = (f[j3][j2  ][j1]-f[j3][j2-1][j1]);
    } else {
      u2 = (f[j3][j2+1][j1]-f[j3][j2-1][j1])*0.5;
    }
    if (k2==0) {
      v2 = (f[k3][k2+1][k1 ]-f[k3][k2  ][k1]);
    } else if (k2==n2-1) {
      v2 = (f[k3][k2  ][k1 ]-f[k3][k2-1][k1]);
    } else {
      v2 = (f[k3][k2+1][k1]-f[k3][k2-1][k1])*0.5;
    }
    if (j3==0) {
      u3 = (f[j3+1][j2][j1]-f[j3  ][j2][j1]);
    } else if (j3==n3-1) {
      u3 = (f[j3  ][j2][j1]-f[j3-1][j2][j1]);
    } else {
      u3 = (f[j3+1][j2][j1]-f[j3-1][j2][j1])*0.5;
    }
    if (k3==0) {
      v3 = (f[k3+1][k2][k1]-f[k3  ][k2][k1]);
    } else if (k3==n3-1) {
      v3 = (f[k3  ][k2][k1]-f[k3-1][k2][k1]);
    } else {
      v3 = (f[k3+1][k2][k1]-f[k3-1][k2][k1])*0.5;
    }
    u1 = (u1+(v1-u1)*dx)/d1;
    u2 = (u2+(v2-u2)*dx)/d2;
    u3 = (u3+(v3-u3)*dx)/d3;
    double us = 1.0/sqrt(u1*u1+u2*u2+u3*u3);
    u[0] = (float)(u1*us);
    u[1] = (float)(u2*us);
    u[2] = (float)(u3*us);
  }

  private boolean isInBounds(int i3, int i2, int i1) {
    return (i3>=0 && i2>=0 && i1>=0 && i3<_n3-1 && i2<_n2-1 && i1<_n1-1);
  }

  /* Edges intersected. Each group of three indices represents a triangle.
   * For the eight sample values in each cube, there are 256 cases. However,
   * most of those 256 cases are complements or rotations of 16 base cases.
   * Comments at end of each line are case number and base-case number.
   * This table was adopted from one in VTK, the Visualization Toolkit.
   */
  private static final int[][] _edges = {
    {}, // 0 0
    { 0, 3, 8}, // 1 1
    { 0, 9, 1}, // 2 1
    { 1, 3, 8, 9, 1, 8}, // 3 2
    { 1, 11, 2}, // 4 1
    { 0, 3, 8, 1, 11, 2}, // 5 3
    { 9, 11, 2, 0, 9, 2}, // 6 2
    { 2, 3, 8, 2, 8, 11, 11, 8, 9}, // 7 5
    { 3, 2, 10}, // 8 1
    { 0, 2, 10, 8, 0, 10}, // 9 2
    { 1, 0, 9, 2, 10, 3}, // 10 3
    { 1, 2, 10, 1, 10, 9, 9, 10, 8}, // 11 5
    { 3, 1, 11, 10, 3, 11}, // 12 2
    { 0, 1, 11, 0, 11, 8, 8, 11, 10}, // 13 5
    { 3, 0, 9, 3, 9, 10, 10, 9, 11}, // 14 5
    { 9, 11, 8, 11, 10, 8}, // 15 8
    { 4, 8, 7}, // 16 1
    { 4, 0, 3, 7, 4, 3}, // 17 2
    { 0, 9, 1, 8, 7, 4}, // 18 3
    { 4, 9, 1, 4, 1, 7, 7, 1, 3}, // 19 5
    { 1, 11, 2, 8, 7, 4}, // 20 4
    { 3, 7, 4, 3, 4, 0, 1, 11, 2}, // 21 7
    { 9, 11, 2, 9, 2, 0, 8, 7, 4}, // 22 7
    { 2, 9, 11, 2, 7, 9, 2, 3, 7, 7, 4, 9}, // 23 14
    { 8, 7, 4, 3, 2, 10}, // 24 3
    {10, 7, 4, 10, 4, 2, 2, 4, 0}, // 25 5
    { 9, 1, 0, 8, 7, 4, 2, 10, 3}, // 26 6
    { 4, 10, 7, 9, 10, 4, 9, 2, 10, 9, 1, 2}, // 27 9
    { 3, 1, 11, 3, 11, 10, 7, 4, 8}, // 28 7
    { 1, 11, 10, 1, 10, 4, 1, 4, 0, 7, 4, 10}, // 29 11
    { 4, 8, 7, 9, 10, 0, 9, 11, 10, 10, 3, 0}, // 30 12
    { 4, 10, 7, 4, 9, 10, 9, 11, 10}, // 31 5
    { 9, 4, 5}, // 32 1
    { 9, 4, 5, 0, 3, 8}, // 33 3
    { 0, 4, 5, 1, 0, 5}, // 34 2
    { 8, 4, 5, 8, 5, 3, 3, 5, 1}, // 35 5
    { 1, 11, 2, 9, 4, 5}, // 36 3
    { 3, 8, 0, 1, 11, 2, 4, 5, 9}, // 37 6
    { 5, 11, 2, 5, 2, 4, 4, 2, 0}, // 38 5
    { 2, 5, 11, 3, 5, 2, 3, 4, 5, 3, 8, 4}, // 39 9
    { 9, 4, 5, 2, 10, 3}, // 40 4
    { 0, 2, 10, 0, 10, 8, 4, 5, 9}, // 41 7
    { 0, 4, 5, 0, 5, 1, 2, 10, 3}, // 42 7
    { 2, 5, 1, 2, 8, 5, 2, 10, 8, 4, 5, 8}, // 43 11
    {11, 10, 3, 11, 3, 1, 9, 4, 5}, // 44 7
    { 4, 5, 9, 0, 1, 8, 8, 1, 11, 8, 11, 10}, // 45 12
    { 5, 0, 4, 5, 10, 0, 5, 11, 10, 10, 3, 0}, // 46 14
    { 5, 8, 4, 5, 11, 8, 11, 10, 8}, // 47 5
    { 9, 8, 7, 5, 9, 7}, // 48 2
    { 9, 0, 3, 9, 3, 5, 5, 3, 7}, // 49 5
    { 0, 8, 7, 0, 7, 1, 1, 7, 5}, // 50 5
    { 1, 3, 5, 3, 7, 5}, // 51 8
    { 9, 8, 7, 9, 7, 5, 11, 2, 1}, // 52 7
    {11, 2, 1, 9, 0, 5, 5, 0, 3, 5, 3, 7}, // 53 12
    { 8, 2, 0, 8, 5, 2, 8, 7, 5, 11, 2, 5}, // 54 11
    { 2, 5, 11, 2, 3, 5, 3, 7, 5}, // 55 5
    { 7, 5, 9, 7, 9, 8, 3, 2, 10}, // 56 7
    { 9, 7, 5, 9, 2, 7, 9, 0, 2, 2, 10, 7}, // 57 14
    { 2, 10, 3, 0, 8, 1, 1, 8, 7, 1, 7, 5}, // 58 12
    {10, 1, 2, 10, 7, 1, 7, 5, 1}, // 59 5
    { 9, 8, 5, 8, 7, 5, 11, 3, 1, 11, 10, 3}, // 60 10
    { 5, 0, 7, 5, 9, 0, 7, 0, 10, 1, 11, 0, 10, 0, 11}, // 61 7
    {10, 0, 11, 10, 3, 0, 11, 0, 5, 8, 7, 0, 5, 0, 7}, // 62 7
    {10, 5, 11, 7, 5, 10}, // 63 2
    {11, 5, 6}, // 64 1
    { 0, 3, 8, 5, 6, 11}, // 65 4
    { 9, 1, 0, 5, 6, 11}, // 66 3
    { 1, 3, 8, 1, 8, 9, 5, 6, 11}, // 67 7
    { 1, 5, 6, 2, 1, 6}, // 68 2
    { 1, 5, 6, 1, 6, 2, 3, 8, 0}, // 69 7
    { 9, 5, 6, 9, 6, 0, 0, 6, 2}, // 70 5
    { 5, 8, 9, 5, 2, 8, 5, 6, 2, 3, 8, 2}, // 71 11
    { 2, 10, 3, 11, 5, 6}, // 72 3
    {10, 8, 0, 10, 0, 2, 11, 5, 6}, // 73 7
    { 0, 9, 1, 2, 10, 3, 5, 6, 11}, // 74 6
    { 5, 6, 11, 1, 2, 9, 9, 2, 10, 9, 10, 8}, // 75 12
    { 6, 10, 3, 6, 3, 5, 5, 3, 1}, // 76 5
    { 0, 10, 8, 0, 5, 10, 0, 1, 5, 5, 6, 10}, // 77 14
    { 3, 6, 10, 0, 6, 3, 0, 5, 6, 0, 9, 5}, // 78 9
    { 6, 9, 5, 6, 10, 9, 10, 8, 9}, // 79 5
    { 5, 6, 11, 4, 8, 7}, // 80 3
    { 4, 0, 3, 4, 3, 7, 6, 11, 5}, // 81 7
    { 1, 0, 9, 5, 6, 11, 8, 7, 4}, // 82 6
    {11, 5, 6, 1, 7, 9, 1, 3, 7, 7, 4, 9}, // 83 12
    { 6, 2, 1, 6, 1, 5, 4, 8, 7}, // 84 7
    { 1, 5, 2, 5, 6, 2, 3, 4, 0, 3, 7, 4}, // 85 10
    { 8, 7, 4, 9, 5, 0, 0, 5, 6, 0, 6, 2}, // 86 12
    { 7, 9, 3, 7, 4, 9, 3, 9, 2, 5, 6, 9, 2, 9, 6}, // 87 7
    { 3, 2, 10, 7, 4, 8, 11, 5, 6}, // 88 6
    { 5, 6, 11, 4, 2, 7, 4, 0, 2, 2, 10, 7}, // 89 12
    { 0, 9, 1, 4, 8, 7, 2, 10, 3, 5, 6, 11}, // 90 13
    { 9, 1, 2, 9, 2, 10, 9, 10, 4, 7, 4, 10, 5, 6, 11}, // 91 6
    { 8, 7, 4, 3, 5, 10, 3, 1, 5, 5, 6, 10}, // 92 12
    { 5, 10, 1, 5, 6, 10, 1, 10, 0, 7, 4, 10, 0, 10, 4}, // 93 7
    { 0, 9, 5, 0, 5, 6, 0, 6, 3, 10, 3, 6, 8, 7, 4}, // 94 6
    { 6, 9, 5, 6, 10, 9, 4, 9, 7, 7, 9, 10}, // 95 3
    {11, 9, 4, 6, 11, 4}, // 96 2
    { 4, 6, 11, 4, 11, 9, 0, 3, 8}, ///97 7
    {11, 1, 0, 11, 0, 6, 6, 0, 4}, // 98 5
    { 8, 1, 3, 8, 6, 1, 8, 4, 6, 6, 11, 1}, // 99 14
    { 1, 9, 4, 1, 4, 2, 2, 4, 6}, // 100 5
    { 3, 8, 0, 1, 9, 2, 2, 9, 4, 2, 4, 6}, // 101 12
    { 0, 4, 2, 4, 6, 2}, // 102 8
    { 8, 2, 3, 8, 4, 2, 4, 6, 2}, // 103 5
    {11, 9, 4, 11, 4, 6, 10, 3, 2}, // 104 7
    { 0, 2, 8, 2, 10, 8, 4, 11, 9, 4, 6, 11}, // 105 10
    { 3, 2, 10, 0, 6, 1, 0, 4, 6, 6, 11, 1}, // 106 12
    { 6, 1, 4, 6, 11, 1, 4, 1, 8, 2, 10, 1, 8, 1, 10}, // 107 7
    { 9, 4, 6, 9, 6, 3, 9, 3, 1, 10, 3, 6}, // 108 11
    { 8, 1, 10, 8, 0, 1, 10, 1, 6, 9, 4, 1, 6, 1, 4}, // 109 7
    { 3, 6, 10, 3, 0, 6, 0, 4, 6}, // 110 5
    { 6, 8, 4, 10, 8, 6}, // 111 2
    { 7, 6, 11, 7, 11, 8, 8, 11, 9}, // 112 5
    { 0, 3, 7, 0, 7, 11, 0, 11, 9, 6, 11, 7}, // 113 11
    {11, 7, 6, 1, 7, 11, 1, 8, 7, 1, 0, 8}, // 114 9
    {11, 7, 6, 11, 1, 7, 1, 3, 7}, // 115 5
    { 1, 6, 2, 1, 8, 6, 1, 9, 8, 8, 7, 6}, // 116 14
    { 2, 9, 6, 2, 1, 9, 6, 9, 7, 0, 3, 9, 7, 9, 3}, // 117 7
    { 7, 0, 8, 7, 6, 0, 6, 2, 0}, // 118 5
    { 7, 2, 3, 6, 2, 7}, // 119 2
    { 2, 10, 3, 11, 8, 6, 11, 9, 8, 8, 7, 6}, // 120 12
    { 2, 7, 0, 2, 10, 7, 0, 7, 9, 6, 11, 7, 9, 7, 11}, // 121 7
    { 1, 0, 8, 1, 8, 7, 1, 7, 11, 6, 11, 7, 2, 10, 3}, // 122 6
    {10, 1, 2, 10, 7, 1, 11, 1, 6, 6, 1, 7}, // 123 3
    { 8, 6, 9, 8, 7, 6, 9, 6, 1, 10, 3, 6, 1, 6, 3}, // 124 7
    { 0, 1, 9, 10, 7, 6}, // 125 4
    { 7, 0, 8, 7, 6, 0, 3, 0, 10, 10, 0, 6}, // 126 3
    { 7, 6, 10}, // 127 1
    { 7, 10, 6}, // 128 1
    { 3, 8, 0, 10, 6, 7}, // 129 3
    { 0, 9, 1, 10, 6, 7}, // 130 4
    { 8, 9, 1, 8, 1, 3, 10, 6, 7}, // 131 7
    {11, 2, 1, 6, 7, 10}, // 132 3
    { 1, 11, 2, 3, 8, 0, 6, 7, 10}, // 133 6
    { 2, 0, 9, 2, 9, 11, 6, 7, 10}, // 134 7
    { 6, 7, 10, 2, 3, 11, 11, 3, 8, 11, 8, 9}, // 135 12
    { 7, 3, 2, 6, 7, 2}, // 136 2
    { 7, 8, 0, 7, 0, 6, 6, 0, 2}, // 137 5
    { 2, 6, 7, 2, 7, 3, 0, 9, 1}, // 138 7
    { 1, 2, 6, 1, 6, 8, 1, 8, 9, 8, 6, 7}, // 139 14
    {11, 6, 7, 11, 7, 1, 1, 7, 3}, // 140 5
    {11, 6, 7, 1, 11, 7, 1, 7, 8, 1, 8, 0}, // 141 9
    { 0, 7, 3, 0, 11, 7, 0, 9, 11, 6, 7, 11}, // 142 11
    { 7, 11, 6, 7, 8, 11, 8, 9, 11}, // 143 5
    { 6, 4, 8, 10, 6, 8}, // 144 2
    { 3, 10, 6, 3, 6, 0, 0, 6, 4}, // 145 5
    { 8, 10, 6, 8, 6, 4, 9, 1, 0}, // 146 7
    { 9, 6, 4, 9, 3, 6, 9, 1, 3, 10, 6, 3}, // 147 11
    { 6, 4, 8, 6, 8, 10, 2, 1, 11}, // 148 7
    { 1, 11, 2, 3, 10, 0, 0, 10, 6, 0, 6, 4}, // 149 12
    { 4, 8, 10, 4, 10, 6, 0, 9, 2, 2, 9, 11}, // 150 10
    {11, 3, 9, 11, 2, 3, 9, 3, 4, 10, 6, 3, 4, 3, 6}, // 151 7
    { 8, 3, 2, 8, 2, 4, 4, 2, 6}, // 152 5
    { 0, 2, 4, 4, 2, 6}, // 153 8
    { 1, 0, 9, 2, 4, 3, 2, 6, 4, 4, 8, 3}, // 154 12
    { 1, 4, 9, 1, 2, 4, 2, 6, 4}, // 155 5
    { 8, 3, 1, 8, 1, 6, 8, 6, 4, 6, 1, 11}, // 156 14
    {11, 0, 1, 11, 6, 0, 6, 4, 0}, // 157 5
    { 4, 3, 6, 4, 8, 3, 6, 3, 11, 0, 9, 3, 11, 3, 9}, // 158 7
    {11, 4, 9, 6, 4, 11}, // 159 2
    { 4, 5, 9, 7, 10, 6}, // 160 3
    { 0, 3, 8, 4, 5, 9, 10, 6, 7}, // 161 6
    { 5, 1, 0, 5, 0, 4, 7, 10, 6}, // 162 7
    {10, 6, 7, 8, 4, 3, 3, 4, 5, 3, 5, 1}, // 163 12
    { 9, 4, 5, 11, 2, 1, 7, 10, 6}, // 164 6
    { 6, 7, 10, 1, 11, 2, 0, 3, 8, 4, 5, 9}, // 165 13
    { 7, 10, 6, 5, 11, 4, 4, 11, 2, 4, 2, 0}, // 166 12
    { 3, 8, 4, 3, 4, 5, 3, 5, 2, 11, 2, 5, 10, 6, 7}, // 167 6
    { 7, 3, 2, 7, 2, 6, 5, 9, 4}, // 168 7
    { 9, 4, 5, 0, 6, 8, 0, 2, 6, 6, 7, 8}, // 169 12
    { 3, 2, 6, 3, 6, 7, 1, 0, 5, 5, 0, 4}, // 170 10
    { 6, 8, 2, 6, 7, 8, 2, 8, 1, 4, 5, 8, 1, 8, 5}, // 171 7
    { 9, 4, 5, 11, 6, 1, 1, 6, 7, 1, 7, 3}, // 172 12
    { 1, 11, 6, 1, 6, 7, 1, 7, 0, 8, 0, 7, 9, 4, 5}, // 173 6
    { 4, 11, 0, 4, 5, 11, 0, 11, 3, 6, 7, 11, 3, 11, 7}, // 174 7
    { 7, 11, 6, 7, 8, 11, 5, 11, 4, 4, 11, 8}, // 175 3
    { 6, 5, 9, 6, 9, 10, 10, 9, 8}, // 176 5
    { 3, 10, 6, 0, 3, 6, 0, 6, 5, 0, 5, 9}, // 177 9
    { 0, 8, 10, 0, 10, 5, 0, 5, 1, 5, 10, 6}, // 178 14
    { 6, 3, 10, 6, 5, 3, 5, 1, 3}, // 179 5
    { 1, 11, 2, 9, 10, 5, 9, 8, 10, 10, 6, 5}, // 180 12
    { 0, 3, 10, 0, 10, 6, 0, 6, 9, 5, 9, 6, 1, 11, 2}, // 181 6
    {10, 5, 8, 10, 6, 5, 8, 5, 0, 11, 2, 5, 0, 5, 2}, // 182 7
    { 6, 3, 10, 6, 5, 3, 2, 3, 11, 11, 3, 5}, // 183 3
    { 5, 9, 8, 5, 8, 2, 5, 2, 6, 3, 2, 8}, // 184 11
    { 9, 6, 5, 9, 0, 6, 0, 2, 6}, // 185 5
    { 1, 8, 5, 1, 0, 8, 5, 8, 6, 3, 2, 8, 6, 8, 2}, // 186 7
    { 1, 6, 5, 2, 6, 1}, // 187 2
    { 1, 6, 3, 1, 11, 6, 3, 6, 8, 5, 9, 6, 8, 6, 9}, // 188 7
    {11, 0, 1, 11, 6, 0, 9, 0, 5, 5, 0, 6}, // 189 3
    { 0, 8, 3, 5, 11, 6}, // 190 4
    {11, 6, 5}, // 191 1
    {10, 11, 5, 7, 10, 5}, // 192 2
    {10, 11, 5, 10, 5, 7, 8, 0, 3}, // 193 7
    { 5, 7, 10, 5, 10, 11, 1, 0, 9}, // 194 7
    {11, 5, 7, 11, 7, 10, 9, 1, 8, 8, 1, 3}, // 195 10
    {10, 2, 1, 10, 1, 7, 7, 1, 5}, // 196 5
    { 0, 3, 8, 1, 7, 2, 1, 5, 7, 7, 10, 2}, // 197 12
    { 9, 5, 7, 9, 7, 2, 9, 2, 0, 2, 7, 10}, // 198 14
    { 7, 2, 5, 7, 10, 2, 5, 2, 9, 3, 8, 2, 9, 2, 8}, // 199 7
    { 2, 11, 5, 2, 5, 3, 3, 5, 7}, // 200 5
    { 8, 0, 2, 8, 2, 5, 8, 5, 7, 11, 5, 2}, // 201 11
    { 9, 1, 0, 5, 3, 11, 5, 7, 3, 3, 2, 11}, // 202 12
    { 9, 2, 8, 9, 1, 2, 8, 2, 7, 11, 5, 2, 7, 2, 5}, // 203 7
    { 1, 5, 3, 3, 5, 7}, // 204 8
    { 0, 7, 8, 0, 1, 7, 1, 5, 7}, // 205 5
    { 9, 3, 0, 9, 5, 3, 5, 7, 3}, // 206 5
    { 9, 7, 8, 5, 7, 9}, // 207 2
    { 5, 4, 8, 5, 8, 11, 11, 8, 10}, // 208 5
    { 5, 4, 0, 5, 0, 10, 5, 10, 11, 10, 0, 3}, // 209 14
    { 0, 9, 1, 8, 11, 4, 8, 10, 11, 11, 5, 4}, // 210 12
    {11, 4, 10, 11, 5, 4, 10, 4, 3, 9, 1, 4, 3, 4, 1}, // 211 7
    { 2, 1, 5, 2, 5, 8, 2, 8, 10, 4, 8, 5}, // 212 11
    { 0, 10, 4, 0, 3, 10, 4, 10, 5, 2, 1, 10, 5, 10, 1}, // 213 7
    { 0, 5, 2, 0, 9, 5, 2, 5, 10, 4, 8, 5, 10, 5, 8}, // 214 7
    { 9, 5, 4, 2, 3, 10}, // 215 4
    { 2, 11, 5, 3, 2, 5, 3, 5, 4, 3, 4, 8}, // 216 9
    { 5, 2, 11, 5, 4, 2, 4, 0, 2}, // 217 5
    { 3, 2, 11, 3, 11, 5, 3, 5, 8, 4, 8, 5, 0, 9, 1}, // 218 6
    { 5, 2, 11, 5, 4, 2, 1, 2, 9, 9, 2, 4}, // 219 3
    { 8, 5, 4, 8, 3, 5, 3, 1, 5}, // 220 5
    { 0, 5, 4, 1, 5, 0}, // 221 2
    { 8, 5, 4, 8, 3, 5, 9, 5, 0, 0, 5, 3}, // 222 3
    { 9, 5, 4}, // 223 1
    { 4, 7, 10, 4, 10, 9, 9, 10, 11}, // 224 5
    { 0, 3, 8, 4, 7, 9, 9, 7, 10, 9, 10, 11}, // 225 12
    { 1, 10, 11, 1, 4, 10, 1, 0, 4, 7, 10, 4}, // 226 11
    { 3, 4, 1, 3, 8, 4, 1, 4, 11, 7, 10, 4, 11, 4, 10}, // 227 7
    { 4, 7, 10, 9, 4, 10, 9, 10, 2, 9, 2, 1}, // 228 9
    { 9, 4, 7, 9, 7, 10, 9, 10, 1, 2, 1, 10, 0, 3, 8}, // 229 6
    {10, 4, 7, 10, 2, 4, 2, 0, 4}, // 230 5
    {10, 4, 7, 10, 2, 4, 8, 4, 3, 3, 4, 2}, // 231 3
    { 2, 11, 9, 2, 9, 7, 2, 7, 3, 7, 9, 4}, // 232 14
    { 9, 7, 11, 9, 4, 7, 11, 7, 2, 8, 0, 7, 2, 7, 0}, // 233 7
    { 3, 11, 7, 3, 2, 11, 7, 11, 4, 1, 0, 11, 4, 11, 0}, // 234 7
    { 1, 2, 11, 8, 4, 7}, // 235 4
    { 4, 1, 9, 4, 7, 1, 7, 3, 1}, // 236 5
    { 4, 1, 9, 4, 7, 1, 0, 1, 8, 8, 1, 7}, // 237 3
    { 4, 3, 0, 7, 3, 4}, // 238 2
    { 4, 7, 8}, // 239 1
    { 9, 8, 11, 11, 8, 10}, // 240 8
    { 3, 9, 0, 3, 10, 9, 10, 11, 9}, // 241 5
    { 0, 11, 1, 0, 8, 11, 8, 10, 11}, // 242 5
    { 3, 11, 1, 10, 11, 3}, // 243 2
    { 1, 10, 2, 1, 9, 10, 9, 8, 10}, // 244 5
    { 3, 9, 0, 3, 10, 9, 1, 9, 2, 2, 9, 10}, // 245 3
    { 0, 10, 2, 8, 10, 0}, // 246 2
    { 3, 10, 2}, // 247 1
    { 2, 8, 3, 2, 11, 8, 11, 9, 8}, // 248 5
    { 9, 2, 11, 0, 2, 9}, // 249 2
    { 2, 8, 3, 2, 11, 8, 0, 8, 1, 1, 8, 11}, // 250 3
    { 1, 2, 11}, // 251 1
    { 1, 8, 3, 9, 8, 1}, // 252 2
    { 0, 1, 9}, // 253 1
    { 0, 8, 3}, // 254 1
    {} // 255 0
  };
}
