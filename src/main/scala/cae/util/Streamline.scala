package cae.util

/**
 * A streamline.
 * @param i1 seed location in dimension 1.
 * @param i2 seed location in dimension 2.
 * @param i3 seed location in dimension 3.
 */
class Streamline(i1: Float, i2: Float, i3: Float) {
  
  var nf = 0
  var nb = 0
  var sf = new Array[Float](64)
  var sb = new Array[Float](64)

  def append(d1: Float, d2: Float, d3: Float) {
    if (3*nf+2>=sf.length) {
      val t = new Array[Float](sf.length*2)
      System.arraycopy(sf,0,t,0,sf.length)
      sf = t
    }
    sf(3*nf+0) = d1
    sf(3*nf+1) = d2
    sf(3*nf+2) = d3
    nf += 1
  }

  def prepend(d1: Float, d2: Float, d3: Float) {
    if (3*nb+2>=sb.length) {
      val t = new Array[Float](sb.length*2)
      System.arraycopy(sb,0,t,0,sb.length)
      sb = t
    }
    sb(3*nb+0) = d1
    sb(3*nb+1) = d2
    sb(3*nb+2) = d3
    nb += 1
  }

  def collapse() {
    val x = new Array[Float](sf.length+sb.length)
    for (i <- 0 until sb.length)
      x(i) = sb(sb.length-i-1)
    for (i <- 0 until sf.length)
      x(i+sb.length-1) = sf(i)
  }
}
