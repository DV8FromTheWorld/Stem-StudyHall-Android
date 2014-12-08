package net.dv8tion.qrreader.sql;

import java.net.HttpURLConnection;

import org.json.JSONObject;

import android.text.Html;
import android.widget.TextView;

public class AttendSQLHelper extends SQLHelper
{
	public static final String FAIL_SESSION =           "Failure: (2)";
	public static final String FAIL_SQL_INSERT =        "Failure: (3)";
	public static final String FAIL_ALREADY_ATTENDED =  "Failure: (4)";
	
	public AttendSQLHelper(TextView output)
	{
		super(output, "Attend");
	}
	
	@Override
	protected String doInBackground(String... params)
	{
		String s = "";
		try
		{
			HttpURLConnection conn = this.createConnection(ATTEND_ENDPOINT, POST);
			
			JSONObject json = new JSONObject();
			json.put("username", SQL_UserName);
			json.put("password", SQL_Pass);
			json.put("sessionId", params[0]);
			json.put("email", emailCleanUp(params[1]));
			
			this.writeToOutput(conn.getOutputStream(), json.toString());
			
			return this.readInputStream(conn.getInputStream());
		}
		catch (Exception e)
		{
			s += e.getMessage()+ "\n";
			s += e.getClass().getName()+ "\n";
		}
		s += "Failed apparently \n";
		publishProgress(s);						//DEBUG
		return null;
	}
	
	@Override
	protected void onPostExecute(String result)
	{		
//		this.debugPrint(output, result, "AttendSQL");
		if (result != null)
		{
			System.out.println(result);				
			
			if (result.contains(SUCCESS))
			{
				output.setText("Successfully registered attendence for this Study Session");
			}
			else if (result.contains(FAIL_SQL_CONNECT))
			{
				output.setText(
						Html.fromHtml(
								FAIL_MESSAGE_PREPEND 
								+ "<b>Unable to communicate with the SQL server.</b><br><br>" 
								+ "Please have developer check SQL auth systems.<br>"
								+ CONTACT_INFO)
						);
			}
			else if (result.contains(FAIL_SQL_INSERT))
			{
				output.setText(
						Html.fromHtml(
								FAIL_MESSAGE_PREPEND
								+ "<b>Successfully auth'd with SQL Server but unable to insert into Attend Table.</b><br><br>"
								+ "Please have developer check SQL insert statement in " + ATTEND_ENDPOINT + "<br>"
								+ CONTACT_INFO)
						);
			}
			else if (result.contains(FAIL_SESSION))
			{
				output.setText(
						Html.fromHtml(
								FAIL_MESSAGE_PREPEND
							+ "<b>The Session has already been closed by a studyhall leader or does not exist.</b><br><br>"
							+ "If you believe this to be a mistake, rescan the QR code.  If it does not fix it, contact developer.<br>"
							+ CONTACT_INFO)
						);
			}
			else if (result.contains(FAIL_ALREADY_ATTENDED))
			{
				output.setText(
						Html.fromHtml(
								FAIL_MESSAGE_PREPEND
								+ "<b>You have already registered attendence for this study session.</b><br><br>"
								+ "Make sure the QR code you are scanning is for the current study hall session.<br>"
								+ "If you believe there is an error here, please contact the developer.<br>"
								+ CONTACT_INFO)
						);
			}
			else
			{
				output.setText(
						Html.fromHtml(
								FAIL_MESSAGE_PREPEND
								+ "<b>An unknown error has occured. The follow is the return from the SQL server:</b><br>"
								+ "Result:  " + result + "<br><br>"
								+ "Please contact the developer about this issue.<br>"
								+ CONTACT_INFO)
						);
			}
		}				
		else
		{
			output.setText(
					Html.fromHtml(
							FAIL_MESSAGE_PREPEND
							+ "<b>An unknown error has occured. The SQL Server returned null.</b><br><br>"
							+ "Please contact the developer about this issue.<br>"
							+ CONTACT_INFO)
					);
		}
	}
	
	@Override
	protected void onProgressUpdate(String... progress) {
		//output.setText("Timer: " + progress[0].toString());
     }
	
	
    
    
}
