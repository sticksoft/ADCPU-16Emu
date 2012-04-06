package uk.co.sticksoft.adce;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Bitmap.Config;
import android.util.AttributeSet;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class RAMViz extends ImageView
{
	public volatile char[] RAM;
	
	private Bitmap[] buffers = new Bitmap[2];
	
	private int which;
	
	public RAMViz(Context context, char[] RAM)
	{
		super(context);
		this.RAM = RAM;
		
		buffers[0] = Bitmap.createBitmap(256, 256, Config.ARGB_8888);
		buffers[1] = Bitmap.createBitmap(256, 256, Config.ARGB_8888);
		
		setAdjustViewBounds(true);
		
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(512, 512);
		lp.topMargin = 50;
		setLayoutParams(lp);
		
		setImageBitmap(buffers[0]);
		//updateBitmapBuffer();
		//updateFinished();
		//updateBuffer();
	}
	
	// There'a something wrong with the threading code.  I don't know what it is.
	private Thread thread;
	private int[] bitmapBuffer = new int[256*256];
	public void updateBuffer()
	{
		
		if (thread != null)
			return;
		
		thread = new Thread(new Runnable()
		{
			public void run()
			{
				updateBitmapBuffer();
				
				thread = null;
				post(new Runnable() { public void run() { updateFinished(); } });
			}
		});
		
		thread.start();
		
		
		/*
		updateBitmapBuffer();
		updateFinished();*/
	}
	
	private void updateBitmapBuffer()
	{
		for (int i = 0; i < 256*256; i++)
		{
			int c = RAM[i];
			bitmapBuffer[i] = ((c != 0) ? 0xff000000 | c : 0xff401010);  
		}
	}
	
	private void updateFinished()
	{
		which = (which+1) % 2;
		Bitmap bmp = buffers[which];
		bmp.setPixels(bitmapBuffer, 0, 256, 0, 0, 256, 256);
		setImageBitmap(bmp);
		invalidate();
		thread = null;
	}
	
}
