/****************************************************************************
Copyright (c) 2010, Colorado School of Mines and others. All rights reserved.
This program and accompanying materials are made available under the terms of
the Common Public License - v1.0, which accompanies this distribution, and is
available at http://www.eclipse.org/legal/cpl-v10.html
****************************************************************************/
package cae.vis;

import cae.util.Mat4x4;
import cae.util.Vector3;

import java.awt.event.*;

import static edu.mines.jtk.ogl.Gl.*;
import static edu.mines.jtk.util.ArrayMath.*;

/**
 * @author Chris Engelsma, Colorado School of Mines
 * @version 2010.03.14
 */
public class TrackBall extends MouseAdapter implements KeyListener {

  public TrackBall(ModelCanvas canvas) {
    _canvas = canvas;
    _canvas.addMouseListener(this);
    _canvas.addMouseMotionListener(this);
    _canvas.addMouseWheelListener(this);
    _canvas.addKeyListener(this);
  }

  public void applyRotation() {
    glMatrixMode(GL_MODELVIEW);
      if (rotate) {
        angle1 += dangle1;
        angle2 += dangle2;
      }
      R = getRotation();
      T = getTranslation();
      glRotatef(angle1,1,2,3);
      glRotatef(angle2,-2,-1,0);
      glMultMatrixf(T.toArray(),0);
      glMultMatrixf(R.toArray(),0);
  }

  public void mouseClicked(MouseEvent e) {
  }

  public void mousePressed(MouseEvent e) {
    trackBallBelowCursor = lastPress = screenTo3D(e.getX(),e.getY());
    _canvas.repaint();
  }

  public void mouseReleased(MouseEvent e) {
    trackBallBelowCursor = screenTo3D(e.getX(),e.getY());
    finishedRotation = getRotation();
    finishedTranslation = getTranslation();
    lastPress = trackBallBelowCursor = new Vector3();
    _canvas.repaint();
  }

  public void mouseDragged(MouseEvent e) {
    trackBallBelowCursor = screenTo3D(e.getX(),e.getY());
    _canvas.repaint();
  }

  public void keyTyped(KeyEvent e) {
    if (e.getKeyChar()=='r') {
      if (rotate) rotate = false;
      else rotate = true;
    }
  }

  public void keyPressed(KeyEvent e) {
    if (e.getKeyCode()==e.VK_SHIFT)
      shifting = true;
  }

  public void keyReleased(KeyEvent e) {
    if (e.getKeyCode()==e.VK_SHIFT)
      shifting = false;
  }

  public void mouseWheelMoved(MouseWheelEvent e) {
    int nclicks = e.getWheelRotation();
    _canvas.zoom += nclicks*0.05;
  }

  ///////////////////////////////////////////////////////////////////////////
  // private

  private ModelCanvas _canvas;
  private Vector3 trackBallBelowCursor = new Vector3();
  private Vector3 lastPress = new Vector3();
  private Mat4x4 finishedRotation = new Mat4x4();
  private Mat4x4 finishedTranslation = new Mat4x4();
  private Mat4x4 R = new Mat4x4();
  private Mat4x4 T = new Mat4x4();

  private boolean rotate = false;
  private boolean shifting = false;
  
  private float dangle1 = 0.12f, dangle2 = 0.08f;
  private float angle1 = 0.0f, angle2 = 0.0f;

  private Mat4x4 getCurrentRotation() {
    if (!shifting) {
      if (trackBallBelowCursor.equals(lastPress)) {
        return new Mat4x4();
      } else {
        Vector3 axis = lastPress.cross(trackBallBelowCursor);
        axis.normalize();
        float ang =
          (float)(-acos(lastPress.dot(trackBallBelowCursor))*180.0f/PI);
        Mat4x4 mat = new Mat4x4();
        mat.setToRotate(ang,axis);
        return mat;
      }
    } else {
      return new Mat4x4();
    }
  }

  private Mat4x4 getCurrentTranslation() {
    if (shifting) {
      if (trackBallBelowCursor.equals(lastPress)) {
        return new Mat4x4();
      } else {
        Vector3 vector = new Vector3(
          lastPress.x-trackBallBelowCursor.x,
          lastPress.y-trackBallBelowCursor.y,
          lastPress.z-trackBallBelowCursor.z);
        Mat4x4 t = new Mat4x4();
        t.setToTranslate(vector);
        return t;
      }
    } else {
      return new Mat4x4();
    }
  }

  private Mat4x4 getRotation() {
    return finishedRotation.times(getCurrentRotation());
  }

  private Mat4x4 getTranslation() {
    return finishedTranslation.times(getCurrentTranslation());
  }

  public Vector3 screenTo3D(int x, int y) {
    float cx = _canvas.vpd-x;
    float cy = _canvas.vpd-y;
    float w = (float)_canvas.getWidth();
    float h = (float)_canvas.getHeight();
    float xx = 2.0f*cx/w-1.0f;
    float yy = 1.0f-2.0f*cy/h;
    float x2y2 = 1.0f-xx*xx-yy*yy;
    if (x2y2<0.0f) {
      float l = sqrt(xx*xx+yy*yy);
      xx /= l;
      yy /= l;
      x2y2 = 0;
    }
    float z = sqrt(x2y2);
    return new Vector3(xx,yy,z);
  }


}
