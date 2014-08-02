package mandelbrot.core

import akka.actor.{ActorRef, Props, Actor}
import mandelbrot.frontend.processing.App

object Drawer
{
	case object DrawPreview
	case object DrawPreviewComplete

	case object DrawLatest

	case object Redraw
	case object RedrawComplete
}

/**
 * Single entry-point for graphics drawing operations
 */
class Drawer extends Actor
{
	private val frameDrawer = context.actorOf(Props(new FrameDrawer), name="frameDrawer")
	private var drawFrameRequester:ActorRef = _

	private var isRedrawing:Boolean = _
	private var flagRedraw:Boolean = _
	private var flagDrawLatest:Boolean = _


	override def receive =
	{
		case Drawer.DrawLatest =>

			if (isRedrawing)
			{
				flagDrawLatest = true
			}
			else
			{
				App.instance.frameManager.drawLatest()
				App.instance.frameManager.updatePixels()
			}


		case Drawer.DrawPreview =>

			App.instance.frameManager.drawPreview()
			App.instance.frameManager.updatePixels()

			sender ! Drawer.DrawPreviewComplete

		case Drawer.Redraw =>

			if (isRedrawing)
			{
				flagRedraw = true
			}
			else
			{
				isRedrawing = true
				drawFrameRequester = sender
				frameDrawer ! FrameDrawer.Redraw
			}

		case FrameDrawer.RedrawComplete =>

			isRedrawing = false
			drawFrameRequester ! Drawer.RedrawComplete  // this maybe should move

			if (flagRedraw)
			{
				// println("doing extra redraw to make up for cancelled one")
				flagRedraw = false
				isRedrawing = true
				frameDrawer ! FrameDrawer.Redraw
			}
			else if (flagDrawLatest)
			{
				// println("doing deferred draw-latest")
				flagDrawLatest = false
				App.instance.frameManager.drawLatest()
				App.instance.frameManager.updatePixels()
			}
	}
}

object FrameDrawer
{
	case object Redraw
	case object RedrawComplete
}
class FrameDrawer extends Actor
{
	override def receive =
	{
		case FrameDrawer.Redraw =>

			App.instance.frameManager.redraw()
			App.instance.frameManager.updatePixels()
			sender ! FrameDrawer.RedrawComplete
	}
}
