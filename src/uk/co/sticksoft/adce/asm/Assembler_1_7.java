package uk.co.sticksoft.adce.asm;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import android.util.Log;

import uk.co.sticksoft.adce.asm._1_7.Consts;
import uk.co.sticksoft.adce.asm._1_7.Dat;
import uk.co.sticksoft.adce.asm._1_7.DebugToken;
import uk.co.sticksoft.adce.asm._1_7.Instruction;
import uk.co.sticksoft.adce.asm._1_7.Label;
import uk.co.sticksoft.adce.asm._1_7.Token;


public class Assembler_1_7 implements Assembler
{
	private final static String TAG = "ADCPU";
	private final static boolean VERBOSE_DEBUG = false;
	
	private void verbose(String s)
	{
		if (VERBOSE_DEBUG)
		{
			Log.i(TAG, s);
			messages.add(s);
		}
	}
	
	public Assembler_1_7() {}
	
	public char[] assemble(String source, ArrayList<String> out_message, HashMap<Integer, String> out_debugSymbols)
	{
		reset();
		
		this.source = source;
		this.sourceLength = source.length();
		this.messages = out_message;
		this.debugSymbols = out_debugSymbols;
		
		parseToStructre();
		
		handleLabels();
		
		for (Token t : structure)
			verbose(t.toString());
		
		handleDebugSymbols();
		
		return getMachineCodeFromStructure();
	}
	
	public ArrayList<Token> assembleAsStructure(String source)
	{
		reset();
		
		this.source = source;
		this.sourceLength = source.length();
		this.messages = new ArrayList<String>();
		this.debugSymbols = new HashMap<Integer, String>();
		
		parseToStructre();
		
		return structure;
	}
	
	protected String source;
	protected int sourceIndex;
	protected int sourceLength;
	protected int currentLine;
	
	protected HashMap<String, Label> labels = new HashMap<String, Label>();
	protected ArrayList<Token> structure = new ArrayList<Token>();
	protected ArrayList<String> messages;
	protected HashMap<Integer, String> debugSymbols;
	protected ArrayList<DebugToken> debugTokens = new ArrayList<DebugToken>();
	
	protected void reset()
	{
		labels.clear();
		structure.clear();
		this.sourceIndex = 0;
		this.currentLine = 1;
	}
	
	protected void parseToStructre()
	{
		try
		{
		while (sourceIndex < sourceLength)
			readNextToken();
		}
		catch (Exception e)
		{
			
		}
	}
	
	protected char readNextCharacter()
	{
		char c = source.charAt(sourceIndex++);
		
		if ((c == '\r' && !(/* Check for CRLF */ sourceIndex < source.length() && source.charAt(sourceIndex) == '\n')) || c == '\n')
			currentLine++;
		
		return c;
	}
	
	protected void readNextToken()
	{
		char c = readNextCharacter();
		if (Character.isWhitespace(c))
		{
			//verbose(""+sourceIndex+": Whitespace");
			return;
		}
		else if (c == ';')
		{
			verbose(""+sourceIndex+": Start of comment");
			readComment();
			verbose(""+sourceIndex+": Finished comment");
		}
		else if (c == ':')
		{
			verbose(""+sourceIndex+": Start of label");
			readLabel();
			verbose(""+sourceIndex+": Finished label");
		}
		else
		{
			verbose(""+sourceIndex+": Presumable start of instruction? '"+c+"'");
			sourceIndex--; // Optimisation - go back a character and use a substring instead of building the string again
			readInstruction();
			verbose(""+sourceIndex+": Finished instruction");
		}
	}
	
	protected void readComment()
	{
		int originalLine = currentLine;
		while (sourceIndex < sourceLength)
		{
			readNextCharacter();
			if (currentLine != originalLine)
				break;
		}
	}
	
	protected void readLabel()
	{
		int start = sourceIndex;
		while (sourceIndex < sourceLength)
		{
			if (Character.isWhitespace(readNextCharacter()))
				break;
		}
		
		if (start < sourceLength && sourceIndex - start > 1)
		{
			String labelName = source.substring(start, sourceIndex).trim();
			verbose("Found label: '"+labelName+"'");
			Label label = new Label(labelName);
			structure.add(label);
			labels.put(labelName, label);
		}
	}
	
	public void error(String text)
	{
		Log.e(TAG, text);
		messages.add(String.valueOf(currentLine)+": error: "+text);
	}
	
	public void warning(String text)
	{
		Log.w(TAG, text);
		messages.add(String.valueOf(currentLine)+": warning: "+text);
	}
	
	private int debugTokenIndex = 0;
	protected void readInstruction()
	{
		if (sourceIndex + 3 >= sourceLength)
		{
			warning("Superfluous characters at end of file.");
			sourceIndex = sourceLength;
			return;
		}
		
		String mneumonic = source.substring(sourceIndex, sourceIndex + 3);
		verbose("Mneumonic: "+mneumonic);
		sourceIndex += 3;
		
		DebugToken debugToken = new DebugToken();
		structure.add(debugToken);
		debugTokens.add(debugToken);
		
		char opcode;
		if ((opcode = Consts.getBasicOpcode(mneumonic)) != 0)
		{
			verbose("Basic instruction (opcode: "+(int)opcode+")");
			readBasicInstruction(opcode);
		}
		else if ((opcode = Consts.getAdvancedOpcode(mneumonic)) != 0)
		{
			verbose("Advanced instruction (opcode: "+(int)opcode+")");
			readAdvancedInstruction(opcode);
		}
		else if (mneumonic.equalsIgnoreCase("DAT"))
		{
			verbose("Dat.");
			readDat();
		}
		else
		{
			error("Unknown instruction: "+mneumonic);
		}
		
		
		debugToken.setToken(source.substring(debugTokenIndex, sourceIndex));
		debugTokenIndex = sourceIndex;
	}
	
	protected int nextNonWhitespace()
	{
		for (int i = sourceIndex; i < sourceLength; i++)
			if (!Character.isWhitespace(source.charAt(i)))
				return i;
		return -1;
	}
	
	protected boolean readToNextNonWhitespace()
	{
		while (sourceIndex < sourceLength)
			if (!Character.isWhitespace(readNextCharacter()))
			{
				sourceIndex--;
				return true;
			}
		return false;
	}
	
	
	protected String readValue()
	{
		if (sourceIndex >= sourceLength)
			return null;
		
		int start = nextNonWhitespace();
		if (start == -1)
		{
			warning("Value seems to be all whitespace.");
			return null;
		}

		boolean foundOpenBracket = false;
		for (sourceIndex = start; sourceIndex < sourceLength; sourceIndex++)
		{
			char c = source.charAt(sourceIndex);
			if (!foundOpenBracket)
			{
				if (c == ',' || Character.isWhitespace(c))
				{
					break;
				}
				else if (c == '[')
				    foundOpenBracket = true;
			}
			else
			{
				if (c == ']')
				{
					sourceIndex++;
					break;
				}
			}
		}
		
		return source.substring(start, sourceIndex++);
	}
	
	protected void readBasicInstruction(char opcode)
	{
		String bString = readValue();
		if (bString == null)
		{
			error("Unable to read b-value.");
			return;
		}
		else
		{
			verbose("Read bString: "+bString);
		}
		String aString = readValue();
		if (aString == null)
		{
			error("Unable to read a-value.");
			return;
		}
		else
		{
			verbose("Read aString: "+aString);
		}
		
		structure.add(new Instruction(opcode, bString, aString, this));
	}
	
	protected void readAdvancedInstruction(char opcode)
	{
		String aString = readValue();
		if (aString == null)
		{
			error("Unable to read a-value.");
			return;
		}
		
		structure.add(new Instruction(opcode, aString, this));
	}
	
	private static final char INVALID_ESCAPE = 'x';
	protected char unescapeCharacter(char c)
	{
		if (c == '\\')
			return '\\';
		else if (c == 'n')
			return '\n';
		else if (c == 'r')
			return '\r';
		else if (c == 't')
			return '\t';
		else if (c == '0')
			return '\0';
		else
		{
			warning("Unrecognised escape sequence: '\\"+c+'\'');
			return c;
		}
	}
	protected char readCharacterLiteral()
	{
		boolean escaped = false, foundChar = false;
		char ret = 0;
		while (sourceIndex < sourceLength)
		{
			char c = readNextCharacter();
			if (c < ' ')
				continue;
			else if (!escaped && c == '\\')
				escaped = true;
			else if (!foundChar)
			{
				if (escaped)
					c = unescapeCharacter(c);
				ret = c;
				foundChar = true;
			}
			else if (c == '\'')
			{
				verbose("Dat character: "+ret);
				return ret;
			}
			else
				break;
		}
		
		error("Unclosed character literal.");
		return 0;
	}
	
	protected String readStringLiteral()
	{
		boolean escaping = false;
		StringBuilder builder = new StringBuilder();
		while (sourceIndex < sourceLength)
		{
			char c = readNextCharacter();
			if (c < ' ')
				continue;
			
			if (!escaping)
			{
				if (c == '"')
				{
					verbose("Dat string: "+builder.toString());
					return builder.toString();
				}
				else if (c == '\\')
					escaping = true;
				else
					builder.append(c);
			}
			else
			{
				builder.append(unescapeCharacter(c));
			}
		}
		
		error("Unterminated string literal.");
		return builder.toString();
	}
	
	private int readDatNumber(String number)
	{
		if (number == null || number.length() == 0)
			return 0;
		if (number.startsWith("0x") || number.startsWith("0X"))
		{
			try
			{
				return Integer.parseInt(number.substring(2), 16);
			}
			catch (Exception ex) { return 0; }
		}
		
		if (number.startsWith("0b") || number.startsWith("0B"))
		{
			try
			{
				return Integer.parseInt(number.substring(2), 2);
			}
			catch (Exception ex) { return 0; }
		}
		
		try
		{
			return Integer.parseInt(number);
		}
		catch (Exception ex) { return 0; }
	}
	
	protected void readDat()
	{
		boolean carryOn = true;
		while (carryOn)
		{
			carryOn = false;
			
			if (!readToNextNonWhitespace())
			{
				error("Can't read to whitespace after DAT!");
				return;
			}
			
			char c = readNextCharacter();
			if (c == '\'')
				structure.add(new Dat(readCharacterLiteral()));
			else if (c == '"')
				structure.add(new Dat(readStringLiteral()));
			else if (Character.isDigit(c))
			{
				StringBuilder builder = new StringBuilder();
				builder.append(c);
				while (sourceIndex < sourceLength)
				{
					c = readNextCharacter();
					if (!Character.isDigit(c) && !Character.isLetter(c)) //c != 'x' && c != 'X' && c != 'b' && c != 'B')
					{
						if (c == ',')
							carryOn = true;
						break;
					}
					else
						builder.append(c);
				}
				
				verbose("Dat number: \""+builder.toString()+"\"");
				structure.add(new Dat((char)readDatNumber(builder.toString())));
			}
			else if (Character.isLetter(c))
			{
				StringBuilder builder = new StringBuilder();
				builder.append(c);
				while (sourceIndex < sourceLength)
				{
					c = readNextCharacter();
					if (!Character.isDigit(c) && !Character.isLetter(c) && c != '_')
					{
						if (c == ',')
							carryOn = true;
						break;
					}
					else
						builder.append(c);
				}
				
				verbose("Dat label: \""+builder.toString()+"\"");
				structure.add(new Dat(builder.toString(), true));
			}
		}
		
		verbose("End of dat.");
	}
	
	protected void handleLabels()
	{
		// Work out label positions by adding up the size of consecutive tokens
		// TODO: This could be done whilst the tokens are being added.
		int tokenCount = structure.size();
		char index = 0;
		for (int i = 0; i < tokenCount; i++)
		{
			Token t = structure.get(i);
			t.setOrigin(index);
			index += t.getCharCount();
		}
		
		// Map these up
		Map<String, Character> labelMap = new HashMap<String, Character>();
		for (Label label : labels.values())
			labelMap.put(label.getName(), label.getLocation());
		
		// Propogate through
		for (Token t : structure)
			t.substituteLabels(labelMap);
	}
	
	protected void handleDebugSymbols()
	{
		for (DebugToken dt : debugTokens)
			debugSymbols.put((int)dt.origin, dt.token);
	}
	
	protected char[] getMachineCodeFromStructure()
	{
		ArrayList<Character> outputList = new ArrayList<Character>();
		int count = structure.size();
		for (int i = 0; i < count; i++)
			structure.get(i).writeTo(outputList, this);
		
		char[] outputArray = new char[count = outputList.size()];
		for (int i = 0; i < count; i++)
			outputArray[i] = outputList.get(i).charValue();
		
		return outputArray;
	}
	
}
