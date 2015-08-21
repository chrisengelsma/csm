"""
Computes a mask for samples that are zero or near zero
"""
from pgsutils import *

setupForSubset("subz_900_500_160")
s1,s2,s3 = getSamplings()
n1,n2,n3 = s1.count,s2.count,s3.count

sfile = "pgssub"
mfile = "pgsmask"

def main(args):
  s = readImage(sfile)
  print "min =",min(s),"max =",max(s)
  mask = ZeroMask(0.1,5.0,1.0,1.0,s)
  m = mask.getAsFloats()
  writeImage(mfile,m)
  display()

def display():
  s = readImage(sfile)
  m = readImage(mfile)
  sf = SimpleFrame()
  world = sf.getWorld()
  ipg = ImagePanelGroup2(s1,s2,s3,s,m)
  ipg.setColorModel1(ColorMap.getGray())
  ipg.setColorModel2(ColorMap.getJet(0.3))
  world.addChild(ipg)

#############################################################################
run(main)
