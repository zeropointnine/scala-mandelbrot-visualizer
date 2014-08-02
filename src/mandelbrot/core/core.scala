package mandelbrot.core
{
	object Config
	{
		var ShellWidth = 1820
		var ShellHeight = 1024
		var NumChunkMakers = 4
		var DrawBufferIntervalNs:Long = 33 * 1e6.toLong
		var chunkWidth = 30
		var chunkHeight = 30

		private var _escapeThreshold:Int = _
		private var _loggedEscapeThreshold:Float = _

		def escapeThreshold:Int = _escapeThreshold
		def escapeThreshold_=(i:Int)
		{
			_escapeThreshold = i
			_loggedEscapeThreshold = math.log(_escapeThreshold).toFloat
		}
		def loggedEscapeThreshold =  _loggedEscapeThreshold

		escapeThreshold = 250
	}
}

