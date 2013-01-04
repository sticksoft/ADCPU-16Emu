package uk.co.sticksoft.adce.cpu;

import android.widget.Toast;
import uk.co.sticksoft.adce.MainActivity;

public class CPU_1_1 extends CPU
{
	public char[] register = new char[8];
	public char PC,SP,O;
	
	public char lastResult;
	public char getLastResult() { return lastResult; }
	public enum Value
	{
		A,B,C,X,Y,Z,I,J,
		_A,_B,_C,_X,_Y,_Z,_I,_J,
		_nextwordA,_nextwordB,_nextwordC,_nextwordX,_nextwordY,_nextwordZ,_nextwordI,_nextwordJ,
		POP, PEEK, PUSH,
		SP, PC, O,
		_nextword,
		nextword,
		x00,x01,x02,x03,x04,x05,x06,x07,x08,x09,x0a,x0b,x0c,x0d,x0e,x0f,
		x10,x11,x12,x13,x14,x15,x16,x17,x18,x19,x1a,x1b,x1c,x1d,x1e,x1f,
	}
	
	public enum Opcode
	{
		non_basic_instruction,
		SET,
		ADD,
		SUB,
		MUL,
		DIV,
		MOD,
		SHL,
		SHR,
		AND,
		BOR,
		XOR,
		IFE,
		IFN,
		IFG,
		IFB,
	}
	
	private enum AddressType
	{
		REGISTER,
		RAM,
		PC,
		SP,
		O,
		LITERAL
	}
	
	public CPU_1_1()
	{
		reset();
	}
	
	public void reset()
	{
		for (int i = 0; i < register.length; i++)
			register[i] = 0;
		for (int i = 0; i < 0x10000; i++)
			RAM[i] = 0;
		PC = SP = O = 0;
		cycleCount = 0;
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
			return AddressType.O;
		if (value == 0x1e)
			return AddressType.RAM;
		if (value == 0x1f)
			return AddressType.RAM;
		
		return AddressType.LITERAL;
	}
	
	private char address(char value)
	{
		if (value < 0x08)
			return value;
		if (value < 0x10)
			return register[value-0x08];
		if (value < 0x18)
			return (char)(RAM[PC++]+register[value-0x10]);
		if (value == 0x18)
			return SP++;
		if (value == 0x19)
			return SP;
		if (value == 0x1a)
			return --SP;
		if (value == 0x1b)
			return 0;
		if (value == 0x1c)
			return 0;
		if (value == 0x1d)
			return 0;
		if (value == 0x1e)
			return RAM[PC++];
		if (value == 0x1f)
			return PC++;
		
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
		case O:
			return O;
		default:
			return address;
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
		case O:
			O = word;
			break;
		default:
			// What yo playin' at?!
		}
	}
	
	private boolean skipping = false;
	
	public void execute()
	{
		cycleCount++; // TODO: work out correct cycle counts
		
		char instruction = RAM[PC];
		
		int op = instruction & 0x0f;
		
		if (op != 0) // Basic instruction!
		{
			// Increment now in case PC is modified by instruction or reading a or b
			PC++;
			
			// Work out value type
			char _a = (char)((instruction >> 0x04) & 0x3f);
			char _b = (char)((instruction >> 0x0a) & 0x3f);
		
			// Work out address type and location before instruction
			// This may modify PC!
			AddressType aType = addressType(_a), bType = addressType(_b);
			char aAddr = address(_a), bAddr = address(_b);
		
			// Grab the actual values
			char a = read(aType, aAddr), b = read(bType, bAddr);
			
			int res = 0; // Result holder
			
			if (skipping)
			{
				skipping = false;
				return;
			}
			
			// TODO: Replace with something faster?
			switch (Opcode.values()[op])
			{
			case non_basic_instruction:
				break;
			case SET:
				write(aType, aAddr, (char)(res = read(bType, bAddr)));
				break;
			case ADD:
				res = a + b;
				write(aType, aAddr, (char)(res & 0xffff));
				O = res > 0xffff ? (char)0x0001 : (char)0x0000;
				break;
			case SUB:
				res = a - b;
				write(aType, aAddr, (char)(res & 0xffff));
				O = res < 0 ? (char)0xffff : (char)0x0000;
				break;
			case MUL:
				res = a * b;
				write(aType, aAddr, (char)(res & 0xffff));
				O = (char)((res >> 16) & 0xffff);
				break;
			case DIV:
				if (b != 0)
				{
					write(aType, aAddr, (char)(res = ((a / b) & 0xffff)));
					O = (char)(((a << 16)/b) & 0xffff);
				}
				else
				{
					write(aType, aAddr, (char)0);
					O = 0;
				}
				break;
			case MOD:
				if (b != 0)
					write(aType, aAddr, (char)(res = (a % b)));
				else
					write(aType, aAddr, (char)0);
				break;
			case SHL:
				res = a << b;
				write(aType, aAddr, (char)(res & 0xffff));
				O = (char)((res >> 16) & 0xffff);
				break;
			case SHR:
			    res = a >> b;
				write(aType, aAddr, (char)(res & 0xffff));
				O = (char)(((a << 16) >> b) & 0xffff);
				break;
			case AND:
				write(aType, aAddr, (char)(res = (a & b)));
				break;
			case BOR:
				write(aType, aAddr, (char)(res = (a | b)));
				break;
			case XOR:
				write(aType, aAddr, (char)(res = (a ^ b)));
				break;
			case IFE:
				if (a != b)
					skipping = true;
				break;
			case IFN:
				if (a == b)
					skipping = true;
				break;
			case IFG:
				if (a <= b)
					skipping = true;
				break;
			case IFB:
				if ((a & b) == 0)
					skipping = true;
				break;
			}
			
			lastResult = skipping ? 1 : (char)res;
		}
		else // Non-basic instruction!
		{
			// Work out opcode and value
			char _op = (char)((instruction >> 0x04) & 0x3f);
			char _a = (char)((instruction >> 0x0a) & 0x3f);
					
			// Increment now in case PC is modified by instruction
			PC++;
			
			// TODO: Replace with something faster?
			switch (_op)
			{
				case 0x00:
					break;
				case 0x01: // JSR a
				{
					// Get jump address
					char jmp = read(addressType(_a), address(_a));
					
					// Push PC
					RAM[--SP] = PC;
					
					// Set PC to jump address
					PC = jmp;
					break;
				}
			}
		}
		
		notifyObservers();
	}
	
	public char[] getStateInfo()
	{
		int size = register.length /* registers */ + 3 /* PC, SP, O */ + 1 /* skipping */ + 2 /* cyclecount */;
		
		char[] out = new char[size];
		int cursor = 0;
		System.arraycopy(register, 0, out, 0, cursor = register.length);
		out[cursor++] = PC;
		out[cursor++] = SP;
		out[cursor++] = O;
		out[cursor++] = (char) (skipping ? 0xFFFF : 0x0000);
		intToLittleEndian((int)cycleCount, out, cursor);
		
		return out;
	}
	
	public void setStateInfo(char[] state)
	{
		try
		{
			int size = register.length /* registers */ + 3 /* PC, SP, O */ + 1 /* skipping */ + 2 /* cyclecount */;
			if (state.length != size)
			{
				MainActivity.showToast("State info doesn't match this processor version!", Toast.LENGTH_LONG);
				return;
			}
			
			int cursor = 0;
			System.arraycopy(state, 0, register, 0, cursor = register.length);
			PC = state[cursor++];
			SP = state[cursor++];
			O  = state[cursor++];
			skipping = (state[cursor++] == 0xFFFF);
			cycleCount = intFromLittleEndian(state, cursor);
		}
		catch (Exception ex)
		{
			MainActivity.showToast("Unable to retreive CPU state!", Toast.LENGTH_LONG);
		}
	}
	
	public String getStatusText()
	{
		return String.format(" A:%04x B:%04x C:%04x\n X:%04x Y:%04x Z:%04x\n I:%04x J:%04x\n PC:%04x SP:%04x O:%04x",
				(int)register[0], (int)register[1], (int)register[2],
				(int)register[3], (int)register[4], (int)register[5],
				(int)register[6], (int)register[7],
				(int)PC, (int)SP, (int)O);
	}
}
