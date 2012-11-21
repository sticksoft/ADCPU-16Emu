package uk.co.sticksoft.adce.asm2;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;

public class BubbleNodeProperty extends BubbleNode
{
	public BubbleNode parent;
	
	public BubbleNodeProperty()
	{
		super();
	}
	
	public BubbleNodeProperty(String text)
	{
		super(text);
	}
	
	public BubbleNodeProperty(String text, int colour)
	{
		super(text, colour);
	}
	
	public BubbleNodeProperty(BubbleNodeProperty original)
	{
		width = original.width;
		height = original.height;
		text = original.text;
		setColours(original.normalColour, original.selectedColour, original.normalTextColour, original.selectedTextColour);
	}
	
	public void delete(Context context, BubbleView view)
	{
		if (this.parent != null)
			this.parent.removeProperty(this);
		
		view.invalidate();
	}
	
	public void addProperty(BubbleNodeProperty node)
	{
		parent.addProperty(node);
	}
	
	public void insertProperty(BubbleNodeProperty node, int index)
	{
		parent.insertProperty(node, index);
	}
	
	public void removeProperty(BubbleNodeProperty node)
	{
		parent.removeProperty(node);
	}
	
	public ArrayList<BubbleNodeProperty> properties()
	{
		return parent.properties();
	}
	
	public BubbleNode getRoot()
	{
		return parent;
	}
	
	protected void guessColourBasedOnContent()
	{
		normalColour = Color.rgb(128,64,64);
	}
}
