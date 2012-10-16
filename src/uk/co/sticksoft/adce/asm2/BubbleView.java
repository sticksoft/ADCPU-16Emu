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
import java.lang.reflect.*;
import org.apache.http.client.methods.*;
import android.app.*;
import org.apache.http.impl.auth.*;
import android.text.*;
import uk.co.sticksoft.adce.*;

public class BubbleView extends View implements TextWatcher
{

	private ArrayList<BubbleNode> roots = new ArrayList<BubbleNode>();
	private ViewGroup container;
	//private Interpreter interpreter;
	private TextView editor;
	
	public BubbleView(Context context, ViewGroup container, TextView editor)
	{
		super(context);
		
		this.container = container;
		this.editor = editor;
		
		if (editor != null)
		{
			editor.addTextChangedListener(this);
			reading = true;
			BubbleParser.parse(editor.getText().toString(), this);
		}
		//this.interpreter = new Interpreter(this);
		
		LayoutParams lp = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
		setLayoutParams(lp);
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
		try
		{
		    unparse();
		}
		catch (Exception e)
		{
			ErrorHandling.handle(getContext(), e);
		}
	}
	
	private String unparsed;
	private Thread unparseThread;
	private boolean restartUnparsing;
	private boolean reading = false, writing = false;
	private void unparse()
	{
		if (reading)
		{
			reading = false;
			return;
		}
		if (unparseThread == null)
		{
			unparseThread = new Thread(new Runnable() { public void run() { unparse_onThread(); } });
			restartUnparsing = false;
			unparseThread.start();
		}
		else
		{
			restartUnparsing = true;
		}
	}
	private void unparse_onThread()
	{
		try
		{
			do
			{
				restartUnparsing = false;
				unparsed = BubbleParser.unparse(this);
			} while (restartUnparsing);
			if (editor != null && unparsed != null)
			{
				this.post(new Runnable() { public void run()
				{
					writing = true;
					editor.setText(unparsed);
				} });
			}
		}
		catch (Exception e)
		{
			ErrorHandling.handle(getContext(), e);
		}
		unparseThread = null;
	}
	
	private final static Paint measurePaint = new Paint();
	private final static Rect bounds = new Rect();
	
	private final static float MARGIN = 2.0f;
	public PointF layoutBubble(BubbleNode b, PointF point)
	{
		b.width = 16;
		String text = b.text();
		if (text != null)
		{
			measurePaint.getTextBounds(text, 0, text.length(), bounds);
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
			//if (scale > 1.5f)
			//	alpha = 1.0f / (1.0f + (scale - 1.5f) * 10.0f);
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
			c.drawText(b.text(), b.x + 4, b.y + 12, textPaint);
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
	
	private boolean _no_multitouch = false;
	private boolean isMultitouch(MotionEvent event)
	{
		if (_no_multitouch)
			return false;
		
		int d = 0;
		try
		{
			d=1;
			Method meth = event.getClass().getMethod("getPointerCount", new Class[0]);
			d=2;
			Integer count = (Integer)meth.invoke(event);
			d=3;
			return count > 1;
		}
		catch (Exception e)
		{
			e.printStackTrace();
			String msg = ""+ d + "\n" + e.toString() + "\n" + e.getMessage() + "\n" + event.getClass().toString() + "\n";
			for (Method m : event.getClass().getMethods())
			    msg += m.toString() + "\n";
			new AlertDialog.Builder(getContext()).setMessage(msg).setPositiveButton("Ok",null).show();
			_no_multitouch = true;
			return false;
		}
	}
	
	private float getPinchDist(MotionEvent event)
	{
		if (_no_multitouch)
			return 0;
		
		try
		{
			Method getPointerCount = event.getClass().getMethod("getPointerCount");
			int pointercount = ((Integer)getPointerCount.invoke(event)).intValue();
			Method getXI = event.getClass().getMethod("getX", new Class[] { int.class });
			Method getYI = event.getClass().getMethod("getY", new Class[] { int.class });
			
			float[] xs = new float[pointercount];
			float[] ys = new float[pointercount];
			
			for (int i = 0; i < pointercount; i++)
			{
				xs[i] = ((Float)getXI.invoke(event, Integer.valueOf(i))).floatValue();
				ys[i] = ((Float)getYI.invoke(event, Integer.valueOf(i))).floatValue();
			}
			
			float maxDistSq = 0;
			for (int j = pointercount; j-->0;)
				for (int i = j; i-->0;)
				{
					float dx = xs[j] - xs[i], dy = ys[j] - ys[i];
					float distSq = dx*dx + dy*dy;
					if (distSq > maxDistSq)
						maxDistSq = distSq;
				}
			return (float)Math.sqrt(maxDistSq);
		}
		catch (Exception e)
		{
			e.printStackTrace();
			String msg = e.toString() + "\n" + e.getMessage() + "\n";
			//for (Method m : event.getClass().getMethods())
			    //msg += m.toString() + "\n";
			new AlertDialog.Builder(getContext()).setMessage(msg).setPositiveButton("Ok",null).show();
		}
		return 0;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		boolean currentlyMultitouch = isMultitouch(event);
		if (currentlyMultitouch)
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
			else if (currentlyMultitouch)
			{
				/*
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
				*/
				
				float dist = getPinchDist(event);
				
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
			String[] actions;
			if (BubbleNodeActions.clipboard == null)
				actions = new String[] { "Add Instruction", "Add comment", "Add label" };
			else
			    actions = new String[] { "Add Instruction", "Add comment", "Add label", "Paste" };
			
			new AlertDialog.Builder(getContext()).setItems(actions, new DialogInterface.OnClickListener()
			{
				@Override
				public void onClick(DialogInterface dialog, int which)
				{
					switch (which)
					{
					case 0:
					{
					    BubbleNode root = new BubbleNode();
				     	roots.add(root);
					    //root.showEdit(getContext(), BubbleView.this);
					    new InstructionBuilder(getContext(), root, BubbleView.this).show();
				    	layoutBubbles();
					    invalidate();
						break;
					}
					case 1:
					{
						BubbleNode root = new BubbleNode("; ");
						roots.add(root);
						root.showEdit(getContext(), BubbleView.this);
						layoutBubbles();
						invalidate();
						break;
					}
						case 2:
							{
								BubbleNode root = new BubbleNode(":");
								roots.add(root);
								root.showEdit(getContext(), BubbleView.this);
								layoutBubbles();
								invalidate();
								break;
							}
						case 3:
							{
								roots.add(new BubbleNode(BubbleNodeActions.clipboard));
								layoutBubbles();
								invalidate();
								break;
							}
					}
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
	
	public void beforeTextChanged(CharSequence p1, int p2, int p3, int p4)
	{
		// TODO: Implement this method
	}

	public void onTextChanged(CharSequence p1, int p2, int p3, int p4)
	{
		// TODO: Implement this method
	}

	private boolean canUpdate = true;
	public void afterTextChanged(Editable p1)
	{
		if (writing)
		{
			writing = false;
			return;
		}
		if (canUpdate)
		{
			updateWait = System.currentTimeMillis() + 1000;
			if (updateThread == null)
			{
				updateThread = new Thread(new Runnable() { public void run() {waitToUpdate();}});
				updateThread.start();
			}
		}
	}
	private long updateWait = 0;
	private Thread updateThread = null;
	private void waitToUpdate()
	{
		while (System.currentTimeMillis() < updateWait)
		{
			try
			{
				Thread.sleep(5000);
			}
			catch (Exception e)
			{
				
			}
			if (updateThread != Thread.currentThread())
				return;
		}
		
		BubbleParser.parse(editor.getText().toString(), this);
		
		if (updateThread == Thread.currentThread())
			updateThread = null;
	}
}
