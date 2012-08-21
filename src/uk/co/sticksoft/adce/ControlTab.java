package uk.co.sticksoft.adce;

import uk.co.sticksoft.adce.Options.Observer;
import uk.co.sticksoft.adce.cpu.CPU;
import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class ControlTab extends ScrollView implements CPU.Observer, Options.Observer 
{
	private RAMViz ramviz;
	private TextView statusLabel;
	private TextView cycleLabel;
	private Button startButton, stepButton, resetButton;
	private boolean running;
	private TextView log;
	private CPU cpu;
	private MainActivity mainActivity;
	
	public ControlTab(MainActivity context)
	{
		super(context);
		
		mainActivity = context;
		
		cpu = Options.GetCPU();
		cpu.addObserver(this);
		Options.addObserver(this);
		
		//final ScrollView scroll = new ScrollView(this);
        LinearLayout lyt = new LinearLayout(context);
        lyt.setOrientation(LinearLayout.VERTICAL);
		this.addView(lyt);
		
        
        lyt.addView(ramviz = new RAMViz(context));
        
        statusLabel = new TextView(context);
        statusLabel.setText("...");
    	lyt.addView(statusLabel);
    	
    	cycleLabel = new TextView(context);
    	cycleLabel.setText(" ");
    	lyt.addView(cycleLabel);
        
        startButton = new Button(context);
        startButton.setText("Start");
        startButton.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				if (!running) start(); else stop();
			}
		});
        lyt.addView(startButton);
		
		stepButton = new Button(context);
        stepButton.setText("Step");
        stepButton.setOnClickListener(new OnClickListener()
			{
				public void onClick(View v)
				{
					cpu.execute();
				}
			});
        lyt.addView(stepButton);
        
        resetButton = new Button(context);
        resetButton.setText("Reset");
        resetButton.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				reset();
			}
		});
        lyt.addView(resetButton);
        
        log = new TextView(context);
        lyt.addView(log);
        
        
        lastUpdateTime = System.currentTimeMillis();
        
        invalidate();
	}
	
	public void toggleRunning()
	{
		if (running)
			stop();
	    else
		    start();
	}
	
	public void log(String s)
	{
		log.append(s+'\n');
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
				try
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
				catch (final Exception ex)
				{
					post(new Runnable()
					{
						@Override
						public void run()
						{
							Toast.makeText(getContext(), "Exception on CPU thread!", Toast.LENGTH_SHORT).show();
							log(ex.toString());
							for (StackTraceElement ste : ex.getStackTrace())
								log(ste.toString());
							stop();
						}
					});
					
					Log.e("ADCPU", "CPU threw exception.", ex);
					cpuThread = null;
				}

			}
		}, "CPU Thread");
    	
    	//cpuThread.setPriority(Thread.MIN_PRIORITY);
    	cpuThread.start();
    }
    
    public void stop()
    {
    	running = false;
    	post(new Runnable()
		{
			@Override
			public void run()
			{
				startButton.setText("Start");
			}
		});
    }
    
    private long lastActualUpdate = System.currentTimeMillis(), lastCycleCount = 0;
    private String lastSpeed = "(unknown)";
    public void updateInfo()
    {
    	statusLabel.setText(cpu.getStatusText());
    	
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
    	
    	cycleLabel.setText(" Cycles: "+cpu.cycleCount+" Speed: "+speed);
    	
    	ramviz.updateBuffer();
    	
    	mainActivity.tabHost.postInvalidate();
    }
    
    
    public void reset()
    {
		stop();
    	cpu.reset();
    	
    	System.arraycopy(mainActivity.getAssembled(), 0, cpu.RAM, 0, mainActivity.getAssembled().length);
    	
    	updateInfo();
    }

    
    private Runnable updateRunnable = new Runnable()
	{
		@Override
		public void run()
		{
			updateInfo();
		}
	};
	
	private long lastUpdateTime;

	@Override
	public void onCpuExecution(CPU cpu)
	{
		long time = System.currentTimeMillis();
		if (time > lastUpdateTime + 50)
		{
			lastUpdateTime = time;
			mainActivity.runOnUiThread(updateRunnable);
		}
	}

	@Override
	public void optionsChanged()
	{
		cpu.removeObserver(this);
		cpu = Options.GetCPU();
		cpu.addObserver(this);
	}
	
}
