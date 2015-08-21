package cae.la

object MatrixTest {
  def main(args: Array[String]) {
    var a = new Matrix(5,5)
    a.eye()
    println(a)
    println(a.isUpperTriangular())
    println(a.isLowerTriangular())
  }
}
