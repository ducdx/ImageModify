package com.framgia.takasukamera;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;

import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;

import com.example.takasukamera.R;
import com.framgia.takasukamera.constant.AppConstant;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

public class CommentActivity extends Activity {
	private ImageButton btnBack;
	private ImageButton btnShareComment;
	private ImageView imageShare;
	private EditText editComment;
	private ProgressDialog mProgressDialog;
	private SharedPreferences mSharedPreferences;
	private int shareOption;
	private String message;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.comment_activity);
		mSharedPreferences = getApplicationContext().getSharedPreferences(
				"kameraAppSharedPreferences", MODE_PRIVATE);
		mProgressDialog = new ProgressDialog(this);
		// get option to share via twitter or facebook
		shareOption = getIntent().getExtras().getInt(AppConstant.SHARE_REQUEST);

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

		btnBack.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

		btnShareComment.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				message = editComment.getText().toString();
				switch (shareOption) {
				case AppConstant.SHARE_TWITTER_REQUEST:
					shareViaTwitter();
					break;
				case AppConstant.SHARE_FACEBOOK_REQUEST:
					break;

				}
				editComment.setText("");

			}
		});
	}

	private void shareViaTwitter() {
		if (isTwitterLoginedAlready()) {
			if (!isConnectingToInternet()) {
				Toast.makeText(CommentActivity.this, "Connection Lost!",
						Toast.LENGTH_LONG).show();
				return;
			}

			AlertDialog tweetDialog = new AlertDialog.Builder(this)
					.setTitle("Share an Image via Twitter")
					.setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(
										DialogInterface dialogInterface, int i) {
									AsyncTask<String, Void, Boolean> task = new AsyncTask<String, Void, Boolean>() {
										@Override
										protected Boolean doInBackground(
												String... params) {
											try {
												// setting for twitter instance
												Twitter mTwitter = new TwitterFactory()
														.getInstance();
												mTwitter.setOAuthConsumer(
														getString(R.string.twitter_comsumer_key),
														getString(R.string.twitter_comsumer_secret));
												AccessToken accessToken = null;
												try {
													InputStream in = openFileInput("twitterAccessToken");
													ObjectInputStream ois = new ObjectInputStream(
															in);
													accessToken = (AccessToken) ois
															.readObject();
												} catch (Exception e) {
													e.printStackTrace();
												}
												mTwitter.setOAuthAccessToken(accessToken);
												// now is the time to tweet
												// something
												try {
													StatusUpdate status = new StatusUpdate(
															params[0]);
													// status.media(new
													// File(mSavedPicPath));
													mTwitter.updateStatus(status);
													return true;
												} catch (TwitterException e) {
													e.printStackTrace();
												}
												return false;
											} catch (Exception e) {
												e.printStackTrace();
											}
											return false;
										}

										@Override
										protected void onPostExecute(
												Boolean result) {
											// show the toast
											Toast.makeText(
													CommentActivity.this,
													result ? " OK " : "Failed",
													Toast.LENGTH_LONG).show();
											// can dismiss progress status
											if (mProgressDialog.isShowing()) {
												mProgressDialog.hide();
											}
										}
									};
									// show progress dialog
									mProgressDialog.setMessage("Uploading");
									mProgressDialog.show();
									// execute task
									task.execute(message);
								}
							})
					.setNegativeButton("Cancel",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(
										DialogInterface dialogInterface, int i) {

								}
							}).create();
			tweetDialog.show();
		} else {
			// authenticate Twitter Account
			authenticateTwitterAccount();
		}

	}

	/**
	 * Get feedback from browser.
	 */
	@Override
	protected void onNewIntent(Intent intent) {
		super.onNewIntent(intent);
		Uri uri = intent.getData();
		if (uri == null
				|| !uri.toString().startsWith(
						getString(R.string.twitter_callback_url))) {
			return;
		}
		String verifier = uri.getQueryParameter("oauth_verifier");
		AsyncTask<String, Void, Boolean> task = new AsyncTask<String, Void, Boolean>() {
			@Override
			protected Boolean doInBackground(String... params) {
				try {
					Twitter mTwitter = new TwitterFactory().getInstance();
					mTwitter.setOAuthConsumer(
							getString(R.string.twitter_comsumer_key),
							getString(R.string.twitter_comsumer_secret));
					// get RequestToken has been save before
					RequestToken requestToken = null;
					ObjectInputStream mObjectInputStream = null;
					try {
						InputStream mInputStream = openFileInput("twitterRequestToken");
						mObjectInputStream = new ObjectInputStream(mInputStream);
						requestToken = (RequestToken) mObjectInputStream
								.readObject();
						mObjectInputStream.close();
					} catch (Exception e) {
						e.printStackTrace();
					}
					AccessToken accessToken = null;
					accessToken = mTwitter.getOAuthAccessToken(requestToken,
							params[0]);
					// save AccessToken
					ObjectOutputStream mObjectOutputStream = null;
					try {
						OutputStream mOutputStream = openFileOutput(
								"twitterAccessToken", MODE_PRIVATE);
						mObjectOutputStream = new ObjectOutputStream(
								mOutputStream);
						mObjectOutputStream.writeObject(accessToken);
						mObjectOutputStream.close();

					} catch (Exception e) {
						e.printStackTrace();
					}

					Editor editor = mSharedPreferences.edit();
					editor.putBoolean(getString(R.string.twitter_logged_in),
							true);
					editor.commit();
					return true;
				} catch (TwitterException e) {
					e.printStackTrace();
				}
				return false;
			}

			@Override
			protected void onPostExecute(Boolean result) {
				super.onPostExecute(result);
				if (result)
					shareViaTwitter();
				else
					Toast.makeText(CommentActivity.this, "Failed",
							Toast.LENGTH_LONG).show();
			}
		};

		task.execute(verifier);
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
								.getOAuthRequestToken(getString(R.string.twitter_callback_url));
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
}
