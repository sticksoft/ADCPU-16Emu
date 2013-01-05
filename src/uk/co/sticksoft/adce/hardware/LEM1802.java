package uk.co.sticksoft.adce.hardware;

import java.io.InputStream;

import uk.co.sticksoft.adce.Options;
import uk.co.sticksoft.adce.cpu.CPU;
import uk.co.sticksoft.adce.cpu.CPU_1_7;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.view.View;

public class LEM1802 extends View implements Device, CPU.Observer, Options.Observer
{

	public void Reset()
	{
		memAddress = 0;
		optionsChanged();
	}

	public final static int DISPLAY_W = 32, DISPLAY_H = 16;
	public final static int CHARACTER_W = 4, CHARACTER_H = 8;
	
	private final static int DISPLAY_PIXELS = DISPLAY_W * CHARACTER_W * DISPLAY_H * CHARACTER_H;
	private final static int CHARACTER_PIXELS = CHARACTER_W * CHARACTER_H;
	
	private CPU cpu;
	private int memAddress = 0;
	
	private long lastUpdate;
	
	private boolean[] font;
	private int[] palette;
	
	private Bitmap framebuffer;
	private int[] backbuffer;
	
	public LEM1802(Context context)
	{
		super(context);
		
		this.cpu = Options.GetCPU();
		
		cpu.addObserver(this);
		Options.addObserver(this);
		
		lastUpdate = System.currentTimeMillis();
		
		framebuffer = Bitmap.createBitmap(DISPLAY_W * CHARACTER_W, DISPLAY_H * CHARACTER_H, Config.ARGB_8888);
		backbuffer = new int[DISPLAY_PIXELS];
		
		font = new boolean[CHARACTER_PIXELS * 128];
		palette = new int[16];
		
		loadDefaultFont();
		loadDefaultPalette();
		updateBackbuffer();
	}
	
	Bitmap fontBmp;
	private void loadDefaultFont()
	{
		
		try
		{
			InputStream is = getResources().getAssets().open("lem1802_font.png");
			fontBmp = BitmapFactory.decodeStream(is);
			is.close();
		}
		catch (Exception ex)
		{
			// TODO: something else
			return;
		}
		
		int bmpW = fontBmp.getWidth(), bmpH = fontBmp.getHeight();
		int[] pixels = new int[bmpW * bmpH];
		fontBmp.getPixels(pixels, 0, bmpW, 0, 0, bmpW, bmpH);
		int scale = 4;
		int dst = 0;
		for (int y = 0; y < bmpH / (CHARACTER_H * scale); y++)
			for (int x = 0; x < bmpW / (CHARACTER_W * scale); x++)
			{
				for (int j = 0; j < CHARACTER_H; j++)
				{
				for (int i = 0; i < CHARACTER_W; i++)
				{
				int pix = i + x * CHARACTER_W + (j + y * CHARACTER_H) * bmpW;
				pix *= scale;
				if (dst < font.length && pix < pixels.length)
					font[dst++] = (((pixels[pix]) >> 8) & 0xff) > 128;
				else
					break;
				}
				}
			}
		
		//for (int i = 0; i < font.length && i < pixels.length; i++)
		    //font[i] = (Color.red(pixels[i]) > 127);
	}
	
	private void loadDefaultPalette()
	{
		palette[ 0] = Color.argb(255, 0x00, 0x00, 0x00);
		palette[ 1] = Color.argb(255, 0x00, 0x00, 0xaa);
		palette[ 2] = Color.argb(255, 0x00, 0xaa, 0x00);
		palette[ 3] = Color.argb(255, 0x00, 0xaa, 0xaa);
		palette[ 4] = Color.argb(255, 0xaa, 0x00, 0x00);
		palette[ 5] = Color.argb(255, 0xaa, 0x00, 0xaa);
		palette[ 6] = Color.argb(255, 0xaa, 0x55, 0x00);
		palette[ 7] = Color.argb(255, 0xaa, 0xaa, 0xaa);
		palette[ 8] = Color.argb(255, 0x55, 0x55, 0x55);
		palette[ 9] = Color.argb(255, 0x55, 0x55, 0xff);
		palette[10] = Color.argb(255, 0x55, 0xff, 0x55);
		palette[11] = Color.argb(255, 0x55, 0xff, 0xff);
		palette[12] = Color.argb(255, 0xff, 0x55, 0x55);
		palette[13] = Color.argb(255, 0xff, 0x55, 0xff);
		palette[14] = Color.argb(255, 0xff, 0xff, 0x55);
		palette[15] = Color.argb(255, 0xff, 0xff, 0xff);
	}
	
	private void loadFont(char[] RAM, int offset)
	{
		boolean[] ch = new boolean[CHARACTER_PIXELS];
		
		for (int i = 0; i < 128; i++)
		{
			int w;
			w = RAM[offset++];
			for (int j = 8; j < 16; j++)
				ch[(j-8) * CHARACTER_W] = ((w & (1 << j)) != 0);
			for (int j = 0; j < 8; j++)
				ch[1 + j * CHARACTER_W] = ((w & (1 << j)) != 0);
			
			w = RAM[offset++];
			for (int j = 8; j < 16; j++)
				ch[2 + (j-8) * CHARACTER_W] = ((w & (1 << j)) != 0);
			for (int j = 0; j < 8; j++)
				ch[3 + j * CHARACTER_W] = ((w & (1 << j)) != 0);
		}
	}
	
	private void loadPalette(char[] RAM, int offset)
	{
		for (int i = 0; i < 16; i++)
		{
			int w = RAM[offset++];
			palette[i] = Color.argb(
					255,
					((w >> 8) & 0xf) << 4,
					((w >> 4) & 0xf) << 4,
					((w >> 0) & 0xf) << 4);
		}
	}
	
	private void updateBackbuffer()
	{
		//for (int i = 0; i < backbuffer.length && i < font.length; i++)
		    //backbuffer[i] = palette[font[i]?3:15];
		
		/*
		if (memAddress == 0)
			return;*/
		
		final int stride = DISPLAY_W * CHARACTER_W;
		
		int dest_base = 0;
		int mem = memAddress;
		
		for (int j = 0; j < DISPLAY_H && mem < cpu.RAM.length; j++) for (int i = 0; i < DISPLAY_W && mem < cpu.RAM.length; i++)
		{
			dest_base = i * CHARACTER_W + j * CHARACTER_H * stride;
			
			
			int w = cpu.RAM[mem++];
			
			int c = w & 127;
			boolean b = (w & 128) != 0;
			int fg = (w >> 8) & 15;
			int bg = (w >> 12) & 15;
			/*
			int c = (mem++ - memAddress) & 127;
			int fg = 0;
			int bg = 15;
			*/
			
			int dst = dest_base;
			int src = c * CHARACTER_PIXELS;
			for (int y = 0; y < CHARACTER_H; y++)
			{
				for (int x = 0; x < CHARACTER_W; x++)
				{
					backbuffer[dst+x+y*stride] = palette[font[src++] ? fg : bg];
				}
				//dst += stride - CHARACTER_W;
			}
			
			dest_base += CHARACTER_W;
			if (dest_base % stride == 0)
				dest_base += stride * (CHARACTER_H - 1);
		}
		
		
		
		synchronized (framebuffer)
		{
			framebuffer.setPixels(backbuffer, 0, stride, 0, 0, DISPLAY_W * CHARACTER_W, DISPLAY_H * CHARACTER_H);
		}
	}
	
	@Override
	protected void onDraw(Canvas canvas)
	{
		/*
		if (memAddress == 0)
		{
			canvas.drawColor(Color.BLACK);
			return;
		}*/
		
		canvas.save();
		canvas.scale(4.0f, 4.0f);
		canvas.drawBitmap(framebuffer, 0, 0, null);
		//canvas.drawBitmap(fontBmp, 0, 0, null);
		canvas.restore();
	}
	
	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec)
	{
		setMeasuredDimension(DISPLAY_W * CHARACTER_W * 4, DISPLAY_H * CHARACTER_H * 4);
	}

	@Override
	public void onCpuExecution(CPU cpu)
	{
		long time = System.currentTimeMillis();
		if (time > lastUpdate + 50)
		{
			lastUpdate = time;
			
			updateBackbuffer();
			postInvalidate();
		}
	}

	@Override
	public void optionsChanged()
	{
		if (cpu != null)
		    cpu.removeObserver(this);
		cpu = Options.GetCPU();
		cpu.addObserver(this);
		
		loadDefaultFont();
		loadDefaultPalette();
		updateBackbuffer();
		postInvalidate();
	}

	@Override
	public char GetIDHi()
	{
		return 0x7349;
	}

	@Override
	public char GetIDLo()
	{
		return 0xf615;
	}

	@Override
	public char GetVersion()
	{
		return 0x1802;
	}

	@Override
	public char GetManuHi()
	{
		return 0x1c6c;
	}

	@Override
	public char GetManuLo()
	{
		return 0x8b36;
	}

	@Override
	public void HWI_1_7(CPU_1_7 cpu)
	{
		int A = cpu.register[CPU_1_7.A];
		int B = cpu.register[CPU_1_7.B];
		switch (A)
		{
		case 0: // MEM_MAP_SCREEN
			memAddress = B;
			break;
		case 1: // MEM_MAP_FONT
			loadFont(cpu.RAM, B);
			break;
		case 2: // MEM_MAP_PALETTE
			loadPalette(cpu.RAM, B);
			break;
		case 3: // SET_BORDER_COLOR
			break;
		case 4: // MEM_DUMP_FONT
			break;
		case 5: // MEM_DUMP_PALETTE
			break;
		}
	}
}
