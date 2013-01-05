package uk.co.sticksoft.adce.ship2d;

import uk.co.sticksoft.adce.maths.Vector2;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;

public class Asteroid
{
	public Vector2 position, velocity;
	public float rotation, angularMomentum;
	
	public int colour;
	public float size;
	private Paint paint;
	private Bitmap bmp;
	
	public Asteroid(Vector2 position, Bitmap bmp, int colour, float size)
	{
		this.position = position;
		this.bmp = bmp;
		this.colour = colour;
		this.size = size;

		this.velocity = new Vector2(100.0f * (float)(Math.random() - 0.5), 100.0f * (float)(Math.random() - 0.5));
		this.rotation = (float)Math.random();
		this.angularMomentum = (0.5f + (float)Math.random()) * (Math.random() < 0.5 ? 1 : -1);
		
		paint = new Paint();
		//paint.setColorFilter(new PorterDuffColorFilter(colour, Mode.MULTIPLY));
	}
	
	private Vector2 temp = new Vector2();
	public void update(float seconds, PlayerShip ship, float boxSize)
	{
		position.add(velocity.mul(temp, seconds));
		rotation += angularMomentum * seconds;
		
		while (position.x() < ship.position.x() - boxSize)
			position.add(boxSize*2, 0);
		while (position.x() > ship.position.x() + boxSize)
			position.sub(boxSize*2, 0);
		
		while (position.y() < ship.position.y() - boxSize)
			position.add(0, boxSize*2);
		while (position.y() > ship.position.y() + boxSize)
			position.sub(0, boxSize*2);
	}
	
	
	public void render(Canvas canvas)
	{
		canvas.save();
		canvas.translate(position.x(), position.y());
		canvas.rotate((float)Math.toDegrees(rotation));
		canvas.drawBitmap(bmp, -bmp.getWidth()/2, -bmp.getHeight()/2, paint);
		canvas.restore();
	}
}
