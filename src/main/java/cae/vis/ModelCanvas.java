/****************************************************************************
Copyright (c) 2010, Colorado School of Mines and others. All rights reserved.
This program and accompanying materials are made available under the terms of
the Common Public License - v1.0, which accompanies this distribution, and is
available at http://www.eclipse.org/legal/cpl-v10.html
****************************************************************************/
package cae.vis;

import javax.media.opengl.*;
import javax.media.opengl.glu.GLU;

import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;

import static edu.mines.jtk.ogl.Gl.*;

/**
 * A model canvas.
 * @author Chris Engelsma, Colorado School of Mines
 * @version 2010.05.02
 */
public class ModelCanvas extends GLCanvas
  implements GLEventListener, KeyListener
{

  public ModelCanvas(ModelFrame mf) {
    _mf = mf;
    _tb = new TrackBall(this);
    this.addGLEventListener(this);
    this.addKeyListener(this);
  }

  public void init(GLAutoDrawable glDrawable) {
    glEnable(GL_BLEND);
      glBlendFunc(GL_SRC_ALPHA,GL_ONE_MINUS_SRC_ALPHA);
    glEnable(GL_NORMALIZE);
    glEnable(GL_DEPTH_TEST);
    glEnable(GL_CULL_FACE);
  }

  public void display(GLAutoDrawable glDrawable) {

    if (_mf._vs!=null && _mf._fs!=null && _prog==-1)
      initializeShaders();
    
    glShadeModel(sm);
    
    glClearColor(_r,_g,_b,1.0f);
    
    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

    initLightSource();
    
    glCullFace(cull);

    glMatrixMode(GL_PROJECTION);
      glLoadIdentity();
      if (!ortho) _glu.gluPerspective(
        89.98*(Math.atan(zoom-2)+Math.PI/2)/Math.PI+0.01,1,15.0,25.0);
      else glOrtho(-zoom,zoom,-zoom,zoom,3.0f,-3.0f);


    glMatrixMode(GL_MODELVIEW);
      glLoadIdentity();

    glTranslatef(0,0,-(d+1));

    _tb.applyRotation();

    scaleAndTranslate();

    if (_mf.ml.shaders) glUseProgramObjectARB(_prog);
    _mf.ml.draw();
    if (_mf.ml.shaders) glUseProgram(0);

    
    if (wire) {
      glEnable(GL_POLYGON_OFFSET_FILL);
      glPolygonMode(GL_FRONT_AND_BACK,GL_LINE);
      glPolygonOffset(1.0f,1.0f);
      glDisable(GL_LIGHTING);
      glColor3f(1.0f,1.0f,1.0f);
      _mf.ml.draw();
      glPolygonMode(GL_FRONT_AND_BACK,GL_FILL);
    }

    glFlush();
    this.repaint();
  }

  public void reshape(
    GLAutoDrawable glAutoDrawable, int x, int y, int w, int h)
  {
    if (w<h) vpd = h;
    else     vpd = w;

    glViewport(0,0,vpd,vpd);

    glMatrixMode(GL_PROJECTION);
    glLoadIdentity();
    glMatrixMode(GL_MODELVIEW);
    glLoadIdentity();
  }

  public void displayChanged(
    GLAutoDrawable glDrawable, boolean modeChanged, boolean deviceChanged)
  {
  }

  public ModelFrame getFrame() {
    return _mf;
  }

  public void keyPressed(KeyEvent e) {
    if (e.getKeyChar()=='c')
      if (cull==GL_FRONT) cull = GL_BACK;
      else cull = GL_FRONT;
  }

  public void keyTyped(KeyEvent e) {
  }

  public void keyReleased(KeyEvent e) {
  }

  public void setBackgroundColor(float r, float g, float b) {
    _r = r;
    _g = g;
    _b = b;
  }

  ///////////////////////////////////////////////////////////////////////////
  // protected

  protected int vpd = ModelFrame.VPD_DEFAULT;
  protected boolean wire = false;
  protected int cull = GL_BACK;
  protected int sm = GL_SMOOTH;
  protected float zoom = -2.0f;
  protected boolean ortho = false;
  protected int _prog = -1; // Shader program.

  ///////////////////////////////////////////////////////////////////////////
  // private

  private GLU _glu = new GLU();
  private float d = 21.0f;

  private float _r = 1.0f;
  private float _g = 1.0f;
  private float _b = 1.0f;

  private ModelFrame _mf;
  private TrackBall _tb;

  private GLCapabilities _cap;

  private void scaleAndTranslate() {
    glScalef(1.0f/_mf.scale,1.0f/_mf.scale,1.0f/_mf.scale);
    glTranslatef(-_mf.xShift,-_mf.yShift,-_mf.zShift);
  }

  private void initLightSource() {
    float[] lAmbient = new float[]{0.1f,0.1f,0.1f,1.0f};
    float[] lDiffuse = new float[]{1.0f,1.0f,1.0f,1.0f};
    float[] lSpecular = new float[]{0.0f,0.0f,0.0f,1.0f};
    float[] lPosition = new float[]{0,0,0,0};
    if (cull==GL_BACK) {
      lPosition = new float[]{0,0,-999.0f,1.0f};
    } else {
      lPosition = new float[]{0,0,999.0f,1.0f};
    }


    glLightfv(GL_LIGHT0,GL_AMBIENT,lAmbient,0);
    glLightfv(GL_LIGHT0,GL_DIFFUSE,lDiffuse,0);
    glLightfv(GL_LIGHT0,GL_SPECULAR,lSpecular,0);
    glLightf(GL_LIGHT0,GL_CONSTANT_ATTENUATION,1.0f);
    glLightf(GL_LIGHT0,GL_LINEAR_ATTENUATION,0.0f);
    glLightf(GL_LIGHT0,GL_QUADRATIC_ATTENUATION,0.0f);

    glMatrixMode(GL_MODELVIEW);
      glLoadIdentity();
      glLightfv(GL_LIGHT0,GL_POSITION,lPosition,0);

    glEnable(GL_LIGHTING);
    glEnable(GL_LIGHT0);
  }

  private void initializeShaders() {
    String v = _mf._vs.getShaderText();
    String f = _mf._fs.getShaderText();

    String[] vsh = new String[] {v};
    String[] fsh = new String[] {f};
    int[] lenvsh = new int[]{v.length()};
    int[] lenfsh = new int[]{f.length()};

    int vshid = glCreateShaderObjectARB(GL_VERTEX_SHADER_ARB);
    int fshid = glCreateShaderObjectARB(GL_FRAGMENT_SHADER_ARB);

    glShaderSourceARB(vshid,1,vsh,lenvsh,0);
    glShaderSourceARB(fshid,1,fsh,lenfsh,0);

    glCompileShaderARB(vshid);
    glCompileShaderARB(fshid);

    check(vshid,lenvsh);
    check(fshid,lenfsh);

    _prog = glCreateProgramObjectARB();
    glAttachObjectARB(_prog,vshid);
    glAttachObjectARB(_prog,fshid);

    glLinkProgramARB(_prog);
    check(_prog);

    _mf.ml._prog = _prog;
  }
  
  private void check(int program) {
    byte[] buf;
    int[] len;
    int[] ok = new int[1];
    glGetObjectParameterivARB(program,GL_OBJECT_LINK_STATUS_ARB,ok,0);
    if (ok[0]==0) {
      buf = new byte[2048];
      len = new int[1];
      glGetInfoLogARB(program,buf.length,len,0,buf,0);
      System.err.println("Error combining shaders");
      fail(new String(buf,0,len[0]));
    }
  }
  
  private void check(int shader, int[] len) {
    byte[] buf;
    int[] ok = new int[1];
    glGetObjectParameterivARB(shader,GL_OBJECT_COMPILE_STATUS_ARB,ok,0);
    if (ok[0]==0) {
      buf = new byte[2048];
      glGetInfoLogARB(shader,buf.length,len,0,buf,0);
      if (shader==GL_VERTEX_SHADER_ARB) {
        System.err.println("Error in vertex shader");
      } else if (shader==GL_FRAGMENT_SHADER_ARB) {
        System.err.println("Error in fragment shader");
      }
      fail(new String(buf,0,len[0]));
    }
  }

  private void check(String extension) {
    if (!isExtensionAvailable(extension)) {
      System.err.println(extension+" not available");
    }
  }

  private static void fail(String msg) {
    System.err.println(msg);
  }  


}
