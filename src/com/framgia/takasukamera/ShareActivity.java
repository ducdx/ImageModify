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
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.takasukamera.R;

public class ShareActivity extends Activity {

	private ImageButton mBtnShareFacebook;
	private ImageButton mBtnShareTwitter;
	private ImageButton mBtnBack;
	private ProgressDialog mProgressDialog;
	private SharedPreferences mSharedPreferences;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.share_activity_layout);
		mSharedPreferences = getApplicationContext().getSharedPreferences(
				"kameraAppSharedPreferences", MODE_PRIVATE);
		mProgressDialog = new ProgressDialog(this);

		initView();
	}

	private void initView() {
		mBtnShareFacebook = (ImageButton) findViewById(R.id.button_share_facebook);
		mBtnShareTwitter = (ImageButton) findViewById(R.id.button_share_twitter);
		mBtnBack = (ImageButton) findViewById(R.id.button_back_share);

		mBtnBack.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent intent = new Intent(ShareActivity.this,
						EditImageActivity.class);
				startActivity(intent);
				finish();
			}
		});

		mBtnShareFacebook.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {

			}
		});

		mBtnShareTwitter.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				shareViaTwitter();

			}
		});
	}

	private void shareViaTwitter() {
		if (isTwitterLoginedAlready()) {
			if (!isConnectingToInternet()) {
				Toast.makeText(ShareActivity.this, "Connection Lost!",
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
											Toast.makeText(ShareActivity.this,
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
									task.execute(getResources().getString(
											R.string.string_default_twitter));
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
					Toast.makeText(ShareActivity.this, "Failed",
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
			Toast.makeText(ShareActivity.this, "Internet Connection Lost!",
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
