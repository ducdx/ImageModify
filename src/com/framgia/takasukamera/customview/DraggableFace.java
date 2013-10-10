/*
author:huydx
github:https://github.com/huydx
*/
package com.framgia.takasukamera.customview;

import android.graphics.Bitmap;
import android.graphics.Matrix;

public class DraggableFace {
	private boolean activated;
	
	public Bitmap mBitmap;
	private Matrix marginMatrix;
	private Matrix initialMatrix;
	private Matrix currentMatrix;
	private Matrix savedMatrix;
	private boolean touched;
	
	//Face co-ordinate
	private int mLeftFace;
	private int mTopFace;
	private int mFaceWidth;
	private int mFaceHeight;
	
	
	public DraggableFace(Bitmap b) {
		currentMatrix = null;
		savedMatrix = null;
		initialMatrix = null;
		mBitmap = b;
		activated = false;
	}
	
	public void setLeftFace(int left){
		this.mLeftFace = left;
	}
	
	public void setTopFace(int top){
		this.mTopFace = top;
	}
	
	public void setFaceWidth(int width){
		this.mFaceWidth = width;
	}
	
	public void setFaceHeight(int height){
		this.mFaceHeight = height;
	}
	
	public int getLeftFace(){
		return this.mLeftFace;
	}
	
	public int getTopFace(){
		return this.mTopFace;
	}
	
	public int getFaceWidth(){
		return this.mFaceWidth;
	}
	
	public int getFaceHeight(){
		return this.mFaceHeight;
	}
	
	public void setInitialMatrix(Matrix m) {
	    this.initialMatrix = new Matrix(m);
	}
	
	public void setCurrentMatrix(Matrix m) {
		this.currentMatrix = new Matrix(m);
	}
	
	public void setSavedMatrix(Matrix m) {
		this.savedMatrix = new Matrix(m);
	}
	
	public Matrix getCurrentMatrix() {
		return this.currentMatrix;
	}
	
	public Matrix getSavedMatrix() {
		return this.savedMatrix;
	}
	
	public Matrix getInitialMatrix() {
	    return this.initialMatrix;
	}
	
	public void activate() {
		this.activated = true;
	}
	
	public void deActivate() {
		this.activated = false;
	}
	
	public boolean isActivate() {
		return activated;
	}

    public boolean isTouched() {
        return touched;
    }

    public void setTouched(boolean touched) {
        this.touched = touched;
    }

	public Matrix getMarginMatrix() {
		return marginMatrix;
	}

	public void setMarginMatrix(Matrix marginMatrix) {
		this.marginMatrix = new Matrix(marginMatrix);
	}
}
