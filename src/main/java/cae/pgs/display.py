"""
Displays the PGS data.
Author: Chris Engelsma
Version: 2010.09.01
"""
from pgsutils import *

setupForSubset("subz_900_500_160")
s1,s2,s3 = getSamplings()
#s1 = Sampling(1501,0.010,0.000)
#s2 = Sampling(1740,0.025,0.000)
#s3 = Sampling( 484,0.040,0.000)

pgsdat = "pgssub"
pgsets = "pgsets"
sm1 = "pgss1"
sm2 = "pgss2"
sm3 = "pgss3"
ets = "pgets"
pgsall = getDatDir()+getDatFileName()


def main(args):
  load(pgsdat)
#  load(sm1)
#  load(sm2)
#  load(sm3)
#  loadPainting3Data(pgsdat)
#  print pgsall
#  loadAll(pgsall)
#  loadPaintingSlice(pgsdat)

def load(filename):
  show(readImage(filename))

def show(image):
  n1,n2,n3 = s1.count,s2.count,s3.count
  d1,d2,d3 = s1.delta,s2.delta,s3.delta
  f1,f2,f3 = s1.first,s2.first,s3.first
  l1,l2,l3 = s1.last,s2.last,s3.last
  ipg = ImagePanelGroup(s1,s2,s3,image)
#  ipg.setSlices(900,500,160)
  ipg.setClips(-700,700)
  
  sf = SimpleFrame()
  sf.addImagePanels(ipg)
  sf.setSize(1250,900)
  view = sf.getOrbitView()
#  cbar = ColorBar("Semblance")
#  cbar.setFont(cbar.getFont().deriveFont(36.0))
#  sf.add(cbar,BorderLayout.EAST)
#  cbar.setBackground(Color.WHITE);
#  ipg.addColorMapListener(cbar)  
  zscale = 0.75*max(n2*d2,n3*d3)/(n1*d1)
  view.setAxesScale(1.0,1.0,zscale)
  view.setScale(1.3)
  view.setAzimuth(-65.0)
  view.setWorldSphere(BoundingSphere(BoundingBox(f3,f2,f1,l3,l2,l1)))

from org.python.util import PythonObjectInputStream
def loadPaintingSlice(imageFile):
  fileName = "/Users/cengelsm/Test/paintings/pgs_02.p3"
  fis = FileInputStream(fileName)
  ois = PythonObjectInputStream(fis)
  p3 = ois.readObject()
  fis.close()
  image = readImage(imageFile)
  print s1.count,s2.count
  sl = 30
  xz = image[sl]
  pxz = p3.getPaint()[sl]
  panel = PlotPanel(PlotPanel.Orientation.X1DOWN_X2RIGHT)
  pv1 = panel.addPixels(s1,s2,xz)
  pv1.setPercentiles(1,99);
  pv2 = panel.addPixels(s1,s2,pxz)
  pv2.setColorModel(ColorMap.getJet(0.5))
  panel.setVLabel("Depth (km)")
  panel.setHLabel("Distance (km)")
  frame = PlotFrame(panel)
  frame.setDefaultCloseOperation(PlotFrame.EXIT_ON_CLOSE)
  frame.setSize(1800,1000)
  frame.setVisible(True)
  frame.setFontSizeForSlide(1,1)
  frame.paintToPng(300,6,"/Users/cengelsm/Desktop/test2.png")


def loadPainting3Data(imageFile):
  fileName = "/Users/cengelsm/Test/pgsinterp3.p3"
  fis = FileInputStream(fileName)
  ois = PythonObjectInputStream(fis)
  p3 = ois.readObject()
  fis.close()
  image = readImage(imageFile)
  n1,n2,n3 = s1.count,s2.count,s3.count
  d1,d2,d3 = s1.delta,s2.delta,s3.delta
  f1,f2,f3 = s1.first,s2.first,s3.first
  l1,l2,l3 = s1.last,s2.last,s3.last
  ipg = ImagePanelGroup2(s1,s2,s3,image,p3.getPaint())
  ipg.setColorModel1(ColorMap.getGray())
  ipg.setColorModel2(ColorMap.getJet(0.5))
  ipg.setSlices(700,50,80)
  sf = SimpleFrame()
  world = sf.getWorld()
  world.addChild(ipg)
  sf.setSize(1250,900)
  view = sf.getOrbitView()
  zscale = 0.75*max(n2*d2,n3*d3)/(n1*d1)
  view.setAxesScale(1.0,1.0,zscale)
  view.setScale(1.3)
  view.setAzimuth(-65.0)
  view.setWorldSphere(BoundingSphere(BoundingBox(f3,f2,f1,l3,l2,l1)))
#  c = p3.getContour(0.5)
#  tg = TriangleGroup(c.i,c.x)
#  tg.setColor(Color.RED)
#  world.addChild(tg)


def loadAll(fileName):
  image = zerofloat(s1.count,s2.count,s3.count)
  ais = ArrayInputStream(fileName)
  ais.readFloats(image)
  ais.close()
  show(image)


#############################################################################
run(main)
