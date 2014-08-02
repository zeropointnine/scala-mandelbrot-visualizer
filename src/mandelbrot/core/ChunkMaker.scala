package mandelbrot.core

import akka.actor.Actor
import mandelbrot.frontend.processing.App
import leelib.scala.processing.Util

object ChunkMaker
{
	case class Request(requestTime:Long, mandelStartX:Double, mandelStartY:Double, mandelStep:Double, rect:Rect)
	case class Result(req:Request, escapeValues:Array[Int])
}
class ChunkMaker extends Actor
{
	override def receive =
	{
		case chunkReq:ChunkMaker.Request => sender ! Calculator.calculateChunk(chunkReq)
	}
}

