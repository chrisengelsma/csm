/****************************************************************************
Copyright (c) 2010, Colorado School of Mines and others. All rights reserved.
This program and accompanying materials are made available under the terms of
the Common Public License - v1.0, which accompanies this distribution, and is
available at http://www.eclipse.org/legal/cpl-v10.html
****************************************************************************/
package cae.io;

import edu.mines.jtk.io.ArrayInputStream;

import java.io.*;
import java.nio.ByteOrder;
import java.util.*;

/**
 * Reads in a nearly-raw raster data file (NRRD).
 * Details can be found at <em>http://teem.sourceforge.net/nrrd/</em>
 * @author Chris Engelsma, Colorado School of Mines
 * @version 2010.04.02
 */
public class NRRDFile {
  public NRRDFile() {
  }

  public NRRDFile(String nhdr) {
    try {
      File fnhdr = new File(nhdr);
      loadHeader(fnhdr);
    } catch (IOException ioe) {
      System.err.println(ioe);
    }
  }

  public float[] getFloats() {
    return raw;
  }

  public int[] getDimensions() {
    int size = Integer.parseInt(header.get("dimension1"));
    int[] dim = new int[size];
    for (int i=0; i<size; ++i) {
      String hsize = header.get("sizes"+(i+1));
      dim[i] = Integer.parseInt(hsize);
    }
    return dim;
  }

  public void loadHeader(File nhdr) throws IOException {
    String nhdrPath = nhdr.getPath();
    System.out.println("Loading nhdr: "+nhdrPath);
    if (!nhdrPath.toLowerCase().endsWith(".nhdr"))
      System.err.println("Possibly incorrect file: nhdr");
    header = getHeaderMap(nhdr);
    loadRaw(nhdrPath,header);
  }
  
  //////////////////////////////////////////////////////////////////////////
  // private

  private Map<String,String> header;
  private float[] raw;


  private void loadRaw(String filePath, Map<String,String> header)
    throws IOException
  {
    int size = 1;
    
    String rawFile = header.get("data file2");
    String endian = header.get("endian1");
    String type = header.get("type1");
    int dim = Integer.parseInt(header.get("dimension1"));

    for (int i=0; i<dim; i++)
      size *= Integer.parseInt(header.get("sizes"+(i+1)));

    ByteOrder bo = ByteOrder.BIG_ENDIAN;
    if (!endian.equals("big")) bo = ByteOrder.LITTLE_ENDIAN;

    String path = filePath.substring(0,filePath.lastIndexOf("/"));
    String file = rawFile.substring(rawFile.indexOf("/"));

    ArrayInputStream ais = new ArrayInputStream(path+file,bo);
    raw = new float[size];

    /* Right now, only floats and ints are supported. */
    if (type.equals("float")) {
      ais.readFloats(raw);
    } else if (type.equals("int")) {
      int[] vals = new int[size];
      ais.readInts(vals);
      for (int i=0; i<size; ++i)
        raw[i] = (float)vals[i];
    }
    ais.close();
  }
  
  private static Map<String,String> getHeaderMap(File header) {
    Map<String,String> parts = new HashMap<String,String>();
    try {
      BufferedReader br = new BufferedReader(new FileReader(header));
      String name = null;
      String line = null;
      while ((line=br.readLine())!=null) {
        if (!line.startsWith("#") && !line.startsWith("NRRD")) {
          name = line.substring(0,line.indexOf(":"));
          String[] splitLine = line.split(" ");
          for (int i=1; i<splitLine.length; ++i)
            if (splitLine[i]!="None")
              parts.put(name+i,splitLine[i]);
        }
      }
      br.close();
      return parts;
    } catch (IOException ioe) {
      throw new RuntimeException(ioe);
    }
  }
}
