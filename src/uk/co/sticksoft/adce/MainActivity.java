package uk.co.sticksoft.adce;

import java.io.NotSerializableException;

import uk.co.sticksoft.adce.cpu.CPU;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends Activity
{
	private CPU cpu = new CPU();
	private RAMViz ramviz;
	private TextView[] statusLabels;
	private Button startButton, resetButton;
	private boolean running;
	
    @Override
    public void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);
        
        LinearLayout lyt = new LinearLayout(this);
        lyt.setOrientation(LinearLayout.VERTICAL);
        setContentView(lyt);
        
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
				stop();
				reset();
			}
		});
        lyt.addView(resetButton);
        
        reset();
        updateInfo();
    }
    
    private void reset()
    {
    	stop();
    	cpu.reset();
    	
    	char[] notchs_example =
		{
			0x7c01, 0x0030, 0x7de1, 0x1000, 0x0020, 0x7803, 0x1000, 0xc00d,
			0x7dc1, 0x001a, 0xa861, 0x7c01, 0x2000, 0x2161, 0x2000, 0x8463,
			0x806d, 0x7dc1, 0x000d, 0x9031, 0x7c10, 0x0018, 0x7dc1, 0x001a,
			0x9037, 0x61c1, 0x7dc1, 0x001a, 0x0000, 0x0000, 0x0000, 0x0000,
		};
    	
    	System.arraycopy(notchs_example, 0, cpu.RAM, 0, notchs_example.length);
    	
    	updateInfo();
    }
    
    private void start()
    {
    	startButton.setText("Pause");
    	running = true;
    	update();
    }
    
    private void stop()
    {
    	running = false;
    	startButton.setText("Start");
    	updateInfo();
    }
    
    private void update()
    {
    	cpu.execute();
    	updateInfo();
    	
    	if (running)
	    	startButton.postDelayed(new Runnable()
			{
				public void run()
				{
					update();
				}
			}, 100);
    }
    
    private void updateInfo()
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
}
