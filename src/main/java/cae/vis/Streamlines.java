/****************************************************************************
Copyright (c) 2010, Colorado School of Mines and others. All rights reserved.
This program and accompanying materials are made available under the terms of
the Common Public License - v1.0, which accompanies this distribution, and is
available at http://www.eclipse.org/legal/cpl-v10.html
****************************************************************************/
package cae.vis;

import cae.util.Vector3;

import java.util.ArrayList;
import java.util.Iterator;

import static edu.mines.jtk.ogl.Gl.*;

/**
 * @author Chris Engelsma, Colorado School of Mines
 * @version 2010.05.02
 */
public class Streamlines implements Model {

  /**
   * Constructs a new streamline with a given array list of streamlines.
   * @param streams array list of streamlines.
   */
  public Streamlines(ArrayList<float[]> streams) {
    this.streams = streams;
    packVertices(streams);
  }

  /**
   * Gets the packed vertices.
   * @return the packed vertices.
   */
  public float[] getVertices() {
    return _v;
  }

  public void setLineWidth(int w) {
    _w = w;
  }

  /**
   * Draws the model.
   */
  public void drawModel() {
    Iterator<float[]> it = streams.iterator();
    float[] f;
    float x,y,z;
    int j;
    float len = (float)streams.size();
    float n = 0.0f;
    int tal = glGetAttribLocationARB(_prog,"tangent");
    int dal = glGetAttribLocationARB(_prog,"side");
    int wal = glGetAttribLocationARB(_prog,"w");
    int dmal = glGetAttribLocationARB(_prog,"dmax");
    Vector3 dir = new Vector3();
    while (it.hasNext()) {
      f = it.next();
      j = f.length;
      x = f[0]; y = f[1]; z = f[2];
      glPushMatrix();
        glTranslatef(x,y,z);
        glColor3f(0.0f,0.0f,0.0f);
          if (shaders) glBegin(GL_TRIANGLE_STRIP);
          else glBegin(GL_LINE_STRIP);

          for (int i=0; i<j-2; i+=3) {
            // Derive direction of line locally
            if (i==0) {
              dir.x = f[i+3]-f[i+0];
              dir.y = f[i+4]-f[i+1];
              dir.z = f[i+5]-f[i+2];
            } else if (i==j-3) {
              dir.x = f[i+0]-f[i-3];
              dir.y = f[i+1]-f[i-2];
              dir.z = f[i+2]-f[i-1];
            } else {
              dir.x = f[i+3]-f[i-3];
              dir.y = f[i+4]-f[i-2];
              dir.z = f[i+5]-f[i-1];
            }
            dir.normalize();

            if (shaders) {
              glVertexAttrib1f(dmal,_dmax);
              glVertexAttrib1f(wal,_w);
              glVertexAttrib3f(tal,dir.x,dir.y,dir.z);
            }

            glTexCoord2f(n/len,0.0f);
            glVertex3f(f[i+0],f[i+1],f[i+2]);

            if (shaders) {
              glVertexAttrib1f(dmal,_dmax);
              glVertexAttrib1f(wal,_w);
              glVertexAttrib3f(tal,dir.x,dir.y,dir.z);
              glTexCoord2f(n/len,1.0f);
              glVertex3f(f[i+0],f[i+1],f[i+2]);
            }
            n++;
          }
          n = 0;
        glEnd();
      glPopMatrix();

    }
  }

  ///////////////////////////////////////////////////////////////////////////
  // protected

  protected void passShaders(
    boolean shaders, int prog)
  {
    this.shaders = shaders;
    _prog = prog;
  }

  protected boolean shaders = false;
  protected int _prog; // Shader program.
  
  ///////////////////////////////////////////////////////////////////////////
  // private

  private ArrayList<float[]> streams;
  private float[] _v;
  private int[] _l;

  private float _w = 5.0f; // line width.
  private float _dmax = 0.0f;

  private void packVertices(ArrayList<float[]> streams) {
    int len = streams.size();
    _l = new int[len];
    int n = 0;
    for (int i=0; i<len; ++i) n += streams.get(i).length;
    _v = new float[n];
    int tl = 0;
    for (int i=0; i<len; ++i) {
      float[] sl = streams.get(i);
      int l = sl.length;
      for (int j=0; j<l; ++j) {
        _v[tl++] = sl[j];
      }
    }
  }
}
