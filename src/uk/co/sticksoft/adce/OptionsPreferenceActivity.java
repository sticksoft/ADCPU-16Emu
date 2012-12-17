package uk.co.sticksoft.adce;

import uk.co.sticksoft.adce.Options.DCPU_VERSION;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.widget.Toast;

public class OptionsPreferenceActivity extends PreferenceActivity
{
	private void debugLog(final String msg)
	{
		runOnUiThread(new Runnable()
		{
			
			@Override
			public void run()
			{
				Toast.makeText(OptionsPreferenceActivity.this, msg, Toast.LENGTH_SHORT).show();
			}
		});
	}
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		try
		{
			updatePreferences();
		}
		catch (Exception ex)
		{
		}
	}
	
	private void updatePreferences()
	{
		PreferenceScreen root = getPreferenceManager().createPreferenceScreen(this);
		
		root.addPreference(makeDcpuVersionPreference());
		root.addPreference(makeTextEditorShownPreference());
		root.addPreference(makeVisualEditorShownPreference());
		root.addPreference(makeM35fdShownPreference());
		
		setPreferenceScreen(root);
	}
	
	private ListPreference makeDcpuVersionPreference()
	{
		final ListPreference dcpuVersion = new ListPreference(this);
		
		String[] versions = { "1.1", "1.7" };
		dcpuVersion.setEntries(versions);
		dcpuVersion.setEntryValues(versions);
		dcpuVersion.setOnPreferenceChangeListener(new OnPreferenceChangeListener()
		{
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue)
			{
				debugLog("Setting changed to "+newValue);
				
				if ("1.7".equals(newValue))
					Options.SetDcpuVersion(DCPU_VERSION._1_7);
				else
					Options.SetDcpuVersion(DCPU_VERSION._1_1);
				
				dcpuVersion.setSummary(newValue.toString());

				return true;
			}
		});
		
		if (Options.GetDcpuVersion() == Options.DCPU_VERSION._1_7)
			dcpuVersion.setValue(versions[1]);
		else
			dcpuVersion.setValue(versions[0]);
		dcpuVersion.setTitle("ADCPU version");
		dcpuVersion.setDialogTitle("ADCPU version");
		dcpuVersion.setSummary(dcpuVersion.getValue());
		
		return dcpuVersion;
	}
	
	private CheckBoxPreference makeCheckboxPreference(String title, boolean checked, OnPreferenceChangeListener listener)
	{
		final CheckBoxPreference checkbox = new CheckBoxPreference(this);
		checkbox.setTitle(title);
		checkbox.setChecked(checked);
		checkbox.setOnPreferenceChangeListener(listener);
		return checkbox;
	}
	
	private CheckBoxPreference makeTextEditorShownPreference()
	{
		return makeCheckboxPreference("Show Text Editor", Options.IsTextEditorShown(), new OnPreferenceChangeListener()
		{
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue)
			{
				Options.SetTextEditorShown(Boolean.TRUE.equals(newValue));
				return true;
			}
		});
	}
	
	private CheckBoxPreference makeVisualEditorShownPreference()
	{
		return makeCheckboxPreference("Show Visual Editor", Options.IsVisualEditorShown(), new OnPreferenceChangeListener()
		{
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue)
			{
				Options.SetVisualEditorShown(Boolean.TRUE.equals(newValue));
				return true;
			}
		});
	}
	
	private CheckBoxPreference makeM35fdShownPreference()
	{
		return makeCheckboxPreference("Show M35FD Drive Controls", Options.IsM35fdShown(), new OnPreferenceChangeListener()
		{
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue)
			{
				Options.SetM35fdShown(Boolean.TRUE.equals(newValue));
				return true;
			}
		});
	}
}
