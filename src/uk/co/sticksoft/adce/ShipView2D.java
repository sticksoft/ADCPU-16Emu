package uk.co.sticksoft.adce;

import java.io.InputStream;
import java.util.HashSet;
import java.util.Random;

import uk.co.sticksoft.adce.cpu.CPU;
import uk.co.sticksoft.adce.cpu.CPU.Observer;
import uk.co.sticksoft.adce.maths.Vector2;
import uk.co.sticksoft.adce.ship2d.Asteroid;
import uk.co.sticksoft.adce.ship2d.PlayerShip;
import uk.co.sticksoft.adce.ship2d.Star;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.view.View;
import android.widget.FrameLayout;
import uk.co.sticksoft.adce.ship2d.*;

public class ShipView2D extends View implements Observer
{
	//public PlayerShip player;
	public Ship2DEnvironment env;
	public final static float starBoxSize = 900;
	private HashSet<Star> stars;
	//private HashSet<Asteroid> asteroids;
	
	Random rand = new Random();
	private long lastUpdate;
	
	public ShipView2D(Context context, CPU cpu)
	{
		super(context);
		
		env = new Ship2DEnvironment(context, cpu);
		//player = new PlayerShip(context, env, cpu);
		
		stars = new HashSet<Star>();
		for (int i = 0; i < 200; i++)
			stars.add(new Star(
					new Vector2((rand.nextFloat()-0.5f) * starBoxSize * 2.0f, (rand.nextFloat()-0.5f) * starBoxSize * 2.0f),
					Color.argb(128 + rand.nextInt(127), 240 + rand.nextInt(15), 240 + rand.nextInt(15), 240 + rand.nextInt(15)),
					rand.nextFloat() + 0.5f));
		
		/*
		Bitmap aster = loadBitmapAsset(context, "asteroid.png");
		asteroids = new HashSet<Asteroid>();
		for (int i = 0; i < 10; i++)
			asteroids.add(new Asteroid(
					new Vector2((rand.nextFloat()-0.5f) * starBoxSize * 2.0f, (rand.nextFloat()-0.5f) * starBoxSize * 2.0f),
					aster,
					Color.argb(128 + rand.nextInt(127), 240 + rand.nextInt(15), 240 + rand.nextInt(15), 240 + rand.nextInt(15)),
					rand.nextFloat() + 0.5f));
		*/
		cpu.addObserver(this);
		
		lastUpdate = System.currentTimeMillis();
	}
	
	public HashSet<Asteroid> getAsteroids()
	{
		return env.asteroids;
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
	
	private boolean rendering = false;
	
	@Override
	protected void onAttachedToWindow()
	{
		super.onAttachedToWindow();
		rendering = true;
	}
	
	@Override
	protected void onDetachedFromWindow()
	{
		super.onDetachedFromWindow();
		rendering = false;
	}
	
	@Override
	protected void onDraw(Canvas canvas)
	{
		if (!rendering)
			return;
		
		canvas.drawRGB(0, 0, 20);
		
		canvas.save();
		canvas.translate(getMeasuredWidth() / 2, getMeasuredHeight() / 2);
		
		canvas.save();
		canvas.translate(-env.player.position.x(), -env.player.position.y());
		
		for (Star s : stars)
		{
			s.update(0, env.player, starBoxSize);
			s.render(canvas);
		}
		
		for (Asteroid a : env.asteroids)
			a.render(canvas);
		
		canvas.restore();
		
		env.player.render(canvas);
		
		canvas.restore();
		
		if (rendering)
			postInvalidateDelayed(30);
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		// From http://stackoverflow.com/questions/2159320/how-to-size-an-android-view-based-on-its-parents-dimensions
		int parentWidth = MeasureSpec.getSize(widthMeasureSpec);
		int parentHeight = MeasureSpec.getSize(heightMeasureSpec);
		this.setMeasuredDimension(parentWidth, parentHeight);
		this.setLayoutParams(new FrameLayout.LayoutParams(parentWidth,parentHeight));
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	@Override
	public void onCpuExecution(CPU cpu)
	{
		long time = System.currentTimeMillis();
		long elapsed = time - lastUpdate;
		if (elapsed >= 20)
		{
			lastUpdate = time;
			if (rendering)
			    postInvalidate();
			env.update((float)elapsed);
		}
	}
}
