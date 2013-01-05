package uk.co.sticksoft.adce.cpu;

import java.util.HashSet;

public abstract class CPU
{
	public char[] RAM = new char[0x10000];
	public long cycleCount = 0;

	public abstract void reset();
	public abstract void execute();
	public abstract String getStatusText();
	public abstract char[] getStateInfo();
	public abstract void setStateInfo(char[] state);
	public abstract char getLastResult();
	
	public interface Observer
	{
		void onCpuExecution(CPU cpu);
	}
	
	private HashSet<Observer> observers = new HashSet<CPU.Observer>();
	public void addObserver(Observer ob)
	{
		observers.add(ob);
	}
	
	public void removeObserver(Observer ob)
	{
		observers.remove(ob);
	}
	
	private long lastNotify = 0;
	public void notifyObservers()
	{
		long time = System.currentTimeMillis();
		if (time == lastNotify)
			return;
		lastNotify = time;
		
		for (Observer o : observers)
			o.onCpuExecution(this);
	}
	
	public static void intToLittleEndian(int in, byte[] out, int outIndex) { out[outIndex++] = (byte)(in & 0xff); out[outIndex++] = (byte)((in >> 8) & 0xff); out[outIndex++] = (byte)((in >> 16) & 0xff); out[outIndex++] = (byte)((in >> 24) & 0xff); }
	public static int intFromLittleEndian(byte[] in, int inIndex) { return (in[inIndex] & 0xff) + ((in[inIndex+1] & 0xff) << 8) + ((in[inIndex+2] & 0xff) << 16) + ((in[inIndex+3] & 0xff) << 24); }
	
	public static void intToLittleEndian(int in, char[] out, int outIndex) { out[outIndex++] = (char)(in & 0xffff); out[outIndex++] = (char)((in >> 16) & 0xffff); }
	public static int intFromLittleEndian(char[] in, int inIndex) { return (in[inIndex] & 0xffff) + ((in[inIndex+1] & 0xffff) << 16); }
}
