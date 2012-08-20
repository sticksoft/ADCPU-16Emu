package uk.co.sticksoft.adce.asm._1_7;

import java.util.List;
import java.util.Map;

import uk.co.sticksoft.adce.asm.Assembler_1_7;

public interface Token
{
	void writeTo(List<Character> output, Assembler_1_7 assembler);
	
	int getCharCount();
	
	void substituteLabels(Map<String, Character> labelMap);
	
	void setOrigin(char origin);
}