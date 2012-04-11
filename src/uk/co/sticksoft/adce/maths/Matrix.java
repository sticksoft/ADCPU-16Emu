package uk.co.sticksoft.adce.maths;

import java.util.Stack;

public class Matrix
{
	public float[] m = new float[16];
	
	public Matrix() { identity(); }
	
	public void set(float _00, float _01, float _02, float _03,
					float _10, float _11, float _12, float _13,
					float _20, float _21, float _22, float _23,
					float _30, float _31, float _32, float _33)
	{
		m[ 0] = _00; m[ 1] = _01; m[ 2] = _02; m[ 3] = _03;
		m[ 4] = _10; m[ 5] = _11; m[ 6] = _12; m[ 7] = _13;
		m[ 8] = _20; m[ 9] = _21; m[10] = _22; m[11] = _23;
		m[12] = _30; m[13] = _31; m[14] = _32; m[15] = _33;
	}
	
	public void set(float[] array)
	{
		System.arraycopy(array, 0, m, 0, 16);
	}
	
	public Matrix identity()
	{
		android.opengl.Matrix.setIdentityM(m, 0);
		return this;
	}
	
	public Matrix translate(float x, float y, float z)
	{
		android.opengl.Matrix.translateM(m, 0, x, y, z);
		return this;
	}
	
	public Matrix translate(Vector3 v)
	{
		android.opengl.Matrix.translateM(m, 0, v.v[0], v.v[1], v.v[2]);
		return this;
	}
	
	public Matrix rotateX(float degrees)
	{
		android.opengl.Matrix.rotateM(m, 0, degrees, 1, 0, 0);
		return this;
	}
	
	public Matrix rotateY(float degrees)
	{
		android.opengl.Matrix.rotateM(m, 0, degrees, 0, 1, 0);
		return this;
	}
	
	public Matrix rotateZ(float degrees)
	{
		android.opengl.Matrix.rotateM(m, 0, degrees, 0, 0, 1);
		return this;
	}
	
	public Matrix rotate(float degrees, float x, float y, float z)
	{
		android.opengl.Matrix.rotateM(m, 0, degrees, x, y, z);
		return this;
	}
	
	public Matrix rotate(float degrees, Vector3 v)
	{
		android.opengl.Matrix.rotateM(m, 0, degrees, v.v[0], v.v[1], v.v[2]);
		return this;
	}
	
	public Matrix scale(float x, float y, float z)
	{
		android.opengl.Matrix.scaleM(m, 0, x, y, z);
		return this;
	}
	
	public Matrix scale(Vector3 v)
	{
		android.opengl.Matrix.scaleM(m, 0, v.v[0], v.v[1], v.v[2]);
		return this;
	}
	
	public Matrix scale(float s)
	{
		android.opengl.Matrix.scaleM(m, 0, s, s, s);
		return this;
	}
	
	public Matrix lookAt(Vector3 origin, Vector3 target, Vector3 up)
	{
		android.opengl.Matrix.setLookAtM(m, 0, origin.v[0], origin.v[1], origin.v[2], target.v[0], target.v[1], target.v[2], up.v[0], up.v[1], up.v[2]);
		return this;
	}
	
	public Matrix frustum(float left, float right, float bottom, float top, float near, float far)
	{
		android.opengl.Matrix.frustumM(m, 0, left, right, bottom, top, near, far);
		return this;
	}
	
	private Stack<float[]> stack = null;
	private Stack<float[]> pool = null;
	
	public Matrix push()
	{
		if (stack == null)
		{
			stack = new Stack<float[]>();
			pool = new Stack<float[]>();
		}
		
		float[] mat;
		
		if (pool.isEmpty())
			mat = new float[16];
		else
			mat = pool.pop();
		
		System.arraycopy(m, 0, mat, 0, 16);
		stack.push(mat);
		
		return this;
	}
	
	public Matrix pop()
	{
		if (stack == null)
		{
			stack = new Stack<float[]>();
			pool = new Stack<float[]>();
		}
		
		float[] mat = stack.pop();
		
		System.arraycopy(mat, 0, m, 0, 16);
		pool.push(mat);
		
		return this;
	}
}
