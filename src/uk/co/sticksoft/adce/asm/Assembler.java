package uk.co.sticksoft.adce.asm;

import java.util.*;

public interface Assembler
{
    public char[] assemble(String s, ArrayList<String> out_messages, HashMap<Integer,String> debugSymbols);
}
