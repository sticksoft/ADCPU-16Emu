package uk.co.sticksoft.adce.asm._1_7;

import java.util.List;
import java.util.Map;

import uk.co.sticksoft.adce.asm.Assembler_1_7;

public class DebugToken implements Token
{
	public String token;
	public char origin;
	
	public DebugToken() {}
	public DebugToken(String token)
	{
		this.token = token;
	}
	
	public void setToken(String token)
	{
		this.token = token;
	}
	
	@Override
	public void writeTo(List<Character> output, Assembler_1_7 assembler)
	{
	}

	@Override
	public int getCharCount()
	{
		return 0;
	}

	@Override
	public void substituteLabels(Map<String, Character> labelMap)
	{
	}

	@Override
	public void setOrigin(char origin)
	{
		this.origin = origin;
	}
	
	@Override
	public String toString()
	{
		return "Debug token";//+token;
	}

}
