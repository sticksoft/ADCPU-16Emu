package uk.co.sticksoft.adce.ship2d;

import uk.co.sticksoft.adce.maths.Vector2;
import android.graphics.Canvas;
import android.graphics.Paint;

public class Star
{
	public Vector2 position;
	public int colour;
	public float size;
	private Paint paint;
	
	public Star(Vector2 position, int colour, float size)
	{
		this.position = position;
		this.colour = colour;
		this.size = size;
		
		paint = new Paint();
		paint.setColor(colour);
		paint.setStrokeWidth(size);
	}
	
	public void update(float seconds, PlayerShip ship, float boxSize)
	{
		while (position.x() < ship.position.x() - boxSize)
			position.add(boxSize*2, 0);
		while (position.x() > ship.position.x() + boxSize)
			position.sub(boxSize*2, 0);
		
		while (position.y() < -ship.position.y() - boxSize)
			position.add(0, boxSize*2);
		while (position.y() > ship.position.y() + boxSize)
			position.sub(0, boxSize*2);
	}
	
	
	public void render(Canvas canvas)
	{
		canvas.drawPoint(position.x(), position.y(), paint);
	}
}
