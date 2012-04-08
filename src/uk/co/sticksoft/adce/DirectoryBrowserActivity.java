package uk.co.sticksoft.adce;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

public class DirectoryBrowserActivity extends ListActivity implements ListAdapter
{
	ArrayList<File> files = new ArrayList<File>();
	ArrayList<String> names = new ArrayList<String>();
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
	    super.onCreate(savedInstanceState);
	    
	    String path = getIntent().getStringExtra("path");
	    navigateTo(path);
	}
	
	private File currentDirectory;
	
	private void navigateTo(String path)
	{
		files.clear();
		names.clear();
		
		if (path == null)
	    	path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separatorChar + "ADCPU";
	    
		currentDirectory = new File(path);
	    if (!currentDirectory.exists())
	    	currentDirectory.mkdir();
	    
	    File parent = currentDirectory.getParentFile();
	    if (parent != null)
	    	addFile(parent, "..");
	    
	    File[] files = currentDirectory.listFiles();
	    for (int i = 0; i < files.length; i++)
	    	addFile(files[i], files[i].getName());
	    
	    addFile(null, "New File...");
	    addFile(null, "New Directory...");
	    
	    setListAdapter(this);
	    
	    for (DataSetObserver dso : observers)
	    	dso.onChanged();
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id)
	{
		super.onListItemClick(l, v, position, id);
		if (position < files.size() - 2)
		{
			File f = files.get(position);
			if (f.isDirectory())
				navigateTo(f.getAbsolutePath());
			else
			{
				Intent data = new Intent();
				data.putExtra("path", f.getAbsolutePath());
				setResult(RESULT_OK, data);
				finish();
			}
		}
		else if (position == files.size() - 2)
			makeNew(true);
		else
			makeNew(false);
	}
	
	private void makeNew(final boolean isFile)
	{
		final EditText txt = new EditText(this);
		new AlertDialog.Builder(this).setTitle(isFile ? "New File" : "New Directory").setView(txt).setNegativeButton("Cancel", null).setPositiveButton("OK", new OnClickListener()
		{
			@Override
			public void onClick(DialogInterface dialog, int which)
			{
				String name = txt.getText().toString();
				if (name != null && name.length() > 0)
				{
					File f = new File(currentDirectory, name);
					if (!f.exists())
					{
						try
						{
							if (isFile)
								f.createNewFile();
							else
								f.mkdir();
							navigateTo(currentDirectory.getAbsolutePath());
						}
						catch (IOException e)
						{
							e.printStackTrace();
						}
					}
				}
			}
		}).show();
	}
	
	private void addFile(File f, String name)
	{
		if (name.endsWith("~"))
			return;
		
		files.add(f);
		if (f != null && f.isDirectory())
			names.add("/"+ name);
		else
			names.add(name);
	}

	@Override
	public int getCount()
	{
		return names.size();
	}

	@Override
	public Object getItem(int position)
	{
		return files.get(position);
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
		if (convertView == null || !(convertView instanceof TextView))
		{
			convertView = new TextView(this);
			//LayoutParams lp = new LayoutParams(LayoutParams.FILL_PARENT, 25);
			//convertView.setLayoutParams(lp);
		}
		
		((TextView)convertView).setTextSize(24);
		((TextView)convertView).setText(names.get(position));
		
		return convertView;
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
		return names.isEmpty();
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
