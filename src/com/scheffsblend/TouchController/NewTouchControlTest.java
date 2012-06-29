package com.scheffsblend.TouchController;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;

import com.scheffsblend.smw.KeyCodes;
import com.scheffsblend.smw.R;
import com.scheffsblend.smw.SDLActivity;
import com.scheffsblend.smw.R.id;
import com.scheffsblend.smw.R.layout;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.View.OnTouchListener;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * This class is to test a new touch control scheme where the whole screen
 * is the direction pad and movement is determined by the direction the user
 * slides their finger.
 */

/**
 * @author Clark Scheff
 *
 */
public class NewTouchControlTest extends Activity 
	implements TouchControllerListener {

	private static final String TAG = "NEW_TOUCH";

	private LinkedHashMap<String, View> mOnScreenControls;
	private LinkedHashMap.Entry<String,View> touchedViews[] = new LinkedHashMap.Entry[2];
	private TextView tv;
	private View mMultiTouchController;
	private Bitmap mTouchDisplay;
	private Canvas mTouchCanvas = new Canvas();
	private PointF point1 = new PointF();
	private PointF point2 = new PointF();
	private int pointerId[] = new int[2];
	private Paint mPaint = new Paint();

	protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );

        // get rid of the title bar and go fullscreen
        requestWindowFeature( Window.FEATURE_NO_TITLE );
    	getWindow().setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN,
    			WindowManager.LayoutParams.FLAG_FULLSCREEN );

        setContentView( R.layout.touch_controls );
        tv = (TextView) findViewById( R.id.touchInfo );
        loadOnScreenControls();
    }
    
    protected void onPause() {
        super.onPause();
    }

    protected void onResume() {
        super.onResume();
        int width = mMultiTouchController.getWidth();
        int height = mMultiTouchController.getHeight();
        
        mTouchDisplay = Bitmap.createBitmap( 800, 480, Config.ARGB_8888 );
        mTouchCanvas.setBitmap( mTouchDisplay );
    }

	@Override
	public void onTouchControlsChanged(int controlsState, int controlsChanged) {
		StringBuilder sb = new StringBuilder();
		sb.append( "" );
		if ( (controlsState & TouchControllerManager.BUTTON_DPAD_UP) > 0 )
			sb.append( "UP " );
		else if ( (controlsState & TouchControllerManager.BUTTON_DPAD_DOWN) > 0 )
			sb.append( "DOWN " );
		
		if ( (controlsState & TouchControllerManager.BUTTON_DPAD_LEFT) > 0 )
			sb.append( "LEFT" );
		else if ( (controlsState & TouchControllerManager.BUTTON_DPAD_RIGHT) > 0 )
			sb.append( "RIGHT" );
		
		if ( controlsState != 0 )
			tv.setText( sb.toString() );
		else
			tv.setText( " " );
	}
    
	/**
     * Adds all the touchscreen controls to the mOnScreenControls HashMap
     */
    private void loadOnScreenControls() {
		mOnScreenControls = new LinkedHashMap<String, View>();
    	mOnScreenControls.put("ENTER", this.findViewById(R.id.enterButton));
    	mOnScreenControls.put("ESCAPE", this.findViewById(R.id.escapeButton));
    	mOnScreenControls.put("USE", this.findViewById(R.id.useButton));
    	mOnScreenControls.put("TURBO", this.findViewById(R.id.turboButton));
    	mOnScreenControls.put("DPAD", this.findViewById(R.id.vTouchPad));
    	
    	touchedViews[0] = touchedViews[1] = null;

        TouchControllerManager.startListening(this, findViewById(R.id.vTouchPad));

        mMultiTouchController = findViewById(R.id.multiTouchView);
		mMultiTouchController.setOnTouchListener(touchControlsListener);
    }

	/**
	 * Custom touch listener for the multi-touch implementation
	 */
	private OnTouchListener touchControlsListener = new OnTouchListener() {

		@Override
		public boolean onTouch(View v, MotionEvent event) {
			final int action = event.getAction();
			float x = -1f, y = -1f;
			boolean consumed = false;
			switch (action & MotionEvent.ACTION_MASK) {
			case MotionEvent.ACTION_DOWN:
				x = event.getX(0);
				y = event.getY(0);
				pointerId[0] = event.getPointerId(0);
				return processTouch(x, y, event, 0);
			case MotionEvent.ACTION_UP:
				x = event.getX(0);
				y = event.getY(0);
				return processTouch(x, y, event, 0);
			case MotionEvent.ACTION_POINTER_DOWN:
				x = event.getX(1);
				y = event.getY(1);
				pointerId[1] = event.getPointerId(1);
				return processTouch(x, y, event, 0);
			case MotionEvent.ACTION_POINTER_UP:
				x = event.getX();
				y = event.getY();
				int pointerIndex = (action & MotionEvent.ACTION_POINTER_INDEX_MASK) >> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
				int pointerId = event.getPointerId(pointerIndex);
				return processTouch(x, y, event, 1);
			case MotionEvent.ACTION_MOVE:
				mTouchDisplay.eraseColor(0x00000000);
				for(int i = 0; i < event.getPointerCount(); i++)
				{
					x = event.getX(i);
					y = event.getY(i);
					consumed |= processTouch(x, y, event, i);
					mPaint.setColor(Color.rgb(255, 0, 255*i));
					mTouchCanvas.drawCircle(x, y, 60 - i*10, mPaint);
				}
				((ImageView)mMultiTouchController).setImageBitmap(mTouchDisplay);
				return consumed;
			}
			return false;
		}
	};

	/**
	 * Processes a touch based at (x,y). This should handle touching parts of
	 * the on-screen controls
	 * 
	 * @param x - x coordinate of the touch
	 * @param y - y coordinate of the touch
	 * @return - true if the touch was on an onScreenControl
	 */
	private boolean processTouch(float x, float y, MotionEvent event, int pointerIndex) {
		int action = event.getAction() & MotionEvent.ACTION_MASK;
		String controlId;
		View v;
		Rect outRect = new Rect();
		StringBuilder sb = new StringBuilder();
		sb.append("processTouch: " + "(");
		sb.append(x);
		sb.append(",");
		sb.append(y);
		sb.append(") ");
		
		switch (action) {
		case MotionEvent.ACTION_DOWN:
			pointerIndex = 0;
		case MotionEvent.ACTION_POINTER_DOWN:
			pointerIndex = (event.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK)
				>> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
			boolean foundView = false;
			LinkedHashMap.Entry<String, View> other = null;
			Iterator it = mOnScreenControls.entrySet().iterator();
			while (it.hasNext() && !foundView) {
				LinkedHashMap.Entry<String, View> entry = (LinkedHashMap.Entry<String, View>) it.next();
				controlId = entry.getKey();
				v = entry.getValue();
				v.getHitRect(outRect);
				if ( !foundView && (x >= outRect.left && x <= outRect.right) && 
					 (y >= outRect.top && y <= outRect.bottom) ) {
					touchedViews[pointerIndex] = entry;
					foundView = true;
					sb.append(controlId); sb.append(" touched! [");
					sb.append(pointerIndex); sb.append("]");
					Log.d(TAG, controlId + " touched! [" + pointerIndex + "]");
				} else if (controlId.contains("FIRE"))
					other = entry;
			}
			Log.d(TAG, sb.toString());
			if (!foundView)
				return false;
			
			v = touchedViews[pointerIndex].getValue();
			controlId = touchedViews[pointerIndex].getKey();
			if (controlId.contains("DPAD")) {
				TouchControllerManager.touchControlsListener.onTouch(v, event);
			} else if (controlId.contains("ENTER")) {
			} else if (controlId.contains("ESCAPE")) {
			} else if (controlId.contains("TURBO")) {
			} else if (controlId.contains("USE")) {
			}
			
			return true;
		case MotionEvent.ACTION_POINTER_UP:
		case MotionEvent.ACTION_UP:
			pointerIndex = (event.getAction() & MotionEvent.ACTION_POINTER_INDEX_MASK)
				>> MotionEvent.ACTION_POINTER_INDEX_SHIFT;
			if(touchedViews[pointerIndex] != null) {
				sb.append(touchedViews[pointerIndex].getKey() + " untouched! [");
				sb.append(pointerIndex); sb.append("]");
				Log.d(TAG, sb.toString());
			} else
				return true;

			v = touchedViews[pointerIndex].getValue();
			controlId = touchedViews[pointerIndex].getKey();
			
			if (controlId.contains("DPAD")) {
				TouchControllerManager.touchControlsListener.onTouch(v, event);
			} else if (controlId.contains("ENTER")) {
				SDLActivity.onNativeKeyUp(KeyCodes.KEY_ENTER);
			} else if (controlId.contains("ESCAPE")) {
				SDLActivity.onNativeKeyUp(KeyCodes.KEY_ESCAPE);
			} else if (controlId.contains("TURBO")) {
				SDLActivity.onNativeKeyUp(KeyCodes.KEY_RCTRL);
			} else if (controlId.contains("USE")) {
				SDLActivity.onNativeKeyUp(KeyCodes.KEY_RSHIFT);
			}
			
			if(pointerIndex == 0) {
				touchedViews[0] = touchedViews[1];
				touchedViews[1] = null;
			} else
				touchedViews[1] = null;
				
			return true;
		case MotionEvent.ACTION_MOVE:
			if(touchedViews[pointerIndex] == null)
				return true;
			//sb.append(touchedViews[pointerIndex].getKey() + " moved! [");
			//sb.append(pointerIndex); sb.append("]");
			//Log.d(TAG, sb.toString());
			v = touchedViews[pointerIndex].getValue();
			controlId = touchedViews[pointerIndex].getKey();
			if (controlId.contains("DPAD")) {
				TouchControllerManager.touchControlsListener.onTouch(v, event);
			}
			return true;
		}
		
		return false;
	}
}