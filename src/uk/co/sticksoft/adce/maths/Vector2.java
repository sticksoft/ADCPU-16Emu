package uk.co.sticksoft.adce.maths;

public class Vector2 extends Vector3
{
	///////////////////////////////////////////////////////////////////////////
	// Constructors
	///////////////////////////////////////////////////////////////////////////
	
	public Vector2()
	{
		super();
	}

	public Vector2(float x, float y)
	{
		super(x, y, 0);
	}

	public Vector2(float[] vector)
	{
		super(vector[0], vector[1], 0);
	}

	public Vector2(Vector4 vector)
	{
		super(vector.v[0], vector.v[1], 0);
	}
	
	///////////////////////////////////////////////////////////////////////////
	// Mutators
	///////////////////////////////////////////////////////////////////////////
	
	// Set
	public Vector2 set(float f)
	{
		v[0] = f;
		v[1] = f;
		return this;
	}
	
	public Vector2 set(float x, float y)
	{
		v[0] = x;
		v[1] = y;
		return this;
	}
	
	public Vector2 set(Vector2 vector)
	{
		this.v[0] = vector.v[0];
		this.v[1] = vector.v[1];
		return this;
	}
	
	// Add
	public Vector2 add(float f)
	{
		v[0] += f;
		v[1] += f;
		return this;
	}
	
	public Vector2 add(float x, float y)
	{
		v[0] += x;
		v[1] += y;
		return this;
	}
	
	public Vector2 add(float[] vector)
	{
		v[0] += vector[0];
		v[1] += vector[1];
		return this;
	}
	
	public Vector2 add(Vector4 vector)
	{
		v[0] += vector.v[0];
		v[1] += vector.v[1];
		return this;
	}
	
	// Subtract
	public Vector2 sub(float f)
	{
		v[0] -= f;
		v[1] -= f;
		return this;
	}
	
	public Vector2 sub(float x, float y)
	{
		v[0] -= x;
		v[1] -= y;
		return this;
	}
	
	public Vector2 sub(float[] vector)
	{
		v[0] -= vector[0];
		v[1] -= vector[1];
		return this;
	}
	
	public Vector2 sub(Vector4 vector)
	{
		v[0] -= vector.v[0];
		v[1] -= vector.v[1];
		return this;
	}
	
	// Multiply
	public Vector2 mul(float f)
	{
		v[0] *= f;
		v[1] *= f;
		return this;
	}
	
	public Vector2 mul(float x, float y)
	{
		v[0] *= x;
		v[1] *= y;
		return this;
	}
	
	public Vector2 mul(float[] vector)
	{
		v[0] *= vector[0];
		v[1] *= vector[1];
		return this;
	}
	
	public Vector2 mul(Vector4 vector)
	{
		v[0] *= vector.v[0];
		v[1] *= vector.v[1];
		return this;
	}
	
	// Divide
	public Vector2 div(float f)
	{
		v[0] /= f;
		v[1] /= f;
		return this;
	}
	
	public Vector2 div(float x, float y)
	{
		v[0] /= x;
		v[1] /= y;
		return this;
	}
	
	public Vector2 div(float[] vector)
	{
		v[0] /= vector[0];
		v[1] /= vector[1];
		return this;
	}
	
	public Vector2 div(Vector4 vector)
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
	public float z () { return 0; }
	public float w () { return 0; }
	
	public float lengthSq() { return v[0]*v[0] + v[1]*v[1]; }
	public float length() { return (float)Math.sqrt(v[0]*v[0] + v[1]*v[1]); }
	
	///////////////////////////////////////////////////////////////////////////
	// Methods that don't fit into any pansy design pattern
	///////////////////////////////////////////////////////////////////////////
	
	// Add
	public Vector2 add(Vector2 dest, float f)
	{
		dest.v[0] = v[0] + f;
		dest.v[1] = v[1] + f;
		return dest;
	}
	
	public Vector2 add(Vector2 dest, float x, float y)
	{
		dest.v[0] = v[0] + x;
		dest.v[1] = v[1] + y;
		return dest;
	}
	
	public Vector2 add(Vector2 dest, float[] vector)
	{
		dest.v[0] = v[0] + vector[0];
		dest.v[1] = v[1] + vector[1];
		dest.v[2] = v[2] + vector[2];
		return dest;
	}
	
	public Vector2 add(Vector2 dest, Vector4 vector)
	{
		dest.v[0] = v[0] + vector.v[0];
		dest.v[1] = v[1] + vector.v[1];
		return dest;
	}
	
	// Subtract
	public Vector2 sub(Vector2 dest, float f)
	{
		dest.v[0] = v[0] - f;
		dest.v[1] = v[1] - f;
		return dest;
	}
	
	public Vector2 sub(Vector2 dest, float x, float y, float z)
	{
		dest.v[0] = v[0] - x;
		dest.v[1] = v[1] - y;
		return dest;
	}
	
	public Vector2 sub(Vector2 dest, float[] vector)
	{
		dest.v[0] = v[0] - vector[0];
		dest.v[1] = v[1] - vector[1];
		dest.v[2] = v[2] - vector[2];
		return dest;
	}
	
	public Vector2 sub(Vector2 dest, Vector4 vector)
	{
		dest.v[0] = v[0] - vector.v[0];
		dest.v[1] = v[1] - vector.v[1];
		return dest;
	}
	
	// Multiply
	public Vector2 mul(Vector2 dest, float f)
	{
		dest.v[0] = v[0] * f;
		dest.v[1] = v[1] * f;
		return dest;
	}
	
	public Vector2 mul(Vector2 dest, float x, float y)
	{
		dest.v[0] = v[0] * x;
		dest.v[1] = v[1] * y;
		return dest;
	}
	
	public Vector2 mul(Vector2 dest, float[] vector)
	{
		dest.v[0] = v[0] * vector[0];
		dest.v[1] = v[1] * vector[1];
		return dest;
	}
	
	public Vector2 mul(Vector2 dest, Vector4 vector)
	{
		dest.v[0] = v[0] * vector.v[0];
		dest.v[1] = v[1] * vector.v[1];
		return dest;
	}
	
	// Divide
	public Vector2 div(Vector2 dest, float f)
	{
		dest.v[0] = v[0] / f;
		dest.v[1] = v[1] / f;
		return dest;
	}
	
	public Vector2 div(Vector2 dest, float x, float y)
	{
		dest.v[0] = v[0] / x;
		dest.v[1] = v[1] / y;
		return dest;
	}
	
	public Vector2 div(Vector2 dest, float[] vector)
	{
		dest.v[0] = v[0] / vector[0];
		dest.v[1] = v[1] / vector[1];
		return dest;
	}
	
	public Vector2 div(Vector2 dest, Vector4 vector)
	{
		dest.v[0] = v[0] / vector.v[0];
		dest.v[1] = v[1] / vector.v[1];
		return dest;
	}
	
	// Dot product
	public float dot(float x, float y, float z)
	{
		return v[0] * x + v[1] * y;
	}
	
	public float dot(float[] vector)
	{
		return v[0] * vector[0] + v[1] * vector[1];
	}
	
	public float dot(Vector4 vector)
	{
		return v[0] * vector.v[0] + v[1] * vector.v[1];
	}
}
