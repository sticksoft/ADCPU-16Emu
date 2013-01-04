package uk.co.sticksoft.adce.asm._1_7;

import java.util.List;
import java.util.Map;

import uk.co.sticksoft.adce.asm.Assembler_1_7;
import uk.co.sticksoft.adce.asm2.BubbleNode;
import android.graphics.Color;

public class Label implements Token
{
	private String name;
	private char location;
	
	public Label(String name)
	{
		this.name = name;
		this.location = 0;
	}
	
	@Override
	public void writeTo(List<Character> output, Assembler_1_7 assembler)
	{
		// Labels are not written to the output, they merely modify instruction values
	}

	@Override
	public int getCharCount()
	{
		return 0; // No extra bytes will be written for this token
	}

	@Override
	public void substituteLabels(Map<String, Character> labelMap)
	{
		// Labels can't reference any other label, so no-op.
	}
	
	public String getName() { return name; }
	public char getLocation() { return location; }

	@Override
	public void setOrigin(char origin)
	{
		this.location = origin;
	}
	
	@Override
	public String toString()
	{
		return "Label: "+name;
	}

	@Override
	public BubbleNode getBubble()
	{
		return new BubbleNode(":"+name, Color.rgb(128,64,64));
	}
}
