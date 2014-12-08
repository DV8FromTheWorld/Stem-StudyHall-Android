package net.dv8tion.qrreader;

import java.util.ArrayList;
import java.util.List;

import net.dv8tion.qrreader.sql.AdminSQLHelper;
import net.dv8tion.qrreader.sql.AttendSQLHelper;
import net.dv8tion.qrreader.sql.SQLHelper;
import net.dv8tion.qrreader.sql.SessionCreateSQLHelper;
import net.dv8tion.qrreader.sql.SessionEndSQLHelper;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.SignInButton;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

/**
 * A login screen that offers login via email/password and via Google+ sign in.
 * <p/>
 * ************ IMPORTANT SETUP NOTES: ************ In order for Google+ sign in
 * to work with your app, you must first go to:
 * https://developers.google.com/+/mobile
 * /android/getting-started#step_1_enable_the_google_api and follow the steps in
 * "Step 1" to create an OAuth 2.0 client for your package.
 */
public class MainActivity extends PlusBaseActivity implements LoaderCallbacks<Cursor> 
{	
	public static final String SESSION_URL = "http://cs.appstate.edu/step/studygroups/SessionID=";
	
	// UI references.
	private AutoCompleteTextView mEmailView;
	private View mProgressView;
	private View mEmailLoginFormView;
	private SignInButton mPlusSignInButton;
	private View mSignOutButtons;
	private View mLoginFormView;
	
	private TextView mLoginMessage;
	private TextView scanMessage;
	private Button scanButton;
	
	private TextView adminMessage;
	private Button adminSessionCreate;
	private Button adminSessionQRCode;
	private Button adminSessionEnd;
	
	
	private String email;
	private String currentSessionId;
	private boolean messageLock;
	
	private DialogInterface.OnClickListener dialogClickListener;
	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Find the Google+ sign in button.
		mPlusSignInButton = (SignInButton) findViewById(R.id.plus_sign_in_button);
		if (supportsGooglePlayServices()) {
			// Set a listener to connect the user when the G+ button is clicked.
			mPlusSignInButton.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View view) {
					signIn();
				}
			});
		} else {
			// Don't offer G+ sign in if the app's version is too low to support
			// Google Play
			// Services.
			mPlusSignInButton.setVisibility(View.GONE);
			return;
		}
		
		dialogClickListener = new DialogInterface.OnClickListener() {
	        @Override
	        public void onClick(DialogInterface dialog, int which) {
	            switch (which){
	            case DialogInterface.BUTTON_POSITIVE:
	            	adminSessionEnd.setEnabled(false);
	            	adminSessionQRCode.setEnabled(false);
	    			SessionEndSQLHelper se = new SessionEndSQLHelper(scanMessage, MainActivity.this);
	    			se.execute(email, currentSessionId);
	                break;
	            }
	        }
	    };

		mLoginFormView = findViewById(R.id.login_form);
		mProgressView = findViewById(R.id.login_progress);
		mEmailLoginFormView = findViewById(R.id.email_login_form);
		mSignOutButtons = findViewById(R.id.plus_sign_out_buttons);
		
		mLoginMessage = (TextView) findViewById(R.id.login_text_message);
		scanMessage = (TextView) findViewById(R.id.scan_result_text);
		scanButton = (Button) findViewById(R.id.scan_button);
		
		adminMessage = (TextView) findViewById(R.id.admin_message);
		adminSessionCreate = (Button) findViewById(R.id.admin_session_create);
		adminSessionQRCode = (Button) findViewById(R.id.admin_session_qrcode);
		adminSessionEnd = (Button) findViewById(R.id.admin_session_end);
		
		scanButton.setEnabled(false);
		updateAdminStatus(false, null);
	}
	
	/**
     * ***************************************************************************************************************************
	 * Start Austin Keener *******************************************************************************************************
	 * ***************************************************************************************************************************
	 */
	
	//https://github.com/zxing/zxing/wiki/Scanning-Via-Intent
	@Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent)
    {
    	IntentResult scanResult = IntentIntegrator.parseActivityResult(requestCode, resultCode, intent);
    	if (scanResult != null && scanResult.getContents() != null)
    	{
    		//Http://cs.appstate.edu/step/studygroups/SessionID=########
    		//Where # can be 0-9 or a-z or A-Z
    		String url = scanResult.getContents();
    		System.out.println(url);
    		Log.w("[URL]", url);
    		if (url.contains(SESSION_URL))
    		{
    			String session = url.substring(url.indexOf("SessionID=") + "SessionID=".length());	//Used for Student session login.
    			System.out.println(session);
    			{
    			AttendSQLHelper sql = new AttendSQLHelper(scanMessage);
    			lockMessage();
    			sql.execute(session, email);
    			
    			//Display info about group
    				//Will need to pull info either from hardcoded info (pls no)
    				//Or from webpage (prefered)
    			//Button to login, start tracking logged in time?
    				//Login using PhoneNumber and SessionID.
    					//Check that Study Hall leader has logged in first.
    					//If this is the study hall leader, let him start a new session.
    					//Otherwise fail.
    				//Start timer (if needed).
    			//Log out.
    				//Students should log out before leader.
    				//Leader logout == close session.
    			}
    		}
    	}
    	else
    	{
    		super.onActivityResult(requestCode, resultCode, intent);
    	}
    }
    
    //https://github.com/zxing/zxing/wiki/Scanning-Via-Intent
    public void onClick(View v)
    {
    	switch (v.getId())
    	{
    		case R.id.scan_button:
    			IntentIntegrator ii_scan = new IntentIntegrator(this); 
    			ii_scan.initiateScan(IntentIntegrator.QR_CODE_TYPES);
    			break;
    		case R.id.admin_session_create:
    			adminSessionCreate.setEnabled(false);
    			SessionCreateSQLHelper sc = new SessionCreateSQLHelper(scanMessage, this);
    			sc.execute(email);
    			break;
    		case R.id.admin_session_qrcode:
    			IntentIntegrator ii_qr = new IntentIntegrator(this);
    			ii_qr.shareText(SESSION_URL + currentSessionId);
    			break;
    		case R.id.admin_session_end:
    			this.endSessionDialog();
    			break;
    	}
    }
        
    @Override
	protected void updateConnectButtonState()
    {
		boolean connected = getPlusClient().isConnected();

		mSignOutButtons.setVisibility(connected ? View.VISIBLE : View.GONE);
		mPlusSignInButton.setVisibility(connected ? View.GONE : View.VISIBLE);
		mEmailLoginFormView.setVisibility(connected ? View.GONE : View.VISIBLE);
		
		if (!connected)
		{
			updateAdminStatus(false, null);
		}
		
		if (connected)
		{
			String email = SQLHelper.emailCleanUp(this.getPlusClient().getAccountName());
			if (email.contains("appstate.edu"))
			{
				mLoginMessage.setText("Currently logged in as: " + email);
				scanButton.setEnabled(true);
				tryMessage(R.string.scan_logged_in);
			}
			else
			{
				mLoginMessage.setText(R.string.login_non_appstate_email);
				scanButton.setEnabled(false);
				scanMessage.setText(R.string.scan_not_logged_in);
			}
		}
		else
		{
			mLoginMessage.setText(R.string.login_use_appstate_email);
			scanButton.setEnabled(false);
			scanMessage.setText(R.string.scan_not_logged_in);
		}
	}
    
    public void updateAdminStatus(boolean admin, String sessionId)
    {
    	
    	adminMessage.setVisibility(admin ? View.VISIBLE : View.GONE);    	
		adminSessionCreate.setVisibility(admin ? View.VISIBLE : View.GONE);	
		adminSessionQRCode.setVisibility(admin ? View.VISIBLE : View.GONE);
		adminSessionEnd.setVisibility(admin ? View.VISIBLE : View.GONE);
		updateSessionStatus(sessionId);
    }
    
    public void updateSessionStatus(String sessionId)
    {
    	boolean sessionActive = sessionId != null;
		currentSessionId = sessionId;
    	adminSessionCreate.setEnabled(!sessionActive);
		adminSessionQRCode.setEnabled(sessionActive);
    	adminSessionEnd.setEnabled(sessionActive);
    	
    }
    
    public String getCurrentSessionId()
    {
    	return currentSessionId;
    }
    
    private void endSessionDialog()
    {
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	
    	builder.setMessage("Are you sure you want to end this session?\nThe session cannot be reopened after it has been ended.")
    		.setPositiveButton("Yes", dialogClickListener)
    	    .setNegativeButton("No",  dialogClickListener)
    	    .show();
    }
    
    private void tryMessage(int messageId)
    {
    	if (messageLock)
    	{
    		messageLock = false;
    		return;
    	}
    	scanMessage.setText(messageId);
    }
    
    private void lockMessage()
    {
    	messageLock = true;
    }   
    
	
    /**
     * ***************************************************************************************************************************
	 * Stop Austin Keener ********************************************************************************************************
	 * ***************************************************************************************************************************
	 */

	/**
	 * Shows the progress UI and hides the login form.
	 */
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	public void showProgress(final boolean show) {
		// On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
		// for very easy animations. If available, use these APIs to fade-in
		// the progress spinner.
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
			int shortAnimTime = getResources().getInteger(
					android.R.integer.config_shortAnimTime);

			mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
			mLoginFormView.animate().setDuration(shortAnimTime)
					.alpha(show ? 0 : 1)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mLoginFormView.setVisibility(show ? View.GONE
									: View.VISIBLE);
						}
					});

			mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
			mProgressView.animate().setDuration(shortAnimTime)
					.alpha(show ? 1 : 0)
					.setListener(new AnimatorListenerAdapter() {
						@Override
						public void onAnimationEnd(Animator animation) {
							mProgressView.setVisibility(show ? View.VISIBLE
									: View.GONE);
						}
					});
		} else {
			// The ViewPropertyAnimator APIs are not available, so simply show
			// and hide the relevant UI components.
			mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
			mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
		}
	}

	@Override
	protected void onPlusClientSignIn() {
		email = this.getPlusClient().getAccountName();
		AdminSQLHelper a = new AdminSQLHelper(this, scanMessage);
		a.execute(email);
		
		// Set up sign out and disconnect buttons.
		Button signOutButton = (Button) findViewById(R.id.plus_sign_out_button);
		signOutButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				signOut();
			}
		});
		Button disconnectButton = (Button) findViewById(R.id.plus_disconnect_button);
		disconnectButton.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View view) {
				revokeAccess();
			}
		});
	}

	@Override
	protected void onPlusClientBlockingUI(boolean show) {
		showProgress(show);
	}

	@Override
	protected void onPlusClientRevokeAccess() {
		// TODO: Access to the user's G+ account has been revoked. Per the
		// developer terms, delete
		// any stored user data here.
	}

	@Override
	protected void onPlusClientSignOut() {
		updateAdminStatus(false, null);
	}

	/**
	 * Check if the device supports Google Play Services. It's best practice to
	 * check first rather than handling this as an error case.
	 * 
	 * @return whether the device supports Google Play Services
	 */
	private boolean supportsGooglePlayServices() {
		return GooglePlayServicesUtil.isGooglePlayServicesAvailable(this) == ConnectionResult.SUCCESS;
	}

	@Override
	public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
		return new CursorLoader(this,
				// Retrieve data rows for the device user's 'profile' contact.
				Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
						ContactsContract.Contacts.Data.CONTENT_DIRECTORY),
				ProfileQuery.PROJECTION,

				// Select only email addresses.
				ContactsContract.Contacts.Data.MIMETYPE + " = ?",
				new String[] { ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE },

				// Show primary email addresses first. Note that there won't be
				// a primary email address if the user hasn't specified one.
				ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
	}

	@Override
	public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
		List<String> emails = new ArrayList<String>();
		cursor.moveToFirst();
		while (!cursor.isAfterLast()) {
			emails.add(cursor.getString(ProfileQuery.ADDRESS));
			cursor.moveToNext();
		}

		addEmailsToAutoComplete(emails);
	}

	@Override
	public void onLoaderReset(Loader<Cursor> cursorLoader) {

	}

	private interface ProfileQuery {
		String[] PROJECTION = { ContactsContract.CommonDataKinds.Email.ADDRESS,
				ContactsContract.CommonDataKinds.Email.IS_PRIMARY, };

		int ADDRESS = 0;
		@SuppressWarnings("unused")
		int IS_PRIMARY = 1;
	}

	private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
		// Create adapter to tell the AutoCompleteTextView what to show in its
		// dropdown list.
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(
				MainActivity.this,
				android.R.layout.simple_dropdown_item_1line,
				emailAddressCollection);

		mEmailView.setAdapter(adapter);
	}

}
