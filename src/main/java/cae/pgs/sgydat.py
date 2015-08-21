"""
Converts SEG-Y format files to dat files (3D array of floats).

Author: Chris Engelsma, Colorado School of Mines
Version 2010.08.13
"""
from pgsutils import *

#############################################################################
def main(args):
  setGlobals()
  #readFormat()
  #testFormat()
  #convert()
  display()
  #displaySlice(100)

nhead=3200 # number of bytes in EBCDIC header
nbhed=400  # number of bytes in binary header
nthed=240  # number of bytes in trace header

def setGlobals():
  """
  Sets survey parameters and file location.
  """
  global n1,n2,n3,d1,d2,d3,f1,f2,f3
  global sgyFile,datFile
  sgyDir = "/Data/pgs/segy/"
  datDir = "/Data/pgs/dat/"
  rootFile = "29j99_segy_beammig_area_A_part"
  n1,d1,f1 = 1501,0.010,0.000
  n2,d2,f2 = 1740,0.025,0.000
  n3,d3,f3 =  484,0.040,0.000
  sgyFile = sgyDir+rootFile
  datFile = datDir+"pgsdat"

def convert():
  """
  Converts binary SEG-Y data to an array of floats.
  """
  aos = ArrayOutputStream(datFile+".dat")
  for i in range(2):
    print "converting",(n2*n3/2),"traces"
    ais = ArrayInputStream(sgyFile+str(i+1))
    ais.skipBytes(nhead+nbhed)
    x = zeroint(n1)
    y = zerofloat(n1)
    n = 0
    j2 = 0
    for i3 in range(n3/2):
      for i2 in range(j2,n2):
        ais.skipBytes(nthed)
        ais.readInts(x)
        IbmIeee.ibmToFloat(x,y)
        aos.writeFloats(y)
        n += 1
        if n%1000==0:
          print "converted",n,"traces"
    ais.close()
  aos.close()

def display():
  """
  Displays the data.
  """
  subn3 = n3/2
  ais = ArrayInputStream(datFile+".dat")
  x = zerofloat(n1,n2,subn3)
  ais.readFloats(x)
  ais.close()
  print "x min =",min(x)," max =",max(x)
  s1,s2,s3 = Sampling(n1,d1,f1),Sampling(n2,d2,f2),Sampling(subn3,d3,f3)
  ipg = ImagePanelGroup(s1,s2,s3,x)
  sf = SimpleFrame()
  ipg = sf.addImagePanels(s1,s2,s3,x)
  ipg.setClips(-2000,2000)

def displaySlice(i3):
  """
  Display a slice of the data.
  """
  ais = ArrayInputStream(datFile+str(2))
  x = zerofloat(n1,n2)
  ais.skipBytes(4*i3)
  ais.readFloats(x)
  ais.close()
  s1,s2 = Sampling(n1,d1,f1),Sampling(n2,d2,f2)
  sp = SimplePlot()
  pp = PlotPanel(PlotPanel.Orientation.X1DOWN_X2RIGHT)
  pp.setHLabel("Distance (km)")
  pp.setVLabel("Depth (km)")
  pp.addColorBar()
  pv = pp.addPixels(s1,s2,x)
  pv.setClips(-2000,2000)
  pf = PlotFrame(pp)
  pf.setDefaultCloseOperation(PlotFrame.EXIT_ON_CLOSE)
  pf.setVisible(True)

def readFormat():
  """
  Reads the format of the data.
  """
  ais = ArrayInputStream(sgyFile)
  ais.skipBytes(nhead)
  h = zeroshort(nbhed)
  ais.readShorts(h)
  ais.close()
  print "current sampling interval in usec =",h[8]
  print "original sampling interval in usec =",h[9]
  print "number of samples per trace =",h[10]
  print "original number of samples per trace =",h[8]
  format = h[12]
  if format==1:
    print "format = 1 = IBM floating point"
  elif format==5:
    print "format = 5 = IEEE floating point"
  else:
    print "format =",format,"is unknown!"

def testFormat():
  """
  Tests the format of the data.
  """
  xi = zeroint(n1)
  x1 = zerofloat(n1)
  x2 = zerofloat(n1)
  ais = ArrayInputStream(sgyFile)
  ais.skipBytes(nhead+nbhed)
  ais.skipBytes(100*n2*(nthed+4*n1))
  ais.skipBytes(n2/2*(nthed+4*n1))
  ais.skipBytes(nthed)
  ais.readInts(xi)
  ais.close()
  IbmIeee.ibmToFloat(xi,x1)
  IbmIeee.ieeeToFloat(xi,x2)
  sp = SimplePlot.asPoints(x1); sp.setTitle("Assuming IBM format")
  sp = SimplePlot.asPoints(x2); sp.setTitle("Assuming IEEE format")

#############################################################################

run(main)
