package uk.co.sticksoft.adce;

import java.io.FileInputStream;
import java.io.FileOutputStream;

import uk.co.sticksoft.adce.Options.DCPU_VERSION;
import uk.co.sticksoft.adce.Options.Observer;
import uk.co.sticksoft.adce.asm.Assembler_1_1;
import uk.co.sticksoft.adce.asm2.BubbleParser;
import uk.co.sticksoft.adce.asm2.BubbleView;
import uk.co.sticksoft.adce.cpu.CPU;
import uk.co.sticksoft.adce.hardware.GenericClock;
import uk.co.sticksoft.adce.hardware.GenericKeyboard;
import uk.co.sticksoft.adce.hardware.HardwareManager;
import uk.co.sticksoft.adce.help.HelpActivity;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.inputmethod.InputMethodManager;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TabHost.TabContentFactory;
import android.widget.TabHost.TabSpec;
import android.widget.TabWidget;
import android.widget.Toast;

public class MainActivity extends Activity implements Observer
{
	private CPU cpu;// = new CPU_1_1();
	
	public TabHost tabHost;
	
	private ControlTab controlTab;
	private AssemblyEditorTab asmEditor;
	private BubbleView bubbleView;
	
	private View focus;
	
	public static MainActivity me;
	
	public static Context getCurrentContext()
	{
		return me;
	}
	
	public static MainActivity getLastInstance()
	{
		return me;
	}
	
	public static void showToast(final String text, final int longOrShort)
	{
		try
		{
			me.runOnUiThread(new Runnable()
			{
				@Override
				public void run()
				{
					Toast.makeText(getCurrentContext(), text, longOrShort).show();
				}
			});
		}
		catch (Exception ex) {}
	}
	
    @Override
    public void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);
        
        me = this;
        
        Options.loadOptions(this);
        
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

		setupTabs();
        
		HardwareManager.instance().addDevice(new GenericClock());
		HardwareManager.instance().addDevice(new GenericKeyboard());
        
        
        // Prepare to start
        controlTab.reset();
        
        //testMyAssembler();
        checkVersion();
        
        Options.addObserver(this);
        
    }
    
    private void setupTabs()
    {
		Options.onCreate();
		
		// This is NOT the way you're meant to make tabs
		tabHost = new TabHost(this, null);
		LinearLayout tablyt = new LinearLayout(this);
		tablyt.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		tablyt.setOrientation(LinearLayout.VERTICAL);
		TabWidget tw = new TabWidget(this);
		tw.setId(android.R.id.tabs);
		tablyt.addView(tw);
		FrameLayout fl = new FrameLayout(this);
		fl.setId(android.R.id.tabcontent);
		fl.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		tablyt.addView(fl);
		tabHost.addView(tablyt);
		tabHost.setup();
		focus = fl;
        
		// Make control tab
		
		addTab(tabHost, "Control", controlTab = new ControlTab(this));

        
        // Make assembly editor tab
		asmEditor = new AssemblyEditorTab(this, this);
		if (Options.IsTextEditorShown())
			addTab(tabHost, "ASM", asmEditor);
		
		// Add test of editor v2
		
		if (Options.IsVisualEditorShown())
		{
			FrameLayout tmpcon = new FrameLayout(this);
			FrameLayout.LayoutParams lp = new FrameLayout.LayoutParams(FrameLayout.LayoutParams.FILL_PARENT, FrameLayout.LayoutParams.FILL_PARENT);
			bubbleView = new BubbleView(this, tmpcon, asmEditor.getEditor());
			tmpcon.setLayoutParams(lp);
			tmpcon.addView(bubbleView);
			addTab(tabHost, "ASM2", tmpcon);
		}
		
		
        
        // Make console tab
        addTab(tabHost, "Console", Options.getConsoleView(this));
        
        if (Options.IsM35fdShown())
        {
        	// Make M35FD tab
        	addTab(tabHost, "M35FD", Options.getM35fdView(this));
        }
        
        // Make ship tab
        addTab(tabHost, "Ship", new ShipView2D(this));
        
        
        setContentView(tabHost);
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
    	final String currentVersion = "0.33";
    	final String currentMessage = "NEW: Fix for PC and divide by 0 bugs!";
    	final boolean massiveUpdate = false;
    	
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
    		if (!massiveUpdate)
    			Toast.makeText(this, currentMessage, Toast.LENGTH_LONG).show();
    		else
    			new AlertDialog.Builder(this).setTitle("Updates!").setMessage(currentMessage).setPositiveButton("SET [mind], [blown]", null).show();
    		
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
    

	
	private char[] assembled = new char[0]; //SampleASM.notchs_example_assembled;
	public void setAssembled(char[] bin)
	{
		assembled = bin;
	}
	
	public char[] getAssembled()
	{
		return assembled;
	}
	
	public void log(final String s)
	{
		runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				controlTab.log(s);
			}
		});
		//log.append(s+'\n');
		//Toast.makeText(this, s, Toast.LENGTH_SHORT).show();
	}
	
	public void logFromUIThread(String s)
	{
		controlTab.log(s);
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
		menu.add(Menu.NONE, 7, Menu.NONE, "Settings");
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
		    case 7:
		    {
		    	Intent intent = new Intent(this, OptionsPreferenceActivity.class);
		    	startActivity(intent);
		    	break;
		    }
		}
		
		return super.onOptionsItemSelected(item);
	}
    
    private boolean dont_autoload = false;
    
    public interface FileDialogCallback { void onPathSelected(String s); }
    
    private static FileDialogCallback fileDialogCallback = null;
    public static void showFileDialog(boolean saving, FileDialogCallback fdc)
    {
    	fileDialogCallback = fdc;
    	
    	if (me == null)
    		return;
    	
    	
    	Intent intent = new Intent(me, DirectoryBrowserActivity.class);
    	if (saving)
    		intent.putExtra("saving", true);
		me.startActivityForResult(intent, 2);
    }
    
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
    	
    	if (fileDialogCallback != null)
    	{
    		FileDialogCallback fdc = fileDialogCallback;
    		fileDialogCallback = null;
    		fdc.onPathSelected(path);
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
		else if (Options.GetDcpuVersion() == DCPU_VERSION._1_7)
		{
			GenericKeyboard kbd = GenericKeyboard.GetLastInstance();
			if (kbd != null)
				kbd.keyDown((char)(event.getUnicodeChar() & 0xFFFF));
		}
		
		return super.onKeyDown(keyCode, event);
	}
	
	@Override
	public boolean onKeyUp(int keyCode, KeyEvent event)
	{
		char c = (char)(event.getUnicodeChar() & 0xFFFF);
		if (c < 32 && c != 13 && c != 10)
			return super.onKeyUp(keyCode, event);
		
		if (Options.GetDcpuVersion() == DCPU_VERSION._1_1)
		{
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
		}
		else
		{
			GenericKeyboard kbd = GenericKeyboard.GetLastInstance();
			if (kbd != null)
				kbd.keyUp((char)(event.getUnicodeChar() & 0xFFFF));
		}
		
		return super.onKeyUp(keyCode, event);
	}
	
	private static Runnable backHandler = null;
	public static void setBackHandler(Runnable runnable)
	{
		backHandler = runnable;
	}
	public static void removeBackHandler(Runnable runnable)
	{
		if (backHandler == runnable)
			backHandler = null;
	}

	@Override
	public void optionsChanged()
	{
		runOnUiThread(new Runnable()
		{
			@Override
			public void run()
			{
				setupTabs();
			}
		});
	}

}
