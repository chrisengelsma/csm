/****************************************************************************
Copyright (c) 2010, Colorado School of Mines and others. All rights reserved.
This program and accompanying materials are made available under the terms of
the Common Public License - v1.0, which accompanies this distribution, and is
available at http://www.eclipse.org/legal/cpl-v10.html
****************************************************************************/
package cae.la;

import java.util.Random;
import static java.lang.Math.*;

/**
 * A matrix.
 * Entries within the matrix are stored in a double-precision two-dimensional
 * array a[i][j], where i corresponds to the <em>i</em>th row, and j
 * corresponds to the <em>j</em>th column of an m x n matrix.
 * @author Chris Engelsma, Colorado School of Mines
 * @version 2010.05.10
 */
public class JMatrix {

  /**
   * Constructs a zero 1 x 1 matrix.
   */
  public JMatrix() {
    _m = 1;
    _n = 1;
    _A = new double[1][1];
  }

  /**
   * Constructs a zero m x n matrix.
   * @param m the number of rows.
   * @param n the number of columns.
   */
  public JMatrix(int m, int n) {
    _m = m;
    _n = n;
    _A = new double[m][n];
  }

  /**
   * Constructs a new matrix with the same value v at each entry.
   * @param m the number of rows.
   * @param n the number of columns.
   * @param v a constant which is assigned to each entry within the matrix.
   */
  public JMatrix(int m, int n, double v) {
    this(m,n);
    flood(v);
  }

  /**
   * Returns an array of doubles as an m x 1 matrix.
   * Equivalently, this returns a column vector.
   * @param x an array of doubles.
   */
  public JMatrix(double[] x) {
    this(x.length,1);
    for (int i=0; i<_m; ++i) _A[i][0] = x[i];
  }
  
  /**
   * Constructs a new matrix from a two-dimensional array of doubles. In this
   * capacity, the matrix will contain the same size and entries of array a.
   * This matrix only references the input array.
   * @param a a 2D array of doubles.
   */
  public JMatrix(double[][] a) {
    _m = a.length;
    _n = a[0].length;
    _A = a;
  }

  /**
   * Sets this matrix to the identity matrix I.
   */
  public void eye() {
    flood(0.0);
    int m = min(_m,_n);
    for (int i=0; i<m; ++i)
      _A[i][i] = 1.0;
  }

  /**
   * Sets this to a zero-matrix.
   */
  public void zeros() {
    flood(0.0);
  }

  /**
   * Sets this to a matrix filled with ones.
   */
  public void ones() {
    flood(1.0);
  }

  /**
   * Sets this to a matrix filled with random values in the range [0,1].
   */
  public void rand() {
    rand(1);
  }

  /**
   * Sets this to a matrix filled with random values between [0,max].
   * @param max maximum threshold for random values.
   */
  public void rand(double max) {
    Random r = new Random();
    for (int i=0; i<_m; ++i)
      for (int j=0; j<_n; ++j)
        _A[i][j] = r.nextDouble()*max;
  }

  /**
   * Sets this matrix to a symmetric tridiagonal matrix.
   * @param main the value on the main diagonal.
   * @param sup the value on the first super diagonal.
   * @param sub the value on the first sub diagonal.
   */
  public void setTridiagonal(double main, double sup, double sub) {
    flood(0.0);
    for (int i=0; i<_m; ++i)
      for (int j=0; j<_n; ++j) {
        if (i==j) _A[i][j] = main;
        if (i+1==j) _A[i][j] = sup;
        if (i-1==j) _A[i][j] = sub;
      }
  }

  /**
   * Sets this matrix to a diagonal matrix with given value.
   * Passing 1 into this is equivalent to calling the function eye()
   * Note: This does not <em>diagonalize</em> this matrix.
   * @param main the value on the main diagonal.
   */
  public void setDiagonal(double main) {
    flood(0.0);
    int m = min(_m,_n);
    for (int i=0; i<m; ++i)
      _A[i][i] = main;
  }

  /**
   * Transposes this matrix.
   */
  public void transposeEquals() {
    double[][] b = new double[_n][_m];
    for (int i=0; i<_m; ++i)
      for (int j=0; j<_n; ++j)
        b[j][i] = _A[i][j];
    _A = b;
    int m = _m;
    _m = _n;
    _n = m;
  }

  /**
   * Returns the transpose of this matrix.
   * @return the transpose of this matrix.
   */
  public JMatrix transpose() {
    double[][] b = new double[_n][_m];
    for (int i=0; i<_m; ++i)
      for (int j=0; j<_n; ++j)
        b[j][i] = _A[i][j];
    return new JMatrix(b);
  }

  /**
   * Extracts the upper-triangular portion of this matrix.
   * @return the upper-triangular portion of this matrix.
   */
  public JMatrix triu() {
    double[][] ut = new double[_m][_n];
    for (int i=0; i<_m; ++i)
      for (int j=0; j<_n; ++j)
        if (i<=j) ut[i][j] = _A[i][j];
    return new JMatrix(ut);
  }

  /**
   * Extracts the lower-triangular portion of this matrix.
   * @return the lower-triangular portion of this matrix.
   */
  public JMatrix tril() {
    double[][] lt = new double[_m][_n];
    for (int i=0; i<_m; ++i)
      for (int j=0; j<_n; ++j)
        if (i>=j) lt[i][j] = _A[i][j];
    return new JMatrix(lt);
  }

  /**
   * Sets this matrix to its upper-triangular portion.
   */
  public void triuEquals() {
    JMatrix B = this.triu();
    _A = B.toArray();
  }

  /**
   * Sets this matrix to its lower-triangular portion.
   */
  public void trilEquals() {
    JMatrix B = this.tril();
    _A = B.toArray();
  }
  
  /**
   * Returns the norm of a specified matrix.
   * @param p the type of norm. 0=infinity norm, 1=1-norm, 2=2-norm
   * @return the norm.
   */
  public double norm(int p) {
    check(p>=0&&p<=2,"invalid parameter "+p+" for p-norm");
    double norm = 0.0;
    switch (p) {
      case(0): // Infinity norm
        for (int i=0; i<_m; ++i) {
          double l=0;
          for (int j=0; j<_n; ++j) l += abs(_A[i][j]);
          norm = max(l,norm);
        }
        break;
      case(1): // 1-norm
        for (int j=0; j<_n; ++j) {
          double l = 0;
          for (int i=0; i<_m; ++i) l += abs(_A[i][j]);
          norm = max(l,norm);
        }
        break;
      default: // 2-norm
        // TODO use SVD to get largest singular value.
    }
    return norm;
  }

  /**
   * Returns the 2-norm of a specified matrix.
   * @return the 2-norm;
   */
  public double norm() {
    return norm(2);
  }


  /**
   * Computes the result of this matrix times a specified matrix B, then sets
   * this matrix equal to that value.
   * The order of operations is assumed to be AB = C, where A is this matrix
   * and B is the specified matrix.
   * @param B a specified matrix.
   */
  public void timesEquals(JMatrix B) {
    int bm = B.getM(), bn = B.getN();
    double[][] b = B.toArray();
    check(bm==_n,"dimensions don't match");
    double[][] c = new double[_m][bn];

    for (int k=0; k<_m; ++k)
      for (int j=0; j<bn; ++j)
        for (int i=0; i<_n; ++i)
          c[k][j] += _A[k][i]*b[i][j];

    _A = c;
    _n = bn;
  }

  /**
   * Multiplies the entries of this matrix by a scalar value.
   * @param v a scalar value.
   */
  public void timesEquals(double v) {
    for (int i=0; i<_m; ++i)
      for (int j=0; j<_n; ++j)
        _A[i][j]*=v;
  }
  
  /**
   * Multiplies this matrix by a specified matrix and returns the result.
   * @param B a matrix.
   * @return the result A*B.
   */
  public JMatrix times(JMatrix B) {
    int bm = B.getM(), bn = B.getN();
    check(_n==bm,"dimensions don't match");
    double[][] b = B.toArray();
    double[][] c = new double[_m][bn]; // Output matrix.
    for (int k=0; k<_m; ++k)
      for (int j=0; j<bn; ++j)
        for (int i=0; i<_n; ++i)
          c[k][j] += _A[k][i]*b[i][j];

    return new JMatrix(c);
  }


  /**
   * Multiplies this matrix by a specified column vector x on the right.
   * Equivalently, this operation performs A*x
   * @param x a column vector.
   * @return the resulting vector.
   */
  public JMatrix rTimes(double[] x) {
    check(x.length==_n,"dimensions don't match");
    JMatrix v = new JMatrix(x);
    return this.times(v);
  }

  /**
   * Multiplies this matrix by a specified row vector x on the left.
   * Equivalently, this operation performs x'*A.
   * @param x a row vector.
   * @return
   */
  public JMatrix lTimes(double[] x) {
    check(x.length==_m,"dimensions don't match");

    JMatrix v = new JMatrix(x);
    v.transpose();
    return v.times(this);
  }

  /**
   * Computes the sum of this matrix to another matrix.
   * @param B another matrix with the same dimension as this matrix.
   * @return the result of A + B.
   */
  public JMatrix plus(JMatrix B) {
    int bm = B.getM(), bn = B.getN();
    check(bm==_m||bn==_n,"dimensions don't match");
    
    double[][] b = B.toArray();
    double[][] c = new double[_m][_n];
    for (int i=0; i<_m; ++i)
      for (int j=0; j<_n; ++j)
        c[i][j] = _A[i][j]+b[i][j];

    return new JMatrix(c);
  }

  /**
   * Adds a specified matrix to this matrix.
   * @param B a matrix with the same dimensions as this matrix.
   */
  public void plusEquals(JMatrix B) {
    int bm = B.getM(), bn = B.getN();
    check(bm==_m||bn==_n,"dimensions don't match");
    
    double[][] b = B.toArray();
    for (int i=0; i<_m; ++i)
      for (int j=0; j<_n; ++j)
        _A[i][j] += b[i][j];
  }

  /**
   * Computes the difference between this matrix and a specified matrix.
   * @param B a matrix with the same dimensions as this matrix.
   * @return the result of A - B.
   */
  public JMatrix minus(JMatrix B) {
    int bm = B.getM(), bn = B.getN();
    check(bm==_m||bn==_n,"dimensions don't match");

    double[][] b = B.toArray();
    double[][] c = new double[_m][_n];
    for (int i=0; i<_m; ++i)
      for (int j=0; j<_n; ++j)
        c[i][j] = _A[i][j]-b[i][j];

    return new JMatrix(c);
  }

  /**
   * Subtracts a specified matrix from this matrix.
   * @param B a matrix with the same dimensions as this matrix.
   */
  public void minusEquals(JMatrix B) {
    int bm = B.getM(), bn = B.getN();
    check(bm==_m||bn==_n,"dimensions don't match");

    double[][] b = B.toArray();
    double[][] c = new double[_m][_n];
    for (int i=0; i<_m; ++i)
      for (int j=0; j<_n; ++j)
        _A[i][j]-=b[i][j];
  }

  /**
   * Performs backwards-substitution on this matrix.
   * Backwards-substitution implies that this matrix is upper-triangular.
   * @param b an m x 1 matrix.
   * @return an n x 1 matrix x which is the solution to Ax = b.
   */
  public JMatrix backSub(JMatrix b) {
    check(this.isUpperTriangular(),"this matrix not upper-triangular");
    check(_m==b.getM(),"dimensions don't match");
    check(b.getN()==1,"vector b has more than one column");
    
    int n = _n;
    double[] x = new double[n];
    double[][] ba = b.toArray();
    double v;
    
    for (int i=n-1; i>=0; --i) {
      v = 0.0;
      for (int j=i; j<n; ++j)
        v += _A[i][j]*x[j]/_A[i][i];
      x[i] = ba[i][0] - v;
    }
    return new JMatrix(x);
  }

  /**
   * Performs forward-substitution on this matrix.
   * Forward-substitution implies that this matrix is lower-triangular.
   * @param b an m x 1 matrix.
   * @return an n x 1 matrix x which is the solution to Ax = b.
   */
  public JMatrix forSub(JMatrix b) {
    check(this.isLowerTriangular(),"this matrix not upper-triangular");
    check(_m==b.getM(),"dimensions don't match");
    check(b.getN()==1,"vector b has more than one column");

    int n = _n;
    double[] x = new double[n];
    double[][] ba = b.toArray();
    double v;

    for (int i=0; i<n; ++i) {
      v = 0.0;
      for (int j=0; j<i; ++j)
        v += _A[i][j]*x[j]/_A[i][i];
      x[i] = ba[i][0] - v;
    }
    return new JMatrix(x);
  }
  
  /**
   * Performs backwards-substitution on an upper-triangular matrix U.
   * @param U an m x n upper-triangular matrix.
   * @param b an m x 1 matrix.
   * @return an n x 1 matrix x which is the solution to Ax = b.
   */
  public static JMatrix backSub(JMatrix U, JMatrix b) {
    check(U.isUpperTriangular(),"this matrix not upper-triangular");
    check(U.getM()==b.getM(),"dimensions don't match");
    check(b.getN()==1,"vector b has more than one column");

    int n = U.getN();
    double[] x = new double[n];
    double[][] ba = b.toArray();
    double[][] A = U.toArray();
    double v;

    for (int i=n-1; i>=0; --i) {
      v = 0.0;
      for (int j=i; j<n; ++j)
        v += A[i][j]*x[j]/A[i][i];
      x[i] = ba[i][0] - v;
    }
    return new JMatrix(x);
  }

  /**
   * Performs forwards-substitution on a lower-triangular matrix L.
   * @param L an m x n lower-triangular matrix.
   * @param b an m x 1 matrix.
   * @return an n x 1 matrix x which is the solution Ax = b.
   */
  public static JMatrix forSub(JMatrix L, JMatrix b) {
    check(L.isLowerTriangular(),"this matrix not lower-triangular");
    check(L.getM()==b.getM(),"dimensions don't match");
    check(b.getN()==1,"vector b has more than one column");

    int n = L.getN();
    double[] x = new double[n];
    double[][] ba = b.toArray();
    double[][] A = L.toArray();
    double v;

    for (int i=0; i<n; ++i) {
      v = 0.0;
      for (int j=0; j<i; ++j)
        v += A[i][j]*x[j]/A[i][i];
      x[i] = ba[i][0] - v;
    }
    return new JMatrix(x);
  }

  /**
   * Gets the number of rows in this matrix.
   * @return the number of rows.
   */
  public int getM() {
    return _m;
  }

  /**
   * Gets the number of columns in this matrix.
   * @return the number of columns.
   */
  public int getN() {
    return _n;
  }

  /**
   * Gets a column vector from this matrix.
   * @param j the index of the column vector.
   * @return a single column matrix (column vector).
   */
  public JMatrix getColumn(int j) {
    return getColumn(j,_m-1);
  }

  /**
   * Gets a subset of a column vector from this matrix.
   * @param j the index of the column vector.
   * @param end the ending row index.
   * @return a single column matrix (column vector).
   */
  public JMatrix getColumn(int j, int end) {
    check(j>=0 && j<_n,"column index out of range: "+j);
    check(end>=0 && end<_m,"end index out of range: "+end);
    double[] jcol = new double[end+1];
    for (int i=0; i<=end; ++i)
      jcol[i] = _A[i][j];
    JMatrix col = new JMatrix(jcol);
    return col;
  }

  /**
   * Gets a row vector from this matrix.
   * @param i the index of the row vector to return.
   * @return a single row matrix (row vector).
   */
  public JMatrix getRow(int i) {
    return getRow(i,_n-1);
  }

  /**
   * Gets a subset of a row vector from this matrix.
   * @param i the index of the row vector to return.
   * @param end the ending column index.
   * @return a single row matrix (row vector).
   */
  public JMatrix getRow(int i, int end) {
    check(i>=0 && i<_m,"row index out of range: "+i);
    check(end>=0 && end<_n,"end index out of range: "+end);
    double[] irow = new double[end+1];
    for (int j=0; j<=end; ++j)
      irow[j] = _A[i][j];
    JMatrix row = new JMatrix(irow);
    row.transpose();
    return row;
  }

  /**
   * Returns this matrix as a double-precision two-dimensional array.
   * @return an array of doubles.
   */
  public double[][] toArray() {
    return _A;
  }

  /**
   * Determines if this matrix is a square matrix, that is if the number of
   * rows and columns are the same.
   * @return true, if this matrix is square; false, otherwise.
   */
  public boolean isSquare() {
    return (_m==_n);
  }

  /**
   * Determines if this matrix is symmetric, or if A = A'.
   * @return true, if this matrix is symmetric; false, otherwise.
   */
  public boolean isSymmetric() {
    if (!this.isSquare()) return false;
    JMatrix B = this.copy();
    B.transpose();
    double[][] b = B.toArray();
    for (int i=0; i<_m; ++i)
      for (int j=0; j<_n; ++j)
        if (_A[i][j]!=b[i][j]) return false;
    return true;
  }

  /**
   * Determines if this matrix is upper-triangular.
   * A matrix is upper-triangular if A[i][j] = 0, where i < j.
   * @return true, if upper-triangular; false, otherwise.
   */
  public boolean isUpperTriangular() {
    for (int i=0; i<_m; ++i)
      for (int j=0; j<i-1; ++j)
        if (_A[i][j]!= 0.0) return false;
    return true;
  }

  /**
   * Determines if this matrix is lower-triangular.
   * A matrix is lower-triangular if A[i][j] = 0, where i > j.
   * @return true ,if lower-triangular; false, otherwise.
   */
  public boolean isLowerTriangular() {
    for (int i=0; i<_m; ++i)
      for (int j=i+1; j<_n; ++j)
        if (_A[i][j]!=0.0) return false;
    return true;
  }

  /**
   * Returns a copy of this matrix.
   * @return a copy of this matrix.
   */
  public JMatrix copy() {
    double[][] b = new double[_m][_n];
    for (int i=0; i<_m; ++i)
      for (int j=0; j<_n; ++j) {
        double v = _A[i][j];
        b[i][j] = v;
      }
    return new JMatrix(b);
  }

  /**
   * Prints this matrix.
   */
  public void print() {
    for (int i=0; i<_m; ++i) {
      for (int j=0; j<_n; ++j) {
        System.out.print(_A[i][j]+" ");
      }
      System.out.println();
    }
  }

  ///////////////////////////////////////////////////////////////////////////
  // private

  private int _m; // rows.
  private int _n; // columns.
  private double[][] _A; // array[_m][_n] of matrix entries.

  private void flood(double v) {
    for (int i=0; i<_m; ++i)
      for (int j=0; j<_n; ++j)
        _A[i][j] = v;
  }

  private static void check(boolean condition, String message) {
    if (!condition) {
      System.out.println(message);
      System.exit(0);
    }
  }
}
