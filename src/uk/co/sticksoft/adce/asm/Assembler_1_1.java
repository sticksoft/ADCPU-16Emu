package uk.co.sticksoft.adce.asm;

import java.util.ArrayList;
import java.util.HashMap;

import uk.co.sticksoft.adce.cpu.CPU;
import uk.co.sticksoft.adce.cpu.CPU_1_1;

public class Assembler_1_1 implements Assembler
{
	public Assembler_1_1() {}
	
	private String source;
	private int sourceIndex;
	
	private ArrayList<Character> machinecode = new ArrayList<Character>();
	private HashMap<String, Integer> labels = new HashMap<String, Integer>();
	private HashMap<Integer, String> labelUsages = new HashMap<Integer, String>();
	private ArrayList<String> out_messages;
	
	public char[] assemble(String s) { return assemble(s, new ArrayList<String>(), new HashMap<Integer,String>()); }
	public char[] assemble(String s, ArrayList<String> out_messages, HashMap<Integer,String> debugSymbols)
	{
		source = s;
		sourceIndex = 0;
		StringBuilder debugBuilder = new StringBuilder();
		int lastInstruction = 0;
		this.out_messages = out_messages;
		
		String line = null;
		for (;;)
		{
			// Maybe write a debug symbol if anything changed!
			if (lastInstruction != machinecode.size())
			{
				debugSymbols.put(Integer.valueOf(lastInstruction), debugBuilder.toString());
				debugBuilder.setLength(0);
				lastInstruction = machinecode.size();
			}
			
			// Read next chunk, unless there was some left over from last loop
			if (line == null)
			{
				line = readLine();
				if (line == null)
					break; // That's all, folks!
				
				debugBuilder.append(line);
			}
			
			// Get rid of whitespace and ignore blank lines
			line = line.trim();
			if (line.length() == 0)
			{
				line = null; // Done with this line
				continue;
			}
			
			char first = line.charAt(0);
			
			// Comments
			if (first == ';')
			{
				line = null;
				continue;
			}
			
			// Labels
			if (first == ':')
			{
				line = readLabel(line.substring(1));
				continue;
			}
			
			// Assuming if we hit this point, can only be an instruction?
			line = readInstruction(line);
		}
		
		// Do labels
		for (Integer i : labelUsages.keySet())
		{
			String label = labelUsages.get(i);
			if (labels.containsKey(label))
				machinecode.set(i.intValue(), Character.valueOf((char)labels.get(label).intValue()));
			else
				out_messages.add(i.toString() + ": Error: Label "+label+" not found!");
		}
		
		// Convert the ArrayList into a char[].
		// Come back, std::vector<unsigned short>; all is forgiven! <3
		char[] data = new char[machinecode.size()];
		for (int i = machinecode.size(); i-->0;)
			data[i] = machinecode.get(i);
		return data;
	}
	
	private String readLine()
	{
		if (sourceIndex >= source.length())
			return null;
		
		// Look for next line-ending character
		// Should work with any combination of Unix, Mac or Windows endings
		int cr = source.indexOf('\r', sourceIndex);
		int lf = source.indexOf('\n', sourceIndex);
		
		if (cr == -1)
			cr = source.length()-1;
		if (lf == -1)
			lf = source.length()-1;
		
		// Skip CRLF
		if (lf == cr+1)
			cr = lf;
		
		int to =  Math.min(cr,lf) + 1;
		String ret = source.substring(sourceIndex, to);
		
		sourceIndex = to;
		return ret;
	}
	
	private String readLabel(String line)
	{
		if (line.length() == 0)
			return null;
		
		
		StringBuilder sb = new StringBuilder();
		int index;
		for (index = 0; index < line.length(); index++)
		{
			char c = line.charAt(index);
			if (Character.isWhitespace(c))
				break;
			else
				sb.append(c);
		}
		
		
		labels.put(line.substring(0, index), Integer.valueOf(machinecode.size()));
		
		if (index < line.length())
			return line.substring(index);
		
		return null;
	}
	
	
	private String readInstruction(String line)
	{
		if (line.length() < 3)
			return null;
		
		int index = 0;
		String opcode = line.substring(0, 3);
		
		for (int i = 1; i < CPU_1_1.Opcode.values().length; i++)
		{
			if (opcode.equalsIgnoreCase(CPU_1_1.Opcode.values()[i].name()))
			{
				// This is a basic instruction, and must have two comma-separated args
				int comma = line.indexOf(',', 5);
				if (comma == -1)
					return null; // Oops
				
				// A is easy to get
				String A = line.substring(4, comma);
				
				// Find B
				int bstart = -1, bend = line.length();
				for (int j = comma + 1; j < line.length(); j++)
				{
					if (bstart == -1)
					{
						if (!Character.isWhitespace(line.charAt(j)))
							bstart = j;
					}
					else if (Character.isWhitespace(line.charAt(j)))
					{
						bend = j;
						break;
					}
				}
				
				if (bstart == -1 || bstart > bend)
					return null; // Oops
				
				// Here it is!
				String B = line.substring(bstart, bend);
								
				writeBasicInstruction(i, A, B);
				
				// Deal with the rest of the line (if any)
				if (bend < line.length())
					return line.substring(bend);
				else
					return null;
			}
		}
		
		if (opcode.equalsIgnoreCase("JSR"))
		{
			// Find address
			int start = -1, end = line.length();
			for (int j = 4; j < line.length(); j++)
			{
				if (start == -1)
				{
					if (!Character.isWhitespace(line.charAt(j)))
						start = j;
				}
				else if (Character.isWhitespace(line.charAt(j)))
				{
					end = j;
					break;
				}
			}
			
			if (start == -1 || start > end)
				return null; // Oops
			
			// Here it is!
			String addr = line.substring(start, end);
			writeNonBasicInstruction(1, addr);
		}
		else if (opcode.equalsIgnoreCase("DAT") && line.length() > 4)
		{
			return writeDat(line.substring(3));
		}
		
		return null;
	}
	
	private void writeNonBasicInstruction(int opcode, String arg)
	{
		// Add placeholder for instruction
		int instr_addr = machinecode.size();
		machinecode.add(Character.valueOf((char)0));
		int argval = writeValue(arg);
		
		// Pack the opcode, a, and b
		opcode = ((opcode & 0x3f) << 4) | ((argval & 0x3f) << 10);
		
		machinecode.set(instr_addr, Character.valueOf((char)opcode));
	}
	
	private String writeDat(String line)
	{
		boolean inString = false, inValue = false, escaping = false;
		StringBuilder buffer = new StringBuilder();
		int index = 0;
		for (; index < line.length(); index++)
		{
			char c = line.charAt(index);
			if (c == ';' && !inString)
				break;
			else if (c == '"')
			{
				if (!inString)
					inString = true;
				else
				{
					if (escaping)
					{
						machinecode.add(Character.valueOf(c));
						escaping = false;
					}
					else
						inString = false;
				}
			}
			else if (c == '\\')
			{
				if (!inString)
				{
					out_messages.add("Invalid escape found: "+line);
				}
				else
				{
					if (escaping)
					{
						machinecode.add(Character.valueOf(c));
						escaping = false;
					}
					else
						escaping = true;
				}
			}
			else if (inValue && !(Character.isLetterOrDigit(c)))
			{
				if (buffer.length() > 0)
				{
					try
					{
						machinecode.add(Character.valueOf((char)Integer.decode(buffer.toString()).intValue()));
					}
					catch (Exception ex)
					{
						out_messages.add("Invalid value found: "+line);
					}
					buffer.setLength(0);
				}
				
				inValue = false;
			}
			else if (inString)
				machinecode.add(Character.valueOf(c));
			else if (Character.isLetterOrDigit(c))
			{
				buffer.append(c);
				inValue = true;
			}
		}
		
		if (buffer.length() > 0 && inValue)
		{
			try
			{
				machinecode.add(Character.valueOf((char)Integer.decode(buffer.toString()).intValue()));
			}
			catch (Exception ex)
			{
				out_messages.add("Invalid value found: "+line);
			}
		}
		
		if (index < line.length())
			return line.substring(index);
		return null;
	}
	
	private void writeBasicInstruction(int opcode, String A, String B)
	{
		// Add placeholder for instruction
		int instr_addr = machinecode.size();
		machinecode.add(Character.valueOf((char)0));
		int a = writeValue(A);
		int b = writeValue(B);
		
		// Pack the opcode, a, and b
		opcode = opcode | ((a & 0x3f) << 4) | ((b & 0x3f) << 10);
		
		machinecode.set(instr_addr, Character.valueOf((char)opcode));
	}
	
	private String[] valcodes =
	{
		"A", "B", "C", "X", "Y", "Z", "I", "J",
		"[A]", "[B]", "[C]", "[X]", "[Y]", "[Z]", "[I]", "[J]",
		null, null, null, null, null, null, null, null,
		"POP", "PEEK", "PUSH", "SP", "PC", "O"
	};
	private int writeValue(String val)
	{
		// Quick check for registers etc., listed above
		for (int i = 0; i < 16; i++)
			if (val.equalsIgnoreCase(valcodes[i]))
				return i;
		for (int i = 24; i < 30; i++)
			if (val.equalsIgnoreCase(valcodes[i]))
				return i;
		
		if (val.startsWith("[") && val.endsWith("]") && val.length() > 2)
		{
			val = val.substring(1, val.length()-1);
			int plus = val.indexOf('+');
			
			if (plus == -1) // No plus
			{
				try
				{
					int number = Integer.decode(val);
					if (number < 0 || number > 0xFFFF)
						return 0;
					machinecode.add(Character.valueOf((char)number));
					return CPU_1_1.Value._nextword.ordinal();
				}
				catch (NumberFormatException e)
				{
					// Not a number, might be a label...
					labelUsages.put(machinecode.size(), val);
					
					// Leave a placeholder
					machinecode.add(Character.valueOf((char)0));
					return CPU_1_1.Value._nextword.ordinal();
				}
			}
			else // Got a plus!
			{
				if (plus == val.length()-1)
					return 0; // Oops
				
				// One of the args is a register.
				String lhs = val.substring(0,plus).trim(), rhs = val.substring(plus+1,val.length()).trim();
				int lregister = -1, rregister = -1;
				for (int i = 0; i < 8; i++)
				{
					if (lhs.equalsIgnoreCase(valcodes[i]))
						lregister = i;
					if (rhs.equalsIgnoreCase(valcodes[i]))
						rregister = i;
				}
				if ((lregister == -1) == (rregister == -1))
					return 0; // Exactly one must be a register
				
				if (rregister == -1)
				{
					lhs = rhs; // Don't bother to swap
					rregister = lregister;
				}
				
				try
				{
					int number = Integer.decode(lhs);
					if (number < 0 || number > 0xFFFF)
						return 0;
					machinecode.add(Character.valueOf((char)number));
					return CPU_1_1.Value._nextwordA.ordinal() + rregister;
				}
				catch (NumberFormatException e)
				{
					// Not a number, might be a label...
					labelUsages.put(machinecode.size(), lhs);
					
					// Leave a placeholder
					machinecode.add(Character.valueOf((char)0));
					return CPU_1_1.Value._nextwordA.ordinal() + rregister;
				}
				
			}
		}
		else // No brackets
		{
			try
			{
				int number = Integer.decode(val);
				if (number < 0 || number > 0xFFFF)
					return 0;
				
				if (number < 0x20)
					return CPU_1_1.Value.x00.ordinal() + number;
				
				machinecode.add(Character.valueOf((char)number));
				return CPU_1_1.Value.nextword.ordinal();
			}
			catch (NumberFormatException e)
			{
				// Not a number, might be a label...
				labelUsages.put(machinecode.size(), val);
				
				// Leave a placeholder
				machinecode.add(Character.valueOf((char)0));
				return CPU_1_1.Value.nextword.ordinal();
			}
		}
	}
}
