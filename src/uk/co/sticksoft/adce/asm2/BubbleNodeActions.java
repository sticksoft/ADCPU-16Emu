package uk.co.sticksoft.adce.asm2;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import uk.co.sticksoft.adce.asm2.BubbleNode.NamedNodeAction;
import uk.co.sticksoft.adce.asm2.BubbleNode.NodeAction;
import android.content.Context;

public class BubbleNodeActions
{
	public static BubbleNode clipboard = null;
	
	public final static String[] basicOpcodes = new String[] { "set", "add", "sub", "mul", "mli", "div", "dvi", "mod", "mdi", "and", "bor", "xor", "shr", "asr", "shl", "ifb", "ifc", "ife", "ifn", "ifg", "ifa", "ifl", "ifu", "adx", "sbx", "sti", "std" };
	
	public final static String[] advancedOpcodes = new String[] { "jsr", "int", "iag", "ias", "rfi", "iaq", "hwn", "hwq", "hwi" };
	
	public final static String[] allOpcodes = combineStringArrays(basicOpcodes, advancedOpcodes);
	
	public final static String[] registers = new String[] { "A", "B", "C", "X", "Y", "Z", "I", "J", "POP", "PEEK", "PICK", "SP", "PC", "EX" };
	
	public static Set<String> labels = new HashSet<String>();
	
	public static void collectActions(ArrayList<BubbleNode.NodeAction> list, BubbleNode node)
	{
		// Replacements
		collectReplacementsMatchCase(list, node, allOpcodes);
		
		//String[] allValues = combineStringArrays(registers, labels.toArray(new String[labels.size()]));
		
		if (collectReplacementsMatchCase(list, node, registers) || isNumeric(node.text))
			addTextActions(list, labels);
		else if (labels.contains(node.text))
		{
			addTextActions(list, registers);
			String lab = node.text.substring(1);
			for (String s : labels)
				if (!s.equals(lab))
					addTextAction(list, s);
		}
		
		
		list.add(new NamedNodeAction("Add")
		{
			@Override
			public void performAction(Context context, BubbleView view, BubbleNode node)
			{
				int index = 0;
				if (node != null)
					index = view.getRoots().indexOf(node.getRoot()) + 1;
				
				BubbleNode instr = new BubbleNode("Set");
				instr.addProperty(new BubbleNodeProperty("A"));
				instr.addProperty(new BubbleNodeProperty("B"));
				view.getRoots().add(index, instr);
				view.layoutBubbles();
			}
		});
		
		list.add(new NamedNodeAction("Append")
		{
			@Override
			public void performAction(Context context, BubbleView view, BubbleNode node)
			{
				BubbleNodeProperty child = new BubbleNodeProperty();
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
		
		list.add(new NamedNodeAction("Indirect")
		{
			@Override
			public void performAction(Context context, BubbleView view, BubbleNode node)
			{
				String text = node.text;
				if (text.startsWith("[") && text.endsWith("]"))
					node.text(text.substring(1, text.length()-1));
				else
					node.text("["+text+"]");
			}
		});
		
		list.add(new NamedNodeAction("Delete")
		{
			@Override
			public void performAction(Context context, BubbleView view, BubbleNode node)
			{
				node.delete(context, view);
			}
		});
	}

	private static String[] combineStringArrays(String[] a, String[] b)
	{
		if (b.length == 0)
			return a;
		if (a.length == 0)
			return b;
		
		String[] output = new String[a.length + b.length];
		System.arraycopy(a, 0, output, 0, a.length);
		System.arraycopy(b, 0, output, a.length, b.length);
		return output;
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
	
	private static boolean collectReplacementsMatchCase(ArrayList<NodeAction> list, BubbleNode node, final String[] reps)
	{
		String text = node.text();
		if (text == null || text.length() == 0)
			return false;

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
			return false;

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
			addTextAction(list, name);
		}
		
		return true;
	}
	
	private static void addTextAction(ArrayList<NodeAction> list, final String text)
	{
		list.add(new NamedNodeAction(text)
		{
			@Override
			public void performAction(Context context, BubbleView view, BubbleNode node)
			{
				node.text(text);
			}
		});
	}
	
	private static void addTextActions(ArrayList<NodeAction> list, Iterable<String> strings)
	{
		for (String s : strings)
			addTextAction(list, s);
	}
	
	private static void addTextActions(ArrayList<NodeAction> list, String[] strings)
	{
		for (String s : strings)
			addTextAction(list, s);
	}
	
	private static boolean isNumeric(String text)
	{
		if (text.startsWith("0x"))
		{
			if (text.length() > 2)
			{
				try
				{
					Integer.parseInt(text.substring(2));
					return true;
				}
				catch (NumberFormatException nfe)
				{
				}
			}
			
			return false;
		}
		else
		{
			try
			{
				Integer.parseInt(text);
				return true;
			}
			catch (NumberFormatException nfe)
			{
				return false;
			}
		}
	}
	
}
