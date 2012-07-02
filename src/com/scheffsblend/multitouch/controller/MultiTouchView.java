package com.scheffsblend.multitouch.controller;

import java.util.Vector;

import com.scheffsblend.smw.R;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.SystemClock;
import android.util.AttributeSet;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;

public class MultiTouchView extends ImageView {

	private static final boolean DEBUG = false;
	private static final String TAG = "MultiTouchView";
	private static final int MAX_TOUCH_POINTS = 10;
	private static final long UPDATE_INTERVAL = 100;
	private OnInputChangedListener mListener = null;

	private int[] mPointerIDs = new int[MAX_TOUCH_POINTS];
	private int numPoints = 0;
	private float[] xVals = new float[MAX_TOUCH_POINTS];
	private float[] yVals = new float[MAX_TOUCH_POINTS];
	
	private int mNumButtons = 0;
	private Vector<Rect> mButtons = new Vector<Rect>();
	private int mButtonsState = 0;
	private Bitmap mJoystickBounds;
	private Bitmap mJoystickNib;
	private PointF mJoystickOrigin = new PointF();
	private PointF mJoystickPosition = new PointF();
	private float mBoundsRadius;
	private int mJoystickID = -1;
	private boolean mNormalizeJoystick = false;
	private long mLastUpdateTime = 0;

	public MultiTouchView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public MultiTouchView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mJoystickBounds = BitmapFactory.decodeResource(getResources(), R.drawable.joystick_bounds);
		mJoystickNib = BitmapFactory.decodeResource(getResources(), R.drawable.joystick_nib);
		mBoundsRadius = mJoystickBounds.getWidth()/2;
	}

	public MultiTouchView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		mJoystickBounds = BitmapFactory.decodeResource(getResources(), R.drawable.joystick_bounds);
		mJoystickNib = BitmapFactory.decodeResource(getResources(), R.drawable.joystick_nib);
		mBoundsRadius = mJoystickBounds.getWidth()/2;
	}

	/* (non-Javadoc)
	 * @see android.view.View#dispatchTouchEvent(android.view.MotionEvent)
	 */
	@Override
	public boolean dispatchTouchEvent(MotionEvent event) {
		boolean ret = super.dispatchTouchEvent(event);
		if(ret)
			try {
				Thread.sleep(30);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		return ret;
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		int action = event.getAction();
		int index = (action & MotionEvent.ACTION_POINTER_INDEX_MASK) 
                >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
		switch(action & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:
			numPoints = 1;
			xVals[0] = event.getX();
			yVals[0] = event.getY();
			touchPointChanged(action, index);
			return true;
		case MotionEvent.ACTION_UP:
			numPoints = 0;
			touchPointChanged(action, index);
			return true;
		case MotionEvent.ACTION_POINTER_DOWN:
			numPoints = event.getPointerCount();
			for (int i = 0; i < numPoints; i++) {
				xVals[i] = event.getX(i);
				yVals[i] = event.getY(i);
				mPointerIDs[i] = event.getPointerId(i);
			}
			touchPointChanged(action, index);
			return true;
		case MotionEvent.ACTION_POINTER_UP:
			numPoints = event.getPointerCount();
			int idx = 0;
			int upIdx = (action & MotionEvent.ACTION_POINTER_INDEX_MASK) 
	                >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
			if (upIdx == mJoystickID) {
				mJoystickID = -1;
				mJoystickPosition.x = mJoystickOrigin.x;
				mJoystickPosition.y = mJoystickOrigin.y;
			}
			for (int i = 0; i < numPoints; i++) {
				if(i != upIdx) {
					xVals[idx] = event.getX(i);
					yVals[idx] = event.getY(i);
					mPointerIDs[idx++] = event.getPointerId(i);
				}
			}
			numPoints--;
			touchPointChanged(action, index);
			return true;
		case MotionEvent.ACTION_MOVE:
			if ((System.currentTimeMillis() - mLastUpdateTime) < UPDATE_INTERVAL )
				return true;
			numPoints = event.getPointerCount();
			for (int i = 0; i < numPoints; i++) {
				xVals[i] = event.getX(i);
				yVals[i] = event.getY(i);
				mPointerIDs[i] = event.getPointerId(i);
			}
			touchPointChanged(action, index);
			return true;
		}
		return false;
	}
	
	public void setNormalizeValues(boolean normalize) {
		mNormalizeJoystick = normalize;
	}

	/**
	 * Called when the touch point info changes, causes a redraw.
	 * 
	 * @param touchPoint
	 */
	private void touchPointChanged(int action, int index) {
		// Take a snapshot of touch point info, the touch point is volatile
		int currButtonsState = mButtonsState;
		boolean hit = false;
		int id;
		Rect r;

		switch(action & MotionEvent.ACTION_MASK) {
		case MotionEvent.ACTION_DOWN:
		case MotionEvent.ACTION_POINTER_DOWN:
			boolean activeHit = false;
			hit = false;
			for (int i = 0; i < mButtons.size() && !hit; i++) {
				id = 1 << i;
				r = mButtons.elementAt(i);
				hit = r.contains((int)xVals[index], (int)yVals[index]);
				if (hit)
					mButtonsState |= 1 << i;
			}
			if (!hit && mJoystickID == -1) {
				mJoystickID = mPointerIDs[index];
				mJoystickPosition.x = mJoystickOrigin.x = xVals[index];
				mJoystickPosition.y = mJoystickOrigin.y = yVals[index];
			}
			break;
		case MotionEvent.ACTION_UP:
			mJoystickID = -1;
			mJoystickPosition.x = mJoystickOrigin.x;
			mJoystickPosition.y = mJoystickOrigin.y;
			mButtonsState = 0;
			break;
		case MotionEvent.ACTION_POINTER_UP:
			mButtonsState = 0;
			for (int j = 0; j < mButtons.size(); j++) {
				id = 1 << j;
				r = mButtons.elementAt(j);
				hit = false;
				for (int i = 0; i < numPoints && !hit; i++)
					hit = (r.contains((int)xVals[i], (int)yVals[i])
							&& mPointerIDs[i] != mJoystickID);
				if (hit)
					mButtonsState |= id;
			}
			break;
		case MotionEvent.ACTION_MOVE:
			for (int i = 0; i < numPoints; i++) {
				if (mPointerIDs[i] == mJoystickID) {
					setStickPos(xVals[i], yVals[i]);
				} else {
					mButtonsState = 0;
					for (int j = 0; j < mButtons.size() && !hit; j++) {
						id = 1 << j;
						r = mButtons.elementAt(j);
						hit = r.contains((int)xVals[i], (int)yVals[i]);
						if (hit)
							mButtonsState |= 1 << j;
						else
							mButtonsState &= ~(1 << j);
					}
				}
			}
			break;
		}
		float dx = mJoystickPosition.x - mJoystickOrigin.x;
		float dy = mJoystickPosition.y - mJoystickOrigin.y;
		if (mNormalizeJoystick) {
			dx = dx / mBoundsRadius;
			dy = dy / mBoundsRadius;
		}
		if (mListener != null)
			mListener.onInputChanged(dx, dy, mButtonsState);
			
		invalidate();
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		if (mJoystickID != -1) {
			canvas.drawBitmap(mJoystickBounds, mJoystickOrigin.x - mJoystickBounds.getWidth()/2, 
					mJoystickOrigin.y - mJoystickBounds.getHeight()/2, null);
			canvas.drawBitmap(mJoystickNib, mJoystickPosition.x - mJoystickNib.getWidth()/2, 
					mJoystickPosition.y - mJoystickNib.getHeight()/2, null);
		}

		// Log touch point indices
		if (DEBUG) {
			StringBuilder buf = new StringBuilder();
			for (int i = 0; i < numPoints; i++)
				buf.append(" " + i + "->" + mPointerIDs[i]);
			Log.i("MultiTouchVisualizer", buf.toString());
		}
	}
	
	public int addButton(Rect bounds) {
		int id = 1 << mNumButtons;
		mButtons.add(bounds);
		mNumButtons++;
		
		if (DEBUG)
			Log.i(TAG, String.format("Added button [%d] - (%d, %d, %d, %d)",
					id, bounds.left, bounds.top, bounds.right, bounds.bottom ));
		
		return id;
	}
	
	private void setStickPos(float x, float y) {
		float theta = (float)Math.atan2(y - mJoystickOrigin.y, x - mJoystickOrigin.x);
		float d = FloatMath.sqrt((float)Math.pow(mJoystickOrigin.x - x, 2) + (float)Math.pow(mJoystickOrigin.y - y, 2));
		if (d > mBoundsRadius) {
			d = mBoundsRadius;
			x = mJoystickOrigin.x + FloatMath.cos(theta) * mBoundsRadius;
			y = mJoystickOrigin.y + FloatMath.sin(theta) * mBoundsRadius;
		}
			
		mJoystickPosition.x = x;
		mJoystickPosition.y = y;
	}
	
	public void setOnInputChangeListener(OnInputChangedListener listener) {
		mListener = listener;
	}
	
	public interface OnInputChangedListener {
		public void onInputChanged(float stickX, float stickY, int buttons);
	}
}
