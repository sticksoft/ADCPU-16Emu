package uk.co.sticksoft.adce.hardware;
import uk.co.sticksoft.adce.cpu.*;
import uk.co.sticksoft.adce.*;

public class GenericClock implements Device
{

	public char GetIDHi()
	{
		return 0x12d0;
	}

	public char GetIDLo()
	{
		return 0xb402;
	}

	public char GetVersion()
	{
		return 1;
	}

	public char GetManuHi()
	{
		return 0;
	}

	public char GetManuLo()
	{
		return 0;
	}
	
	private int millisDelay = 1;
	private char interrupt = 0;
	private int tickCount = 0;

	public void HWI_1_7(final CPU_1_7 cpu)
	{
		int A = cpu.register[CPU_1_7.A];
		char B = cpu.register[CPU_1_7.B];
		switch (A)
		{
			case 0:
			    if (B != 0)
					millisDelay = 1000 / (60 / B);
				else
				    millisDelay = 0;
			    break;
			case 1:
				cpu.register[CPU_1_7.C] = (char)(tickCount);
			    break;
			case 2:
			    interrupt = B;
			    break;
		}
		
		if ((A == 0 || A == 2) && millisDelay != 0 && interrupt != 0)
		{
			thread = new Thread(new Runnable()
			{
				public void run()
				{
					while (thread == Thread.currentThread() && millisDelay != 0 && interrupt != 0 && cpu == Options.GetCPU())
					{
						try
						{
							Thread.sleep(millisDelay);
						}
						catch (Exception ex) {}
						
						cpu.interrupt(interrupt);
					}
				}
			});
			MainActivity.me.log("Starting GenericClock!");
			thread.start();
		}
	}
	
	private Thread thread;
	
	public void Reset()
	{
		thread = null;
	}

}
