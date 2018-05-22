/**
  * Created by Taehee on 2018. 5. 19..
  */


import breeze.linalg.SparseVector


object FtrlRun {



  def mapToSparseVector(kv : Map[Int, Double], n:Int) = {
    val vec = SparseVector.zeros[Double](n)
    kv.keys.foreach(i => vec(i) = kv(i))
    vec
  }

  def rGaussian(n:Int) = {
    (1 to n).map(x=> util.Random.nextGaussian())
  }

  def rBernoulli(n:Int, p:Double) = {
    (1 to n).map(x=> if(util.Random.nextInt(10000) <= p * 10000) 1 else 0)
  }




  def logisticSample(coef:SparseVector[Double]) = {

    val sample = coef.index.map(x=> (x, rGaussian(1)(0))).toMap
    val feature = mapToSparseVector(sample, coef.length)
    val label = rBernoulli(1, sigmoid(coef.dot(feature)))(0)
    (label, feature)

  }

  def nLogisticSample(n:Int, coef:SparseVector[Double]) = {
    (1 to n).map(x=> logisticSample(coef))
  }

  def makeCoef() = {
    (1 to 3).map(x=> (util.Random.nextInt(5000), rGaussian(1)(0) * 0.01 + 5)).toMap
  }


  def linear(w : SparseVector[Double], x:SparseVector[Double]) = {
    w.dot(x)
  }


  def sigmoid(a:Double) = {
    1 / (1 + math.exp(-a))
  }




  def main(args: Array[String]): Unit = {


    val coef = Map(0 -> 1.0, 1-> 2.0, 2->3.0, 3-> 4.0, 4-> 5.0, 5-> 0D, 9->0D)

    val coef2 = Map(0 -> 1.0, 1-> 2.0, 2->3.0, 3-> 4.0, 4-> 5.0, 6-> 15D, 8->7D)

    println(mapToSparseVector(coef, Int.MaxValue))//.index.max)
    //val sampledData = nLogisticSample(500000, mapToSparseVector(coef, Int.MaxValue)).toArray
    //val sampledData2 = nLogisticSample(500000, mapToSparseVector(coef2, Int.MaxValue)).toArray
    //val data =sampledData.union(sampledData2)
    //val data = nLogisticSample(1000000, mapToSparseVector(makeCoef(), Int.MaxValue)).toArray
    //println(data.mkString("\n"))

    val data = (1 to 100000).
      map(x=> makeCoef()).
      map(x=> nLogisticSample(1, mapToSparseVector(x, Int.MaxValue))).
      flatten.toArray
    //println(data.mkString("\n"))
    //val initialWeight = SparseVector.zeros[Double](coef.keys.max + 1)
    //gradientDescent(data, 10, 1, 2, initialWeight, 1, 0.00001)

    val opt1 = new Ftrl().setAlpha(2).setBeta(1).setLambda(2)
    val opt2 = new Ogd()

    val opt = Array(opt1, opt2)

    var i = 1
    var correct = Array.fill(2)(0)
    data.foreach{x=>

      val pred = Array(
        opt1.predictLabel(x._2, 0.5),
        opt2.predictLabel(x._2, 0.5)
      )

      val aa = Array(
        opt1.update(x._1, x._2),
        opt2.update(x._1, x._2)
      )

      val ss = pred.map{c=>
        if (c == x._1) 1 else 0
      }

      correct = correct.zip(ss).map(k => k._1 + k._2)

      val gg = correct.map(k=> k.toDouble / i.toDouble).map(k=> k - (k % 0.0001))

      if (i % 1000 == 0) println(gg.mkString("\t"), i)
      //if (i % 1000 == 0) println(opt2.i, opt2.n, opt2.weight, i)
      i += 1
    }




  }
}
