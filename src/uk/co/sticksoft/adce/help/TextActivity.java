package uk.co.sticksoft.adce.help;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.nio.charset.Charset;

import android.app.Activity;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.text.util.Linkify;
import android.widget.FrameLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public class TextActivity extends Activity
{
	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		
		FrameLayout lyt = new FrameLayout(this);
		
		ScrollView scroll = new ScrollView(this);
		lyt.addView(scroll);
		
		TextView text = new TextView(this);
		scroll.addView(text);
		
		int textResID = getIntent().getIntExtra("text", 0);
		
		if (textResID == 0)
		{
			finish();
			return;
		}
		
		try
		{
			InputStream is = getResources().openRawResource(textResID);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			
			byte[] buffer = new byte[1024];
			int read = -1;
			while ((read = is.read(buffer)) > 0)
				baos.write(buffer, 0, read);
			
			String loadedText = new String(baos.toByteArray(), Charset.defaultCharset().name());
		
			text.setText(Html.fromHtml(loadedText));
			text.setMovementMethod(LinkMovementMethod.getInstance());
			text.setTextSize(20);
		}
		catch (Exception ex)
		{
			finish();
			return;
		}
		
		setContentView(lyt);
	}
}
