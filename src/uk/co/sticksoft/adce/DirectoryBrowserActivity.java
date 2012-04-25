package uk.co.sticksoft.adce;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
	private ArrayList<File> files = new ArrayList<File>();
	private ArrayList<String> names = new ArrayList<String>();
	private boolean saving = false; 
	
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
	    super.onCreate(savedInstanceState);
	    
	    String path = getIntent().getStringExtra("path");
	    saving = getIntent().getBooleanExtra("saving", false);
	    navigateTo(path);
	}
	
	private File currentDirectory;
	
	private void navigateTo(String path)
	{
		try
		{
			files.clear();
			names.clear();
			
			boolean nullpath;
			if (nullpath = (path == null))
		    	path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separatorChar + "ADCPU";
		    
			currentDirectory = new File(path);
		    if (!currentDirectory.exists())
		    	currentDirectory.mkdir();
		    
		    // Bit of a hack here - copy default files if not found
		    if (nullpath)
		    	copyDefaultAsmFiles();
		    
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
		catch (Exception ex)
		{
			finish();
		}
	}
	
	private void copyDefaultAsmFiles()
	{
		try
    	{
    		File[] files = currentDirectory.listFiles();
    		
    		int[] ids = { R.raw.pong, R.raw.move, R.raw.radar, R.raw.keyboard };
    		String[] filenames = { "pong.dasm", "move.dasm", "radar.dasm", "keyboard.dasm" };
    		
    		for (int i = 0; i < filenames.length; i++)
    		{
    			boolean found = false;
    			for (int j = 0; j < files.length; j++)
    			{
    				if (files[j].getName().equals(filenames[i]))
    				{
    					found = true;
    					break;
    				}
    			}
    			if (!found)
    			{
    				// Based on http://stackoverflow.com/questions/8664468/copying-raw-file-into-sdcard
    				File f = new File(currentDirectory, filenames[i]);
    				FileOutputStream fos = new FileOutputStream(f);
    				InputStream is = getResources().openRawResource(ids[i]);
    				byte[] buffer = new byte[1024];
    				int read = 0;
    				
    				try
    				{
	    				while ((read = is.read(buffer)) > 0)
	    				{
	    					fos.write(buffer, 0, read);
	    				}
    				}
    				finally
    				{
    					fos.close();
    					is.close();
    				}
    				
    			}
    		}
    	}
    	catch (Exception ex)
    	{
    		
    	}
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id)
	{
		super.onListItemClick(l, v, position, id);
		if (position < files.size() - 2)
		{
			final File f = files.get(position);
			if (f.isDirectory())
				navigateTo(f.getAbsolutePath());
			else
			{
				if (saving && f.length() > 5) // Why 5?  Well, if your code is that short you can retype it :P (and it takes care of off-by-one errors)
				{
					// Show confirm dialog
					new AlertDialog.Builder(this).setTitle("Overwrite?").setPositiveButton("Yes", new OnClickListener()
					{
						@Override
						public void onClick(DialogInterface dialog, int which)
						{
							Intent data = new Intent();
							data.putExtra("path", f.getAbsolutePath());
							setResult(RESULT_OK, data);
							finish();
						}
					}).setNegativeButton("No", null).show();
				}
				else
				{
					Intent data = new Intent();
					data.putExtra("path", f.getAbsolutePath());
					setResult(RESULT_OK, data);
					finish();
				}
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
