/****************************************************************************
Copyright (c) 2010, Colorado School of Mines and others. All rights reserved.
This program and accompanying materials are made available under the terms of
the Common Public License - v1.0, which accompanies this distribution, and is
available at http://www.eclipse.org/legal/cpl-v10.html
****************************************************************************/
package cae.la;

/**
 * A QR Decomposition of a matrix.
 * <em>NOT YET IMPLEMENTED</em>
 * @author Chris Engelsma, Colorado School of Mines
 * @version 2010
 */
public class QRDecompose {

  public QRDecompose(JMatrix A) {
    this(A,true);
  }

  public QRDecompose(JMatrix A, boolean modified) {
    gramSchmidt(A.copy(),modified);
  }

  ///////////////////////////////////////////////////////////////////////////
  // private
  private JMatrix Q,R;

  private void gramSchmidt(JMatrix A, boolean modified) {
    int m = A.getM(), n = A.getN();
    Q = new JMatrix(m,n);
    R = new JMatrix(n,n);
    double[][] q = Q.toArray();
    double[][] r = R.toArray();
    JMatrix v;
    for (int j=0; j<n; ++j) {
      v = A.getColumn(j);
      for (int i=0; i<j; ++i) {
        JMatrix qv = (Q.getColumn(j,i)).transpose();
        if (modified) qv.timesEquals(v);
        else          qv.timesEquals(A.getColumn(j,j));
        // TODO
        //r[i][j] = (Q.getColumn(j,i).transpose()).times(v);
      }
    }
  }
}
