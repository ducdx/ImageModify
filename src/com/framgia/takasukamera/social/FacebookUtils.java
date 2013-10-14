package com.framgia.takasukamera.social;

import java.io.ByteArrayOutputStream;

import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.Bundle;

import com.example.takasukamera.R;
import com.framgia.takasukamera.constant.AppConstant;

/**
 * This class contains all function execute with Facebook.
 * 
 * @author Penguin
 * 
 */
public class FacebookUtils {

	/** Variable of Facebook Object. */
	private static Facebook facebook;
	

	/** APP_ID for facebook. */
	private static String APP_ID = "";

	/**
	 * This function is used to write information login to file.
	 * 
	 * @param context
	 * @return
	 */
	public static boolean save(Context context) {
		Editor editor = context.getSharedPreferences(AppConstant.PREFERENCE_NAME,
				Context.MODE_PRIVATE).edit();
		editor.putString(AppConstant.TOKEN, facebook.getAccessToken());
		editor.putLong(AppConstant.EXPIRES, facebook.getAccessExpires());
		return editor.commit();
	}

	/**
	 * This function is used to restore information login.
	 * 
	 * @param context
	 * @return true if session valid otherwise false.
	 */
	public static boolean restore(Context context) {
		facebook = new Facebook(context.getString(R.string.fb_appid));
		SharedPreferences savedSession = context.getSharedPreferences(
				AppConstant.PREFERENCE_NAME, Context.MODE_PRIVATE);
		facebook.setAccessToken(savedSession.getString(AppConstant.TOKEN, null));
		facebook.setAccessExpires(savedSession.getLong(AppConstant.EXPIRES, 0));
		return facebook.isSessionValid();
	}

	/**
	 * This function is used to clear session.
	 */
	public static void clear(Context context) {
		Editor editor = context.getSharedPreferences(AppConstant.PREFERENCE_NAME,
				Context.MODE_PRIVATE).edit();
		// editor.clear();
		editor.remove(AppConstant.TOKEN);
		editor.remove(AppConstant.EXPIRES);
		editor.remove(AppConstant.USER_FACEBOOK);
		editor.commit();
	}

	/**
	 * This function is used to check authenticated.
	 * 
	 * @return true if session valid otherwise false.
	 */
	public static boolean isAuthenticated(Context context) {

		boolean result = false;
		if (restore(context))
			result = facebook.isSessionValid();
		return result;
	}

	/**
	 * This function is used to login Facebook.
	 * 
	 * @param context
	 * @return
	 */
	public static void login(Activity context) {
		FbLoginDialogListener listener = new FbLoginDialogListener(context);
		listener.setUpdater((FbLoginChangeListener) context);
		facebook = new Facebook(context.getString(R.string.fb_appid));
		facebook.authorize(context, AppConstant.PERMISSIONS,
				Facebook.FORCE_DIALOG_AUTH, listener);
	}

	/**
	 * This function is used to logout Facebook.
	 */
	public static boolean logOut(Context context) {

		boolean result = false;

		if (restore(context)) {
			try {
				clear(context);
				facebook.logout(context);
				result = true;
			} catch (Exception e) {
				result = false;
			}
		}
		return result;
	}

	/**
	 * This function is used to get user information.
	 * 
	 * @return null if not exists or error when connect to server.
	 */
	public static FbUserFace getInfo(Context context) {

		FbUserFace fbUserFace = null;
		if (restore(context)) {
			try {
				String result = facebook.request("me");
				JSONObject response = new JSONObject(result);
				if (response != null) {
					fbUserFace = new FbUserFace();
					fbUserFace.setId(response.getString("id"));
					fbUserFace.setName(response.getString("name"));
					fbUserFace.setUserName(response.getString("username"));
					fbUserFace.setGender(response.getString("gender"));
				}
			} catch (Exception e) {
				return null;
			}

		}
		return fbUserFace;
	}

	/**
	 * This function is used to get user name.
	 * 
	 * @return null if not exists or error when connect to server.
	 */
	public static String getUserName(Context context) {

		String userName = getUserFromFile(context);
		if (userName == null) {
			if (restore(context)) {
				try {
					String result = facebook.request("me");
					JSONObject response = new JSONObject(result);
					if (response != null) {
						userName = response.getString("name");
						if (userName != null) {
							Editor editor = context.getSharedPreferences(
									AppConstant.PREFERENCE_NAME,
									Context.MODE_PRIVATE).edit();
							editor.putString(AppConstant.USER_FACEBOOK, userName);
							editor.commit();
						}
					}
				} catch (Exception e) {
					return null;
				}
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
		return savedSession.getString(AppConstant.USER_FACEBOOK, null);
	}

	/**
	 * Post image to wall.
	 * 
	 * @param {context}
	 * @param {message} Comment
	 * @param {bitmap} image.
	 * @return
	 */
	public static boolean postToWall(Context context, String message,
			Bitmap bitmap) {

		Boolean result = false;
		String url = "me/feed";
		if (restore(context)) {
			try {
				Bundle b = new Bundle();
				if (bitmap != null) {
					b.putString("app_id", APP_ID);
					b.putString("name", message);
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					bitmap.compress(CompressFormat.JPEG, 100, baos);
					byte[] data = null;
					data = baos.toByteArray();
					b.putByteArray("picture", data);
					url = "me/photos";
				} else {
					b.putString("message", message);
				}
				String response = facebook.request(url, b, "POST");
				if (response == null || response.equals("")
						|| response.equals("false")) {
					result = false;
				} else {
					result = true;
				}
			} catch (Exception e) {
				result = false;
			} finally {
				if (bitmap != null) {
					bitmap.recycle();
					bitmap = null;
				}
			}
		}
		return result;
	}
	
	/**
	 * TODO: Method used to set APP_ID for facebook.
	 * 
	 * @param APP_ID
	 *            String value.
	 */
	public static void setAppId(String ID) {
		APP_ID = ID;
	}
}
