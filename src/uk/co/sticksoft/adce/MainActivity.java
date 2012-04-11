package uk.co.sticksoft.adce;

import java.io.FileInputStream;
import java.io.FileOutputStream;

import uk.co.sticksoft.adce.asm.Assembler;
import uk.co.sticksoft.adce.cpu.CPU;
import uk.co.sticksoft.adce.hardware.Console;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
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

public class MainActivity extends Activity
{
	private CPU cpu = new CPU();
	private RAMViz ramviz;
	private TextView[] statusLabels;
	private Button startButton, resetButton;
	private boolean running;
	private TextView log;
	private AssemblyEditorTab asmEditor;
	
    @Override
    public void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);

		// This is NOT the way you're meant to make tabs
		TabHost tabHost = new TabHost(this);
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
        
        statusLabels = new TextView[4];
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
        
        // Make console tab
        addTab(tabHost, "Console", new Console(this, cpu));
        
        // Make ship tab
        addTab(tabHost, "Ship", new ShipView2D(this, cpu));
        
        // Prepare to start
        reset();
        
        //testMyAssembler();
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
		ts.setIndicator(name, new BitmapDrawable(Bitmap.createBitmap(16, 16, Config.ARGB_8888)));
		
		tabHost.addTab(ts);
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
    
    public void start()
    {
    	log("Starting...\n");
    	startButton.setText("Pause");
    	running = true;
    	update();
    }
    
    public void stop()
    {
    	running = false;
    	startButton.setText("Start");
    }
    
    
    public final static int CYCLES_PER_UPDATE = 20;
    private void update()
    {
    	if (!running)
    		return;
    	
    	for (int i = 0; i < CYCLES_PER_UPDATE; i++)
    		cpu.execute();
    	
    	updateInfo();
    	
    	if (running)
	    	startButton.postDelayed(new Runnable()
			{
				public void run()
				{
					update();
				}
			}, 50);
    }
    
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
    	
    	ramviz.updateBuffer();
    }
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		menu.add(Menu.NONE, 1, Menu.NONE, "Load...");
		menu.add(Menu.NONE, 2, Menu.NONE, "Save...");
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
				startActivityForResult(intent, 2);
				break;
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
}
