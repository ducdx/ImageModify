package com.framgia.takasukamera;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

import com.example.takasukamera.R;

public class ShareActivity extends Activity{

	private ImageButton mBtnShareFacebook;
	private ImageButton mBtnShareTwitter;
	private ImageButton mBtnBack;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.share_activity_layout);
		
		initView();
	}
	
	private void initView(){
		mBtnShareFacebook = (ImageButton)findViewById(R.id.button_share_facebook);
		mBtnShareTwitter = (ImageButton)findViewById(R.id.button_share_twitter);
		mBtnBack = (ImageButton)findViewById(R.id.button_back_share);
		
		mBtnBack.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(ShareActivity.this,EditImageActivity.class);
				startActivity(intent);
				finish();
			}
		});
		
		mBtnShareFacebook.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
			}
		});
		
		mBtnShareTwitter.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
			}
		});
	}
}
