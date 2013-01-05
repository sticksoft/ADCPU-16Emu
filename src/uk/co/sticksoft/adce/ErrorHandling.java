package uk.co.sticksoft.adce;
import android.app.AlertDialog;
import android.content.Context;

public class ErrorHandling
{
    public static Context context;
	public static void handle(Exception e)
	{
		handle(null, e);
	}
    public static void handle(Context c, final Throwable e)
	{
		context = MainActivity.me;
		
		MainActivity.me.runOnUiThread(new Runnable()
		{
			
			@Override
			public void run()
			{
				StringBuilder builder = new StringBuilder();
				
				//builder.append(e.getMessage()).append(" at\n");
				for (StackTraceElement el : e.getStackTrace())
				    builder.append(el.toString()).append("\n");
				new AlertDialog.Builder(context).setTitle("Exception").setMessage(builder).show();
		
				//new AlertDialog.Builder(context).setTitle("Exception").setMessage("doh").setPositiveButton("OK", null).show();
			}
		});
	}
    
    
}
