package uk.co.sticksoft.adce.asm2;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Color;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

public class BubbleNode
{
	
	protected ArrayList<BubbleNodeProperty> properties = new ArrayList<BubbleNodeProperty>();
	
	public float width = 16, height = 16;
	public float x,y;
	
	protected String text = "";
	
	public int normalColour = Color.rgb(0, 138, 0), selectedColour = Color.rgb(0, 0, 138);
	public int normalTextColour = Color.LTGRAY, selectedTextColour = Color.WHITE;
	
	public BubbleNode()
	{
	}
	
	public BubbleNode(String text)
	{
		text(text);
	}
	
	public BubbleNode(String text, int colour)
	{
		text(text);
		this.normalColour = colour;
	}
	
	public BubbleNode(BubbleNode original)
	{
		for (int i = 0; i < original.properties.size(); i++)
			addProperty(new BubbleNodeProperty(original.properties.get(i)));
		
		width = original.width;
		height = original.height;
		text(text);
		setColours(original.normalColour, original.selectedColour, original.normalTextColour, original.selectedTextColour);
	}
	
	public void text(String s)
	{
		if (text != null && text.startsWith(":") && text.length() > 1)
			BubbleNodeActions.labels.remove(text.substring(1));
		text = s;
		guessColourBasedOnContent();
	}
	
	public String text()
	{
		return text;
	}
	
	public void addProperty(BubbleNodeProperty node)
	{
		properties.add(node);
		node.parent = this;
	}
	
	public void insertProperty(BubbleNodeProperty node, int index)
	{
		properties.add(index, node);
		node.parent = this;
	}
	
	public void removeProperty(BubbleNodeProperty node)
	{
		properties.remove(node);
		if (node.parent == this)
			node.parent = null;
	}
	
	public ArrayList<BubbleNodeProperty> properties()
	{
		return properties;
	}
	
	public static BubbleNode supplementalTap = null;
	public BubbleNode checkTap(float x, float y)
	{
		for (BubbleNode b : properties)
		{
			BubbleNode n = b.checkTap(x,y);
			if (n != null)
				return n;
		}
		
		final int marg = 1;
		
		if (x >= this.x - marg && y >= this.y - marg && y <= this.y + this.height + marg)
		{
			if (x <= this.x + this.width + marg)
				return this;
			else if (supplementalTap == null || this.x + this.width > supplementalTap.x + supplementalTap.width) 
				supplementalTap = this;
		}
		
		return null;
	}
	
	public static interface NodeAction
	{
		void performAction(Context context, BubbleView view, BubbleNode node);
		String getName();
		int getColour();
	}
	
	public static abstract class NamedNodeAction implements NodeAction
	{
		private String name;
		private int colour = Color.WHITE;
		
		public NamedNodeAction(String name, int colour) { this.name = name; this.colour = colour; }
		public String getName() { return name; }
		public int getColour() { return colour; }
	}
	
	public void showOptions(final Context context, final BubbleView view)
	{
		final ArrayList<ArrayList<NodeAction>> actions = new ArrayList<ArrayList<NodeAction>>();
		BubbleNodeActions.collectActions(actions, this);
		
		view.showActions(actions, this);
	}

	public void showEdit(final Context context, final BubbleView view)
	{
		final EditText txt_edit = new EditText(context);
		txt_edit.setText(text);
		new AlertDialog.Builder(context).setTitle("Edit").setView(txt_edit).setPositiveButton("OK", new OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				text(txt_edit.getText().toString());
				view.layoutBubbles();
				view.invalidate();
			}
		}).show();
		
		txt_edit.requestFocus();
		
		txt_edit.postDelayed(new Runnable()
		{
			@Override
			public void run()
			{
				InputMethodManager imm = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
				imm.showSoftInput(txt_edit, InputMethodManager.SHOW_IMPLICIT);
			}
		}, 100);
	}
	
	public void delete(Context context, BubbleView view)
	{
	    view.getRoots().remove(this);
		view.invalidate();
	}
	
	protected void setColours(int normal, int selected, int text, int selectedText)
	{
		normalColour = normal;
		selectedColour = selected;
		normalTextColour = text;
		selectedTextColour = selectedText;
	}
	
	public boolean hasProperty(String text)
	{
		if (this.text != null && this.text.equals(text))
			return true;
		for (BubbleNode node : properties)
			if (node != null && node.text != null && node.text.equals(text))
				return true;
		return false;
	}
	
	public boolean hasPropertyContaining(String text)
	{
		if (this.text != null && this.text.contains(text))
			return true;
		for (BubbleNode node : properties)
			if (node != null && node.text != null && node.text.contains(text))
				return true;
		return false;
	}
	
	private static long lastUpdateTime;
	public static void changed()
	{
		lastUpdateTime = System.currentTimeMillis();
	}
	public static long getLastUpdateTime()
	{
		return lastUpdateTime;
	}
	
	public BubbleNode getRoot()
	{
		return this;
	}
	
	public boolean isRoot()
	{
		return getRoot() == this;
	}
	
	protected void guessColourBasedOnContent()
	{
		if (text.startsWith(";"))
			normalColour = Color.rgb(64, 128, 64);
		else if (text.startsWith(":") && text.length() > 1)
		{
			normalColour = Color.rgb(128,64,64);
			BubbleNodeActions.labels.add(text.substring(1));
			return;
		}
		else
			for (String s : BubbleNodeActions.allOpcodes)
				if (s.equalsIgnoreCase(text))
				{
					normalColour = Color.rgb(64, 64, 128);
					return;
				}
		
		normalColour = Color.rgb(64, 64, 64);
	}
}
