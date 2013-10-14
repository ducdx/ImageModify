package com.framgia.takasukamera.social;

/**
 * 
 * @author TuyenDN
 *
 */
public class FbError extends Throwable {

    private static final long serialVersionUID = 1L;

    private int mErrorCode = 0;
    private String mErrorType;

    public FbError(String message) {
        super(message);
    }

    public FbError(String message, String type, int code) {
        super(message);
        mErrorType = type;
        mErrorCode = code;
    }

    public int getErrorCode() {
        return mErrorCode;
    }

    public String getErrorType() {
        return mErrorType;
    }

}
