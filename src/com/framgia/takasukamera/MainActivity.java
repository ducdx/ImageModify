package com.framgia.takasukamera;

import com.example.takasukamera.R;
import com.framgia.takasukamera.util.Utils;

import android.os.Bundle;
import android.provider.MediaStore;
import android.app.Activity;
import android.content.Intent;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;

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
			break;
		case R.id.button_album:
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
		Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		//String folderStore = Utils.
		//intent.putExtra(name, value)
	}
}
