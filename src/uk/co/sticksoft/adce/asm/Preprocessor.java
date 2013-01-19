package uk.co.sticksoft.adce.asm;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;

import android.util.Log;

public class Preprocessor
{
	private final static String INCLUDE_DIRECTIVE = "include"; 
	public static String preprocess(String asm, File searchPathRoot, ArrayList<String> messages)
	{
		// Early outs
		if (asm == null)
			return null;
		
		if (!asm.contains("#") && !asm.contains("."))
			return asm;
		
		for (int i = 0; i < asm.length(); i++)
		{
			// Find next '#' or '.'
			i = minimumIndex(asm.indexOf('#', i), asm.indexOf('.', i));
			
			if (i == -1)
				break;
			
			// Start of line
			if (i == 0 || asm.charAt(i-1) == '\n')
			{
				if (i < asm.length() && asm.regionMatches(true, i+1, INCLUDE_DIRECTIVE, 0, INCLUDE_DIRECTIVE.length()))
				{
					int lineEnding = indexOfLineEnding(asm, i+1);
					String filename = asm.substring(i+1+INCLUDE_DIRECTIVE.length(), lineEnding).trim();
					if (filename.startsWith("\"") && filename.endsWith("\""))
						filename = filename.substring(1, filename.length()-1);
					String replacement = getFileContent(filename, searchPathRoot);
					if (replacement == null)
					{
						messages.add("error: can't read included file "+filename);
						return null;
					}
					else
					{
						asm = asm.substring(0, i) + replacement + "\n" + asm.substring(lineEnding);
						i += replacement.length();
					}
				}
			}
					
		}
		
		return asm;
	}
	
	private static int minimumIndex(int a, int b)
	{
		if (a == -1)
		{
			if (b == -1)
				return -1;
			else
				return b;
		}
		else
		{
			if (b == -1)
				return a;
			else
				return Math.min(a, b);
		}
	}
	
	private static int indexOfLineEnding(String s, int index)
	{
		int i = minimumIndex(s.indexOf('\r', index), s.indexOf('\n'));
		
		if (i != -1)
			return i;
		else
			return s.length();
	}
	
	private static String getFileContent(String filename, File base)
	{
		File f = new File(base.getAbsolutePath() + File.separator + filename);
		if (!f.exists() || !f.isFile())
			return null;
		
		try
		{
			FileInputStream fis = new FileInputStream(f);
			byte[] data = new byte[(int) f.length()];
			fis.read(data);
			String s = new String(data);
			return s;
		}
		catch (Exception ex)
		{
			Log.e("ADCPU", "Can't read file \""+f.getAbsolutePath()+"\": ", ex);
			return null;
		}
	}
}
