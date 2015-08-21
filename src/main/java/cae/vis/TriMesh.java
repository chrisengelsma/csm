/****************************************************************************
Copyright (c) 2010, Colorado School of Mines and others. All rights reserved.
This program and accompanying materials are made available under the terms of
the Common Public License - v1.0, which accompanies this distribution, and is
available at http://www.eclipse.org/legal/cpl-v10.html
****************************************************************************/
package cae.vis;

import cae.io.TFile;
import cae.util.*;

import static edu.mines.jtk.ogl.Gl.*;

/**
 * A triangle mesh.
 * @author Chris Engelsma, Colorado School of Mines
 * @version 2010.03.05
 */
public class TriMesh implements Model {

  public TriMesh(String fileName) {
    this(new TFile(fileName));
  }
  
  /**
   * Constructs a new triangle mesh with a given t file.
   * @param tfile a file t file.
   */
  public TriMesh(TFile tfile) {
    _nv = tfile.getVertexCount();
    _nt = tfile.getTriangleCount();
    computeNormals(tfile.getTriangles(),tfile.getVertices());
  }
  
  /**
   * Constructs a triangle mesh with given triangle and vertex tables.
   * @param t a triangle table.
   * @param v a vertex table.
   */
  public TriMesh(int[] t, float[] v) {
    computeNormals(t,v);
  }

  /**
   * Gets the packed vertices.
   * @return the packed vertices.
   */
  public float[] getVertices() {
    return _v;
  }

  /**
   * Gets the vertex normals.
   * @return the vertex normals.
   */
  public float[] getNormals() {
    return _vn;
  }

  /**
   * Gets the triangle table.
   * @return the triangle table.
   */
  public int[] getTriangles() {
    return _t;
  }

  /**
   * Draws the model.
   */
  public void drawModel() {
    setMaterialProperties(0.5f,0.5f,0.5f,1.0f);
    glBegin(GL_TRIANGLES);
      for (int i=0; i<_vp.length-2; i+=3) {
        glNormal3f(_vn[i+0],_vn[i+1],_vn[i+2]);
        glVertex3f(_vp[i+0],_vp[i+1],_vp[i+2]);
      }
    glEnd();
  }

  /**
   * Performs n-iterations of loop subdivision.
   * In order for this to be possible, the mesh needs to be manifold.
   * @param n number of iterations.
   */
  public void subdivideMesh(int n) {
    for (int i=0; i<n; ++i) {
      int[] d = computeVertexDegrees();
      IntList[] inc = computeIncidentTriangles();
      int[] adj = computeAdjacencyTable(inc);
      int[] edg = computeEdgeTable(adj);
      loopSub(d,adj,edg,inc);
    }
  }

  public void setMaterialProperties(float r, float g, float b, float a) {
    float[] mAmbientDiffuse = new float[]{0.5f,0.5f,0.5f,1.0f};
    float[] mSpecular = new float[]{0.0f,0.0f,0.0f,1.0f};
    float[] mShininess = new float[]{0.0f};

    mSpecular[0] = mAmbientDiffuse[0] = r;
    mSpecular[1] = mAmbientDiffuse[1] = g;
    mSpecular[2] = mAmbientDiffuse[2] = b;
    mSpecular[3] = mAmbientDiffuse[3] = a;

    glMaterialfv(GL_FRONT,GL_SPECULAR,mSpecular,0);
    glMaterialfv(GL_FRONT,GL_SHININESS,mShininess,0);
    glMaterialfv(GL_FRONT,GL_AMBIENT_AND_DIFFUSE,mAmbientDiffuse,0);
  }

  ///////////////////////////////////////////////////////////////////////////
  // private

  private float[] _vp,_vn,_v;
  private int[] _t;

  private int _nt = 0, _nv = 0;
  
  private void computeNormals(int[] t, float[] v) {
    _t = t;
    _v = v;
    float[] vn = new float[3*_nv];
    float[] tn = new float[3*_nt];
    Vector3 v0 = new Vector3();
    Vector3 v1 = new Vector3();
    Vector3 v2 = new Vector3();
    Vector3 v0v1 = new Vector3();
    Vector3 v0v2 = new Vector3();
    Vector3 xv;
    for (int i=0; i<3*_nt-2; i+=3) {
      v0.x = v[3*t[i+0]+0]; v0.y = v[3*t[i+0]+1]; v0.z = v[3*t[i+0]+2];
      v1.x = v[3*t[i+1]+0]; v1.y = v[3*t[i+1]+1]; v1.z = v[3*t[i+1]+2];
      v2.x = v[3*t[i+2]+0]; v2.y = v[3*t[i+2]+1]; v2.z = v[3*t[i+2]+2];

      v0v1.x = v1.x-v0.x; v0v1.y = v1.y-v0.y; v0v1.z = v1.z-v0.z;
      v0v2.x = v2.x-v0.x; v0v2.y = v2.y-v0.y; v0v2.z = v2.z-v0.z;

      xv = Vector3.cross(v0v2,v0v1);

      vn[3*t[i+0]+0] += xv.x; vn[3*t[i+0]+1] += xv.y; vn[3*t[i+0]+2] += xv.z;
      vn[3*t[i+1]+0] += xv.x; vn[3*t[i+1]+1] += xv.y; vn[3*t[i+1]+2] += xv.z;
      vn[3*t[i+2]+0] += xv.x; vn[3*t[i+2]+1] += xv.y; vn[3*t[i+2]+2] += xv.z;

      tn[i+0] = xv.x; tn[i+1] = xv.y; tn[i+2] = xv.z;
    }
    packArray(v,vn);
  }

  private void packArray(float[] v, float[] vn) {
    int X = 0, Y = 1, Z = 2;

    FloatList vf = new FloatList();
    FloatList nf = new FloatList();
    for (int it=0; it<_t.length; it+=3){
      nf.add(vn[3*_t[it+X]+0]);
      nf.add(vn[3*_t[it+X]+1]);
      nf.add(vn[3*_t[it+X]+2]);

      nf.add(vn[3*_t[it+Y]+0]);
      nf.add(vn[3*_t[it+Y]+1]);
      nf.add(vn[3*_t[it+Y]+2]);

      nf.add(vn[3*_t[it+Z]+0]);
      nf.add(vn[3*_t[it+Z]+1]);
      nf.add(vn[3*_t[it+Z]+2]);

      vf.add(v[3*_t[it+X]+0]);
      vf.add(v[3*_t[it+X]+1]);
      vf.add(v[3*_t[it+X]+2]);

      vf.add(v[3*_t[it+Y]+0]);
      vf.add(v[3*_t[it+Y]+1]);
      vf.add(v[3*_t[it+Y]+2]);

      vf.add(v[3*_t[it+Z]+0]);
      vf.add(v[3*_t[it+Z]+1]);
      vf.add(v[3*_t[it+Z]+2]);
    }
    _vn = nf.trim();
    _vp = vf.trim();
  }

  private int[] computeVertexDegrees() {
    int[] degree = new int[_nv];
    for (int i=0; i<3*_nt; ++i) degree[_t[i]]++;
    return degree;
  }

  private IntList[] computeIncidentTriangles() {
    IntList[] inc = new IntList[_nv];
    for (int i=0; i<_nv; ++i) inc[i] = new IntList();
    for (int i=0; i<3*_nt; i+=3) {
      inc[_t[i+0]].add(i/3);
      inc[_t[i+1]].add(i/3);
      inc[_t[i+2]].add(i/3);
    }
    return inc;
  }

  /**
   * Assembles a table of adjacent triangles.
   * Two triangles are adjacent if they share an edge. If we assume that the
   * mesh is manifold, then each triangle T will have 3 adjacent triangles,
   * t0, t1, and t2, which respectively share edges e0, e1, and e2 with T:
   * <code>
   *         0
   *        /\
   *   e0 /   \ e1
   *    /   T  \
   *  1---------2
   *       e2
   * </code>
   * where, e0 = [0,1], e1 = [0,2], e2 = [1,2].
   * <p>
   * The adjacency table will therefore be a nt x 3 array , where nt is the
   * number of triangles in the mesh. Each row i in the adjacency table will
   * contain three integers, corresponding to the three triangles adjacent
   * to Ti.
   * Note that this technique is rather slow for larger meshes and therefore
   * not recommended for them.
   * @param inc the incident triangle list.
   * @return the adjacency table.
   */
  private int[] computeAdjacencyTable(IntList[] inc) {
    int[] iladj = new int[3*_nt];
    for (int i=0; i<iladj.length; ++i) iladj[i] = -1;
    int[] iladjc = new int[_nt];
    int v0,v1,v2,i0,i1,i03,i13;
    int ia0,ia1,ia2;
    int ib0,ib1,ib2;

    // tuples containing the two vertices bounding the edges
    int[] elist;
    int[] counter;

    for (int i=0; i<inc.length; ++i) { // over all vertices in the inc list
      int[] ilinc = inc[i].trim();
      elist = new int[2*_nv];
      counter = new int[_nv];
      for (int j=0; j<ilinc.length; ++j) { // over all incident triangles
        v0 = _t[3*ilinc[j]+0];
        v1 = _t[3*ilinc[j]+1];
        v2 = _t[3*ilinc[j]+2];
        
        for (int v:new int[]{v0,v1,v2}) {
          if (v!=i) elist[2*v+(counter[v]++)] = ilinc[j];
          if (counter[v]==2) {
            i0 = elist[2*v+0];
            i1 = elist[2*v+1];
            i03 = 3*i0;
            i13 = 3*i1;

            ia0 = iladj[i03+0];
            ia1 = iladj[i03+1];
            ia2 = iladj[i03+2];

            ib0 = iladj[i13+0];
            ib1 = iladj[i13+1];
            ib2 = iladj[i13+2];

            if (iladjc[i0]!=3 && !equiv(i1,ia0,ia1,ia2)) {
              iladj[i03+(iladjc[i0])] = i1;
              iladjc[i0]++;
            }
            if (iladjc[i1]!=3 && !equiv(i0,ib0,ib1,ib2)) {
              iladj[i13+(iladjc[i1])] = i0;
              iladjc[i1]++;
            }
          }
        }
      }
    }

    /*
    for (int i=0; i<iladj.length-2; i+=3) {
      System.out.println((i/3)+": "+iladj[i+0]+" "+iladj[i+1]+" "+iladj[i+2]);
    }
    */

    // use for benchmarking later
    /*
    for (int i=0; i<inc.length; ++i) {
      int[] ilinc = inc[i].trim();
      for (int ti:ilinc) {
        // First check we don't repeat what we've done
        if (iladj[3*ti+0] == 0 &&
            iladj[3*ti+1] == 0 &&
            iladj[3*ti+2] == 0)
        {
          // Get the vertex pairs bounding all three edges of the triangle
          e0[0] = _t[3*ti+0]; e0[1] = _t[3*ti+1];
          e1[0] = _t[3*ti+0]; e1[1] = _t[3*ti+2];
          e2[0] = _t[3*ti+1]; e2[1] = _t[3*ti+2];

          // Find triangles t0,t1,t2 that also have edges e0,e1,e2
          int t0 = sharesEdge(e0,ti,inc,i);
          int t1 = sharesEdge(e1,ti,inc,i);
          int t2 = sharesEdge(e2,ti,inc,i);

          // Since t0,t1,t2 share those edges, they are adjacent to T, so
          //  add them to the adjacency table.
          iladj[3*ti+0] = t0;
          iladj[3*ti+1] = t1;
          iladj[3*ti+2] = t2;
        }
      }
    }
    */

    /*
    System.out.println("Triangle table");
    for (int i=0; i<_t.length-2; i+=3)
      System.out.println(_t[i+0]+" "+_t[i+1]+" "+_t[i+2]);
    System.out.println();

    System.out.println("Vertex table");
    for (int i=0; i<_v.length-2; i+=3)
      System.out.println(_v[i+0]+" "+_v[i+1]+" "+_v[i+2]);
    System.out.println();

    System.out.println("Adjacency Table");
    for (int i=0; i<iladj.length-2; i+=3)
      System.out.println(
        "T"+(i/3)+": "+iladj[i+0]+" "+iladj[i+1]+" "+iladj[i+2]);
    System.out.println();
    */
    return iladj;
  }


  /**
   * Constructs an edge table which holds identifiers for the edges opposite
   * to vertices of a triangle.
   * @param adj the adjacency table.
   * @return a table full of edge identifiers.
   */
  private int[] computeEdgeTable(int[] adj) {
    int v; // current vertex
    int tn = 0; // current triangle
    int n;
    int v0,v1; // other two vertices of the triangle
    int[] edge = new int[3*_nt];
    int en = 0; // edge number
    for (int i=0; i<edge.length; ++i) edge[i] = -1;
    
    for (int t=0 ;t<3*_nt; t++) {
      v = _t[t];
      n = edge[t];
      edge[t] = (n==-1)?en:n;

      // Now find adjacent triangle that shares this edge
      // The other two vertices of this triangle are...
      if ((t+0)%3==0) {
        v0 = _t[t+1];
        v1 = _t[t+2];
      } else if ((t+1)%3==0) {
        v0 = _t[t-2];
        v1 = _t[t-1];
      } else {
        v0 = _t[t-1];
        v1 = _t[t+1];
      }

      // look up this triangle in the adjacency table, and search for adjacent
      //  triangles that share v0 & v1
      int t0 = adj[3*tn+0];
      int t1 = adj[3*tn+1];
      int t2 = adj[3*tn+2];

      int i0,vt=0,tt=0;
      int w0,w1,w2;
      for (int it:new int[]{t0,t1,t2}) {
        i0 = 0;
        w0 = _t[3*it+0];
        w1 = _t[3*it+1];
        w2 = _t[3*it+2];
        if (w0==v0 || w0==v1) i0 += 1;
        if (w1==v0 || w1==v1) i0 += 2;
        if (w2==v0 || w2==v1) i0 += 4;

        if (i0==3) {
          vt = 2;
          tt = it;
          break;
        } else if (i0==6) {
          vt = 0;
          tt = it;
          break;
        } else if (i0==5) {
          vt = 1;
          tt = it;
          break;
        }
      }

      n = edge[3*tt+vt];
      edge[3*tt+vt] = (n==-1)?en++:n;
      if ((t+1)%3==0) tn++;
    }

    /*
    System.out.println("Edge table");
    for (int i=0; i<edge.length-2; i+=3) {
      System.out.println(
        "T"+(i/3)+": "+edge[i+0]+" "+edge[i+1]+" "+edge[i+2]);
    }
    System.out.println();
    */

    return edge;
  }

  /**
   * Performs loop subdivision.
   * @param adj
   * @param edge
   */
  private void loopSub(
    int[] deg, int[] adj, int[] edge, IntList[] inc)
  {
    int nt = 4*_nt;
    int nv = _nv+3*_nt/2;
    int[] t = new int[3*nt];
    for (int i=0,k=0; i<_nt; i++) {
      // The indices of the "big" triangle
      int v0 = _t[3*i+0];
      int v1 = _t[3*i+1];
      int v2 = _t[3*i+2];

      // The indices of the edge vertices
      int v00 = edge[3*i+0]+_nv;
      int v11 = edge[3*i+1]+_nv;
      int v22 = edge[3*i+2]+_nv;

      // Organized in the same order as the big triangle
      t[k++] = v0;  t[k++] = v22; t[k++] = v11;
      t[k++] = v1;  t[k++] = v00; t[k++] = v22;
      t[k++] = v2;  t[k++] = v11; t[k++] = v00;
      t[k++] = v00; t[k++] = v11; t[k++] = v22;
    }

    /*
     * Weights are applied in a two-step process
     * First, we loop over the old mesh vertices and apply a weight that's
     * related to each vertex's degree. Given that each vertex v has a degree
     * k, then we have v*5/8 + 3/(8k)*(each vertex incident on v)
     * Second, we loop over the new edge vertices, and assign the weight to
     * be 3/8*(incident vertices) + 1/8*(adjacent vertices)
     */
    float[] v = new float[3*nv];

    for (int i=0; i<3*nt; ++i) {
      float vx,vy,vz;
      int vi = t[i];
      if (vi<_nv) {
        int d = deg[vi];
        float wx=0,wy=0,wz=0;
        float w0 = 5.0f/8.0f;
        float w1 = 3.0f/(8.0f*(float)d);
        
        if (d==3) {
          w0 = 7.0f/16.0f;
          w1 = 3.0f/16.0f;
        }
        
        vx = _v[3*vi+0];
        vy = _v[3*vi+1];
        vz = _v[3*vi+2];

        // Now for all triangles incident on this triangle, take the two
        // vertices other than this vertex,
        int[] ninc = inc[vi].trim();
        IntList il = new IntList();
        for (int k=0; k<ninc.length; ++k)
        for (int in:ninc) { // For every triangle incident on vi
          for (int n=0; n<3; ++n) {
            int tt = _t[3*in+n];
            if (tt!=vi && !il.contains(tt)) {
              wx += w1*_v[3*tt+0];
              wy += w1*_v[3*tt+1];
              wz += w1*_v[3*tt+2];
              il.add(tt);
            }
          }
        }

        float v0x = w0*vx;
        float v0y = w0*vy;
        float v0z = w0*vz;
        
        vx = v0x+wx;
        vy = v0y+wy;
        vz = v0z+wz;

        v[3*vi+0] = vx;
        v[3*vi+1] = vy;
        v[3*vi+2] = vz;
      } else {                // Routine for edge vertices

        /*
         *       v0   
         *       /\
         *     / T \
         * v1/___E__\v2
         *   \      /
         *    \   /
         *     \/
         *     v3
         *
         */
        
        float w0 = 1.0f/8.0f;
        float w1 = 3.0f/8.0f;

        // Find index of vertex
        int ein = vi-_nv;
        int t0=-1,t1=-1,tt=-1;
        int i0=-1,i1=-1,ii=-1;
        for (int j=0; j<edge.length-2; j+=3) {
          if (edge[j+0]==ein) {
            tt = (j/3);
            ii = 0;
          }
          if (edge[j+1]==ein) {
            tt = (j/3);
            ii = 1;
          }
          if (edge[j+2]==ein) {
            tt = (j/3);
            ii = 2;
          }
          if (tt!=-1 && t0==-1) {
            t0 = tt;
            i0 = ii;
            tt = -1;
          } else if (tt!=-1 && t0!=-1) {
            t1 = tt;
            i1 = ii;
            break;
          }
        }

        int v0 = _t[3*t0+i0];
        int v1 = _t[3*t0+((i0+1)%3)];
        int v2 = _t[3*t0+((i0+2)%3)];
        int v3 = _t[3*t1+i1];
        
        float v0x = _v[3*v0+0];
        float v0y = _v[3*v0+1];
        float v0z = _v[3*v0+2];

        float v1x = _v[3*v1+0];
        float v1y = _v[3*v1+1];
        float v1z = _v[3*v1+2];

        float v2x = _v[3*v2+0];
        float v2y = _v[3*v2+1];
        float v2z = _v[3*v2+2];

        float v3x = _v[3*v3+0];
        float v3y = _v[3*v3+1];
        float v3z = _v[3*v3+2];

        vx = w0*(v0x+v3x)+w1*(v1x+v2x);
        vy = w0*(v0y+v3y)+w1*(v1y+v2y);
        vz = w0*(v0z+v3z)+w1*(v1z+v2z);
        
        v[3*vi+0] = vx;
        v[3*vi+1] = vy;
        v[3*vi+2] = vz;
      }
    }

    /*
    for (int i=0; i<3*nt-2; i+=3)
      System.out.println((i/3)+": "+t[i+0]+" "+t[i+1]+" "+t[i+2]);

    for (int i=0; i<3*nv-2; i+=3)
      System.out.println((i/3)+":: "+v[i+0]+" "+v[i+1]+" "+v[i+2]);
      */

    // Finalize
    _nv = nv;
    _nt = nt;
    computeNormals(t,v);
  }

  private static boolean equiv(int a, int b, int c, int d) {
    return (a==b || a==c || a==d);
  }

}
