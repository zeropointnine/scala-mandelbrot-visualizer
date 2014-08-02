package mandelbrot.frontend.processing.ui

import leelib.scala.processing.View
import processing.core.PGraphics

object KeyButton
{
	val Width = 25
	val Height = 25
}
class KeyButton(text:String) extends View
{
	width = KeyButton.Width
	height = KeyButton.Height

	var pg:PGraphics = View.p.createGraphics(width.toInt, height.toInt)
	pg.beginDraw()
	pg.noStroke()
	pg.fill(0xffffffff)
	pg.rect(0,0, width,height)

	pg.textFont(Ui.smallFont)
	val tw = pg.textWidth(text)
	val tx = (width - tw) / 2f

	pg.fill(0xff000000)
	pg.text(text, tx,16)

	pg.endDraw()

	registerMouseEvents()


	/**
	 * pressDelegate wrapper logic
	 */
	var selectDelegate: ()=>Unit =  _
	var selectIntervalMax = 250
	var selectIntervalMin = 100
	private var selectInterval:Float = _
	private var selectTime:Long = _

	pressDelegate = (MouseEvent)=>
	{
		selectInterval = selectIntervalMax
		selectTime = System.currentTimeMillis()
		if (selectDelegate != null) selectDelegate()
	}



	override def drawSelf()
	{
		val alpha = if (isPressed) (1.0 * 255).toInt else (0.5 * 255).toInt
		View.p.tint(255,255,255, alpha);
		View.p.image(pg, 0,0)

		if (isPressed)
		{
			if (System.currentTimeMillis() - selectTime > selectInterval)
			{
				selectInterval *= 0.85f
				if (selectInterval < selectIntervalMin) selectInterval = selectIntervalMin
				selectTime = System.currentTimeMillis()
				if (selectDelegate != null) selectDelegate()
			}
		}
	}
}
