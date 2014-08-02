package mandelbrot.core

object ChunkRequester
{
	sealed class Order   
	case object OrderSquares extends Order
	case object OrderRadial extends Order
}

class ChunkRequester(pChunkWidth:Int, pChunkHeight:Int)
{
	val chunkWidth:Int = pChunkWidth
	val chunkHeight:Int = pChunkHeight
	val numChunkColumns = math.ceil(Config.ShellWidth / chunkWidth.toFloat).toInt
	val numChunkRows = math.ceil(Config.ShellHeight / chunkHeight.toFloat).toInt
	val totalNum = numChunkRows * numChunkColumns

	private var chunkCounter:Int = _
	private var chunkOrder:Vector[(Int,Int)] = _

	private var mandelbrotView:MandelbrotView = _

	def init(order:ChunkRequester.Order)
	{
		order match {
			case ChunkRequester.OrderRadial => initExpandingRadial()
			case ChunkRequester.OrderSquares => initExpandingSquares()
			case _ => ()
		}
	}

	def reset(pMandelbrotView:MandelbrotView)
	{
		mandelbrotView = pMandelbrotView
		chunkCounter = 0
	}

	def numLeft:Int = chunkOrder.length - chunkCounter - 1

	def nextChunkRequest: ChunkMaker.Request =
	{
		if (chunkOrder == null)
		{
			println("hello?")
		}

		if (chunkCounter >= chunkOrder.length - 1) return null

		val colRow = chunkOrder(chunkCounter)
		chunkCounter += 1

		val col = colRow._1
		val row = colRow._2
		val x = col * chunkWidth
		val y = row * chunkHeight
		val width = if (x + chunkWidth <= Config.ShellWidth - 1) chunkWidth else Config.ShellWidth - x
		val height = if (y + chunkHeight <= Config.ShellHeight - 1) chunkHeight else Config.ShellHeight - y

		new ChunkMaker.Request(
			requestTime = System.nanoTime(),
			mandelStartX = mandelbrotView.xMin + x * mandelbrotView.step,
			mandelStartY = mandelbrotView.yMin + y * mandelbrotView.step,
			mandelStep = mandelbrotView.step,
			rect = Rect(x,y,width,height)
		)
	}

	private def initExpandingRadial()
	{
		val centerCol = numChunkColumns / 2
		val centerRow = numChunkRows / 2

		chunkOrder = Vector[(Int,Int)]()
		for (r <- 0 until numChunkRows; c <- 0 until numChunkColumns) {
			chunkOrder = chunkOrder :+ (c,r)
		}

		chunkOrder = chunkOrder.sortBy( tup => {
			val dx = math.abs(centerCol - tup._1)
			val dy = math.abs(centerRow - tup._2)
			math.sqrt(dx*dx + dy*dy)
		})
	}

	private def initExpandingSquares()
	{
		val centerCol = numChunkColumns / 2
		val centerRow = numChunkRows / 2

		val dist = math.max( math.ceil(numChunkColumns/2f), math.ceil(numChunkRows/2f) ).toInt

		chunkOrder = Vector[(Int,Int)]()
		chunkOrder = chunkOrder :+ (centerCol,centerRow)

		for (i <- 1 to dist)
		{
			val left = centerCol - i
			val right = centerCol + i
			val top = centerRow - i
			val bottom = centerRow + i

			// top line (from left to right)
			for (col <- left until right) {
				if (col >= 0 && col < numChunkColumns && top >= 0) {
					chunkOrder = chunkOrder :+ (col, top)
				}
			}
			// right line (from top to bottom)
			for (row <- top until bottom) {
				if (row >= 0 && row < numChunkRows && right < numChunkColumns) {
					chunkOrder = chunkOrder :+ (right,row)
				}
			}
			// bottom line (from right to left)
			for (col <- right until left by -1) {
				if (col >= 0 && col < numChunkColumns && bottom < numChunkRows) {
					chunkOrder = chunkOrder :+ (col, bottom)
				}
			}
			// left line (from bottom to top)
			for (row <- bottom until top by -1) {
				if (row >= 0 && row < numChunkRows && left >= 0) {
					chunkOrder = chunkOrder :+ (left,row)
				}
			}
		}
	}
}
