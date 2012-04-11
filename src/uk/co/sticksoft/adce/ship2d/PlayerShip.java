package uk.co.sticksoft.adce.ship2d;

import java.io.InputStream;

import uk.co.sticksoft.adce.cpu.CPU;
import uk.co.sticksoft.adce.cpu.CPU.Observer;
import uk.co.sticksoft.adce.maths.Vector2;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RadialGradient;
import android.graphics.Shader;
import android.graphics.drawable.GradientDrawable;

public class PlayerShip implements Observer
{
	public Vector2 position, velocity;
	public float rotation, angularMomentum;
	public float hull, shields;
	public CPU cpu;
	
	private Bitmap bmp;
	private Paint shieldPaint;
	
	public PlayerShip(Context context, CPU cpu)
	{
		this.cpu = cpu;
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
	}
	
	private float throttleControl, yawControl;
	
	Vector2 temp = new Vector2();
	public void update(float seconds)
	{
		position.add(velocity.mul(temp, seconds));
		
		rotation += yawControl * seconds;
		
		float speed = 1000.0f;
		
		float target_xv = (float)Math.cos(rotation) * speed, target_yv = (float)Math.sin(rotation) * speed;
		float current_xv = velocity.v[0], current_yv = velocity.v[1];
		
		float change = seconds * throttleControl;
		
		velocity.v[0] = current_xv + (target_xv - current_xv) * change;
		velocity.v[1] = current_yv + (target_yv - current_yv) * change;
	}
	
	
	public void render(Canvas canvas)
	{
		canvas.save();
		canvas.rotate((float)Math.toDegrees(rotation));
		canvas.drawBitmap(bmp, -bmp.getWidth()/2, -bmp.getHeight()/2, null);
		canvas.drawCircle(0, 0, Math.max(bmp.getWidth(), bmp.getHeight()), shieldPaint);
		canvas.restore();
	}

	public final int NAVI_START = 0xAD00;
	public final int NAVI_THROTTLE = 	NAVI_START + 0;
	public final int NAVI_PITCH = 		NAVI_START + 1;
	public final int NAVI_YAW = 		NAVI_START + 2;
	public final int NAVI_ROLL = 		NAVI_START + 3;

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
	
	@Override
	public void onCpuExecution(CPU cpu)
	{
		throttleControl = unsignedToScalar(cpu.RAM[NAVI_THROTTLE]);
		yawControl = unsignedToScalar(cpu.RAM[NAVI_YAW]);
	}
}
