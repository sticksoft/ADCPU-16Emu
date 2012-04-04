package uk.co.sticksoft.adce;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.util.AttributeSet;
import android.widget.ImageView;

public class RAMViz extends ImageView
{
	private volatile char[] RAM;
	
	private Bitmap[] buffers = new Bitmap[2];
	
	private int which;
	
	public RAMViz(Context context, char[] RAM)
	{
		super(context);
		this.RAM = RAM;
		
		buffers[0] = Bitmap.createBitmap(256, 256, Config.ARGB_8888);
		buffers[1] = Bitmap.createBitmap(256, 256, Config.ARGB_8888);
		
		setImageBitmap(buffers[0]);
		updateBuffer();
	}
	
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
				for (int i = 0; i < 256*256; i++)
				{
					char c = RAM[i];
					bitmapBuffer[i] = 0xff000000 | c;  
				}
				
				post(new Runnable() { public void run() { updateFinished(); } });
			}
		});
		
		thread.start();
	}
	
	private void updateFinished()
	{
		which = (which+1) % 2;
		Bitmap bmp = buffers[which];
		bmp.setPixels(bitmapBuffer, 0, 256, 0, 0, 256, 256);
		setImageBitmap(bmp);
		thread = null;
	}
	
}
