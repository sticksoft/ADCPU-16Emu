package uk.co.sticksoft.adce.asm2;

import java.util.List;

import uk.co.sticksoft.adce.MainActivity;
import uk.co.sticksoft.adce.asm2.BubbleNode.NodeAction;
import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;

public class ActionTable extends ScrollView
{
	private final static int COLUMNS = 4;
	
	private BubbleView view;
	private BubbleNode node;
	private List<NodeAction> actions;
	
	public ActionTable(BubbleView view, List<NodeAction> actions, BubbleNode node)
	{
		super(view.getContext());
		
		Context context = view.getContext();
		
		this.view = view;
		this.actions = actions;
		this.node = node;
		
		TableLayout table = new TableLayout(context);
		
		for (int i = 0; i < actions.size(); )
		{
			TableRow row = new TableRow(context);
			
			for (int j = 0; j < COLUMNS && i < actions.size(); j++)
			{
				Button button = new Button(context);
				button.setText(actions.get(i).getName());
				final int I = i;
				button.setOnClickListener(new OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						onAction(I);
					}
				}); 
				row.addView(button);
				i++;
			}
			
			table.addView(row);
		}
		
		for (int i = 0; i < COLUMNS; i++)
			table.setColumnStretchable(i, true);
		
		addView(table);
		
		MainActivity.setBackHandler(new Runnable()
		{
			@Override
			public void run()
			{
				ActionTable.this.view.uncover(ActionTable.this);
			}
		});
	}
	
	public void onAction(int index)
	{
		view.uncover(this);
		actions.get(index).performAction(view.getContext(), view, node);
		MainActivity.setBackHandler(null);
	}

}
