package net.dv8tion.qrreader.sql;

import java.net.HttpURLConnection;

import net.dv8tion.qrreader.MainActivity;

import org.json.JSONObject;

import android.widget.TextView;

public class SessionCreateSQLHelper extends SQLHelper
{
	public static final String SESSION_ID_INDEX = "session_id\":\"";
	
	private MainActivity main;
	private String email;
	
	public SessionCreateSQLHelper(TextView output, MainActivity main)
	{
		super(output, "SessionCreate");
		this.main = main;
	}
	@Override
	protected String doInBackground(String... params) 
	{
		String s = "";
		try
		{
			HttpURLConnection conn = this.createConnection(SESSION_CREATE_ENDPOINT, POST);
			
			JSONObject json = new JSONObject();
			json.put("username", SQL_UserName);
			json.put("password", SQL_Pass);
			json.put("email", emailCleanUp(params[0]));
			
			email = emailCleanUp(params[0]);
			this.writeToOutput(conn.getOutputStream(), json.toString());				//DEBUG
			
			return this.readInputStream(conn.getInputStream());
		}
		catch (Exception e)
		{
			s += e.getMessage() + "\n";
			s += e.getClass().getName() + "\n";
		}
		s += "Failed apparently \n";
		publishProgress(s);
		return null;
	}

	@Override
	protected void onPostExecute(String result)
	{
		//this.debugPrint(result);
		if (result != null)
		{
			if (result.contains(SUCCESS))
			{
				int index = result.indexOf(SESSION_ID_INDEX) + SESSION_ID_INDEX.length();
				String sessionId = result.substring(index, index + 10);
				main.updateSessionStatus(sessionId);
				
				//Logs the studyhall leader into the new session.
				AttendSQLHelper ah = new AttendSQLHelper(output);
				ah.execute(sessionId, email);
			}
			else
			{
				main.updateSessionStatus(null);
				output.setText("Failed to create session.  JSON returned:\n" + result);
						
			}
		}
	}

}
