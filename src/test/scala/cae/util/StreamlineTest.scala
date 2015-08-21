package cae.util

object StreamlineTest {
  def main(args: Array[String]) {
    var i1 = 0.0f
    var i2 = 0.0f
    var i3 = 0.0f
    var stream = new Streamline(i1,i2,i3)
    for (i <- 0 until 30) {
      stream.append(i,i,i)
    }
  }
}
