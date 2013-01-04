package uk.co.sticksoft.adce;

import java.util.HashSet;

import uk.co.sticksoft.adce.Options.DCPU_VERSION;
import android.app.Activity;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;

public class OptionsActivity extends Activity
{
	private ListView list;
	
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		list = new ListView(this);
		list.setAdapter(rootListAdapter);
		setContentView(list);
		list.setOnItemClickListener(new OnItemClickListener()
		{
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3)
			{
				((OptionsAdapter)arg0.getAdapter()).onItemClick(arg0, arg1, arg2, arg3);
			}
		});

	}
	
	private abstract class OptionsAdapter implements ListAdapter, OnItemClickListener
	{
		@Override
		public Object getItem(int position) { return null; }
		
		@Override
		public long getItemId(int position) { return position; }
	
		@Override
		public int getItemViewType(int position) { return position; }
		
		@Override
		public int getViewTypeCount() { return getCount(); }
	
		@Override
		public boolean hasStableIds() { return true; }
	
		@Override
		public boolean isEmpty() { return false; }
	
		private HashSet<DataSetObserver> observers = new HashSet<DataSetObserver>();
		
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
		public boolean areAllItemsEnabled() { return true; }
	
		@Override
		public boolean isEnabled(int position) { return true; }
	}
	
	private OptionsAdapter rootListAdapter = new OptionsAdapter()
	{
		@Override
		public int getCount() { return 1; }
	
		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			if (convertView == null)
			{
				RelativeLayout rel = new RelativeLayout(OptionsActivity.this);
				
				switch (position)
				{
				case 0:
					TextView text = new TextView(OptionsActivity.this);
					LayoutParams lp1 = new LayoutParams(LayoutParams.FILL_PARENT, LayoutParams.WRAP_CONTENT);
					lp1.addRule(RelativeLayout.ALIGN_LEFT);
					text.setLayoutParams(lp1);
					text.setText("DCPU Version");
					text.setTextSize(32);
					rel.addView(text);
				}
				convertView = rel;
			}
			
			((TextView)((RelativeLayout)convertView).getChildAt(0)).setText("DCPU Version = "+ ((Options.GetDcpuVersion() == Options.DCPU_VERSION._1_1) ? "1.1" : "1.7"));
			
			return convertView;
		}
		
		public void onItemClick(android.widget.AdapterView<?> adapter, View view, int position, long id)
		{
			switch (position)
			{
			case 0:
				list.setAdapter(dcpuVersionAdapter);
			}
		};
		
	};
	
	private OptionsAdapter dcpuVersionAdapter = new OptionsAdapter()
	{
		@Override
		public View getView(int position, View convertView, ViewGroup parent)
		{
			TextView tv;
			if (convertView == null || !(convertView instanceof TextView))
			{
				tv = new TextView(OptionsActivity.this);
				tv.setTextSize(48);
			}
			else
				tv = (TextView)convertView;
			
			final String[] values = { "1.1", "1.7" };
			
			if (position < values.length)
				tv.setText(values[position]);
			else
				tv.setText("(index out of bounds");
			
			return tv;
		}
		
		@Override
		public int getCount()
		{
			return 2;
		}
		
		public void onItemClick(android.widget.AdapterView<?> adapter, View view, int position, long id)
		{
			switch (position)
			{
			case 0:
				Options.SetDcpuVersion(DCPU_VERSION._1_1);
				break;
			case 1:
				Options.SetDcpuVersion(DCPU_VERSION._1_7);
				break;
			}
			
			list.setAdapter(rootListAdapter);
		};
	};
	
}
