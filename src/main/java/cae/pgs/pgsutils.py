"""
Jython utilities for Rapid Salt Identification Research Project
Author: Chris Engelsma, Colorado School of Mines
Version: 2010.08.26
"""
from imports import *

#############################################################################
# Internal constants

_pgsDir = "/Data/pgs/"
_csmDir = _pgsDir+"csm/"
_datDir = _pgsDir+"dat/"
_datFile = "pgsdat.dat"

def setupForSubset(name):
  global seismicDir
  global s1,s2,s3
  subdir = name.split("/")[-1]
  parts = subdir.split("_")
  n1 = int(parts[1])
  n2 = int(parts[2])
  n3 = int(parts[3])
  d1,d2,d3 = 0.010,0.025,0.040
  f1,f2,f3 = 0.000,0.000,0.000
  s1 = Sampling(n1,d1,f1)
  s2 = Sampling(n2,d2,f2)
  s3 = Sampling(n3,d3,f3)
  seismicDir = _csmDir+subdir+"/"

def getSubDir():
  return seismicDir

def getSamplings():
  return s1,s2,s3

def getCSMDir():
  return _csmDir

def getDatDir():
  return _datDir

def getDatFileName():
  return _datFile

def readImage(name):
  """
  Reads an image from a file with specified name
  """
  fileName = seismicDir+name+".dat"
  n1,n2,n3 = s1.count,s2.count,s3.count
  image = zerofloat(n1,n2,n3)
  ais = ArrayInputStream(fileName)
  ais.readFloats(image)
  ais.close()
  return image

def writeImage(name,image):
  fileName = seismicDir+name+".dat"
  aos = ArrayOutputStream(fileName)
  aos.writeFloats(image)
  aos.close()
  return image

from org.python.util import PythonObjectInputStream
def readTensors(name):
  fis = FileInputStream(seismicDir+name+".dat")
  ois = PythonObjectInputStream(fis)
  tensors = ois.readObject()
  fis.close()
  return tensors

def writeTensors(name,tensors):
  fos = FileOutputStream(seismicDir+name+".dat")
  oos = ObjectOutputStream(fos)
  oos.writeObject(tensors)
  fos.close()

#############################################################################
# Run the function main on the Swing thread
class _RunMain(Runnable):
  def __init__(self,main):
    self.main = main
  def run(self):
    self.main(sys.argv)
def run(main):
  SwingUtilities.invokeLater(_RunMain(main))
