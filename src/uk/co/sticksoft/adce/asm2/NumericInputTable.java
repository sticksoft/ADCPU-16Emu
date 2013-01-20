package uk.co.sticksoft.adce.asm2;

import android.content.Context;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;


public class NumericInputTable extends ActionTable
{
	private static int COLUMNS = 3;
	
	private TextView numberTextView;
	private Button decButton, hexButton;
	
	private static boolean hexMode = true;
	
	public NumericInputTable(final BubbleView view, final BubbleNode node)
	{
		super(view, node);
		
		Context context = view.getContext();
		
		LinearLayout linear = new LinearLayout(context);
		linear.setOrientation(LinearLayout.VERTICAL);
		linear.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		addView(linear);
		
		numberTextView = new TextView(context);
		numberTextView.setGravity(Gravity.RIGHT);
		numberTextView.setTextSize(32.0f);
		numberTextView.setPadding(20, 20, 20, 20);
		linear.addView(numberTextView);
		
		
		TableLayout table = new TableLayout(context);
		linear.addView(table);
		table.setLayoutParams(new LinearLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
		
		TableRow row;
		
		
		OnClickListener flipRadix = new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				hexMode = !hexMode;
				setupHexMode();
			}
		};
		
		row = new TableRow(context);
		
		decButton = new Button(context);
		decButton.setText("Dec");
		decButton.setOnClickListener(flipRadix);
		decButton.setLayoutParams(new TableRow.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT, 1.0f));
		row.addView(decButton);
		
		hexButton = new Button(context);
		hexButton.setText("Hex");
		hexButton.setOnClickListener(flipRadix);
		hexButton.setLayoutParams(new TableRow.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT, 1.0f));
		row.addView(hexButton);
		
		Button backspace = new Button(context);
		backspace.setText("<--");
		backspace.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				String number = numberTextView.getText().toString();
				if (number != null && number.length() > 0)
					setNumber(number.substring(0, number.length()-1), false);
			}
		});
		backspace.setLayoutParams(new TableRow.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT, 1.0f));
		row.addView(backspace);
		
		table.addView(row);
		
		for (int i = 15-2; i > 0; i -= 6) 
		{
			
			row = new TableRow(context);
			row.setLayoutParams(new TableLayout.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT));
			
			for (int j = 0; j < 3; j++, i++)
			{
				Button button = new Button(context);
				final char digit = i < 10 ? (char)('0' + i) : (char)('A' + (i-10));
				button.setText("" + digit);
				button.setLayoutParams(new TableRow.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT, 1.0f));
				button.setOnClickListener(new OnClickListener()
				{
					@Override
					public void onClick(View v)
					{
						String number = numberTextView.getText().toString();
						if (number == null)
							setNumber(""+digit, true);
						else
							setNumber(number + digit, true);
					}
				});
				
				int colour = Color.argb(255, 127, 255, 127);
				button.getBackground().setColorFilter(colour, PorterDuff.Mode.MULTIPLY);
				if (Color.red(colour) < 127 && Color.green(colour) < 127 && Color.blue(colour) < 127)
					button.setTextColor(Color.WHITE);
				
				row.addView(button);
			}
			
			table.addView(row);
		}
		
		row = new TableRow(context);
		
		Button zeroButton = new Button(context);
		zeroButton.setText("0");
		zeroButton.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				String number = numberTextView.getText().toString();
				if (number == null)
					setNumber(0);
				else
					setNumber(number + '0', false);
			}
		});
		zeroButton.setLayoutParams(new TableRow.LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT, 1.0f));
		zeroButton.getBackground().setColorFilter(Color.argb(255, 127, 127, 255), PorterDuff.Mode.MULTIPLY);
		row.addView(zeroButton);
		
		Button okButton = new Button(context);
		okButton.setText("OK");
		okButton.setOnClickListener(new OnClickListener()
		{
			@Override
			public void onClick(View v)
			{
				node.text(numberTextView.getText().toString());
				view.uncover(NumericInputTable.this);
				view.layoutBubbles();
			}
		});
		okButton.getBackground().setColorFilter(Color.argb(255, 127, 127, 255), PorterDuff.Mode.MULTIPLY);
		row.addView(okButton);
		
		table.addView(row);
		
		for (int i = 0; i < COLUMNS; i++)
		{
			table.setColumnStretchable(i, true);
			table.setColumnShrinkable(i, true);
		}
		
		setNumber(node.text(), true);
		setupHexMode();
	}
	
	private void setupHexMode()
	{
		setNumber(numberTextView.getText().toString(), false);
	}
	
	private void setNumber(String number, boolean overrideMode)
	{
		if (number != null && number.length() > 0)
		{
			if (number.startsWith("0x") || number.startsWith("0X"))
			{
				if (number.length() > 2)
				{
					try
					{
						int num = Integer.parseInt(number.substring(2), 16);
						if (overrideMode)
							hexMode = true;
						setNumber(num);
						return;
					}
					catch (NumberFormatException nfe)
					{
						Log.e("ASM2", "Can't parse number: "+number, nfe);
					}
				}
			}
			else
			{
				try
				{
					int num = Integer.parseInt(number, 10);
					if (overrideMode)
						hexMode = false;
					setNumber(num);
					return;
				}
				catch (NumberFormatException nfe)
				{
					Log.e("ASM2", "Can't parse decimal: "+number, nfe);
				}
				
				try
				{
					int num = Integer.parseInt(number, 16);
					if (overrideMode)
						hexMode = true;
					setNumber(num);
					return;
				}
				catch (NumberFormatException nfe)
				{
					Log.e("ASM2", "Can't parse hexadecimal: "+number, nfe);
				}
			}
		}
		
		setNumber(0);
	}
	
	private void setNumber(int number)
	{
		if (number > 65535)
		{
			String current = numberTextView.getText().toString();
			if (current == null || current.length() == 0)
				setNumber(0);
			return;
		}
		
		Button on = hexMode ? hexButton : decButton, off = hexMode ? decButton : hexButton;
		
		on.getBackground().setColorFilter(Color.DKGRAY, PorterDuff.Mode.MULTIPLY);
		on.setTextColor(Color.WHITE);
		
		off.getBackground().setColorFilter(Color.argb(255, 255, 127, 127), PorterDuff.Mode.MULTIPLY);
		off.setTextColor(Color.BLACK);
		
		if (hexMode)
			numberTextView.setText(String.format("0x%x", number));
		else
			numberTextView.setText(""+number);
	}
}
