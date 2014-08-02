package mandelbrot.frontend.processing.ui


import leelib.scala.processing.View
import mandelbrot.frontend.processing.App
import processing.event.MouseEvent
import processing.core.PImage


class ColorView extends View
{
	var name:String = _
	val colorWidget = new ColorWidget
	private val nextColorKey = new KeyButton("C")
	private val previousColorKey = new KeyButton("X")

	width = Ui.Width

	previousColorKey.selectDelegate = () => App.instance.nextColorScheme(true)
	previousColorKey.x = 0
	previousColorKey.y = 15
	previousColorKey.selectIntervalMax = Int.MaxValue
	previousColorKey.selectIntervalMin = Int.MaxValue
	addChild(previousColorKey)

	nextColorKey.selectDelegate = () => App.instance.nextColorScheme(false)
	nextColorKey.x = 30
	nextColorKey.y = 15
	nextColorKey.selectIntervalMax = Int.MaxValue
	nextColorKey.selectIntervalMin = Int.MaxValue
	addChild(nextColorKey)

	colorWidget.x = 365
	colorWidget.y = 15
	addChild(colorWidget)

	override def drawSelf()
	{
		View.p.fill(255,255,255, (.66 * 255).toInt)

		View.p.textFont(Ui.titleFont)
		View.p.text("COLOR SCHEME", 68, 25)

		View.p.textFont(Ui.copyFont)
		View.p.text(name, 68, 25+14)
	}
}

object ColorWidget
{
	val Width = 155
	val Height = 22
}
class ColorWidget extends View
{
	var leftRatio:Float = 0
	var rightRatio:Float = 1

	var _leftPx:Float = _
	var _rightPx:Float = _

	val leftHandle = new ColorBarHandle(false)
	val rightHandle = new ColorBarHandle(true)

	val colorBar = new ColorBar

	private var leftButtonDragStartLeftPx:Float = _
	private var leftButtonDragStartMouseX:Float = _
	private var rightButtonDragStartRightPx:Float = _
	private var rightButtonDragStartMouseX:Float = _
	private var spectrumButtonDragStartLeftPx:Float = _
	private var spectrumButtonDragStartMouseX:Float = _

	ctor()

	private def ctor()
	{
		width = ColorWidget.Width
		height = ColorWidget.Height

		colorBar.y = 2
		addChild(colorBar)

		leftHandle.y = 1
		addChild(leftHandle)

		rightHandle.y = 1
		addChild(rightHandle)

		leftPx = leftRatio * ColorWidget.Width
		rightPx = rightRatio * ColorWidget.Width
	}

	override def drawSelf()
	{
		View.p.noFill()
		View.p.strokeWeight(.5f)
		View.p.stroke(0xccffffff)
		View.p.rect(0,0, width,height-1)

		View.p.fill(255,255,255, (.75 * 255).toInt)
		View.p.textFont(Ui.smallFont)
		View.p.text("0", 0-3, height+10)
		View.p.text("max", width-10, height+10)
	}

	protected def leftPx = _leftPx
	protected def leftPx_=(f:Float)
	{
		_leftPx = f
		leftHandle.x = _leftPx - leftHandle.width
		colorBar.x = _leftPx
		colorBar.width = _rightPx - _leftPx
	}

	protected def rightPx = _rightPx
	protected def rightPx_=(f:Float)
	{
		_rightPx = f
		rightHandle.x = _rightPx
		colorBar.width = _rightPx - _leftPx
	}

	private def updateColorMappings()
	{
		// visually, this is distance of the left edge of the widget-stroked-bar from the left edge of color-bar
		// over the width of the color-bar
		val colorWidgetLeft = 0
		val min = (colorWidgetLeft - _leftPx) / colorBar.width

		// distance of right edge of widget-stroked-bar from the left edge of color-bar
		// over the width of the color-bar
		val colorWidgetRight = ColorWidget.Width
		val max = (colorWidgetRight - _leftPx) / colorBar.width

		App.instance.updateColorMappings(min, max)
	}

	//

	leftHandle.pressDelegate = (e:MouseEvent) =>
	{
		leftButtonDragStartLeftPx = _leftPx
		leftButtonDragStartMouseX = e.getX
	}

	leftHandle.dragDelegate = (e:MouseEvent) =>
	{
		val dmx = e.getX() - leftButtonDragStartMouseX
		var l = leftButtonDragStartLeftPx + dmx
		if (l < ColorWidget.Width * -1) l = ColorWidget.Width * -1
		if (l > ColorWidget.Width * 1) l = ColorWidget.Width * 1
		if (rightPx - l > ColorWidget.Width * 2) l = rightPx - ColorWidget.Width * 2
		if (l >= rightPx) l = rightPx
		leftPx = l

		updateColorMappings()
	}

	leftHandle.releaseDelegate = (e:MouseEvent) =>
	{
		updateColorMappings()
	}

	//

	rightHandle.pressDelegate = (e:MouseEvent) =>
	{
		rightButtonDragStartRightPx = _rightPx
		rightButtonDragStartMouseX = e.getX
	}

	rightHandle.dragDelegate = (e:MouseEvent) =>
	{
		val dmx = e.getX() - rightButtonDragStartMouseX
		var r = rightButtonDragStartRightPx + dmx
		if (r < ColorWidget.Width * 0) r = ColorWidget.Width * 0
		if (r > ColorWidget.Width * 2) r = ColorWidget.Width * 2
		if (r - leftPx > ColorWidget.Width * 2) r = leftPx + ColorWidget.Width * 2
		if (r <= leftPx) r = leftPx
		rightPx = r

		updateColorMappings()
	}

	rightHandle.releaseDelegate = (e:MouseEvent) =>
	{
		updateColorMappings()
	}

	//

	colorBar.pressDelegate = (e:MouseEvent) =>
	{
		spectrumButtonDragStartLeftPx = _leftPx
		spectrumButtonDragStartMouseX = e.getX
	}

	colorBar.dragDelegate = (e:MouseEvent) =>
	{
		val dmx = e.getX() - spectrumButtonDragStartMouseX
		var l = spectrumButtonDragStartLeftPx + dmx

		if (l < ColorWidget.Width * -1) l = ColorWidget.Width * -1
		if (l + colorBar.width > ColorWidget.Width * 2) l = ColorWidget.Width * 2 - colorBar.width

		if (l + colorBar.width < 0) l = -colorBar.width
		if (l > ColorWidget.Width) l = ColorWidget.Width

		val widthWas = rightPx - leftPx
		leftPx = l
		rightPx = l + widthWas

		updateColorMappings()
	}

	colorBar.releaseDelegate = (e:MouseEvent) =>
	{
		updateColorMappings()
	}
}

object ColorBar
{
	val ImageWidth = ColorWidget.Width * 3
	val Height = ColorWidget.Height - 4
}
class ColorBar extends View
{
	width = 1
	height = ColorBar.Height

	private val pImage = new PImage(ColorBar.ImageWidth, height.toInt)
	private var leftColor:Int = _
	private var rightColor:Int = _

	updateImage()
	registerMouseEvents()

	def updateImage()
	{
		for (x <- 0 until ColorBar.ImageWidth) {
			val rat = x / (ColorBar.ImageWidth - 1).toFloat
			val col = App.instance.frameManager.transformFunction(rat)

			for (y <- 0 until pImage.height) {
				pImage.set(x,y, col)
			}
		}

		leftColor = (64 << 24) + App.instance.frameManager.transformFunction(0)
		rightColor = (64 << 24) + App.instance.frameManager.transformFunction(1)
	}

	override def drawSelf()
	{
		View.p.noStroke()

		// bar

		val alpha = if (isPressed) (1.0 * 255).toInt else (0.5 * 255).toInt
		View.p.tint(255,255,255, alpha);

		View.p.image(pImage, 0,0, width,height-2)

		View.p.noFill()
		View.p.stroke(0xbbffffff)
		View.p.rect(0,0,width,height-1)

		// 'overflow'

		View.p.noStroke()
		if (x > 0) {
			// fill left side of widget-stroke-rect with 'ghosted' left-color
			View.p.fill(leftColor)
			View.p.rect(-x, 0, x - 2, height-2)
		}

		if (x + width < ColorWidget.Width) {
			// fill right side of widget-stroke-rect with 'ghosted' right-color
			View.p.fill(rightColor)
			View.p.rect(width + 1, 0, ColorWidget.Width - width - x - 2, height-2)
		}
	}
}

class ColorBarHandle(val isRight:Boolean) extends View
{
	width = 6
	height = ColorBar.Height

	registerMouseEvents()

	override def drawSelf()
	{
		View.p.noStroke()
		View.p.fill(if (isPressed) 0xaaffffff else 0x66ffffff)
		if (isRight)
			View.p.rect(1,0, width-2,height)
		else
			View.p.rect(0,0, width-2,height)
	}
}
