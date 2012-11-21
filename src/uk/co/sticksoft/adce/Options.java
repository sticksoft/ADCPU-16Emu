package uk.co.sticksoft.adce;

import java.util.HashSet;
import java.util.Set;

import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

import uk.co.sticksoft.adce.asm.Assembler;
import uk.co.sticksoft.adce.asm.Assembler_1_1;
import uk.co.sticksoft.adce.asm.Assembler_1_7;
import uk.co.sticksoft.adce.cpu.CPU;
import uk.co.sticksoft.adce.cpu.CPU_1_1;
import uk.co.sticksoft.adce.cpu.CPU_1_7;
import uk.co.sticksoft.adce.hardware.Console;
import uk.co.sticksoft.adce.hardware.HardwareManager;
import uk.co.sticksoft.adce.hardware.LEM1802;
import android.content.Context;
import android.util.Xml;
import android.view.View;
import android.widget.Toast;

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
		
		optionsChanged();
	}
	
	public static DCPU_VERSION GetDcpuVersion()
	{
		return current_version;
	}
	
	
	private static boolean showTextEditor = true;
	private static boolean showVisualEditor = true, visualEditorSet = false;
	
	public static boolean IsTextEditorShown() { return showTextEditor; }
	public static boolean IsVisualEditorShown() { return showVisualEditor; }
	
	public static void SetTextEditorShown(boolean show) { showTextEditor = show; optionsChanged(); }
	public static void SetVisualEditorShown(boolean show) { showVisualEditor = show; optionsChanged(); }
	
	
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
	
	
	private static LEM1802 lem1802;
	public static View getConsoleView(Context context)
	{
		switch (current_version)
		{
		case _1_1:
			return new Console(context);
		case _1_7:
			if (lem1802 == null)
			{
				lem1802 = new LEM1802(context);
				HardwareManager.instance().addDevice(lem1802);
			}
			return lem1802;
		}
		
		return null;
	}
	
	private static boolean loading = false;
	private static void optionsChanged()
	{
		if (!loading)
		{
			saveOptions(MainActivity.getCurrentContext());
			notifyObservers();
		}
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
		HashSet<Observer> copy = new HashSet<Options.Observer>(observers);
		for (Observer o : copy)
			o.optionsChanged();
	}
	
	
	private final static String
		XML_FILENAME = "config.xml",
		XML_ELEMENT_ROOT = "config",
		XML_ELEMENT_HARDWARE_CONTAINER = "box",
		XML_ELEMENT_DCPU = "dcpu",
		XML_ATTRIBUTE_MAJOR = "major",
		XML_ATTRIBUTE_MINOR = "minor",
		XML_ELEMENT_EDITORS = "editors",
		XML_ELEMENT_TEXT_EDITOR = "text_editor",
		XML_ELEMENT_VISUAL_EDITOR = "visual_editor",
		XML_ATTRIBUTE_SHOWN = "shown",
		
		XML_VALUE_TRUE = "true",
		XML_VALUE_FALSE = "false",
		XML_VALUE_UNSET = "unset";
	
	public static void saveOptions(Context context)
	{
		XmlSerializer serializer = Xml.newSerializer();
		
		try
		{
			serializer.setOutput(context.openFileOutput(XML_FILENAME, Context.MODE_WORLD_WRITEABLE), null);
			
			serializer.startDocument("UTF-8", true);
			serializer.startTag("", XML_ELEMENT_ROOT);
			
				serializer.startTag("", XML_ELEMENT_HARDWARE_CONTAINER);
					serializer.startTag("", XML_ELEMENT_DCPU);
						if (current_version == DCPU_VERSION._1_1)
						{
							serializer.attribute("", XML_ATTRIBUTE_MAJOR, "1");
							serializer.attribute("", XML_ATTRIBUTE_MINOR, "1");
						}
						else if (current_version == DCPU_VERSION._1_7)
						{
							serializer.attribute("", XML_ATTRIBUTE_MAJOR, "1");
							serializer.attribute("", XML_ATTRIBUTE_MINOR, "7");
						}
					serializer.endTag("", XML_ELEMENT_DCPU);
				serializer.endTag("", XML_ELEMENT_HARDWARE_CONTAINER);
				
				serializer.startTag("", XML_ELEMENT_EDITORS);
					serializer.startTag("", XML_ELEMENT_TEXT_EDITOR);
						serializer.attribute("", XML_ATTRIBUTE_SHOWN, showTextEditor ? XML_VALUE_TRUE : XML_VALUE_FALSE);
					serializer.endTag("", XML_ELEMENT_TEXT_EDITOR);
					serializer.startTag("", XML_ELEMENT_VISUAL_EDITOR);
						serializer.attribute("", XML_ATTRIBUTE_SHOWN, showVisualEditor ? XML_VALUE_TRUE : (visualEditorSet ? XML_VALUE_FALSE : XML_VALUE_UNSET));
					serializer.endTag("", XML_ELEMENT_VISUAL_EDITOR);
				serializer.endTag("", XML_ELEMENT_EDITORS);
			
			serializer.endTag("", XML_ELEMENT_ROOT);
			serializer.endDocument();
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	
	public static void loadOptions(Context context)
	{
		int dcpuMajor = 1, dcpuMinor = 7;
		boolean textEditor = true;
		boolean visualEditor = true;
		
		try
		{
			XmlPullParser parser = Xml.newPullParser();
			parser.setInput(context.openFileInput(XML_FILENAME), null);
			int event = parser.getEventType();
			
			while (event != XmlPullParser.END_DOCUMENT)
			{
				if (event == XmlPullParser.START_TAG)
				{
					if (parser.getName().equals(XML_ELEMENT_DCPU))
					{
						for (int i = 0; i < parser.getAttributeCount(); i++)
						{
							String attrib = parser.getAttributeName(i);
							if (XML_ATTRIBUTE_MAJOR.equals(attrib))
							{
								try { dcpuMajor = Integer.parseInt(parser.getAttributeValue(i)); } catch (NumberFormatException nfe) {}
							}
							else if (XML_ATTRIBUTE_MINOR.equals(attrib))
							{
								try { dcpuMinor = Integer.parseInt(parser.getAttributeValue(i)); } catch (NumberFormatException nfe) {}
							}
						}
					}
					else if (parser.getName().equals(XML_ELEMENT_TEXT_EDITOR))
					{
						if (parser.getAttributeCount() == 1 && XML_ATTRIBUTE_SHOWN.equals(parser.getAttributeName(0)))
							textEditor = XML_VALUE_TRUE.equals(parser.getAttributeValue(0));
					}
					else if (parser.getName().equals(XML_ELEMENT_VISUAL_EDITOR))
					{
						if (parser.getAttributeCount() == 1 && XML_ATTRIBUTE_SHOWN.equals(parser.getAttributeName(0)))
						{
							visualEditor = XML_VALUE_TRUE.equals(parser.getAttributeValue(0));
							visualEditorSet = !XML_VALUE_UNSET.equals(parser.getAttributeValue(0));
						}
					}
				}
				event = parser.next();
			}
			
			MainActivity.showToast("Loaded settings; DCPU version "+dcpuMajor+"."+dcpuMinor, Toast.LENGTH_SHORT);
			
			// Apply loaded values
			loading = true;
			
			if (dcpuMajor == 1 && dcpuMinor == 7)
				SetDcpuVersion(DCPU_VERSION._1_7);
			else
				SetDcpuVersion(DCPU_VERSION._1_1);
			
			SetTextEditorShown(textEditor);
			SetVisualEditorShown(visualEditor);
		}
		catch (Exception ex)
		{
			MainActivity.showToast("Couldn't load settings, using defaults.", Toast.LENGTH_SHORT);
		}
		finally
		{
			loading = false;
		}

		// Finish up and notify of changes
		notifyObservers();


	}
}