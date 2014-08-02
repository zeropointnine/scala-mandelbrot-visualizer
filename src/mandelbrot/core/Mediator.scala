package mandelbrot.core

import akka.actor.{Props, Actor}
import akka.routing.RoundRobinRouter
import mandelbrot.frontend.FrameManager
import leelib.scala.processing.Util

/**
 * Is the bottleneck to all messaging.
 * Any work done by actor must be very short, or will decrease responsiveness
 *
 */
object Mediator
{
	case object Init
	case object Start
	case object Redraw
	case object NormalizeColorRange

	var instance:Mediator = _
}

class Mediator(val callbacks:Callbacks, val frameManager:FrameManager) extends Actor
{
	private var mandelbrotView:MandelbrotView = _

	private var previewStartTime:Long = _
	private var frameStartTime:Long = _

	private var chunksComplete:Int = _

	private var numChunksPending:Int = _

	// Allocating 1 more than the normal number of workers that we'll be using.
	// Idea is to start a new job quicker while one was already pending and has all the normal number of workers occupied.
	private val workerRouter = context.actorOf(
		Props[ChunkMaker].withRouter(RoundRobinRouter(Config.NumChunkMakers + 1)), name="workerRouter")

	private val previewMaker = context.actorOf(Props(new PreviewMaker), name="previewMaker")
	private val drawer = context.actorOf(Props(new Drawer), name="drawer")

	private var nextDrawBufferTime:Long = _
	
	Mediator.instance = this // TODO: bad


	override def receive() =
	{
		// [4] Got chunk result. Add it to buffer, etc.
		case pChunk:ChunkMaker.Result => handleChunk(pChunk)

		// [3] Preview drawn. Start getting chunks.
		case Drawer.DrawPreviewComplete =>

			frameManager.resetFrame(mandelbrotView)
			chunksComplete = 0

			val ns = System.nanoTime()
			frameStartTime = ns
			nextDrawBufferTime = ns + Config.DrawBufferIntervalNs

			// Request a chunk no matter what
			doNextChunk()
			// And ensure all workers are employed
			while (numChunksPending < Config.NumChunkMakers) doNextChunk()

		// [2] Got preview results. Draw preview.
		case preview:PreviewMaker.Result =>

			frameManager.preview = preview
			drawer ! Drawer.DrawPreview

		// [1] Got view specs from App. Create preview.
		case pView:MandelbrotView =>

			mandelbrotView = pView
			previewStartTime = System.nanoTime()
			nextDrawBufferTime = Long.MaxValue

			previewMaker ! new PreviewMaker.Request(
				mandelbrotView.xMin, mandelbrotView.yMin,
				frameManager.chunkRequester.numChunkColumns, frameManager.chunkRequester.numChunkRows,
				mandelbrotView.step * frameManager.chunkRequester.chunkWidth, mandelbrotView.step * frameManager.chunkRequester.chunkHeight,
				frameManager.chunkRequester.chunkWidth, frameManager.chunkRequester.chunkHeight)  // wtf

		case Mediator.Redraw =>

			drawer ! Drawer.Redraw

		case Drawer.RedrawComplete =>

		case Mediator.NormalizeColorRange =>

			// THIS IS STILL FUCKED
			// ...

			// how to send message to self?  sender ! Mediator.DrawFrame
			frameManager.redraw()

		case 666 =>

		case any:Any => throw new Exception("No: " + any)
	}

	private def handleChunk(chunk:ChunkMaker.Result)
	{
		numChunksPending -= 1

		if (chunk.req.requestTime < previewStartTime) {  // 'expired'
			// println("Mediator got expired chunk result")
			return;
		}

		chunksComplete += 1
		frameManager.addChunk(chunk)

		// println(s"numComplete $chunksComplete numRequests ${chunkRequester.totalNum} numLeft ${chunkRequester.numLeft} numPending $numChunksPending")

		val ns = System.nanoTime()
		if (ns > nextDrawBufferTime) {
			drawer ! Drawer.DrawLatest
			nextDrawBufferTime = ns + Config.DrawBufferIntervalNs
		}

		callbacks.onFrameUpdated(chunksComplete / frameManager.chunkRequester.totalNum.toFloat)

		if (frameManager.chunkRequester.numLeft == 0)
		{
			if (numChunksPending > 0)
			{
				// println(s"Mediator got chunk result; no more chunks to send off; $numChunksPending chunks pending")
			}
			else
			{
				// TODO: wtf - chunksComplete is under by one here
				println(s"Mediator.receive() => ChunkMaker.Result - DONE - ${Util.elapsedMs(frameStartTime)}ms - chunksComplete $chunksComplete")
				// println(s"min ${frameManager.minEscape} max ${frameManager.maxEscape}")

				drawer ! Drawer.DrawLatest

				callbacks.onFrameComplete(Util.elapsedMs(frameStartTime))
			}
		}
		else
		{
			while (frameManager.chunkRequester.numLeft > 0 && numChunksPending < Config.NumChunkMakers)
			{
				doNextChunk()
			}
		}
	}

	private def doNextChunk()
	{
		val req = frameManager.chunkRequester.nextChunkRequest
		if (req == null) return  // bad

		numChunksPending += 1

		workerRouter ! req
	}
}
