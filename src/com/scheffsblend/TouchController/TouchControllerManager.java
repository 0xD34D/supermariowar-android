/**
 * 
 */
package com.scheffsblend.TouchController;

import android.content.Context;
import android.graphics.PointF;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;

/**
 * @author Clark Scheff
 *
 */
public class TouchControllerManager {

	private static final String TAG = "TCM";
	/**
	 * Button definitions
	 */
	public static final int BUTTON_DPAD_UP		= 0x01;
	public static final int BUTTON_DPAD_DOWN	= 0x02;
	public static final int BUTTON_DPAD_LEFT	= 0x04;
	public static final int BUTTON_DPAD_RIGHT	= 0x08;
	public static final float DEFAULT_MOTION_DELTA = 25.0f;
	
	private static int mCurrentState;
	private static int mPreviousState;
	private static boolean isListening = false;
	private static PointF mStartPosition;
	private static PointF mEndPosition;
	private static float mMotionDelta = DEFAULT_MOTION_DELTA;
	private static TouchControllerListener mListener = null;
	

	/**
	 * Our OnTouchListener to catch MotionEvents on the attached view
	 */
	public static OnTouchListener touchControlsListener = new OnTouchListener() {
		@Override
		public boolean onTouch(View v, MotionEvent event) {
		if ( !isListening )
			return false;
		
		int action = event.getAction();
		switch ( action ) {
		case MotionEvent.ACTION_DOWN:
		case MotionEvent.ACTION_POINTER_DOWN:
			mStartPosition = new PointF( event.getX(), event.getY() );
			mEndPosition = new PointF( event.getX(), event.getY() );
			Log.i( TAG, "DOWN (" + mStartPosition.x + "," + mStartPosition.y + ")" );
			break;
		case MotionEvent.ACTION_MOVE:
			mEndPosition.x = event.getX();
			mEndPosition.y = event.getY();
			float dx = mEndPosition.x - mStartPosition.x;
			float dy = mEndPosition.y - mStartPosition.y;
			Log.i( TAG, "MOVED (" + dx + "," + dy + ")" );
			mCurrentState = 0;
			if ( dx >= mMotionDelta ) {
				mCurrentState |= BUTTON_DPAD_RIGHT;
			}
			else if ( dx <= -mMotionDelta ) {
				mCurrentState |= BUTTON_DPAD_LEFT;
			}
			
			if ( dy >= mMotionDelta ) {
				mCurrentState |= BUTTON_DPAD_DOWN;
			}
			else if ( dy <= -mMotionDelta ) {
				mCurrentState |= BUTTON_DPAD_UP;
			}
			
			if ( mCurrentState != mPreviousState )
				mListener.onTouchControlsChanged(mCurrentState, mCurrentState ^ mPreviousState);
			
			break;
		case MotionEvent.ACTION_UP:
		case MotionEvent.ACTION_POINTER_UP:
			mCurrentState = 0;
			mListener.onTouchControlsChanged(mCurrentState, mCurrentState ^ mPreviousState);
			break;
		}
		
		return false;
		}
	};
	
	/**
	 * Register a TouchControllerListener
	 * @param listener - the listener to call on touch events
	 * @param touchView - the view to listen for touch events on
	 */
	public static void startListening( TouchControllerListener listener, View touchView ) {
		if ( listener == null || touchView == null )
			return;
		
		mListener = listener;
		touchView.setOnTouchListener( touchControlsListener );
		isListening = true;
	}
	
	/**
	 * Register a TouchControllerListener
	 * @param listener - the listener to call on touch events
	 * @param touchView - the view to listen for touch events on
	 * @param motionDelta - user defined motion delta
	 */
	public static void startListening( TouchControllerListener listener, 
			View touchView, float motionDelta ) {
		
		startListening (listener, touchView );
		mMotionDelta = motionDelta;
	}
	
	public static void stopListening() {
		isListening = false;
	}
}
