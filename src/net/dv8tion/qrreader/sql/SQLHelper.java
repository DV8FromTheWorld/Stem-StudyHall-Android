package net.dv8tion.qrreader.sql;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import android.os.AsyncTask;
import android.widget.TextView;

public abstract class SQLHelper extends AsyncTask<String, String, String>
{
	public static final String FAIL_MESSAGE_PREPEND = 	"Unable to register attendence due to the following reason:<br>";
	public static final String CONTACT_INFO = 			"Contact developer at:  Keenerga@email.appstate.edu";
	
	public static final String API_BASE_URL = 			"http://student.cs.appstate.edu/keenerga/studyhall/api/v1/";
	public static final String ATTEND_ENDPOINT = 		"attend.php";
	public static final String ADMIN_ENDPOINT = 		"admin.php";
	public static final String SESSION_CREATE_ENDPOINT = "session.php/create";
	public static final String SESSION_END_ENDPOINT = 	"session.php/end";
	
	public static final String POST = 					"POST";
	public static final String GET = 					"GET";
	
	public static final String SUCCESS =                "Success: (1)";	
	public static final String FAIL_SQL_CONNECT =       "Failure: (1)";
	
	public static final String SQL_UserName = 			"keenerga";
	public static final String SQL_Pass = 				"gak720";
	
	TextView output;
	String tag;
	
	public SQLHelper(TextView output, String tag)
	{
		this.output = output;
		this.tag = "[" + tag + "] ";
	}
	
	public static String emailCleanUp(String email)
	{
		if (email.contains("email.appstate"))
		{
			email = email.replace("email.", "");
		}
		return email;
	}
	
	protected void debugPrint(String message)
	{
		if (message != null)
		{
			output.setText(tag + message);
		}
		else
		{
			output.setText(tag + "The return/message was null.  Check PHP.");
		}
	}
	
	protected HttpURLConnection createConnection(final String endPoint, final String method) throws MalformedURLException, IOException
	{
		String url = API_BASE_URL + endPoint;
		HttpURLConnection conn = (HttpURLConnection) new URL(url).openConnection();
		conn.setDoOutput(true);
		conn.setDoInput(true);
		conn.setRequestProperty("Content-Type", "application/json; charset=utf8");
		conn.setRequestMethod(method);
		conn.setReadTimeout(100000);
		conn.connect();
		return conn;
	}
	
	protected String readInputStream(InputStream stream) throws IOException
	{
		StringBuilder sb = new StringBuilder();
	    BufferedReader br = new BufferedReader(new InputStreamReader(stream, "utf-8"));  
	    String line = null;
	    while ((line = br.readLine()) != null)
	    {  
	    	sb.append(line + "\n");  
	    }
	    br.close();
	    return sb.toString();
	}
	
	protected void writeToOutput(OutputStream wr, String message) throws UnsupportedEncodingException, IOException
	{
		wr.write(message.getBytes("UTF-8"));
		wr.flush();
		wr.close();
	}
	
	protected abstract String doInBackground(String... params);
	protected abstract void onPostExecute(String result);
}
