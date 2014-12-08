package net.dv8tion.qrreader.sql;

import java.net.HttpURLConnection;

import net.dv8tion.qrreader.MainActivity;

import org.json.JSONObject;

import android.widget.TextView;

public class AdminSQLHelper extends SQLHelper
{
	
	public static final String SESSION_ACTIVE = "session_status\":\"active\"";
	public static final String SESSION_ID_INDEX = "session_id\":\"";
	
	private MainActivity main;
	
	public AdminSQLHelper(MainActivity main, TextView output)
	{
		super(output, "Admin");
		this.main = main;		
	}

	@Override
	protected String doInBackground(String... params)
	{
		String s = "";
		try
		{
			HttpURLConnection conn = this.createConnection(ADMIN_ENDPOINT, POST);
			
			JSONObject json = new JSONObject();
			json.put("username", SQL_UserName);
			json.put("password", SQL_Pass);
			json.put("email", emailCleanUp(params[0]));

			this.writeToOutput(conn.getOutputStream(),json.toString());
			
			return this.readInputStream(conn.getInputStream());
		}
		catch (Exception e)
		{
			s += e.getMessage()+ "\n";
			s += e.getClass().getName()+ "\n";
		}
		s += "Failed apparently \n";
		publishProgress(s);
		return null;
	}

	/**
	 * Format of json:
	 * 	{
	 * 		"status":"<success or fail message>",
	 * 		"admin","<true or false>",
	 * 		"session_status":"<active or inactive",
	 * 		"session_id":"<session id>"
	 * 	}
	 */
	@Override
	protected void onPostExecute(String result) 
	{
		//Used for debug to check the return of the Admin.php endpoint.
		//this.debugPrint(result);
		
		if (result != null && result.contains(SUCCESS) && result.contains("true"))
		{				
			if (result.contains(SESSION_ACTIVE))
			{
				int index = result.indexOf(SESSION_ID_INDEX) + SESSION_ID_INDEX.length();
				String sessionId = result.substring(index, index + 10);
//					this.debugPrint(sessionId);
				main.updateAdminStatus(true, sessionId);
			}
			else
			{
				main.updateAdminStatus(true, null);
			}
			
		}
		else
		{
			main.updateAdminStatus(false, null);
		}
	}
	
}
