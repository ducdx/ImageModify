package com.framgia.takasukamera;

import java.io.ObjectOutputStream;
import java.io.OutputStream;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.RequestToken;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.takasukamera.R;
import com.framgia.takasukamera.constant.AppConstant;
import com.framgia.takasukamera.social.FacebookUtils;
import com.framgia.takasukamera.social.FbLoginChangeListener;
import com.framgia.takasukamera.social.TwitterUtils;

public class CommentActivity extends Activity implements OnClickListener,FbLoginChangeListener{
	private ImageButton btnBack;
	private ImageButton btnShareComment;
	private ImageView imageShare;
	private EditText editComment;
	private ProgressDialog mProgressDialog;
	private SharedPreferences mSharedPreferences;
	private int shareOption = AppConstant.TO_FACEBOOK;
	private String message;
	
	/** Variable of CallBack. */
	private String urlCallBack = "takasukamera://twitter";
	
	@Override
	protected void onNewIntent(Intent intent) {
		Uri uri = intent.getData();
		boolean result = TwitterUtils.handleOAuthCallback(this, uri,
				urlCallBack);
		if (result) {
			Toast.makeText(this, getString(R.string.login_fb_success),
					Toast.LENGTH_LONG).show();
		} else {
			Toast.makeText(this, getString(R.string.login_fb_failure),
					Toast.LENGTH_LONG).show();
			finish();
		}
		super.onNewIntent(intent);
	}

	private Bundle bundle;
	
	/** Content post to wall */
	private String postContent = "";
	String imgPath;
	
	private Bitmap bmpShare;	
	private boolean isResumeWhenPressBack = false;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.comment_activity);
		
		bundle = getIntent().getExtras();
		if(bundle == null){
			return;
		}
		
		imgPath = bundle.getString(AppConstant.IMAGE_PATH);
		bmpShare = BitmapFactory.decodeFile(imgPath);
		
		shareOption = bundle.getInt("app_pos");
		mSharedPreferences = getApplicationContext().getSharedPreferences(
				"kameraAppSharedPreferences", MODE_PRIVATE);
		mProgressDialog = new ProgressDialog(this);
		// get option to share via twitter or facebook
		shareOption = getIntent().getExtras().getInt(AppConstant.SHARE_REQUEST);
		
		if (FacebookUtils.isAuthenticated(this)) {
			// Process logout facebook.
			FacebookUtils.logOut(CommentActivity.this);
		}

		// Init View
		initView();
	}

	/**
	 * Initialize View components
	 */
	private void initView() {
		btnBack = (ImageButton) findViewById(R.id.button_back);
		btnShareComment = (ImageButton) findViewById(R.id.button_sharecomment);
		imageShare = (ImageView) findViewById(R.id.image_share);
		editComment = (EditText) findViewById(R.id.edit_comment);
		
		
		if(shareOption == AppConstant.TO_FACEBOOK){
			postContent = "\n" + getString(R.string.string_default_facebook);
		}else{
			postContent = "\n" + getString(R.string.string_default_twitter);
		}
		
		editComment.setText(postContent);
		
		BitmapFactory.Options option = new BitmapFactory.Options();
		option.inJustDecodeBounds = false;
		option.inSampleSize = 8;
		Bitmap bmp = BitmapFactory.decodeFile(imgPath, option);
		imageShare.setImageBitmap(bmp);
		
		btnBack.setOnClickListener(this);
		btnShareComment.setOnClickListener(this);
	}


	
	/**************************************************************************
	 * 
	 * This class is used for post message and image to facebook, twitter.
	 * 
	 **************************************************************************/
	class PostMessage extends AsyncTask<Integer, Void, Boolean> implements
			OnCancelListener {

		/** Dialog show when load token. */
		private ProgressDialog prDialog = null;

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			if (prDialog == null) {
				prDialog = new ProgressDialog(CommentActivity.this);
				prDialog.setMessage("Please wait");
				prDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				prDialog.setCancelable(true);
				prDialog.setCanceledOnTouchOutside(false);
				prDialog.show();
			}

		}

		@Override
		protected Boolean doInBackground(Integer... params) {
			Boolean result = false;
			if (params[0] == AppConstant.TO_FACEBOOK) {
				// Post to facebook wall.
				if (FacebookUtils.postToWall(CommentActivity.this,postContent, bmpShare)) {
					result = true;
				}
			} else {
				if (TwitterUtils.sendTweet(CommentActivity.this,postContent, bmpShare)) {
					result = true;
				}
			}
			return result;
		}

		@Override
		protected void onPostExecute(Boolean result) {

			if (prDialog != null) {
				prDialog.dismiss();
				prDialog = null;
			}

			if (result) {
				Toast.makeText(CommentActivity.this,
						getString(R.string.post_success), Toast.LENGTH_SHORT)
						.show();
				finish();
			} else {
				Toast.makeText(CommentActivity.this,
						getString(R.string.post_failed), Toast.LENGTH_SHORT)
						.show();
			}

			super.onPostExecute(result);
		}

		@Override
		public void onCancel(DialogInterface dialog) {
			if (prDialog != null) {
				prDialog.dismiss();
				prDialog = null;
			}
			dialog.cancel();
		}
	}
	
	
	
	class LoginTwitter extends AsyncTask<Void, Void, Void> implements OnCancelListener{
		/** Dialog show when load token. */
		private ProgressDialog prDialog = null;

		@Override
		protected void onPreExecute() {

			if (prDialog == null) {
				prDialog = new ProgressDialog(CommentActivity.this);
				prDialog.setMessage("Please wait");
				prDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
				prDialog.setCancelable(true);
				prDialog.setOnCancelListener(this);
				prDialog.setCanceledOnTouchOutside(false);
				prDialog.show();
			}
			super.onPreExecute();
		}

		@Override
		protected Void doInBackground(Void... params) {

			// Login to twitter.
			TwitterUtils.login(CommentActivity.this, urlCallBack);
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {

			if (prDialog != null) {
				prDialog.dismiss();
				prDialog = null;
			}
			super.onPostExecute(result);
		}

		@Override
		public void onCancel(DialogInterface dialog) {
			this.cancel(true);
			if (prDialog != null) {
				prDialog.dismiss();
				prDialog = null;
			}
			finish();
		}
	}

	/**
	 * Get RequestToken and open browser to sign in a twitter account .
	 * */
	private void authenticateTwitterAccount() {
		if (isConnectingToInternet()) {
			AsyncTask<Void, Void, String> task = new AsyncTask<Void, Void, String>() {
				@Override
				protected String doInBackground(Void... params) {
					try {
						Twitter mTwitter = new TwitterFactory().getInstance();
						mTwitter.setOAuthConsumer(
								getString(R.string.twitter_comsumer_key),
								getString(R.string.twitter_comsumer_secret));
						RequestToken requestToken = mTwitter
								.getOAuthRequestToken(TwitterUtils.CALLBACK_URL);
						// save request token
						ObjectOutputStream mObjectOutputStream = null;
						try {
							OutputStream mOutputStream = openFileOutput(
									"twitterRequestToken", MODE_PRIVATE);
							mObjectOutputStream = new ObjectOutputStream(
									mOutputStream);
							mObjectOutputStream.writeObject(requestToken);
							mObjectOutputStream.close();
						} catch (Exception e) {
							e.printStackTrace();
						}
						// return Url
						return requestToken.getAuthenticationURL();
					} catch (TwitterException e) {
						e.printStackTrace();
					}
					return null;
				}

				@Override
				protected void onPostExecute(String url) {
					super.onPostExecute(url);
					if (url != null) {
						Intent intent = new Intent(Intent.ACTION_VIEW,
								Uri.parse(url));
						startActivity(intent);
					}
				}
			};

			task.execute();
		} else {
			Toast.makeText(CommentActivity.this, "Internet Connection Lost!",
					Toast.LENGTH_LONG).show();
		}
	}

	private boolean isTwitterLoginedAlready() {
		return mSharedPreferences.getBoolean(
				getString(R.string.twitter_logged_in), false);
	}

	/**
	 * Check device has been connect to Internet.
	 */
	private boolean isConnectingToInternet() {
		ConnectivityManager mConnectivityManager = (ConnectivityManager) getApplicationContext()
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		if (mConnectivityManager != null) {
			NetworkInfo[] mNetworkInfo = mConnectivityManager
					.getAllNetworkInfo();
			if (mNetworkInfo != null) {
				for (int i = 0; i < mNetworkInfo.length; i++) {
					if (mNetworkInfo[i].isConnectedOrConnecting()) {
						return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	public void updateLogin(boolean loginStatus) {
		if (!loginStatus) {
			finish();
		} else {
			editComment.requestFocus();
			getWindow().setSoftInputMode(LayoutParams.SOFT_INPUT_STATE_VISIBLE);
		}
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.button_back:
			finish();
			break;
			
		case R.id.button_sharecomment:
			postContent = editComment.getText().toString();
			if (shareOption == AppConstant.TO_FACEBOOK) {
				if (!FacebookUtils.isAuthenticated(this)) {
					FacebookUtils.login(this);
				}else{
					new PostMessage().execute(AppConstant.TO_FACEBOOK);
				}
				
			} else {
				//shareViaTwitter();
				
				if (!TwitterUtils.isAuthenticated(this)) {
					(new LoginTwitter()).execute();
				}else{
					if (postContent.length() > 140) {
						// Show message post failed.
						AlertDialog alertDlg = new AlertDialog.Builder(
								CommentActivity.this).create();
						alertDlg.setMessage("The maximum number of characterizes is 140");
						alertDlg.setButton(
								android.content.DialogInterface.BUTTON_POSITIVE,
								"OK", new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										// TODO Auto-generated method stub
										dialog.dismiss();
									}
								});
						alertDlg.show();
						return;
					} else {
						// Perform post to twitter.
						new PostMessage().execute(AppConstant.TO_TWITTER);
					}
				}
				
			}
			break;

		default:
			break;
		}
	}
	
	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		if (isResumeWhenPressBack) {
			if (!TwitterUtils.isAuthenticated(this)) {
				finish();
			}
		}
		getWindow().setSoftInputMode(
				WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		isResumeWhenPressBack = true;
		super.onPause();
	}
}
