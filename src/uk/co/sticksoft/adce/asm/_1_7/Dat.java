package uk.co.sticksoft.adce.asm._1_7;

import java.util.List;
import java.util.Map;

import uk.co.sticksoft.adce.asm.Assembler_1_7;
import uk.co.sticksoft.adce.asm2.BubbleNode;
import uk.co.sticksoft.adce.asm2.BubbleNodeProperty;
import android.graphics.Color;

public class Dat implements Token
{
	private char[] data;
	
	private String label;
	
	public Dat(char c)
	{
		data = new char[1];
		data[0] = c;
	}
	
	public Dat(String s)
	{
		data = s.toCharArray();
	}
	public Dat(String s, boolean isLabel)
	{
		if (!isLabel)
			data = s.toCharArray();
		else
		{
			data = new char[1];
			data[0] = 0;
			label = s;
		}
	}
	
	@Override
	public void writeTo(List<Character> output, Assembler_1_7 assembler)
	{
		for (char c : data)
			output.add(c);
	}

	@Override
	public int getCharCount()
	{
		return data.length;
	}

	@Override
	public void substituteLabels(Map<String, Character> labelMap)
	{
		if (label != null)
		{
			data = new char[1];
			data[0] = labelMap.get(label);
		}
	}

	@Override
	public void setOrigin(char origin)
	{
	}
	
	@Override
	public String toString()
	{
		return "Dat length "+data.length;
	}

	@Override
	public BubbleNode getBubble()
	{
		BubbleNode inst = new BubbleNode("DAT", Color.rgb(64,64,128));
		
		if (label != null)
		{
			inst.addProperty(new BubbleNodeProperty(new String(data), Color.rgb(64,64,128)));
		}
		else
		{
			if (data.length == 1)
			{
				if (data[0] >= 32 && data[0] < 127)
					inst.addProperty(new BubbleNodeProperty("'"+data[0]+"'", Color.rgb(64,64,128)));
				else
					inst.addProperty(new BubbleNodeProperty(""+(int)data[0], Color.rgb(64,64,128)));
			}
			else if (data.length > 1)
			{
				inst.addProperty(new BubbleNodeProperty(new String(data), Color.rgb(64,64,128)));
			}
		}
		
		return inst;
	}
}
