/****************************************************************************
Copyright (c) 2010, Colorado School of Mines and others. All rights reserved.
This program and accompanying materials are made available under the terms of
the Common Public License - v1.0, which accompanies this distribution, and is
available at http://www.eclipse.org/legal/cpl-v10.html
****************************************************************************/
package cae.vis;

import static cae.util.VectorMath.*;

import javax.media.opengl.*;
import javax.media.opengl.glu.GLU;
import javax.swing.*;
import java.awt.event.*;

import static edu.mines.jtk.ogl.Gl.*;
import static edu.mines.jtk.util.ArrayMath.*;


/**
 * @author Chris Engelsma, Colorado School of Mines
 * @version 2010.02.23
 */
public class ShadowVolume extends MouseAdapter
  implements GLEventListener, KeyListener
{

  public static final int VPD_DEFAULT = 800;
  public static final int VPD_MIN = 200;
  public static final int VPD_MAX = 1024;

  /**
   * Constructs a new shadow volume.
   * @param fileName the name of the file.
   */
  public ShadowVolume(String fileName) {
    this(new TriMesh(fileName));
  }

  /**
   * Construts a new shadow volume
   * @param data a model data object.
   */
  public ShadowVolume(TriMesh data) {
    cap = new GLCapabilities();
    cap.setStencilBits(8);                  // Request stencil bits
    canvas = new GLCanvas(cap);
    md = data;
    v = md.getVertices();
    sv = md.getVertices();
    t = md.getTriangles();
    nt = t.length/3;
    nv = v.length/3;
    computeTranslation();
    computeNormals();
    canvas.addGLEventListener(this);
    frame.add(canvas);
    frame.setSize(VPD_DEFAULT,VPD_DEFAULT);
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    JMenuBar menuBar = setUpMenuBar();
    frame.setJMenuBar(menuBar);
    frame.setVisible(true);
    canvas.requestFocus();
  }

  public void init(GLAutoDrawable glDrawable) {
    glDrawable.addKeyListener(this);
    glDrawable.addMouseListener(this);
    glDrawable.addMouseMotionListener(this);
    
    glClearColor(1.0f,1.0f,1.0f,1.0f);


    glEnable(GL_NORMALIZE);

    glEnable(GL_BLEND);
    glBlendFunc(GL_SRC_ALPHA,GL_ONE_MINUS_SRC_ALPHA);
    
    glEnable(GL_STENCIL_TEST);
    glEnable(GL_DEPTH_TEST);
    initLightSource();
  }

  private void drawScene() {
    glMatrixMode(GL_PROJECTION);
      glLoadIdentity();
      glu.gluPerspective(fov,1.0f,15.0f,25.0f);

    glMatrixMode(GL_MODELVIEW);
      glLoadIdentity();

    if (animate) {
      angle1 += dangle1;
      angle2 += dangle2;
    }

    glTranslatef(0,0,-20);

    /* Handles the trackball interface */
    if (clicking && dragging && !zooming) {
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

    glRotatef(angle1,1,2,3);
    glRotatef(angle2,-2,-1,0);

    glMultMatrixf(Ro,0);
    glMultMatrixf(R,0);

    glScalef(2/S[0],2/S[1],2/S[2]);
    glTranslatef(-C[0],-C[1],-C[2]);

    setMaterialProperties(0.12f,0.56f,1.0f,1.0f);
    renderTriangles();
    setMaterialProperties(1.0f,0.0f,0.0f,1.0f);
    renderLightSource();
  }
  
  public void display(GLAutoDrawable glDrawable) {
    /* If there are triangles and vertices... */
    if (t!=null && v!=null) {
      
      /* Set the material */
//      setMaterialProperties(0.8f,0.8f,0.8f,1.0f);

      /* Clear the buffers */
      glClear(GL_COLOR_BUFFER_BIT|GL_DEPTH_BUFFER_BIT|GL_STENCIL_BUFFER_BIT);

      /* Render the scene in shadow */
      glDisable(GL_LIGHT0);                   // Turn the lights off
      drawScene();                            // Draw the mesh
      
      glColorMask(false,false,false,false);   // Color mask read-only
      glDepthMask(false);                     // Depth mask read-only

      glEnable(GL_STENCIL_TEST);              // Enable stencil test
      glClearStencil(0x0);
      
      glStencilFunc(GL_ALWAYS,0x0,0x0);       // Set stencil to always pass

      /* Scan convert the shadows, so combine the stencil operations */
      glStencilOpSeparate(GL_FRONT,GL_KEEP,GL_KEEP,GL_INCR_WRAP);
      glStencilOpSeparate(GL_BACK,GL_KEEP,GL_KEEP,GL_DECR_WRAP);
      renderShadowPolygons();                 // Draw the shadows

      glDepthMask(true);                      // Depth mask can write
      glDepthFunc(GL_LEQUAL);                 // Depth func <=
      
      glColorMask(true,true,true,true);       // Color mask can write
      glStencilOp(GL_KEEP,GL_KEEP,GL_KEEP);   // No stencil operations

      /* Render the lit part */
      glStencilFunc(GL_EQUAL,0x0,0xffffffff);       // If = 0
      glEnable(GL_LIGHT0);                    // Turn light on
      drawScene();                            // Draw lit mesh

      glDepthFunc(GL_LESS);                   // Reset depth function to <
      glDisable(GL_STENCIL_TEST);             // Disable stencil test
      
      /* Optional rendering for shadow lines */
      if (shadows==true) {
        setMaterialProperties(0.2f,0.2f,0.2f,0.1f);
        renderShadowPolygons();
      } else if (shadowsLines==true) {
        glPolygonMode(GL_FRONT_AND_BACK,GL_LINE);
        setMaterialProperties(0.2f,0.2f,0.2f,1.0f);
        renderShadowPolygons();
        glPolygonMode(GL_FRONT_AND_BACK,GL_FILL);
      }
    }

    glFlush();                                // Flush the pipeline
    canvas.repaint();                         // Repaint the canvas
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

  // Not used
  public void displayChanged(
    GLAutoDrawable glDrawable, boolean modeChanged, boolean deviceChanged) {}

  // Key Events
  public void keyTyped(KeyEvent e) {
    if (e.getKeyChar()=='r') {
      if (animate) animate = false;
      else animate = true;
    }
    if (e.getKeyChar()=='s') {
      if (shadows==false && shadowsLines==false) {
        shadows = true;
        shadowsLines = false;
      } else if (shadows==true && shadowsLines==false) {
        shadowsLines = true;
        shadows = false;
      } else {
        shadows = false;
        shadowsLines = false;
      }
    }
    if (e.getKeyChar()=='n') {
      if (gouraud) {
        gouraud = false;
        flat = true;
      } else {
        gouraud = true;
        flat = false;
      }
    }
  }

  public void keyPressed(KeyEvent e) {
    if (e.getKeyCode()==KeyEvent.VK_ESCAPE) {
      frame.dispose();
      System.exit(0);
    }
    if (e.getKeyCode()==KeyEvent.VK_ALT)
      zooming = true;
  }

  public void keyReleased(KeyEvent e) {
    if (e.getKeyCode()==KeyEvent.VK_ALT)
      zooming = false;
  }

  public void mouseClicked(MouseEvent e) {
    dragged = false;
  }

  public void mousePressed(MouseEvent e) {
    clicking = true;
    dragged = false;
    i0 = vpd-e.getX();
    j0 = vpd-e.getY();
    u = getTrackballXY(i0,j0);
  }

  public void mouseReleased(MouseEvent e) {
    clicking = false;
    dragging = false;
    float i1 = vpd-e.getX();
    float j1 = vpd-e.getY();
    a = getTrackballXY(i1,j1);
    if (!zooming) {
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
    float[] b = getTrackballXY(i1,j1);
    if (zooming) {
      float dif = a[1]-b[1];
      if (dif>0.005f && fov<120)
        fov *= 1.03f;
      else if (dif<-0.005f)
        fov *= 0.97f;
      a = b;
    } else {
      a = b;
      axis = cp(a,u,true);
      angle = (float)(acos(dot(a,u))*180.0f/PI);
    }
    canvas.repaint();
  }
  
  ///////////////////////////////////////////////////////////////////////////
  // private

  private float[] R = I();
  private float[] Ro = I();

  private float i0 = 0; // Event coords X
  private float j0 = 0; // Event coords Y

  private float[] u = new float[3]; // Original coords
  private float[] a = new float[3]; // Current coords

  private float angle = 0;
  private float axis[] = new float[3];

  private float[] C = new float[] {0,0,0};
  private float[] S = new float[] {2,2,2};
  private float[] min = new float[4];
  private float[] max = new float[4];
  private float fov = 8.0f;

  // Key Handlers
  private boolean zooming = false;
  private boolean dragged = false;
  private boolean clicking = false;
  private boolean dragging = false;

  // Shading
  private boolean flat = true;
  private boolean gouraud = false;

  // Animation
  private boolean animate = false;
  private boolean shadows = true;
  private boolean shadowsLines = false;

  private int vpd = VPD_DEFAULT; // (square) viewport dimensions

  private float angle1 = 0.0f;
  private float angle2 = 0.0f;
  private float dangle1 = 0.010f;
  private float dangle2 = 0.015f;

  private GLCanvas canvas;
  private GLCapabilities cap;
  
  private final GLU glu = new GLU();

  private final JFrame frame = new JFrame("Shadow Volume Rendering");

  private int[] t;        // Model triangles
  private float[] v;      // Model vertices
  private float[] sv;     // Model vertices for shadow computation
  private float[] vn,tn;  // Model vertex/triangle normals
  private int nt,nv;      // Model #of triangles/vertices

  private float[] lightModelAmbient = new float[] {0.5f,0.5f,0.5f,1.0f};
  private float[] lightAmbient = new float[] {0.1f,0.1f,0.1f,1.0f};
  private float[] lightDiffuse = new float[] {0.7f,0.7f,0.7f,1.0f};
  private float[] lightSpecular = new float[] {0.0f,0.0f,0.0f,1.0f};
  private float[] L = new float[] {10.0f,30.0f,50.0f,1.0f};

  private TriMesh md;

  private void initLightSource(){
    glLightModelfv(GL_LIGHT_MODEL_AMBIENT,lightModelAmbient,0);
    
    glLightfv(GL_LIGHT0,GL_AMBIENT,lightAmbient,0);
    glLightfv(GL_LIGHT0,GL_DIFFUSE,lightDiffuse,0);
    glLightfv(GL_LIGHT0,GL_SPECULAR,lightSpecular,0);
    glLightf(GL_LIGHT0,GL_CONSTANT_ATTENUATION,1.0f);
    glLightf(GL_LIGHT0,GL_LINEAR_ATTENUATION,0.0f);
    glLightf(GL_LIGHT0,GL_QUADRATIC_ATTENUATION,0.0f);

    glMatrixMode(GL_MODELVIEW);
      glLoadIdentity();
      glLightfv(GL_LIGHT0,GL_POSITION,L,0);
    
    glEnable(GL_LIGHTING);
    glEnable(GL_LIGHT0);
  }

  private void setMaterialProperties(float r, float g, float b, float a) {
    float[] matSpecular = new float[]{0.0f,0.0f,0.0f,1.0f};
    float[] matAmbientAndDiffuse = new float[]{0.2f,0.2f,0.2f,1.0f};
    float[] matShininess = new float[]{0.0f};

    matSpecular[0] = matAmbientAndDiffuse[0] = r;
    matSpecular[1] = matAmbientAndDiffuse[1] = g;
    matSpecular[2] = matAmbientAndDiffuse[2] = b;
    matSpecular[3] = matAmbientAndDiffuse[3] = a;

    glMaterialfv(GL_FRONT,GL_SPECULAR,matSpecular,0);
    glMaterialfv(GL_FRONT,GL_SHININESS,matShininess,0);
    glMaterialfv(GL_FRONT,GL_AMBIENT_AND_DIFFUSE,matAmbientAndDiffuse,0);
  }

  private void computeNormals() {
    vn = new float[3*nv];
    tn = new float[3*nt];
    float[] v0 = new float[3];
    float[] v1 = new float[3];
    float[] v2 = new float[3];
    float[] v0v1 = new float[3];
    float[] v0v2 = new float[3];
    float[] xv;
    for (int i=0; i<3*nt-2; i+=3) {
      v0[0] = v[3*t[i+0]+0]; v0[1] = v[3*t[i+0]+1]; v0[2] = v[3*t[i+0]+2];
      v1[0] = v[3*t[i+1]+0]; v1[1] = v[3*t[i+1]+1]; v1[2] = v[3*t[i+1]+2];
      v2[0] = v[3*t[i+2]+0]; v2[1] = v[3*t[i+2]+1]; v2[2] = v[3*t[i+2]+2];

      v0v1[0] = v1[0]-v0[0]; v0v1[1] = v1[1]-v0[1]; v0v1[2] = v1[2]-v0[2];
      v0v2[0] = v2[0]-v0[0]; v0v2[1] = v2[1]-v0[1]; v0v2[2] = v2[2]-v0[2];

      xv = cp(v0v1,v0v2,true);

      vn[3*t[i+0]+0] += xv[0];vn[3*t[i+0]+1] += xv[1];vn[3*t[i+0]+2] += xv[2];
      vn[3*t[i+1]+0] += xv[0];vn[3*t[i+1]+1] += xv[1];vn[3*t[i+1]+2] += xv[2];
      vn[3*t[i+2]+0] += xv[0];vn[3*t[i+2]+1] += xv[1];vn[3*t[i+2]+2] += xv[2];

      tn[i+0] = xv[0]; tn[i+1] = xv[1]; tn[i+2] = xv[2];
    }
  }

  private void computeTranslation() {
     float xmin = v[0]; float xmax = v[0];
     float ymin = v[1]; float ymax = v[1];
     float zmin = v[2]; float zmax = v[2];
     for (int i=0; i<3*nv-2; i+=3) {
       if (v[i+0]<xmin) xmin = v[i+0];
       if (v[i+0]>xmax) xmax = v[i+0];
       if (v[i+1]<ymin) ymin = v[i+1];
       if (v[i+1]>ymax) ymax = v[i+1];
       if (v[i+2]<zmin) zmin = v[i+2];
       if (v[i+2]>zmax) zmax = v[i+2];
     }
     min[0] = xmin; min[1] = ymin; min[2] = zmin;
     max[0] = xmax; max[1] = ymax; max[2] = zmax;

     min[3] = min[0];
     max[3] = max[0];

     for (int i=0; i<3; ++i) {
       min[3] = (min[3]>min[i])?min[i]:min[3];
       max[3] = (max[3]<max[i])?max[i]:max[3];
     }

     C[0] = (xmax+xmin)/2; C[1] = (ymax+ymin)/2; C[2] = (zmax+zmin)/2;
     S[0] = S[1] = S[2] = max[3]-min[3];
   }

  /**
   * Renders shadow polygons.
   * For each triangle,
   * determine if it is ccw to the lightsource (v0v1 x v0v2) * v0L >? 0
   * if not (<0), swap two vertices
   * Assemble shadow polygons:
   * <code>
   *           L
   *           v2
   *          /\
   *        /   \
   *      /      \
   * v0 /_________\ v1 ,
   * </code>
   * where vi = (xi,yi,zi)
   *       L  = (lx,ly,lz)
   * A quad with back-face culling will have vertices:
   * (x1   , y1   , z1   , 1)
   * (x0   , y0   , z0   , 1)
   * (x0-lx, y0-ly, z0-lz, 0)
   * (x1-lx, y1-ly, z1-lz, 0)
   */
  private void renderShadowPolygons() {
    int n;
    float a,b,c,sign;
    float[] v0 = new float[3], v1 = new float[3], v2 = new float[3];
    float[] v0v1 = new float[3], v0v2 = new float[3], v0L = new float[3];

    /* Matrices used for computing new vertices
     * Appropriate reproduction of in-software transformations:
     * Translate
     * Scale
     * Multiply by R
     * Multiply by Ro
     * Rotate by angle2
     * Rotate by angle1
     */
    
    // Translation
    float[] Tr = I();
    Tr[3] = -C[0]; Tr[7] = -C[1]; Tr[11] = -C[2];
    float[] tr = I();
    tr[3] = tr[7] = tr[11] = -20;

    // Scale
    float[] Sc = I();
    Sc[0] = 2/S[0]; Sc[5] = 2/S[1]; Sc[10] = 2/S[2];

    // Rotation
    float[] rt = rotateMat(angle,normalize(axis));
    float[] r2 = rotateMat(angle2,normalize(new float[]{-2,-1,0}));
    float[] r1 = rotateMat(angle1,normalize(new float[]{ 1, 2,3}));

    glBegin(GL_QUADS);
      glEnable(GL_NORMALIZE);
      for (n=0; n<3*nt-2; n+=3) {
        v0[0] = sv[3*t[n+0]+0];v0[1] = sv[3*t[n+0]+1];v0[2] = sv[3*t[n+0]+2];
        v1[0] = sv[3*t[n+1]+0];v1[1] = sv[3*t[n+1]+1];v1[2] = sv[3*t[n+1]+2];
        v2[0] = sv[3*t[n+2]+0];v2[1] = sv[3*t[n+2]+1];v2[2] = sv[3*t[n+2]+2];

        v0v1[0] = v1[0]-v0[0]; v0v1[1] = v1[1]-v0[1]; v0v1[2] = v1[2]-v0[2];
        v0v2[0] = v2[0]-v0[0]; v0v2[1] = v2[1]-v0[1]; v0v2[2] = v2[2]-v0[2];
         v0L[0] =  L[0]-v0[0];  v0L[1] =  L[1]-v0[1];  v0L[2] =  L[2]-v0[2];

        sign = dot(cp(v0v1,v0v2,true),v0L);

        // Swap v0 and v1 if oriented incorrectly.
        if (sign<0) {
          float[] temp = copy(v0);
          v0 = copy(v1);
          v1 = copy(temp);
        }

        glVertex3f(v1[0],v1[1],v1[2]);
        glVertex3f(v0[0],v0[1],v0[2]);
        glVertex4f(v0[0]-L[0],v0[1]-L[1],v0[2]-L[2],0.0f);
        glVertex4f(v1[0]-L[0],v1[1]-L[1],v1[2]-L[2],0.0f);

        glVertex3f(v2[0],v2[1],v2[2]);
        glVertex3f(v1[0],v1[1],v1[2]);
        glVertex4f(v1[0]-L[0],v1[1]-L[1],v1[2]-L[2],0.0f);
        glVertex4f(v2[0]-L[0],v2[1]-L[1],v2[2]-L[2],0.0f);
        
        glVertex3f(v0[0],v0[1],v0[2]);
        glVertex3f(v2[0],v2[1],v2[2]);
        glVertex4f(v2[0]-L[0],v2[1]-L[1],v2[2]-L[2],0.0f);
        glVertex4f(v0[0]-L[0],v0[1]-L[1],v0[2]-L[2],0.0f);
      }
    glEnd();
  }
  
  private void renderTriangles() {
    float na0,na1,na2;
    float nb0,nb1,nb2;
    float nc0,nc1,nc2;
    float vax,vay,vaz;
    float vbx,vby,vbz;
    float vcx,vcy,vcz;
    float n0,n1,n2;
    glBegin(GL_TRIANGLES);
      glEnable(GL_NORMALIZE);
      for (int n=0; n<3*nt-2; n+=3) {
        na0 = vn[3*t[n+0]+0]; na1 = vn[3*t[n+0]+1]; na2 = vn[3*t[n+0]+2];
        nb0 = vn[3*t[n+1]+0]; nb1 = vn[3*t[n+1]+1]; nb2 = vn[3*t[n+1]+2];
        nc0 = vn[3*t[n+2]+0]; nc1 = vn[3*t[n+2]+1]; nc2 = vn[3*t[n+2]+2];

        vax = v[3*t[n+0]+0]; vay = v[3*t[n+0]+1]; vaz = v[3*t[n+0]+2];
        vbx = v[3*t[n+1]+0]; vby = v[3*t[n+1]+1]; vbz = v[3*t[n+1]+2];
        vcx = v[3*t[n+2]+0]; vcy = v[3*t[n+2]+1]; vcz = v[3*t[n+2]+2];

        n0 = tn[n+0]; n1 = tn[n+1]; n2 = tn[n+2];

        float[] nan = normalize(na0,na1,na2);
        float[] nbn = normalize(nb0,nb1,nb2);
        float[] ncn = normalize(nc0,nc1,nc2);

        if (gouraud) {
          glNormal3f(nbn[0],nbn[1],nbn[2]); glVertex3f(vbx,vby,vbz);
          glNormal3f(nan[0],nan[1],nan[2]); glVertex3f(vax,vay,vaz);
          glNormal3f(ncn[0],ncn[1],ncn[2]); glVertex3f(vcx,vcy,vcz);
        } else if (flat) {
          glNormal3f(n0,n1,n2);
          glVertex3f(vax,vay,vaz);
          glVertex3f(vbx,vby,vbz);
          glVertex3f(vcx,vcy,vcz);
        }
      }
    glEnd();
  }
  
  private float[] getTrackballXY(float i, float j) {
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

  private JMenuBar setUpMenuBar() {
    JMenu fileMenu = new JMenu("File");
    Action loadAction = new AbstractAction("Exit") {
      public void actionPerformed(ActionEvent event) {
        frame.dispose();
        System.exit(0);
      }
    };
    fileMenu.add(loadAction);
    JMenuBar menuBar = new JMenuBar();
    menuBar.add(fileMenu);
    return menuBar;
  }
  
  private boolean isInsideTrackball(float x, float y) {
    return (1-x*x-y*y>=0)?true:false;
  }

  private float[] I() {
    float[] m = new float[16];
    for (int i=0; i<m.length; ++i)
      m[i] = (i%5==0)?1:0;
    return m;
  }

  private static float[] rotateMat(float theta, float[] vec) {
    float x = vec[0], y = vec[1], z = vec[2];
    float c = cos(theta);
    float s = sin(theta);
    return new float[]{
      x*x*(1-c)+c,   x*y*(1-c)-z*s, x*z*(1-c)+y*s, 0,
      y*x*(1-c)+z*s, y*y*(1-c)+c,   y*z*(1-c)-x*s, 0,
      z*x*(1-c)-y*s, z*y*(1-c)+x*s, z*z*(1-c)+c,   0,
      0,             0,             0,             1 };
  }
  
  private static void multMat(float[] M, float[] v) {
    float a = v[0], b = v[1], c = v[2];
    v[0] = M[0 ]*a+M[1 ]*b+M[2 ]*c+M[3 ];
    v[1] = M[4 ]*a+M[5 ]*b+M[6 ]*c+M[7 ];
    v[2] = M[8 ]*a+M[9 ]*b+M[10]*c+M[12];
  }

  private void renderLightSource() {
    glPointSize(10);
    glBegin(GL_POINTS);
      glVertex3f(L[0],L[1],L[2]);
    glEnd();
  }
}
