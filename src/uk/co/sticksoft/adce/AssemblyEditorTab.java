package uk.co.sticksoft.adce;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;

import uk.co.sticksoft.adce.cpu.CPU;
import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.text.InputType;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

public class AssemblyEditorTab extends ScrollView
{
	private TextView asmInput, asmOutput;
	private MainActivity main;
	private int textSize = 12;
	
	public AssemblyEditorTab(MainActivity main, Context context)
	{
		super(context);
		
		this.main = main;
		this.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, 480));
		this.setMinimumHeight(480);
		
		
		LinearLayout lyt = new LinearLayout(context);
		lyt.setOrientation(LinearLayout.VERTICAL);
		lyt.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		
		asmInput = new EditText(context);
		asmInput.setRawInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
		//asmInput.setMaxLines(65536);
        asmInput.setTextSize(textSize);
        asmInput.setTypeface(Typeface.MONOSPACE);
        asmInput.setHorizontallyScrolling(true);
        
        autoload();
        lyt.addView(asmInput);
        
        Button assembleButton = new Button(context);
        assembleButton.setText("Assemble");
        assembleButton.setOnClickListener(new OnClickListener()
		{
			public void onClick(View v)
			{
				assemble();
			}
		});
        lyt.addView(assembleButton);
        
        asmOutput = new TextView(context);
        lyt.addView(asmOutput);
        
        addView(lyt);
	}
	
	public void setAsm(String s)
	{
		asmInput.setText(s);
	}
	
	public String getAsm()
	{
		return asmInput.getText().toString();
	}
	
	public TextView getEditor()
	{
		return asmInput;
	}
	
    public void autosave()
    {
    	try
		{
			FileOutputStream fos = getContext().openFileOutput("asm", Context.MODE_PRIVATE);
			fos.write(asmInput.getText().toString().getBytes());
			fos.flush();
			fos.close();
		}
		catch (Exception e) // Pokemon exceptions are evil, apparently.  I think just the name is evil :s
		{
			e.printStackTrace();
		}
    }
    
    public void autoload()
    {
    	try
		{
			FileInputStream fis = getContext().openFileInput("asm");
			byte[] buffer = new byte[(int) fis.getChannel().size()];
			fis.read(buffer);
			fis.close();
			asmInput.setText(new String(buffer));
		}
		catch (Exception e) // Pokemon exceptions are evil, apparently.  I think just the name is evil :s
		{
			asmInput.setText(SampleASM.notchs_example_asm);
			e.printStackTrace();
		}
    }
    
    private float initialPinchDistance = 0, lastPinchDistance = 0;
    private int initialFontSize;
	private boolean _no_multitouch = false;
	
	private float getPinchDist(MotionEvent event)
	{
		if (_no_multitouch)
			return 0;
		
		try
		{
			Method getPointerCount = event.getClass().getMethod("getPointerCount");
			int pointercount = ((Integer)getPointerCount.invoke(event)).intValue();
			
			if (pointercount == 0)
				return 0;
			
			Method getXI = event.getClass().getMethod("getX", new Class[] { int.class });
			Method getYI = event.getClass().getMethod("getY", new Class[] { int.class });
			
			float[] xs = new float[pointercount];
			float[] ys = new float[pointercount];
			
			for (int i = 0; i < pointercount; i++)
			{
				xs[i] = ((Float)getXI.invoke(event, Integer.valueOf(i))).floatValue();
				ys[i] = ((Float)getYI.invoke(event, Integer.valueOf(i))).floatValue();
			}
			
			float maxDistSq = 0;
			for (int j = pointercount; j-->0;)
				for (int i = j; i-->0;)
				{
					float dx = xs[j] - xs[i], dy = ys[j] - ys[i];
					float distSq = dx*dx + dy*dy;
					if (distSq > maxDistSq)
						maxDistSq = distSq;
				}
			return (float)Math.sqrt(maxDistSq);
		}
		catch (Exception e)
		{
			_no_multitouch = true;
		}
		return 0;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		switch (event.getAction())
		{
		case MotionEvent.ACTION_DOWN:
			lastPinchDistance = 0;
			initialPinchDistance = 0;
			initialFontSize = textSize;
			break;
		case MotionEvent.ACTION_MOVE:
			float pinch = getPinchDist(event);
			if (pinch > 40)
			{
				if (initialPinchDistance == 0)
				{
					initialPinchDistance = pinch;
					initialFontSize = textSize;
				}
				else
				{
					if (Math.abs(lastPinchDistance - pinch) > 40) // Filter huge jumps
					{
						initialPinchDistance = pinch;
						initialFontSize = textSize;
					}
					else
					{
						int font = (int)(initialFontSize * pinch / initialPinchDistance);
						if (font != textSize)
							asmInput.setTextSize(textSize = font);
					}
						
				}
			}
			else
				initialPinchDistance = 0;
			lastPinchDistance = pinch;
			
			if (pinch > 0)
				return true;
			
			break;
		case MotionEvent.ACTION_UP:
			lastPinchDistance = 0;
			initialPinchDistance = 0;
			break;
		}
		
		return super.onTouchEvent(event);
	}
    
    public void assemble()
    {
    	synchronized (assemblerLock)
		{
    		if (assembling)
    			return;
    		assembling = true;
		}
    	
		new AssembleTask().execute(asmInput.getText().toString());
    }
    
    private class AssembleTask extends AsyncTask<String, Integer, Boolean>
    {
    	ProgressDialog progress;
    	
    	@Override
    	protected void onPreExecute()
    	{
    		super.onPreExecute();
    		
    		main.stop();
        	autosave();
        	main.log("Assembling...");
        	//MainActivity.showToast("Assembling...!", Toast.LENGTH_SHORT);
        	CPU cpu = Options.GetCPU();
        	cpu.reset();
        	asmOutput.setText("");
        	
        	progress = new ProgressDialog(getContext());
        	progress.setMessage("Assembling...");
        	progress.setIndeterminate(true);
        	progress.show();
    	}
    	
    	private ArrayList<String> messages = new ArrayList<String>();
    	String output;
    	String messageOutput;
    	
		@Override
		protected Boolean doInBackground(String... source)
		{
			try
			{
				HashMap<Integer,String> debugSymbols = new HashMap<Integer,String>();
				
		    	char[] assembled = Options.getAssembler().assemble(source[0], messages, debugSymbols);
		    	
		    	System.arraycopy(assembled, 0, Options.GetCPU().RAM, 0, assembled.length);
		    	main.setAssembled(assembled);
		    	
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
		    	
		    	sb.append('\n');
		    	
		    	int words = 0;
		    	for (int i = 0; i < assembled.length; i++)
		    	{
		    		Integer intobj = Integer.valueOf(i);
		    		if (debugSymbols.containsKey(intobj))
		    		{
		    			sb.append("\n\n").append(debugSymbols.get(intobj)).append('\n');
		    			words = 0;
		    		}
		    		
		    		if (words == 0)
		    			sb.append(String.format("%04x: ", i));
		    		else
		    			sb.append(' ');
		    		
		    		sb.append(String.format("%04x", (int)assembled[i]));
		    		words++;
		    		if (words == 4 || (i > 0 && ((i%4) == 0)))
		    		{
		    			sb.append('\n');
		    			words = 0;
		    		}
		    	}
		    	
		    	output = sb.toString();
		    	
		    	// Now do messages
		    	sb.setLength(0);
		    	for (String s : messages)
		    		sb.append(s).append('\n');
		    			
		    	messageOutput = sb.toString();
		    	
		    	return true;
			}
			catch (Exception ex)
			{
				Log.e("ASM", "Assembling failed!", ex);
				MainActivity.showToast("Assembling failed! "+ex.getClass().getSimpleName(), Toast.LENGTH_LONG);
				return false;
			}
		}
		
		@Override
		protected void onPostExecute(Boolean result)
		{
			if (result)
			{
				/*
		    	for (int i = 0; i < messages.size(); i++)
		    	{
		    		main.log(messages.get(i));
		    		asmOutput.append(messages.get(i) + "\n");
		    	}*/
				
				
				main.log(messageOutput);
				asmOutput.append(messageOutput);
		    	
		    	asmOutput.append(output);
		    	
		    	main.log("");
		    	main.updateInfo();
		    	
		    	//MainActivity.showToast("Assembling finished!", Toast.LENGTH_LONG);
			}
			
			progress.hide();
			
			synchronized (assemblerLock)
			{
				assembling = false;
			}
		}
		
		
    }
    
    private static boolean assembling = false;
    private static Object assemblerLock = new Object();

}
