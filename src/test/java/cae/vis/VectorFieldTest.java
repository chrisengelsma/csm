package cae.vis;

import cae.io.ImageData;
import cae.vis.VectorField;

import java.util.Random;
import javax.swing.SwingUtilities;

/**
 * Tests {@link main.java.cae.vis.VectorField}
 * @author Chris Engelsma, Colorado School of Mines.
 * @version
 */
public class VectorFieldTest {

  public static String path = "/Users/Chris/Test/csci598/proj3/";
  public static String file = "circle.png";

  public static void main(String[] args) {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        go();
      }
    });
  }

  public static void go() {
    ImageData id = new ImageData();
    id.loadPNG(path+file);
    float[][] raw = makeRandomImage(id.nx,id.ny);
    ImageData id2 = new ImageData();
    id2.loadArray(raw,true);

    VectorField vf = new VectorField(id);

    System.out.println("Computing gradient field");
    vf.computeGradientField(1);

    System.out.println("Convolving vectors");
    vf.convolveVectors(id2,1,500);

    vf.plotResult(true);
  }

  private static float[][] makeRandomImage(int x, int y) {
    Random r = new Random();
    System.out.println("Building random image "+x+"x"+y);
    float[][] temp = new float[x][y];
    for (int i=0; i<x; ++i)
      for (int j=0; j<y; ++j)
        temp[i][j] = r.nextFloat();
    return temp;
  }
}
