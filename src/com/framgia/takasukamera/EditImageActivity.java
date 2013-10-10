package com.framgia.takasukamera;

import java.util.ArrayList;
import java.util.Collections;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PointF;
import android.graphics.RectF;
import android.media.FaceDetector;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.takasukamera.R;
import com.framgia.takasukamera.customview.DraggableFace;
import com.framgia.takasukamera.customview.MultiTouchImageView;
import com.framgia.takasukamera.util.Utils;

public class EditImageActivity extends Activity implements OnClickListener {

	/** Buttons */
	private ImageButton mBtnBack;
	private ImageButton mBtnFinish;
	private ImageButton mBtnDeleteFace;
	private ImageButton mBtnStamp;
	private ImageButton mBtnFaceDetect;
	private ImageButton mBtnUndo;
	
	/** Bitmap */
	private Bitmap mSourceBitmap;

	/** View */
	private MultiTouchImageView mImgView;
	
	private ArrayList<DraggableFace> mFaceList;
		
	Thread detectFaceThread;

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
		mImgView = (MultiTouchImageView) findViewById(R.id.imgView);

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
		mSourceBitmap = Utils.getBitmapFromUri(EditImageActivity.this, photoUri,true);
		
		if(mSourceBitmap == null){
			return;
		}
		
		Log.i("ImageViewSize", "width: " + mSourceBitmap.getWidth() + " height: " + mSourceBitmap.getHeight());
		mImgView.setImageBitmap(mSourceBitmap);
		
		detectFaceThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				mFaceList = getFaceRectInBitmap(mSourceBitmap);
			}
		});
		
		detectFaceThread.start();
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
			new ReplaceFace().execute();
			break;
		case R.id.button_undo:
			break;
		default:
			break;
		}
	}

	/**
	 * This function is used to back to main activity
	 */
	private void backToMainActivity() {
		Intent intent = new Intent(EditImageActivity.this, MainActivity.class);
		startActivity(intent);
		finish();
	}

	private void startShareActivity() {
		Intent intent = new Intent(EditImageActivity.this, ShareActivity.class);
		startActivity(intent);
	}

	@Override
	public void onBackPressed() {
		backToMainActivity();
	}
	
	private class ReplaceFace extends AsyncTask<Void, Void, Void>{

		ProgressDialog mProgressDlg = null;
		MediaPlayer mMediaPlayer = null;
		
		Thread delayThread = new Thread(new Runnable() {
			
			@Override
			public void run() {
				try{
					Thread.sleep(2000);
				}catch(Exception e){
					e.printStackTrace();
				}
			}
		});
		
		@Override
		protected void onPreExecute() {
			mProgressDlg = new ProgressDialog(EditImageActivity.this);
			mProgressDlg.setMessage("Please wait...");
			mProgressDlg.setCancelable(false);
			mProgressDlg.show();
			delayThread.start();
			playSound(R.raw.yes3);
			super.onPreExecute();
		}
		
		@Override
		protected Void doInBackground(Void... params) {
			try{
				detectFaceThread.join();
				delayThread.join();
			}catch(InterruptedException e){
				e.printStackTrace();
			}catch(Exception e){
				e.printStackTrace();
			}
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			
			if(mProgressDlg != null){
				mProgressDlg.dismiss();
				mProgressDlg = null;
			}
			if(mFaceList == null || mFaceList.isEmpty()){
				Toast.makeText(EditImageActivity.this, "Could not detect any face", Toast.LENGTH_SHORT).show();
				playSound(R.raw.takasu1);
			}else{
				int numberSize = mFaceList.size();
				//create list of random face index
				ArrayList<Integer> arrIndex = new ArrayList<Integer>();
				for(int i=0;i<numberSize;i++){
					int pos = i%3;
					arrIndex.add(Integer.valueOf(pos));
				}
				
				//Shuffle array index to get random value
				Collections.shuffle(arrIndex);
				
				RectF rect = mImgView.getInnerBitmapSize();
				float scale = (float)mSourceBitmap.getWidth()/rect.width();
				
				for(int index = 0;index<numberSize;index++){
					//get random face bitmap				       
					int posFace = arrIndex.get(index).intValue();
					int resourceId = -1;
					if(posFace == 1){
						resourceId = R.drawable.incho_1;
					}else if(posFace == 2){
						resourceId = R.drawable.incho_2;
					}else{
						resourceId = R.drawable.incho;
					}
					
					DraggableFace drgFace = mFaceList.get(index);
					Bitmap bmp = BitmapFactory.decodeResource(getResources(), resourceId);
					Bitmap scaleBitmap = Bitmap.createScaledBitmap(bmp, (int)(drgFace.getFaceWidth()/scale), (int)(drgFace.getFaceHeight()/scale), false);
					drgFace.mBitmap = scaleBitmap;
					
					mImgView.addOverlayBitmap(drgFace, scale);
					mImgView.invalidate();
				}

				playSound(R.raw.takasusound);
			}
			super.onPostExecute(result);
		}

		private void playSound(int soundId){
			if(mMediaPlayer!=null){
                if(mMediaPlayer.isPlaying()){
                	mMediaPlayer.stop();
                }
                mMediaPlayer.release();
                mMediaPlayer = null;
            }
			mMediaPlayer = MediaPlayer.create(EditImageActivity.this, soundId);
            if (mMediaPlayer!=null) {
            	mMediaPlayer.start();
            }
		}
		
	}
	
	/**
	 * This function is used to detect face in a picture.
	 */
	private ArrayList<DraggableFace> getFaceRectInBitmap(Bitmap bmp){
		int numberFace = 50;
		FaceDetector faceDetector;
		FaceDetector.Face[] detectedFace = new FaceDetector.Face[numberFace];
		float eyesDistance;
		int numberFaceDetected;
		int leftFace;
		int topFace;
		int rightFace;
		int bottomFace;
		
		ArrayList<DraggableFace> FaceArr = new ArrayList<DraggableFace>();
		faceDetector = new FaceDetector(bmp.getWidth(), bmp.getHeight(), numberFace);
		Bitmap detectBitmap = Utils.changeBitmapConfig(bmp, Bitmap.Config.RGB_565);
		numberFaceDetected = faceDetector.findFaces(detectBitmap, detectedFace);
		Log.i("Number of face detected:","" + numberFaceDetected);
		if(numberFaceDetected <= 0){
			return null;
		}
		
		for(int i=0;i<numberFaceDetected;i++){
			FaceDetector.Face face = detectedFace[i];
			PointF midPoint = new PointF();
			face.getMidPoint(midPoint);
			eyesDistance = face.eyesDistance();
			
			//create rect for face
			leftFace = (int) (midPoint.x - 7*eyesDistance /5);
            topFace = (int) (midPoint.y - 9*eyesDistance/5);
            rightFace = (int) (midPoint.x + 7*eyesDistance /5);
            bottomFace = (int) (midPoint.y + 9*eyesDistance/5);
			            
           
            
			//Create face to replace
			DraggableFace dragFace = new DraggableFace(null);
			dragFace.setLeftFace(leftFace);
			dragFace.setTopFace(topFace);
			dragFace.setFaceWidth(rightFace - leftFace);
			dragFace.setFaceHeight(bottomFace - topFace); 
			
			FaceArr.add(dragFace);
		}
		
		return FaceArr;
	}

}
