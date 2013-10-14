package com.framgia.takasukamera;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

import com.example.takasukamera.R;
import com.framgia.takasukamera.constant.AppConstant;

public class ShareActivity extends Activity implements OnClickListener {

	private ImageButton mBtnShareFacebook;
	private ImageButton mBtnShareTwitter;
	private ImageButton mBtnBack;
	String imgPath;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.share_activity_layout);
		Bundle bundle = getIntent().getExtras();
		if(bundle == null){
			return;
		}
		
		imgPath = bundle.getString(AppConstant.IMAGE_PATH);
		
		initView();
	}

	private void initView() {
		mBtnShareFacebook = (ImageButton) findViewById(R.id.button_share_facebook);
		mBtnShareTwitter = (ImageButton) findViewById(R.id.button_share_twitter);
		mBtnBack = (ImageButton) findViewById(R.id.button_back_share);

		mBtnBack.setOnClickListener(this);
		mBtnShareTwitter.setOnClickListener(this);
		mBtnShareFacebook.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		Intent i = new Intent(ShareActivity.this, CommentActivity.class);
		switch (v.getId()) {
		case R.id.button_back_share:
			finish();
			break;
		case R.id.button_share_twitter:
			i.putExtra(AppConstant.SHARE_REQUEST,
					AppConstant.TO_TWITTER);
			i.putExtra(AppConstant.IMAGE_PATH, imgPath);
			startActivity(i);
			break;
		case R.id.button_share_facebook:
			i.putExtra(AppConstant.SHARE_REQUEST,
					AppConstant.TO_FACEBOOK);
			i.putExtra(AppConstant.IMAGE_PATH, imgPath);
			startActivity(i);
			break;

		}

	}

}
