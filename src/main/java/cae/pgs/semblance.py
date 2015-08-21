"""
Computes semblance images
"""
from pgsutils import *

setupForSubset("subz_900_500_160")
s1,s2,s3 = getSamplings()

sfile = "pgssub"
efile = "pgset"
mfile = "pgsmask"
s1file = "pgss1" # semblance w,uv
s2file = "pgss2" # semblance vw,u 
s3file = "pgss3" # semblance uvw

lsf1 = LocalSemblanceFilter(2,2)
lsf2 = LocalSemblanceFilter(2,8)
lsf3 = LocalSemblanceFilter(16,0)
mask = ZeroMask(readImage(mfile))

def main(args):
  #semblance1()
  #semblance2()
  #semblance3()
  display(sfile,s1file,"s1")
#  display(sfile,s2file,"s2")
#  display(sfile,s3file,"s3")

def maskSemblance():
  for smfile in [s1file,s2file,s3file]:
    sm = readImage(smfile)
    if smfile==s3file:
      mask.apply(0.0001,sm)
    else:
      mask.apply(1.00,sm)
    writeImage(smfile,sm)

def semblance1():
  print "computing s1"
  e = readTensors(efile)
  s = readImage(sfile)
  s1 = lsf1.semblance(LocalSemblanceFilter.Direction3.W,e,s)
  mask.apply(1.00,s1)
  print "writing s1"
  writeImage(s1file,s1)

def semblance2():
  print "computing s2"
  e = readTensors(efile)
  s = readImage(sfile)
  s2 = lsf2.semblance(LocalSemblanceFilter.Direction3.VW,e,s)
  mask.apply(1.00,s2)
  print "writing s2"
  writeImage(s2file,s2)

def semblance3():
  print "computing s3"
  e = readTensors(efile)
  s = readImage(sfile)
  s3 = lsf3.semblance(LocalSemblanceFilter.Direction3.UVW,e,s)
  mask.apply(1.00,s3)
  print "writing s3"
  writeImage(s3file,s3)

def display(sfile,smfile,name):
  s = readImage(sfile)
  sm = readImage(smfile)
  print name,"min =",min(sm),"max =",max(sm)
  sf = SimpleFrame()
  sf.setTitle(name)
  ipg = ImagePanelGroup(s1,s2,s3,sm)

  world = sf.getWorld()
  world.addChild(ipg)

#############################################################################
run(main)
