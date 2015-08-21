package cae.la

import java.lang.Math._

class LUDecompose(a: Matrix, pivoting: Boolean) {

  var m = a.m
  var n = a.n
  var l = new Matrix(m,n)
  var u = new Matrix(m,n)
  var p = new Matrix(m,n)

  def this(a: Matrix) = this(a,true)

  def gauss() {
    Matrix.check(m==n,"not a square matrix")
    var piv = 0
    var v = 0.0f
    var temp = 0.0f
    l.eye()

    for (k <- 0 until m-1) {
      piv = k
      // Find the pivot row
      if (pivoting) {
        for (s <- k+1 until m) {
          v = a.get(s,k)
          if (abs(v)>abs(a.get(k,k))) piv = s
        }
        // Swap rows so largest value is on diagonal
        if (piv!=k) {
          for (t <- 0 until m) {
            temp = a.get(piv,t)
            a.set(piv,t,a.get(k,t))
            a.set(k,t,temp)
          }
        }
      }

      //TODO Compute permutation matrix P

      for (i <- k+1 until m) {
        l.set(i,k,a.get(i,k)/a.get(k,k))
        for (j <- k+1 until m) {
          a.set(i,j,a.get(i,j)-l.get(i,k)*a.get(k,j))
        }
      }
    }

    u = a.triu()

  }
}
