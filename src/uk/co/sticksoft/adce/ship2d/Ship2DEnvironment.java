package uk.co.sticksoft.adce.ship2d;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Random;

import uk.co.sticksoft.adce.Environment;
import uk.co.sticksoft.adce.ShipView2D;
import uk.co.sticksoft.adce.cpu.CPU;
import uk.co.sticksoft.adce.maths.Vector2;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;

public class Ship2DEnvironment implements Environment
{
    protected CPU cpu;
	public HashSet<Asteroid> asteroids;
	private Random rand = new Random();

	public PlayerShip player;
	
    public Ship2DEnvironment(Context context, CPU cpu)
	{
		this.cpu = cpu;
		
		player = new PlayerShip(context, this, cpu);
		
		Bitmap aster = loadBitmapAsset(context, "asteroid.png");
		asteroids = new HashSet<Asteroid>();
		for (int i = 0; i < 10; i++)
			asteroids.add(new Asteroid(
							  new Vector2((rand.nextFloat()-0.5f) * ShipView2D.starBoxSize * 2.0f, (rand.nextFloat()-0.5f) * ShipView2D.starBoxSize * 2.0f),
							  aster,
							  Color.argb(128 + rand.nextInt(127), 240 + rand.nextInt(15), 240 + rand.nextInt(15), 240 + rand.nextInt(15)),
							  rand.nextFloat() + 0.5f));
	}
	
	private static Bitmap loadBitmapAsset(Context context, String name)
	{
		Bitmap bmp = null;
		InputStream is = null;
		try
		{
			bmp = BitmapFactory.decodeStream(is = context.getAssets().open(name));
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
		return bmp;
	}

	public void update(float seconds)
	{
		player.update(seconds);
		
		for (Asteroid a : asteroids)
			a.update(seconds, player, ShipView2D.starBoxSize);
	}
	
	public void render(Canvas canvas)
	{
		for (Asteroid a : asteroids)
		    a.render(canvas);
	}
}
