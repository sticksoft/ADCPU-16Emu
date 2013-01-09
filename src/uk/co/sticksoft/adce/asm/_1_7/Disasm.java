package uk.co.sticksoft.adce.asm._1_7;

import uk.co.sticksoft.adce.Options;
import uk.co.sticksoft.adce.cpu.CPU;
import uk.co.sticksoft.adce.cpu.CPU_1_7;

public class Disasm
{
    static int PC;
	static char[] RAM;
	static CPU_1_7 cpu;
	static StringBuilder builder = new StringBuilder();
	static StringBuilder coda = new StringBuilder();
    public static String disasm(char[] ram, int address)
	{
    	CPU c = Options.GetCPU();
    	if (c instanceof CPU_1_7)
    		cpu = (CPU_1_7)c;
    	
		RAM = ram;
		PC = address;
		try
		{
			builder.setLength(0);
			coda.setLength(0);
			
			for (int i = 0; i < 5; i++)
			{
				builder.append(i == 0 ? "> " : "   ");
				
				int pc = PC;
				char val = RAM[PC++];
				
				int opcode = val & 0x1f;
				int b = (val >> 5) & 0x1f;
				int a = (val >> 10) & 0x3f;
				
				if (opcode != 0)
				{
					String aName = GetValueName(a, true);
					String bName = GetValueName(b, false);
					builder.append(String.format("0x%04x %s %s, %s",
					    pc,
						Consts.BasicOpcode.values()[opcode].toString(),
						bName,
						aName));
				}
				else if (val != 0)
				{
					builder.append(String.format("0x%04x %s %s",
					    pc,
						Consts.AdvancedOpcode.values()[b].toString(),
						GetValueName(a, true)));
				}
				else
				{
					builder.append(String.format("0x%04x <halt>", pc));
				}
				builder.append(" ").append(coda.toString()).append('\n');
			}
			return builder.toString();
		}
		catch (Exception e) { e.printStackTrace(); }
		
		try
		{
			return String.format("0x%04x (error)", address);
		}
		catch (Exception e) { e.printStackTrace(); }
		
		return "(error)";
	}
	
	public static String GetValueName(int val, boolean aValue)
	{
		if (val < 8)
			return Consts.ValueCode.values()[val].toString();
		else if (val < 16)
		{
			if (cpu != null)
				return "["+Consts.ValueCode.values()[val-8].toString()+"]"+String.format(" (0x%x)", (int)RAM[cpu.register[val-8]]);
			else
				return "["+Consts.ValueCode.values()[val-8].toString()+"]";
		}
		else if (val < 24)
		{
			int add = (int)RAM[PC++];
			if (cpu != null)
				return String.format("[%s+0x%04x] (0x%x)",
					    Consts.ValueCode.values()[val-16].toString(),
						add,
						(int)RAM[cpu.register[val-16] + add]);
				
			else
				return String.format("[%s+0x%04x]",
						Consts.ValueCode.values()[val-16].toString(),
						add);
		}
		else if (val == 24)
		    return aValue ? "POP" : "PUSH";
		else if (val == 26)
			return String.format("PICK 0x%04x (%d)", (int)RAM[PC], (int)RAM[PC++]);
		else if (val < 30)
		    return Consts.ValueCode.values()[val].toString();
		else if (val == 30)
		{
			int add = RAM[PC++];
		    return String.format("[0x%04x] (0x%x)", add, (int)RAM[add]);
		}
		else if (val == 31)
		    return String.format("0x%04x (%d)", (int)RAM[PC], (int)RAM[PC++]);
		else
		    return String.format("%d", val-33);
	}
	
	public static int addCoda(int address)
	{
		coda.append(String.format("[0x%04x]: 0x%04x ", address, (int)RAM[address]));
		return address;
	}
	
	
}
