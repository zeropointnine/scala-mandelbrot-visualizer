package mandelbrot

import _root_.processing.core.PApplet
import leelib.scala.processing.View


object ColorFunctions
{
	def greenToBlue(ratio:Float):Int =
	{
		val v = (ratio * 255).toInt
		val color = (v << 8) + (255 - v)
		color
	}

	def gray(ratio:Float):Int =
	{
		val v = (ratio * 255).toInt
		val color = ((v << 16) + (v << 8) + (v))
		color
	}

	def lerpColor(color1:Int, color2:Int, pConstantsColorMode:Int)(ratio:Float):Int =
	{
		PApplet.lerpColor(color1, color2, ratio, pConstantsColorMode)
	}
}

object IndexedColors
{
	def makeWithLerpedColors(startColor:Int, endColor:Int, colorMode:Int, length:Int):IndexedColors =
	{
		val ic = new IndexedColors()
		ic.colors = new Array[Int](length)

		for (i <- 0 until length) {
			val ratio = i / (length-1).toFloat
			ic.colors(i) = ColorFunctions.lerpColor(startColor, endColor, colorMode)(ratio)
		}
		ic
	}

	/**
	 * Samples colors along top row of image
	 */
	def makeWithImage(imagePath:String):IndexedColors =
	{
		val pImage = View.p.loadImage(imagePath)
		if (pImage.width == -1) return null

		val ic = new IndexedColors()
		ic.colors = new Array[Int](pImage.width)
		for (i <- 0 until pImage.width) {
			ic.colors(i) = pImage.get(i, 0)
		}
		ic
	}
}

class IndexedColors()
{
	private var colors:Array[Int] = _

	def ratioToColor(ratio:Float):Int =
	{
		val index =  math.floor(ratio * (colors.length-1)).toInt

		if (index >= colors.length)
		{
			println("hi")
		}

		colors(index)
	}
}