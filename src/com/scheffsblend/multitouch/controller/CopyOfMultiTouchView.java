package com.scheffsblend.multitouch.controller;

import java.util.Vector;

import com.scheffsblend.multitouch.controller.MultiTouchController.MultiTouchObjectCanvas;
import com.scheffsblend.multitouch.controller.MultiTouchController.PointInfo;
import com.scheffsblend.multitouch.controller.MultiTouchController.PositionAndScale;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.widget.ImageView;

public class CopyOfMultiTouchView extends ImageView implements
		MultiTouchObjectCanvas<Object> {

	private static final boolean DEBUG = true;
	private static final String TAG = "MultiTouchView";
	private MultiTouchController<Object> mMultiTouchController;
	private PointInfo mCurrTouchPoint;
	private OnInputChangedListener mListener = null;

	private static final int[] TOUCH_COLORS = { Color.BLUE, Color.MAGENTA };

	private int[] mTouchPointColors = new int[MultiTouchController.MAX_TOUCH_POINTS];

	private Paint mTouchPaint;
	
	private int mNumButtons = 0;
	private Vector<Rect> mButtons = new Vector<Rect>();
	private int mButtonsState = 0;

	public CopyOfMultiTouchView(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public CopyOfMultiTouchView(Context context, AttributeSet attrs) {
		super(context, attrs);
		mMultiTouchController = new MultiTouchController<Object>(this);
		mCurrTouchPoint = new PointInfo();

		for (int i = 0; i < MultiTouchController.MAX_TOUCH_POINTS; i++)
			mTouchPointColors[i] = i < TOUCH_COLORS.length ? TOUCH_COLORS[i]
					: (int) (Math.random() * 0xffffff) + 0xff000000;

		mTouchPaint = new Paint();
		mMultiTouchController.setHandleSingleTouchEvents(true);
	}

	public CopyOfMultiTouchView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);

		mMultiTouchController = new MultiTouchController<Object>(this);
		mCurrTouchPoint = new PointInfo();

		for (int i = 0; i < MultiTouchController.MAX_TOUCH_POINTS; i++)
			mTouchPointColors[i] = i < TOUCH_COLORS.length ? TOUCH_COLORS[i]
					: (int) (Math.random() * 0xffffff) + 0xff000000;

		mTouchPaint = new Paint();
		mMultiTouchController.setHandleSingleTouchEvents(true);
	}

	@Override
	public boolean onTouchEvent(MotionEvent event) {
		boolean ret = mMultiTouchController.onTouchEvent(event);
		return ret;
	}

	@Override
	public Object getDraggableObjectAtPoint(PointInfo touchPoint) {
		return this;
	}

	@Override
	public void getPositionAndScale(Object obj,
			PositionAndScale objPosAndScaleOut) {
		// TODO Auto-generated method stub

	}

	@Override
	public boolean setPositionAndScale(Object obj,
			PositionAndScale newObjPosAndScale, PointInfo touchPoint) {
		touchPointChanged(touchPoint);
		return true;
	}

	@Override
	public void selectObject(Object obj, PointInfo touchPoint) {
		touchPointChanged(touchPoint);
	}

	/**
	 * Called when the touch point info changes, causes a redraw.
	 * 
	 * @param touchPoint
	 */
	private void touchPointChanged(PointInfo touchPoint) {
		// Take a snapshot of touch point info, the touch point is volatile
		mCurrTouchPoint.set(touchPoint);
		int currButtonsState = mButtonsState;
		int numPoints = touchPoint.getNumTouchPoints();
		float[] xs = touchPoint.getXs();
		float[] ys = touchPoint.getYs();
		mButtonsState = 0;
		for (int i = 0; i < mButtons.size(); i++) {
			int id = 1 << i;
			Rect r = mButtons.elementAt(i);
			boolean hit = false;
			for(int j = 0; j < numPoints && !hit; j++) {
				hit = r.contains((int)xs[j], (int)ys[j]);
			}
			if (hit)
				mButtonsState |= 1 << i;
			else
				mButtonsState &= ~(1 << i);
		}
		if ((mButtonsState ^ currButtonsState) != 0 && mListener != null)
			mListener.onInputChanged(0, 0, mButtonsState);
			
		invalidate();
	}
	
	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		int numPoints = mCurrTouchPoint.getNumTouchPoints();
		float[] xs = mCurrTouchPoint.getXs();
		float[] ys = mCurrTouchPoint.getYs();
		float[] pressures = mCurrTouchPoint.getPressures();
		int[] pointerIds = mCurrTouchPoint.getPointerIds();

		// Show touch circles
		for (int i = 0; i < numPoints; i++) {
			mTouchPaint.setColor(mTouchPointColors[pointerIds[i]]);
			float r = 70;// + pressures[i] * 120;
			canvas.drawCircle(xs[i], ys[i], r, mTouchPaint);
		}

		// Log touch point indices
		if (DEBUG) {
			StringBuilder buf = new StringBuilder();
			for (int i = 0; i < numPoints; i++)
				buf.append(" " + i + "->" + pointerIds[i]);
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
	
	public void setOnInputChangeListener(OnInputChangedListener listener) {
		mListener = listener;
	}
	
	public interface OnInputChangedListener {
		public void onInputChanged(float stickX, float stickY, int buttons);
	}
}
