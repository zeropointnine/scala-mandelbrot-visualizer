import scala.collection.mutable


package mandelbrot
{
	package object core
	{
		type Chunks = mutable.Queue[ChunkMaker.Result]


		case class Rect(x:Int, y:Int, width:Int, height:Int)


		trait Callbacks
		{
			def onFrameUpdated(percentComplete:Float)
			def onFrameComplete(elapsedMs:Int)
			def onColorRange(lower:Float, upper:Float)
		}
	}
}
