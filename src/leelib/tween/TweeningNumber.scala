package leelib.tween

import java.util.{Timer, TimerTask}

/**
 * Lightweight time-based number tweener. Acts as a wrapper around a value.
 * Uses TimerTask for expiration check and callback
 */
class TweeningNumber(pValue:Float)
{
	private var _startValue:Float = pValue
	private var _destValue:Float = pValue
	private var _startTime = 0L
	private var _duration = 1
	private var _easeFunction:(Float) => Float = _
	private var _callback: () => Any = _
	private var timer:Timer = new Timer()


	// NOT SURE IF/HOW THIS COULD BE PUT TO USE
	var customGet: ()=>Float =  _
	var customSet:(Float)=>Unit = _


	def startValue =  _startValue
	def destValue = _destValue
	def startTime = _startTime
	def endTime = _startTime + _duration
	def duration = _duration
	def easeFunction = _easeFunction
	def callback = _callback

	/**
	 * @return ratio of time elapsed to duration
	 */
	def progress = {
		val prog = (System.currentTimeMillis() - _startTime) / _duration.toFloat
		if (prog < 1) prog else 1f
	}

	/**
	 * @param pEaseFunction    null == linear
	 * @return                 false if tween didn't start
	 */
	def start(pDest:Float, pMillis:Int, pEaseFunction:(Float) => Float, pCallback:() => Any):Boolean =
	{
		require(pMillis > 0)
		if (value == pDest) return false
		_startValue = value
		_destValue = pDest
		_startTime = System.currentTimeMillis()
		_duration = pMillis
		_easeFunction = pEaseFunction
		_callback = pCallback

		if (timer != null) timer.cancel()
		val timerTask:TimerTask = new TimerTask {
			def run() {
				timer = null
				if (_callback != null) {
					val cb = callback
					_callback = null
					cb()
				}
			}
		}
		timer = new Timer()
		timer.schedule(timerTask, pMillis)
		return true
	}

	/**
	 * start, with no callback method
	 */
	def to(pDest:Float, pMillis:Int, pEaseFunction:(Float) => Float) {
		start(pDest, pMillis, pEaseFunction, null)
	}

	/**
	 * start, with no callback and reusing same easeFunction
	 */
	def start(pDest:Float, pMillis:Int) {
		to(pDest, pMillis, _easeFunction)
	}

	/**
	 * @return the untransformed value (ie, linear)
	 */
	def untransformedValue = {
		_startValue + (_destValue - _startValue) * progress
	}

	def transformedValue = {
		if (_easeFunction != null)
			_startValue + (_destValue - _startValue) * _easeFunction(progress)
		else
			Float.NaN
	}

	/**
	 * @return the transformed value
	 */
	def value = {
		if (_easeFunction == null)
			untransformedValue
		else {
			transformedValue
		}
	}

	/**
	 * Interrupts any tween in progress
	 */
	def value_=(f:Float) {
		if (timer != null) timer.cancel()
		_callback = null
		_startValue = f
		_destValue = f
	}

	/**
	 * Shorthand convenience getter
	 */
	def apply():Float = value

	/**
	 * Shorthand convenience setter
	 */
	def apply(f:Float) =  value = f

	def stop() {
		if (timer != null) timer.cancel()
		_callback = null
		if (progress < 1) {
			_startValue = value
			_destValue = _startValue
		}
	}
}
