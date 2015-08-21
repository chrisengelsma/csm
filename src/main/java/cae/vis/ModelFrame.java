/****************************************************************************
Copyright (c) 2010, Colorado School of Mines and others. All rights reserved.
This program and accompanying materials are made available under the terms of
the Common Public License - v1.0, which accompanies this distribution, and is
available at http://www.eclipse.org/legal/cpl-v10.html
****************************************************************************/
package cae.vis;

import javax.swing.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;

import static edu.mines.jtk.util.ArrayMath.*;

import static edu.mines.jtk.ogl.Gl.*;

/**
 * An OpenGL 3D display.
 * @author Chris Engelsma, Colorado School of Mines
 * @version 2010.03.05
 */
public class ModelFrame extends JFrame {

  public static final int VPD_DEFAULT = 800;

  public ModelFrame() {
    this(null);
  }

  public ModelFrame(String title) {
    setGUI();
    _mc = new ModelCanvas(this);
    if(title!=null) this.setTitle(title);
    this.add(_mc);
    this.setSize(_vpd,_vpd);
    this.setVisible(true);
    this.setDefaultCloseOperation(this.EXIT_ON_CLOSE);
    _mc.requestFocus();
  }

  public TriMesh addTriMesh(String tm) {
    return addTriMesh(new TriMesh(tm));
  }
  
  public TriMesh addTriMesh(TriMesh tm) {
    addModel(tm);
    return tm;
  }

  public Streamlines addStreamlines(ArrayList<float[]> streams) {
    return addStreamlines(new Streamlines(streams));
  }

  public Streamlines addStreamlines(Streamlines sl) {
    addModel(sl);
    return sl;
  }

  public void setShaders(Shader vs, Shader fs) {
    _vs = vs;
    _fs = fs;
  }

  ///////////////////////////////////////////////////////////////////////////
  // protected
  
  protected float scale;
  protected float xShift,yShift,zShift;
  protected Shader _vs,_fs;

  ///////////////////////////////////////////////////////////////////////////
  // private

  private int _vpd = VPD_DEFAULT;
  private ModelCanvas _mc;
  public ModelList ml = new ModelList();

  private void addModel(Model m) {
    ml.add(m);
    updateTransforms(m);
  }

  public void updateTransforms(Model m) {
    float[] v = m.getVertices();
    float xmin = v[0]; float xmax = v[0];
    float ymin = v[1]; float ymax = v[1];
    float zmin = v[2]; float zmax = v[2];
    for (int i=0; i<v.length-2; i+=3) {
      if (v[i+0]<xmin) xmin = v[i+0];
      if (v[i+0]>xmax) xmax = v[i+0];
      if (v[i+1]<ymin) ymin = v[i+1];
      if (v[i+1]>ymax) ymax = v[i+1];
      if (v[i+2]<zmin) zmin = v[i+2];
      if (v[i+2]>zmax) zmax = v[i+2];
    }

    float xs = xmax-xmin, ys = ymax-ymin, zs = zmax-zmin;
    float xc = (xmax+xmin)/2.0f, yc = (ymax+ymin)/2.0f, zc = (zmax+zmin)/2.0f;

    if (abs(xc)>xShift) xShift = xc;
    if (abs(yc)>yShift) yShift = yc;
    if (abs(zc)>zShift) zShift = zc;
    
    float max = xs;
    if (max<ys) max = ys;
    if (max<zs) max = zs;
    if (max>scale)
      scale = max;
  }

  private void loadData() throws IOException {
    JFileChooser jfc = new JFileChooser();
    int returnVal = jfc.showOpenDialog(null);
    if (returnVal==JFileChooser.APPROVE_OPTION) {
      File file = jfc.getSelectedFile();
      String filePath = file.getPath();
      if (!filePath.toLowerCase().endsWith(".t"))
        System.err.println("unrecognized extension");
      addTriMesh(filePath);
      _mc.repaint();
    } else return;
  }

  private void setGUI() {
    JMenu fileMenu = new JMenu("File");
    JMenu renderMenu = new JMenu("Render");
    JMenu meshMenu = new JMenu("Mesh");
    JMenu bgMenu = new JMenu("Background");

    Action clearAction = new AbstractAction("Clear") {
      public void actionPerformed(ActionEvent event) {
        ml.clear();
        scale = 0;
        xShift = yShift = zShift = 0;
    }};

    Action loadAction = new AbstractAction("Load") {
      public void actionPerformed(ActionEvent event) {
        try {
          loadData();
        } catch(IOException ioe) {
          System.err.println(ioe);
        }
    }};

    Action solidMeshAction = new AbstractAction("Solid") {
      public void actionPerformed(ActionEvent event) {
        _mc.wire = false;
        _mc.repaint();
    }};

    Action wireMeshAction = new AbstractAction("Wireframe") {
      public void actionPerformed(ActionEvent event) {
        _mc.wire = true;
        _mc.repaint();
    }};

    Action backCullingAction = new AbstractAction("Back") {
      public void actionPerformed(ActionEvent event) {
        _mc.cull = GL_BACK;
    }};

    Action frontCullingAction = new AbstractAction("Front") {
      public void actionPerformed(ActionEvent event) {
        _mc.cull = GL_FRONT;
      }};

    Action exitAction = new AbstractAction("Exit") {
      public void actionPerformed(ActionEvent event) {
        System.exit(0);
    }};

    Action blackBGAction = new AbstractAction("Black") {
      public void actionPerformed(ActionEvent event) {
        _mc.setBackgroundColor(0.0f,0.0f,0.0f);
    }};

    Action whiteBGAction = new AbstractAction("White") {
      public void actionPerformed(ActionEvent event) {
        _mc.setBackgroundColor(1.0f,1.0f,1.0f);
      }};

    Action loopSubAction = new AbstractAction("Loop Subdivide") {
      public void actionPerformed(ActionEvent event) {
        for (Model m:ml.a)
          if (m instanceof TriMesh)
            ((TriMesh) m).subdivideMesh(1);
        scale = 0;
        xShift = yShift = zShift = 0;
        for (Model m:ml.a)
          updateTransforms(m);
      }
    };

    Action shadingAction = new AbstractAction("Smooth/Flat Shading") {
      public void actionPerformed(ActionEvent event) {
        int sm = _mc.sm;
        if (sm==GL_SMOOTH) {
          _mc.sm = GL_FLAT;
        } else {
          _mc.sm = GL_SMOOTH;
        }
    }};

    Action setShadersAction = new AbstractAction("Shaders") {
      public void actionPerformed(ActionEvent event) {
        // TODO Load in new shaders
        if (_vs!=null && _fs!=null) {
          if (ml.shaders) ml.shaders = false;
          else ml.shaders = true;
        }
    }};

    Action setPerspectiveAction = new AbstractAction("Perspective") {
      public void actionPerformed(ActionEvent event) {
        boolean ortho = _mc.ortho;
        if (ortho) _mc.ortho = false;
        else _mc.ortho = true;
    }};



    fileMenu.add(loadAction);
    fileMenu.add(clearAction);
    fileMenu.add(exitAction);

    renderMenu.add(solidMeshAction);
    renderMenu.add(wireMeshAction);
    renderMenu.add(shadingAction);
    renderMenu.add(setShadersAction);
    renderMenu.add(setPerspectiveAction);

    bgMenu.add(blackBGAction);
    bgMenu.add(whiteBGAction);

    meshMenu.add(backCullingAction);
    meshMenu.add(frontCullingAction);
    meshMenu.add(loopSubAction);

    JMenuBar menuBar = new JMenuBar();
    menuBar.add(fileMenu);
    menuBar.add(renderMenu);
    menuBar.add(meshMenu);
    menuBar.add(bgMenu);

    this.setJMenuBar(menuBar);
  }
}
