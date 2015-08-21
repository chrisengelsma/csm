"""
Computes structure tensors for the PGS data
Author: Chris Engelsma, Colorado School of Mines
Version: 2010.08.26
"""
from pgsutils import *

setupForSubset("subz_900_500_160")
s1,s2,s3 = getSamplings()

sDir = getSubDir()
sfile  = "pgssub"
efile  = "pgset"
esfile = "pgsets"
s1file = "pgss1"
s2file = "pgss2"
s3file = "pgss3"

def main(args):
#  makeStructureTensors()
  assignSemblance()
  display(esfile)

def makeStructureTensors():
  print "building tensors"
  sigma = 8.0
  s = readImage(sfile)
  lof = LocalOrientFilter(sigma)
  e = lof.applyForTensors(s)
  writeTensors(efile,e)

def assignSemblance():
  print "reassigning tensors"
  e = readTensors(efile)
  sm1 = readImage(s1file)
  sm2 = readImage(s2file)
  sm3 = readImage(s3file)
  a = zerofloat(3)
  for i3 in range(s3.count):
    for i2 in range(s2.count):
      for i1 in range(s1.count):
        a[2] = sm1[i3][i2][i1]
        a[1] = sm2[i3][i2][i1]
        a[0] = sm3[i3][i2][i1]
        e.setEigenvalues(i1,i2,i3,a)
  writeTensors(esfile,e)

def display(ef):
  s = readImage(sfile)
  e = readTensors(ef)
  sf = SimpleFrame()
  ipg = sf.addImagePanels(s1,s2,s3,s)
  ipg.setClips(-2000,2000)
  sf.addTensorsGroup(s1,s2,s3,e)

#############################################################################
run(main)
