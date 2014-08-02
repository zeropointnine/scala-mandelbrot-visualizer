package mandelbrot.core

/**
 * Describes the view/viewport of the rendered image
 * @param numCols
 * @param numRows
 */
case class MandelbrotView(numCols:Int, numRows:Int)
{
	private var _xCenter:Double = _
	private var _yCenter:Double = _
	private var _step:Double = _

	private var _xMin:Double = _
	private var _xMax:Double = _
	private var _yMin:Double = _
	private var _yMax:Double = _

	def xCenter = _xCenter
	def yCenter = _yCenter
	def step = _step

	def xMin = _xMin
	def xMax = _xMax
	def yMin = _yMin
	def yMax = _yMax

	setPosition(0,0, 3e-3)

	def setPosition(pXCenter:Double, pYCenter:Double, pStep:Double)
	{
		println(s"MandelbrotView.setPosition($pXCenter, $pYCenter, $pStep)")

		_xCenter = pXCenter
		_yCenter = pYCenter
		_step = pStep

		_xMin = xCenter - step * numCols/2f
		_xMax = xCenter + step * numCols/2f
		_yMin = yCenter - step * numRows/2f
		_yMax = yCenter + step * numRows/2f
	}
}
