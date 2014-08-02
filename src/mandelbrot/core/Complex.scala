package mandelbrot.core

class Complex(val real : Double, val imag : Double)
{
	def +(other : Complex) = new Complex(real + other.real, imag + other.imag)
	def *(other : Complex) = new Complex(real*other.real - imag*other.imag, imag*other.real + real*other.imag)
	def magnitude() : Double = Math.sqrt(real*real + imag*imag)
}
