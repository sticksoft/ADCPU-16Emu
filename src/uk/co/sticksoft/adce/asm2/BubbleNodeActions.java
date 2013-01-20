package uk.co.sticksoft.adce.asm2;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import uk.co.sticksoft.adce.asm2.BubbleNode.NamedNodeAction;
import uk.co.sticksoft.adce.asm2.BubbleNode.NodeAction;
import android.content.Context;
import android.graphics.Color;

public class BubbleNodeActions
{
	public static BubbleNode clipboard = null;
	
	public final static String[] arithmeticOpcodes = new String[] { "set", "add", "sub", "mul", "mli", "div", "dvi", "mod", "mdi", "and", "bor", "xor", "shr", "asr", "shl" };
	
	public final static String[] flowOpcodes = new String[] { "ifb", "ifc", "ife", "ifn", "ifg", "ifa", "ifl", "ifu" };
	
	public final static String[] tertiaryOpcodes = new String[] { "adx", "sbx", "sti", "std" };
	
	public final static String[] basicOpcodes = combineStringArrays(combineStringArrays(arithmeticOpcodes, flowOpcodes), tertiaryOpcodes);
	
	public final static String[] advancedOpcodes = new String[] { "jsr", "int", "iag", "ias", "rfi", "iaq", "hwn", "hwq", "hwi" };
	
	public final static String[] allOpcodes = combineStringArrays(basicOpcodes, advancedOpcodes);
	
	public final static String[] registers = new String[] { "A", "B", "C", "X", "Y", "Z", "I", "J", "POP", "PEEK", "PICK", "SP", "PC", "EX" };
	
	public static Set<String> labels = new HashSet<String>();
	
	public static void collectActions(ArrayList<ArrayList<NodeAction>> list, BubbleNode node)
	{
		int utilColour = Color.LTGRAY;
		ArrayList<NodeAction> utilList = new ArrayList<NodeAction>();
		
		if (node.isRoot())
		{
			collectReplacements(list, node, arithmeticOpcodes, Color.argb(255, 192, 255, 192), true);
			collectReplacements(list, node, flowOpcodes, Color.argb(255, 255, 192, 192), true);
			collectReplacements(list, node, tertiaryOpcodes, Color.argb(255, 192, 192, 255), true);
		
		}
		else
		{
			collectReplacements(list, node, registers, Color.argb(255, 192, 255, 192), true);
			
			String[] shortLabels = shortLabels();
			String[] longLabels = longLabels();
			
			if ((shortLabels.length / 4 + longLabels.length) < 5)
				collectReplacements(list, node, combineStringArrays(shortLabels, longLabels), Color.argb(255, 255, 192, 192), false);
			else
				utilList.add(new NamedNodeAction("Labels", utilColour)
				{
					@Override
					public void performAction(Context context, BubbleView view, BubbleNode node)
					{
						
					}
				});
		}
		
		utilList.add(new NamedNodeAction("Add", utilColour)
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
		
		utilList.add(new NamedNodeAction("Append", utilColour)
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
		
		utilList.add(new NamedNodeAction("Edit Text", utilColour)
		{
			@Override
			public void performAction(Context context, BubbleView view, BubbleNode node)
			{
				node.showEdit(context, view);
			}
		});
		
		utilList.add(new NamedNodeAction("Cut", utilColour)
		{
			@Override
			public void performAction(Context context, BubbleView view, BubbleNode node)
			{
				clipboard = new BubbleNode(node);
				node.delete(context, view);
				view.layoutBubbles();
			}
		});
		
		utilList.add(new NamedNodeAction("Copy", utilColour)
		{
			@Override
			public void performAction(Context context, BubbleView view, BubbleNode node)
			{
				clipboard = new BubbleNode(node);
			}
		});
		
		if (clipboard != null)
		{
			utilList.add(new NamedNodeAction("Paste Above", utilColour)
			{
				@Override
				public void performAction(Context context, BubbleView view, BubbleNode node)
				{
					int index = view.getRoots().indexOf(node);
					view.getRoots().add(index, new BubbleNode(clipboard));
					view.layoutBubbles();
				}
			});
			
			utilList.add(new NamedNodeAction("Paste Below", utilColour)
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
		
		if (!node.isRoot())
			utilList.add(new NamedNodeAction("Indirect", utilColour)
			{
				@Override
				public void performAction(Context context, BubbleView view, BubbleNode node)
				{
					String text = node.text;
					if (text.startsWith("[") && text.endsWith("]"))
						node.text(text.substring(1, text.length()-1));
					else
						node.text("["+text+"]");
					view.layoutBubbles();
				}
			});
		
		utilList.add(new NamedNodeAction("Delete", utilColour)
		{
			@Override
			public void performAction(Context context, BubbleView view, BubbleNode node)
			{
				node.delete(context, view);
				view.layoutBubbles();
			}
		});
		
		list.add(utilList);
	}
	
	private static String[] shortLabels()
	{
		ArrayList<String> shorts = new ArrayList<String>();
		for (String s : labels)
			if (s.length() < 8)
				shorts.add(s);
		
		Collections.sort(shorts);
		return shorts.toArray(new String[shorts.size()]);
	}
	
	private static String[] longLabels()
	{
		ArrayList<String> shorts = new ArrayList<String>();
		for (String s : labels)
			if (s.length() >= 8)
				shorts.add(s);
		
		Collections.sort(shorts);
		return shorts.toArray(new String[shorts.size()]);
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
	
	
	private static void collectReplacements(ArrayList<ArrayList<NodeAction>> list, BubbleNode node, final String[] reps, int colour, boolean matchCase)
	{
		ArrayList<NodeAction> currentList = new ArrayList<NodeAction>();
		
		String text = node.text();
		
		if (text != null && text.startsWith(":"))
			text = text.substring(1);

		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < reps.length; i++)
		{
			String name;
			if (matchCase)
			{
				sb.setLength(0);
				String src = text, dst = reps[i];
				boolean upper = true;
				
				for (int j = 0; j < dst.length(); j++)
				{
					if (j < src.length())
						upper = Character.isUpperCase(src.charAt(j));
					if (upper)
						sb.append(Character.toUpperCase(dst.charAt(j)));
					else
						sb.append(Character.toLowerCase(dst.charAt(j)));
				}
				
				name = sb.toString();
			}
			else
				name = reps[i];
			
			addTextAction(currentList, name, (text != null && text.equalsIgnoreCase(name)) ? Color.DKGRAY : colour);
		}
		
		if (currentList.size() > 0)
			list.add(currentList);
	}
	
	private static void addTextAction(ArrayList<NodeAction> list, final String text, int colour)
	{
		list.add(new NamedNodeAction(text, colour)
		{
			@Override
			public void performAction(Context context, BubbleView view, BubbleNode node)
			{
				node.text(text);
			}
		});
	}
	
	private static void addTextActions(ArrayList<NodeAction> list, Iterable<String> strings, int colour)
	{
		for (String s : strings)
			addTextAction(list, s, colour);
	}
	
	private static void addTextActions(ArrayList<NodeAction> list, String[] strings, int colour)
	{
		for (String s : strings)
			addTextAction(list, s, colour);
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
