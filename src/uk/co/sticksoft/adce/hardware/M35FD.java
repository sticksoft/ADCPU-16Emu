package uk.co.sticksoft.adce.hardware;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

import uk.co.sticksoft.adce.MainActivity;
import uk.co.sticksoft.adce.MainActivity.FileDialogCallback;
import uk.co.sticksoft.adce.Options;
import uk.co.sticksoft.adce.cpu.CPU;
import uk.co.sticksoft.adce.cpu.CPU_1_7;
import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.util.*;

public class M35FD extends FrameLayout implements Device, OnClickListener
{
	private Button blankButton, loadButton, saveButton, dumpButton, bootButton, ejectButton;
	private ToggleButton stateToggle;
	private TextView statusText;
	private ToggleButton endiannessToggle; // Only in this fucking game
	
	public M35FD(Context context)
	{
		super(context);
		
		ScrollView scroll = new ScrollView(context);
		LinearLayout layout = new LinearLayout(context);
		layout.setOrientation(LinearLayout.VERTICAL);
		scroll.addView(layout);
		
		blankButton = new Button(context);
		blankButton.setText("Insert blank disk");
		blankButton.setOnClickListener(this);
		layout.addView(blankButton);
		
		loadButton = new Button(context);
		loadButton.setText("Load disk image");
		loadButton.setOnClickListener(this);
		layout.addView(loadButton);
		
		saveButton = new Button(context);
		saveButton.setText("Save disk image");
		saveButton.setOnClickListener(this);
		layout.addView(saveButton);
				
		ejectButton = new Button(context);
		ejectButton.setText("Eject disk");
		ejectButton.setOnClickListener(this);
		layout.addView(ejectButton);
		
		dumpButton = new Button(context);
		dumpButton.setText("Save RAM dump");
		dumpButton.setOnClickListener(this);
		layout.addView(dumpButton);
		
		stateToggle = new ToggleButton(context);
		stateToggle.setTextOn("Append CPU state info");
		stateToggle.setTextOff("Append CPU state info");
		stateToggle.setChecked(true);
		layout.addView(stateToggle);

		
		bootButton = new Button(context);
		bootButton.setText("Load RAM dump");
		bootButton.setOnClickListener(this);
		layout.addView(bootButton);
		
		endiannessToggle = new ToggleButton(context);
		endiannessToggle.setTextOn("Little endian");
		endiannessToggle.setTextOff("Big endian");
		endiannessToggle.setChecked(true);
		layout.addView(endiannessToggle);
		
		statusText = new TextView(context);
		layout.addView(statusText);
		
		addView(scroll);
		
		updateStatus();
	}
	
	private StringBuilder disk; // Hahahahaha
	private boolean writeProtected = false;
	
	public static final int SECTOR_SIZE = 512;
	public static final int SECTORS = 1440;
	
	private void ensureSector(int sec)
	{
		int cap = (sec + 1) * SECTOR_SIZE;
		if (disk.length() < cap)
			disk.setLength(cap);
	}
	
	private void writeSector(char sector, char[] data)
	{
		sectorWrites++;
		ensureSector(sector);
		disk.replace(sector * SECTOR_SIZE, (sector+1)*SECTOR_SIZE-1, new String(data));
	}
	
	private void readSector(char sector, char[] buffer)
	{
		sectorReads++;
		ensureSector(sector);
		disk.getChars(sector * SECTOR_SIZE, (sector+1)*SECTOR_SIZE-1, buffer, 0);
	}
	
	private char[] loadFileAndConvertToChars(String path) throws Exception
	{
		if (endiannessToggle.isChecked())
			return loadFileAndConvertToCharsLittleEndian(path);
		else
			return loadFileAndConvertToCharsBigEndian(path);
	}

	private static char[] loadFileAndConvertToCharsLittleEndian(String path) throws Exception
	{
		FileInputStream fis = new FileInputStream(path);
		byte[] buffer = new byte[Math.max((int) fis.getChannel().size(), SECTORS * SECTOR_SIZE * 2)]; // Ensure we don't overrun disk size
		fis.read(buffer);
		fis.close();

		char[] cbuffer = new char[buffer.length / 2];
		
		for (int i = 0, j = 0; i < buffer.length-1 && j < cbuffer.length; i+=2, j++)
			cbuffer[j] = (char) ((buffer[i] & 0xFF) + ((buffer[i+1] & 0xFF) << 8));
		
		return cbuffer;
	}
	
	private static char[] loadFileAndConvertToCharsBigEndian(String path) throws Exception
	{
		FileInputStream fis = new FileInputStream(path);
		byte[] buffer = new byte[Math.max((int) fis.getChannel().size(), SECTORS * SECTOR_SIZE * 2)]; // Ensure we don't overrun disk size
		fis.read(buffer);
		fis.close();

		char[] cbuffer = new char[buffer.length / 2];
		
		for (int i = 0, j = 0; i < buffer.length-1 && j < cbuffer.length; i+=2, j++)
			cbuffer[j] = (char) ((buffer[i+1] & 0xFF) + ((buffer[i] & 0xFF) << 8));
		
		return cbuffer;
	}
	
	private void saveChars(String path, char[] cbuffer) throws Exception
	{
		if (endiannessToggle.isChecked())
			saveCharsLittleEndian(path, cbuffer);
		else
			saveCharsBigEndian(path, cbuffer);
	}
	
	private static void saveCharsLittleEndian(String path, char[] cbuffer) throws Exception
	{
		FileOutputStream fos = new FileOutputStream(path);
		byte[] buffer = new byte[cbuffer.length*2];
		for (int i = 0, j = 0; i < cbuffer.length; i++, j+=2)
		{
			buffer[j] = (byte)(cbuffer[i] & 0xFF);
			buffer[j+1] = (byte)((cbuffer[i] >> 8) & 0xFF);
		}
		
		fos.write(buffer);
		fos.close();
	}
	
	private static void saveCharsBigEndian(String path, char[] cbuffer) throws Exception
	{
		FileOutputStream fos = new FileOutputStream(path);
		byte[] buffer = new byte[cbuffer.length*2];
		for (int i = 0, j = 0; i < cbuffer.length; i++, j+=2)
		{
			buffer[j+1] = (byte)(cbuffer[i] & 0xFF);
			buffer[j] = (byte)((cbuffer[i] >> 8) & 0xFF);
		}
		
		fos.write(buffer);
		fos.close();
	}
	
	private static long lastExceptionTime = 0;
	private static long exceptionCache = 0;
	public static void handleException(Exception ex)
	{
		Log.e("M35FD", "Exception occurred.", ex);
		long time = System.currentTimeMillis();
		
		if (time > lastExceptionTime + 3000)
		{
			if (exceptionCache == 0)
				MainActivity.showToast("Exception: "+ex.getClass().getSimpleName(), Toast.LENGTH_SHORT);
			else
				MainActivity.showToast("Exception: "+ex.getClass().getSimpleName() + " \n(Plus "+exceptionCache+" other" + ((exceptionCache==1)?"":"s") + ".", Toast.LENGTH_SHORT);
			
			lastExceptionTime = time;
			exceptionCache = 0;
		}
		else
			exceptionCache++;
		
	}
	
	public boolean isWriteProtected()
	{
		return writeProtected;
	}

	@Override
	public void onClick(View v)
	{
		if (v == blankButton)
		{
			disk = new StringBuilder();
			state = isWriteProtected() ? State.STATE_READY_WP : State.STATE_READY;
			updateStatus();
		}
		else if (v == loadButton)
		{
			MainActivity.showFileDialog(false, new FileDialogCallback()
			{
				@Override
				public void onPathSelected(String path)
				{
					try
					{
						char[] buffer = loadFileAndConvertToChars(path);
						disk = new StringBuilder(buffer.length);
						disk.append(buffer);
						state = isWriteProtected() ? State.STATE_READY_WP : State.STATE_READY;
						updateStatus();
					}
					catch (Exception ex)
					{
						handleException(ex);
					}
				}
			});
		}
		else if (v == saveButton)
		{
			MainActivity.showFileDialog(true, new FileDialogCallback()
			{
				@Override
				public void onPathSelected(String path)
				{
					try
					{
						char[] cbuffer = new char[disk.length()];
						disk.getChars(0, disk.length(), cbuffer, 0);
						saveChars(path, cbuffer);
					}
					catch (Exception ex)
					{
						handleException(ex);
					}
				}
			});
		}
		else if (v == dumpButton)
		{
			final char[] data;
			if (stateToggle.isChecked())
			{
				char[] state = Options.GetCPU().getStateInfo();
				char[] RAM = Options.GetCPU().RAM;
				
				data = new char[RAM.length + state.length];
				System.arraycopy(RAM, 0, data, 0, RAM.length);
				System.arraycopy(state, 0, data, RAM.length, state.length);
			}
			else
				data = Options.GetCPU().RAM;
			
			MainActivity.showFileDialog(true, new FileDialogCallback()
			{
				@Override
				public void onPathSelected(String path)
				{
					try
					{
						saveChars(path, data);
					}
					catch (Exception ex)
					{
						handleException(ex);
					}
				}
			});
				
		}
		else if (v == bootButton)
		{
			MainActivity.showFileDialog(false, new FileDialogCallback()
			{
				@Override
				public void onPathSelected(String path)
				{
					try
					{
						char[] buffer = loadFileAndConvertToChars(path);
						
						CPU cpu = Options.GetCPU();
						int extra = buffer.length - cpu.RAM.length;
						System.arraycopy(buffer, 0, cpu.RAM, 0, Math.min(buffer.length, cpu.RAM.length));
						
						if (extra > 0)
						{
							char[] state = new char[extra];
							System.arraycopy(buffer, cpu.RAM.length, state, 0, extra);
							cpu.setStateInfo(state);
						}
						
						MainActivity.getLastInstance().updateInfo();
					}
					catch (Exception ex)
					{
						handleException(ex);
					}
				}
			});
		}
		else if (v == ejectButton)
		{
			disk = null;
			state = State.STATE_NO_MEDIA;
			updateStatus();
		}
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
	
	private CPU_1_7 cpu;
	private char interrupt;
	
	private Thread ioThread;
	
	private int hwiCount, sectorReads, sectorWrites;
	
	@Override
	public void HWI_1_7(final CPU_1_7 cpu)
	{
		hwiCount++;
		char A = cpu.register[CPU_1_7.A];
		
		switch (A)
		{
		case 0:
			cpu.register[CPU_1_7.B] = state.value;
			cpu.register[CPU_1_7.C] = error.value;
			break;
		case 1:
			this.cpu = cpu;
			this.interrupt = cpu.register[CPU_1_7.X];
			updateStatus();
			break;
		case 2:
			if (state == State.STATE_READY || state == State.STATE_READY_WP)
			{
				final char X = cpu.register[CPU_1_7.X], Y = cpu.register[CPU_1_7.Y];
				ioThread = new Thread(new Runnable()
				{
					@Override
					public void run()
					{
						try
						{
							Thread.sleep(16); 
							char[] buffer = new char[SECTOR_SIZE];
							
							if (disk != null)
							{
								readSector(X, buffer);
								System.arraycopy(buffer, 0, cpu.RAM, Y, SECTOR_SIZE);
								if (interrupt != 0)
									cpu.interrupt(interrupt);
								state = isWriteProtected() ? State.STATE_READY_WP : State.STATE_READY;
							}
							else
							{
								state = State.STATE_NO_MEDIA;
								error = Error.ERROR_EJECT;
							}
						}
						catch (Exception ex)
						{
							MainActivity.showToast("Exception reading: "+ex.getClass().getSimpleName(), Toast.LENGTH_SHORT);
							Log.e("M35FD", "Exception reading.", ex);
						}
						updateStatus();
					}
				});
				
				cpu.register[CPU_1_7.B] = 1;
				state = State.STATE_BUSY;
				ioThread.start();
			}
			else
			{
				cpu.register[CPU_1_7.B] = 0;
				if (state == State.STATE_NO_MEDIA)
					error = Error.ERROR_NO_MEDIA;
				else if (state == State.STATE_BUSY)
					error = Error.ERROR_BUSY;
			}
			updateStatus();
			break;
		case 3:
			if ((state == State.STATE_READY || state == State.STATE_READY_WP) && !writeProtected)
			{
				final char X = cpu.register[CPU_1_7.X], Y = cpu.register[CPU_1_7.Y];
				ioThread = new Thread(new Runnable()
				{
					@Override
					public void run()
					{
						try
						{
							Thread.sleep(16);
							char[] buffer = new char[SECTOR_SIZE];
							
							if (disk != null && !writeProtected)
							{
								System.arraycopy(cpu.RAM, Y, buffer, 0, SECTOR_SIZE);
								writeSector(X, buffer);
								if (interrupt != 0)
									cpu.interrupt(interrupt);
								state = isWriteProtected() ? State.STATE_READY_WP : State.STATE_READY;
							}
							else if (writeProtected)
							{
								state = State.STATE_READY_WP;
								error = Error.ERROR_PROTECTED;
							}
							else
							{
								state = State.STATE_NO_MEDIA;
								error = Error.ERROR_EJECT;
							}
						}
						catch (Exception ex)
						{
							MainActivity.showToast("Exception writing: "+ex.getClass().getSimpleName(), Toast.LENGTH_SHORT);
							Log.e("M35FD", "Exception writing.", ex);
						}
						updateStatus();
					}
				});
				
				cpu.register[CPU_1_7.B] = 1;
				state = State.STATE_BUSY;
				ioThread.start();
			}
			else
			{
				cpu.register[CPU_1_7.B] = 0;
				if (state == State.STATE_NO_MEDIA)
					error = Error.ERROR_NO_MEDIA;
				else if (state == State.STATE_BUSY)
					error = Error.ERROR_BUSY;
				else if (writeProtected)
					error = Error.ERROR_PROTECTED;
			}
			updateStatus();
			break;
		}
	}
	
	private static String humanReadableSize(long size)
	{
		return humanReadableSize((float)size);
	}
	private static String humanReadableSize(int size)
	{
		return humanReadableSize((float)size);
	}
	private static String humanReadableSize(float size)
	{
		return humanReadableSize(size, true);
	}
	private static String humanReadableSize(float size, boolean kiloIs1024)
	{
		final int k = (kiloIs1024 ? 1024 : 1000);
		if (size < k)
			return String.format("%.0f", size);
		else if (size < k * k)
			return String.format("%.1fk", size / 1000.0f);
		else if (size < k * k * k)
			return String.format("%.2fM", size / (1000.0f * 1000.0f));
		else
			return String.format("%.2fG", size / (1000.0f * 1000.0f * 1000.0f));
	}
	
	private boolean statusUpdatePending = false;
	public synchronized void updateStatus()
	{
		if (statusUpdatePending)
			return;
		else
		{
			statusUpdatePending = true;
			postDelayed(new Runnable()
			{
				@Override
				public void run()
				{
					statusText.setText(
							"State: "+state.name()+" \n" +
							"Error: "+error.name()+" \n" +
							"Interrupt: "+interrupt+" \n" +
							"Received HWIs: "+hwiCount+" \n" +
							"Sector reads: "+sectorReads+" \n" +
							"Sector writes: "+sectorWrites+" \n" +
							"Disk size: "+((disk == null) ? "" : (humanReadableSize(disk.length()) + "B (" + humanReadableSize(disk.length()/2) + "w)"))
					);
					
					statusUpdatePending = false;
				}
			}, 500);
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
	public State state = State.STATE_NO_MEDIA;
	public enum State
	{
		STATE_NO_MEDIA (0x0000),
		STATE_READY    (0x0001),
		STATE_READY_WP (0x0002),
		STATE_BUSY     (0x0003);
	
		public final char value;
		State(int value) { this.value = (char)value; }
	}
	
	public Error error = Error.ERROR_NONE;
	public enum Error
	{
		ERROR_NONE       (0x0000),
		ERROR_BUSY       (0x0001),
		ERROR_NO_MEDIA   (0x0002),
		ERROR_PROTECTED  (0x0003),
		ERROR_EJECT      (0x0004),
		ERROR_BAD_SECTOR (0x0005),
		ERROR_BROKEN     (0xffff);
			
		public final char value;
		Error(int value) { this.value = (char)value; }
	}

}
