package uk.co.sticksoft.adce;

import java.util.HashSet;
import java.util.Random;

import uk.co.sticksoft.adce.cpu.CPU;
import uk.co.sticksoft.adce.cpu.CPU.Observer;
import uk.co.sticksoft.adce.maths.Vector2;
import uk.co.sticksoft.adce.ship2d.PlayerShip;
import uk.co.sticksoft.adce.ship2d.Star;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.view.View;
import android.widget.FrameLayout;

public class ShipView2D extends View implements Observer
{
	PlayerShip player;
	float starBoxSize = 900;
	HashSet<Star> stars;
	
	Random rand = new Random();
	
	public ShipView2D(Context context, CPU cpu)
	{
		super(context);
		player = new PlayerShip(context, cpu);
		stars = new HashSet<Star>();
		for (int i = 0; i < 200; i++)
			stars.add(new Star(
					new Vector2((rand.nextFloat()-0.5f) * starBoxSize * 2.0f, (rand.nextFloat()-0.5f) * starBoxSize * 2.0f),
					Color.argb(128 + rand.nextInt(127), 240 + rand.nextInt(15), 240 + rand.nextInt(15), 240 + rand.nextInt(15)),
					rand.nextFloat() + 0.5f));
		
		cpu.addObserver(this);
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
		player.render(canvas);
		
		canvas.translate(-player.position.x(), -player.position.y());
		for (Star s : stars)
		{
			s.update(0, player, starBoxSize);
			s.render(canvas);
		}
		
		canvas.restore();
		
		player.update(1.0f / 30.0f);
		
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
		postInvalidate();
	}
}
