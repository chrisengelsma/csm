/****************************************************************************
Copyright (c) 2010, Colorado School of Mines and others. All rights reserved.
This program and accompanying materials are made available under the terms of
the Common Public License - v1.0, which accompanies this distribution, and is
available at http://www.eclipse.org/legal/cpl-v10.html
****************************************************************************/
package cae.vis;

import static cae.util.VectorMath.*;

import edu.mines.jtk.dsp.*;

import javax.media.opengl.*;
import javax.swing.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;

import static edu.mines.jtk.ogl.Gl.*;
import static edu.mines.jtk.util.ArrayMath.*;

/**
 * @author Chris Engelsma, Colorado School of Mines
 * @version 2010.01.01
 */
public class DepthDependentHalos extends MouseAdapter
  implements GLEventListener, KeyListener, MouseWheelListener
{
  
  public static final int VPD_DEFAULT = 800;
  public static final int VPD_MIN = 200;
  public static final int VPD_MAX = 1024;

  public DepthDependentHalos(
    ArrayList<float[]> nodes, Sampling s1, Sampling s2, Sampling s3,
    Shader vs, Shader fs)
  {
    streams = nodes;
    _s1 = s1;
    _s2 = s2;
    _s3 = s3;

    _vs = vs;
    _fs = fs;

    _n1 = s1.getCount();
    _n2 = s2.getCount();
    _n3 = s3.getCount();
  }
  
  public DepthDependentHalos(
    EigenTensors3 et, int n1, int n2, int n3, Shader vs, Shader fs) {
    this(et,new Sampling(n1),new Sampling(n2),new Sampling(n3),vs,fs);
  }

  public DepthDependentHalos(
    EigenTensors3 et, Sampling s1, Sampling s2, Sampling s3,
    Shader vs, Shader fs)
  {
    _s1 = s1;
    _s2 = s2;
    _s3 = s3;

    _vs = vs;
    _fs = fs;

    _n1 = s1.getCount();
    _n2 = s2.getCount();
    _n3 = s3.getCount();
    
    buildLines(et);
  }

  public void setLineWidth(float w) {
    _w = w;
    canvas.repaint();
  }

  public void show() {
    canvas = new GLCanvas();
    frame = new JFrame();
    canvas.addGLEventListener(this);
    frame.add(canvas);
    frame.setSize(VPD_DEFAULT,VPD_DEFAULT);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setVisible(true);
    canvas.addKeyListener(this);
    canvas.addMouseListener(this);
    canvas.addMouseMotionListener(this);
    canvas.addMouseWheelListener(this);
    canvas.requestFocus();
  }
  
  public void init(GLAutoDrawable glDrawable) {
    initializeShaders();
    glEnable(GL_DEPTH_TEST);
      glClearColor(1.0f,1.0f,1.0f,1.0f);
  }

  public void display(GLAutoDrawable glDrawable) {
    glMatrixMode(GL_PROJECTION);
      glLoadIdentity();
      glOrtho(-zoom,zoom,-zoom,zoom,3.0f,-3.0f);

    glMatrixMode(GL_MODELVIEW);
      glLoadIdentity();
    
    if (rotate) {
      angle1 += dangle1;
      angle2 += dangle2;
    }

    glTranslatef(shift[0],shift[1],0);
    
    if (clicking && dragging && !zooming && !shifting) {
      glMatrixMode(GL_MODELVIEW);
      glPushMatrix();
        glLoadIdentity();
        glRotatef(angle,axis[0],axis[1],axis[2]);
        if (!eq(u,a))
          glGetFloatv(GL_MODELVIEW_MATRIX,Ro,0);
        else Ro = I();
      glPopMatrix();
    } else if (!clicking) {
      glMatrixMode(GL_MODELVIEW);
      glPushMatrix();
        glLoadIdentity();
        glRotatef(angle,axis[0],axis[1],axis[2]);
        glMultMatrixf(R,0);
        if (dragged==true)
          glGetFloatv(GL_MODELVIEW_MATRIX,R,0);
      glPopMatrix();
      angle = 0;
    }
    glRotatef(angle1,1,0,0);
    glRotatef(angle2,0,1,1);
    glMultMatrixf(Ro,0);
    glMultMatrixf(R,0);
    glScalef(2.0f/scale,2.0f/scale,2.0f/scale);
    glTranslatef(-xShift,-yShift,-zShift);

    glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);

    if (shaders) glUseProgramObjectARB(_prog);
    drawStreamlines();
    if (shaders) glUseProgram(0);
    
    glFlush();
    canvas.repaint();
  }

  public void reshape(
    GLAutoDrawable glDrawable, int x, int y, int width, int height)
  {
    if (width<height) vpd = height;
    else vpd = width;
    if (vpd<VPD_MIN) vpd = VPD_MIN;
    if (vpd>VPD_MAX) vpd = VPD_MAX;
    glViewport(0,0,vpd,vpd);

    glMatrixMode(GL_PROJECTION);
      glLoadIdentity();
    glMatrixMode(GL_MODELVIEW);
      glLoadIdentity();
  }

  // Not used.
  public void displayChanged(
    GLAutoDrawable glDrawable, boolean modeChanged, boolean deviceChanged) {}
  
 // Key Events
  public void keyTyped(KeyEvent e) {
    if (e.getKeyChar()=='r') {
      if (rotate) rotate = false;
      else rotate = true;
    }
    if (e.getKeyChar()=='s') {
      if (shaders) shaders = false;
      else shaders = true;
    }
  }

  public void keyPressed(KeyEvent e) {
    if (e.getKeyCode()==KeyEvent.VK_ESCAPE) {
      frame.dispose();
      System.exit(0);
    }
    if (e.getKeyCode()==KeyEvent.VK_CONTROL)
      zooming = true;
    if (e.getKeyCode()==KeyEvent.VK_SHIFT)
      shifting = true;
  }

  public void keyReleased(KeyEvent e) {
    if (e.getKeyCode()==KeyEvent.VK_CONTROL)
      zooming = false;
    if (e.getKeyCode()==KeyEvent.VK_SHIFT)
      shifting = false;
  }

  public void mouseClicked(MouseEvent e) {
    dragged = false;
  }

  public void mousePressed(MouseEvent e) {
    clicking = true;
    dragged = false;
    i0 = vpd-e.getX();
    j0 = vpd-e.getY();
    clickedPixel[0] = i0; clickedPixel[1] = j0;
    u = getTrackBallXY(i0,j0);
  }

  public void mouseReleased(MouseEvent e) {
    clicking = false;
    dragging = false;
    float i1 = vpd-e.getX();
    float j1 = vpd-e.getY();
    a = getTrackBallXY(i1,j1);
    if (!zooming && !shifting) {
      axis = cp(a,u,true);
      angle = (float)(acos(dot(a,u))*180.0f/PI);
      u = a;
      Ro = I();
    }
    canvas.repaint();
  }

  public void mouseDragged(MouseEvent e) {
    clicking = true;
    dragging = true;
    dragged = true;
    float i1 = vpd-e.getX();
    float j1 = vpd-e.getY();
    float[] b = getTrackBallXY(i1,j1);
    if (shifting) {
      float difx = clickedPixel[0]-i1;
      float dify = clickedPixel[1]-j1;
      if (difx>0)
        shift[0] += 20.0f/vpd;
      else if (difx<0)
        shift[0] -= 20.0f/vpd;
        
      if (dify>0)
        shift[1] -= 20.0f/vpd;
      else if (dify<0)
        shift[1] += 20.0f/vpd;
        

    } else if (zooming) {
      float dif = a[1]-b[1];
      if (dif>0.001f && zoom<120)
        zoom*=1.03f;
      else if (dif<-0.001f)
        zoom*=0.97f;
      a = b;
    } else {
      a = b;
      axis = cp(a,u,true);
      angle = (float)(acos(dot(a,u))*180.0f/PI);
    }
    canvas.repaint();
  }

  public void mouseWheelMoved(MouseWheelEvent e) {
    int nclicks = e.getWheelRotation();
    _dmax += (float)(nclicks)/100.0f;
  }

  public void computeScales() {
    for (int j=0; j<streams.size(); ++j) {
      float[] v = streams.get(j);
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
  }
  
  ///////////////////////////////////////////////////////////////////////////
  // private

  private GLCanvas canvas;
  private JFrame frame;

  private Sampling _s1,_s2,_s3;
  private int _n1,_n2,_n3;

  private Shader _vs; // Vertex shader
  private Shader _fs; // Fragment shader
  private int _prog;  // Shader program

  private static int vpd = VPD_DEFAULT;
  private float scale = 0.0f;
  private float xShift = 0.0f;
  private float yShift = 0.0f;
  private float zShift = 0.0f;

  private float i0 = 0;
  private float j0 = 0;

  private float[] R = I();
  private float[] Ro = I();

  private float[] u = new float[3];
  private float[] a = new float[3];
  
  private float angle = 0;
  private float[] axis = new float[3];
  private float[] shift = new float[2];
  private float[] clickedPixel = new float[3];

  private float zoom = 1.0f;
  
  private boolean zooming = false;
  private boolean dragged = false;
  private boolean clicking = false;
  private boolean dragging = false;
  private boolean shifting = false;
  
  private boolean rotate = false;
  private boolean shaders = true;

  private float _w = 1.0f;
  
  private static float angle1 = 0.0f;
  private static float angle2 = 0.0f;
  private static final float dangle1 = 0.050f;
  private static final float dangle2 = 0.060f;

  private static float _dmax = 0.0f;

  private static ArrayList<float[]> streams = new ArrayList<float[]>();

  /**
   * Loads the tensors.
   * @param fileName the name of the file.
   * @return the seismic data in an array.
   * @throws IOException
   * @throws ClassNotFoundException
   */
  private static EigenTensors3 loadTensors(String fileName)
    throws IOException, ClassNotFoundException
  {
    ObjectInputStream ois = new ObjectInputStream(
      new FileInputStream(fileName));
    EigenTensors3 et = (EigenTensors3)ois.readObject();
    ois.close();
    return et;
  }

  private void buildLines(EigenTensors3 et) {
    /*
    Hyperstreamline hs = new Hyperstreamline(et,_s3,_s2,_s1);
    for (int i=0; i<10; ++i){
      for (int j=0; j<5; ++j) {
        for (int k=0; k<5; ++k) {
          hs.seed(30+j*10,20+10*i,90+10*k,5);
          streams.add(hs.getNodes());
        }
      }
    }
    */
    /*
    hs.setMaxRecursion(3000);
    for (int i=0; i<40; ++i){
      for (int j=0; j<20; ++j) {
        hs.seed(30+j*1,20+2*i,80,5);
        streams.add(hs.getNodes());
      }
    }
    */

    float p = 28.0f;
    float sigma = 10.0f;
    float beta = 8.0f/3.0f;
    float x = 30.0f, y = 25.0f, z = 30.0f;
    int t = 2500;
    float[] v = new float[(t+1)*3];
//    v[0] = x; v[1] = y; v[2] = z;
    followLorenzAttractor(t,p,sigma,beta,x,y,z,v);
    streams.add(v);

    /*
    float a = 0.2f, b = 0.2f, c = 5.7f;
    int t = 2500;
    float[] v = new float[(t+1)*3];
    float x = 30.0f, y = 25.0f, z = 0.0f;
    followRosslerAttractor(t,a,b,c,x,y,z,v);
    streams.add(v);

    v = new float[(t+1)*3];
    x = 20.0f; y = -25.0f; z = 0.0f;
    followRosslerAttractor(t,a,b,c,x,y,z,v);
    streams.add(v);

    v = new float[(t+1)*3];
    x = 20f; y = 20f; z = 0f;
    followRosslerAttractor(t,a,b,c,x,y,z,v);
    streams.add(v);

    */
    /*
    float a = 1.1f, b = 0.87f;
    int t = 2500;
    float[] v = new float[(t+1)*3];
    float x = 1f, y = 0f, z = 0.02f;
    followRFequation(t,a,b,x,y,z,v);
    streams.add(v);
    */
    
    computeScales();
  }

  private static void followRFequation(int t, float a, float b,
    float x, float y, float z, float[] v)
  {
    float dx = y*(z-1+x*x)+b*x;
    float dy = x*(3*z+1-x*x)+b*y;
    float dz = -2*z*(a + x*y);
    x += dx*0.001f;
    y += dy*0.001f;
    z += dz*0.001f;
    v[3*t+0] = x;
    v[3*t+1] = y;
    v[3*t+2] = z;
    System.out.println(t+": "+x+" "+y+" "+z);
    if (t>1) followRFequation(--t,a,b,x,y,z,v);

  }

   private static void followRosslerAttractor(int t, float a, float b, float c,
    float x, float y, float z, float[] v)
  {
    float dx = -y-z;
    float dy = x+a*y;
    float dz = b+z*(x-c);
    x += dx*0.001f;
    y += dy*0.001f;
    z += dz*0.001f;
    v[3*t+0] = x;
    v[3*t+1] = y;
    v[3*t+2] = z;
    System.out.println(t+": "+x+" "+y+" "+z);
    if (t>1) followLorenzAttractor(--t,a,b,c,x,y,z,v);

  }

  private static void followLorenzAttractor(int t, float p, float sigma, float beta,
    float x, float y, float z, float[] v)
  {
    float dx = sigma*(y-x);
    float dy = x*(p-z)-y;
    float dz = x*y-beta*z;
    x += dx*0.01f;
    y += dy*0.01f;
    z += dz*0.01f;
    v[3*t+0] = x;
    v[3*t+1] = y;
    v[3*t+2] = z;
    System.out.println(t+": "+x+" "+y+" "+z);
    if (t>1) followLorenzAttractor(--t,p,sigma,beta,x,y,z,v);

  }

  private void initializeShaders() {
    String v = _vs.getShaderText();
    String f = _fs.getShaderText();

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
  }

  /**
   * Takes the magnitude of two added vectors
   * @param v1 a vector.
   * @param v2 a vector.
   */
  private static float mag2(float[] v1, float[] v2) {
    int l = v1.length;
    float[] v1v2 = new float[l];
    for (int i=0; i<l; ++i)
      v1v2[i] = v1[i]+v2[i];
    return 0;
  }

  private void drawStreamlines() {
    Iterator<float[]> it = streams.iterator();
    float[] f;
    float[] dir = new float[3];
    float x,y,z;
    int j;
    int tal = glGetAttribLocationARB(_prog,"tangent");
    int dal = glGetAttribLocationARB(_prog,"side");
    int wal = glGetAttribLocationARB(_prog,"w");
    int dmal = glGetAttribLocationARB(_prog,"dmax");
    float len = (float)streams.size();
    float n = 0.0f;
    while(it.hasNext()) {
      f = it.next();
      j = f.length;
      x = f[0]; y = f[1]; z = f[2];
      glPushMatrix();
        glTranslatef(x,y,z);
        glColor3f(0.0f,0.0f,0.0f);
        if (shaders)
          glBegin(GL_TRIANGLE_STRIP);
        else glBegin(GL_LINE_STRIP);
          for (int i=0; i<j-2; i+=3) {
            // Derive direction of line locally
            if (i==0) {
              dir[0] = f[i+3]-f[i+0];
              dir[1] = f[i+4]-f[i+1];
              dir[2] = f[i+5]-f[i+2];
            } else if (i==j-3) {
              dir[0] = f[i+0]-f[i-3];
              dir[1] = f[i+1]-f[i-2];
              dir[2] = f[i+2]-f[i-1];
            } else {
              dir[0] = f[i+3]-f[i-3];
              dir[1] = f[i+4]-f[i-2];
              dir[2] = f[i+5]-f[i-1];
            }
            normalize(dir);

            if (shaders) {
              glVertexAttrib1f(dmal,_dmax);
              glVertexAttrib1f(wal,_w);
              glVertexAttrib3f(tal,dir[0],dir[1],dir[2]);
            }
            glTexCoord2f(n/len,0.0f);
            glVertex3f(f[i+0],f[i+1],f[i+2]);

            if (shaders) {
              glVertexAttrib1f(dmal,_dmax);
              glVertexAttrib1f(wal,_w);
              glVertexAttrib3f(tal,dir[0],dir[1],dir[2]);
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

  private static float[] I() {
    float[] m = new float[16];
    for (int i=0; i<m.length; ++i)
      m[i] = (i%5==0)?1:0;
    return m;
  }

  private float[] getTrackBallXY(float i, float j) {
    float[] m = new float[3];
    float x = (2.0f*i)/(canvas.getWidth()-1.0f)-1.0f;
    float y = -((2.0f*j)/(canvas.getHeight()-1.0f)-1.0f);
    if (isInsideTrackball(x,y)) {
      m[0] = x;
      m[1] = y;
      m[2] = sqrt(1-x*x-y*y);
    } else {
      m[0] = x/sqrt(x*x+y*y);
      m[1] = y/sqrt(x*x+y*y);
      m[2] = 0;
    }
    return m;
  }

  private boolean isInsideTrackball(float x, float y) {
    return (1-x*x-y*y>=0)?true:false;
  }

  private static void normalize(float[] f) {
    float mag = sqrt(f[0]*f[0]+f[1]*f[1]+f[2]*f[2]);
    f[0] /= mag;
    f[1] /= mag;
    f[2] /= mag;
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
