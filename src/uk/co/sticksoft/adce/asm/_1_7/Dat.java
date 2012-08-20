package uk.co.sticksoft.adce.asm._1_7;

import java.util.List;
import java.util.Map;

import uk.co.sticksoft.adce.asm.Assembler_1_7;

public class Dat implements Token
{
	private char[] data;
	
	public Dat(char c)
	{
		data = new char[1];
		data[0] = c;
	}
	
	public Dat(String s)
	{
		data = s.toCharArray();
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
	}

	@Override
	public void setOrigin(char origin)
	{
	}

}
