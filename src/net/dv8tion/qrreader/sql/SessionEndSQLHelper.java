package net.dv8tion.qrreader.sql;

import java.net.HttpURLConnection;

import net.dv8tion.qrreader.MainActivity;

import org.json.JSONObject;

import android.text.Html;
import android.widget.TextView;

public class SessionEndSQLHelper extends SQLHelper
{

	public static final String FAIL_NO_UPDATE = 		"Failure: (2)";
	public static final String FAIL_MULITPLE_UPDATE = 	"Failure: (3)";
	
	private MainActivity main;
	
	public SessionEndSQLHelper(TextView output, MainActivity main)
	{
		super(output, "SessionEnd");
		this.main = main;
	}
	
	@Override
	protected String doInBackground(String... params) 
	{
		String s = "";
		try
		{
			HttpURLConnection conn = this.createConnection(SESSION_END_ENDPOINT, POST);
			
			JSONObject json = new JSONObject();
			json.put("username", SQL_UserName);
			json.put("password", SQL_Pass);
			json.put("email", emailCleanUp(params[0]));
			json.put("sessionId", params[1]);
			
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
		this.debugPrint(result);
		if (result != null)
		{
			if (result.contains(SUCCESS))
			{
				output.setText("Successfully ended session. A new session can now be started");
				main.updateSessionStatus(null);
			}
			else
			{
				if (result.contains(FAIL_SQL_CONNECT))
				{
					output.setText(
							Html.fromHtml(
									FAIL_MESSAGE_PREPEND 
									+ "<b>Unable to communicate with the SQL server.</b><br><br>" 
									+ "Please have developer check SQL auth systems.<br>"
									+ CONTACT_INFO)
							);
				}
				else if (result.contains(FAIL_NO_UPDATE))
				{
					output.setText(
							Html.fromHtml(
									FAIL_MESSAGE_PREPEND
									+ "<b>Successfully auth'd with SQL Server but unable to end current session.</b><br>"
									+ "<b>Does the provided session actually exist?</b><br><br>"
									+ "Please have developer check SQL update statement in " + SESSION_END_ENDPOINT + "<br>"
									+ CONTACT_INFO)
							);
				}
				else if (result.contains(FAIL_MULITPLE_UPDATE))
				{
					output.setText(
							Html.fromHtml(
									FAIL_MESSAGE_PREPEND
									+ "<b>Ended multiple sessions. This can only be caused by multiple session having the same ID.</b><br>"
									+ "<b>Perhaps the device glitched.</b><br><br>"
									+ "Please have developer check SQL update statement in " + SESSION_END_ENDPOINT + "<br>"
									+ CONTACT_INFO)
							);
				}
				main.updateSessionStatus(main.getCurrentSessionId());
			}
		}
	}
}
