/**
 * 
 */
package com.framgia.takasukamera.social;

import twitter4j.Twitter;
import twitter4j.TwitterException;
import twitter4j.auth.AccessToken;
import twitter4j.auth.RequestToken;
import android.os.AsyncTask;
import android.util.Log;

public class TwGetAccessToken extends AsyncTask<Void, Void, AccessToken> {

	private Twitter twitter;

	private String verifier;

	private RequestToken requestToken;

	/** Constructor. */
	public TwGetAccessToken(Twitter twitter, String verifier,
			RequestToken requestToken) {
		this.twitter = twitter;
		this.verifier = verifier;
		this.requestToken = requestToken;
	}

	@Override
	protected AccessToken doInBackground(Void... params) {
		AccessToken accessToken = null;
		try {
			accessToken = twitter.getOAuthAccessToken(requestToken, verifier);
		} catch (TwitterException e) {
			accessToken = null;
			Log.e("GetAccessToken:", e.getMessage());
		}
		return accessToken;
	}
}
