package uk.co.sticksoft.adce.asm._1_7;

import java.util.List;
import java.util.Map;

import uk.co.sticksoft.adce.asm.Assembler_1_7;


public class Instruction implements Token
{
	private char opcode;
	
	private Value aValue, bValue;
	
	private final static int LABEL_VALUE_NUMBER = -2;
	
	private String debugString;
	
	public Instruction(char opcode, String b, String a, Assembler_1_7 assembler)
	{
		this.opcode = opcode;
		this.aValue = translateValue(a, true, assembler);
		this.bValue = translateValue(b, false, assembler);
		try
		{
			this.debugString = Consts.BasicOpcode.values()[opcode].toString() + " " + b + " " + a;
		}
		catch (Exception e)
		{
			this.debugString = "Instruction, opcode: "+opcode+" b:"+b+" a:"+a;
		}
	}
	
	public Instruction(char opcode, String a, Assembler_1_7 assembler)
	{
		this.opcode = 0;
		this.bValue = new Value(opcode);
		this.aValue = translateValue(a, true, assembler);
		try
		{
			this.debugString = Consts.AdvancedOpcode.values()[opcode].toString() + " " + a;
		}
		catch (Exception e)
		{
			this.debugString = "Instruction, opcode: "+opcode+" a:"+a;
		}
	}
	
	private static final String[] valcodes =
	{
		"A", "B", "C", "X", "Y", "Z", "I", "J",
		"[A]", "[B]", "[C]", "[X]", "[Y]", "[Z]", "[I]", "[J]",
		null, null, null, null, null, null, null, null,
		null /* POP / PUSH */, "PEEK", null /* PICK */, "SP", "PC", "EX"
		/* [next_word], next_word,
		/* literals (-1 to 30) */
	};
	
	public static class Value
	{
		public char valcode = 0, extraWord = 0;
		public boolean hasExtra = false;
		public String label = null;
		public Value(int val) { valcode = (char)val; }
		public Value(int val, int extra) { valcode = (char)val; extraWord = (char)extra; hasExtra = true; }
		public Value(int val, String label) { valcode = (char)val; hasExtra = true; this.label = label; }
		
		@Override
		public String toString()
		{
			return "("+(int)valcode
					+ ((valcode < Consts.ValueCode.values().length) ? " \"" + Consts.ValueCode.values()[valcode] + "\"" : "")
					+ (hasExtra ? (", "+(label!=null?"'"+label+"', " : "")+String.format("%04x", (int)extraWord)) : "")
					+")";
		}
	}
	
	public int integerValue(String decHexOrLabel, boolean canBeMinus1, Assembler_1_7 assembler)
	{
		if (decHexOrLabel == null || decHexOrLabel.length() == 0)
		{
			assembler.error("Empty value specified.");
			return 0;
		}
		
		decHexOrLabel = decHexOrLabel.trim();
		
		if (Character.isDigit(decHexOrLabel.charAt(0)))
		{
			if (decHexOrLabel.startsWith("0x") || decHexOrLabel.startsWith("0X"))
			{
				try
				{
					int value = Integer.parseInt(decHexOrLabel.substring(2), 16); // Hexadecimal
					if ((value >= 0 || (canBeMinus1 && value >= -1)) && value <= 0xffff)
						return value;
				}
				catch (NullPointerException nfe) { }
				
				assembler.error("Invalid hexadecimal value ('"+decHexOrLabel+"')");
				return 0;
			}
			else
			{
				try
				{
					int value = Integer.parseInt(decHexOrLabel); // Decimal
					if ((value >= 0 || (canBeMinus1 && value >= -1)) && value <= 0xffff)
						return value;
				}
				catch (NumberFormatException nfe) { }
				
				assembler.error("Invalid decimal value ('"+decHexOrLabel+"')");
				return 0;
			}
		}
		
		return LABEL_VALUE_NUMBER;
	}
	
	public int registerValue(String register)
	{
		for (int i = 0; i < 8; i++)
			if (valcodes[i].equalsIgnoreCase(register))
				return i;
		return -1;
	}
	
	public Value translateValue(String raw, boolean isAnAValue, Assembler_1_7 assembler)
	{
		// TODO: Unroll this loop.
		for (int i = 0; i < valcodes.length; i++)
		{
			if (valcodes[i] == null)
				continue;
			
			if (raw.equalsIgnoreCase(valcodes[i]))
				return new Value((char)i); // No extra word needed
		}
		
		if (raw.equalsIgnoreCase("POP"))
		{
			if (!isAnAValue)
				assembler.warning("Can't POP a b-value, I'm assuming you meant PUSH.");
			return new Value(Consts.ValueCode.POP.ordinal());
		}
		else if (raw.equalsIgnoreCase("PUSH"))
		{
			if (isAnAValue)
				assembler.warning("Can't PUSH an a-value, I'm assuming you meant POP.");
			return new Value(Consts.ValueCode.POP.ordinal());
		}
		
		if (raw.length() > 5 && raw.substring(0, 4).equalsIgnoreCase("PICK"))
		{
			String valueString = raw.substring(4).trim();
			int pickvalue = integerValue(valueString, false, assembler);
			if (pickvalue != LABEL_VALUE_NUMBER)
				return new Value(Consts.ValueCode.PICK.ordinal(), pickvalue);
			else
				return new Value(Consts.ValueCode.PICK.ordinal(), valueString);
		}
		
		int plusIndex = raw.indexOf('+');
		if (raw.startsWith("[") && raw.endsWith("]"))
		{
			if (plusIndex > 1) // Gotta be <register> + <value> or <value> + <register>
			{
				String prePlus = raw.substring(1, plusIndex).trim(), postPlus = raw.substring(plusIndex+1, raw.length()-1).trim();
				
				int preRegister = registerValue(prePlus), postRegister = registerValue(postPlus);
				
				if (preRegister == -1 && postRegister == -1)
				{
					assembler.error("Can't find a register name in [register + next_word] value '"+raw+"'!");
					return null;
				}
				
				if (preRegister != -1 && postRegister != -1)
				{
					assembler.error("Both sides of addition are registers in [register + next_word] value '"+raw+"'!");
					return null;
				}
				
				String numberOrLabel;
				int registerNumber;
				if (preRegister == -1)
				{
					numberOrLabel = prePlus;
					registerNumber = postRegister;
				}
				else
				{
					numberOrLabel = postPlus;
					registerNumber = preRegister;
				}
				
				int value = integerValue(numberOrLabel, false, assembler);
				
				if (value != LABEL_VALUE_NUMBER)
					return new Value((char)(registerNumber + 16), (char)value);
				else
					return new Value((char)(registerNumber + 16), numberOrLabel);
			}
			else
			{
				String numberOrLabel = raw.substring(1, raw.length()-1).trim();
				
				int value = integerValue(numberOrLabel, false, assembler);
				
				if (value != LABEL_VALUE_NUMBER)
					return new Value(Consts.ValueCode._nextword.ordinal(), (char)value);
				else
					return new Value(Consts.ValueCode._nextword.ordinal(), numberOrLabel);
			}
		}
		
		int value = integerValue(raw, true, assembler);
		if (value == LABEL_VALUE_NUMBER)
			return new Value(Consts.ValueCode.nextword.ordinal(), raw);
		else
		{
			if (value > 30)
				return new Value(Consts.ValueCode.nextword.ordinal(), value);
			else
				return new Value(Consts.ValueCode._0.ordinal() + value);
		}
	}

	public void writeTo(List<Character> cs, Assembler_1_7 assembler)
	{
		char b = bValue != null ? ((char)(bValue.valcode & 0x1f)) : 0;
		char a = aValue != null ? ((char)(aValue.valcode & 0x3f)) : 0; 
		cs.add((char)(opcode + (b << 5) + (a << 10)));
		
		if (aValue != null && aValue.hasExtra)
			cs.add(aValue.extraWord);
		if (bValue != null && bValue.hasExtra)
			cs.add(bValue.extraWord);
	}
	
	public int getCharCount()
	{
		return 1 + ((aValue != null && aValue.hasExtra)?1:0) + ((bValue != null && bValue.hasExtra)?1:0);
	}
	
	public void substituteLabels(Map<String, Character> labelMap)
	{
		if (aValue != null && aValue.label != null)
		{
			Character c = labelMap.get(aValue.label);
			if (c != null)
				aValue.extraWord = c;
		}
		if (bValue != null && bValue.label != null)
		{
			Character c = labelMap.get(bValue.label);
			if (c != null)
				bValue.extraWord = c;
		}
		/*
		if (aLabel == null)
		{
			if (bLabel == null)
				return;
			else
			{
				for (String string : labelMap.keySet())
				{
					if (string.equals(bLabel))
					{
						bWord = labelMap.get(string).charValue();
						return;
					}
				}
			}
		}
		else
		{
			if (bLabel == null)
			{
				for (String string : labelMap.keySet())
				{
					if (string.equals(aLabel))
					{
						aWord = labelMap.get(string).charValue();
						return;
					}
				}
			}
			else
			{
				boolean aFound = false, bFound = false;
				for (String string : labelMap.keySet())
				{
					if (!bFound && string.equals(bLabel))
					{
						bWord = labelMap.get(string).charValue();
						if (string.equals(aLabel))
						{
							aWord = bWord;
							return;
						}
						bFound = true;
						if (aFound)
							return;
					}
					else if (string.equals(aLabel))
					{
						aWord = labelMap.get(string).charValue();
						aFound = true;
						if (bFound)
							return;
					}
				}
			}
		}
		*/
	}

	@Override
	public void setOrigin(char origin)
	{
		// Don't need to know, really.
	}
	
	@Override
	public String toString()
	{
		return "Instruction: "+debugString + " => " + (int)opcode + " " + bValue.toString() + " " + aValue.toString();
	}
}
