package uk.co.sticksoft.adce.asm2;

import java.util.ArrayList;
import java.util.List;

import uk.co.sticksoft.adce.MainActivity;
import uk.co.sticksoft.adce.asm2.BubbleNode.NodeAction;
import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TableLayout;
import android.widget.TableRow;

public class ActionTable extends ScrollView
{
	private final static int COLUMNS = 4;
	
	private BubbleView view;
	private BubbleNode node;
	
	public ActionTable(BubbleView view, ArrayList<ArrayList<NodeAction>> actions, BubbleNode node)
	{
		super(view.getContext());
		
		Context context = view.getContext();
		
		this.view = view;
		this.node = node;
		
		TableLayout table = new TableLayout(context);
		table.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		
		for (int i = 0; i < actions.size(); i++)
		{
			ArrayList<NodeAction> group = actions.get(i);
			
			TableRow row = new TableRow(context);
			
			for (int j = 0; j < group.size(); j++)
			{
				if ((j % COLUMNS) == 0)
					row.setLayoutParams(new TableLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
				
				Button button = new Button(context);
				final NodeAction action = group.get(j);
				
				button.setText(action.getName());
				button.setLayoutParams(new TableRow.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT, 1.0f));
				button.setOnClickListener(new OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						onAction(action);
					}
				});
				
				int colour = action.getColour();
				button.getBackground().setColorFilter(colour, PorterDuff.Mode.MULTIPLY);
				if (Color.red(colour) < 127 && Color.green(colour) < 127 && Color.blue(colour) < 127)
					button.setTextColor(Color.WHITE);
				
				row.addView(button);
				
				if (((j+1)%COLUMNS) == 0 || j == group.size() - 1)
				{
					table.addView(row);
					row = new TableRow(context);
				}
			}
		}
		
		for (int i = 0; i < COLUMNS; i++)
		{
			table.setColumnStretchable(i, false);
			table.setColumnShrinkable(i, true);
		}
		
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
	
	public void onAction(NodeAction action)
	{
		view.uncover(this);
		MainActivity.setBackHandler(null);
		action.performAction(view.getContext(), view, node);
		view.layoutBubbles();
	}

}
