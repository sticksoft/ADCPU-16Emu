package uk.co.sticksoft.adce.maths;

public class Vector3 extends Vector4
{
	///////////////////////////////////////////////////////////////////////////
	// Constructors
	///////////////////////////////////////////////////////////////////////////

	public Vector3()
	{
		super();
	}

	public Vector3(float x, float y, float z)
	{
		super(x, y, z, 0);
	}

	public Vector3(float[] vector)
	{
		super(vector[0], vector[1], vector[2], 0);
	}

	public Vector3(Vector4 vector)
	{
		super(vector.v[0], vector.v[1], vector.v[2], 0);
	}
	
	///////////////////////////////////////////////////////////////////////////
	// Mutators
	///////////////////////////////////////////////////////////////////////////
	
	// Add
	public Vector3 add(float f)
	{
		v[0] += f;
		v[1] += f;
		v[2] += f;
		return this;
	}
	
	public Vector3 add(float x, float y, float z)
	{
		v[0] += x;
		v[1] += y;
		v[2] += z;
		return this;
	}
	
	public Vector3 add(float[] vector)
	{
		v[0] += vector[0];
		v[1] += vector[1];
		v[2] += vector[2];
		return this;
	}
	
	public Vector3 add(Vector4 vector)
	{
		v[0] += vector.v[0];
		v[1] += vector.v[1];
		v[2] += vector.v[2];
		return this;
	}
	
	// Subtract
	public Vector3 sub(float f)
	{
		v[0] -= f;
		v[1] -= f;
		v[2] -= f;
		return this;
	}
	
	public Vector3 sub(float x, float y, float z)
	{
		v[0] -= x;
		v[1] -= y;
		v[2] -= z;
		return this;
	}
	
	public Vector3 sub(float[] vector)
	{
		v[0] -= vector[0];
		v[1] -= vector[1];
		v[2] -= vector[2];
		return this;
	}
	
	public Vector3 sub(Vector4 vector)
	{
		v[0] -= vector.v[0];
		v[1] -= vector.v[1];
		v[2] -= vector.v[2];
		return this;
	}
	
	// Multiply
	public Vector3 mul(float f)
	{
		v[0] *= f;
		v[1] *= f;
		v[2] *= f;
		return this;
	}
	
	public Vector3 mul(float x, float y, float z)
	{
		v[0] *= x;
		v[1] *= y;
		v[2] *= z;
		return this;
	}
	
	public Vector3 mul(float[] vector)
	{
		v[0] *= vector[0];
		v[1] *= vector[1];
		v[2] *= vector[2];
		return this;
	}
	
	public Vector3 mul(Vector4 vector)
	{
		v[0] *= vector.v[0];
		v[1] *= vector.v[1];
		v[2] *= vector.v[2];
		return this;
	}
	
	// Divide
	public Vector3 div(float f)
	{
		v[0] /= f;
		v[1] /= f;
		v[2] /= f;
		return this;
	}
	
	public Vector3 div(float x, float y, float z)
	{
		v[0] /= x;
		v[1] /= y;
		v[2] /= z;
		return this;
	}
	
	public Vector3 div(float[] vector)
	{
		v[0] /= vector[0];
		v[1] /= vector[1];
		v[2] /= vector[2];
		return this;
	}
	
	public Vector3 div(Vector4 vector)
	{
		v[0] /= vector.v[0];
		v[1] /= vector.v[1];
		v[2] /= vector.v[2];
		return this;
	}
	
	public Vector3 div(Vector2 vector)
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
	public float w () { return 0; }
	
	public float lengthSq() { return v[0]*v[0] + v[1]*v[1] + v[2]*v[2]; }
	public float length() { return (float)Math.sqrt(v[0]*v[0] + v[1]*v[1] + v[2]*v[2]); }
	
	///////////////////////////////////////////////////////////////////////////
	// Methods that don't fit into any pansy design pattern
	///////////////////////////////////////////////////////////////////////////
	
	// Add
	public Vector3 add(Vector3 dest, float f)
	{
		dest.v[0] = v[0] + f;
		dest.v[1] = v[1] + f;
		dest.v[2] = v[2] + f;
		return dest;
	}
	
	public Vector3 add(Vector3 dest, float x, float y, float z)
	{
		dest.v[0] = v[0] + x;
		dest.v[1] = v[1] + y;
		dest.v[2] = v[2] + z;
		return dest;
	}
	
	public Vector3 add(Vector3 dest, float[] vector)
	{
		dest.v[0] = v[0] + vector[0];
		dest.v[1] = v[1] + vector[1];
		dest.v[2] = v[2] + vector[2];
		dest.v[3] = v[3] + vector[3];
		return dest;
	}
	
	public Vector3 add(Vector3 dest, Vector4 vector)
	{
		dest.v[0] = v[0] + vector.v[0];
		dest.v[1] = v[1] + vector.v[1];
		dest.v[2] = v[2] + vector.v[2];
		return dest;
	}
	
	// Subtract
	public Vector3 sub(Vector3 dest, float f)
	{
		dest.v[0] = v[0] - f;
		dest.v[1] = v[1] - f;
		dest.v[2] = v[2] - f;
		return dest;
	}
	
	public Vector3 sub(Vector3 dest, float x, float y, float z)
	{
		dest.v[0] = v[0] - x;
		dest.v[1] = v[1] - y;
		dest.v[2] = v[2] - z;
		return dest;
	}
	
	public Vector3 sub(Vector3 dest, float[] vector)
	{
		dest.v[0] = v[0] - vector[0];
		dest.v[1] = v[1] - vector[1];
		dest.v[2] = v[2] - vector[2];
		dest.v[3] = v[3] - vector[3];
		return dest;
	}
	
	public Vector3 sub(Vector3 dest, Vector4 vector)
	{
		dest.v[0] = v[0] - vector.v[0];
		dest.v[1] = v[1] - vector.v[1];
		dest.v[2] = v[2] - vector.v[2];
		return dest;
	}
	
	// Multiply
	public Vector3 mul(Vector3 dest, float f)
	{
		dest.v[0] = v[0] * f;
		dest.v[1] = v[1] * f;
		dest.v[2] = v[2] * f;
		return dest;
	}
	
	public Vector3 mul(Vector3 dest, float x, float y, float z)
	{
		dest.v[0] = v[0] * x;
		dest.v[1] = v[1] * y;
		dest.v[2] = v[2] * z;
		return dest;
	}
	
	public Vector3 mul(Vector3 dest, float[] vector)
	{
		dest.v[0] = v[0] * vector[0];
		dest.v[1] = v[1] * vector[1];
		dest.v[2] = v[2] * vector[2];
		return dest;
	}
	
	public Vector3 mul(Vector3 dest, Vector4 vector)
	{
		dest.v[0] = v[0] * vector.v[0];
		dest.v[1] = v[1] * vector.v[1];
		dest.v[2] = v[2] * vector.v[2];
		return dest;
	}
	
	// Divide
	public Vector3 div(Vector3 dest, float f)
	{
		dest.v[0] = v[0] / f;
		dest.v[1] = v[1] / f;
		dest.v[2] = v[2] / f;
		return dest;
	}
	
	public Vector3 div(Vector3 dest, float x, float y, float z)
	{
		dest.v[0] = v[0] / x;
		dest.v[1] = v[1] / y;
		dest.v[2] = v[2] / z;
		return dest;
	}
	
	public Vector3 div(Vector3 dest, float[] vector)
	{
		dest.v[0] = v[0] / vector[0];
		dest.v[1] = v[1] / vector[1];
		dest.v[2] = v[2] / vector[2];
		return dest;
	}
	
	public Vector3 div(Vector3 dest, Vector4 vector)
	{
		dest.v[0] = v[0] / vector.v[0];
		dest.v[1] = v[1] / vector.v[1];
		dest.v[2] = v[2] / vector.v[2];
		return dest;
	}
	
	// Dot product
	public float dot(float x, float y, float z)
	{
		return v[0] * x + v[1] * y + v[2] * z;
	}
	
	public float dot(float[] vector)
	{
		return v[0] * vector[0] + v[1] * vector[1] + v[2] * vector[2];
	}
	
	public float dot(Vector4 vector)
	{
		return v[0] * vector.v[0] + v[1] * vector.v[1] + v[2] * vector.v[2];
	}
}
