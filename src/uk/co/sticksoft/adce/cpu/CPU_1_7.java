package uk.co.sticksoft.adce.cpu;

/*
 * Half-baked 1.7 version of the CPU
 */
public class CPU_1_7 extends CPU
{
	public char[] register = new char[8];
	public char PC, SP, EX, IA;
	
	@Override
	public void reset()
	{
		for (int i = 0; i < register.length; i++)
			register[i] = 0;
		for (int i = 0; i < 0x10000; i++)
			RAM[i] = 0;
		PC = SP = EX = IA = 0;
		cycleCount = 0;
		onFire = skipping = false;
	}
	
	private boolean onFire, skipping;
	
	private char tSP, tPC; // Temporary SP and PC
	
	private enum AddressType
	{
		REGISTER,
		RAM,
		PC,
		SP,
		EX,
		IA,
		LITERAL
	}
	
	private AddressType addressType(char value)
	{
		if (value < 0x08)
			return AddressType.REGISTER;
		if (value < 0x10)
			return AddressType.RAM;
		if (value < 0x18)
			return AddressType.RAM;
		if (value == 0x18)
			return AddressType.RAM;
		if (value == 0x19)
			return AddressType.RAM;
		if (value == 0x1a)
			return AddressType.RAM;
		if (value == 0x1b)
			return AddressType.SP;
		if (value == 0x1c)
			return AddressType.PC;
		if (value == 0x1d)
			return AddressType.EX;
		if (value == 0x1e)
			return AddressType.RAM;
		if (value == 0x1f)
			return AddressType.RAM;
		
		return AddressType.LITERAL;
	}
	
	private char addressA(char value)
	{
		if (value < 0x08)
			return value;
		if (value < 0x10)
			return register[value-0x08];
		if (value < 0x18)
			return (char)(RAM[tPC++]+register[value-0x10]);
		if (value == 0x18)
			return tSP++;
		if (value == 0x19)
			return tSP;
		if (value == 0x1a)
			return (char)(RAM[tPC++] + tSP);
		if (value == 0x1b)
			return 0;
		if (value == 0x1c)
			return 0;
		if (value == 0x1d)
			return 0;
		if (value == 0x1e)
			return RAM[tPC++];
		if (value == 0x1f)
			return tPC++;
		
		return (char)(value - 0x20);
	}
	
	private char addressB(char value)
	{
		if (value < 0x08)
			return value;
		if (value < 0x10)
			return register[value-0x08];
		if (value < 0x18)
			return (char)(RAM[tPC++]+register[value-0x10]);
		if (value == 0x18)
			return --tSP;
		if (value == 0x19)
			return tSP;
		if (value == 0x1a)
			return (char)(RAM[tPC++] + tSP);
		if (value == 0x1b)
			return 0;
		if (value == 0x1c)
			return 0;
		if (value == 0x1d)
			return 0;
		if (value == 0x1e)
			return RAM[tPC++];
		if (value == 0x1f)
			return tPC++;
		
		return (char)(value - 0x20);
	}
	
	private char read(AddressType type, char address)
	{
		switch (type)
		{
		case REGISTER:
			return register[address];
		case RAM:
			return RAM[address];
		case PC:
			return PC;
		case SP:
			return SP;
		case EX:
			return EX;
		default:
			if (address == 0)
				return 0xFFFF;
			else
				return (char)(address-1);
		}
	}
	
	private void write(AddressType type, char address, char word)
	{
		switch (type)
		{
		case REGISTER:
			register[address] = word;
			break;
		case RAM:
			RAM[address] = word;
			break;
		case PC:
			PC = word;
			break;
		case SP:
			SP = word;
			break;
		case EX:
			EX = word;
			break;
		default:
			// What yo playin' at?!
		}
	}
	
	private static int unsignedCharAsSignedInt(char c)
	{
		return c < 0x8000 ? c : c - 0x10000;
	}

	@Override
	public void execute()
	{
		tSP = SP;
		tPC = (char)(PC+1);
		
		// Fetch
		char instr = RAM[PC];
		
		// Decode
		int opcode = instr & 0x1f;
		char b = (char)((instr >> 5) & 0x1f);
		char a = (char)(instr >> 10);
		
		if (opcode != 0)
		{
			// Work out address type and location before instruction
			// These will only modify tSP or tPC, not the actual SP or PC
			// until after the instruction completes. 
			AddressType aType = addressType(a), bType = addressType(b);
			char aAddr = addressA(a), bAddr = addressB(b);
		
			// Grab the actual values
			char aVal = read(aType, aAddr), bVal = read(bType, bAddr);
			
			int res = 0; // Result
			
			switch (opcode)
			{
				case 0x01: // SET
					res = aVal;
					break;
				case 0x02: // ADD
					res = bVal + aVal;
					EX = (res < 0xffff) ? (char)0 : (char)1;
					break;
				case 0x03: // SUB
					res = bVal - aVal;
					EX = (res > 0) ? 0 : (char)0xffff;
					break;
				case 0x04: // MUL
					res = bVal * aVal;
					EX = (char)(res >> 16);
					break;
				case 0x05: // MLI
					res = unsignedCharAsSignedInt(bVal) * unsignedCharAsSignedInt(aVal);
					EX = (char)(res >> 16);
					break;
					// TODO: Finish this
			}
			
			write(bType, bAddr, (char)(res & 0xffff));
		}
		else
		{
		}
	}

	@Override
	public String getStatusText()
	{
		// TODO Auto-generated method stub
		return null;
	}

}
