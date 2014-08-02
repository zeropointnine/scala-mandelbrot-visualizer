package leelib.scala.processing


import processing.core.PApplet
import processing.event.MouseEvent


/**
 * Simple system for drawing nested views which inherit transforms only (no rotations or scaling, etc).
 *
 * Mouse event logic:
 *
 *      A View that does not have a parent is considered a 'root', and gets rootMousePollingLogic() called on every draw()
 *      It does a hit test on its ancestors. On a hit, that ancestor's onMouseDown() is called.
 *      On mouseUp, no matter where, that same ancestor gets an onMouseUp().
 *
 */
object View
{
	/**
	 * This must be set before any View instances do any drawing
	 */
	var p:PApplet = _
}

abstract class View
{
	private var _x:Float = _
	private var _y:Float = _
	private var _width:Float = _
	private var _height:Float = _

	def x:Float = _x
	def x_=(f:Float) = _x = f

	def y:Float = _y
	def y_=(f:Float) = _y = f

	/**
	 * Represents bounding box dimensions.
	 * Used for mouse click logic.
	 */
	def width:Float = _width
	def width_=(f:Float) = _width = f

	def height:Float = _height
	def height_=(f:Float) = _height = f

	// TODO: this blows up if set to protected, figure out why, and what should be done insted
	var pressDelegate: (MouseEvent)=>Unit = _
	var releaseDelegate: (MouseEvent)=>Unit = _
	var dragDelegate: (MouseEvent)=>Unit = _

	private var _parent:View = _
	private var children = Vector[View]()
	private var _isPressed:Boolean = _

	// This doesn't work right; why not.
	//	def View(pX:Int, pY:Int) { x = pX; y = pY }

	
	def draw()
	{
		View.p.pushMatrix()
		View.p.translate(x, y)

		drawSelf()
		children.foreach(_.draw())

		View.p.popMatrix()
	}

	final def addChild(child:View)
	{
		assert(child._parent == null)

		children = children :+ child
		child._parent = this
	}

	// TODO: UNTESTED
	final def removeChild(child:View)
	{
		assert(children.indexOf(child) > -1)

		children = children.filter( _ != child )
		child._parent = null
	}

	final def parent = _parent

	final def isPressed = _isPressed

	final def globalX:Float =
	{
		var cumx = x
		var item:View = this
		while (item.parent != null) {
			cumx += item.parent.x
			item = item.parent
		}
		cumx
	}

	final def globalY:Float =
	{
		var cumy = y
		var item:View = this
		while (item.parent != null) {
			cumy += item.parent.y
			item = item.parent
		}
		cumy
	}

	final def registerMouseEvents()
	{
		_isPressed = false
		View.p.registerMethod("mouseEvent", this)
	}

	final def unregisterMouseEvents()
	{
		View.p.unregisterMethod("mouseEvent", this)
		_isPressed = false
	}

	/**
	 * Processing callback
	 */
	def mouseEvent(e:MouseEvent)
	{
		e.getAction match
		{
			case MouseEvent.PRESS =>
				val gx = globalX
				val gy = globalY
				if (e.getX() >= gx && e.getX() <= gx + width && e.getY() >= gy && e.getY() <= gy + height) {
					_isPressed = true
					if (pressDelegate != null) pressDelegate(e)
				}

			case MouseEvent.RELEASE =>
				if (_isPressed) {
					_isPressed = false
					if (releaseDelegate != null) releaseDelegate(e)
				}

			case MouseEvent.DRAG =>
				if (_isPressed && dragDelegate != null) dragDelegate(e)

			case _ =>
		}
	}

	protected def onPress() {}

	protected def onReleaseAnywhere() {}

	/**
	 * Concrete class drawing operation are done here.
	 * Local origin should be considered to be 0,0.
	 * On exit, be mindful to restore any PApplet states that were changed inside of function.
	 */
	protected def drawSelf() {}

	protected def onMouseDown() {}

	protected def onMouseUp() {}
}
