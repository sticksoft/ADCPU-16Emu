package uk.co.sticksoft.adce.hardware;

import java.io.InputStream;
import java.util.ArrayList;

import uk.co.sticksoft.adce.R;
import uk.co.sticksoft.adce.cpu.CPU;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.View;

public class Console extends View implements CPU.Observer
{
	private CPU cpu;
	public final static int KEYBOARD_MEM_ADDRESS = 0x8000;
	
	public final static int DISPLAY_W = 32, DISPLAY_H = 16;
	public final static int CHARACTER_W = 4, CHARACTER_H = 8;
	
	public Console(Context context, CPU cpu)
	{
		super(context);
		
		this.cpu = cpu;
		
		cpu.addObserver(this);
	}
	
	private int decodeColour(int col)
	{
		int highlight = (col >> 3) & 1, r = (col >> 2) & 1, g = (col >> 1) & 1, b = col & 1;
		int ret = 0xFF000000;
		if (r == 1)
			ret += (highlight == 0) ? 0x007f0000 : 0x00FF0000; 
		if (g == 1)
			ret += (highlight == 0) ? 0x00007f00 : 0x0000FF00;
		if (b == 1)
			ret += (highlight == 0) ? 0x0000007f : 0x000000FF;
		
		return ret;
	}
	
	private float[][] charmap;
	
	private Bitmap[] bmpChars;
	
	private void loadFont()
	{
		charmap = new float[128][];
		bmpChars = new Bitmap[128];
		
		Bitmap fontBmp;
		try
		{
			InputStream is = getResources().getAssets().open("ascii.png");
			fontBmp = BitmapFactory.decodeStream(is);
			is.close();
		}
		catch (Exception ex)
		{
			// TODO: something else
			return;
		}
		ArrayList<Float> buffer = new ArrayList<Float>();
		int charIndex = 0;
		for (int y = 0; y < 8; y++)
			for (int x = 0; x < 16; x++)
			{
				buffer.clear();
				Bitmap bmp = Bitmap.createBitmap(4, 8, Config.ARGB_8888);
				for (int j = 0; j < CHARACTER_H; j++)
					for (int i = 0; i < CHARACTER_W; i++)
					{
						int px_x = x*CHARACTER_W + i, px_y = y*CHARACTER_H + j; 
						int col = fontBmp.getPixel(px_x, px_y);
						if (Color.red(col) > 0x7f)
						{
							bmp.setPixel(i, j, Color.argb(0xFF, (charIndex * 64) % 256, (charIndex * 128) % 256, (charIndex * 32) % 256));
							buffer.add(Float.valueOf((float)i));
							buffer.add(Float.valueOf((float)j));
						}
					}
				
				float[] flts = new float[buffer.size()];
				for (int i = 0; i < flts.length; i++)
					flts[i] = buffer.get(i);
				
				bmpChars[charIndex] = bmp;
				charmap[charIndex++] = flts;
				
			}
		
		this.backbuffer = Bitmap.createBitmap(CHARACTER_W * DISPLAY_W, CHARACTER_H * DISPLAY_H, Config.ARGB_8888);
	}
	
	private Bitmap backbuffer;
	
	@Override
	protected void onDraw(Canvas canvas)
	{
		//super.onDraw(canvas);
		
		Paint p = new Paint(); p.setColor(Color.CYAN);
		canvas.drawRect(0, 0, DISPLAY_W * CHARACTER_W * 4, DISPLAY_H * CHARACTER_H * 4, p);
		
		if (charmap == null)
			loadFont();
		
		drawToBackbuffer();
		
		canvas.save();
		canvas.scale(4.0f, 4.0f);
		canvas.drawBitmap(backbuffer, 0, 0, null);
		canvas.restore();
	}
	
	private void drawToBackbuffer()
	{
		Canvas canvas = new Canvas(backbuffer);
		
		int fgcol = Color.WHITE, bgcol = Color.BLACK;
		int canvas_x = 0, canvas_y = 0, dataIndex = KEYBOARD_MEM_ADDRESS;
		
		Paint bgpaint = new Paint();
		bgpaint.setColor(bgcol);
		
		Paint fgpaint = new Paint();
		fgpaint.setColor(fgcol);

		int debug_c = 0;
		for (int y = DISPLAY_H; y-->0;)
		{
			canvas.save();
			for (int x = DISPLAY_W; x-->0;)
			{
				char character = cpu.RAM[dataIndex++];
				int fgcolour = character >> 12, bgcolour = (character >> 8) & 0xF;
				int c = character & 0xFF;
				
				//bgpaint.setColor(Color.argb(255, 0, (debug_c * 32) % 256, (debug_c * 2) % 256));// decodeColour(bgcolour));
				bgpaint.setColor(decodeColour(bgcolour));
				canvas.drawRect(canvas_x, canvas_y, canvas_x + CHARACTER_W, canvas_y + CHARACTER_H, bgpaint);
				
				debug_c++;//c = debug_c++;
				
				if (c >= 0 && c < charmap.length && charmap[c] != null && charmap[c].length > 0)
				{
					fgpaint.setColor(decodeColour(fgcolour));
					//fgpaint.setStrokeWidth(0.9f);
					canvas.drawPoints(charmap[c], fgpaint);
					
					//canvas.drawBitmap(bmpChars[c], 0, 0, null);
				}
				
				canvas.translate(CHARACTER_W, 0);
			}
			canvas.restore();
			canvas.translate(0, CHARACTER_H);
		}
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		setMeasuredDimension(DISPLAY_W * CHARACTER_W * 4, DISPLAY_H * CHARACTER_H * 4);
	}

	@Override
	public void onCpuExecution(CPU cpu)
	{
		postInvalidate();
	}
}
