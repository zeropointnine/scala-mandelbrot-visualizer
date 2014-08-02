package mandelbrot.core

import akka.actor.{ActorRef, Actor}
import leelib.scala.processing.Util
import java.util.{Timer, TimerTask}
import scala.annotation.tailrec

object PreviewMaker
{
	case class Request(mandelStartX:Double, mandelStartY:Double, numColumns:Int, numRows:Int, mandelStepX:Double, mandelStepY:Double, chunkWidth:Int, chunkHeight:Int)
	case class Result(req:Request, escapeValues:Array[Int])
	case object Cancel

	val MaxLoopTimeNs = 50 * 1e6
}

class PreviewMaker extends Actor
{
	private var isRunning:Boolean = _
	private var restartFlag:Boolean = _
	private var requestSender:ActorRef = _
	private var request:PreviewMaker.Request = _

	private var startTime:Long = _
	private var values:Array[Int] = _
	private var r:Int = _
	private var y:Double = _
	private var c:Int = _
	private var x:Double = _
	private var i:Int = _
	private var loopStartTime:Long = _

	override def receive =
	{
		case request:PreviewMaker.Request =>

			if (false) {
				sender ! calculatePreviewSync(request)
			}
			else {
				requestSender = sender
				calculatePreview(request)
			}

		case PreviewMaker.Cancel =>

			println("PreviewMaker - GOT CANCEL - isRunning? " + isRunning)
			restartFlag = true
	}

	def calculatePreview(p:PreviewMaker.Request)
	{
		request = p

		if (isRunning)
		{
			println("PreviewMaker.calculatePreview() - is currently running; will restart at next 'pause'")
			restartFlag = true
			return
		}

		isRunning = true

		startTime = System.nanoTime()
		values = new Array[Int](p.numRows * p.numColumns)
		i = 0

		r = 0
		y = request.mandelStartY  +  request.mandelStepY/2f

		c = 0
		x = p.mandelStartX  +  p.mandelStepX/2f

		loopStartTime = System.nanoTime()
		next()
	}

	@tailrec
	private def next()
	{
		if (restartFlag)
		{
			println("PreviewMaker.next() - restartFlag is true, restarting")
			restartFlag = false
			isRunning = false
			calculatePreview(request)
			return
		}

		var pauseFlag = false

		values(i) = Calculator.calculateEscapeValue(x,y)
		i += 1

		c += 1
		x += request.mandelStepX

		if (c < request.numColumns)
		{
			if (System.nanoTime() - loopStartTime >  PreviewMaker.MaxLoopTimeNs) pauseFlag = true
		}
		else
		{
			// do next row

			c = 0
			x = request.mandelStartX  +  request.mandelStepX/2f

			r += 1
			y += request.mandelStepY

			if (r < request.numRows)
			{
				if (System.nanoTime() - loopStartTime >  PreviewMaker.MaxLoopTimeNs) pauseFlag = true
			}
			else
			{
				// println(s"PreviewMaker - FINISHED ${Util.elapsedMs(startTime)}ms")

				isRunning = false
				requestSender ! new PreviewMaker.Result(request, values)
				return
			}
		}

		if (pauseFlag) {
			loopStartTime = System.nanoTime()
			new Timer().schedule(new TimerTask { def run() { doNext() } }, 1)  // avoid compiler error about tail recursion here
		}
		else {
			next()  // hopefully this qualifies
		}
	}

	private def doNext() = next()

	/**
	 * Original (simpler) synchronous version
	 */
	def calculatePreviewSync(p:PreviewMaker.Request): PreviewMaker.Result =
	{
		val start = System.nanoTime()

		val values = new Array[Int](p.numRows * p.numColumns)

		var i = 0

		var y = p.mandelStartY  +  p.mandelStepY/2f  // sampling from middle of 'square' rather than top-left
		for (r <- 0 until p.numRows)
		{
			var x = p.mandelStartX  +  p.mandelStepX/2f
			for (c <- 0 until p.numColumns)
			{
				values(i) = Calculator.calculateEscapeValue(x,y)
				x += p.mandelStepX
				i += 1
			}
			y += p.mandelStepY
		}

		println(s"calculatePreviewSync() ${Util.elapsedMs(start)}ms")

		new PreviewMaker.Result(p, values)
	}
}
