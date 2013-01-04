package uk.co.sticksoft.adce;

import uk.co.sticksoft.adce.cpu.CPU;
import android.content.Context;
import android.view.View;
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
		
		View console = Options.getConsoleView(context);
		lyt.addView(console);
		addView(lyt);
	}
}
