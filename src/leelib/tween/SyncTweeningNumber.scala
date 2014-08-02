package leelib.tween

/**
 * Time-based number tweener. Acts as a wrapper around a value.
 *
 * This version does an 'expiration check' at the start of every method.
 * Ensures getters return reliable values (ie, when expired, they return default values).
 * Also makes it not dependent on any asynchronous mechanisms, which has its pros.
 *
 * TODO: don't 'reset' values at end of tween to make it consistent with TweeningNumber
 */
class SyncTweeningNumber(pValue:Float)
{
	private var _startValue:Float = pValue
	private var _endValue:Float = Float.NaN
	private var _startTime = 0L
	private var _endTime = 0L
	private var _shaper:(Float) => Float = _

	/**
	 * Returns current value if not currently tweening
	 */
	def startValue = { isRunning();_startValue }

	/**
	 * Returns NaN if not currently tweening
	 */
	def endValue = { isRunning(); _endValue }

	/**
	 * Returns 0 if not currently tweening
	 */
	def startTime = { isRunning(); _startTime }

	/**
	 * Returns 0 if not currently tweening
	 */
	def endTime = { isRunning(); _endTime }

	/**
	 * Returns NaN if not currently tweening
	 */
	def progress = {
		if (_startTime > 0)
			(System.currentTimeMillis() - _startTime) / (_endTime - _startTime).toFloat
		else
			Float.NaN
	}

	def shaper = _shaper

	/**
	 * @param pShape    null == use linear
	 */
	def start(pEnd:Float, pMillis:Int, pShape:(Float) => Float) {
		require(pMillis > 0)
		if (isRunning()) _startValue = value
		_endValue = pEnd
		_startTime = System.currentTimeMillis()
		_endTime = _startTime + pMillis
		_shaper = pShape
	}

	/**
	 * Uses current shaper method
	 */
	def start(pEnd:Float, pMillis:Int) {
		start(pEnd, pMillis, _shaper)
	}

	def untransformedValue = {
		if (isRunning())
			_startValue + (_endValue - _startValue) * progress
		else
			_startValue
	}

	/**
	 * @return the transformed/shaped value
	 */
	def value =
	{
		if (isRunning()) {
			if (_shaper == null)
				untransformedValue
			else
				_startValue + (_endValue - _startValue) * _shaper(progress)
		} else _startValue
	}

	/**
	 * Any tween in progress is halted
	 */
	def value_=(f:Float) {
		_startValue = f
		_endValue = Float.NaN
		_startTime = 0
		_endTime = 0
	}

	def stop() {
		if (isRunning()) _startValue = value
		_endValue = Float.NaN
		_startTime = 0
		_endTime = 0
	}

	/**
	 * Any time-related operation calls this first
	 * Does expiration test with side effects before returning running status
	 */
	private def isRunning():Boolean =
	{
		// expiration test
		if (_endTime > 0 && System.currentTimeMillis() > _endTime) {
			_startValue = _endValue
			_endValue = Float.NaN
			_startTime = 0
			_endTime = 0
			false
		}
		else
			System.currentTimeMillis() <= _endTime
	}
}
