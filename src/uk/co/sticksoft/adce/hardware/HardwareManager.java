package uk.co.sticksoft.adce.hardware;

import java.util.ArrayList;
import java.util.List;

import android.util.Log;

public class HardwareManager
{
	private static HardwareManager mgr = new HardwareManager();
	public static HardwareManager instance()
	{
		return mgr;
	}
	
	private HardwareManager()
	{
	}
	
	private List<Device> devices = new ArrayList<Device>();
	
	public void addDevice(Device d)
	{
		Log.i("HardwareManager", "Device "+devices.size()+" added:" + d.getClass().getSimpleName());
		devices.add(d);
	}
	
	public void removeDevice(Device d)
	{
		devices.remove(d);
	}
	
	public int getCount()
	{
		return devices.size();
	}
	
	public void clear()
	{
		devices.clear();
	}
	
	public void reset()
	{
		for (Device d : devices)
		    d.Reset();
	}
	
	public Device getDevice(int index)
	{
		if (index >= 0 && index < devices.size())
			return devices.get(index);
		else
			return null;
	}
}
