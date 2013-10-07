package com.framgia.takasukamera.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.media.ExifInterface;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;

import com.framgia.takasukamera.constant.AppConstant;

/**
 * @author Admin
 * 
 */
public class Utils {
	private static final String TAG = "TakasuKamera";

	
	public static boolean checkDeviceStorage(){
		String externalStorateState = Environment.getExternalStorageState();

		if (externalStorateState.equals(Environment.MEDIA_MOUNTED)) {
			return true;
		}
		return false;
	}

	public static Uri getPhotoUri() {
		if (!checkDeviceStorage()) {
			return null;
		}
		File rootFolder = Environment.getExternalStorageDirectory();
		File tempPhoto = new File(rootFolder.getAbsolutePath()
				+ AppConstant.TEMP_FILE_JPG);
		try {
			if (!tempPhoto.exists()) {

				tempPhoto.createNewFile();
			}
			Uri tempPhotoUri = Uri.fromFile(tempPhoto);
			return tempPhotoUri;
		} catch (IOException e) {
			e.printStackTrace();
			return Uri.EMPTY;
		}
	}

	public static void deleteTempFile() {
		File rootFolder = Environment.getExternalStorageDirectory();
		File temFile = new File(rootFolder.getAbsolutePath()
				+ AppConstant.TEMP_FILE_JPG);
		if (temFile.exists()) {
			temFile.delete();
		}
	}

	public static boolean isTempFileExisted() {
		File rootFolder = Environment.getExternalStorageDirectory();
		File tempFile = new File(rootFolder.getAbsolutePath()
				+ AppConstant.TEMP_FILE_JPG);
		if (tempFile.exists()) {
			return true;
		} else {
			return false;
		}
	}

	public static Bitmap convert(Bitmap bitmap, Bitmap.Config config) {
		Bitmap convertedBitmap = Bitmap.createBitmap(bitmap.getWidth(),
				bitmap.getHeight(), config);
		Canvas canvas = new Canvas(convertedBitmap);
		Paint paint = new Paint();
		paint.setColor(Color.BLACK);
		canvas.drawBitmap(bitmap, 0, 0, paint);
		return convertedBitmap;
	}

	public static Bitmap codec(Bitmap src, Bitmap.CompressFormat format,
			int quality) {
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		src.compress(format, quality, os);

		byte[] array = os.toByteArray();
		return BitmapFactory.decodeByteArray(array, 0, array.length);
	}

	public static String saveBitmapToGallery(Bitmap bitmap, Activity activity) {
		// Get path to public storage
		File fileStore = Environment
				.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
		File mediaStorageDir = new File(fileStore, AppConstant.APP_NAME);

		// Check whether storage folder exist?
		if (!mediaStorageDir.exists()) {
			// If this folder is not exist, create it (include its parent)
			if (!mediaStorageDir.mkdirs()) {
				return "";
			}
		}

		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss")
				.format(new Date());
		File mediaFile = new File(mediaStorageDir.getPath() + File.separator
				+ AppConstant.APP_NAME + timeStamp + ".jpg");

		try {
			FileOutputStream stream = new FileOutputStream(mediaFile);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
		} catch (IOException exception) {
			Log.w(TAG, "IOException during saving bitmap", exception);
			return "";
		}

		if (mediaFile.getPath() != null) {

			MediaScannerConnection.scanFile(activity,
					new String[] { mediaFile.toString() },
					new String[] { "image/jpeg" }, null);
			return mediaFile.getPath();
		}
		return "";
	}

	public static Bitmap getBitmapFromUri(Activity activity, Uri uri,
			boolean forDetection) {

		// Get screen size to calculate appropriate size for bitmap
		DisplayMetrics displaymetrics = new DisplayMetrics();
		activity.getWindowManager().getDefaultDisplay()
				.getMetrics(displaymetrics);
		Resources r = activity.getResources();
		int px = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
				forDetection ? 95 : 50, r.getDisplayMetrics());
		int targetWidth = displaymetrics.heightPixels;
		int targetHeight = displaymetrics.widthPixels - px;

		Bitmap bitmap = null;
		Bitmap rotatedBitmap = null;

		try {
			bitmap = decodeSampleBitmap(activity, uri, targetWidth,
					targetHeight, forDetection);
			if (bitmap == null) {
				return null;
			}

			ExifInterface ex = new ExifInterface(uri.getPath());
			int orientation = ex.getAttributeInt(ExifInterface.TAG_ORIENTATION,
					ExifInterface.ORIENTATION_UNDEFINED);

			if (orientation == ExifInterface.ORIENTATION_UNDEFINED) {
				Cursor cursor = activity
						.getContentResolver()
						.query(uri,
								new String[] { MediaStore.Images.ImageColumns.ORIENTATION },
								null, null, null);

				try {
					if (cursor.moveToFirst()) {
						int deg = cursor
								.getInt(cursor
										.getColumnIndexOrThrow(MediaStore.Images.ImageColumns.ORIENTATION));
						if (deg == 90) {
							orientation = ExifInterface.ORIENTATION_ROTATE_90;
						} else if (deg == 180) {
							orientation = ExifInterface.ORIENTATION_ROTATE_180;
						} else if (deg == 270) {
							orientation = ExifInterface.ORIENTATION_ROTATE_270;
						}
					}

					cursor.close();
				} catch (Exception e) {

				}
			}

			int degree = 0;
			switch (orientation) {
			case ExifInterface.ORIENTATION_ROTATE_270:
				degree += 270;
				break;
			case ExifInterface.ORIENTATION_ROTATE_180:
				degree += 180;
				break;
			case ExifInterface.ORIENTATION_ROTATE_90:
				degree += 90;
				break;
			case ExifInterface.ORIENTATION_TRANSPOSE:
				degree += 45;
				break;
			case ExifInterface.ORIENTATION_UNDEFINED:
				degree += 360;
				break;
			default:
				break;
			}

			if (degree > 0) {
				Matrix matrix = new Matrix();
				matrix.postRotate(degree);
				rotatedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
						bitmap.getWidth(), bitmap.getHeight(), matrix, true);
			} else {
				rotatedBitmap = bitmap;
			}

		} catch (Exception e) {
			// handle the exception(s)
		}

		return rotatedBitmap;
	}

	public static int calculateSampleSize(Activity activity, int width,int height,
			int reqWidth, int reqHeight, boolean forFacedetect) {

		if (forFacedetect) {
			int bestSize = 1200;
			// Recalculate require size
			if (reqWidth < bestSize && reqHeight < bestSize) {
				final float heightRatio = (float) bestSize / (float) reqHeight;
				final float widthRatio = (float) bestSize / (float) reqWidth;
				final float ratio = (heightRatio < widthRatio) ? heightRatio
						: widthRatio;
				reqWidth = (int) (reqWidth * ratio);
				reqHeight = (int) (reqHeight * ratio);
			}

		}

		int inSampleSize = 1;

		if (height > reqHeight || width > reqWidth) {

			// Calculate ratios of height and width to requested height and
			// width
			final int heightRatio = Math.round((float) height
					/ (float) reqHeight);
			final int widthRatio = Math.round((float) width / (float) reqWidth);

			if (heightRatio == 0 || widthRatio == 0) {
				return inSampleSize;
			}

			// Choose the smallest ratio as inSampleSize value, this will
			// guarantee
			// a final image with both dimensions larger than or equal to the
			// requested height and width.
			inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
		}

		return inSampleSize;
	}

	public static Bitmap decodeSampleBitmap(Activity activity, Uri uri,
			int reqWidth, int reqHeight, boolean forFaceDetect)
			throws Exception {

		// First decode with inJustDecodeBounds=true to check dimensions
		BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;
		
		Bitmap bmp = null;
		
		InputStream is = activity.getContentResolver().openInputStream(uri);		
		BitmapFactory.decodeStream(is, null, options);
		is.close();
		
		is = activity.getContentResolver().openInputStream(uri);	
		// Calculate inSampleSize
		options.inSampleSize = calculateSampleSize(activity, options.outWidth,options.outHeight, reqWidth,
				reqHeight, forFaceDetect);

		// Decode bitmap with inSampleSize set
		options.inJustDecodeBounds = false;		
		bmp = BitmapFactory.decodeStream(is, null, options);		
		is.close();
		
		return bmp;
	}
}
