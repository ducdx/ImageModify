package com.framgia.takasukamera.social;

import android.content.Context;
import android.os.Bundle;
import android.widget.Toast;

import com.example.takasukamera.R;
import com.framgia.takasukamera.social.Facebook.DialogListener;

/**
 * 
 * @author Penguin
 *
 */
public class FbLoginDialogListener implements DialogListener {

	private Context context;

	/** The reference to UI object. */
	private FbLoginChangeListener updater;

	public FbLoginDialogListener(Context context) {
		this.context = context;
	}

	public FbLoginChangeListener getUpdater() {
		return updater;
	}

	public void setUpdater(FbLoginChangeListener context2) {
		this.updater = context2;
	}

	public void onComplete(Bundle values) {

		// Save information login.
		FacebookUtils.save(context);
		// update UI.
		updater.updateLogin(true);
		Toast.makeText(context, context.getString(R.string.login_fb_success),
				Toast.LENGTH_LONG).show();
	}

	public void onFacebookError(FbError error) {
		updater.updateLogin(false);
		Toast.makeText(context, context.getString(R.string.login_fb_error),
				Toast.LENGTH_LONG).show();
	}

	public void onError(FbDialogError error) {
		updater.updateLogin(false);
		Toast.makeText(context, context.getString(R.string.login_fb_error),
				Toast.LENGTH_LONG).show();
	}

	public void onCancel() {
		updater.updateLogin(false);
		Toast.makeText(context, context.getString(R.string.login_fb_canceled),
				Toast.LENGTH_LONG).show();
	}
}
