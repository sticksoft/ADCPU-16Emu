package uk.co.sticksoft.adce.asm2;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.RadialGradient;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Region.Op;
import android.graphics.Shader.TileMode;
import android.view.MotionEvent;
import android.view.ViewGroup;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.view.*;

public class BubbleView extends View
{
	private ArrayList<BubbleNode> roots = new ArrayList<BubbleNode>();
	private ViewGroup container;
	//private Interpreter interpreter;
	
	public BubbleView(Context context, ViewGroup container)
	{
		super(context);
		
		this.container = container;
		//this.interpreter = new Interpreter(this);
		
		LayoutParams lp = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		setLayoutParams(lp);
		
		/*
		for (int i = 0; i < 5; i++)
		{
			BubbleNode root = new BubbleNode();
			for (int j = (int)(Math.random() * 5); j-->0;)
			{
				BubbleNode child = new BubbleNode();
				root.addChild(child);
				
				if (Math.random() < 0.2)
				{
					for (int k = (int)(Math.random() * 3); k-->0;)
						child.addChild(new BubbleNode());
				}
			}
			roots.add(root);
		}*/
		
		roots.add(new BubbleNode());
	}
	
	private boolean layoutChanged = true;
	
	private PointF canvasOrigin = new PointF();
	private float scale = 1.0f;
	
	@Override
	public void draw(Canvas canvas)
	{
		super.draw(canvas);
		
		canvas.drawColor(Color.DKGRAY);
		
		canvas.save();
		
		canvas.clipRect(new RectF(0, 0, getWidth(), getHeight()), Op.REPLACE);
		
		canvas.translate(getWidth() / 2, getHeight() / 2);
		
		canvas.scale(scale, scale);
		canvas.translate(canvasOrigin.x, canvasOrigin.y);
		
		
		
		
		
		if (layoutChanged)
			layoutBubbles();
		
		drawBubbles(canvas);
		
	//	interpreter.render(canvas);
	}
	
	public void layoutBubbles()
	{
		PointF origin = new PointF(0,0);
		for (int i = 0; i < roots.size(); i++)
		{
			BubbleNode root = roots.get(i);
			layoutBubble(root, origin);
			origin.x = 0;
		}
		
		layoutChanged = false;
	}
	
	private final static Paint measurePaint = new Paint();
	private final static Rect bounds = new Rect();
	
	private final static float MARGIN = 2.0f;
	public PointF layoutBubble(BubbleNode b, PointF point)
	{
		b.width = 16;
		if (b.text != null)
		{
			measurePaint.getTextBounds(b.text, 0, b.text.length(), bounds);
			final float extra = 12;
			if (bounds.width() + extra > b.width)
				b.width = bounds.width() + extra;
		}
		
		b.x = point.x;
		b.y = point.y;
		
		float maxX = b.x + b.width + MARGIN;
		float maxY = b.y + b.height + MARGIN;
		
		float childrenX = maxX;
		
		point.x = maxX;
		
		for (int i = 0; i < b.properties.size(); i++)
		{
			BubbleNode child = b.properties.get(i);
			layoutBubble(child, point);
			if (point.y > maxY)
				maxY = point.y;
			point.y = b.y;
		}
		
		maxX = Math.max(maxX, point.x);
		
		point.x = childrenX;
		point.y = maxY;
		
		for (int i = 0; i < b.children.size(); i++)
		{
			BubbleNode child = b.children.get(i);
			layoutBubble(child, point);
			
			if (point.x > maxX)
				maxX = point.x;
			
			point.x = childrenX;
		}
		
		point.x = maxX;
		
		return point;
	}
	
	public void drawBubbles(Canvas c)
	{
		if (roots.size() > 1)
		{
			if (linePaint == null)
			{
				linePaint = new Paint();
				linePaint.setColor(Color.argb(128, 255, 255, 255));
			}
			BubbleNode top = roots.get(0), bottom = roots.get(roots.size()-1);
			c.drawLine(top.x + top.width / 2, top.y + top.height / 2, bottom.x + bottom.width / 2, bottom.y + bottom.height / 2, linePaint);
		}
		for (int i = 0; i < roots.size(); i++)
			drawBubble(c, roots.get(i));
	}
	
	private Paint bubblePaint = new Paint(), textPaint = new Paint(), linePaint = new Paint(), selectedBubblePaint = new Paint();
	private float lastAlpha;
	private RadialGradient grad;
	public void drawBubble(Canvas c, BubbleNode b)
	{
		bubblePaint.setColor(Color.GREEN);
		//selectedBubblePaint.setColor(Color.BLUE);
		textPaint.setColor(Color.BLACK);
		//linePaint.setColor(Color.WHITE);
		
		if (b.children.size() > 0)
		{
			float left = b.x + b.width / 2;
			
			BubbleNode lowest = b.children.get(b.children.size()-1);
			c.drawLine(left, b.y + b.height / 2, left, lowest.y + lowest.height / 2, linePaint);
			for (int i = 0; i < b.children.size(); i++)
			{
				BubbleNode child = b.children.get(i);
				float y = child.y + child.height / 2;
				float right = child.x + child.width / 2;
				c.drawLine(left, y, right, y, linePaint);
			}
		}
		
		if (b.properties.size() > 0)
		{
			float top = b.y + b.height / 2;
			BubbleNode furthest = b.properties.get(b.properties.size()-1);
			c.drawLine(b.x + b.width / 2, top, furthest.x + furthest.width / 2, top, linePaint);
		}
		
		Paint p = bubblePaint;//b == selectedBubble ? selectedBubblePaint : bubblePaint;
		if (c.getClipBounds().intersects((int)b.x, (int)b.y, (int)(b.x+b.width), (int)(b.y+b.height)))
		{
			// Calculate colours
			float alpha = 1.0f;
			if (scale > 1.5f)
				alpha = 1.0f / (1.0f + (scale - 1.5f) * 10.0f);
			float radius = b.width * (scale) / 2.0f;
			
			boolean selected = b == selectedBubble;
			int backColour = !selected ? b.normalColour : b.selectedColour;
			int textColour = !selected ? b.normalTextColour : b.selectedTextColour;
			
			int red = Color.red(backColour), green = Color.green(backColour), blue = Color.blue(backColour);
			int c0 = Color.argb((int)(255 * alpha), red, green, blue);
			int c1 = Color.argb((int)(255 * Math.min(1, alpha * 2.0f)), red, green, blue);
			bubblePaint.setShader(new RadialGradient(b.x + b.width / 2, b.y + b.height / 2, radius, c0, c1, TileMode.CLAMP));
			
			textPaint.setColor(textColour);
			textPaint.setAlpha(55 + (int)(200 * Math.min(1, alpha * 4.0f)));
			
			// Draw
			if (alpha > 0.02f)
				c.drawRoundRect(new RectF(b.x, b.y, b.x+b.width, b.y+b.height), 10, 10, p);
			c.drawText(b.text, b.x + 4, b.y + 12, textPaint);
		}
		
		for (int i = 0; i < b.children.size(); i++)
			drawBubble(c, b.children.get(i));
		
		for (int i = 0; i < b.properties.size(); i++)
			drawBubble(c, b.properties.get(i));
	}
	
	private boolean dragging = false;
	private boolean multitouch = false;
	private PointF dragOrigin = new PointF();
	private PointF lastDragPoint = new PointF();
	
	private float lastPinchDistance = 0;
	
	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		if (event.getPointerCount() > 1)
			multitouch = true;
		
		switch (event.getAction())
		{
		case MotionEvent.ACTION_DOWN:
			dragging = false;
			multitouch = false;
			dragOrigin.x = event.getX();
			dragOrigin.y = event.getY();
			lastDragPoint.set(dragOrigin);
			lastPinchDistance = 0;
			break;
		case MotionEvent.ACTION_MOVE:
			float x = event.getX() - dragOrigin.x, y = event.getY() - dragOrigin.y;
			if (x*x + y*y > 50)
				dragging = true;
			
			if (!multitouch)
			{
				if (dragging)
				{
					final float scrollSpeed = 2.0f / scale;
					canvasOrigin.x += (event.getX() - lastDragPoint.x) * scrollSpeed;
					canvasOrigin.y += (event.getY() - lastDragPoint.y) * scrollSpeed;
				}
				
				lastDragPoint.x = event.getX();
				lastDragPoint.y = event.getY();
				
				invalidate();
			}
			else if (event.getPointerCount() > 1)
			{
				float maxDistSq = 0;
				for (int j = event.getPointerCount(); j-->0;)
					for (int i = j; i-->0;)
					{
						float dx = event.getX(j) - event.getX(i), dy = event.getY(j) - event.getY(i);
						float distSq = dx*dx + dy*dy;
						if (distSq > maxDistSq)
							maxDistSq = distSq;
					}
				float dist = (float)Math.sqrt(maxDistSq);
				
				if (dist != 0 && lastPinchDistance != 0)
				{
					float change = (dist - lastPinchDistance) / 100.0f; 
					if (change > 0)
						scale *= 1.0f + change;
					else if (change < 0)
						scale /= 1.0f - change;
				}
				
				lastPinchDistance = dist;
				invalidate();
			}
				
			break;
		case MotionEvent.ACTION_UP:
			if (!dragging && !multitouch)
				return handleTap(event);
		}
		
		
		
		return true;
	}
	
	private boolean handleTap(MotionEvent event)
	{
		if (event.getAction() != MotionEvent.ACTION_UP)
			return true;
		
		BubbleNode.supplementalTap = null;
		float x = (event.getX() - getWidth() / 2) / scale - canvasOrigin.x, y = (event.getY() - getHeight() / 2) / scale - canvasOrigin.y;
		for (int i = roots.size(); i-->0;)
		{
			BubbleNode b = roots.get(i);
			if (y < b.y - 1 || y > b.y + 1 + b.getChildrenHeight())
				continue;
			
			BubbleNode result = b.checkTap(x, y);
			if (result == null)
				result = BubbleNode.supplementalTap;
			
			if (result != null)
			{
				showBubbleMenu(result);
				return true;
			}
		}
		
		showBubbleMenu(null);
		
		return true;
	}
	
	private BubbleNode selectedBubble;
	
	private void showBubbleMenu(BubbleNode b)
	{
		selectedBubble = b;
		if (b != null)
			b.showOptions(getContext(), this);
		else
		{
			new AlertDialog.Builder(getContext()).setItems(new String[] { "Add Root", "Cancel" }, new DialogInterface.OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					if (which != 0)
						return;
					
					BubbleNode root = new BubbleNode();
					roots.add(root);
					root.showEdit(getContext(), BubbleView.this);
					
					layoutBubbles();
					invalidate();
				}
			}).show();
		}
		invalidate();
	}
	
	public ArrayList<BubbleNode> getRoots()
	{
		return roots;
	}
	
	public void onResume()
	{
		//interpreter.start();
	}
	
	public void onPause()
	{
	    //interpreter.stop();
	}
}
