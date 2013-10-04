package com.framgia.takasukamera;

import java.io.File;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.takasukamera.R;
import com.framgia.takasukamera.constant.AppConstant;
import com.framgia.takasukamera.util.Utils;

public class MainActivity extends Activity implements OnClickListener{

	private ImageButton mBtnCamera;
	private ImageButton mBtnAlbum;
	private ImageButton mBtnOther;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		//initialize view components
		initView();
	}

	/**
	 * This function is used to initialize view components.
	 */
	private void initView(){
		//initialize buttons 
		mBtnCamera = (ImageButton)findViewById(R.id.button_camera);
		mBtnAlbum = (ImageButton)findViewById(R.id.button_album);
		mBtnOther = (ImageButton)findViewById(R.id.button_other);
		
		//set onClick listener for buttons.
		mBtnCamera.setOnClickListener(this);
		mBtnAlbum.setOnClickListener(this);
		mBtnOther.setOnClickListener(this);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.button_camera:
			openCameraToTakePicture();
			break;
		case R.id.button_album:
			openGallery();
			break;
		case R.id.button_other:
			break;
		default:
			break;
		}
	}
	
	
	/**
	 * This function is used to process onClick event for button camera
	 */
	private void openCameraToTakePicture(){
		if(!Utils.checkDeviceStorage()){
			return;
		}
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		/*
		File tmpFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath(),"tmp_img_" + String.valueOf(System.currentTimeMillis()) + ".png");
		if(!tmpFile.exists()){
			try{
				tmpFile.createNewFile();
			}catch(Exception e){
				e.printStackTrace();
			}
			
		}
		tmpFile.delete();
		Uri imgUri = Uri.fromFile(tmpFile);
		
		intent.putExtra(MediaStore.EXTRA_OUTPUT,imgUri);*/
		startActivityForResult(intent, AppConstant.TAKE_PICTURE_REQUEST);
	}
	
	
	/**
	 * This function is used to process onClick event for button Gallery
	 */
	private void openGallery(){
		Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
		intent.setType("image/*");
		startActivityForResult(intent, AppConstant.SELECT_PICTURE_REQUEST);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if(resultCode != RESULT_OK ){
			return;
		}
		
		switch(requestCode){
		case AppConstant.TAKE_PICTURE_REQUEST:
			break;
		case AppConstant.SELECT_PICTURE_REQUEST:
			
			break;
		}
	}
	
	
}
