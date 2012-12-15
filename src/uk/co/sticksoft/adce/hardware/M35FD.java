package uk.co.sticksoft.adce.hardware;

import uk.co.sticksoft.adce.cpu.CPU_1_7;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.util.*;

public class M35FD extends View implements Device, OnClickListener
{
	private Button loadButton, saveButton, dumpButton;
	
	public M35FD(Context context)
	{
		super(context);
		
		LinearLayout layout = new LinearLayout(context);
		layout.setOrientation(LinearLayout.VERTICAL);
		
		loadButton = new Button(context);
		loadButton.setText("Load disk image");
		loadButton.setOnClickListener(this);
		layout.addView(loadButton);
		
		saveButton = new Button(context);
		saveButton.setText("Save disk image");
		saveButton.setOnClickListener(this);
		layout.addView(saveButton);
		
		dumpButton = new Button(context);
		dumpButton.setText("Dump RAM as disk image");
		dumpButton.setOnClickListener(this);
		layout.addView(dumpButton);
	}
	
	private StringBuilder disk = new StringBuilder(); // Hahahahaha
	
	public static final int SECTOR_SIZE = 1024;
	public static final int SECTORS = 1440;
	private void blankDisk(int words)
	{
		disk.setLength(0);
		disk.setLength(words);
	}
	
	private void ensureSector(int sec)
	{
		int cap = (sec + 1) * SECTOR_SIZE;
		if (disk.length() < cap)
			disk.setLength(cap);
	}
	
	private void writeSector(char sector, char[] data)
	{
		ensureSector(sector);
		disk.replace(sector * SECTOR_SIZE, (sector+1)*SECTOR_SIZE-1, new String(data));
	}
	
	private void readSector(char sector, char[] buffer)
	{
		ensureSector(sector);
		disk.getChars(sector * SECTOR_SIZE, (sector+1)*SECTOR_SIZE-1, buffer, 0);
	}
	

	@Override
	public void onClick(View v)
	{
		
	}

	@Override
	public char GetIDHi()
	{
		return 0x4fd5;
	}

	@Override
	public char GetIDLo()
	{
		return 0x24c5;
	}

	@Override
	public char GetVersion()
	{
		return 0xb;
	}

	@Override
	public char GetManuHi()
	{
		return 0x1eb3;
	}

	@Override
	public char GetManuLo()
	{
		return 0x7e91;
	}

	@Override
	public void HWI_1_7(CPU_1_7 cpu)
	{
		char A = cpu.register[cpu.A];
		
		switch (A)
		{
		    
		}
	}

	@Override
	public void Reset()
	{
	}
	
	/*
	 * A: Behavior:

0  Poll device. Sets B to the current state (see below) and C to the last error
   since the last device poll.
   
1  Set interrupt. Enables interrupts and sets the message to X if X is anything
   other than 0, disables interrupts if X is 0. When interrupts are enabled,
   the M35FD will trigger an interrupt on the DCPU-16 whenever the state or
   error message changes.

2  Read sector. Reads sector X to DCPU ram starting at Y.
   Sets B to 1 if reading is possible and has been started, anything else if it
   fails. Reading is only possible if the state is STATE_READY or
   STATE_READY_WP.
   Protects against partial reads.
   
3  Write sector. Writes sector X from DCPU ram starting at Y.
   Sets B to 1 if writing is possible and has been started, anything else if it
   fails. Writing is only possible if the state is STATE_READY.
   Protects against partial writes.


    .-------------.
----! STATE CODES !-------------------------------------------------------------
    '-------------'
  
0x0000 STATE_NO_MEDIA   There's no floppy in the drive.
0x0001 STATE_READY      The drive is ready to accept commands.
0x0002 STATE_READY_WP   Same as ready, except the floppy is write protected.
0x0003 STATE_BUSY       The drive is busy either reading or writing a sector.



    .-------------.
----! ERROR CODES !-------------------------------------------------------------
    '-------------'
    
0x0000 ERROR_NONE       There's been no error since the last poll.
0x0001 ERROR_BUSY       Drive is busy performing an action
0x0002 ERROR_NO_MEDIA   Attempted to read or write with no floppy inserted.
0x0003 ERROR_PROTECTED  Attempted to write to write protected floppy.
0x0004 ERROR_EJECT      The floppy was removed while reading or writing.
0x0005 ERROR_BAD_SECTOR The requested sector is broken, the data on it is lost.
0xffff ERROR_BROKEN     There's been some major software or hardware problem,
                        try turning off and turning on the device again.
	 */

}
