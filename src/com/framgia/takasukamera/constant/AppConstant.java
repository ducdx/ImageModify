package com.framgia.takasukamera.constant;

public class AppConstant {
	public static final int TAKE_PICTURE_REQUEST = 1;
	public static final int SELECT_PICTURE_REQUEST = 2;
	public static final int STAMP_REQUEST = 3;
	
    public static final String SHARE_REQUEST ="share request";
    
	public static final String APP_NAME = "TakasuKamera";
	public static final String STAMP_FOLDER = "stamp";
	public static final String TEMP_FILE_JPG = "/.tmp.jpg";

	public static final String IMAGE_PATH = "imagepath";
	public static final String JP_NAVER_LINE_ANDROID = "jp.naver.line.android";
	public static final String TAKASU_HOME_PAGE = "https://takasuapp.com/sp/";
	
	public static final String PREFERENCE_NAME = "TAKASU_CAMERA";
	/** Twitter Constant */
	public static final String PREF_KEY_SECRET = "oauth_token_secret";

	public static final String PREF_KEY_TOKEN = "oauth_token";

	public static final String IEXTRA_AUTH_URL = "auth_url";

	public static final String IEXTRA_OAUTH_VERIFIER = "oauth_verifier";

	public static final String IEXTRA_OAUTH_TOKEN = "oauth_token";

	public static final String USER_TWITTER = "user_twitter";

	/** Permission access facebook. */
	public static final String[] PERMISSIONS = new String[] { "create_event",
			"user_events", "friends_events", "publish_stream", "read_stream",
			"rsvp_event", "user_photos", "friends_photos" };

	/** Strings used in the authorization flow. */
	public static final String REDIRECT_URI = "fbconnect://success";

	public static final String CANCEL_URI = "fbconnect://cancel";

	public static final String TOKEN = "access_token";

	public static final String EXPIRES = "expires_in";

	public static final String SINGLE_SIGN_ON_DISABLED = "service_disabled";

	/** Facebook server endpoints: may be modified in a subclass for testing. */
	public static final String DIALOG_BASE_URL = "https://m.facebook.com/dialog/";

	public static final String GRAPH_BASE_URL = "https://graph.facebook.com/";

	public static final String RESTSERVER_URL = "https://api.facebook.com/restserver.php";

	public static final String USER_FACEBOOK = "user_facebook";

	public static final int TO_LOGIN_TWITTER = 100;

	public static final int TO_FACEBOOK_POST = 101;

	public static final int TO_TWITTER_POST = 102;

	public static final int TO_ALL = 0;

	public static final int TO_TWITTER = 1;

	public static final int TO_FACEBOOK = 2;
}
