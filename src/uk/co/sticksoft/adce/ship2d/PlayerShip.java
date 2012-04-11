package uk.co.sticksoft.adce.ship2d;

import java.io.InputStream;

import uk.co.sticksoft.adce.cpu.CPU;
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

public class PlayerShip
{
	public Vector2 position, velocity;
	public float rotation;
	public float hull, shields;
	public CPU cpu;
	
	private Bitmap bmp;
	private Paint shieldPaint;
	
	public PlayerShip(Context context, CPU cpu)
	{
		this.cpu = cpu;
		this.position = new Vector2();
		this.velocity = new Vector2();
		
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
	
	public void update(float seconds)
	{
		
	}
	
	
	public void render(Canvas canvas)
	{
		canvas.drawBitmap(bmp, -bmp.getWidth()/2, -bmp.getHeight()/2, null);
		canvas.drawCircle(0, 0, Math.max(bmp.getWidth(), bmp.getHeight()), shieldPaint);
	}
}
