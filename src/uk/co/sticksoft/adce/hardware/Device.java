package uk.co.sticksoft.adce.hardware;

import uk.co.sticksoft.adce.cpu.CPU_1_7;

public interface Device
{
	char GetIDHi();
	char GetIDLo();
	char GetVersion();
	char GetManuHi();
	char GetManuLo();
	
	void HWI_1_7(CPU_1_7 cpu);
}
