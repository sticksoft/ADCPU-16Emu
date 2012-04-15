package uk.co.sticksoft.adce.ship2d;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Random;

import uk.co.sticksoft.adce.ShipView2D;
import uk.co.sticksoft.adce.cpu.CPU;
import uk.co.sticksoft.adce.cpu.CPU.Observer;
import uk.co.sticksoft.adce.maths.Vector2;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.RadialGradient;
import android.graphics.Shader;

public class PlayerShip implements Observer
{
	public Vector2 position, velocity;
	public float rotation, angularMomentum;
	public float hull, shields;
	public CPU cpu;
	
	private Bitmap bmp;
	private Paint shieldPaint;
	private Paint mainThrusterPaint, yawThrusterPaint;
	public float mainThrusterLength;
	
	private Random rand = new Random();
	
	private ShipView2D controller;
	
	public PlayerShip(Context context, ShipView2D shipView, CPU cpu)
	{
		this.cpu = cpu;
		this.controller = shipView;
		this.position = new Vector2();
		this.velocity = new Vector2();
		
		cpu.addObserver(this);
		
		InputStream is = null;
		try
		{
			bmp = BitmapFactory.decodeStream(is = context.getAssets().open("ship.png"));
		}
		catch (Exception ex)
		{
			ex.printStackTrace();
		}
		finally
		{
			try
			{
				if (is != null)
					is.close();
			}
			catch (Exception ex)
			{
				ex.printStackTrace();
			}
		}
		
		shieldPaint = new Paint();
		//shieldPaint.setColor(Color.argb(64, 128, 128, 255));
		shieldPaint.setShader(new RadialGradient(0, 0, 70, Color.argb(0, 0, 128, 255), Color.argb(32, 128, 230, 255), Shader.TileMode.MIRROR));
		
		mainThrusterPaint = new Paint();
		mainThrusterPaint.setShader(new RadialGradient(0, 0, 70, Color.argb(0, 0, 128, 255), Color.argb(192, 128, 255, 255), Shader.TileMode.MIRROR));
		
		yawThrusterPaint = new Paint();
		yawThrusterPaint.setShader(new RadialGradient(bmp.getWidth() / 2, 0, 30, Color.argb(0, 0, 128, 255), Color.argb(192, 128, 255, 255), Shader.TileMode.MIRROR));
	}
	
	private float throttleControl, yawControl;
	
	Vector2 temp = new Vector2();
	public void update(float seconds)
	{
		position.add(velocity.mul(temp, seconds));
		
		// Too many hackish values right now, someone help me clear up this mess...
		// Also, accelerations should use SUVAT to cope with variable seconds
		
		final float maxYawChange = 1.0f; // per second
		
		// These are all scalar values between -1 and 1 inclusive
		/*
		yawFiring = yawControl - angularMomentum;
		if (yawFiring > maxYawChange) yawFiring = maxYawChange;
		else if (yawFiring < -maxYawChange) yawFiring = -maxYawChange;
		
		// Allow brisk stop
		if (yawFiring < maxYawChange * seconds && yawFiring > -maxYawChange * seconds)
			angularMomentum = yawControl;
		else
			angularMomentum += yawFiring * seconds;
		*/
		
		angularMomentum += yawControl * seconds;
		
		rotation += angularMomentum * seconds;
		
		//angularMomentum *= 0.99f - (0.05f * seconds);
		
		float speed = 100.0f;
		
		float target_xv = (float)Math.cos(rotation) * speed, target_yv = (float)Math.sin(rotation) * speed;
		float current_xv = velocity.v[0], current_yv = velocity.v[1];
		
		float change = seconds * throttleControl;
		
		velocity.v[0] = current_xv + (target_xv - current_xv) * change;
		velocity.v[1] = current_yv + (target_yv - current_yv) * change;
		
		mainThrusterLength = mainThrusterLength + (throttleControl - mainThrusterLength) * 0.1f;
	}
	
	
	
	public void render(Canvas canvas)
	{
		canvas.save();
		canvas.rotate((float)Math.toDegrees(rotation));
		
		float w = bmp.getWidth(), h = bmp.getHeight();
		
		drawMainThruster(canvas, w, h);
		drawYawThrusters(canvas, w, h);
		
		canvas.drawBitmap(bmp, -w/2, -h/2, null);
		canvas.drawCircle(0, 0, Math.max(w, h), shieldPaint);
		
		canvas.restore();
	}
	
	private Path flamePath = new Path();
	private void drawMainThruster(Canvas canvas, float w, float h)
	{
		if (mainThrusterLength <= 0)
			return;
		
		flamePath.reset();
		flamePath.moveTo(-w * 0.4f, -h * 0.12f);
		flamePath.lineTo(-w * (0.4f + (2.0f + 2.0f * rand.nextFloat()) * mainThrusterLength), 0);
		flamePath.lineTo(-w * 0.4f, h * 0.13f);
		flamePath.close();
		
		Paint paint = new Paint();
		paint.setColor(Color.BLUE);
		paint.setStyle(Style.FILL_AND_STROKE);
		canvas.drawPath(flamePath, mainThrusterPaint);
	}
	
	private void drawYawThrusters(Canvas canvas, float w, float h)
	{
		if (yawControl == 0)
			return;
		
		if (yawControl > 0)
		{
			flamePath.reset();
			flamePath.moveTo(w * 0.3f,   h * -0.1f);
			flamePath.lineTo(w * 0.35f, h * -(0.3f + (0.3f + 0.3f * rand.nextFloat()) * yawControl));
			flamePath.lineTo(w * 0.4f,  h * -0.1f);
			flamePath.close();
		}
		else
		{
			flamePath.reset();
			flamePath.moveTo(w * 0.3f,   h *  0.1f);
			flamePath.lineTo(w * 0.35f, h * (0.3f + (0.3f + 0.3f * rand.nextFloat()) * -yawControl));
			flamePath.lineTo(w * 0.4f,  h *  0.1f);
			flamePath.close();
		}
		Paint paint = new Paint();
		paint.setColor(Color.BLUE);
		paint.setStyle(Style.FILL_AND_STROKE);
		canvas.drawPath(flamePath, yawThrusterPaint);
	}
	
	// Integration with the DCPU
	// VERY rough draft

	public final static int NAVI_START = 0xAD00;
	public final static int NAVI_THROTTLE = 	NAVI_START + 0;
	public final static int NAVI_PITCH = 		NAVI_START + 1;
	public final static int NAVI_YAW = 		NAVI_START + 2;
	public final static int NAVI_ROLL = 		NAVI_START + 3;
	
	public final static int NAVI_PITCH_GYRO =	NAVI_START + 4;
	public final static int NAVI_YAW_GYRO = 	NAVI_START + 5;
	public final static int NAVI_ROLL_GYRO = 	NAVI_START + 6;
	
	public final static int SENS_START = 0xAD10;
	public final static int SENS_CONTROL = 	SENS_START + 0;
	public final static int SENS_INDEX = 		SENS_START + 1;
	public final static int SENS_X = 			SENS_START + 2;
	public final static int SENS_Y = 			SENS_START + 3;
	public final static int SENS_Z =           SENS_START + 4;
	public final static int SENS_TYPE = 		SENS_START + 5;
	public final static int SENS_SIZE = 		SENS_START + 6;
	public final static int SENS_IFF = 		SENS_START + 7;
	
	public final static int SHLD_START = 0xAD20;
	public final static int SHLD_ONOFF =		SHLD_START + 0;

	private static int unsignedToSigned(char c)
	{
		if (c < 0x8000)
			return c;
		else
			return -0x10000 + (int)c;
	}
	
	private static float unsignedToScalar(char c)
	{
		if (c < 0x8000)
			return (float)c / (float)0x7fff;
		else
			return (float)(-0x10000 + (int)c + 1) / (float)0x7fff;
	}
	
	private static char scalarToUnsigned(float f)
	{
		if (f >= 0)
			return (char)(0x7fff * Math.min(f,1.0f));
		else
		{
			float unsigned_f = (float)0xffff + (float)0x7fff * Math.max(f,-1.0f);
			int unsigned_i = (int)unsigned_f;
			char unsigned_c = (char)unsigned_i;
			return unsigned_c;
		}
	}
	
	private ArrayList<Asteroid> blips;
	
	private final static float RADAR_RANGE = 1000.0f;
	
	@Override
	public void onCpuExecution(CPU cpu)
	{
		// Navigation
		throttleControl = unsignedToScalar(cpu.RAM[NAVI_THROTTLE]);
		yawControl = unsignedToScalar(cpu.RAM[NAVI_YAW]);
		char unsigned = scalarToUnsigned(angularMomentum);
		cpu.RAM[NAVI_YAW_GYRO] = unsigned;
		
		// Sensors
		int control = cpu.RAM[SENS_CONTROL];
		if (control == 0xFFFF)
		{
			blips = new ArrayList<Asteroid>();
			for (Asteroid a : controller.getAsteroids())
			{
				if (position.sub(temp, a.position).length() < RADAR_RANGE)
					blips.add(a);
			}
					
			cpu.RAM[SENS_INDEX] = (char)blips.size();
		}
		else if (control > 0 && control <= blips.size())
		{
			Asteroid blip = blips.get(control-1);
			blip.position.sub(temp, position).div(RADAR_RANGE);
			
			// Write blip data into memory
			cpu.RAM[SENS_X] = scalarToUnsigned(temp.x());
			cpu.RAM[SENS_Z] = scalarToUnsigned(temp.y());
		}
	}
}
