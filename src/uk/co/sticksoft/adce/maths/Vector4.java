package uk.co.sticksoft.adce.maths;

public class Vector4
{
	
	public float[] v = new float[4];
	
	///////////////////////////////////////////////////////////////////////////
	// Constructors
	///////////////////////////////////////////////////////////////////////////
	
	public Vector4() {}
	
	public Vector4(float x, float y, float z, float w)
	{
		v[0] = x;
		v[1] = y;
		v[2] = z;
		v[3] = w;
	}
	
	public Vector4(float[] vector)
	{
		v[0] = vector[0];
		v[1] = vector[1];
		v[2] = vector[2];
		v[3] = vector[3];
	}
	
	public Vector4(Vector4 vector)
	{
		v[0] = vector.v[0];
		v[1] = vector.v[1];
		v[2] = vector.v[2];
		v[3] = vector.v[3];
	}
	
	///////////////////////////////////////////////////////////////////////////
	// Mutators
	///////////////////////////////////////////////////////////////////////////
	
	// Add
	public Vector4 add(float f)
	{
		v[0] += f;
		v[1] += f;
		v[2] += f;
		v[3] += f;
		return this;
	}
	
	public Vector4 add(float x, float y, float z, float w)
	{
		v[0] += x;
		v[1] += y;
		v[2] += z;
		v[3] += w;
		return this;
	}
	
	public Vector4 add(float[] vector)
	{
		v[0] += vector[0];
		v[1] += vector[1];
		v[2] += vector[2];
		v[3] += vector[3];
		return this;
	}
	
	public Vector4 add(Vector4 vector)
	{
		v[0] += vector.v[0];
		v[1] += vector.v[1];
		v[2] += vector.v[2];
		v[3] += vector.v[3];
		return this;
	}
	
	// Subtract
	public Vector4 sub(float f)
	{
		v[0] -= f;
		v[1] -= f;
		v[2] -= f;
		v[3] -= f;
		return this;
	}
	
	public Vector4 sub(float x, float y, float z, float w)
	{
		v[0] -= x;
		v[1] -= y;
		v[2] -= z;
		v[3] -= w;
		return this;
	}
	
	public Vector4 sub(float[] vector)
	{
		v[0] -= vector[0];
		v[1] -= vector[1];
		v[2] -= vector[2];
		v[3] -= vector[3];
		return this;
	}
	
	public Vector4 sub(Vector4 vector)
	{
		v[0] -= vector.v[0];
		v[1] -= vector.v[1];
		v[2] -= vector.v[2];
		v[3] -= vector.v[3];
		return this;
	}
	
	// Multiply
	public Vector4 mul(float f)
	{
		v[0] *= f;
		v[1] *= f;
		v[2] *= f;
		v[3] *= f;
		return this;
	}
	
	public Vector4 mul(float x, float y, float z, float w)
	{
		v[0] *= x;
		v[1] *= y;
		v[2] *= z;
		v[3] *= w;
		return this;
	}
	
	public Vector4 mul(float[] vector)
	{
		v[0] *= vector[0];
		v[1] *= vector[1];
		v[2] *= vector[2];
		v[3] *= vector[3];
		return this;
	}
	
	public Vector4 mul(Vector4 vector)
	{
		v[0] *= vector.v[0];
		v[1] *= vector.v[1];
		v[2] *= vector.v[2];
		v[3] *= vector.v[3];
		return this;
	}
	
	// Divide
	public Vector4 div(float f)
	{
		v[0] /= f;
		v[1] /= f;
		v[2] /= f;
		v[3] /= f;
		return this;
	}
	
	public Vector4 div(float x, float y, float z, float w)
	{
		v[0] /= x;
		v[1] /= y;
		v[2] /= z;
		v[3] /= w;
		return this;
	}
	
	public Vector4 div(float[] vector)
	{
		v[0] /= vector[0];
		v[1] /= vector[1];
		v[2] /= vector[2];
		v[3] /= vector[3];
		return this;
	}
	
	public Vector4 div(Vector4 vector)
	{
		v[0] /= vector.v[0];
		v[1] /= vector.v[1];
		v[2] /= vector.v[2];
		v[3] /= vector.v[3];
		return this;
	}
	
	public Vector4 div(Vector3 vector)
	{
		v[0] /= vector.v[0];
		v[1] /= vector.v[1];
		v[2] /= vector.v[2];
		return this;
	}
	
	public Vector4 div(Vector2 vector)
	{
		v[0] /= vector.v[0];
		v[1] /= vector.v[1];
		return this;
	}
	
	///////////////////////////////////////////////////////////////////////////
	// Accessors
	///////////////////////////////////////////////////////////////////////////
	
	public float x () { return v[0]; }
	public float y () { return v[1]; }
	public float z () { return v[2]; }
	public float w () { return v[3]; }
	
	public float lengthSq() { return v[0]*v[0] + v[1]*v[1] + v[2]*v[2] + v[3]*v[3]; }
	public float length() { return (float)Math.sqrt(v[0]*v[0] + v[1]*v[1] + v[2]*v[2] + v[3]*v[3]); }
	
	///////////////////////////////////////////////////////////////////////////
	// Methods that don't fit into any pansy design pattern
	///////////////////////////////////////////////////////////////////////////
	
	// Add
	public Vector4 add(Vector4 dest, float f)
	{
		dest.v[0] = v[0] + f;
		dest.v[1] = v[1] + f;
		dest.v[2] = v[2] + f;
		dest.v[3] = v[3] + f;
		return dest;
	}
	
	public Vector4 add(Vector4 dest, float x, float y, float z, float w)
	{
		dest.v[0] = v[0] + x;
		dest.v[1] = v[1] + y;
		dest.v[2] = v[2] + z;
		dest.v[3] = v[3] + w;
		return dest;
	}
	
	public Vector4 add(Vector4 dest, float[] vector)
	{
		dest.v[0] = v[0] + vector[0];
		dest.v[1] = v[1] + vector[1];
		dest.v[2] = v[2] + vector[2];
		dest.v[3] = v[3] + vector[3];
		return dest;
	}
	
	public Vector4 add(Vector4 dest, Vector4 vector)
	{
		dest.v[0] = v[0] + vector.v[0];
		dest.v[1] = v[1] + vector.v[1];
		dest.v[2] = v[2] + vector.v[2];
		dest.v[3] = v[3] + vector.v[3];
		return dest;
	}
	
	// Subtract
	public Vector4 sub(Vector4 dest, float f)
	{
		dest.v[0] = v[0] - f;
		dest.v[1] = v[1] - f;
		dest.v[2] = v[2] - f;
		dest.v[3] = v[3] - f;
		return dest;
	}
	
	public Vector4 sub(Vector4 dest, float x, float y, float z, float w)
	{
		dest.v[0] = v[0] - x;
		dest.v[1] = v[1] - y;
		dest.v[2] = v[2] - z;
		dest.v[3] = v[3] - w;
		return dest;
	}
	
	public Vector4 sub(Vector4 dest, float[] vector)
	{
		dest.v[0] = v[0] - vector[0];
		dest.v[1] = v[1] - vector[1];
		dest.v[2] = v[2] - vector[2];
		dest.v[3] = v[3] - vector[3];
		return dest;
	}
	
	public Vector4 sub(Vector4 dest, Vector4 vector)
	{
		dest.v[0] = v[0] - vector.v[0];
		dest.v[1] = v[1] - vector.v[1];
		dest.v[2] = v[2] - vector.v[2];
		dest.v[3] = v[3] - vector.v[3];
		return dest;
	}
	
	// Multiply
	public Vector4 mul(Vector4 dest, float f)
	{
		dest.v[0] = v[0] * f;
		dest.v[1] = v[1] * f;
		dest.v[2] = v[2] * f;
		dest.v[3] = v[3] * f;
		return dest;
	}
	
	public Vector4 mul(Vector4 dest, float x, float y, float z, float w)
	{
		dest.v[0] = v[0] * x;
		dest.v[1] = v[1] * y;
		dest.v[2] = v[2] * z;
		dest.v[3] = v[3] * w;
		return dest;
	}
	
	public Vector4 mul(Vector4 dest, float[] vector)
	{
		dest.v[0] = v[0] * vector[0];
		dest.v[1] = v[1] * vector[1];
		dest.v[2] = v[2] * vector[2];
		dest.v[3] = v[3] * vector[3];
		return dest;
	}
	
	public Vector4 mul(Vector4 dest, Vector4 vector)
	{
		dest.v[0] = v[0] * vector.v[0];
		dest.v[1] = v[1] * vector.v[1];
		dest.v[2] = v[2] * vector.v[2];
		dest.v[3] = v[3] * vector.v[3];
		return dest;
	}
	
	// Divide
	public Vector4 div(Vector4 dest, float f)
	{
		dest.v[0] = v[0] / f;
		dest.v[1] = v[1] / f;
		dest.v[2] = v[2] / f;
		dest.v[3] = v[3] / f;
		return dest;
	}
	
	public Vector4 div(Vector4 dest, float x, float y, float z, float w)
	{
		dest.v[0] = v[0] / x;
		dest.v[1] = v[1] / y;
		dest.v[2] = v[2] / z;
		dest.v[3] = v[3] / w;
		return dest;
	}
	
	public Vector4 div(Vector4 dest, float[] vector)
	{
		dest.v[0] = v[0] / vector[0];
		dest.v[1] = v[1] / vector[1];
		dest.v[2] = v[2] / vector[2];
		dest.v[3] = v[3] / vector[3];
		return dest;
	}
	
	public Vector4 div(Vector4 dest, Vector4 vector)
	{
		dest.v[0] = v[0] / vector.v[0];
		dest.v[1] = v[1] / vector.v[1];
		dest.v[2] = v[2] / vector.v[2];
		dest.v[3] = v[3] / vector.v[3];
		return dest;
	}
	
	// Dot product
	public float dot(float x, float y, float z, float w)
	{
		return v[0] * x + v[1] * y + v[2] * z + v[3] * w;
	}
	
	public float dot(float[] vector)
	{
		return v[0] * vector[0] + v[1] * vector[1] + v[2] * vector[2] + v[3] * vector[3];
	}
	
	public float dot(Vector4 vector)
	{
		return v[0] * vector.v[0] + v[1] * vector.v[1] + v[2] * vector.v[2] + v[3] * vector.v[3];
	}
}
