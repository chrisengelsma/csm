/****************************************************************************
Copyright (c) 2010, Colorado School of Mines and others. All rights reserved.
This program and accompanying materials are made available under the terms of
the Common Public License - v1.0, which accompanies this distribution, and is
available at http://www.eclipse.org/legal/cpl-v10.html
****************************************************************************/
package cae.vis;

import static edu.mines.jtk.ogl.Gl.*;

/**
 * A list of 3D models.
 * @author Chris Engelsma, Colorado School of Mines
 * @version 2010.03.05
 */
public class ModelList {
  
  public int n;
  public Model[] a = new Model[1];

  public void add(Model m) {
    if (n==a.length) {
      Model[] t = new Model[n+1];
      System.arraycopy(a,0,t,0,n);
      a = t;
    }
    a[n++] = m;
  }

  public void print() {
    for (Model m:a) {
      System.out.println(m);
    }
  }

  public void clear() {
    n = 0;
    a = new Model[1];
  }

  public void draw() {
    for(Model m:a) {
      if (m==null) break;
      if (m instanceof Streamlines)
        ((Streamlines) m).passShaders(shaders,_prog);
      glPushMatrix();
        m.drawModel();
      glPopMatrix();
    }
  }

  ///////////////////////////////////////////////////////////////////////////
  // protected

  protected boolean shaders = false;
  protected int _prog; // Shader program.

}
