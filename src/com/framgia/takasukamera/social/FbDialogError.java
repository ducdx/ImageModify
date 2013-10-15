package com.framgia.takasukamera.social;


public class FbDialogError extends Throwable {

    private static final long serialVersionUID = 1L;

    /**
     * The ErrorCode received by the WebView: see
     * http://developer.android.com/reference/android/webkit/WebViewClient.html
     */
    private int mErrorCode;

    /** The URL that the dialog was trying to load */
    private String mFailingUrl;

    public FbDialogError(String message, int errorCode, String failingUrl) {
        super(message);
        mErrorCode = errorCode;
        mFailingUrl = failingUrl;
    }

    int getErrorCode() {
        return mErrorCode;
    }

    String getFailingUrl() {
        return mFailingUrl;
    }

}