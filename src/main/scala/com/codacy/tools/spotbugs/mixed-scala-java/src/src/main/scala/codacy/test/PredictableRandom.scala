import scala.util.Random

object PredictableRandom {

  def generateSecretToken(): java.lang.Boolean = {
    val result = Seq.fill(16)(Random.nextInt + 2)
    result.map("%02x" format _).mkString
    null
  }

}