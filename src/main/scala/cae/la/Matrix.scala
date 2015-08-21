package cae.la

import java.util.Random

/**
 * A matrix.
 * Entries within the matrix are stored in a float-point precision
 * two-dimensional array a(i)(j), where i corresponds to the <em>i</em>th row,
 * and j corresponds to the <em>j</em>th column.
 * @author Chris Engelsma
 * @version 2010.12.18
 */
class Matrix(var a: Array[Array[Float]]) {

  var m = a.length    // rows
  var n = a(0).length // cols

  /**
   * Constructs a new matrix of size (m,n).
   * This matrix will be filled with zeros.
   */
  def this(m: Int, n: Int) = this(Array.ofDim[Float](m,n))

  /**
   * Constructs a new square matrix of size (m,m).
   * This matrix will be filled with zeros.
   */
  def this(m: Int) = this(Array.ofDim[Float](m,m))

  /**
   * Sets this matrix to the identity matrix.
   * The identity matrix contains ones along the diagonal and zeros
   * everywhere else.
   */
  def eye() {
    for (i <- 0 until m)
      for (j <- 0 until n)
        if (i==j) a(i)(j) = 1.0f else a(i)(j) = 0.0f
  }

  /**
   * Sets this matrix's entries to random floats between [0,1).
   */
  def rand() {
    rand(1.0f)
  }

  /**
   * Sets this matrix's entries to random floats between [0,max).
   * Note: rand(1.0f) is the same as calling rand().
   */
  def rand(max: Float) {
    val r = new Random()
    for (i <- 0 until m)
      for (j <- 0 until n)
        a(i)(j) = max*r.nextFloat()
  }

  /**
   * Returns the upper-triangular portion of this matrix.
   * @return the upper-triangular portion of this matrix.
   */
  def triu(): Matrix = {
    val b = this.copy()
    for (i <- 0 until m)
      for (j <- 0 until n)
        if (i < j) b.set(i,j,0.0f)
    return b
  }

  /**
   * Returns the lower-triangular portion of this matrix.
   * @return the lower-triangular portion of this matrix.
   */
  def tril(): Matrix = {
    val b = this.copy()
    for (i <- 0 until m)
      for (j <- 0 until n)
        if (i>j) b.set(i,j,0.0f)
    return b
  }

  /**
   * Returns the transpose of this matrix.
   * @return the transpose of this matrix.
   */
  def transpose(): Matrix = {
    val b = new Matrix(n,m)
    for (i <- 0 until m)
      for (j <- 0 until n)
        b.set(j,i,a(i)(j))
    return b
  }

  /**
   * Transposes this matrix.
   */
  def transposeEquals() {
    val b = this.copy()
    return b.transpose()
  }

  /**
   * Adds two matrices
   * Both matrices must have the same dimensions.
   * @param b a matrix.
   * @return the result C = A + B
   */
  def +(b: Matrix): Matrix = {
    var c = this.copy()
    c+=b
    return c
  }

  /**
   * Subtracts two matrices.
   * Both matrices must have the same dimensions.
   * @param b a matrix.
   * @return the result C = A - B
   */
  def -(b: Matrix): Matrix = {
    var c = this.copy()
    c-=b
    return c
  }

  /**
   * Multiplies two matrices.
   * The number of columns in the first matrix must equal the number of rows
   * in the second matrix.
   * @param b a matrix.
   * @return the result C = A * B
   */
  def *(b: Matrix): Matrix = {
    var c = this.copy()
    c*=b
    return c
  }

  /**
   * Adds a matrix to this matrix.
   * The submitted matrix must have the same dimensions as this matrix.
   * @param b a matrix.
   */
  def +=(b: Matrix) {
    Matrix.check((m==b.m)&&(n==b.n),"dimensions don't match")
    for (i <- 0 until m)
      for (j <- 0 until n)
        a(i)(j) += b.get(i,j)
  }

  /**
   * Subtracts a matrix from this matrix.
   * The submitted matrix must have the same dimensions as this matrix.
   * @param b a matrix.
   */
  def -=(b: Matrix) {
    Matrix.check((m==b.m)&&(n==b.n),"dimensions don't match")
    for (i <- 0 until m)
      for (j <- 0 until n)
        a(i)(j) -= b.get(i,j)
  }

  /**
   * Multiplies this matrix times another matrix.
   * The number of columns in this matrix must equal the number of rows in
   * the submitted matrix.
   * @param b a matrix.
   */
  def *=(b: Matrix) {
    Matrix.check(b.m==n,"dimensions don't match")
    val c = Array.ofDim[Float](m,b.n)
    for (k <- 0 until m)
      for (j <- 0 until b.n)
        for (i <- 0 until n)
          c(k)(j) += a(k)(i)*b.get(i,j)
    a = c
    n = b.n
  }

  /**
   * Gets a value from this matrix.
   * @param i a row of this matrix.
   * @param j a column of this matrix.
   * @return the value at (i,j).
   */
  def get(i: Int, j: Int): Float = {
    return a(i)(j)
  }

  /**
   * Sets a value in this matrix.
   * @param i a row of this matrix.
   * @param j a column of this matrix.
   * @param x a value to be entered at (i,j).
   */
  def set(i: Int, j: Int, x: Float) {
    a(i)(j) = x
  }

  /**
   * Determines if this matrix is square.
   * A square matrix has the same number of rows and columns.
   * @return true, if this matrix is square; false, otherwise.
   */
  def isSquare(): Boolean = {
    return m==n
  }

  /**
   * Determines if this matrix is symmetric.
   * A matrix is symmetric if A = A'.
   * @return true, if this matrix is symmetric; false, otherwise.
   */
  def isSymmetric(): Boolean = {
    if (!this.isSquare()) return false
    for (i <- 0 until m)
      for (j <- 0 until n)
        if (a(i)(j)!=a(j)(i)) return false
    return true
  }

  /**
   * Determines if this matrix is upper-triangular.
   * @return true, if upper-triangular; false, otherwise.
   */
  def isUpperTriangular(): Boolean = {
    for (i <- 0 until m)
      for (j <- 0 until n)
        if (i>j && a(i)(j)!=0.0f) return false
    return true
  }

  /**
   * Determines if this matrix is lower-triangular.
   * @return true, if lower-triangular; false, otherwise.
   */
  def isLowerTriangular(): Boolean = {
    for (i <- 0 until m)
      for (j <- 0 until n)
        if (i<j && a(i)(j)!=0.0f) return false
    return true
  }

  /**
   * Copies the values of this matrix into a new matrix
   * @return a copy of this matrix.
   */
  def copy(): Matrix = {
    var b = Array.ofDim[Float](m,n)
    for (i <- 0 until m)
      for (j <- 0 until n)
        b(i)(j) = a(i)(j)
    return new Matrix(b)
  }

  /**
   * Returns this matrix as a 2D array.
   * @return a 2D array.
   */
  def toArray(): Array[Array[Float]] = {
    return a
  }

  /**
   * Floods this matrix with a given value.
   * @param f a value to flood.
   */
  def flood(f: Float) {
    for (i <- 0 until m)
      for (j <- 0 until n)
        a(i)(j) = f;
  }

  /**
   * Overrides the print defintion.
   */
  override def toString() = {
    a.map(_.mkString(" ")).mkString("\n")
  }
}

object Matrix {
  /**
   * Checks a certain condition and returns a message if that condition is not
   * met.
   */
  def check(cond: Boolean, message: String) {
    if (!cond) {
      println(message)
      exit(0)
    }
  }

  /**
   * Performs backwards-substitution on an upper-triangular matrix.
   * @param a an m x n upper-triangular matrix.
   * @param b an m x 1 matrix.
   * @return an n x 1 matrix which is the solution to Ax = b.
   */
  def backSub(a: Matrix, b: Matrix): Matrix = {
    check(a.isUpperTriangular(),"a is not upper-triangular")
    check(a.m==b.m,"dimensions don't match")
    check(b.n==1,"vector b has more than one column")

    val n = a.n
    var v = 0.0f
    val x = new Matrix(n,1)

    for (i <- n-1 to 0 by -1) {
      v = 0.0f
      for (j <- i until n)
        v += a.get(i,j)*x.get(j,0)/a.get(i,i)
      x.set(i,0,b.get(i,0)-v)
    }
    return x
  }

  /**
   * Performs forwards-substitution on a lower-triangular matrix.
   * @param a an m x n lower-triangular matrix.
   * @param b an m x 1 matrix.
   * @return an n x 1 matrix which is the solution to Ax = b.
   */
  def forSub(a: Matrix, b: Matrix): Matrix = {
    check(a.isLowerTriangular(),"a is not lower-triangular")
    check(a.m==b.m,"dimensions don't match");
    check(b.n==1,"vector b has more than one column");

    val n = a.n
    var v = 0.0f
    val x = new Matrix(n,1)

    for (i <- 0 until n) {
      v = 0.0f
      for (j <- 0 until i)
        v += a.get(i,j)*x.get(j,0)/a.get(i,i)
      x.set(i,0,b.get(i,0)-v)
    }
    return x
  }
}
