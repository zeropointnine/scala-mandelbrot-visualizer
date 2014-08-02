package leelib.tween

import processing.core.PApplet

/**
 * Frame-based number tweener.  Acts as a wrapper around a value.
 * TODO: make step parameter always positive so it's easier to swap out with TimeTweenTween
 */
class SteppingNumber(pStartValue:Float, pEndValue:Float, pStep:Float, pShaper:(Float)=>Float)
{
	var _startValue:Float = _
	var _untransformedValue:Float = _
	var _endValue:Float = _
	var _step:Float = _
	var _shaper:(Float) => Float = _

 	// ctor
	_untransformedValue = pStartValue
	update(pEndValue, pStep, pShaper)

	def this() = this(0,0,0,null)

	def start = _startValue
	def end = _endValue
	def step = _step

	def update(pEnd:Float, pStep:Float) { update(pEnd, pStep, null) }
	def update(pEndValue:Float, pStep:Float, pShape:(Float)=>Float) {
		_untransformedValue = value  // * transformed-value becomes raw-value
		_startValue = _untransformedValue
		_endValue = pEndValue
		_step = pStep
		_shaper = pShape
		require(isStepLegal)
	}

	/**
	 * returns a result code
	 * 0 = incremented
	 * 1 = incremented and finished
	 * -1 = already done
	 */
	def next():Int =
	{
		if (_untransformedValue == _endValue)
			-1
		else {
			_untransformedValue += _step
			if ((isEndGe && _untransformedValue >= _endValue ) || (!isEndGe && _untransformedValue <= _endValue)) {
				_untransformedValue = _endValue  // 'clamp'
				1
			}
			else
				0
		}
	}

	def untransformedValue = _untransformedValue

	/**
	 * @return the transformed/shaped value
	 */
	def value = {
		if (_shaper == null  ||  _startValue == _endValue)
			_untransformedValue
		else {
			val normed = PApplet.map(_untransformedValue, _startValue, _endValue, 0,1)
			val shaped = _shaper(normed)
			val denormed = PApplet.map(shaped, 0,1, _startValue, _endValue)
			denormed
		}
	}

	def isComplete = { _untransformedValue == _endValue }

	private def isEndGe = { _endValue >= _startValue }

	private def isStepLegal = {
		if (_startValue == _endValue) true
		else if (isEndGe) _step >= 0
		else _step <= 0
	}
}
