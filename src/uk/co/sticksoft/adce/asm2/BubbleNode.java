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
	public enum NodeRelation
	{
		Property,
		Child
	};
	
	protected ArrayList<BubbleNode> children = new ArrayList<BubbleNode>();
	protected ArrayList<BubbleNode> properties = new ArrayList<BubbleNode>();
	
	public float width = 16, height = 16;
	public float x,y;
	
	public BubbleNode parent;
	public NodeRelation relation;
	
	protected String text = "";
	
	public int normalColour = Color.rgb(0, 138, 0), selectedColour = Color.rgb(0, 0, 138);
	public int normalTextColour = Color.LTGRAY, selectedTextColour = Color.WHITE;
	
	public BubbleNode()
	{
	}
	
	public BubbleNode(String text)
	{
		this.text = text;
	}
	
	public BubbleNode(String text, int colour)
	{
		this.text = text;
		this.normalColour = colour;
	}
	
	public BubbleNode(BubbleNode original)
	{
		for (int i = 0; i < original.children.size(); i++)
			addChild(new BubbleNode(original.children.get(i)));
		for (int i = 0; i < original.properties.size(); i++)
			addProperty(new BubbleNode(original.properties.get(i)));
		
		width = original.width;
		height = original.height;
		text = original.text;
		setColours(original.normalColour, original.selectedColour, original.normalTextColour, original.selectedTextColour);
	}
	
	public void text(String s)
	{
		text = s;
	}
	
	public String text()
	{
		return text;
	}
	
	public void addChild(BubbleNode node)
	{
		children.add(node);
		node.parent = this;
		node.relation = NodeRelation.Child;
	}
	
	public void insertChild(BubbleNode node, int index)
	{
		children.add(index, node);
		node.parent = this;
		node.relation = NodeRelation.Child;
	}
	
	public void setChild(BubbleNode node, int index)
	{
		children.set(index, node);
		node.parent = this;
		node.relation = NodeRelation.Child;
	}
	
	public void removeChild(BubbleNode node)
	{
		children.remove(node);
		properties.remove(node);
		if (node.parent == this)
			node.parent = null;
	}
	
	public void addProperty(BubbleNode node)
	{
		properties.add(node);
		node.parent = this;
		node.relation = NodeRelation.Property;
	}
	
	public void insertProperty(BubbleNode node, int index)
	{
		properties.add(index, node);
		node.parent = this;
		node.relation = NodeRelation.Property;
	}
	
	public void removeProperty(BubbleNode node)
	{
		properties.remove(node);
		if (node.parent == this)
			node.parent = null;
	}
	
	public ArrayList<BubbleNode> children()
	{
		return children;
	}
	
	public ArrayList<BubbleNode> properties()
	{
		return properties;
	}
	
	public float getChildrenHeight()
	{
		float h = height;
		for (BubbleNode b : children)
			h = Math.max(h, b.getChildrenHeight() + b.y - this.y);
		
		return h;
	}
	
	public static BubbleNode supplementalTap = null;
	public BubbleNode checkTap(float x, float y)
	{
		for (BubbleNode b : children)
		{
			BubbleNode n = b.checkTap(x,y);
			if (n != null)
				return n;
		}
		
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
	}
	
	public static abstract class NamedNodeAction implements NodeAction
	{
		public NamedNodeAction(String name) { this.name = name; }
		private String name;
		public String getName() { return name; }
	}
	
	public void showOptions(final Context context, final BubbleView view)
	{
		final ArrayList<NodeAction> actions = new ArrayList<BubbleNode.NodeAction>();
		collectOptions(actions);
		
		String[] names = new String[actions.size()];
		for (int i = 0; i < actions.size(); i++)
			names[i] = actions.get(i).getName();
		
		new AlertDialog.Builder(context).setItems(names, new OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				actions.get(which).performAction(context, view, BubbleNode.this);
				dialog.dismiss();
				
				view.layoutBubbles();
				view.invalidate();
			}
		}).setNegativeButton("Cancel", null).show();
	}
	
	protected void collectOptions(ArrayList<NodeAction> list)
	{
		BubbleNodeActions.collectActions(list, this);
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
				text = txt_edit.getText().toString();
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
		if (this.parent != null)
			this.parent.removeChild(this);
		else
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
	
	public BubbleNode getNextSibling()
	{
		if (parent == null)
			return null;
		
		if (relation == NodeRelation.Child)
		{
			int index = parent.children.indexOf(this);
			if (index < parent.children.size()-1 && index > -1)
				return parent.children.get(index + 1);
			else
				return parent.getNextSibling();
		}
		else if (relation == NodeRelation.Property)
		{
			int index = parent.properties.indexOf(this);
			if (index < parent.properties.size()-1 && index > -1)
				return parent.properties.get(index + 1);
			else
				return parent.getNextSibling();
		}
		
		return null;
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
}
