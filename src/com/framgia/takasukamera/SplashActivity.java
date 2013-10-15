package com.framgia.takasukamera;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.example.takasukamera.R;
import com.framgia.takasukamera.social.FacebookUtils;
import com.framgia.takasukamera.social.TwitterUtils;

public class SplashActivity extends Activity{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.splash_activity);
		
		TwitterUtils.setConsumerId(getString(R.string.twitter_comsumer_key), getString(R.string.twitter_comsumer_secret));
		FacebookUtils.setAppId(getString(R.string.fb_appid));
		Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			
			@Override
			public void run() {
				Intent intent = new Intent(SplashActivity.this,MainActivity.class);
				startActivity(intent);
				finish();
			}
		}, 2000);
	}
	
}
