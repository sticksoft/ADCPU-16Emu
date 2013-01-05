package uk.co.sticksoft.adce.help;

import java.util.HashSet;
import java.util.Set;

import uk.co.sticksoft.adce.R;
import android.app.ListActivity;
import android.content.Intent;
import android.database.DataSetObserver;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class HelpActivity extends ListActivity implements ListAdapter
{
	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
	    super.onCreate(savedInstanceState);

	    setListAdapter(this);
	}
	
	public final static String[] helpEntries =
	{
		"Introduction",
		"What's Assembly Language?",
		"DASM Quick Reference",
		"I/O Quick Reference",
		"Readme",
		"Chat",
		"Email Developer",
		"Links",
	};
	
	private void showTextRes(int resID)
	{
		Intent intent = new Intent(this, TextActivity.class);
		intent.putExtra("text", resID);
		startActivity(intent);
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id)
	{
		super.onListItemClick(l, v, position, id);
		
		switch (position)
		{
			case 0:
				showTextRes(R.raw.help_introduction);
				break;
			case 1:
				showTextRes(R.raw.help_assembly);
				break;
			case 2:
				//showTextRes(R.raw.help_dasm_qr);
				showTextRes(R.raw.dcpu_1_7);
				break;
			case 3:
				showTextRes(R.raw.help_io_qr);
				break;
			case 4:
				showTextRes(R.raw.help_readme);
				break;
			case 5:
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.sticksoft.co.uk/android/adcpuchat.html")));
				break;
			case 6:
				startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("mailto:android@sticksoft.co.uk")));
				break;
			case 7:
				showTextRes(R.raw.help_links);
				break;
			default:
				break;
		}
	}
	

	@Override
	public int getCount()
	{
		return helpEntries.length;
	}

	@Override
	public Object getItem(int position)
	{
		return helpEntries[position];
	}

	@Override
	public long getItemId(int position)
	{
		return position;
	}

	@Override
	public int getItemViewType(int position)
	{
		return 0;
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent)
	{
		TextView tv = null;
		if (convertView != null && convertView instanceof TextView)
			tv = (TextView)convertView;
		else
		{
			tv = new TextView(this);
			tv.setTextSize(26);
			tv.setPadding(5, 10, 5, 10);
		}
		
		tv.setText(helpEntries[position]);
		
		return tv;
	}

	@Override
	public int getViewTypeCount()
	{
		return 1;
	}

	@Override
	public boolean hasStableIds()
	{
		return true;
	}

	@Override
	public boolean isEmpty()
	{
		return false;
	}

	Set<DataSetObserver> observers = new HashSet<DataSetObserver>();
	@Override
	public void registerDataSetObserver(DataSetObserver observer)
	{
		observers.add(observer);
	}

	@Override
	public void unregisterDataSetObserver(DataSetObserver observer)
	{
		observers.remove(observer);
	}

	@Override
	public boolean areAllItemsEnabled()
	{
		return true;
	}

	@Override
	public boolean isEnabled(int position)
	{
		return true;
	}
}
