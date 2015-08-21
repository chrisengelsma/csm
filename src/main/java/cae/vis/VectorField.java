package cae.vis;

import cae.io.ImageData;
import cae.util.*;

import edu.mines.jtk.mosaic.*;

import java.awt.Dimension;
import static java.lang.Math.*;

/**
 * Visualizes vector fields by implementing line integral convolution.
 * @author Chris Engelsma, Colorado School of Mines.
 * @version 2009.11.16
 */
public class VectorField {

  /**
   * Constructs a new vector field with a given image data object.
   * @param id an image data object.
   */
  public VectorField(ImageData id) {
    oi = id.image;
    width = oi.length;
    height = oi[0].length;
    g = new Vector2[width][height];
    ci = new float[width][height];
  }

  /**
   * Computes the gradient field.
   * Note, user must choose between a second or fourth-order derivative
   * approximation.
   * @param s the type of derivative approximation: 0 for 2nd-order, 1 for
   * 4th-order.
   */
  public void computeGradientField(int s) {
    if (s==0) {
        computeSecondOrderGradient();
        normalizeVectors();
        rotateVectors();
    } else if (s==1) {
        computeFourthOrderGradient();
        normalizeVectors();
        rotateVectors();
    }
  }

  /**
   * Convolves the vectors with a given image data object.
   * @param im an image to convolve.
   */
  public void convolveVectors(ImageData im, int step, int ll) {
    this.im = im;
    if (im.rgb!=null)
      rgb = new float[3][width][height];
    ri = im.image;
    for (int i=0; i<width; ++i) {
      for (int j=0; j<height; ++j) {
        stepThroughLine(i,j,i,j,step,true,ll);
        stepThroughLine(i,j,i,j,step,false,ll);
        ci[i][j] /= (2*step);
        if (rgb!=null) {
          rgb[0][i][j] /= (ll);
          rgb[1][i][j] /= (ll);
          rgb[2][i][j] /= (ll);
        }
      }
    }
    fillBorder();
  }

  /**
   * Plots the result in a plot frame.
   * @param combined true, if combining to one set; false, otherwise.
   */
  public void plotResult(boolean combined) {
    if (ci!=null) {
      float[][] cif = new float[width][height];
      float[][] oif = new float[width][height];
      float[][] rif = new float[width][height];

      for (int i=0; i<width; ++i) {
        for (int j=0; j<height; ++j) {
          cif[i][j] = ci[i][j];
          rif[i][j] = ri[i][j];
          oif[i][j] = oi[i][j];
        }
      }
      if (combined) {
        PlotPanel pp = new PlotPanel(1,3,PlotPanel.Orientation.X1DOWN_X2RIGHT);
        PixelsView pv1 = pp.addPixels(0,0,rif);
        PixelsView pv2 = pp.addPixels(0,1,oif);
        PixelsView pv3 = pp.addPixels(0,2,cif);
        PlotFrame pf = new PlotFrame(pp);
        pf.setDefaultCloseOperation(PlotFrame.EXIT_ON_CLOSE);
        pf.setVisible(true);
      } else {
        SimplePlot sp0 = new SimplePlot();
        SimplePlot sp1 = new SimplePlot();
        SimplePlot sp2 = new SimplePlot();

        sp0.addPixels(rif);
        sp0.setSize(new Dimension(600,600));
        sp0.addTitle("Input Texture");
        sp0.setHLabel("X-Index");
        sp0.setVLabel("Y-Index");

        sp1.addPixels(cif);
        sp1.setSize(new Dimension(600,600));
        sp1.addTitle("Convolved Image");
        sp1.setHLabel("X-Index");
        sp1.setVLabel("Y-Index");

        sp2.addPixels(oif);
        sp2.setSize(new Dimension(600,600));
        sp2.addTitle("Gradient Field");
        sp2.setHLabel("X-Index");
        sp2.setVLabel("Y-Index");
      }
    }
  }

  /**
   * Plots a composite color image.
   */
  public void plotColor() {
    if (rgb!=null) {
      PlotPanel pp = new PlotPanel(PlotPanel.Orientation.X1DOWN_X2RIGHT);
      pp.setSize(new Dimension(600,600));
      pp.setTitle("\"Painted\" Image");
      pp.setVLabel("Y-Index");
      pp.setHLabel("X-Index");
      PixelsView pv = pp.addPixels(rgb);
      PlotFrame pf = new PlotFrame(pp);
      pf.setDefaultCloseOperation(PlotFrame.EXIT_ON_CLOSE);
      pf.setVisible(true);
    }
  }

  ////////////////////////////////////////////////////////////////////////////
  // private

  private Vector2[][] g; // The gradient field.
  private float[][] ri,oi,ci; // Random, original and convolved image.
  private float[][][] rgb; // for RGB
  private ImageData im;
  private int width,height; // Width, height of image.

  /**
   * Computes the streamlines using Euler's method.
   * @param xn estimated x-index.
   * @param yn estimated y-index.
   * @param h streamline step.
   * @param forward true, if marching forward; false, otherwise.
   */
  private void stepThroughLine(
    double xn, double yn, int x0, int y0, int h, boolean forward, int step)
  {
    if (step>0 && xn>=0 && xn<width-1 && yn>=0 && yn<height-1) {
      double xn2,yn2; // Walking index.
      int xi,yi; // Walking index - integer nearest.
      Vector2 biv = biVector(xn,yn); // Interpolate the vector.
      if (forward) {
        xn2 = xn+h*biv.x; // Walk forward if marching forward
        yn2 = yn+h*biv.y;
      } else {
        xn2 = xn-h*biv.x; // Walk backward if marching backward
        yn2 = yn-h*biv.y;
      }
      xi = (int)floor(xn2+0.5); // Find nearest x-index.
      yi = (int)floor(yn2+0.5); // Find nearest y-index.
      if (isInData(xi,yi)) { // If we're in bounds.
        ci[x0][y0] +=
          (ri[xi-1][yi]+ri[xi][yi-1]+ri[xi+1][yi]+ri[xi][yi+1])/4;
        if (rgb!=null) {
          rgb[0][x0][y0] +=
            (im.rgb[0][xi-1][yi]+
             im.rgb[0][xi][yi-1]+
             im.rgb[0][xi+1][yi]+
             im.rgb[0][xi][yi+1])/4;
          rgb[1][x0][y0] +=
            (im.rgb[1][xi-1][yi]+
             im.rgb[1][xi][yi-1]+
             im.rgb[1][xi+1][yi]+
             im.rgb[1][xi][yi+1])/4;
          rgb[2][x0][y0] +=
            (im.rgb[2][xi-1][yi]+
             im.rgb[2][xi][yi-1]+
             im.rgb[2][xi+1][yi]+
             im.rgb[2][xi][yi+1])/4;
        }

        // Recursively continue.
        if (forward) stepThroughLine(xn2,yn2,x0,y0,h,true,step-1);
                else stepThroughLine(xn2,yn2,x0,y0,h,false,step-1);
      }
    }
  }

  /**
   * Fills the borders with the mean value.
   */
  private void fillBorder() {
    float average = 0;
    float realcount = 0;
    for (int i=0; i<width; ++i) {
      for (int j=0; j<height; ++j) {
        average+=ci[i][j];
        realcount+=(ci[i][j]==0)?0:1;
      }
    }
    average/=realcount;
    for (int i=0; i<width; ++i) {
      ci[i][0] = average;
      ci[i][1] = average;
      ci[i][height-1] = average;
      ci[i][height-2] = average;
    }
    for (int j=0; j<height; ++j) {
      ci[0][j] = average;
      ci[1][j] = average;
      ci[width-1][j] = average;
      ci[width-2][j] = average;
    }
  }

  /**
   * Uses bilinear interpolation to determine the vector at given decimated
   * indices.
   * @param x the x-index.
   * @param y the y-index.
   * @return the interpolated vector at (x,y).
   */
  private Vector2 biVector(double x, double y) {
    double i1,i2,jx,jy;
    int xf = (int)floor(x);
    int yf = (int)floor(y);
    int xc = xf+1;
    int yc = yf+1;
    // X
    i1 = (xc-x)/(xc-xf)*g[xf][yf].x + (x-xf)/(xc-xf)*g[xc][yf].x;
    i2 = (xc-x)/(xc-xf)*g[xf][yc].x + (x-xf)/(xc-xf)*g[xc][yc].x;
    jx = (yc-y)/(yc-yf)*i1 + (y-yf)/(yc-yf)*i2;
    // Y
    i1 = (xc-x)/(xc-xf)*g[xf][yf].y + (x-xf)/(xc-xf)*g[xc][yf].y;
    i2 = (xc-x)/(xc-xf)*g[xf][yc].y + (x-xf)/(xc-xf)*g[xc][yc].y;
    jy = (yc-y)/(yc-yf)*i1 + (y-yf)/(yc-yf)*i2;
    return new Vector2(jx,jy);
  }

  /**
   * Computes a second-order central-difference operation.
   */
  private void computeSecondOrderGradient() {
    double x,y;
    for (int i=0; i<g.length; ++i) {
      for (int j=0; j<g[0].length; ++j) {
        x = 0; y = 0;
        if (i>0 && j>0 && i<g.length-1 && j<g[0].length-1) {
          x = (-1*oi[i-1][j  ]+1*oi[i+1][j  ])/2;
          y = (-1*oi[i  ][j-1]+1*oi[i  ][j+1])/2;
        }
        g[i][j] = new Vector2(x,y);
      }
    }
  }
  
  /**
   * Computes a fourth-order central-difference operation.
   */
  private void computeFourthOrderGradient() {
    double x,y;
    for (int i=0; i<g.length; ++i) {
      for (int j=0; j<g[0].length; ++j) {
        x = 0; y = 0;
        if (i>1 && j>1 && i<g.length-2 && j<g[0].length-2) {
          x = (-1*oi[i-2][j  ]+
                  8*oi[i-1][j  ]-
                  8*oi[i+1][j  ]+
                  1*oi[i+2][j  ])/12;
          y = (-1*oi[i  ][j-2]+
                  8*oi[i  ][j-1]-
                  8*oi[i  ][j+1]+
                  1*oi[i  ][j+2])/12;
        }
        g[i][j] = new Vector2(x,y);
      }
    }
  }

  /**
   * Normalizes the loaded vectors.
   */
  private void normalizeVectors() {
    double mag = 0;
    double max = 0;
    Vector2 v = new Vector2();
    for (int i=0; i<g.length; ++i) {
      for (int j=0; j<g[0].length; ++j) {
        v = g[i][j];
        mag = v.magnitude();
        max = (max>mag)?max:mag;
      }
    }
    for (int i=0; i<g.length; ++i) {
      for (int j=0; j<g[0].length; ++j) {
        g[i][j].x/=max;
        g[i][j].y/=max;
      }
    }
  }

  /**
   * Rotates the vectors by 90 degrees so that it will follow the image
   * trends as opposed to against it.
   */
  private void rotateVectors() {
    double temp;
    for (int i=0; i<g.length; ++i) {
      for (int j=0; j<g[0].length; ++j) {
        temp = g[i][j].x;
        g[i][j].x = -g[i][j].y;
        g[i][j].y = temp;
      }
    }
  }

  /**
   * Determines if given indices are within the loaded data set.
   * @param x the x index.
   * @param y the y index.
   * @return true, if within the data set; false, otherwise.
   */
  private boolean isInData(double x, double y) {
    return (x>0 && y>0 && x<width-1 && y<height-1);
  }
}
