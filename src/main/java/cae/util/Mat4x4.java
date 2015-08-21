/****************************************************************************
Copyright (c) 2010, Colorado School of Mines and others. All rights reserved.
This program and accompanying materials are made available under the terms of
the Common Public License - v1.0, which accompanies this distribution, and is
available at http://www.eclipse.org/legal/cpl-v10.html
****************************************************************************/
package cae.util;

/**
 * A 4x4 JMatrix.
 * Matrices of this nature are primarily intended for use in graphics,
 * particularly for setting transformations.
 * @author Chris Engelsma, Colorado School of Mines
 * @version 2010.04.30
 */
public class Mat4x4 {

  public float[] m = new float[16];

  /**
   * Constructs a new 4x4 identity matrix.
   */
  public Mat4x4() {
    m[0] = m[5] = m[10] = m[15] = 1.0f;
  }

  /**
   * Constructs a new 4x4 matrix with given elements.
   * @param m00 the matrix element with indices (0,0)
   * @param m01 the matrix element with indices (0,1)
   * @param m02 the matrix element with indices (0,2)
   * @param m03 the matrix element with indices (0,3)
   * @param m10 the matrix element with indices (1,0)
   * @param m11 the matrix element with indices (1,1)
   * @param m12 the matrix element with indices (1,2)
   * @param m13 the matrix element with indices (1,3)
   * @param m20 the matrix element with indices (2,0)
   * @param m21 the matrix element with indices (2,1)
   * @param m22 the matrix element with indices (2,2)
   * @param m23 the matrix element with indices (2,3)
   * @param m30 the matrix element with indices (3,0)
   * @param m31 the matrix element with indices (3,1)
   * @param m32 the matrix element with indices (3,2)
   * @param m33 the matrix element with indices (3,3)
   */
  public Mat4x4(float m00, float m01, float m02, float m03,
                float m10, float m11, float m12, float m13,
                float m20, float m21, float m22, float m23,
                float m30, float m31, float m32, float m33)
  {
    set(m00,m01,m02,m03,
        m10,m11,m12,m13,
        m20,m21,m22,m23,
        m30,m31,m32,m33);
  }

  /**
   * Constructs a new 4x4 matrix from a given array[16] of floats.
   * @param m an array[16] of floats.
   */
  public Mat4x4(float[] m) {
    this.m = m;
  }

  /**
   * Sets the elements of this matrix to given elements.
   * @param m00 the matrix element with indices (0,0)
   * @param m01 the matrix element with indices (0,1)
   * @param m02 the matrix element with indices (0,2)
   * @param m03 the matrix element with indices (0,3)
   * @param m10 the matrix element with indices (1,0)
   * @param m11 the matrix element with indices (1,1)
   * @param m12 the matrix element with indices (1,2)
   * @param m13 the matrix element with indices (1,3)
   * @param m20 the matrix element with indices (2,0)
   * @param m21 the matrix element with indices (2,1)
   * @param m22 the matrix element with indices (2,2)
   * @param m23 the matrix element with indices (2,3)
   * @param m30 the matrix element with indices (3,0)
   * @param m31 the matrix element with indices (3,1)
   * @param m32 the matrix element with indices (3,2)
   * @param m33 the matrix element with indices (3,3)
   */
  public void set(float m00, float m01, float m02, float m03,
                  float m10, float m11, float m12, float m13,
                  float m20, float m21, float m22, float m23,
                  float m30, float m31, float m32, float m33)
  {
    m[ 0] = m00; m[ 4] = m01; m[ 8] = m02; m[12] = m03;
    m[ 1] = m10; m[ 5] = m11; m[ 9] = m12; m[13] = m13;
    m[ 2] = m20; m[ 6] = m21; m[10] = m22; m[14] = m23;
    m[ 3] = m30; m[ 7] = m31; m[11] = m32; m[15] = m33;
  }

  /**
   * Returns the transpose of this matrix.
   * @return the transpose of this matrix.
   */
  public Mat4x4 transpose() {
    float[] t = {
      m[ 0],m[ 4],m[ 8],m[12],
      m[ 1],m[ 5],m[ 9],m[13],
      m[ 2],m[ 6],m[10],m[14],
      m[ 3],m[ 7],m[11],m[15]
    };
    return new Mat4x4(t);
  }

  /**
   * Sets this matrix to a rotation-only matrix about a specific vector axis.
   * @param ang the angle of rotation in degrees.
   * @param rx the x component of the vector axis of rotation.
   * @param ry the y component of the vector axis of rotation.
   * @param rz the z component of the vector axis of rotation.
   * @return this rotation-only matrix.
   */
  public Mat4x4 setToRotate(float ang, float rx, float ry, float rz) {
    float rs = 1.0f/(float)Math.sqrt(rx*rx+ry*ry+rz*rz);
    rx *= rs;
    ry *= rs;
    rz *= rs;
    float ca = (float)Math.cos(ang*degToRad);
    float sa = (float)Math.sin(ang*degToRad);
    float xx = rx*rx, xy = rx*ry, xz = rx*rz;
    float yx = xy   , yy = ry*ry, yz = ry*rz;
    float zx = xz   , zy = yz   , zz = rz*rz;
    float m00 = xx + ca*(1.0f-xx) + sa*(0.0f);
    float m01 = xy + ca*(0.0f-xy) + sa*( -rz);
    float m02 = xz + ca*(0.0f-xz) + sa*(  ry);
    float m10 = yx + ca*(0.0f-yx) + sa*(  rz);
    float m11 = yy + ca*(1.0f-yy) + sa*(0.0f);
    float m12 = yz + ca*(0.0f-yz) + sa*( -rx);
    float m20 = zx + ca*(0.0f-zx) + sa*( -ry);
    float m21 = zy + ca*(0.0f-zy) + sa*(  rx);
    float m22 = zz + ca*(1.0f-zz) + sa*(0.0f);
    set(m00, m01, m02, 0.0f,
        m10, m11, m12, 0.0f,
        m20, m21, m22, 0.0f,
        0.0f,0.0f,0.0f,1.0f);
    return this;
  }

  /**
   * Sets this matrix to a rotation-only matrix.
   * @param ang the angle of rotation.
   * @param v the vector axis of rotation.
   * @return this rotation-only matrix.
   */
  public Mat4x4 setToRotate(float ang, Vector3 v) {
    return setToRotate(ang,v.x,v.y,v.z);
  }

  /**
   * Sets this matrix to a translation-only matrix.
   * @param tx the x-component of the translation.
   * @param ty the y-component of the translation.
   * @param tz the z-component of the translation.
   * @return this translation-only matrix.
   */
  public Mat4x4 setToTranslate(float tx, float ty, float tz) {
    set(1.0f,0.0f,0.0f, tx,
        0.0f,1.0f,0.0f, ty,
        0.0f,0.0f,1.0f, tz,
        0.0f,0.0f,0.0f,1.0f);
    return this;
  }

  /**
   * Sets this matrix to a translation-only matrix.
   * @param t the translation vector.
   * @return this translation-only matrix.
   */
  public Mat4x4 setToTranslate(Vector3 t) {
    return setToTranslate(t.x,t.y,t.z);
  }
  
  /**
   * Returns a new identity matrix.
   * @return an identity matrix.
   */
  public Mat4x4 I() {
    return new Mat4x4();
  }

  /**
   * Multiplies this matrix (M) times another 4x4 matrix (A): C = MA.
   * @param a a 4x4 matrix.
   * @return the matrix multplication MA.
   */
  public Mat4x4 times(Mat4x4 a) {
    return new Mat4x4(mul(m,a.m,new float[16]));
  }

  public float[] toArray() {
    return m;
  }

  /**
   * Determines whether this matrix is symmetric by comparing M = M'.
   * @return true, if symmetric; false, otherwise.
   */
  public boolean isSymmetric() {
   Mat4x4 t = transpose();
    boolean sym = true;
    for (int i=0; i<16; ++i)
      if (m[i]!=t.m[i]) sym = false;

    return sym;
  }

  public void print() {
    System.out.println(m[ 0]+"\t"+m[ 4]+"\t"+m[ 8]+"\t"+m[12]+"\n"+
                       m[ 1]+"\t"+m[ 5]+"\t"+m[ 9]+"\t"+m[13]+"\n"+
                       m[ 2]+"\t"+m[ 6]+"\t"+m[10]+"\t"+m[14]+"\n"+
                       m[ 3]+"\t"+m[ 7]+"\t"+m[11]+"\t"+m[15]);
  }

  ///////////////////////////////////////////////////////////////////////////
  // private

  private static final float degToRad = (float)Math.PI/180.0f;

  private static float[] mul(float[] a, float[] b, float[] c) {
    float a00=a[ 0],a01=a[ 4],a02=a[ 8],a03=a[12],
          a10=a[ 1],a11=a[ 5],a12=a[ 9],a13=a[13],
          a20=a[ 2],a21=a[ 6],a22=a[10],a23=a[14],
          a30=a[ 3],a31=a[ 7],a32=a[11],a33=a[15];
    float b00=b[ 0],b01=b[ 4],b02=b[ 8],b03=b[12],
          b10=b[ 1],b11=b[ 5],b12=b[ 9],b13=b[13],
          b20=b[ 2],b21=b[ 6],b22=b[10],b23=b[14],
          b30=b[ 3],b31=b[ 7],b32=b[11],b33=b[15];
    c[ 0] = a00*b00+a01*b10+a02*b20+a03*b30;
    c[ 1] = a10*b00+a11*b10+a12*b20+a13*b30;
    c[ 2] = a20*b00+a21*b10+a22*b20+a23*b30;
    c[ 3] = a30*b00+a31*b10+a32*b20+a33*b30;
    c[ 4] = a00*b01+a01*b11+a02*b21+a03*b31;
    c[ 5] = a10*b01+a11*b11+a12*b21+a13*b31;
    c[ 6] = a20*b01+a21*b11+a22*b21+a23*b31;
    c[ 7] = a30*b01+a31*b11+a32*b21+a33*b31;
    c[ 8] = a00*b02+a01*b12+a02*b22+a03*b32;
    c[ 9] = a10*b02+a11*b12+a12*b22+a13*b32;
    c[10] = a20*b02+a21*b12+a22*b22+a23*b32;
    c[11] = a30*b02+a31*b12+a32*b22+a33*b32;
    c[12] = a00*b03+a01*b13+a02*b23+a03*b33;
    c[13] = a10*b03+a11*b13+a12*b23+a13*b33;
    c[14] = a20*b03+a21*b13+a22*b23+a23*b33;
    c[15] = a30*b03+a31*b13+a32*b23+a33*b33;
    return c;
  }
}
