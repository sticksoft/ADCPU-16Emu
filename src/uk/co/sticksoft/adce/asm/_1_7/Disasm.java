package uk.co.sticksoft.adce.asm._1_7;

public class Disasm
{
    static int PC;
	static char[] RAM;
	static StringBuilder builder = new StringBuilder();
    public static String disasm(char[] ram, int address)
	{
		RAM = ram;
		PC = address;
		try
		{
			builder.setLength(0);
			
			for (int i = 0; i < 5; i++)
			{
				builder.append(i == 0 ? ">" : "   ");
				
			char val = RAM[PC++];
			
			int opcode = val & 0x1f;
			int b = (val >> 5) & 0x1f;
			int a = (val >> 10) & 0x3f;
			
			if (opcode != 0)
			{
				String aName = GetValueName(a, true);
				String bName = GetValueName(b, false);
				builder.append(String.format("%04x %s %s, %s",
				    PC-1,
					Consts.BasicOpcode.values()[opcode].toString(),
					bName,
					aName));
			}
			else
			{
				builder.append(String.format("%04x %s %s",
				    PC-1,
					Consts.AdvancedOpcode.values()[b].toString(),
					GetValueName(a, true)));
			}
			builder.append('\n');
			}
			return builder.toString();
		}
		catch (Exception e) {}
		
		try
		{
			return String.format("%04x (error)", address);
		}
		catch (Exception e) {}
		
		return "(error)";
	}
	
	public static String GetValueName(int val, boolean aValue)
	{
		if (val < 8)
			return Consts.ValueCode.values()[val].toString();
		else if (val < 16)
		    return "["+Consts.ValueCode.values()[val-8].toString()+"]";
		else if (val < 24)
		    return String.format("[%s+%04x]",
			    Consts.ValueCode.values()[val-16].toString(),
				(int)RAM[PC++]);
		else if (val == 24)
		    return aValue ? "PUSH" : "POP";
		else if (val < 30)
		    return Consts.ValueCode.values()[val].toString();
		else if (val == 30)
		    return String.format("[%04x]", (int)RAM[PC++]);
		else if (val == 31)
		    return String.format("%04x", (int)RAM[PC++]);
		else
		    return String.format("%d", val-33);
	}
	
	
}
