package uk.co.sticksoft.adce;

import uk.co.sticksoft.adce.Options.Observer;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Bitmap.Config;
import android.text.GetChars;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class RAMViz extends ImageView implements Observer
{
	public volatile char[] RAM;
	
	private Bitmap[] buffers = new Bitmap[2];
	
	private int which;
	
	public RAMViz(Context context)
	{
		super(context);
		this.RAM = Options.GetCPU().RAM;
		
		buffers[0] = Bitmap.createBitmap(256, 256, Config.ARGB_8888);
		buffers[1] = Bitmap.createBitmap(256, 256, Config.ARGB_8888);
		
		setAdjustViewBounds(true);
		
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(512, 512);
		lp.topMargin = 50;
		setLayoutParams(lp);
		
		setImageBitmap(buffers[which = 0]);
		updateBitmapBuffer();
		updateFinished();
		//updateBuffer();
		
		Options.addObserver(this);
	}
	
	// There'a something wrong with the threading code.  I don't know what it is.
	private Thread thread;
	private int[] bitmapBuffer = new int[256*256];
	private boolean threadShouldUpdate = true;
	public void updateBuffer()
	{
		if (thread != null)
			threadShouldUpdate = true;
		else
			startUpdateThread();
	}
	
	private Runnable updateRunnable = new Runnable() { public void run() { updateFinished(); } };
	
	private int threadCounter = 0;
	private synchronized void startUpdateThread()
	{
		if (thread != null)
			return;
		
		thread = new Thread(new Runnable()
		{
			public void run()
			{
				try
				{
					int misses = 0;
					
					while (thread == Thread.currentThread()) // Can end this thread simply by starting a new one or setting to null
					{
						if (threadShouldUpdate)
						{
							misses = 0;
							
							updateBitmapBuffer();
				
							post(updateRunnable);
							
							threadShouldUpdate = false;
							
							Thread.sleep(10);
						}
						else
						{
							Thread.sleep(20);
							
							if (misses++ > 100) // Naturally die if not used for a while
							{
								Log.i("ADCPU", "RAM thread not being used; killing.");
								break;
							}
						}
					}
				}
				catch (Exception ex)
				{
					Log.i("ADCPU", "RAM thread hit exception; killing.");
				}
				
				stopUpdateThread(Thread.currentThread());
			}
		}, "RAM thread "+(threadCounter++));
		thread.setPriority(Thread.MIN_PRIORITY);
		thread.start();
	}
	
	private synchronized void stopUpdateThread(Thread t)
	{
		//if (thread == t)
			thread = null;
	}
	
	private char[] ramBackup = new char[256*256];
	private void updateBitmapBuffer()
	{
		synchronized (RAM)
		{
			System.arraycopy(RAM, 0, ramBackup, 0, RAM.length);
		}
		
		int i = 0;
		//for (int i = 256*256; i-->0;)
		for (int y = 0; y < 256; y++)
		{
			for (int x = 0; x < 256; x++)
			{
				int c = ramBackup[i];
				bitmapBuffer[i++] = ((c != 0) ? 0xff000000 | c : 0xff401010);
			}
			Thread.yield();
		}
	}
	
	private void updateFinished()
	{
		which = (which+1) % 2;
		Bitmap bmp = buffers[which];
		bmp.setPixels(bitmapBuffer, 0, 256, 0, 0, 256, 256);
		setImageBitmap(bmp);
		//invalidate();
		postInvalidate();
	}

	@Override
	public void optionsChanged()
	{
		RAM = Options.GetCPU().RAM;
	}
	
}
