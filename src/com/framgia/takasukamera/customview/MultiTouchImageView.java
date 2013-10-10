/*
author:huydx
github:https://github.com/huydx
 */
package com.framgia.takasukamera.customview;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Stack;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PointF;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

import com.framgia.takasukamera.customview.BitmapOperationMap.OPERATION;

public class MultiTouchImageView extends ImageView {

	// some private variable use for detect multi touch
	public enum EDITMODE {
		NONE, DRAG, ZOOM, ROTATE
	}
	
	private boolean mDrawOpacityBackground = false;
	private Paint mPaint = new Paint();
	private DraggableFace mActiveBitmap = null;
	private RectF mInnerImageBounds = null;
	private Stack<BitmapOperationMap> mOperationStack = new Stack<BitmapOperationMap>();
	
	// list of stamp bitmaps
	private List<DraggableFace> mOverlayBitmaps;

	// constructors
	public MultiTouchImageView(Context context) {
		super(context);
		initMembers();
		this.setOnTouchListener(touchListener);
	}

	public MultiTouchImageView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initMembers();
		this.setOnTouchListener(touchListener);
	}

	private void initMembers() {
		mOverlayBitmaps = new ArrayList<DraggableFace>();
	}

	// listeners
	private OnTouchListener touchListener = new OnTouchListener() {
		private EDITMODE mEditMode = EDITMODE.NONE; // to get mode [drag, zoom,
													// rotate]
		private float[] mLastEvent;
		private PointF mStart = new PointF();
		private PointF mMid = new PointF();
		private float mOldDistance;
		private float mNewRotation = 0f;
		private float mDist = 0f;
		
		private boolean touchMoveEndChecker = false; //this variable use to deal with android odd touch behavior (MOVE -> UP -> MOVE -> UP)
		
		@Override
		public boolean onTouch(View v, MotionEvent event) {

			// switch finger events
			switch (event.getAction() & MotionEvent.ACTION_MASK) {
			case (MotionEvent.ACTION_DOWN):
				touchMoveEndChecker = true;
				mDrawOpacityBackground = true;
				int activebmpIdx = getActiveBitmap(event.getX(), event.getY());
																	
				if (activebmpIdx != -1)
					mActiveBitmap = mOverlayBitmaps.get(activebmpIdx);
				else {
					mActiveBitmap = null;
					break;
				}
				mLastEvent = null;
				mEditMode = EDITMODE.DRAG;
				mStart.set(event.getX(), event.getY());

				if (mActiveBitmap != null) {
					mActiveBitmap.setSavedMatrix(mActiveBitmap
							.getCurrentMatrix());
				}
				break;

			case (MotionEvent.ACTION_POINTER_DOWN):
				touchMoveEndChecker = false;
				mDrawOpacityBackground = true;
				if (mActiveBitmap != null) {
					mOldDistance = spacing(event);
					if (mOldDistance > 10f) {
						mActiveBitmap.setSavedMatrix(mActiveBitmap
								.getCurrentMatrix());
						midPoint(mMid, event);
						mEditMode = EDITMODE.ZOOM;
					}

					mLastEvent = new float[4];
					mLastEvent[0] = event.getX(0);
					mLastEvent[1] = event.getX(1);
					mLastEvent[2] = event.getY(0);
					mLastEvent[3] = event.getY(1);

					mDist = rotation(event);
				}
				break;

			case (MotionEvent.ACTION_POINTER_UP):
				mEditMode = EDITMODE.NONE;
				break;

			case (MotionEvent.ACTION_MOVE):
				touchMoveEndChecker = false;
				mDrawOpacityBackground = true;
				
				if (mActiveBitmap != null) {
					if (mEditMode == EDITMODE.DRAG) {
						mActiveBitmap.setCurrentMatrix(mActiveBitmap
								.getSavedMatrix());
						mActiveBitmap.getCurrentMatrix().postTranslate(
								event.getX() - mStart.x,
								event.getY() - mStart.y);
					} else if (mEditMode == EDITMODE.ZOOM
							&& event.getPointerCount() == 2) {
						float newDistance = spacing(event);
						mActiveBitmap.setCurrentMatrix(mActiveBitmap
								.getSavedMatrix());
						if (newDistance > 10f) {
							float scale = newDistance / mOldDistance;
                            mActiveBitmap.getCurrentMatrix()
                                    .postScale(scale, scale, mMid.x, mMid.y);
                        }

                        if (mLastEvent != null) {
                            mNewRotation = rotation(event);
                            float r = mNewRotation - mDist;
                            RectF rec = new RectF(0, 0, mActiveBitmap.mBitmap.getWidth(),
                                    mActiveBitmap.mBitmap.getHeight());
                            mActiveBitmap.getCurrentMatrix().mapRect(rec);
                            mActiveBitmap.getCurrentMatrix().postRotate(r,
                                    rec.left + rec.width() / 2, rec.top + rec.height() / 2);
                        }
                    }
					
                }

            case (MotionEvent.ACTION_UP):
				if (touchMoveEndChecker) { //means 2 continuous ACTION_UP, or real finger up after moving
					mDrawOpacityBackground = false;
					if (mActiveBitmap != null) {
                        // push a map to bitmap and clone of current matrix
                        mOperationStack.push(new BitmapOperationMap(
                                                    mActiveBitmap, 
                                                    new Matrix(mActiveBitmap.getCurrentMatrix()), 
                                                    OPERATION.ADD));
						mActiveBitmap.deActivate();
					}
				}
				touchMoveEndChecker = true;
			default:
				break;
			}

			invalidate();
			return true;
		}

	};

	public void addOverlayBitmap(DraggableFace dBitmap, float scale) {
		Matrix marginMtx = new Matrix();
		
		marginMtx.postTranslate(mInnerImageBounds.left, mInnerImageBounds.top);
		dBitmap.setMarginMatrix(marginMtx);
		
		Matrix curMtx = new Matrix();
		curMtx.postConcat(marginMtx);
		
		if (dBitmap instanceof DraggableFace) {
			curMtx.postTranslate(((DraggableFace) dBitmap).getLeftFace()
					/ scale, ((DraggableFace) dBitmap).getTopFace()
					/ scale);
		}

		dBitmap.setCurrentMatrix(curMtx);
		mOperationStack.push(new BitmapOperationMap(dBitmap, null,
				OPERATION.NEW));
		mOperationStack.push(new BitmapOperationMap(dBitmap, dBitmap
				.getCurrentMatrix(), OPERATION.ADD));
		mOverlayBitmaps.add(dBitmap);

	}

	private int getActiveBitmap(float event_x, float event_y) {
		int size = mOverlayBitmaps.size();
		int retidx = -1;
		DraggableFace retBmp = null;
		// search for all bitmap to find closest to finger
		for (int i = 0; i < size; i++) {
			DraggableFace dBmp = mOverlayBitmaps.get(i);
			dBmp.deActivate();
			float bmp_x = 0;
			float bmp_y = 0;
            RectF r = new RectF(0, 0, dBmp.mBitmap.getWidth(), dBmp.mBitmap.getHeight());
            Matrix mtx = dBmp.getCurrentMatrix() == null ? 
                    dBmp.getMarginMatrix() : dBmp.getCurrentMatrix();

            mtx.mapRect(r);
            bmp_x = r.left;
            bmp_y = r.top;

			if (event_x >= bmp_x && event_x < (bmp_x + r.width())
					&& event_y >= bmp_y
					&& event_y < (bmp_y + r.height())) {
				retBmp = dBmp;
				retidx = i;
			}
		}
		if (retBmp != null) {
		    if (!retBmp.isTouched()) {
		        retBmp.setTouched(true);
		    }
		    retBmp.activate();
		}
		return retidx;
	}

	private float spacing(MotionEvent event) {
		float x = event.getX(0) - event.getX(1);
		float y = event.getY(0) - event.getY(1);
		return (float) Math.sqrt(x * x + y * y);
	}

	/** Calculate the mid point of the first two fingers */
	private void midPoint(PointF point, MotionEvent event) {
		float x = event.getX(0) + event.getX(1);
		float y = event.getY(0) + event.getY(1);
		point.set(x / 2, y / 2);
	}

	private float rotation(MotionEvent event) {
		double delta_x = (event.getX(0) - event.getX(1));
		double delta_y = (event.getY(0) - event.getY(1));
		double rad = Math.atan2(delta_y, delta_x);

		return (float) Math.toDegrees(rad);
	}

	public List<DraggableFace> getOverlayList() {
		return mOverlayBitmaps;
	}
	
    public void undo() {
       if (!mOperationStack.empty()) {
           BitmapOperationMap prev = mOperationStack.pop();
           if (!mOperationStack.empty()) { //current stack is final operation
               prev = mOperationStack.peek();  
           }
           DraggableFace bmp = prev.getDraggableBitmap();
           Matrix mtx = prev.getOperationMatrix();
           
           switch (prev.getOption()) {
           case NEW: //if action is create new, then delete             
               mOverlayBitmaps.remove(bmp);
               break;
           case ADD:
               bmp.setCurrentMatrix(mtx);
               break;
           case DELETE: //not implement yet
               break;
           default:
               break;
           }
       }
	}
	
	@Override
	protected void onDraw(Canvas canvas) { // [TODO] khi xoay man hinh error
        super.onDraw(canvas);
        RectF bitmapRect = getInnerBitmapSize();
        
        mInnerImageBounds = bitmapRect;
        canvas.clipRect(bitmapRect);
      
        // loop to draw all bitmap
        Enumeration<DraggableFace> e = Collections
				.enumeration(mOverlayBitmaps);
		while (e.hasMoreElements()) {
			DraggableFace dBmp = (DraggableFace) e.nextElement();
			if (true) {
				if (dBmp.getCurrentMatrix() != null) {
					canvas.drawBitmap(dBmp.mBitmap, dBmp.getCurrentMatrix(),
							null);
					RectF r = getStampBounding(dBmp);
					if (mDrawOpacityBackground) {
						mPaint.setColor(0x00000000);
						mPaint.setStyle(Style.FILL);
						mPaint.setAlpha(20);
						canvas.drawRect(r, mPaint);

					}
				} 
			}
		}
    }

	public RectF getInnerBitmapSize() {
        RectF bitmapRect = new RectF();

        bitmapRect.right = this.getDrawable().getIntrinsicWidth();
        bitmapRect.bottom = this.getDrawable().getIntrinsicHeight();

        Matrix m = this.getImageMatrix();
        m.mapRect(bitmapRect);
        return bitmapRect;
    }
	
	private RectF getStampBounding(DraggableFace bmp) {
	    RectF r = new RectF(0, 0, bmp.mBitmap.getWidth(), bmp.mBitmap.getHeight());
	    bmp.getCurrentMatrix().mapRect(r);
	    return r;
	}

    public void deleteActiveBitmap() {
        mOverlayBitmaps.remove(mActiveBitmap);        
    }
}
