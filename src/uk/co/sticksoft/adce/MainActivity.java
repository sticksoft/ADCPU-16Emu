package uk.co.sticksoft.adce;

import java.io.FileInputStream;
import java.io.FileOutputStream;

import uk.co.sticksoft.adce.asm.Assembler;
import uk.co.sticksoft.adce.cpu.CPU;
import uk.co.sticksoft.adce.hardware.Console;
import uk.co.sticksoft.adce.help.HelpActivity;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TabHost;
import android.widget.TabHost.TabContentFactory;
import android.widget.TabHost.TabSpec;
import android.widget.TabWidget;
import android.widget.TextView;
import android.widget.Toast;
import uk.co.sticksoft.adce.asm2.*;

public class MainActivity extends Activity implements CPU.Observer
{
	private CPU cpu = new CPU();
	private RAMViz ramviz;
	private TextView[] statusLabels;
	private Button startButton, resetButton;
	private boolean running;
	private TextView log;
	private AssemblyEditorTab asmEditor;
	private long lastUpdateTime;
	
    @Override
    public void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);

		// This is NOT the way you're meant to make tabs
		TabHost tabHost = new TabHost(this, null);
		LinearLayout tablyt = new LinearLayout(this);
		tablyt.setOrientation(LinearLayout.VERTICAL);
		TabWidget tw = new TabWidget(this);
		tw.setId(android.R.id.tabs);
		tablyt.addView(tw);
		FrameLayout fl = new FrameLayout(this);
		fl.setId(android.R.id.tabcontent);
		tablyt.addView(fl);
		tabHost.addView(tablyt);
		tabHost.setup();
        
		// Make control tab
		final ScrollView scroll = new ScrollView(this);
        LinearLayout lyt = new LinearLayout(this);
        lyt.setOrientation(LinearLayout.VERTICAL);
		scroll.addView(lyt);
		
		addTab(tabHost, "Control", scroll);
		
		setContentView(tabHost);
        
        lyt.addView(ramviz = new RAMViz(this, cpu.RAM));
        
        statusLabels = new TextView[5];
        for (int i = 0; i < statusLabels.length; i++)
        {
        	TextView tv = new TextView(this);
        	tv.setText("...");
        	lyt.addView(tv);
        	statusLabels[i] = tv;
        }
        
        startButton = new Button(this);
        startButton.setText("Start");
        startButton.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				if (!running) start(); else stop();
			}
		});
        lyt.addView(startButton);
        
        resetButton = new Button(this);
        resetButton.setText("Reset");
        resetButton.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				reset();
			}
		});
        lyt.addView(resetButton);
        
        log = new TextView(this);
        lyt.addView(log);
        
        
        // Make assembly editor tab
        addTab(tabHost, "ASM", asmEditor = new AssemblyEditorTab(this, this, cpu));
		/*
		// Add test of editor v2
		FrameLayout tmpcon = new FrameLayout(this);
		FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.FILL_PARENT, FrameLayout.LayoutParams.FILL_PARENT);
		tmpcon.setLayoutParams(lp);
		tmpcon.addView(new BubbleView(this, tmpcon));
		addTab(tabHost, "ASM2", tmpcon);
		*/
        
        // Make console tab
        addTab(tabHost, "Console", new Console(this, cpu));
        
        
        // Make ship tab
        addTab(tabHost, "Ship", new ShipView2D(this, cpu));
        
        
        // Prepare to start
        reset();
        
        //testMyAssembler();
        checkVersion();
        
        lastUpdateTime = System.currentTimeMillis();
        cpu.addObserver(this);
    }
    
    private void addTab(TabHost tabHost, String name, final View view)
    {
		TabSpec ts = tabHost.newTabSpec(name);
		ts.setContent(new TabContentFactory()
		{
			@Override
			public View createTabContent(String tag)
			{
				return view;
			}
		});
		ts.setIndicator(name, getResources().getDrawable(R.drawable.clear));
		
		tabHost.addTab(ts);
    }
    
    private void checkVersion()
    {
    	final String currentVersion = "0.21";
    	final String currentMessage = "NEW: Added keyboard.dasm!";
    	
    	FileInputStream fis = null;
    	boolean up_to_date = false;
    	try
    	{
    		fis = openFileInput("version");
    		byte[] buffer = new byte[(int) fis.getChannel().size()];
			fis.read(buffer);
			if (currentVersion.equals(new String(buffer)))
				up_to_date = true;
    	}
    	catch (Exception ex)
    	{
    		// Don't worry
    	}
    	finally
    	{
    		if (fis != null)
    		{
    			try
    			{
    				fis.close();
    			}
    			catch (Exception ex)
    			{
    				// Not much we can do here
    			}
    		}
    	}
    	
    	if (!up_to_date)
    	{
    		Toast.makeText(this, currentMessage, Toast.LENGTH_LONG).show();
    		
    		FileOutputStream fos = null;
    		try
    		{
    			fos = openFileOutput("version", 0);
    			fos.write(currentVersion.getBytes());
    		}
    		catch (Exception ex)
    		{
    			// Don't panic!
    		}
    		finally
    		{
    			try
    			{
    				fos.close();
    			}
    			catch (Exception e)
    			{
    				// Not much we can do here
    			}
    		}
    	}
    }
    

	
	private char[] assembled = SampleASM.notchs_example_assembled;
	public void setAssembled(char[] bin)
	{
		assembled = bin;
	}
	
	public char[] getAssembled()
	{
		return assembled;
	}
	
	public void log(String s)
	{
		log.append(s+'\n');
		//Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
	}
	
	private void testMyAssembler()
	{
		char[] output = new Assembler().assemble(SampleASM.notchs_example_asm);
		
		if (output == null)
		{
			log("Assembled output was null.");
			return;
		}
		
		int correct = 0, incorrect = 0;
		for (int i = 0; i < output.length || i < SampleASM.notchs_example_assembled.length; i++)
		{
			char actual, target;
			if (i < output.length) actual = output[i]; else actual = 0;
			if (i < SampleASM.notchs_example_assembled.length) target = SampleASM.notchs_example_assembled[i]; else target = 0;
			
			if (actual == target)
			{
				log(String.format("%04x", (int)actual) + " == " + String.format("%04x", (int)target));
				correct++;
			}
			else
			{
				log(String.format("%04x", (int)actual) + " != " + String.format("%04x", (int)target));
				incorrect++;
			}
		}
		log("Assembled example: "+correct+" correct, "+incorrect+" incorrect.");
	}
    
    public void reset()
    {
		stop();
    	cpu.reset();
    	

    	
    	System.arraycopy(assembled, 0, cpu.RAM, 0, assembled.length);
    	
    	updateInfo();
    }

    private static int CYCLES_PER_SECOND = 100 * 1000;
    private static int CYCLES_PER_MILLI = CYCLES_PER_SECOND / 1000;
    private Thread cpuThread = null;
    public void start()
    {
    	log("Starting...\n");
    	startButton.setText("Pause");
    	running = true;


    	cpuThread = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				while (running)
				{
					int millis_per_loop = 10;
					long time = System.currentTimeMillis();
					for (int i = millis_per_loop; i-->0;)
					{
						for (int j = CYCLES_PER_MILLI; j-->0;) // Yes, this doesn't actually measure cycles at the moment
							cpu.execute();
						
						Thread.yield(); // Be nice
					}
					
					while (System.currentTimeMillis() < time + millis_per_loop) // If we're running too fast, wait a bit.
					{
						try { Thread.sleep(1); } catch (Exception e) { return; }
					}
				}
			}
		}, "CPU Thread");
    	
    	//cpuThread.setPriority(Thread.MIN_PRIORITY);
    	cpuThread.start();
    }
    
    public void stop()
    {
    	running = false;
    	startButton.setText("Start");
    }
    
    private long lastActualUpdate = System.currentTimeMillis(), lastCycleCount = 0;
    private String lastSpeed = "(unknown)";
    public void updateInfo()
    {
    	statusLabels[0].setText(
    			" A:"+String.format("%04x", (int)cpu.register[0]) +
    			" B:"+String.format("%04x", (int)cpu.register[1]) +
    			" C:"+String.format("%04x", (int)cpu.register[2]));
    	
    	statusLabels[1].setText(
    			" X:"+String.format("%04x", (int)cpu.register[3]) +
    			" Y:"+String.format("%04x", (int)cpu.register[4]) +
    			" Z:"+String.format("%04x", (int)cpu.register[5]));
    	
    	statusLabels[2].setText(
    			" I:"+String.format("%04x", (int)cpu.register[6]) +
    			" J:"+String.format("%04x", (int)cpu.register[7]));
    	
    	statusLabels[3].setText(
    			" PC:"+String.format("%04x", (int)cpu.PC) +
    			" SP:"+String.format("%04x", (int)cpu.SP) +
    			" O:"+String.format("%04x", (int)cpu.O));
    	
    	String speed = "(unknown)";
    	long cyclesElapsed = cpu.cycleCount - lastCycleCount;
    	long time = System.currentTimeMillis();
    	long millisElapsed = time - lastActualUpdate;
    	if (millisElapsed > 0)
    	{
    		float hz = (float)cyclesElapsed * 1000.0f / (float)millisElapsed;
    		
    		if (hz < 1000)
    			speed = String.format("%.0fHz", hz);
    		else if (hz < 1000 * 1000)
    			speed = String.format("%.1fkHz", hz / 1000.0f);
    		else if (hz < 1000 * 1000 * 1000)
    			speed = String.format("%.2fMHz", hz / (1000.0f * 1000.0f));
    		else
    			speed = String.format("%.2fGHz", hz / (1000.0f * 1000.0f * 1000.0f));
    		
    		// I'm not going above GHz, don't be silly
    	}
    	else
    		speed = lastSpeed + " (est)";
    	
    	lastActualUpdate = time;
    	lastCycleCount = cpu.cycleCount;
    	
    	statusLabels[4].setText(
    			" Cycles: "+cpu.cycleCount+" Speed: "+speed);
    	
    	ramviz.updateBuffer();
    }
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		menu.add(Menu.NONE, 1, Menu.NONE, "Load...");
		menu.add(Menu.NONE, 2, Menu.NONE, "Save...");
		menu.add(Menu.NONE, 3, Menu.NONE, "Assemble");
		menu.add(Menu.NONE, 4, Menu.NONE, "Start/stop");
		menu.add(Menu.NONE, 5, Menu.NONE, "Help");
		return super.onCreateOptionsMenu(menu);
	}
    
    private boolean loading = true;
    
    @Override
	public boolean onOptionsItemSelected(MenuItem item)
	{
		switch (item.getItemId())
		{
			case 1:
			{
				loading = true;
				Intent intent = new Intent(this, DirectoryBrowserActivity.class);
				startActivityForResult(intent, 1);
				break;
			}
			case 2:
			{
				loading = false;
				Intent intent = new Intent(this, DirectoryBrowserActivity.class);
				intent.putExtra("saving", true);
				startActivityForResult(intent, 2);
				break;
			}
			case 3:
			    asmEditor.assemble();
			    break;
		    case 4:
				if (running)
					stop();
			    else
				    start();
				break;
		    case 5:
		    {
		    	Intent intent = new Intent(this, HelpActivity.class);
		    	startActivity(intent);
		    }
		}
		
		return super.onOptionsItemSelected(item);
	}
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
    	super.onActivityResult(requestCode, resultCode, data);
    	
    	String path;
    	if (data == null || (path = data.getStringExtra("path")) == null || path.length() == 0)
    	{
    		log("No file path returned!\n");
    		return;
    	}
    	
    	if (loading)
    	{
    		log("Loading from "+path+"...");
    		try
    		{
    			FileInputStream fis = new FileInputStream(path);
    			byte[] buffer = new byte[(int) fis.getChannel().size()];
    			fis.read(buffer);
    			fis.close();
    			asmEditor.setAsm(new String(buffer));
    		}
    		catch (Exception ex)
    		{
    			log("Exception loading file: "+ex);
    		}
    	}
    	else
    	{
    		log("Saving to "+path+"...");
    		try
    		{
    			FileOutputStream fos = new FileOutputStream(path);
				fos.write(asmEditor.getAsm().getBytes());
				fos.flush();
				fos.close();
    		}
    		catch (Exception ex)
    		{
    			log("Exception saving file: "+ex);
    		}
    	}
    	
    }
    
    private Runnable updateRunnable = new Runnable()
	{
		@Override
		public void run()
		{
			updateInfo();
		}
	};

	@Override
	public void onCpuExecution(CPU cpu)
	{
		long time = System.currentTimeMillis();
		if (time > lastUpdateTime + 50)
		{
			lastUpdateTime = time;
			runOnUiThread(updateRunnable);
		}
	}
	
	@Override
	protected void onPause()
	{
		super.onPause();
		running = false;
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event)
	{
		char c = (char)(event.getUnicodeChar() & 0xFFFF);
		if (c < 32 && c != 13 && c != 10)
			return super.onKeyUp(keyCode, event);
			
		char next = cpu.RAM[0x9010];
		if (next == 0)
			next = 0x9000;
		else
		{
			next++;
			if (next >= 0x9010)
				next = 0x9000;
		}
		
		if (cpu.RAM[next] == 0)
		{
			cpu.RAM[next] = (char)(event.getUnicodeChar() & 0xFFFF);
			cpu.RAM[0x9010] = next;
		}
		
		return super.onKeyUp(keyCode, event);
	}
}
