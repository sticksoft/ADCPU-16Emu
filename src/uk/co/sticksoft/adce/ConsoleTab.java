package uk.co.sticksoft.adce;

import uk.co.sticksoft.adce.cpu.CPU;
import uk.co.sticksoft.adce.hardware.Console;
import android.content.Context;
import android.widget.LinearLayout;
import android.widget.ScrollView;

public class ConsoleTab extends ScrollView
{
	private CPU cpu;
	
	public ConsoleTab(Context context, CPU cpu)
	{
		super(context);
		
		LinearLayout lyt = new LinearLayout(context);
		lyt.setOrientation(LinearLayout.VERTICAL);
		
		Console console = new Console(context);
		lyt.addView(console);
		addView(lyt);
	}
}
