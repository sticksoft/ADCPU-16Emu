package uk.co.sticksoft.adce.asm2;

import java.util.ArrayList;

import uk.co.sticksoft.adce.asm2.BubbleNode.NamedNodeAction;
import uk.co.sticksoft.adce.asm2.BubbleNode.NodeAction;
import uk.co.sticksoft.adce.asm2.BubbleNode.NodeRelation;

import android.content.Context;

public class BubbleNodeActions
{
	public static BubbleNode clipboard = null;
	
	public final static String[] basicOpcodes = new String[] { "set", "add", "sub", "mul", "mli", "div", "dvi", "mod", "mdi", "and", "bor", "xor", "shr", "asr", "shl", "ifb", "ifc", "ife", "ifn", "ifg", "ifa", "ifl", "ifu", "adx", "sbx", "sti", "std" };
	
	public final static String[] advancedOpcodes = new String[] { "jsr", "int", "iag", "ias", "rfi", "iaq", "hwn", "hwq", "hwi" };
	
	public final static String[] registers = new String[] { "a", "b", "c", "x", "y", "z", "i", "j", "pop", "peek", "pick", "sp", "pc", "ex" };
	
	public static void collectActions(ArrayList<BubbleNode.NodeAction> list, BubbleNode node)
	{
		// Replacements
		collectReplacementsMatchCase(list, node, basicOpcodes);
		collectReplacementsMatchCase(list, node, advancedOpcodes);
		collectReplacementsMatchCase(list, node, registers);
		
		/*
		// Prefixes and suffixes
		if (!"//".equals(node.text) && node.relation != NodeRelation.Property)
			list.add(new NamedNodeAction("//")
			{
				@Override
				public void performAction(Context context, BubbleView view, BubbleNode node)
				{
					BubbleNode child = new BubbleNode();
					child.text = node.text;
					node.insertProperty(child, 0);
					
					node.text = "//";
					
					view.layoutBubbles();
				}
			});
		else if ("//".equals(node.text) && node.properties.size() > 0)
			list.add(new NamedNodeAction("//")
			{
				@Override
				public void performAction(Context context, BubbleView view, BubbleNode node)
				{
					BubbleNode child = node.properties.get(0);
					node.text = child.text;
					node.removeProperty(child);
					
					view.layoutBubbles();
				}
			});
		
		if (node.parent != null)
		{
			if (node.relation == NodeRelation.Property && !";".equals(node.text) && node.parent.properties.get(node.parent.properties.size()-1) == node)
			{
				list.add(new NamedNodeAction(";")
				{
					@Override
					public void performAction(Context context, BubbleView view, BubbleNode node)
					{
						BubbleNode child = new BubbleNode();
						child.text = ";";
						node.parent.addProperty(child);
						view.layoutBubbles();
					}
				});
				
				if (node.parent.parent != null)
					list.add(new NamedNodeAction("{ }")
					{
						@Override
						public void performAction(Context context, BubbleView view, BubbleNode node)
						{
							int index = node.parent.parent.children.indexOf(node.parent);
							if (index < 0)
								return;
							
							BubbleNode child = new BubbleNode();
							child.text = "{";
							node.parent.parent.insertChild(child, index + 1);
							
							child = new BubbleNode();
							child.text = "}";
							node.parent.parent.insertChild(child, index + 2);
							
							view.layoutBubbles();
						}
					});
			}
		}
		*/
		// Generic actions
		/*
		list.add(new NamedNodeAction("Add Child")
		{
			@Override
			public void performAction(Context context, BubbleView view, BubbleNode node)
			{
				BubbleNode child = new BubbleNode();
				node.addChild(child);
				child.showEdit(context, view);
				view.layoutBubbles();
			}
		});
		*/
		
		list.add(new NamedNodeAction("Append")
		{
			@Override
			public void performAction(Context context, BubbleView view, BubbleNode node)
			{
				BubbleNode child = new BubbleNode();
				node.addProperty(child);
				child.showEdit(context, view);
				view.layoutBubbles();
			}
		});
		
		list.add(new NamedNodeAction("Edit Text")
		{
			@Override
			public void performAction(Context context, BubbleView view, BubbleNode node)
			{
				node.showEdit(context, view);
			}
		});
		
		list.add(new NamedNodeAction("Cut")
		{
			@Override
			public void performAction(Context context, BubbleView view, BubbleNode node)
			{
				clipboard = new BubbleNode(node);
				node.delete(context, view);
				view.layoutBubbles();
			}
		});
		
		list.add(new NamedNodeAction("Copy")
		{
			@Override
			public void performAction(Context context, BubbleView view, BubbleNode node)
			{
				clipboard = new BubbleNode(node);
			}
		});
		
		if (clipboard != null)
		{
			list.add(new NamedNodeAction("Paste Above")
			{
				@Override
				public void performAction(Context context, BubbleView view, BubbleNode node)
				{
					int index = view.getRoots().indexOf(node);
					view.getRoots().add(index, new BubbleNode(clipboard));
					view.layoutBubbles();
				}
			});
			
			list.add(new NamedNodeAction("Paste Below")
			{
				@Override
				public void performAction(Context context, BubbleView view, BubbleNode node)
				{
					int index = view.getRoots().indexOf(node);
					view.getRoots().add(index + 1, new BubbleNode(clipboard));
					view.layoutBubbles();
				}
			});
		}
		
		list.add(new NamedNodeAction("Delete")
		{
			@Override
			public void performAction(Context context, BubbleView view, BubbleNode node)
			{
				node.delete(context, view);
			}
		});
	}
	
	private static void collectGenericReplacements(ArrayList<NodeAction> list, BubbleNode node, final String[] reps)
	{
		String text = node.text();
		if (text == null || text.length() == 0)
			return;
		
		int index = -1;
		for (int i = 0; i < reps.length; i++)
		{
			if (text.equals(reps[i]))
			{
				index = i;
				break;
			}
		}
		if (index == -1)
			return;
		
		for (int i = 0; i < reps.length; i++)
		{
			if (index == i)
				continue;
			
			final int j = i;
			list.add(new NamedNodeAction(reps[i])
			{
				@Override
				public void performAction(Context context, BubbleView view, BubbleNode node)
				{
					node.text(reps[j]);
				}
			});
		}
	}
	
	private static void collectReplacementsMatchCase(ArrayList<NodeAction> list, BubbleNode node, final String[] reps)
	{
		String text = node.text();
		if (text == null || text.length() == 0)
			return;

		int index = -1;
		for (int i = 0; i < reps.length; i++)
		{
			if (text.equalsIgnoreCase(reps[i]))
			{
				index = i;
				break;
			}
		}
		if (index == -1)
			return;

		for (int i = 0; i < reps.length; i++)
		{
			if (index == i)
				continue;
				
			StringBuilder sb = new StringBuilder();
			String src = text, dst = reps[i];
			boolean upper = true;
			
			for (int j = 0; j < dst.length(); j++)
			{
				if (j < src.length())
					upper = Character.isUpperCase(src.charAt(j));
					sb.append(Character.toUpperCase(dst.charAt(j)));
			}
			
			final String name = sb.toString();
			list.add(new NamedNodeAction(name)
				{
					@Override
					public void performAction(Context context, BubbleView view, BubbleNode node)
					{
						node.text(name);
					}
				});
		}
	}
}
