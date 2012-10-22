package uk.co.sticksoft.adce;

import android.app.*;
import android.content.*;
import android.os.*;
import android.view.*;
import android.view.View.*;
import android.view.inputmethod.*;
import android.widget.*;
import android.widget.TabHost.*;
import java.io.*;
import uk.co.sticksoft.adce.asm.*;
import uk.co.sticksoft.adce.asm2.*;
import uk.co.sticksoft.adce.cpu.*;
import uk.co.sticksoft.adce.hardware.*;
import uk.co.sticksoft.adce.help.*;

import android.view.View.OnClickListener;
import uk.co.sticksoft.adce.hardware.Console;

public class MainActivity extends Activity
{
	private CPU cpu;// = new CPU_1_1();
	
	public TabHost tabHost;
	
	private ControlTab controlTab;
	private AssemblyEditorTab asmEditor;
	private BubbleView bubbleView;
	
	private View focus;
	
	public static MainActivity me;
	
    @Override
    public void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);
        
        me = this;
        
        cpu = Options.GetCPU();
		HardwareManager.instance().clear();
        
		/*
		Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {

				public void uncaughtException(Thread p1, Throwable e)
				{
					ErrorHandling.handle(getApplicationContext(), e);
				}
			});
			*/
			
		//ErrorHandling.handle(this, null);

		// This is NOT the way you're meant to make tabs
		tabHost = new TabHost(this, null);
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
		focus = fl;
        
		// Make control tab
		
		addTab(tabHost, "Control", controlTab = new ControlTab(this));

        
        // Make assembly editor tab
        addTab(tabHost, "ASM", asmEditor = new AssemblyEditorTab(this, this));
		
		// Add test of editor v2
        
		FrameLayout tmpcon = new FrameLayout(this);
		FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.FILL_PARENT, FrameLayout.LayoutParams.FILL_PARENT);
		tmpcon.setLayoutParams(lp);
		tmpcon.addView(bubbleView = new BubbleView(this, tmpcon, asmEditor.getEditor()));
		addTab(tabHost, "ASM2", tmpcon);
		
		
        
        // Make console tab
        addTab(tabHost, "Console", Options.getConsoleView(this));
        
        
        // Make ship tab
        addTab(tabHost, "Ship", new ShipView2D(this));
        
        
        setContentView(tabHost);
		HardwareManager.instance().addDevice(new GenericClock());
        
        
        // Prepare to start
        controlTab.reset();
        
        //testMyAssembler();
        checkVersion();
        
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
    	final String currentVersion = "0.24";
    	final String currentMessage = "NEW: Fixed bug with JSR!";
    	
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
		controlTab.log(s);
		//log.append(s+'\n');
		//Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
	}
	
	private void testMyAssembler()
	{
		char[] output = new Assembler_1_1().assemble(SampleASM.notchs_example_asm);
		
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

    
    
    @Override
	public boolean onCreateOptionsMenu(Menu menu)
	{
		menu.add(Menu.NONE, 1, Menu.NONE, "Load...");
		menu.add(Menu.NONE, 2, Menu.NONE, "Save...");
		menu.add(Menu.NONE, 3, Menu.NONE, "Assemble");
		menu.add(Menu.NONE, 4, Menu.NONE, "Start/stop");
		menu.add(Menu.NONE, 5, Menu.NONE, "Help");
		menu.add(Menu.NONE, 6, Menu.NONE, "Toggle Keyboard");
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
				controlTab.toggleRunning();
				break;
		    case 5:
		    {
		    	Intent intent = new Intent(this, HelpActivity.class);
		    	startActivity(intent);
		    	break;
		    }
		    case 6:
		    {
		    	InputMethodManager imm = ((InputMethodManager)getSystemService(INPUT_METHOD_SERVICE));
		    	imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
		    	break;
		    }
		}
		
		return super.onOptionsItemSelected(item);
	}
    
    private boolean dont_autoload = false;
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
    	super.onActivityResult(requestCode, resultCode, data);
    	
    	dont_autoload = true;
    	
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
				String source = new String(buffer);
    			asmEditor.setAsm(source);
				if (bubbleView != null)
					BubbleParser.parse(source, bubbleView);
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

	
	@Override
	protected void onPause()
	{
		super.onPause();
		controlTab.stop();
		try
		{
			asmEditor.autosave();
		}
		catch (Exception ex) {}
	}
	
	public void stop()
	{
		controlTab.stop();
	}
	
	public void updateInfo()
	{
		controlTab.updateInfo();
	}
	
	@Override
	protected void onResume()
	{
		super.onResume();
		
		if (dont_autoload)
		{
			dont_autoload = false;
			return;
		}
		else
		{
			try
			{
				asmEditor.autoload();
			}
			catch (Exception ex) {}
		}
	}
	
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event)
	{
		if (keyCode == KeyEvent.KEYCODE_BACK && backHandler != null)
		{
			backHandler.run();
			backHandler = null;
			return true;
		}
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
	
	private static Runnable backHandler = null;
	public static void setBackHandler(Runnable runnable)
	{
		backHandler = runnable;
	}

}
