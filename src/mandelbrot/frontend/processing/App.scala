package mandelbrot.frontend.processing


import akka.actor.{ActorSystem, Props}
import processing.core.{PImage, PConstants, PApplet}
import processing.event.KeyEvent
import mandelbrot.core._
import mandelbrot.frontend.{FrameManager}
import mandelbrot.frontend.processing.ui._
import leelib.scala.processing.View
import mandelbrot.{IndexedColors, ColorFunctions}


object App
{
	var instance:App = _

	def main(args: Array[String])
	{
		val s = getClass.getCanonicalName  // eg, "mazeApp.MazeApp$"
		val id  = if (s.last == '$') s.init else s
		PApplet.main(Array[String](id))
	}
}

class App extends PApplet with Callbacks
{
	val frameManager = new FrameManager(Config.ShellWidth, Config.ShellHeight)

	private val actorSystem = ActorSystem("MandelbrotSystem")
	private val mandelActor = actorSystem.actorOf(Props( new Mediator(this, frameManager) ), name="master")

	private val mandelbrotPos = new MandelbrotView(Config.ShellWidth, Config.ShellHeight)
	private var colorSchemeIndex:Int = _
	private var colorSchemes:List[ (String, (Float)=>Int) ] = _
	private var escapeThresholdFloat:Float = _

	private var ui:Ui = _

	App.instance = this // har


	override def setup()
	{
		size(Config.ShellWidth, Config.ShellHeight, PConstants.OPENGL)
		// if (frame != null) frame.setResizable(true)

		View.p = this
		ui = new Ui  // cannot be instantiated before setup()

		colorSchemes =
			("RED TO CYAN, HUE LERPED", IndexedColors.makeWithLerpedColors(0xff0000, 0x00ffff, PConstants.HSB, 1024).ratioToColor _) ::
			("Gray", ColorFunctions.gray _) ::
			("Blue Red Yellow", IndexedColors.makeWithImage("gradient_blue_red_yellow.png").ratioToColor _) ::
			("Yellow Blue Various", IndexedColors.makeWithImage("gradient_yellowmultiblue.png").ratioToColor _) ::
			("Slate to Copper", IndexedColors.makeWithImage("gradient_slatecopper.png").ratioToColor _) ::
			("Lime to Pink", IndexedColors.makeWithImage("candy1.png").ratioToColor _) ::
			("Sky Blue to White", IndexedColors.makeWithImage("gradient_blue_to_white.png").ratioToColor _) ::
			("Iroiro 1", IndexedColors.makeWithImage("various1.png").ratioToColor _) ::
			// ("Blue to Green, Lerped", ColorFunctions.greenToBlue _) ::
			Nil

		frameManager.transformFunction = colorSchemes(colorSchemeIndex)._2

		Config.escapeThreshold = 250
		escapeThresholdFloat = Config.escapeThreshold

		updateUiValues(true)

		// RANDOM POINTS OF INTEREST:
		// TODO: possibly add to UI
		mandelbrotPos.setPosition(-1.023757625175305, -0.35703258690436374, 2.688881434776663E-17)  // extreme zoom-in, glitch-action, limits of Float!
		// mandelbrotPos.setPosition(0.2500098592373869, 0.0, 1.0432211481373952E-9) // elephant valley, extreme zoom-in (not interesting)
		// mandelbrotPos.setPosition(0.31150820528911133, 0.02717402708297212, 2.2162437281928722E-9) // my test coords (cobwebs)
		// mandelbrotPos.setPosition(-1.7945182953133427, 0.0, 2.6588814358957506E-4) // left tip, nice
		// mandelbrotPos.setPosition(-0.7519042141584763, -0.056189744139429953, 4.738210751727511E-6)  // horseshoe valley
		// mandelbrotPos.setPosition(-0.7547363578387823, -0.05522911465536986, 9.174720572097596E-11)  // horseshoe vallue, zoom-in nice
		// mandelbrotPos.setPosition(-0.7499262858840302, 0.012319750525780977, 6.706188475040683E-8)  // good high-threshold test, lots of detail and very slow
		// mandelbrotPos.setPosition(-0.4296875, -0.5859375, 3.90625E-4) // TEST
		// mandelbrotPos.setPosition(0,0, 2.7 / Config.ShellHeight.toFloat ) // default
		// mandelbrotPos.setPosition(-0.7547363578387823, -0.05522911465536986, 8.901060106540721E-9) good medium-threshold test; thresh 1700

		mandelActor ! mandelbrotPos
	}

	override def draw()
	{
		background(0x444444)

		View.p.tint(255,255,255, 255);
		this.image(frameManager.image, 0,0)

		ui.draw()
	}

	override def keyPressed(e:KeyEvent)
	{
		// println(key.getNumericValue + " " + e.getKeyCode)

		e.getKeyCode match
		{
			case PConstants.ESC =>
				key = '`'  // deprecated workaround to cancel quit behavior
				ui.toggleClose()

			// translate (cursor keys)
			case 38 => moveUp()
			case 40 => moveDown()
			case 37 => moveLeft()
			case 39 => moveRight()

			// zoom (plus, minus)
			case 61 => zoomIn()
			case 45 => zoomOut()

			// escape thresh (bracket keys)
			case 93 => increaseEscapeThresh()
			case 91 => decreaseEscapeThresh()

			case 67 =>  nextColorScheme(false) // c
			case 88 =>  nextColorScheme(true) // x

			case 86 => // v
				mandelActor ! Mediator.NormalizeColorRange

			case 90 => // z

				// TEST
				mandelActor ! 666


			// color mapping
			/*
			case 65 => // a/z
				imageManager.mapMin += 0.1f
				println( f"colorizer min ${imageManager.mapMin} max ${imageManager.mapMax}" )
				mandelActor ! Mediator.DrawFrame
			case 90 =>
				imageManager.mapMin -= 0.1f
				println( f"colorizer min ${imageManager.mapMin} max ${imageManager.mapMax}" )
				mandelActor ! Mediator.DrawFrame

			case 83 => // s/x
				imageManager.mapMax += 0.1f
				println( f"colorizer min ${imageManager.mapMin} max ${imageManager.mapMax}" )
				mandelActor ! Mediator.DrawFrame
			case 88 =>
				imageManager.mapMax -= 0.1f
				println( f"colorizer min ${imageManager.mapMin} max ${imageManager.mapMax}" )
				mandelActor ! Mediator.DrawFrame
			*/

			case _ => ()
		}
	}

	override def onFrameUpdated(percentComplete:Float)
	{
		ui.statusView.text = f"${percentComplete * 100}%.2f" + "%"
	}

	override def onFrameComplete(elapsedMs:Int)
	{
		println(s"App.onMandelImageComplete($elapsedMs" + "ms)")

		ui.statusView.text = elapsedMs + "ms"

		// this is not helpful:
		// mandelActor ! MandelActor.GetColorRange
	}

	override def onColorRange(lower:Float, upper:Float)
	{
		println(s"App.onColorRange($lower, $upper)")
		frameManager.mapMin = lower
		frameManager.mapMax = upper
		mandelActor ! Mediator.Redraw
	}

	def moveUp()
	{
		mandelbrotPos.setPosition(mandelbrotPos.xCenter, mandelbrotPos.yCenter - mandelbrotPos.step * 100, mandelbrotPos.step)
		mandelActor ! mandelbrotPos
		updateUiValues()
	}

	def moveDown()
	{
		mandelbrotPos.setPosition(mandelbrotPos.xCenter, mandelbrotPos.yCenter + mandelbrotPos.step * 100, mandelbrotPos.step)
		mandelActor ! mandelbrotPos
		updateUiValues()
	}

	def moveLeft()
	{
		mandelbrotPos.setPosition(mandelbrotPos.xCenter - mandelbrotPos.step * 100, mandelbrotPos.yCenter, mandelbrotPos.step)
		mandelActor ! mandelbrotPos
		updateUiValues()
	}

	def moveRight()
	{
		mandelbrotPos.setPosition(mandelbrotPos.xCenter + mandelbrotPos.step * 100, mandelbrotPos.yCenter, mandelbrotPos.step)
		mandelActor ! mandelbrotPos
		updateUiValues()
	}

	def zoomIn()
	{
		mandelbrotPos.setPosition(mandelbrotPos.xCenter, mandelbrotPos.yCenter, mandelbrotPos.step * 1/1.10)
		mandelActor ! mandelbrotPos
		updateUiValues()
	}

	def zoomOut()
	{
		mandelbrotPos.setPosition(mandelbrotPos.xCenter, mandelbrotPos.yCenter, mandelbrotPos.step * 1.10)
		mandelActor ! mandelbrotPos
		updateUiValues()
	}

	def increaseEscapeThresh()
	{
		escapeThresholdFloat *= 1.10f
		Config.escapeThreshold = escapeThresholdFloat.toInt // ie, to-Int
		mandelActor ! mandelbrotPos
		updateUiValues()
	}

	def decreaseEscapeThresh()
	{
		escapeThresholdFloat *= 1/1.10f
		Config.escapeThreshold = escapeThresholdFloat.toInt
		mandelActor ! mandelbrotPos
		updateUiValues()
	}

	def nextColorScheme(back:Boolean)
	{
		if (back) {
			colorSchemeIndex -= 1
			if (colorSchemeIndex < 0) colorSchemeIndex = colorSchemes.length - 1
		}
		else {
			colorSchemeIndex += 1
			if (colorSchemeIndex >= colorSchemes.length) colorSchemeIndex = 0
		}

		frameManager.transformFunction = colorSchemes(colorSchemeIndex)._2
		updateUiValues(true)
		mandelActor ! Mediator.Redraw
	}

	def updateColorMappings(min:Float, max:Float)
	{
		frameManager.mapMin = min
		frameManager.mapMax = max
		mandelActor ! Mediator.Redraw
	}

	private def updateUiValues(redrawSpectrumButton:Boolean=false)
	{
		ui.positionView.posX = mandelbrotPos.xCenter
		ui.positionView.posY = mandelbrotPos.yCenter

		ui.zoomView.pixelSize = mandelbrotPos.step
		val defaultStep = 3 / Config.ShellHeight.toFloat
		ui.zoomView.zoom = (defaultStep / mandelbrotPos.step).toFloat

		ui.threshView.escapeThreshold = Config.escapeThreshold

		ui.colorView.name = colorSchemes(colorSchemeIndex)._1

		if (redrawSpectrumButton) ui.colorView.colorWidget.colorBar.updateImage()
	}
}
