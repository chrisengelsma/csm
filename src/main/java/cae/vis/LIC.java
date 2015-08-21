/****************************************************************************
Copyright (c) 2010, Colorado School of Mines and others. All rights reserved.
This program and accompanying materials are made available under the terms of
the Common Public License - v1.0, which accompanies this distribution, and is
available at http://www.eclipse.org/legal/cpl-v10.html
****************************************************************************/
package cae.vis;

import cae.io.ImageData;

import edu.mines.jtk.util.Direct;
import static edu.mines.jtk.ogl.Gl.*;

import javax.media.opengl.*;
import javax.swing.*;
import java.awt.event.*;
import java.nio.ByteBuffer;
import java.util.Random;
import java.nio.FloatBuffer;

/**
 * @author Chris Engelsma, Colorado School of Mines
 * @version 2010.03.22
 */
public class LIC implements GLEventListener, KeyListener, MouseWheelListener {

  public static final int VPD_DEFAULT = 800;
  public static final int VPD_MIN = 200;
  public static final int VPD_MAX = 1024;


  public LIC(String vertexShader, String fragmentShader) {
    this(vertexShader,fragmentShader,null);
  }

  public LIC(String vertexShader, String fragmentShader, String texture) {
    this(new Shader(vertexShader), new Shader(fragmentShader), texture);
  }

  public LIC(Shader vertexShader, Shader fragmentShader, String texture) {
    _vs = vertexShader;
    _fs = fragmentShader;

    _tex = texture;

    GLCapabilities cap = new GLCapabilities();
    cap.setAlphaBits(8);
    cap.setDoubleBuffered(true);
    
    _canvas = new GLCanvas(cap);
    _canvas.addGLEventListener(this);
    _frame.add(_canvas);
    _frame.setSize(VPD_DEFAULT,VPD_DEFAULT);
    _frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    JMenuBar menuBar = setUpMenuBar();
    _frame.setJMenuBar(menuBar);
    _frame.setVisible(true);
    _canvas.requestFocus();
  }

  public void init(GLAutoDrawable glDrawable) {
    glDrawable.addKeyListener(this);
    glDrawable.addMouseWheelListener(this);

    check("GL_ARB_shader_objects");
    check("GL_ARB_vertex_shader");
    check("GL_ARB_fragment_shader");
    
    initializeShaders();

    /////////////////////////
    if (_tex==null) {
      generateRandomNoise();
    } else {
      loadTexture();
    }

    glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MAG_FILTER,GL_LINEAR);
    glTexParameteri(GL_TEXTURE_2D,GL_TEXTURE_MIN_FILTER,GL_LINEAR);
  }



  public void display(GLAutoDrawable glDrawable) {
    glClear(GL_COLOR_BUFFER_BIT);

    glMatrixMode(GL_PROJECTION);
      glLoadIdentity();
      glOrtho(zoom,-zoom,zoom,-zoom,0.5f,-0.5f);

    glMatrixMode(GL_MODELVIEW);
      glLoadIdentity();

    glBindTexture(GL_TEXTURE_2D,noise[0]);
    glEnable(GL_TEXTURE_2D);

    glDisable(GL_DEPTH_TEST);

    glUseProgramObjectARB(_prog);

    glBegin(GL_QUADS);
      glTexCoord2f(0,0);
      glVertex2f(-1,-1);
      glTexCoord2f(1,0);
      glVertex2f(1,-1);
      glTexCoord2f(1,1);
      glVertex2f(1,1);
      glTexCoord2f(0,1);
      glVertex2f(-1,1);
    glEnd();

    glFinish();

    glUseProgram(0);

    _canvas.repaint();
  }

  public void reshape(
    GLAutoDrawable glDrawable, int x, int y, int width, int height)
  {
    if (width<height) _vpd = height;
    else _vpd = width;
    if (_vpd<VPD_MIN) _vpd = VPD_MIN;
    if (_vpd>VPD_MAX) _vpd = VPD_MAX;

    glViewport(0,0,_vpd,_vpd);

    glMatrixMode(GL_PROJECTION);
      glLoadIdentity();
    glMatrixMode(GL_MODELVIEW);
      glLoadIdentity();
  }

  public void displayChanged(
    GLAutoDrawable glDrawable, boolean modeChanged, boolean deviceChanged) {}


 // Key Events
  public void keyTyped(KeyEvent e) {
  }

  public void keyPressed(KeyEvent e) {
    if (e.getKeyCode()==KeyEvent.VK_ESCAPE) {
      _frame.dispose();
      System.exit(0);
    }
  }

  public void keyReleased(KeyEvent e) {
  }

  public void mouseWheelMoved(MouseWheelEvent e) {
    int nclicks = e.getWheelRotation();
    zoom += nclicks*0.1f;
    _canvas.repaint();
  }

  ///////////////////////////////////////////////////////////////////////////
  // private

  private String _tex;
  private ByteBuffer _tb;

  private GLCanvas _canvas = new GLCanvas();
  private Shader _vs;
  private Shader _fs;

  private float zoom = 1.0f;
  private int _prog;

  private int[] noise = new int[1];
  
  private int _vpd = VPD_DEFAULT;
  private JFrame _frame =
    new JFrame("Line Integral Convolution using Shaders");

  private JMenuBar setUpMenuBar() {
    JMenu fileMenu = new JMenu("File");
    Action loadAction = new AbstractAction("Exit") {
      public void actionPerformed(ActionEvent event) {
        _frame.dispose();
        System.exit(0);
      }
    };
    fileMenu.add(loadAction);
    JMenuBar menuBar = new JMenuBar();
    menuBar.add(fileMenu);
    return menuBar;
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

  private void generateRandomNoise() {
   glGenTextures(1,noise,0);
    glBindTexture(GL_TEXTURE_2D,noise[0]);
    int size = VPD_DEFAULT*VPD_DEFAULT;
    float[] arr = new float[3*size];
    Random r = new Random();
    for (int i=0; i<size; ++i)
      arr[i] = r.nextFloat();
    FloatBuffer texels = Direct.newFloatBuffer(arr);
    glTexImage2D(GL_TEXTURE_2D,0,1,VPD_DEFAULT,VPD_DEFAULT,
      0,GL_LUMINANCE,GL_FLOAT,texels);

  }

  private void loadTexture() {
    ImageData texture = new ImageData(_tex);
    int w = texture.nx;
    int h = texture.ny;
    _tb = texture.buf;
    
    glTexImage2D(GL_TEXTURE_2D,0,GL_RGB,
      w,h,0,GL_RGB,GL_UNSIGNED_BYTE,_tb);
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
