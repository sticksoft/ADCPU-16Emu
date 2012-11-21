package uk.co.sticksoft.adce.asm._1_7;

import java.util.HashMap;

public class Consts
{
	public static enum BasicOpcode
	{
		_0x00,
		SET,
		ADD,
		SUB,
		MUL,
		MLI,
		DIV,
		DVI,
		MOD,
		MDI,
		AND,
		BOR,
		XOR,
		SHR,
		ASR,
		SHL,
		IFB,
		IFC,
		IFE,
		IFN,
		IFG,
		IFA,
		IFL,
		IFU,
		_0x18,
		_0x19,
		ADX,
		SBX,
		_0x1c,
		_0x1d,
		STI,
		STD,
	}
	
	public static enum AdvancedOpcode
	{
		_0x00,
		JSR,
		_0x02,
		_0x03,
		_0x04,
		_0x05,
		_0x06,
		_0x07,
		INT,
		IAG,
		IAS,
		RFI,
		IAQ,
		_0x0d,
		_0x0e,
		_0x0f,
		HWN,
		HWQ,
		HWI,
		_0x13,
		_0x14,
		_0x15,
		_0x16,
		_0x17,
		_0x18,
		_0x19,
		_0x1a,
		_0x1b,
		_0x1c,
		_0x1d,
		_0x1e,
		_0x1f,
	}
	
	public static enum ValueCode
	{
		A,B,C,X,Y,Z,I,J,
		_A,_B,_C,_X,_Y,_Z,_I,_J,
		_nextwordA,_nextwordB,_nextwordC,_nextwordX,_nextwordY,_nextwordZ,_nextwordI,_nextwordJ,
		POP, PEEK, PICK, SP, PC, EX,
		_nextword, nextword,
		_minus1,
		 _0, _1, _2, _3, _4, _5, _6, _7, _8, _9,
		_10,_11,_12,_13,_14,_15,_16,_17,_18,_19,
		_20,_21,_22,_23,_24,_25,_26,_27,_28,_29,
		_30
	}
	
	// Dirty, dirty code that purists will likely dislike.
	// If this is the case, please tell Oracle to make an Enum.valueOf(String) method that doesn't throw.
	// Or at least Enum.contains(String).
	private final static HashMap<String, Character> basicOpcodes = new HashMap<String, Character>();
	private final static HashMap<String, Character> advancedOpcodes = new HashMap<String, Character>();
	static
	{
		for (BasicOpcode bo : BasicOpcode.values())
			basicOpcodes.put(bo.name(), Character.valueOf((char)bo.ordinal())); // This will fail if we get more than 65,536 opcodes
				
		for (AdvancedOpcode ao : AdvancedOpcode.values())
			advancedOpcodes.put(ao.name(), Character.valueOf((char)ao.ordinal())); // This will fail if we get more than 65,536 opcodes
	}
	
	public final static char getBasicOpcode(String name)
	{
		name = name.toUpperCase();
		Character opcode = basicOpcodes.get(name);
		if (opcode == null)
			return 0;
		else
			return opcode.charValue();
	}
	
	public final static char getAdvancedOpcode(String name)
	{
		name = name.toUpperCase();
		Character opcode = advancedOpcodes.get(name);
		if (opcode == null)
			return 0;
		else
			return opcode.charValue();
	}
}
