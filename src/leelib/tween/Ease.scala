package leelib.tween

object Ease
{
	// When f == 0, function must return 0
	// When f == 1, function must return 1

	def linear(f:Float) = f

	def inQuad(f:Float):Float = f * f
	def inCubic(f:Float):Float = f * f * f
	def inQuart(f:Float):Float = f * f * f * f

	def outQuad(f:Float):Float = 1 - (1-f) * (1-f)
	def outCubic(f:Float):Float = 1 - (1-f) * (1-f) * (1-f)
	def outQuart(f:Float):Float = 1 - (1-f) * (1-f) * (1-f) * (1-f)

	/**
	 * In the real world, not a very pretty shape function, but shows how currying can actually be useful
	 * @param multiplier    should typically be a small number close to zero
	 */
	def outInSine(multiplier:Float)(f:Float):Float = math.sin(f * 2*math.Pi).toFloat * multiplier  +  f

	/**
	 * @param multiplier    should typically be a small number close to zero
	 */
	def inOutSine(multiplier:Float)(f:Float):Float = math.sin((f + math.Pi/2f) * 2*math.Pi).toFloat * multiplier  +  f
}
