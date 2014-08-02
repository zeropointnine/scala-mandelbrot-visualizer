package leelib.scala.processing

object Util
{
	def elapsedMs(nanoTime:Long):Int = math.round((System.nanoTime() - nanoTime) / 1e6).toInt
}
