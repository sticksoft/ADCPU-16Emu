package uk.co.sticksoft.adce.asm2;

import android.app.*;
import android.content.*;
import android.view.*;
import android.widget.*;

public class InstructionBuilder
{
    public InstructionBuilder(Context context, BubbleNode node, BubbleView view)
    {
	    this.node = node;
		this.context = context;
		this.view = view;
    }
	
	private BubbleNode node;
	private BubbleView view;
	private Context context;
	
	public void show()
	{
		LinearLayout lyt = new LinearLayout(context);
		lyt.setOrientation(LinearLayout.VERTICAL);
		
		final Button opBtn = new Button(context);
		opBtn.setText("SET");
		View v;
		opBtn.setOnClickListener(new View.OnClickListener()
		{
		    public void onClick(View p1)
			{
				new AlertDialog.Builder(context).setItems(BubbleNodeActions.basicOpcodes, new DialogInterface.OnClickListener()
				{
					public void onClick(DialogInterface p1, int i)
					{
						opBtn.setText(BubbleNodeActions.basicOpcodes[i]);
					}
				}).show();
			}
		});
		
		
		final EditText aTxt = new EditText(context);
		final EditText bTxt = new EditText(context);
		
		lyt.addView(opBtn);
		lyt.addView(aTxt);
		lyt.addView(bTxt);
		
		new AlertDialog.Builder(context)
		    .setView(lyt)
			.setPositiveButton("OK", new DialogInterface.OnClickListener()
			{

				public void onClick(DialogInterface p1, int p2)
				{
					node.text(opBtn.getText().toString());
					while (node.properties.size() < 2)
						node.properties.add(new BubbleNode());
					node.properties.get(0).text(aTxt.getText().toString());
					node.properties.get(1).text(bTxt.getText().toString());
					view.layoutBubbles();
					view.invalidate();
				}
				
			})
			.show();
	}
}
