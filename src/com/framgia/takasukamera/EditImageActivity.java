package com.framgia.takasukamera;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.example.takasukamera.R;
import com.framgia.takasukamera.util.Utils;

public class EditImageActivity extends Activity implements OnClickListener {

	/** Buttons */
	private ImageButton mBtnBack;
	private ImageButton mBtnFinish;
	private ImageButton mBtnDeleteFace;
	private ImageButton mBtnStamp;
	private ImageButton mBtnFaceDetect;
	private ImageButton mBtnUndo;

	/** View */
	private ImageView mImgView;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.edit_activity_layout);

		// initialize view components
		initView();

	}

	private void initView() {
		// Initialize Buttons and View
		mBtnBack = (ImageButton) findViewById(R.id.button_back);
		mBtnFinish = (ImageButton) findViewById(R.id.button_finish);
		mBtnDeleteFace = (ImageButton) findViewById(R.id.button_delete_image);
		mBtnStamp = (ImageButton) findViewById(R.id.button_stamp);
		mBtnFaceDetect = (ImageButton) findViewById(R.id.button_face_detect);
		mBtnUndo = (ImageButton) findViewById(R.id.button_undo);
		mImgView = (ImageView) findViewById(R.id.imgView);

		// Set event listener for buttons
		mBtnBack.setOnClickListener(this);
		mBtnFinish.setOnClickListener(this);
		mBtnDeleteFace.setOnClickListener(this);
		mBtnStamp.setOnClickListener(this);
		mBtnFaceDetect.setOnClickListener(this);
		mBtnUndo.setOnClickListener(this);

		// get Uri of image file
		Uri photoUri = getIntent().getData();
		// show image file on an ImageView
		Bitmap bmp = Utils.getBitmapFromUri(EditImageActivity.this, photoUri,
				true);
		// mImgView.setImageBitmap(bmp);
		mImgView.setImageURI(photoUri);

	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.button_back:
			backToMainActivity();
			break;
		case R.id.button_finish:
			startShareActivity();
			break;
		case R.id.button_delete_image:
			break;
		case R.id.button_stamp:
			break;
		case R.id.button_face_detect:
			break;
		case R.id.button_undo:
			break;
		default:
			break;
		}
	}

	private void backToMainActivity() {
		Intent intent = new Intent(EditImageActivity.this, MainActivity.class);
		startActivity(intent);
		finish();
	}

	private void startShareActivity() {
		Intent intent = new Intent(EditImageActivity.this, ShareActivity.class);
		startActivity(intent);
	}

}
