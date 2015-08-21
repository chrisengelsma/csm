/****************************************************************************
Copyright (c) 2010, Colorado School of Mines and others. All rights reserved.
This program and accompanying materials are made available under the terms of
the Common Public License - v1.0, which accompanies this distribution, and is
available at http://www.eclipse.org/legal/cpl-v10.html
****************************************************************************/
package cae.la;

import static java.lang.Math.*;

/**
 * An LU decomposition of a square matrix.
 * Assume we have an m x m matrix A, then there is an m x m unit-lower
 * triangular matrix L, and an m x m upper-triangular matrix U, where A = LU.
 * We find these matrices using gaussian elimination with or without partial
 * pivoting. In the case of partial pivoting, an m x m matrix P is constructed
 * such that P*A = L*U.
 * @author Chris Engelsma, Colorado School of Mines.
 * @version 2010.05.19
 */
public class JLUDecompose {

  /**
   * Performs an LU decomposition on a square matrix using gaussian
   * elimination with partial pivoting.
   * @param A a square matrix.
   */
  public JLUDecompose(JMatrix A) {
    this(A,true);
  }

  /**
   * Performs an LU decomposition on a square matrix using gaussian
   * elimination with or without partial pivoting.
   * @param A a square matrix.
   * @param pivoting true, if using partial pivoting; false, otherwise.
   */
  public JLUDecompose(JMatrix A, boolean pivoting) {
    gauss(A.copy(),pivoting);
  }

  /**
   * Gets the unit lower triangular matrix L for this LU decomposition.
   * @return the unit lower triangular matrix L.
   */
  public JMatrix getL() {
    return L;
  }

  /**
   * Gets the upper triangular matrix U for this LU decomposition.
   * @return the upper triangular matrix U.
   */
  public JMatrix getU() {
    return U;
  }

  /**
   * Gets the permutation matrix P such that P*A = L*U.
   * <em>NOT YET IMPLEMENTED</em>
   * @return the permutation matrix P.
   */
  public JMatrix getP() {
    return P;
  }

  //////////////////////////////////////////////////////////////////////////
  // private
  private JMatrix L,U,P;

  private void gauss(JMatrix A, boolean pivoting) {
    int piv;    // pivot
    int m = A.getM(), n = A.getN();
    if (m!=n) {
      // TODO check
    }
    L = new JMatrix(m,m);
    P = new JMatrix(m,m);
    L.eye();
    double[][] p = P.toArray();
    double[][] l = L.toArray();
    double[][] a = A.toArray();
    for (int k=0; k<m-1; ++k) { // Over all columns
      
      piv = k;
      
      // Find the pivot
      if (pivoting) {
        for (int s=k+1; s<m; ++s) {
          double v = a[s][k];
          if (abs(v)>abs(a[k][k])) piv = s;
        }
        // Swap rows to largest value is on diagonal.
        if (piv!=k) {
          for (int t=0; t<m; ++t) {
            double temp = a[piv][t];
            a[piv][t] = a[k][t];
            a[k][t] = temp;
          }
        }
      }

      // TODO: Compute permutation matrix P.

      for (int i=k+1; i<m; ++i) { // Over all rows > k
        l[i][k] = a[i][k]/a[k][k]; // Compute multiplication factor.
        for (int j=k+1; j<m; ++j) {
          a[i][j] = a[i][j] - l[i][k]*a[k][j];
        }
      }
    }
    U = A.triu();
  }
}
