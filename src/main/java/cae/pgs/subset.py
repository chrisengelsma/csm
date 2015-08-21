"""
Makes directories with subsets of seismic depth images.
Author: Chris Engelsma, Colorado School of Mines
Version 2010.08.19
"""
from pgsutils import *

#############################################################################
def main(args):
  s1o = Sampling(900,s1i.delta,s1i.first)
  s2o = Sampling(500,s2i.delta,s2i.first)
  s3o = Sampling(160,s3i.delta,s3i.first)
  makeSubset(s1o,s2o,s3o)

s1i = Sampling(1501,0.010,0.000)
s2i = Sampling(1740,0.025,0.000)
s3i = Sampling( 484,0.040,0.000)

def makeSubset(s1o,s2o,s3o):
  n1o,d1o,f1o = s1o.count,s1o.delta,s1o.first
  n2o,d2o,f2o = s2o.count,s2o.delta,s2o.first
  n3o,d3o,f3o = s3o.count,s3o.delta,s3o.first

  n1i,d1i,f1i = s1i.count,s1i.delta,s1i.first
  n2i,d2i,f2i = s2i.count,s2i.delta,s2i.first
  n3i,d3i,f3i = s3i.count,s3i.delta,s3i.first

  k1i = round(d1o/d1i)
  j1i = round((f1o-f1i)/d1i)

  n1s,n2s,n3s = str(n1o),str(n2o),str(n3o)
  fileName = "pgssub.dat"
  datFile = getDatFileName()
  inDir = getDatDir()
  outDir = getCSMDir()+"subz_"+n1s+"_"+n2s+"_"+n3s+"/"
  inFile = inDir+datFile
  outFile = outDir+fileName
  File(outDir).mkdir()
  xi = zerofloat(n1i)
  xo = zerofloat(n1o)

  ais = ArrayInputStream(inFile)
  aos = ArrayOutputStream(outFile)
  for i3 in range(n3o):
    for i2 in range(n2o):
      ais.readFloats(xi)
      copy(n1o,j1i,k1i,xi,0,1,xo)
      aos.writeFloats(xo)
    ais.skipBytes(4*n1i*(n2i-n2o))
  aos.close()
  ais.close()

  display(outFile,s1o,s2o,s3o)

def display(file,s1,s2,s3):
  n1,n2,n3 = s1.count,s2.count,s3.count
  d1,d2,d3 = s1.delta,s2.delta,s3.delta
  x = zerofloat(n1,n2,n3)
  ais = ArrayInputStream(file)
  ais.readFloats(x)
  ais.close()

  sf = SimpleFrame()
  ipg = sf.addImagePanels(s1,s2,s3,x)
  ipg.setClips(-2000,2000)

#############################################################################
run(main)
