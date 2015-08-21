package cae.vis;

import edu.mines.jtk.mosaic.*;
import static edu.mines.jtk.util.ArrayMath.*;

/**
 * Creates and maintains the volume rendering transfer function.
 * A transfer function is a linear operation to transform unsigned integer
 * or byte values to RGB values.
 * @author Chris Engelsma, Colorado School of Mines.
 * @version 2009.09.29
 */
public class TransferFunction {
  public double[] _r;
  public double[] _g;
  public double[] _b;
  public double r,g,b;
  public double _w;
  public int _len;
  public double[] _function;

  /**
   * Constructs a new transfer function with given parameters.
   * @param r the red max value.
   * @param g the green max value.
   * @param b the blue max value.
   * @param max the max value of the dataset.
   * @param w the width of the max values.
   */
  public TransferFunction(
    int r, int g, int b, int max, int w) {
    _len = max;
    this.r = r;
    this.g = g;
    this.b = b;
    _w = w;
    _r = new double[_len];
    _g = new double[_len];
    _b = new double[_len];

    if (r+w<_len && r>=0 && r-w>=0)
      for (int i=-w; i<=w; ++i)
        _r[r+i] = 1;
    if (g+w<_len && g>=0 && g-w>=0)
      for (int i=-w; i<=w; ++i)
        _g[g+i] = 1;
    if (b+w<_len && b>=0 && b-w>=0)
      for (int i=-w; i<=w; ++i)
        _b[b+i] = 1;
  }

  /**
   * A double-sided exponential smoothing function.
   * This smooths the transfer function with the intent of limited artifacts
   * in the color due to discontinuities.
   * @param m the smoothing factor within the range [0,1].
   */
  public void smoothFilter(double m) {
    double[] y1r = new double[_len];
    double[] y2r = new double[_len];
    double[] y1g = new double[_len];
    double[] y2g = new double[_len];
    double[] y1b = new double[_len];
    double[] y2b = new double[_len];

    y1r[0] = _r[0];
    y1g[0] = _g[0];
    y1b[0] = _b[0];

    y2r[_len-1] = _r[_len-1];
    y2g[_len-1] = _g[_len-1];
    y2b[_len-1] = _b[_len-1];

    for (int n=1; n<_len; ++n) {
      y1r[n] = _r[n]+m*y1r[n-1];
      y1g[n] = _g[n]+m*y1g[n-1];
      y1b[n] = _b[n]+m*y1b[n-1];
    }

    for (int n=_len-2; n>=0; --n) {
      y2r[n] = m*_r[n+1]+m*y2r[n+1];
      y2g[n] = m*_g[n+1]+m*y2g[n+1];
      y2b[n] = m*_b[n+1]+m*y2b[n+1];
    }

    for (int n=0; n<_len; ++n) {
      _r[n] = y1r[n]+y2r[n];
      _g[n] = y1g[n]+y2g[n];
      _b[n] = y1b[n]+y2b[n];
    }
    normalize();
  }

  /**
   * Ramps the red and blue components with a given slope.
   * Every value to the left of the red sloping point is set to 1, likewise
   * every value to the right of the blue sloping point is set to 1.
   * @param slope a slope.
   */
  public void rampRB(double slope) {
    for (int n=0; n<_len; ++n) {
      _r[n] = 1;
      _b[n] = 1;
      if (n>(r+_w)) 
        _r[n] = (_r[n-1]-slope>=0)?_r[n-1]-slope:0;
      if ((_len-n-1)<(b-_w))
        _b[_len-n-1] = (_b[_len-n]-slope>=0)?_b[_len-n]-slope:0;
    }
  }
  
  /**
   * Scales the data by a specified value.
   * @param a a scaling value.
   */
  public void scaleValues(double a) {
    for (int n=0; n<_len; ++n) {
      _r[n] *= a;
      _b[n] *= a;
      _g[n] *= a;
    }
  }

  /**
   * Sets all values to 1 on all curves.
   */
  public void allWhite() {
    for (int n=0; n<_len; ++n)
      _r[n] = _g[n] = _b[n] = 1;
  }

  /**
   * Normalizes the function to 1 being the maximum value.
   */
  public void normalize() {
    double mr = max(_r);
    double mg = max(_g);
    double mb = max(_b);
    for (int i=0; i<_len; ++i) {
      if (mr!=0)
        _r[i] /= mr;
      if (mg!=0)
        _g[i] /= mg;
      if (mb!=0)
        _b[i] /= mb;
    }
  }

  /**
   * Gets the array of red values.
   * @return the array of red values.
   */
  public double[] getRed() {
    return _r;
  }

  /**
   * Gets the array of green values.
   * @return the array of green values.
   */
  public double[] getGreen() {
    return _g;
  }

  /**
   * Gets the array of blue values.
   * @return the array of blue values.
   */
  public double[] getBlue() {
    return _b;
  }

  /**
   * Returns a specified red value at a given point through linear
   * interpolation.
   * @param i the index.
   * @return the interpolated red value.
   */
  public double returnRed(double i) {
    return getSpecificValue(_r,i);
  }

  /**
   * Returns a specified green value at a given point through linear
   * interpolation.
   * @param i the index.
   * @return the interpolated green value.
   */
  public double returnGreen(double i) {
    return getSpecificValue(_g,i);
  }

  /**
   * Returns a specified blue value at a given point through linear
   * interpolation.
   * @param i the index.
   * @return the interpolated blue value.
   */
  public double returnBlue(double i) {
    return getSpecificValue(_b,i);
  }

  /**
   * Displays the transfer function in a PlotFrame.
   */
  public void show() {
    float[] r = new float[_len];
    float[] g = new float[_len];
    float[] b = new float[_len];
    for (int i=0; i<_len; ++i) {
      r[i] = (float)_r[i];
      g[i] = (float)_g[i];
      b[i] = (float)_b[i];
    }
    PlotPanel panel = new PlotPanel();
    PointsView rp = panel.addPoints(r);
    PointsView gp = panel.addPoints(g);
    PointsView bp = panel.addPoints(b);
    rp.setStyle("r-");
    gp.setStyle("g-");
    bp.setStyle("b-");
    panel.setTitle("Transfer Function");
    panel.setHLabel("Value");
    panel.setVLabel("Amplitude");
    PlotFrame frame = new PlotFrame(panel);
    frame.setDefaultCloseOperation(PlotFrame.DISPOSE_ON_CLOSE);
    frame.setVisible(true);
  }

  ////////////////////////////////////////////////////////////////////////////
  // private

  /**
   * Interpolates the function to get the specified alpha value.
   * @param a the color curve.
   * @param v the index.
   * @return the interpolated alpha value.
   */
  private double getSpecificValue(double[] a, double v) {
    int vf = (int)v;
    int vc = vf+1;
    double vd = v-vf;
    if (vf<0 || vc<0 || vf>=_len || vc>=_len)
      return 0;
    double iv = a[vf]*(1-vd)+a[vc]*vd;
    return iv;
  }
}
