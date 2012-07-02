package com.scheffsblend.TouchController;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;

import com.scheffsblend.multitouch.controller.MultiTouchView;
import com.scheffsblend.multitouch.controller.MultiTouchView.OnInputChangedListener;
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
import android.widget.SlidingDrawer;
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
	implements OnInputChangedListener {

	private static final String TAG = "NEW_TOUCH";
	private static final int USE_BUTTON = 0;
	private static final int TURBO_BUTTON = 1;
	private static final int ENTER_BUTTON = 2;
	private static final int ESCAPE_BUTTON = 3;

	private TextView tv;
	private MultiTouchView mTouchView;
	private int[] mButtonIDs = new int[4];

	protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );

        // get rid of the title bar and go fullscreen
        requestWindowFeature( Window.FEATURE_NO_TITLE );
    	getWindow().setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN,
    			WindowManager.LayoutParams.FLAG_FULLSCREEN );

        setContentView( R.layout.touch_controls );
        tv = (TextView) findViewById( R.id.touchInfo );
        mTouchView = (MultiTouchView) findViewById(R.id.multiTouchView);
        mTouchView.setOnInputChangeListener(this);
        //loadOnScreenControls();
    }
    
    /* (non-Javadoc)
	 * @see android.app.Activity#onAttachedToWindow()
	 */
	@Override
	public void onWindowFocusChanged(boolean hasFocus) {
		super.onWindowFocusChanged(hasFocus);
		if (hasFocus) {
			View v;
			Rect r = new Rect();
			v = findViewById(R.id.useButton);
			v.getHitRect(r);
			mButtonIDs[USE_BUTTON] = mTouchView.addButton(r);
			v = findViewById(R.id.turboButton);
			r = new Rect();
			v.getHitRect(r);
			mButtonIDs[TURBO_BUTTON] = mTouchView.addButton(r);
			v = findViewById(R.id.enterButton);
			r = new Rect();
			v.getHitRect(r);
			mButtonIDs[ENTER_BUTTON] = mTouchView.addButton(r);
			v = findViewById(R.id.escapeButton);
			r = new Rect();
			v.getHitRect(r);
			mButtonIDs[ESCAPE_BUTTON] = mTouchView.addButton(r);
		}
	}

	@Override
	protected void onPause() {
        super.onPause();
    }

	@Override
    protected void onResume() {
        super.onResume();
    }

	@Override
	public void onInputChanged(float stickX, float stickY, int buttons) {
		StringBuilder sb = new StringBuilder("");
		if ((buttons & mButtonIDs[USE_BUTTON]) != 0)
			sb.append("USE ");
		if ((buttons & mButtonIDs[TURBO_BUTTON]) != 0)
			sb.append("TURBO ");
		if ((buttons & mButtonIDs[ENTER_BUTTON]) != 0)
			sb.append("ENTER ");
		if ((buttons & mButtonIDs[ESCAPE_BUTTON]) != 0)
			sb.append("ESCAPE");
		tv.setText(sb.toString());
	}
}