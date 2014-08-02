package mandelbrot.frontend

import _root_.processing.core.PImage
import mandelbrot.core._
import leelib.scala.processing.Util
import mandelbrot.ColorFunctions

/**
 * Note how this holds the shared 'mandelbrotView' data
 * Be mindful of that in relation to different actors reading and writing simultaneously...
 *
 * updatePixels must be called from outside
 */
class FrameManager(val width:Int, val height:Int)
{
	private val _chunkRequester = new ChunkRequester(Config.chunkWidth, Config.chunkHeight)
	private var _preview:PreviewMaker.Result = _
	private var flatFrame:Array[Int] = _
	private var chunks:Chunks = _
	private var lastDrawnChunkIndex:Int = _

	private var _mapMin:Float = _
	private var _mapMax:Float = _
	private var _mapRange:Float = _  // avoids one subtract operation
	private var _transformFunction:(Float) => Int =  ColorFunctions.gray

	private var _escapeColor:Int = 0x0

	private var _minEscape:Double = _  // TODO: make private
	private var _maxEscape:Double = _

	private var _image:PImage = _


	_chunkRequester.init(ChunkRequester.OrderRadial)
	mapMin = 0f
	mapMax = 1f

	_image = new PImage(width, height)
	_image.loadPixels()


	def image:PImage = _image

	def chunkRequester = _chunkRequester

	/**
	 * Takes a value from 0 to 1 and converts it into a color
	 */
	def transformFunction = _transformFunction
	def transformFunction_=(f: (Float) => Int) {
		_transformFunction = f
	}

	def mapMin = _mapMin
	def mapMin_=(f:Float) {
		_mapMin = f;
		_mapRange = _mapMax - _mapMin
	}

	def mapMax = _mapMax
	def mapMax_=(f:Float) {
		_mapMax = f;
		_mapRange = _mapMax - _mapMin
	}

	def escapeColor = _escapeColor
	def escapeColor_=(color:Int) {
		_escapeColor = color
	}

	def preview = _preview
	def preview_=(p: PreviewMaker.Result) {
		_preview = p
	}

	def isComplete =  flatFrame != null

	def updatePixels() = _image.updatePixels()

	//

	def resetFrame(pMandelbrotView:MandelbrotView)
	{
		_chunkRequester.reset(pMandelbrotView)

		chunks = new Chunks
		flatFrame = null
		lastDrawnChunkIndex = 0
		_minEscape = 999999
		_maxEscape = 0
	}

	def drawPreview()
	{
		val start = System.nanoTime()
		val escapeValuesIterator = preview.escapeValues.iterator
		for (row <- 0 until preview.req.numRows)
		{
			for (col <- 0 until preview.req.numColumns)
			{
				val color = transform( escapeValuesIterator.next() )

				val left = col * preview.req.chunkWidth
				val right = math.min( (col+1) * preview.req.chunkWidth - 1, Config.ShellWidth - 1)
				val top = row * preview.req.chunkHeight
				val bottom = math.min( (row+1) * preview.req.chunkHeight - 1, Config.ShellHeight - 1)

				for (y <- top to bottom)
				{
					var index = y * Config.ShellWidth + left

					for (i <- left to right)
					{
						_image.pixels(index) = color
						index += 1
					}
				}
			}
		}
		// println(s"drawPreview - ${Util.elapsedMs(start)}ms")
	}

	def addChunk(pChunk:ChunkMaker.Result)
	{
		chunks += pChunk

		if (chunks.length == _chunkRequester.totalNum-1)
		{
			val start = System.nanoTime()
			flatFrame = new Array[Int](width * height)
			for (i <- 0 until chunks.length) {
				val chunk = chunks(i)
				val iter = chunk.escapeValues.iterator
				for (y <- chunk.req.rect.y until chunk.req.rect.y + chunk.req.rect.height) {
					var index = y * _image.width + chunk.req.rect.x
					for (x <- 0 until chunk.req.rect.width) {
						val escapeValue = iter.next()
						flatFrame(index) = escapeValue
						index += 1
					}
				}
			}

			// TODO: DOTHIS...
			// chunks = null

			// println(s"flatten time ${Util.elapsedMs(start)}ms")
		}
	}

	def drawLatest()
	{
		val a = lastDrawnChunkIndex + 1
		val b = chunks.length - 1
		lastDrawnChunkIndex = b
		drawChunks(a, b)
	}

	protected def transform(escapeValue:Int):Int =
	{
		if (escapeValue == Config.escapeThreshold)
		{
			escapeColor
		}
		else
		{
			val ratio = (Math.log(escapeValue) / Config.loggedEscapeThreshold).toFloat  // linear version is a non-starter mkay

			// TODO - MOVE THIS INTO OWN FUNCTION AGAIN LATER
			_minEscape = math.min(_minEscape, escapeValue)
			_maxEscape = math.max(_maxEscape, escapeValue)

			val mappedRatio = mapMin  +  ratio * _mapRange  // ie, map(ratio,0,1, mapMin,mapMax)

			transformFunction( if (mappedRatio < 0) 0 else if (mappedRatio > 1) 1 else mappedRatio )
		}
	}

	private def drawChunks(start:Int, end:Int)
	{
		for (i <- start to end) {
			val chunk = chunks(i)  // TODO: 'index out of bounds' 2019
			val iter = chunk.escapeValues.iterator
			for (y <- chunk.req.rect.y until chunk.req.rect.y + chunk.req.rect.height) {
				var index = y * _image.width + chunk.req.rect.x
				for (x <- 0 until chunk.req.rect.width) { // note, 'while' loop is no faster
					val escapeValue = iter.next()
					_image.pixels(index) = transform(escapeValue)
					index += 1
				}
			}
		}
	}

	def redraw()
	{
		if (! isComplete)
		{
			drawPreview()
			drawChunks(0, chunks.length-1)
		}
		else
		{
			val start = System.nanoTime()
			for (i <- 0 until flatFrame.length) {
				_image.pixels(i) = transform( flatFrame(i) )  // retardedly more straightforward...
			}
			println(s"redraw with flatarray ${Util.elapsedMs(start)}ms")
		}
	}
}
