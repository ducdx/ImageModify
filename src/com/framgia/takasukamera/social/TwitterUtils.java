package com.framgia.takasukamera.social;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import twitter4j.StatusUpdate;
import twitter4j.Twitter;
import twitter4j.TwitterFactory;
import twitter4j.User;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import twitter4j.conf.Configuration;
import twitter4j.conf.ConfigurationBuilder;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import android.util.Log;

import com.framgia.takasukamera.constant.AppConstant;
import com.framgia.takasukamera.util.Utils;

/**
 * This class contains all function execute with Twitter.
 * 
 * @author TuyenDN
 * 
 */
public class TwitterUtils {

	/** Variable of Twitter Object. */
	private static Twitter twitter;

	/** Variable of RequestToken Object. */
	private static RequestToken requestToken;

	/** Default of CallBack. */
	public static final String CALLBACK_URL = "takasukamera://twitter";

	/** Consumer key */
	private static String CONSUMER_KEY = "";
	
	/** Consumer secret */
	private static String CONSUMER_SECRET = "";

	/**
	 * This function is used to check authenticated.
	 * 
	 * @param context
	 * @return true if authenticated otherwise false.
	 */
	public static boolean isAuthenticated(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(
				AppConstant.PREFERENCE_NAME, Context.MODE_PRIVATE);
		return prefs.getString(AppConstant.PREF_KEY_TOKEN, null) != null;
	}

	/**
	 * This function is used to get information of user.
	 * 
	 * @param context
	 * @return null if not exists or error when connect to server.
	 */
	public static User getInfo(Context context) {
		settingTwitter(context);
		try {
			return twitter.showUser(twitter.getId());
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * This function is used to get user name.
	 * 
	 * @param context
	 * @return null if not exists or error when connect to server.
	 */
	public static String getUserName(Context context) {
		String userName = getUserFromFile(context);
		if (userName == null) {
			settingTwitter(context);
			try {
				userName = twitter.showUser(twitter.getId()).getName();
				if (userName != null) {
					Editor editor = context.getSharedPreferences(
							AppConstant.PREFERENCE_NAME, Context.MODE_PRIVATE)
							.edit();
					editor.putString(AppConstant.USER_TWITTER, userName);
					editor.commit();
				}
			} catch (Exception e) {
				Log.i("Get Twitter UserName", "Error: "+e.getMessage());
				return null;
			}
		}
		return userName;
	}

	/** Flag to check show dialog or not. */
	public static boolean showDialogGetInfor(Context context) {
		if (isAuthenticated(context)) {
			if (getUserFromFile(context) == null)
				return true;
		}
		return false;
	}

	/** Get user from file. */
	public static String getUserFromFile(Context context) {
		SharedPreferences savedSession = context.getSharedPreferences(
				AppConstant.PREFERENCE_NAME, Context.MODE_PRIVATE);
		return savedSession.getString(AppConstant.USER_TWITTER, null);
	}

	/**
	 * This function is used to send tweed to twitter.
	 * 
	 * @param context
	 * @param msg
	 * @param {bitmap} if the image is null, only post message to wall.
	 * @throws Exception
	 */
	public static boolean sendTweet(Context context, String msg, Bitmap bitmap) {
		Boolean result = false;
		try {
			settingTwitter(context);
			StatusUpdate status = new StatusUpdate(msg);
			if (bitmap != null) {
				ByteArrayOutputStream bos = new ByteArrayOutputStream();
				bitmap.compress(CompressFormat.JPEG, 100, bos);
				byte[] bitmapdata = bos.toByteArray();
				ByteArrayInputStream bs = new ByteArrayInputStream(bitmapdata);
				status.setMedia("", bs);
			}
			twitter.updateStatus(status);
			result = true;
		} catch (Exception e) {
			result = false;
			Log.e("sendTweet:", e.getMessage());
		}
		return result;
	}

	/**
	 * This function is used to setting Twitter.
	 * 
	 * @param context
	 */
	public static void settingTwitter(Context context) {

		SharedPreferences prefs = context.getSharedPreferences(
				AppConstant.PREFERENCE_NAME, Context.MODE_PRIVATE);
		String token = prefs.getString(AppConstant.PREF_KEY_TOKEN, "");
		String secret = prefs.getString(AppConstant.PREF_KEY_SECRET, "");

		ConfigurationBuilder confbuilder = new ConfigurationBuilder();
		Configuration conf = confbuilder
				.setOAuthConsumerKey(CONSUMER_KEY)
				.setOAuthConsumerSecret(
						CONSUMER_SECRET)
				.setOAuthAccessToken(token).setOAuthAccessTokenSecret(secret)
				.build();
		twitter = new TwitterFactory(conf).getInstance();
	}

	/**
	 * This function is used to authentication twitter.
	 * 
	 * @param context
	 */
	public static boolean login(Activity context, String callBackURL) {

		if (callBackURL == null) {
			callBackURL = CALLBACK_URL;
		}
		ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
		configurationBuilder.setOAuthConsumerKey(CONSUMER_KEY);
		configurationBuilder.setOAuthConsumerSecret(CONSUMER_SECRET);
		Configuration configuration = configurationBuilder.build();
		twitter = new TwitterFactory(configuration).getInstance();
		try {
			requestToken = twitter.getOAuthRequestToken(callBackURL);
			Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(requestToken
					.getAuthenticationURL() + "&force_login=true"));
			context.startActivity(i);
		} catch (Exception e) {
			return false;
		}
		return true;
	}

	/**
	 * This function is used to handle OAuth callback.
	 * 
	 * @return
	 */
	public static boolean handleOAuthCallback(Context context, Uri uri,
			String callBackURL) {

		if (callBackURL == null) {
			callBackURL = CALLBACK_URL;
		}
		boolean result = false;
		if (uri != null && uri.toString().startsWith(callBackURL)) {
			String verifier = uri
					.getQueryParameter(AppConstant.IEXTRA_OAUTH_VERIFIER);
			try {
				AccessToken accessToken = (new TwGetAccessToken(twitter,
						verifier, requestToken)).execute().get();
				SharedPreferences prefs = context.getSharedPreferences(
						AppConstant.PREFERENCE_NAME, Context.MODE_PRIVATE);
				Editor e = prefs.edit();
				e.putString(AppConstant.PREF_KEY_TOKEN, accessToken.getToken());
				e.putString(AppConstant.PREF_KEY_SECRET,
						accessToken.getTokenSecret());
				e.commit();
				result = true;
			} catch (Exception e) {
				Log.e("Exception", "" + e.getMessage());
				result = false;
			}
		}
		return result;
	}

	/**
	 * This function is used to remove Token, secret from SharedPreferences.
	 */
	public static void logout(Context context) {

		// Clear Cookies.
		Utils.clearCookies(context);

		SharedPreferences prefs = context.getSharedPreferences(
				AppConstant.PREFERENCE_NAME, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.remove(AppConstant.PREF_KEY_TOKEN);
		editor.remove(AppConstant.PREF_KEY_SECRET);
		editor.remove(AppConstant.USER_TWITTER);
		editor.commit();
	}

	/**
	 * Method used to set ConsumerId for twitter.
	 * 
	 * @param CONSUMER_KEY
	 * @param CONSUMER_SECRET
	 */
	public static void setConsumerId(String consumer_key, String consumer_secret) {
		CONSUMER_KEY = consumer_key;
		CONSUMER_SECRET = consumer_secret;
	}

}
