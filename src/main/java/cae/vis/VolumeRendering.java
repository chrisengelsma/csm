package cae.vis;

import cae.util.SceneData;

import java.io.*;
import java.util.ArrayList;
import java.util.Scanner;

import edu.mines.jtk.io.ArrayInputStream;
import static edu.mines.jtk.util.ArrayMath.*;

/**
 * Volume Rendering.
 * CSCI 598
 * Reads in a binary 3D volume of amplitudes and produces a visualization from
 * the scalar field.  The algorithm follows a method described by Levoy ['97].
 * Along with a dataset, a user-created file containing important parameters
 * is also read.
 * @author Chris Engelsma, Colorado School of Mines.
 * @version 2009.09.29
 */
public class VolumeRendering {

  public String _fileName;
  public String _parameters;
  public TransferFunction _tf;
  public int maxValue;

  /**
   * Contructs a new volume renderer with a given file name.
   * @param fileName the file.
   * @throws IOException
   */
  public VolumeRendering(String fileName) throws IOException {
    _fileName = fileName;
    _parameters = fileName+".txt";
    File data = new File(fileName);
    File param = new File(fileName+".txt");
    setScene(param);
    loadData(data);
    maxValue = (int)max(_xyz);
  }

  /**
   * Sets a given transfer function for the rendering algorithm.
   * @param tf the transfer function.
   */
  public void setTransferFunction(TransferFunction tf) {
    _tf = tf;
  }
  
  /**
   * Sets the resolution of the projection.
   * Note: The resolution is first set in the input file.
   * @param x the horizontal resolution.
   * @param y the vertical resolution.
   */
  public void setResolution(int x, int y) {
    xResolution = x;
    yResolution = y;
  }

  /**
   * Sets the array of isovalues to be searched.
   * Note: The isovalues are first set in the input file.
   * @param ni the array of isovalues
   */
  public void setIsovalues(double[] ni) {
    isovalues = ni;
  }

  /**
   * Displays the transfer function.
   */
  public void displayTransferFunction() {
    if (_tf!=null) 
      _tf.show();
  }

  /**
   * Performs Levoy's rendering algorithm with a defualt output name being
   * the input name + .ppm.
   * @throws IOException
   */
  public void render() throws IOException {
    render(_fileName+".ppm");
  }

  /**
   * Performs Levoy's rendering algorithm with a given output name.
   * @param output the output file name ending in .ppm
   * @throws IOException
   */
  public void render(String output) throws IOException {
    if (_tf==null) {
      setTransferFunction(new TransferFunction(10,10,10,maxValue,10));
      _tf.allWhite();
      System.out.println("Warning: Transfer function hasn't been set");
      System.out.println("Using default values");
    }

    // Currently only supports isotropic data set.
    double[] cubex = new double[]{n1,0,0};
    double[] cubey = new double[]{0,n2,0};
    double[] cubez = new double[]{0,0,n3};
    double[] origin = new double[]{0,0,0};
    _ds = new SceneData(_xyz,cubex,cubey,cubez,origin);

    Image image = new Image(xResolution,yResolution);
    int maxVal = (int)max(_xyz);

    // Load isovalues
    isovalues = _scene.get(6);

    // Sampling approximations
    double dg = (_scene.get(7))[0];     // gradient approximation
    double delta = (_scene.get(7))[1];  // ray sampling

    // Phong Model parameters
    double I,Ii,t;
    double[] b = _scene.get(4); // Light source
    double[] L = new double[3]; // Shadow ray
    double[] H = new double[3]; // Halfway vector
    double[] V = new double[3]; // Viewpoint vector
    double[] N = new double[3]; // Normal vector (gradient)
    double[] Iout = new double[3];

    // Light coefficients
    double[] par = _scene.get(5);
    double li = par[0]; double Ia = par[1]; double ka = par[2];
    double kd = par[3]; double ks = par[4]; double m = par[5];
    
    // Scene parameters
    double[] e = _scene.get(0);
    double[] ll = _scene.get(1);
    double[] v = _scene.get(2);
    double[] h = _scene.get(3);

    double alpha;
    double[] grad;

    int count = xResolution*yResolution;
    System.out.println();
    System.out.println("Rendering at: "+xResolution+"x"+yResolution);
    System.out.println(count+" pixels");

    double c=0;

    for (int x=0; x<xResolution; ++x) {
      for (int y=0; y<yResolution; ++y) {

        double[] pixel = new double[] {
          ll[0]+(x+0.5)/xResolution*h[0]+(y+0.5)/yResolution*v[0],
          ll[1]+(x+0.5)/xResolution*h[1]+(y+0.5)/yResolution*v[1],
          ll[2]+(x+0.5)/xResolution*h[2]+(y+0.5)/yResolution*v[2]
        };

        double[] o = new double[]{pixel[0],pixel[1],pixel[2]};
        double[] d = unit(new double[]{
          pixel[0]-e[0],
          pixel[1]-e[1],
          pixel[2]-e[2]
        });

        Iout[0] = 0; Iout[1] = 0; Iout[2] = 0;
        Ii = 0.0;
        I = 0.0;
        t = 1.0;
        for (int n=0; n<4000; n++) {
          double dn = delta*n;
          double[] p = new double[]{o[0]+dn*d[0],o[1]+dn*d[1],o[2]+dn*d[2]};
          if (_ds.isInData(p)) {
            double value = _ds.tI(p[0],p[1],p[2]);
            double R = _tf.returnRed(value);
            double G = _tf.returnGreen(value);
            double B = _tf.returnBlue(value);
            grad = _ds.grad(p[0],p[1],p[2],dg);
            alpha = getAlpha(value,grad);
            L = unit(new double[] {b[0]-p[0],b[1]-p[1],b[2]-p[2]});
            V = unit(new double[] {e[0]-p[0],e[1]-p[1],e[2]-p[2],});
            H = unit(new double[] {L[0]+V[0],L[1]+V[1],L[2]+V[2]});
            if (magnitude(grad)!=0) {
              N = unit(grad);
//              Ii = alpha*abs(dot(N,L)); // For a simple illumination
              Ii = alpha*(Ia*ka+li*(kd*abs(dot(N,L))+ks*(pow(dot(H,N),m))));
              Iout[0] += t*Ii*R;
              Iout[1] += t*Ii*G;
              Iout[2] += t*Ii*B;
              t = t*(1-alpha);
              if (t<1.0e-3) // Breaking point avoids unnecessary calculations
                break;
            }
          }
        }
        RGB pix = image.pixel(y,xResolution-x-1);
        pix.r = Iout[0];
        pix.g = Iout[1];
        pix.b = Iout[2];
        System.out.print("\r"+(int)((c++/count)*100)+" %");
      }
    }
    image.saveToPPMFile(output);
    System.out.println();
  }

  ////////////////////////////////////////////////////////////////////////////
  // private

  /**
   * Internal class that handles RGB values for the image.
   */
  private class RGB {
    double r,g,b;
  }

  /**
   * Internal class that handles image properties and functions.
   */
  private class Image {
    int xSize, ySize;
    RGB[] rgb;

    /**
     * Constructs a new Image with given dimensions.
     * @param m the horizontal dimension.
     * @param n the vertical dimension.
     */
    public Image(int m, int n) {
      this.xSize = m;
      this.ySize = n;
      rgb = new RGB[m*n];
      for (int i=0; i<rgb.length; ++i)
        rgb[i] = new RGB();
    }

    /**
     * Returns the RGB value at a specified pixel value (x,y).
     * @param x the x pixel value.
     * @param y the y pixel value.
     * @return the RGB at pixel value (x,y).
     */
    public RGB pixel(int x, int y) {
      return rgb[x+xSize*y];
    }

    /**
     * Saves the image a binary PPM file.
     * @param fileName the name of the outputted image.
     * @throws IOException
     */
    public void saveToPPMFile(String fileName) throws IOException {
      FileOutputStream fos =  new FileOutputStream(fileName);
      fos.write(new String("P6\n").getBytes());
      fos.write(new String(xSize+" "+ySize+"\n").getBytes());
      fos.write(new String("255\n").getBytes());

      byte[] ppm = new byte[xSize*3];
      for (int i=0; i<ySize*xSize; ++i) {
        byte r = clipNRound(256*rgb[i].r);
        byte g = clipNRound(256*rgb[i].g);
        byte b = clipNRound(256*rgb[i].b);
        fos.write(r);
        fos.write(g);
        fos.write(b);
      }
      fos.close();
    }

    /**
     * Clips the value and rounds to fit with a 0-255 range.
     * @param x the value to clip.
     * @return a clipped and rounded value.
     */
    private byte clipNRound(double x) {
      if (x>255)
        x = 255;
      if (x<0)
        x = 0;
      return (byte)floor(x+0.5);
    }
  }

  private ArrayList<double[]> _scene = new ArrayList<double[]>();
  private double[][][] _xyz;

  private int n1; // X
  private int n2; // Y
  private int n3; // Z

  private int xResolution;
  private int yResolution;

  private double[] isovalues;

  private double iR = 1.0;
  private double a0 = 1.0;
  private SceneData _ds;

  /**
   * Calculates the alpha given the distance away from the isocontour line.
   * The radius is calculated for each isovalue, so the alpha value takes
   * into consideration each isovalue being searched for in the algorithm.
   * @param f the function value.
   * @param gf the gradient at that point.
   * @return the alpha value
   */
  private double getAlpha(double f, double[] gf) {
    double alpha = 1.0;
    double magGrad = magnitude(gf);
    for (int n=0; n<isovalues.length; ++n) {
      if (magGrad>0 &&
            (isovalues[n]-iR*magGrad)<=f &&
            (isovalues[n]+iR*magGrad)>=f) {
        double proxy = abs((f-isovalues[n])/magGrad);
        alpha *= (1-a0*(1-(1/iR)*proxy));
      } else if (magGrad==0 && f==isovalues[n]) {
        alpha *= (1-a0);
      }
    }
    double aTotal = 1-alpha;
    return aTotal;
  }

  /**
   * Loads scalar field data from a given file.
   * @param data the file.
   * @throws IOException
   */
  private void loadData(File data) throws IOException {
    _xyz = new double[n1][n2][n3];
    byte[][][] xyzbytes = new byte[n1][n2][n3];
    ArrayInputStream ais = new ArrayInputStream(data);
    ais.readBytes(xyzbytes);
    ais.close();
    for (int i1=0; i1<_xyz.length; ++i1)
      for (int i2=0; i2<_xyz[0].length; ++i2)
        for (int i3=0; i3<_xyz[0][0].length; ++i3)
          _xyz[i1][i2][i3] = (double)xyzbytes[i1][i2][i3];
  }

  /**
   * Sets up important scene information related to the scene.
   * Information includes coordinates of the viewpoint, lower left corner
   * of the screen and the light source.  Directional vectors for the screen,
   * as well as light intensity is also initialized.
   */
  private void setScene(File file) throws IOException {
    double[] e = new double[3];
    double[] c = new double[3];
    double[] h = new double[3];
    double[] v = new double[3];
    double[] b = new double[3];
    double[] p = new double[6];
    double li,Ia,ka,kd,ks,m;
    Scanner s = new Scanner(new FileInputStream(file));
    n1 = s.nextInt();
    n2 = s.nextInt();
    n3 = s.nextInt();
    xResolution = s.nextInt();
    yResolution = s.nextInt();
    e[0] = s.nextDouble(); e[1] = s.nextDouble(); e[2] = s.nextDouble();
    c[0] = s.nextDouble(); c[1] = s.nextDouble(); c[2] = s.nextDouble();
    h[0] = s.nextDouble(); h[1] = s.nextDouble(); h[2] = s.nextDouble();
    v[0] = s.nextDouble(); v[1] = s.nextDouble(); v[2] = s.nextDouble();
    b[0] = s.nextDouble(); b[1] = s.nextDouble(); b[2] = s.nextDouble();
    li = s.nextDouble();
    Ia = s.nextDouble();
    ka = s.nextDouble();
    kd = s.nextDouble();
    ks = s.nextDouble();
    m = s.nextDouble();
    p[0] = li; p[1] = Ia; p[2] = ka;
    p[3] = kd; p[4] = ks; p[5] = m;
    int nc = s.nextInt();
    double[] con = new double[nc];
    for (int n=0; n<nc; ++n)
      con[n] = s.nextDouble();
    double dg = s.nextDouble();
    double dr = s.nextDouble();
    s.close();
    _scene.add(e);
    _scene.add(c);
    _scene.add(v);
    _scene.add(h);
    _scene.add(b);
    _scene.add(p);
    _scene.add(con);
    _scene.add(new double[]{dg,dr});
    System.out.println(_fileName);
    System.out.println(n1*n2*n3+" bytes");
    System.out.println();
  }

  /**
   * Calculates the magnitude of a vector V.
   * @param V a three-component vector.
   * @return the magnitude of V.
   */
  private double magnitude(double[] V) {
    return (sqrt(V[0]*V[0]+V[1]*V[1]+V[2]*V[2]));
  }

  /**
   * Returns the unit vector of vector V.
   * @param V a three-component vector.
   * @return the unit vector V.
   */
  private double[] unit(double[] V) {
    double mag = magnitude(V);
    V[0] /= mag; V[1] /= mag; V[2] /= mag;
    return (V);
  }

  /**
   * Returns the dot product between two vectors (v1*v2).
   * @param v1 a three-component vector.
   * @param v2 a three-component vector.
   * @return the dot product.
   */
  private double dot(double[] v1, double[] v2) {
    return (v1[0]*v2[0]+v1[1]*v2[2]+v1[2]*v2[2]);
  }
}
