package cae.util;

/**
 * Handles the calculations needed for a given 3D data set.
 * Currently this only supports an isotropic scalar field.
 * @author Chris Engelsma, Colorado School of Mines.
 * @version 2009.09.29
 */
public class SceneData {

  public static double[][][] _data;
  public static double[] _x, _y, _z;
  public static double[] _origin;

  /**
   * Constructs a new data scene with given properties.
   * @param data a 3D array of doubles.
   * @param x a 3D vector lining the x-dimension of the data.
   * @param y a 3D vector lining the y-dimension of the data.
   * @param z a 3D vector lining the z-dimension of the data.
   * @param origin the origin of the data.
   */
  public SceneData(
    double[][][] data, double[] x, double[] y, double[] z, double[] origin)
  {
    _data = data;
    _origin = origin;
    _x = x;
    _z = z;
    _y = y;
  }

  /**
   * Determines whether a given data point lies within a 3D data set.
   * @param p a 3D point.
   * @return true, if inside; false, otherwise.
   */
  public boolean isInData(double[] p) {
    double[] max = new double[3];
    for (int i=0; i<3; ++i) 
      max[i] = _origin[i]+_x[i]+_y[i]+_z[i];
        
    return (p[0]>_origin[0] && p[0]<max[0]-2 &&
            p[1]>_origin[1] && p[1]<max[1]-2 &&
            p[2]>_origin[2] && p[2]<max[2]-2);
  }

  /**
   * Calculates the gradient vector for a given point in the dataset.
   * @param xt x value.
   * @param yt y value.
   * @param zt z value.
   * @param h the gradient width.
   * @return the gradient.
   */
  public double[] grad(double xt, double yt, double zt, double h) {
    double[] v = new double[3];
    double gz1 = tI(xt,yt,zt-h);
    double gz2 = tI(xt,yt,zt+h);
    double gy1 = tI(xt,yt-h,zt);
    double gy2 = tI(xt,yt+h,zt);
    double gx1 = tI(xt-h,yt,zt);
    double gx2 = tI(xt+h,yt,zt);
    v[0] = (gx1-gx2)/(2.0*h);
    v[1] = (gy1-gy2)/(2.0*h);
    v[2] = (gz1-gz2)/(2.0*h);
    return v;
  }

  /**
   * Trilinearly interpolates a data point.
   * @param xt x value.
   * @param yt y value.
   * @param zt z value.
   * @return interpolated point.
   */
  public double tI(double xt, double yt, double zt) {
    int xf = (int)xt;
    int yf = (int)yt;
    int zf = (int)zt;
    int xc = xf+1;
    int yc = yf+1;
    int zc = zf+1;
    double xd = xt-xf;
    double yd = yt-yf;
    double zd = zt-zf;
    double i1 = _data[xf][yf][zf]*(1-zd)+_data[xf][yf][zc]*zd;
    double i2 = _data[xf][yc][zf]*(1-zd)+_data[xf][yc][zc]*zd;
    double j1 = _data[xc][yf][zf]*(1-zd)+_data[xc][yf][zc]*zd;
    double j2 = _data[xc][yc][zf]*(1-zd)+_data[xc][yc][zc]*zd;
    double w1 = i1*(1-yd)+i2*yd;
    double w2 = j1*(1-yd)+j2*yd;
    double v = w1*(1-xd)+w2*xd;
    return v;
  }
}
