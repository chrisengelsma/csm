/****************************************************************************
Copyright (c) 2009, Colorado School of Mines and others. All rights reserved.
This program and accompanying materials are made available under the terms of
the Common Public License - v1.0, which accompanies this distribution, and is
available at http://www.eclipse.org/legal/cpl-v10.html
****************************************************************************/
package cae.vis;

import edu.mines.jtk.dsp.*;
import static edu.mines.jtk.util.ArrayMath.*;

import java.util.ArrayList;

/**
 * Assembles hyperstreamlines.
 * Hyperstreamlines are a 3-dimensional extension streamlines.  Given a 3D
 * second-order tensor, the direction of the streamlines are determined by
 * the major eigenvector, and the elliptical radii of the streamline are
 * determined by the two remaining eigenvectors.
 * @author Chris Engelsma, Colorado School of Mines.
 * @version 2009.12.07
 */
public class Hyperstreamline {

  /**
   * Constructs a new hyperstreamline.
   * @param et a tensor field.
   * @param sx sampling in the third axis (X).
   * @param sy sampling in the second axis (Y).
   * @param sz sampling in the first axis (Z).
   */
  public Hyperstreamline(
    EigenTensors3 et, Sampling sx, Sampling sy, Sampling sz)
  {
    _et = et;
    _sx = sx; 
    _sy = sy;
    _sz = sz;
    _nx = sx.getCount();  
    _ny = sy.getCount();
    _nz = sz.getCount();
    buildCircle();
  }

  /**
   * Begins a hyperstreamline at a given point, with a given interval.
   * @param x x-coordinate of the seed point.
   * @param y y-coordinate of the seed point.
   * @param z z-coordinate of the seed point.
   * @param step the stepping interval.
   */
  public void seed(float x, float y, float z, float step) {
    int xi = _sx.indexOfNearest(x);
    int yi = _sy.indexOfNearest(y);
    int zi = _sz.indexOfNearest(z);
    _fnl = new NodeList();
    _rnl = new NodeList();
    _fel = new EigenvectorList();
    _rel = new EigenvectorList();
    _fnl.add(x,y,z);
    _rnl.add(x,y,z);
    followLargestEigenvector(xi,yi,zi,step,true,maxRecursion);
    followLargestEigenvector(xi,yi,zi,step,false,maxRecursion);
    mergeStreamline();
    buildStreamline();
  }

  /**
   * Sets the maximum width of a hyperstreamline.
   * @param w a width.
   */
  public void setSize(float w) {
    if (w>0) _hr = w;
    buildCircle();
  }

  public void setMaxRecursion(int max) {
    maxRecursion = max;
  }

  /**
   * Gets the nodes of each hyperstreamline.
   * @return the hyperstreamline nodes.
   */
  public float[] getNodes() {
    return _nl.trim();
  }

  /**
   * Gets the triangles for the hyperstreamlines.
   * @return the triangulated hyperstreamline.
   */
  public float[] getTube() {
    return _tube;
  }

  /**
   * Gets the colors for each vertex within the hyperstreamline.
   * The colors are scaled within the range [0,1].
   * @return the hyperstreamline vertex colors.
   */
  public float[] getColors() {
    return _colors;
  }

  ///////////////////////////////////////////////////////////////////////////
  // private

  /**
   * List containing vertex locations.
   */
  private class NodeList {
    public float[] nodes;
    public int n;
    public NodeList() {
      nodes = new float[3*64];
      n = 0;
    }
    public void add(float x, float y, float z) {
      if (n==nodes.length) {
        float[] a = new float[2*3*n];
        for (int i=0; i<n; ++i)
          a[i] = nodes[i];
        nodes = a;
      }
      nodes[n+0] = x;
      nodes[n+1] = y;
      nodes[n+2] = z;
      n+=3;
    }
    public float[] trim() {
      float[] t = new float[n];
      for (int i=0; i<n; ++i)
        t[i] = nodes[i];
      return t;
    }
  }

  /**
   * 3D Eigenvector.
   * Also contains eigenvalues.
   */
  private class Eigenvector3 {
    public float[] u,v,w;
    public float ev,ew;
    public float mag;
    public Eigenvector3(
      float[] dir, float[] d1, float[] d2, float ev, float ew, float mag)
    {
      u = dir;
      v = d1;
      w = d2;
      this.ev = ev;
      this.ew = ew;
      this.mag = mag;
    }
  }

  /**
   * List of eigenvectors.
   */
  private class EigenvectorList {
    public Eigenvector3[] e;
    public int n;
    public EigenvectorList() {
      e = new Eigenvector3[3*64];
      n = 0;
    }
    public void add(
      float[] dir, float[] d1, float[] d2, float rad1, float rad2, float mag)
    {
      if (n==e.length) {
        Eigenvector3[] a = new Eigenvector3[2*3*n];
        for (int i=0; i<n; ++i)
          a[i] = e[i];
        e = a;
      }
      e[n] = new Eigenvector3(dir,d1,d2,rad1,rad2,mag);
      n++;
    }
    public Eigenvector3[] trim() {
      Eigenvector3[] t = new Eigenvector3[n];
      for (int i=0; i<n; ++i)
        t[i] = e[i];
      return t;
    }
  }

  private EigenTensors3 _et;              // eigentensor.
  private Sampling _sx,_sy,_sz;           // sampling for x,y,z.
  private int _nx,_ny,_nz;                // number of samples.
  private NodeList _fnl,_rnl,_nl;         // node lists.
  private EigenvectorList _fel,_rel,_el;  // eigenvector lists.
  private int maxRecursion = 2000;        // maximum recursion allowed.
  private float[] _c;                     // circle vertices.
  private float[] _tube,_colors;          // vertices of hyperstreamline.
  private float _hr = 2.0f;

  /**
   * Follows the largets eigenvector recursively using Euler's method.
   * @param x the starting x-coordinate.
   * @param y the starting y-coordinate.
   * @param z the starting z-coordinate.
   * @param step the streamline step.
   * @param forward the direction.
   * @param recursion the recursion counter.
   */
  private void followLargestEigenvector(
    int x, int y, int z, double step, boolean forward, int recursion)
  {
    float xn,yn,zn;
    float rad1 = 0,rad2 = 0,mag = 0;
    float[] dir,d1,d2,e;
    int i,xi,yi,zi;

    if (recursion==0) return;

    if (!isInData(z,y,x))
      return;

    e = getEigenvalues(z,y,x);
    i = indexOfLargest(e);
    dir = new float[3];
    d1 = new float[3];
    d2 = new float[3];

    if (i==0) {
      dir = _et.getEigenvectorU(z,y,x);
      d1 = _et.getEigenvectorV(z,y,x);
      d2 = _et.getEigenvectorW(z,y,x);
      mag = e[0]; rad1 = e[1]; rad2 = e[2];
    } else if (i==1) {
      dir = _et.getEigenvectorV(z,y,x);
      d1 = _et.getEigenvectorU(z,y,x);
      d2 = _et.getEigenvectorW(z,y,x);
      rad2 = e[0]; mag = e[1]; rad1 = e[2];
    } else if (i==2) {
      dir = _et.getEigenvectorW(z,y,x);
      d1 = _et.getEigenvectorU(z,y,x);
      d2 = _et.getEigenvectorV(z,y,x);
      rad1 = e[0]; rad2 = e[1]; mag = e[2];
    }
    correctDir(dir); // Ensure the streamlines aren't backtracking.
    if (conditionsMet(dir)) { // Ensure streamlines are in bounds.
      if (forward) {
          xn = (float)(x+step*dir[2]);
          yn = (float)(y+step*dir[1]);
          zn = (float)(z+step*dir[0]);
      } else {
          xn = (float)(x-step*dir[2]);
          yn = (float)(y-step*dir[1]);
          zn = (float)(z-step*dir[0]);
      }
      xi = _sx.indexOfNearest(xn);
      yi = _sy.indexOfNearest(yn);
      zi = _sz.indexOfNearest(zn);

      if (isInData(xi,yi,zi)) {
        if (forward) {
          _fnl.add(xn,yn,zn);               // Append node list.
          _fel.add(dir,d1,d2,rad1,rad2,mag);// Append eigenvector list.
          followLargestEigenvector(xi,yi,zi,step,true,--recursion);
        } else {
          _rnl.add(xn,yn,zn);               // Append node list.
          _rel.add(dir,d1,d2,rad1,rad2,mag);// Append eigenvector list.
          followLargestEigenvector(xi,yi,zi,step,false,--recursion);
        }
      }
    }
  }

  /**
   * Corrects the direction of the primary eigenvector.
   * Keeps the direction consistent by ensuring the the first value is
   * always positive.
   * @param dir the major eigenvector.
   */
  private void correctDir(float[] dir) {
    if (dir[0]<0) {
      dir[0]*=-1; dir[1]*=-1; dir[2]*=-1;
    }
  }


  /**
   * Gets the scaled eigenvalues.
   * Scaling the eigenvalues entails perturbing by a small value and taking
   * the inverse.
   * @param x the index in the third dimension.
   * @param y the index in the second dimension.
   * @param z the index in the first dimension.
   * @return the scaled eigenvalues.
   */
  private float[] getEigenvalues(int x, int y, int z) {
    float[] e = _et.getEigenvalues(x,y,z);
    e[0] = 1.0f/(e[0]+0.01f);
    e[1] = 1.0f/(e[1]+0.01f);
    e[2] = 1.0f/(e[2]+0.01f);
    return e;
  }

  /**
   * Finds the index of the largest eigenvalue.
   * @param e an array of eigenvalues.
   * @return the index of the largest eigenvalue.
   */
  private int indexOfLargest(float[] e) {
    int i = 0;
    if (e[0]>e[1] && e[0]>e[2]) i = 0;
    if (e[1]>e[0] && e[1]>e[2]) i = 1;
    if (e[2]>e[1] && e[2]>e[0]) i = 2;
    return i;
  }

  /**
   * Determines whether a given point lies within the data.
   * @param x the x-index.
   * @param y the y-index.
   * @param z the z-index.
   * @return true, if within the data; false, otherwise.
   */
  private boolean isInData(int x, int y, int z) {
    return (x>0 && x<_nx-1 &&
            y>0 && y<_ny-1 &&
            z>0 && z<_nz-1);
  }

  /**
   * Determines whether the default eigenvector wasn't thrown.
   * The default eigenvector is considered to be (1,0,0), we want to stop
   * when this happens because this means we've reached no data.
   * @param dir an eigenvector.
   * @return false, if not default; true, otherise.
   */
  private boolean conditionsMet(float[] dir) {
//    if (dir[0]==1 && dir[1]==0 && dir[2]==0)
//      return false;
    return true;
  }

  /**
   * Merges the streamlines into one seamless list.
   */
  private void mergeStreamline() {
    float[] fnodes = _fnl.trim();
    float[] rnodes = _rnl.trim();
    int fn = fnodes.length;
    int rn = rnodes.length;
    _nl = new NodeList();
    for (int i=rn-6; i>=0; i-=3)
      _nl.add(rnodes[i+0],rnodes[i+1],rnodes[i+2]);
    for (int i=0; i<fn; i+=3) 
      _nl.add(fnodes[i+0],fnodes[i+1],fnodes[i+2]);
    Eigenvector3[] fel = _fel.trim();
    Eigenvector3[] rel = _rel.trim();
    fn = fel.length;
    rn = rel.length;
    _el = new EigenvectorList();
    for (int i=rn-1; i>=0; --i)
      _el.add(rel[i].u,rel[i].v,rel[i].w,rel[i].ev,rel[i].ew,rel[i].mag);
    for (int i=0; i<fn; ++i)
      _el.add(fel[i].u,fel[i].v,fel[i].w,fel[i].ev,fel[i].ew,fel[i].mag);
  }

  /**
   * Stores a new circle with a set number of vertices.
   * The circle lies in the XY-plane centered at the origin.
   */
  private void buildCircle() {
    int n = 50;
    _c = new float[3*(n+1)];
    for (int i=0; i<=n; ++i) {
      _c[3*i+0] = _hr*cos((float)i*FLT_PI/(n/2)); // X
      _c[3*i+1] = _hr*sin((float)i*FLT_PI/(n/2)); // Y
      _c[3*i+2] = 0;                            // Z
    }
  }

  /**
   * Transforms the circle into the correct ellipse in 3D space.
   * @param cx the center x-coordinate.
   * @param cy the center y-coordinate.
   * @param cz the center z-coordinate.
   * @param ev the ellipse major radius.
   * @param ew the ellipse minor radius.
   * @param v the ellipse major radius direction.
   * @param w the ellipse minor radius direction.
   * @return vertices of the ellipse.
   */
  private float[] transform(
    float cx, float cy, float cz,
    float ev, float ew,
    float[] v, float[] w)
  {
    float[] el = new float[_c.length];
    for (int i=0; i<_c.length-2; i+=3) {
      el[i+0] = v[2]*_c[i+0] + w[2]*_c[i+1] + cx;
      el[i+1] = (v[1]*_c[i+0] + w[1]*_c[i+1])/sqrt(ev) + cy;
      el[i+2] = (v[0]*_c[i+0] + w[0]*_c[i+1])/sqrt(ew) + cz;
    }
    return el;
  }

  /**
   * Triangulates a hyperstreamline.
   */
  private void buildStreamline() {
    float cx,cy,cz,rad1,rad2;
    float[] u = new float[3];
    float[] v = new float[3];
    float[] w = new float[3];
    Eigenvector3[] e = _el.e;
    int en = _el.n, k = 0;
    ArrayList<float[]> tempstore = new ArrayList<float[]>(en);
    float[] colors = new float[en];

    float[] e1 = new float[_c.length]; // Node 1 ellipse
    float[] e2 = new float[_c.length]; // Node 2 ellipse
    _tube = new float[2*3*(_c.length-3)*(en-1)]; // Final tube array
    _colors = new float[2*3*(_c.length-3)*(en-1)]; // Final color array

    for (int i=0; i<en; ++i) {
      cx = _nl.nodes[3*i+0];
      cy = _nl.nodes[3*i+1];
      cz = _nl.nodes[3*i+2];
      u = e[i].u;
      v = e[i].v;
      w = e[i].w;
      rad1 = e[i].ev;
      rad2 = e[i].ew;
      tempstore.add(transform(cx,cy,cz,rad1,rad2,v,w));
      colors[i] = e[i].mag;
    }
    colors = norm(colors);
    e2 = organizeVertices(tempstore.get(0));
    for (int i=1; i<tempstore.size(); ++i) {

      e1 = e2;                                 // Previous node
      e2 = organizeVertices(tempstore.get(i)); // This node

      for (int j=0; j<e1.length-5; j+=3,k+=18) {
        _tube[k+0] = e1[j+0]; _colors[k+0] = colors[i-1];
        _tube[k+1] = e1[j+1]; _colors[k+1] = 0;
        _tube[k+2] = e1[j+2]; _colors[k+2] = 1-colors[i-1];

        _tube[k+3] = e2[j+0]; _colors[k+3] = colors[i-0];
        _tube[k+4] = e2[j+1]; _colors[k+4] = 0;
        _tube[k+5] = e2[j+2]; _colors[k+5] = 1-colors[i-0];

        _tube[k+6] = e1[j+3]; _colors[k+6] = colors[i-1];
        _tube[k+7] = e1[j+4]; _colors[k+7] = 0;
        _tube[k+8] = e1[j+5]; _colors[k+8] = 1-colors[i-1];

        _tube[k+9 ] = _tube[k+6]; _colors[k+9] = _colors[k+6];
        _tube[k+10] = _tube[k+7]; _colors[k+10] = _colors[k+7];
        _tube[k+11] = _tube[k+8]; _colors[k+11] = _colors[k+8];

        _tube[k+12] = _tube[k+3]; _colors[k+12] = _colors[k+3];
        _tube[k+13] = _tube[k+4]; _colors[k+13] = _colors[k+4];
        _tube[k+14] = _tube[k+5]; _colors[k+14] = _colors[k+5];

        _tube[k+15] = e2[j+3]; _colors[k+15] = colors[i-0];
        _tube[k+16] = e2[j+4]; _colors[k+16] = 0;
        _tube[k+17] = e2[j+5]; _colors[k+17] = 1-colors[i-0];
      }
    }
  }

  /**
   * Organizes the vertices with the first index being closest to the origin.
   * @param v the list of vertices.
   * @return the organized list of vertices
   */
  private static float[] organizeVertices(float[] v) {
    int i1 = 0,i = 0;
    float mag = mag(v[0],v[1],v[2]);
    float tempmag = 0;
    for (i=0; i<v.length-2; i+=3) {
      tempmag = mag(v[i+0],v[i+1],v[i+2]);
      if (mag<tempmag) {
        i1 = i;
        mag = tempmag;
      }
    }
    float[] v1n = new float[v.length];
    for (i=0; i<v.length; i++) {
      if (i<(v.length-1-i1)) {
        v1n[i] = v[i+i1];
      } else {
        v1n[i] = v[i-(v.length-3-i1)];
      }
    }
    return v1n;
  }

  /**
   * Normalizes an array of floats to the range [0,1].
   * @param x an array of floats.
   * @return the normalized array.
   */
  private static float[] norm(float[] x) {
    float max = x[0];
    for (int i=1; i<x.length; ++i)
      if (x[i]>max) x[i] = max;
    for (int i=0; i<x.length; ++i)
      x[i]/=max;
    return x;
  }

  /**
   * Finds the magnitude of three components to a vector.
   * @param x the x-component.
   * @param y the y-component.
   * @param z the z-component.
   * @return the magnitude.
   */
  private static float mag(float x, float y, float z) {
    return sqrt(x*x+y*y+z*z);
  }
}
