package uk.co.sticksoft.adce;

import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;

import uk.co.sticksoft.adce.asm.Assembler;
import uk.co.sticksoft.adce.cpu.CPU;
import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class MainActivity extends Activity
{
	private CPU cpu = new CPU();
	private RAMViz ramviz;
	private TextView[] statusLabels;
	private Button startButton, resetButton;
	private boolean running;
	private TextView asmInput, asmOutput, log;
	
    @Override
    public void onCreate(Bundle savedInstanceState)
	{
        super.onCreate(savedInstanceState);
        
		ScrollView scroll = new ScrollView(this);
        LinearLayout lyt = new LinearLayout(this);
        lyt.setOrientation(LinearLayout.VERTICAL);
        //setContentView(lyt);
		scroll.addView(lyt);
		setContentView(scroll);
        
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
        
        asmInput = new EditText(this);
        asmInput.setTextSize(8);
        load();
        lyt.addView(asmInput);
        
        Button assembleButton = new Button(this);
        assembleButton.setText("Assemble");
        assembleButton.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				assemble();
			}
		});
        lyt.addView(assembleButton);
        
        asmOutput = new TextView(this);
        lyt.addView(asmOutput);
        
        log = new TextView(this);
        lyt.addView(log);
        
        reset();
        
        //testMyAssembler();
    }
    
    final String notchs_example_asm =
    		"        ; Try some basic stuff\r\n" + 
    		"                      SET A, 0x30              ; 7c01 0030\r\n" + 
    		"                      SET [0x1000], 0x20       ; 7de1 1000 0020\r\n" + 
    		"                      SUB A, [0x1000]          ; 7803 1000\r\n" + 
    		"                      IFN A, 0x10              ; c00d \r\n" + 
    		"                         SET PC, crash         ; 7dc1 001a [*]\r\n" + 
    		"                      \r\n" + 
    		"        ; Do a loopy thing\r\n" + 
    		"                      SET I, 10                ; a861\r\n" + 
    		"                      SET A, 0x2000            ; 7c01 2000\r\n" + 
    		"        :loop         SET [0x2000+I], [A]      ; 2161 2000\r\n" + 
    		"                      SUB I, 1                 ; 8463\r\n" + 
    		"                      IFN I, 0                 ; 806d\r\n" + 
    		"                         SET PC, loop          ; 7dc1 000d [*]\r\n" + 
    		"        \r\n" + 
    		"        ; Call a subroutine\r\n" + 
    		"                      SET X, 0x4               ; 9031\r\n" + 
    		"                      JSR testsub              ; 7c10 0018 [*]\r\n" + 
    		"                      SET PC, crash            ; 7dc1 001a [*]\r\n" + 
    		"        \r\n" + 
    		"        :testsub      SHL X, 4                 ; 9037\r\n" + 
    		"                      SET PC, POP              ; 61c1\r\n" + 
    		"                        \r\n" + 
    		"        ; Hang forever. X should now be 0x40 if everything went right.\r\n" + 
    		"        :crash        SET PC, crash            ; 7dc1 001a [*]\r\n" + 
    		"        \r\n" + 
    		"        ; [*]: Note that these can be one word shorter and one cycle faster by using the short form (0x00-0x1f) of literals,\r\n" + 
    		"        ;      but my assembler doesn't support short form labels yet.     ";
    
	final char[] notchs_example_assembled =
	{
		0x7c01, 0x0030, 0x7de1, 0x1000, 0x0020, 0x7803, 0x1000, 0xc00d,
		0x7dc1, 0x001a, 0xa861, 0x7c01, 0x2000, 0x2161, 0x2000, 0x8463,
		0x806d, 0x7dc1, 0x000d, 0x9031, 0x7c10, 0x0018, 0x7dc1, 0x001a,
		0x9037, 0x61c1, 0x7dc1, 0x001a, 0x0000, 0x0000, 0x0000, 0x0000,
	};
	
	private void log(String s)
	{
		log.append(s+'\n');
	}
	
	private void testMyAssembler()
	{
		char[] output = new Assembler().assemble(notchs_example_asm);
		
		if (output == null)
		{
			log("Assembled output was null.");
			return;
		}
		
		int correct = 0, incorrect = 0;
		for (int i = 0; i < output.length || i < notchs_example_assembled.length; i++)
		{
			char actual, target;
			if (i < output.length) actual = output[i]; else actual = 0;
			if (i < notchs_example_assembled.length) target = notchs_example_assembled[i]; else target = 0;
			
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
    
    private void reset()
    {
		stop();
    	cpu.reset();
    	

    	
    	System.arraycopy(notchs_example_assembled, 0, cpu.RAM, 0, notchs_example_assembled.length);
    	
    	updateInfo();
    }
    
    private void start()
    {
    	log("Starting...\n");
    	startButton.setText("Pause");
    	running = true;
    	update();
    }
    
    private void stop()
    {
    	running = false;
    	startButton.setText("Start");
    }
    
    private void save()
    {
    	try
		{
			FileOutputStream fos = openFileOutput("asm", MODE_PRIVATE);
			fos.write(asmInput.getText().toString().getBytes());
			fos.flush();
			fos.close();
		}
		catch (Exception e) // Pokémon exceptions are evil, apparently.  I think just the name is evil :s
		{
			e.printStackTrace();
		}
    }
    
    private void load()
    {
    	try
		{
			FileInputStream fis = openFileInput("asm");
			byte[] buffer = new byte[(int) fis.getChannel().size()];
			fis.read(buffer);
			fis.close();
			asmInput.setText(new String(buffer));
		}
		catch (Exception e) // Pokémon exceptions are evil, apparently.  I think just the name is evil :s
		{
			asmInput.setText(notchs_example_asm);
			e.printStackTrace();
		}
    }
    
    private void assemble()
    {
    	stop();
    	save();
    	log("Assembling...");
    	cpu.reset();
    	asmOutput.setText("");
    	
    	
    	ArrayList<String> messages = new ArrayList<String>();
    	char[] assembled = new Assembler().assemble(asmInput.getText().toString(), messages);
    	
    	for (int i = 0; i < messages.size(); i++)
    	{
    		log(messages.get(i));
    		asmOutput.append(messages.get(i) + "\n");
    	}
    	
    	StringBuilder sb = new StringBuilder();
    	for (int i = 0; i < assembled.length; i += 8)
    	{
    		sb.append(String.format("%04x", i)).append(":");
    		for (int j = 0; j < 8; j++)
    		{
    			if (i+j < assembled.length)
    				sb.append(" ").append(String.format("%04x", (int)assembled[i+j]));
    			else
    				sb.append(" 0000");
    		}
    		sb.append('\n');
    	}
    	asmOutput.append(sb);
    	
    	System.arraycopy(assembled, 0, cpu.RAM, 0, assembled.length);
    	log("");
    }
    
    private void update()
    {
    	if (!running)
    		return;
    	
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
