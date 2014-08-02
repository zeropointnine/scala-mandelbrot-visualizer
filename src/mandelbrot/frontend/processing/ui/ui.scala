package mandelbrot.frontend.processing.ui


import mandelbrot.core.Config
import leelib.scala.processing.View
import java.text.DecimalFormat
import mandelbrot.frontend.processing.App
import leelib.tween._
import processing.event.MouseEvent
import processing.core.{PApplet, PConstants}


object Ui
{
	val titleFont = View.p.loadFont("FuturaStd-Bold-12.vlw")
	val copyFont = View.p.loadFont("FuturaStd-Medium-10.vlw")
	val smallFont = View.p.loadFont("FuturaStd-Medium-10.vlw")
	val decimalFormat = new DecimalFormat("0.##################E0")

	val BgColor = 0xaa000000
	val Height1 = 75
	val Height2 = 53
	val Height  = Height1 + 1 + Height2
	val Width = 10+240+240+210

	var closeTween = new TweeningNumber(0)
}

class Ui extends View
{
	val closeButton = new CloseButton
	val statusView = new StatusView
	val positionView = new PositionView
	val zoomView = new ZoomView
	val threshView = new ThreshView
	val colorView = new ColorView

	val openY = Config.ShellHeight - Ui.Height - 1
	val closedY = Config.ShellHeight

	ctor()

	private def ctor()
	{
		width = Ui.Width
		height = Ui.Height

		x = 1

		closeButton.x = 0
		closeButton.y = -closeButton.height - 1
		closeButton.pressDelegate = (e:MouseEvent) => toggleClose()
		addChild(closeButton)

		statusView.x = closeButton.x + closeButton.width + 10
		statusView.y = -statusView.height - 1
		addChild(statusView)

		positionView.x = 10
		positionView.y = 0
		addChild(positionView)

		zoomView.x = 10+240
		zoomView.y = 0
		addChild(zoomView)

		threshView.x = 10+240+240
		threshView.y = 0
		addChild(threshView)

		colorView.x = 10
		colorView.y = Ui.Height1 + 1
		addChild(colorView)
	}


	override def y:Float = PApplet.map(Ui.closeTween(), 0,1, openY,closedY)

	override def drawSelf()
	{
		View.p.noStroke()
		View.p.fill(Ui.BgColor)

		View.p.rect(0,0, width, Ui.Height1)
		View.p.rect(0,Ui.Height1 + 1, width, Ui.Height2)
	}

	def toggleClose()
	{
		if (Ui.closeTween() == 0)
			Ui.closeTween.to(1, 333, Ease.outCubic)
		else
			Ui.closeTween.to(0, 333, Ease.outCubic)
	}
}

class CloseButton extends View
{
	width = 20
	height = 20

	registerMouseEvents()

	override def drawSelf()
	{
		View.p.noStroke()
		View.p.fill(Ui.BgColor)
		View.p.rect(0,0, width,height)

		View.p.noFill()
		View.p.strokeWeight(1.5f)

		// crossfade action

		if (Ui.closeTween() > 0)
		{
			val alpha = (0x88 * Ui.closeTween()).toInt << 24
			View.p.stroke(alpha + 0xffffff)

			val lx = 4
			val rx = width-1 - 4
			val mx = width/2
			val ty = 6
			val by = height-1 - 6
			View.p.line(lx,by, mx,ty)
			View.p.line(mx,ty, rx,by)
		}

		if (Ui.closeTween() < 1)
		{
			val alpha = (0x88 * (1 - Ui.closeTween())).toInt << 24
			View.p.stroke(alpha + 0xffffff)

			val lx = 6
			val rx = width-1 - 6
			val ty = 6
			val by = height-1 - 6
			View.p.line(lx,ty, rx,by)
			View.p.line(lx,by, rx,ty)
		}

	}
}

object StatusView
{
	val Width = 100
	val Height = 20
}
class StatusView extends View
{
	var text = "Hello text"

	width = StatusView.Width
	height = StatusView.Height

	override def drawSelf()
	{
		/*
		View.p.noStroke()
		View.p.fill(0x88000000)
		View.p.rect(0,0, width,height)
		*/

		View.p.fill(255,255,255, (.75 * 255).toInt)
		View.p.textFont(Ui.smallFont)
		View.p.text(text, 0, 13)

		// View.p.textAlign(PConstants.RIGHT);
		// View.p.textAlign(PConstants.LEFT);
	}
}

class PositionView extends View
{
	var posX:Double = _
	var posY:Double = _

	val upKey:KeyButton = new KeyButton("up")
	upKey.selectDelegate = App.instance.moveUp
	upKey.x = 30
	upKey.y = 10
	addChild(upKey)

	val leftKey:KeyButton = new KeyButton("lt")
	leftKey.selectDelegate = App.instance.moveLeft
	leftKey.x = 0
	leftKey.y = 40
	addChild(leftKey)

	val downKey:KeyButton = new KeyButton("dn")
	downKey.selectDelegate = App.instance.moveDown
	downKey.x = 30
	downKey.y = 40
	addChild(downKey)

	val rightKey:KeyButton = new KeyButton("rt")
	rightKey.selectDelegate = App.instance.moveRight
	rightKey.x = 60
	rightKey.y = 40
	addChild(rightKey)

	override def drawSelf()
	{
		View.p.fill(255,255,255, (.66 * 255).toInt)

		View.p.textFont(Ui.titleFont)
		View.p.text("POSITION", 100, 30)

		View.p.textFont(Ui.copyFont)
		View.p.text(posX.toString, 100, 45)
		View.p.text(posY.toString, 100, 45+14)
	}
}

class ZoomView extends View
{
	var pixelSize:Double = _
	var zoom:Float = _

	val outKey:KeyButton = new KeyButton("-")
	outKey.selectDelegate = App.instance.zoomOut
	outKey.x = 0
	outKey.y = 26
	addChild(outKey)

	val inKey:KeyButton = new KeyButton("+")
	inKey.selectDelegate = App.instance.zoomIn
	inKey.x = 30
	inKey.y = 26
	addChild(inKey)

	override def drawSelf()
	{
		View.p.fill(255,255,255, (.66 * 255).toInt)

		View.p.textFont(Ui.titleFont)
		View.p.text("ZOOM", 73, 30)

		View.p.textFont(Ui.copyFont)
		View.p.text("1PX = " + Ui.decimalFormat.format(pixelSize), 73, 45)
		View.p.text("(" + f"$zoom%.1f" + "X)", 73, 45+14)
	}
}

class ThreshView extends View
{
	var escapeThreshold:Int = _

	private val decreaseKey:KeyButton = new KeyButton("[")
	decreaseKey.selectDelegate = App.instance.decreaseEscapeThresh
	decreaseKey.selectIntervalMax = Int.MaxValue
	decreaseKey.selectIntervalMin = Int.MaxValue
	decreaseKey.x = 0
	decreaseKey.y = 26
	addChild(decreaseKey)

	private val increaseKey:KeyButton = new KeyButton("]")
	increaseKey.selectDelegate = App.instance.increaseEscapeThresh
	increaseKey.selectIntervalMax = Int.MaxValue
	increaseKey.selectIntervalMin = Int.MaxValue
	increaseKey.x = 30
	increaseKey.y = 26
	addChild(increaseKey)

	override def drawSelf()
	{
		View.p.fill(255,255,255, (.66 * 255).toInt)

		View.p.textFont(Ui.titleFont)
		View.p.text("ESCAPE THRESHOLD", 73, 36)

		View.p.textFont(Ui.copyFont)
		View.p.text(escapeThreshold.toString, 73, 36+14)
	}
}
