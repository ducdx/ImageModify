/*
author:huydx
github:https://github.com/huydx
*/
package com.framgia.takasukamera.customview;

import android.graphics.Matrix;

public class BitmapOperationMap {
    DraggableFace mBitmap;
    Matrix mOperationMtx;
    OPERATION mOpt;
    
    public enum OPERATION {
        NEW, ADD, DELETE
    }
    
    //map between a (ref to) bitmap and (ref to) matrix
    public BitmapOperationMap(DraggableFace bmp, Matrix mtx, OPERATION op) {
        mBitmap = bmp;
        mOperationMtx = mtx;
        mOpt = op;
    }
    
    public DraggableFace getDraggableBitmap() { return mBitmap; }
    public Matrix getOperationMatrix() { return mOperationMtx; }
    public OPERATION getOption() { return mOpt; }
}
