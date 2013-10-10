package com.framgia.takasukamera;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import com.example.takasukamera.R;
import com.framgia.takasukamera.constant.AppConstant;

import android.app.Activity;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class StampActivity extends Activity implements OnClickListener {
	private ImageButton btnClose;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.stamp_activity);

		btnClose = (ImageButton) findViewById(R.id.btn_close);
		btnClose.setOnClickListener(this);
		LinearLayout stampList = (LinearLayout) findViewById(R.id.stamp_list);
		try {
			AssetManager assetManager = getAssets();
			String[] files = assetManager.list(AppConstant.STAMP_FOLDER);
			for (int i = 0; i < files.length; i++) {
				View row = getLayoutInflater()
						.inflate(R.layout.stamp_row, null);
				ImageView imageView1 = (ImageView) row
						.findViewById(R.id.stamp1);
				ImageView imageView2 = (ImageView) row
						.findViewById(R.id.stamp2);

				InputStream inputStream = assetManager
						.open(AppConstant.STAMP_FOLDER + File.separator
								+ files[i]);
				Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
				imageView1.setImageBitmap(bitmap);
				imageView1.setOnClickListener(this);
				inputStream.close();

				if (i < files.length - 1) {
					i++;
					inputStream = assetManager.open(AppConstant.STAMP_FOLDER
							+ File.separator + files[i]);
					bitmap = BitmapFactory.decodeStream(inputStream);
					imageView2.setImageBitmap(bitmap);
					imageView2.setOnClickListener(this);
					inputStream.close();
				}
				stampList.addView(row);
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onClick(View v) {
		Intent returnIntent = new Intent();
		switch (v.getId()) {
		case R.id.btn_close:
			setResult(RESULT_CANCELED, returnIntent);
			finish();
			break;

		default:
			ImageView imageView = (ImageView) v;
			returnIntent.putExtra("data",
					((BitmapDrawable) imageView.getDrawable()).getBitmap());
			setResult(RESULT_OK, returnIntent);
			finish();
			break;
		}
	}
}
