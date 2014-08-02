package mandelbrot.core

import leelib.scala.processing.Util

/**
 * TODO: calculatePreview/Chunk could be combined; and Preview/Chunk/Request/Result as well
 */
object Calculator
{
	def calculateEscapeValue(mandelX:Double, mandelY:Double):Int =
	{
		val complexC = new Complex(mandelX, mandelY)
		var complexZ = new Complex(0.0, 0.0)

		var count = 0
		while (complexZ.magnitude() < 2.0 && count < Config.escapeThreshold) {  
			complexZ = complexZ * complexZ + complexC
			count = count + 1
		}
		count
	}

	//

	def calculateChunk(p:ChunkMaker.Request): ChunkMaker.Result =
	{
		val values = new Array[Int](p.rect.width * p.rect.height)

		var i = 0
		var y = p.mandelStartY
		for (r <- 0 until p.rect.height)
		{
			var x = p.mandelStartX
			for (c <- 0 until p.rect.width)
			{
				values(i) = calculateEscapeValue(x,y)
				i += 1
				x += p.mandelStep
			}
			y += p.mandelStep
		}

		new ChunkMaker.Result(p, values)
	}
}
