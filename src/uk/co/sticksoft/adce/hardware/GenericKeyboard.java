package uk.co.sticksoft.adce.hardware;

import java.util.LinkedList;
import java.util.Queue;

import uk.co.sticksoft.adce.Options;
import uk.co.sticksoft.adce.Options.DCPU_VERSION;
import uk.co.sticksoft.adce.Options.Observer;
import uk.co.sticksoft.adce.cpu.CPU_1_7;

public class GenericKeyboard implements Device, Observer
{
	private static GenericKeyboard me;
	public static GenericKeyboard GetLastInstance()
	{
		return me;
	}
	
	public GenericKeyboard()
	{
		me = this;
	}
	
	@Override
	public char GetIDHi()
	{
		return 0x30cf;
	}

	@Override
	public char GetIDLo()
	{
		return 0x7406;
	}

	@Override
	public char GetVersion()
	{
		return 1;
	}

	@Override
	public char GetManuHi()
	{
		return 0;
	}

	@Override
	public char GetManuLo()
	{
		return 0;
	}
	
	private char interrupt = 0;
	private CPU_1_7 cpu;

	@Override
	public void HWI_1_7(CPU_1_7 cpu)
	{
		int A = cpu.register[CPU_1_7.A];
		char B = cpu.register[CPU_1_7.B];
		switch (A)
		{
			case 0:
				synchronized (buffer)
				{
					buffer.clear();
				}
			    break;
			case 1:
				synchronized (buffer)
				{
					Character c = buffer.poll();
					if (c != null)
						cpu.register[CPU_1_7.C] = c.charValue();
					else
						cpu.register[CPU_1_7.C] = 0;
				}
			    break;
			case 2:
				if (B < keys.length)
					cpu.register[CPU_1_7.C] = keys[B] ? (char)1 : (char)0;
			    break;
			case 3:
				interrupt = B;
				this.cpu = cpu;
				Options.addObserver(this);
		}
	}

	@Override
	public void Reset()
	{
		interrupt = 0;
		synchronized (buffer)
		{
			buffer.clear();
		}
	}

	private boolean[] keys = new boolean[256];
	
	private Queue<Character> buffer = new LinkedList<Character>();
	
	
	public void keyDown(char key)
	{
		if (key < keys.length)
			keys[key] = true;
		
		synchronized (buffer)
		{
			if (buffer.size() < 16)
				buffer.add(Character.valueOf(key));
		}
		
		if (cpu != null && interrupt != 0)
			cpu.interrupt(interrupt);
	}
	
	public void keyUp(char key)
	{
		if (key < keys.length)
			keys[key] = false;
		
		if (cpu != null && interrupt != 0)
			cpu.interrupt(interrupt);
	}

	@Override
	public void optionsChanged()
	{
		if (cpu != null)
		{
			if (Options.GetDcpuVersion() == DCPU_VERSION._1_7)
				cpu = (CPU_1_7)Options.GetCPU();
			else
				cpu = null;
		}
	}
}
