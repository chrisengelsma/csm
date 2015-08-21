package cae.util;

/**
 * A 2D streamline.
 * @author Chris Engelsma, Colorado School of Mines.
 * @version 2010.02.23
 */
public class JStreamline {

  public int x0,y0;

  /**
   * Constructs a new streamline with given seed (x0,y0)
   * @param x0 the seed x-coordinate.
   * @param y0 the seed y-coordinate.
   */
  public JStreamline(int x0, int y0) {
    this.x0 = x0;
    this.y0 = y0;
    xsf = new double[nf];
    ysf = new double[nf];
    xsb = new double[nf];
    ysb = new double[nf];
  }

  /**
   * Appends the streamline with the coordinate (x,y).
   * @param x the next x-coordinate.
   * @param y the next y-coordinate.
   */
  public void append(double x, double y) {
    if (nf==xsf.length) {
      double[] xt = new double[xsf.length*2];
      double[] yt = new double[ysf.length*2];
      for (int i=0; i<xt.length; i++) {
        xt[i] = xsf[i];
        yt[i] = ysf[i];
      }
      xsf = xt;
      ysf = yt;
    }
    xsf[nf  ] = x;
    ysf[nf++] = y;
  }

  /**
   * Prepends the streamline with coordinate (x,y).
   * @param x the previous x-coordinate.
   * @param y the previous y-coordinate.
   */
  public void prepend(double x, double y) {
    if (nb==xsb.length) {
      double[] xt = new double[xsb.length*2];
      double[] yt = new double[ysb.length*2];
      for (int i=0; i<xt.length; i++) {
        xt[i] = xsb[i];
        yt[i] = ysb[i];
      }
      xsb = xt;
      ysb = xt;
    }
    xsb[nb  ] = x;
    ysb[nb++] = y;
  }

  /**
   * Orders the streamline in order.
   */
  public void collapse() {
    x = new double[xsf.length+xsb.length];
    y = new double[ysf.length+ysb.length];
    for (int i=0; i<xsb.length-1; ++i)
      x[i] = xsb[xsb.length-i-1];
    for (int i=0; i<xsf.length; ++i)
      x[i+xsb.length-1] = xsf[i];
    for (int i=0; i<ysb.length-1; ++i)
      y[i] = ysb[ysb.length-i-1];
    for (int i=0; i<ysf.length; ++i)
      y[i+ysb.length-1] = ysf[i];
  }

  /**
   * Prints the streamline coordinates.
   */
  public void print() {
    for (int i=0; i<x.length; ++i)
      System.out.println(x[i]+" "+y[i]);
  }

  ////////////////////////////////////////////////////////////////////////////
  // private

  private int sid;
  private double[] xsf,ysf;
  private double[] xsb,ysb;
  private double[] x,y;
  private int nf = 64;
  private int nb = 64;

}
