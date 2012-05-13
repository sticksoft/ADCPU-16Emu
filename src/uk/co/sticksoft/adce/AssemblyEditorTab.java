package uk.co.sticksoft.adce;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import uk.co.sticksoft.adce.asm.Assembler_1_1;
import uk.co.sticksoft.adce.cpu.CPU;
import android.content.Context;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class AssemblyEditorTab extends ScrollView
{
	private CPU cpu;
	private TextView asmInput, asmOutput;
	private MainActivity main;
	
	public AssemblyEditorTab(MainActivity main, Context context, CPU cpu)
	{
		super(context);
		
		this.main = main;
		this.cpu = cpu;
		
		LinearLayout lyt = new LinearLayout(context);
		lyt.setOrientation(LinearLayout.VERTICAL);
		
		asmInput = new EditText(context);
		asmInput.setRawInputType(InputType.TYPE_TEXT_FLAG_MULTI_LINE | InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
		//asmInput.setMaxLines(65536);
        asmInput.setTextSize(12);
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
    
    public void assemble()
    {
    	main.stop();
    	autosave();
    	main.log("Assembling...");
    	cpu.reset();
    	asmOutput.setText("");
    	
    	
    	ArrayList<String> messages = new ArrayList<String>();
		HashMap<Integer,String> debugSymbols = new HashMap<Integer,String>();
    	char[] assembled = new Assembler_1_1().assemble(asmInput.getText().toString(), messages, debugSymbols);
    	
    	for (int i = 0; i < messages.size(); i++)
    	{
    		main.log(messages.get(i));
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
    	
    	asmOutput.append(sb);
    	
    	System.arraycopy(assembled, 0, cpu.RAM, 0, assembled.length);
    	main.setAssembled(assembled);
    	main.log("");
    	main.updateInfo();
    }
}
