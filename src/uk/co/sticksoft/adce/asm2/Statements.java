package uk.co.sticksoft.adce.asm2;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.net.NetworkInfo.State;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class Statements
{
	public static void showAddStatement(final Context context, final BubbleView view, final BubbleNode node)
	{
		Statement[] values = Statement.values();
		String[] titles = new String[values.length+1];
		for (int i = 0; i < values.length; i++)
			titles[i] = values[i].name().toLowerCase();
		titles[values.length] = "Cancel";
		
		new AlertDialog.Builder(context).setItems(titles, new OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				if (which >= Statement.values().length)
				{
					dialog.dismiss();
					return;
				}
				
				Statement.values()[which].showAdd(context, view, node);
			}
		}).show();
	}
	
	public static Button makeVariableSelector(Context context, BubbleView view, BubbleNode node, String defaultValue)
	{
		final Button btn = new Button(context);
		btn.setText(defaultValue);
		return btn;
	}
	
	public static Button makeTextEditButton(Context context, BubbleView view, BubbleNode node, String defaultValue)
	{
		final Button btn = new Button(context);
		btn.setText(defaultValue);
		return btn;
	}
	
	private static TextView makeTextView(Context context, String text)
	{
		TextView tv = new TextView(context);
		tv.setText(text);
		return tv;
	}
	
	// Can't believe I'm using Java enums again :s
	public enum Statement
	{
		IF
		{
			@Override
			public void showAdd(Context context, BubbleView view, BubbleNode node)
			{
				
			}
		},
		FOR
		{
			@Override
			public void showAdd(Context context, final BubbleView view, final BubbleNode node)
			{
				final Button loopvar = makeVariableSelector(context, view, node, "i");
				final Button from = makeTextEditButton(context, view, node, "0");
				final Button to = makeTextEditButton(context, view, node, "10");
				final Button increment = makeTextEditButton(context, view, node, "++");
				
				final LinearLayout lyt = new LinearLayout(context);
				lyt.setOrientation(LinearLayout.VERTICAL);
				lyt.addView(makeTextView(context, "Loopvar"));
				lyt.addView(loopvar);
				lyt.addView(makeTextView(context, "From"));
				lyt.addView(from);
				lyt.addView(makeTextView(context, "To"));
				lyt.addView(to);
				lyt.addView(makeTextView(context, "Increment"));
				lyt.addView(increment);
				
				new AlertDialog.Builder(context).setTitle("For")
					.setView(lyt)
					.setPositiveButton("OK", new OnClickListener()
					{
						public BubbleNode addNode(BubbleNode node, String text)
						{
							BubbleNode b = new BubbleNode();
							b.text(text);
							node.addChild(b);
							return b;
						}
						
						public BubbleNode addProperty(BubbleNode node, String text)
						{
							BubbleNode b = new BubbleNode();
							b.text(text);
							node.addProperty(b);
							return b;
						}
						
						@Override
						public void onClick(DialogInterface dialog, int which)
						{
							BubbleNode _for = addNode(node, "for");
							String i = loopvar.getText().toString();
							addProperty(_for, i + " = " + from.getText().toString());
							addProperty(_for, i + " < " + to.getText().toString());
							String inc = increment.getText().toString();
							addProperty(_for, i + ((inc.length() == 2) ? inc : " " + inc));
							
							view.layoutBubbles();
							view.invalidate();
						}
					})
					.setNegativeButton("Cancel", null)
					.show();
			}
		},
		
		;
		
		public abstract void showAdd(Context context, BubbleView view, BubbleNode node);
	}
}
