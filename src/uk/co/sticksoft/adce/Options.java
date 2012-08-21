package uk.co.sticksoft.adce;

import java.util.HashSet;
import java.util.Set;

import uk.co.sticksoft.adce.asm.Assembler;
import uk.co.sticksoft.adce.asm.Assembler_1_1;
import uk.co.sticksoft.adce.asm.Assembler_1_7;
import uk.co.sticksoft.adce.cpu.CPU;
import uk.co.sticksoft.adce.cpu.CPU_1_1;
import uk.co.sticksoft.adce.cpu.CPU_1_7;

public class Options
{
	public enum DCPU_VERSION
	{
		_1_1,
		_1_7;
	}
	
	private static DCPU_VERSION current_version = DCPU_VERSION._1_7;
	
	public static void SetDcpuVersion(DCPU_VERSION version)
	{
		current_version = version;
		cpu = null; // Recreate when next got
	}
	
	private static CPU cpu;
	public static CPU GetCPU()
	{
		if (cpu == null)
		{
			switch (current_version)
			{
			case _1_1:
				cpu = new CPU_1_1();
				break;
			case _1_7:
				cpu = new CPU_1_7();
				break;
			}
		}
		
		return cpu;
	}
	
	public static Assembler getAssembler()
	{
		switch (current_version)
		{
		case _1_1:
			return new Assembler_1_1();
		case _1_7:
			return new Assembler_1_7();
		}
		
		return null;
	}
	
	public interface Observer
	{
		void optionsChanged();
	}
	
	private static Set<Observer> observers = new HashSet<Observer>();
	public static void addObserver(Observer o)
	{
		observers.add(o);
	}
	public static void removeObserver(Observer o)
	{
		observers.remove(o);
	}
	protected static void notifyObservers()
	{
		for (Observer o : observers)
			o.optionsChanged();
	}
}