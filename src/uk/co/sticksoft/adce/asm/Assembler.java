package uk.co.sticksoft.adce.asm;

import java.util.ArrayList;
import java.util.HashMap;

public interface Assembler
{
    public char[] assemble(String s, ArrayList<String> out_messages, HashMap<Integer,String> debugSymbols);
}
