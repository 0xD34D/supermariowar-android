package com.scheffsblend.smw;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import javax.microedition.khronos.egl.*;

import com.scheffsblend.multitouch.controller.MultiTouchView;
import com.scheffsblend.multitouch.controller.MultiTouchView.OnInputChangedListener;

import android.app.*;
import android.content.*;
import android.view.*;
import android.view.View.OnTouchListener;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.os.*;
import android.preference.PreferenceManager;
import android.util.Log;
import android.graphics.*;
import android.media.*;
import android.hardware.*;

import java.lang.*;
import java.util.HashMap;
import java.util.Iterator;


/**
    SDL Activity
*/
public class SDLActivity extends Activity
	implements OnInputChangedListener {

	private static final String TAG = "SMW-JAVA";
    // Main components
    private static SDLActivity mSingleton;
    private static SDLSurface mSurface;

    // Audio
    private static Thread mAudioThread;
    private static AudioTrack mAudioTrack;
    private static RelativeLayout mControls;
	private VirtualDPad mVStick = null;
	private int mVstickPosition = VirtualDPad.POS_CENTER;
	private int mVstickLastPos = VirtualDPad.POS_CENTER;
	private float mVstickX = 0;
	private float mVstickY = 0;
	private View mMultiTouchController;
	private MultiTouchView mTouchView;
	public static boolean mGameStarted = false;
	private HashMap<String, View> mOnScreenControls;
	private HashMap.Entry<String,View> touchedViews[] = new HashMap.Entry[2];
	private static MusicPlayer mMusicPlayer;
	private static Context ctx;
  
	private static final int USE_BUTTON = 0;
	private static final int TURBO_BUTTON = 1;
	private static final int ENTER_BUTTON = 2;
	private static final int ESCAPE_BUTTON = 3;
	private int[] mButtonIDs = new int[4];
	private int mCurrButtonsState = 0;
	private int mPrevButtonsState = 0;

    static {
        // load the game as a shared library
        System.loadLibrary("smw_jni");
    }
    
    // Setup
    protected void onCreate(Bundle savedInstanceState) {
        //Log.v("SDL", "onCreate()");
        super.onCreate(savedInstanceState);

        // get rid of the title bar and go fullscreen
        requestWindowFeature(Window.FEATURE_NO_TITLE);
		getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN);

        // So we can call stuff from static callbacks
        mSingleton = this;
        
        mMusicPlayer = new MusicPlayer();

        setControlSchemeLayout();
        
        mControls = (RelativeLayout)findViewById(R.id.controls);
        FrameLayout fl = (FrameLayout)findViewById(R.id.frameLayout);
        
        mTouchView = (MultiTouchView) findViewById(R.id.multiTouchView);
        mTouchView.setNormalizeValues(true);
        mTouchView.setOnInputChangeListener(this);
        //loadOnScreenControls();
        
        // Set up the surface
        mSurface = new SDLSurface(getApplication());
        fl.addView(mSurface);
        //setContentView(mSurface);
        mControls.bringToFront();
        SurfaceHolder holder = mSurface.getHolder();
        holder.setType(SurfaceHolder.SURFACE_TYPE_GPU);
    }

    // Events
    protected void onPause() {
        //Log.v("SDL", "onPause()");
        super.onPause();
    }

    protected void onResume() {
        //Log.v("SDL", "onResume()");
        super.onResume();
    }

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
			mVStick = new VirtualDPad();
		}
	}

    private void setControlSchemeLayout() {
        SharedPreferences prefs = PreferenceManager
    	.getDefaultSharedPreferences(getBaseContext());
        String s = prefs.getString( "layout", "1" );
        int scheme = Integer.parseInt(s);
    	setContentView(R.layout.controls_scheme1);
/*
    	switch ( scheme ) {
        case 1:
        	setContentView(R.layout.controls_scheme1);
        	break;
        case 2:
        	setContentView(R.layout.controls_scheme2);
        	break;
        case 3:
        	setContentView(R.layout.controls_scheme3);
        	break;
        case 4:
        	setContentView(R.layout.controls_scheme4);
        	break;
        case 5:
        	setContentView(R.layout.controls_scheme5);
        	break;
        case 6:
        	setContentView(R.layout.controls_scheme6);
        	break;
        case 7:
        	setContentView(R.layout.controls_scheme7);
        	break;
        case 8:
        	setContentView(R.layout.controls_scheme8);
        	break;
        }
*/
    }
    
    // Messages from the SDLMain thread
    static int COMMAND_CHANGE_TITLE = 1;

    // Handler for the messages
    Handler commandHandler = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.arg1 == COMMAND_CHANGE_TITLE) {
                setTitle((String)msg.obj);
            }
        }
    };

    // Send a message from the SDLMain thread
    void sendCommand(int command, Object data) {
        Message msg = commandHandler.obtainMessage();
        msg.arg1 = command;
        msg.obj = data;
        commandHandler.sendMessage(msg);
    }

    // C functions we call
    public static native void nativeInit();
    public static native void nativeQuit();
    public static native void onNativeResize(int x, int y, int format);
    public static native void onNativeKeyDown(int keycode);
    public static native void onNativeKeyUp(int keycode);
    public static native void onNativeTouch(int action, float x, 
                                            float y, float p);
    public static native void onNativeAccel(float x, float y, float z);
    public static native void nativeRunAudioThread();
    public static native void gfxInit(int width, int height);


    // Java functions called from C

    public static boolean createGLContext(int majorVersion, int minorVersion) {
        return mSurface.initEGL(majorVersion, minorVersion);
    }

    public static void flipBuffers() {
        mSurface.flipEGL();
    }

    public static void setActivityTitle(String title) {
        // Called from SDLMain() thread and can't directly affect the view
        mSingleton.sendCommand(COMMAND_CHANGE_TITLE, title);
    }

	public static Context getContext() {
		return mSingleton;
	}

    // Audio
    private static Object buf;
    
    public static Object audioInit(int sampleRate, boolean is16Bit, boolean isStereo, int desiredFrames) {
        int channelConfig = isStereo ? AudioFormat.CHANNEL_CONFIGURATION_STEREO : AudioFormat.CHANNEL_CONFIGURATION_MONO;
        int audioFormat = is16Bit ? AudioFormat.ENCODING_PCM_16BIT : AudioFormat.ENCODING_PCM_8BIT;
        int frameSize = (isStereo ? 2 : 1) * (is16Bit ? 2 : 1);
        
        Log.v("SDL", "SDL audio: wanted " + (isStereo ? "stereo" : "mono") + " " + (is16Bit ? "16-bit" : "8-bit") + " " + ((float)sampleRate / 1000f) + "kHz, " + desiredFrames + " frames buffer");
        
        // Let the user pick a larger buffer if they really want -- but ye
        // gods they probably shouldn't, the minimums are horrifyingly high
        // latency already
        desiredFrames = Math.max(desiredFrames, (AudioTrack.getMinBufferSize(sampleRate, channelConfig, audioFormat) + frameSize - 1) / frameSize);
        
        mAudioTrack = new AudioTrack(AudioManager.STREAM_MUSIC, sampleRate,
                channelConfig, audioFormat, desiredFrames * frameSize, AudioTrack.MODE_STREAM);
        
        audioStartThread();
        
        Log.v("SDL", "SDL audio: got " + ((mAudioTrack.getChannelCount() >= 2) ? "stereo" : "mono") + " " + ((mAudioTrack.getAudioFormat() == AudioFormat.ENCODING_PCM_16BIT) ? "16-bit" : "8-bit") + " " + ((float)mAudioTrack.getSampleRate() / 1000f) + "kHz, " + desiredFrames + " frames buffer");
        
        if (is16Bit) {
            buf = new short[desiredFrames * (isStereo ? 2 : 1)];
        } else {
            buf = new byte[desiredFrames * (isStereo ? 2 : 1)]; 
        }
        return buf;
    }
    
    public static void audioStartThread() {
        mAudioThread = new Thread(new Runnable() {
            public void run() {
                mAudioTrack.play();
                nativeRunAudioThread();
            }
        });
        
        // I'd take REALTIME if I could get it!
        mAudioThread.setPriority(Thread.MAX_PRIORITY);
        mAudioThread.start();
    }
    
    public static void audioWriteShortBuffer(short[] buffer) {
        for (int i = 0; i < buffer.length; ) {
            int result = mAudioTrack.write(buffer, i, buffer.length - i);
            if (result > 0) {
                i += result;
            } else if (result == 0) {
                try {
                    Thread.sleep(1);
                } catch(InterruptedException e) {
                    // Nom nom
                }
            } else {
                Log.w("SDL", "SDL audio: error return from write(short)");
                return;
            }
        }
    }
    
    public static void audioWriteByteBuffer(byte[] buffer) {
        for (int i = 0; i < buffer.length; ) {
            int result = mAudioTrack.write(buffer, i, buffer.length - i);
            if (result > 0) {
                i += result;
            } else if (result == 0) {
                try {
                    Thread.sleep(1);
                } catch(InterruptedException e) {
                    // Nom nom
                }
            } else {
                Log.w("SDL", "SDL audio: error return from write(short)");
                return;
            }
        }
    }

    public static void audioQuit() {
        if (mAudioThread != null) {
            try {
                mAudioThread.join();
            } catch(Exception e) {
                Log.v("SDL", "Problem stopping audio thread: " + e);
            }
            mAudioThread = null;

            //Log.v("SDL", "Finished waiting for audio thread");
        }

        if (mAudioTrack != null) {
            mAudioTrack.stop();
            mAudioTrack = null;
        }
    }

    /**
     * Adds all the touchscreen controls to the mOnScreenControls HashMap
     */
    private void loadOnScreenControls() {

		mOnScreenControls = new HashMap<String, View>();
    	mOnScreenControls.put("DPAD", this.findViewById(R.id.vstick));
    	mOnScreenControls.put("ENTER", this.findViewById(R.id.enterButton));
    	mOnScreenControls.put("ESCAPE", this.findViewById(R.id.escapeButton));
    	mOnScreenControls.put("USE", this.findViewById(R.id.useButton));
    	mOnScreenControls.put("TURBO", this.findViewById(R.id.turboButton));
    	
    	touchedViews[0] = touchedViews[1] = null;

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
				return processTouch(x, y, event, 0);
			case MotionEvent.ACTION_UP:
				x = event.getX(0);
				y = event.getY(0);
				return processTouch(x, y, event, 0);
			case MotionEvent.ACTION_POINTER_DOWN:
				x = event.getX(1);
				y = event.getY(1);
				return processTouch(x, y, event, 0);
			case MotionEvent.ACTION_POINTER_UP:
				x = event.getX();
				y = event.getY();
				return processTouch(x, y, event, 1);
			case MotionEvent.ACTION_MOVE:
				for(int i = 0; i < event.getPointerCount(); i++)
				{
					x = event.getX(i);
					y = event.getY(i);
					consumed |= processTouch(x, y, event, i);
				}
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
		if(mGameStarted == false)
			return false;
		
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
			HashMap.Entry<String, View> other = null;
			Iterator it = mOnScreenControls.entrySet().iterator();
			while (it.hasNext() && !foundView) {
				HashMap.Entry<String, View> entry = (HashMap.Entry<String, View>) it.next();
				controlId = entry.getKey();
				v = entry.getValue();
				v.getHitRect(outRect);
				if ( (x >= outRect.left && x <= outRect.right) && 
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
				v.getHitRect(outRect);
				mVstickX = x - outRect.left;
				mVstickY = y - outRect.top;
				mVStick.mWidth = outRect.right-outRect.left;
				mVStick.mHeight = outRect.bottom-outRect.top;
				mVstickPosition = mVStick.getPosition(mVstickX, mVstickY);
				int pos = VirtualDPad.POS_CENTER;
				if ((mVstickPosition & VirtualDPad.POS_UP) != 0) {
					SDLActivity.onNativeKeyDown(KeyCodes.KEY_UPARROW);
					pos |= VirtualDPad.POS_UP;
				}
				if ((mVstickPosition & VirtualDPad.POS_LEFT) != 0) {
					SDLActivity.onNativeKeyDown(KeyCodes.KEY_LEFTARROW);
					pos |= VirtualDPad.POS_LEFT;
				}
				if ((mVstickPosition & VirtualDPad.POS_DOWN) != 0) {
					SDLActivity.onNativeKeyDown(KeyCodes.KEY_DOWNARROW);
					pos |= VirtualDPad.POS_DOWN;
				}
				if ((mVstickPosition & VirtualDPad.POS_RIGHT) != 0) {
					SDLActivity.onNativeKeyDown(KeyCodes.KEY_RIGHTARROW);
					pos |= VirtualDPad.POS_RIGHT;
				}
				mVstickLastPos = pos;
				mVstickPosition = pos;
			} else if (controlId.contains("ENTER")) {
				SDLActivity.onNativeKeyDown(KeyCodes.KEY_ENTER);
			} else if (controlId.contains("ESCAPE")) {
				SDLActivity.onNativeKeyDown(KeyCodes.KEY_ESCAPE);
			} else if (controlId.contains("TURBO")) {
				SDLActivity.onNativeKeyDown(KeyCodes.KEY_RCTRL);
			} else if (controlId.contains("USE")) {
				SDLActivity.onNativeKeyDown(KeyCodes.KEY_RSHIFT);
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
				if ((mVstickLastPos & VirtualDPad.POS_UP) != 0)
					SDLActivity.onNativeKeyUp(KeyCodes.KEY_UPARROW);
				if ((mVstickLastPos & VirtualDPad.POS_LEFT) != 0)
					SDLActivity.onNativeKeyUp(KeyCodes.KEY_LEFTARROW);
				if ((mVstickLastPos & VirtualDPad.POS_DOWN) != 0)
					SDLActivity.onNativeKeyUp(KeyCodes.KEY_DOWNARROW);
				if ((mVstickLastPos & VirtualDPad.POS_RIGHT) != 0)
					SDLActivity.onNativeKeyUp(KeyCodes.KEY_RIGHTARROW);

				mVstickLastPos = VirtualDPad.POS_CENTER;
				mVstickPosition = VirtualDPad.POS_CENTER;
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
				v.getHitRect(outRect);
				mVstickX = x - outRect.left;
				mVstickY = y - outRect.top;
				mVstickPosition = mVStick.getPosition(mVstickX, mVstickY);
				if (mVstickPosition == mVstickLastPos)
					return true;
				else {
					// get the bits that changed
					int diff = (mVstickPosition ^ mVstickLastPos) & 0x0F;
					// get the bits that changed and are current
					int curr = (mVstickPosition & diff);
					// get the bits that changed and are previous
					int prev = (mVstickLastPos & diff);

					// first check which direction(s) is no longer being
					// pressed from the previous time
					// and send a keyup event
					if ((prev & VirtualDPad.POS_UP) != 0)
						SDLActivity.onNativeKeyUp(KeyCodes.KEY_UPARROW);
					if ((prev & VirtualDPad.POS_LEFT) != 0)
						SDLActivity.onNativeKeyUp(KeyCodes.KEY_LEFTARROW);
					if ((prev & VirtualDPad.POS_DOWN) != 0)
						SDLActivity.onNativeKeyUp(KeyCodes.KEY_DOWNARROW);
					if ((prev & VirtualDPad.POS_RIGHT) != 0)
						SDLActivity.onNativeKeyUp(KeyCodes.KEY_RIGHTARROW);
					// now check which direction(s) is current and was not
					// pressed the previous time
					// and send the keydown event.
					if ((curr & VirtualDPad.POS_UP) != 0)
						SDLActivity.onNativeKeyDown(KeyCodes.KEY_UPARROW);
					if ((curr & VirtualDPad.POS_LEFT) != 0)
						SDLActivity.onNativeKeyDown(KeyCodes.KEY_LEFTARROW);
					if ((curr & VirtualDPad.POS_DOWN) != 0)
						SDLActivity.onNativeKeyDown(KeyCodes.KEY_DOWNARROW);
					if ((curr & VirtualDPad.POS_RIGHT) != 0)
						SDLActivity.onNativeKeyDown(KeyCodes.KEY_RIGHTARROW);
						if (mVstickPosition != mVstickLastPos)
						mVstickLastPos = mVstickPosition;
				}
			}
			return true;
		}
		
		return false;
	}

	/**
	 * Start background music callback
	 * @param name
	 */
	@SuppressWarnings("unused")
	private static void OnStartMusic(boolean playOnce, String name) {
		Log.i(TAG, "Play music fired: " + name);
		try {
			mMusicPlayer.playFile( playOnce, name );
		} catch (Exception e) { 
			Log.e( TAG, "Unable to load music: " + name );
		}
	}

	/**
	 * Stop bg music
	 */
	@SuppressWarnings("unused")
	private static void OnStopMusic() {
		Log.i(TAG, "OnStopMusic fired!");
		mMusicPlayer.stop();
	}
	
	/**
	 * Called when SMW wishes to pause the music
	 * @param pause - true if it is to be paused
	 */
	@SuppressWarnings("unused")
	private static void OnPauseMusic(boolean pause) {
		Log.i(TAG, "OnPauseMusic fired!");
		mMusicPlayer.pauseMusic( pause );
	}
	
	/**
	 * Called when SMW wishes to adjust the music volume
	 * @param level - level 
	 */
	@SuppressWarnings("unused")
	private static void OnSetVolume( int level ) {
		Log.i(TAG, "OnSetVolume fired!");
		mMusicPlayer.setVolume( level );
	}

	@Override
	public void onInputChanged(float stickX, float stickY, int buttons) {
		mVstickPosition = mVStick.getPosition(stickX, stickY);
		if (mVstickPosition != mVstickLastPos) {
			// get the bits that changed
			int diff = (mVstickPosition ^ mVstickLastPos) & 0x0F;
			// get the bits that changed and are current
			int curr = (mVstickPosition & diff);
			// get the bits that changed and are previous
			int prev = (mVstickLastPos & diff);

			// first check which direction(s) is no longer being
			// pressed from the previous time
			// and send a keyup event
			if ((prev & VirtualDPad.POS_UP) != 0)
				SDLActivity.onNativeKeyUp(KeyCodes.KEY_UPARROW);
			if ((prev & VirtualDPad.POS_LEFT) != 0)
				SDLActivity.onNativeKeyUp(KeyCodes.KEY_LEFTARROW);
			if ((prev & VirtualDPad.POS_DOWN) != 0)
				SDLActivity.onNativeKeyUp(KeyCodes.KEY_DOWNARROW);
			if ((prev & VirtualDPad.POS_RIGHT) != 0)
				SDLActivity.onNativeKeyUp(KeyCodes.KEY_RIGHTARROW);
			// now check which direction(s) is current and was not
			// pressed the previous time
			// and send the keydown event.
			if ((curr & VirtualDPad.POS_UP) != 0)
				SDLActivity.onNativeKeyDown(KeyCodes.KEY_UPARROW);
			if ((curr & VirtualDPad.POS_LEFT) != 0)
				SDLActivity.onNativeKeyDown(KeyCodes.KEY_LEFTARROW);
			if ((curr & VirtualDPad.POS_DOWN) != 0)
				SDLActivity.onNativeKeyDown(KeyCodes.KEY_DOWNARROW);
			if ((curr & VirtualDPad.POS_RIGHT) != 0)
				SDLActivity.onNativeKeyDown(KeyCodes.KEY_RIGHTARROW);
				if (mVstickPosition != mVstickLastPos)
				mVstickLastPos = mVstickPosition;
		}
		
		int changed = mPrevButtonsState ^ buttons;
		if ((changed & mButtonIDs[TURBO_BUTTON]) != 0) {
			if ((buttons & mButtonIDs[TURBO_BUTTON]) != 0)
				SDLActivity.onNativeKeyDown(KeyCodes.KEY_RCTRL);
			else
				SDLActivity.onNativeKeyUp(KeyCodes.KEY_RCTRL);
		}
			
		if ((changed & mButtonIDs[USE_BUTTON]) != 0) {
			if ((buttons & mButtonIDs[USE_BUTTON]) != 0)
				SDLActivity.onNativeKeyDown(KeyCodes.KEY_RSHIFT);
			else
				SDLActivity.onNativeKeyUp(KeyCodes.KEY_RSHIFT);
		}
			
		if ((changed & mButtonIDs[ENTER_BUTTON]) != 0) {
			if ((buttons & mButtonIDs[ENTER_BUTTON]) != 0)
				SDLActivity.onNativeKeyDown(KeyCodes.KEY_ENTER);
			else
				SDLActivity.onNativeKeyUp(KeyCodes.KEY_ENTER);
		}
			
		if ((changed & mButtonIDs[ESCAPE_BUTTON]) != 0) {
			if ((buttons & mButtonIDs[ESCAPE_BUTTON]) != 0)
				SDLActivity.onNativeKeyDown(KeyCodes.KEY_ESCAPE);
			else
				SDLActivity.onNativeKeyUp(KeyCodes.KEY_ESCAPE);
		}
		
		mPrevButtonsState = buttons;
	}
}

/**
    Simple nativeInit() runnable
*/
class SDLMain implements Runnable {
    public void run() {
        // Runs SDL_main()
        SDLActivity.mGameStarted = true;
    	SDLActivity.nativeInit();
        
        //Log.v("SDL", "SDL thread terminated");
    }
}


/**
    SDLSurface. This is what we draw on, so we need to know when it's created
    in order to do anything useful. 

    Because of this, that's where we set up the SDL thread
*/
class SDLSurface extends SurfaceView implements SurfaceHolder.Callback, 
    View.OnKeyListener, View.OnTouchListener, SensorEventListener  {

    // This is what SDL runs in. It invokes SDL_main(), eventually
    private Thread mSDLThread;    
    
    // EGL private objects
    private EGLContext  mEGLContext;
    private EGLSurface  mEGLSurface;
    private EGLDisplay  mEGLDisplay;

    // Sensors
    private static SensorManager mSensorManager;

    // Startup    
    public SDLSurface(Context context) {
        super(context);
        getHolder().addCallback(this); 
    
        setFocusable(true);
        setFocusableInTouchMode(true);
        requestFocus();
        setOnKeyListener(this); 
        setOnTouchListener(this);   

        mSensorManager = (SensorManager)context.getSystemService("sensor");  
    }

    // Called when we have a valid drawing surface
    public void surfaceCreated(SurfaceHolder holder) {
        //Log.v("SDL", "surfaceCreated()");

        enableSensor(Sensor.TYPE_ACCELEROMETER, true);
    }

    // Called when we lose the surface
    public void surfaceDestroyed(SurfaceHolder holder) {
        //Log.v("SDL", "surfaceDestroyed()");

        // Send a quit message to the application
        SDLActivity.nativeQuit();

        // Now wait for the SDL thread to quit
        if (mSDLThread != null) {
            try {
                mSDLThread.join();
            } catch(Exception e) {
                Log.v("SDL", "Problem stopping thread: " + e);
            }
            mSDLThread = null;

            //Log.v("SDL", "Finished waiting for SDL thread");
        }

        enableSensor(Sensor.TYPE_ACCELEROMETER, false);
    }

    // Called when the surface is resized
    public void surfaceChanged(SurfaceHolder holder,
                               int format, int width, int height) {
        //Log.v("SDL", "surfaceChanged()");

        int sdlFormat = 0x85151002; // SDL_PIXELFORMAT_RGB565 by default
        switch (format) {
        case PixelFormat.A_8:
            Log.v("SDL", "pixel format A_8");
            break;
        case PixelFormat.LA_88:
            Log.v("SDL", "pixel format LA_88");
            break;
        case PixelFormat.L_8:
            Log.v("SDL", "pixel format L_8");
            break;
        case PixelFormat.RGBA_4444:
            Log.v("SDL", "pixel format RGBA_4444");
            sdlFormat = 0x85421002; // SDL_PIXELFORMAT_RGBA4444
            break;
        case PixelFormat.RGBA_5551:
            Log.v("SDL", "pixel format RGBA_5551");
            sdlFormat = 0x85441002; // SDL_PIXELFORMAT_RGBA5551
            break;
        case PixelFormat.RGBA_8888:
            Log.v("SDL", "pixel format RGBA_8888");
            sdlFormat = 0x86462004; // SDL_PIXELFORMAT_RGBA8888
            break;
        case PixelFormat.RGBX_8888:
            Log.v("SDL", "pixel format RGBX_8888");
            sdlFormat = 0x86262004; // SDL_PIXELFORMAT_RGBX8888
            break;
        case PixelFormat.RGB_332:
            Log.v("SDL", "pixel format RGB_332");
            sdlFormat = 0x84110801; // SDL_PIXELFORMAT_RGB332
            break;
        case PixelFormat.RGB_565:
            Log.v("SDL", "pixel format RGB_565");
            sdlFormat = 0x85151002; // SDL_PIXELFORMAT_RGB565
            break;
        case PixelFormat.RGB_888:
            Log.v("SDL", "pixel format RGB_888");
            // Not sure this is right, maybe SDL_PIXELFORMAT_RGB24 instead?
            sdlFormat = 0x86161804; // SDL_PIXELFORMAT_RGB888
            break;
        default:
            Log.v("SDL", "pixel format unknown " + format);
            break;
        }
        SDLActivity.onNativeResize(width, height, sdlFormat);

        // Now start up the C app thread
        if (mSDLThread == null) {
            mSDLThread = new Thread(new SDLMain(), "SDLThread"); 
            mSDLThread.start();       
        }
    }

    // unused
    public void onDraw(Canvas canvas) {}


    // EGL functions
    public boolean initEGL(int majorVersion, int minorVersion) {
        Log.v("SDL", "Starting up OpenGL ES " + majorVersion + "." + minorVersion);

        try {
            EGL10 egl = (EGL10)EGLContext.getEGL();

            EGLDisplay dpy = egl.eglGetDisplay(EGL10.EGL_DEFAULT_DISPLAY);

            int[] version = new int[2];
            egl.eglInitialize(dpy, version);

            int EGL_OPENGL_ES_BIT = 1;
            int EGL_OPENGL_ES2_BIT = 4;
            int renderableType = 0;
            if (majorVersion == 2) {
                renderableType = EGL_OPENGL_ES2_BIT;
            } else if (majorVersion == 1) {
                renderableType = EGL_OPENGL_ES_BIT;
            }
            int[] configSpec = {
                //EGL10.EGL_DEPTH_SIZE,   16,
                EGL10.EGL_RENDERABLE_TYPE, renderableType,
                EGL10.EGL_NONE
            };
            EGLConfig[] configs = new EGLConfig[1];
            int[] num_config = new int[1];
            if (!egl.eglChooseConfig(dpy, configSpec, configs, 1, num_config) || num_config[0] == 0) {
                Log.e("SDL", "No EGL config available");
                return false;
            }
            EGLConfig config = configs[0];

            EGLContext ctx = egl.eglCreateContext(dpy, config, EGL10.EGL_NO_CONTEXT, null);
            if (ctx == EGL10.EGL_NO_CONTEXT) {
                Log.e("SDL", "Couldn't create context");
                return false;
            }

            EGLSurface surface = egl.eglCreateWindowSurface(dpy, config, this, null);
            if (surface == EGL10.EGL_NO_SURFACE) {
                Log.e("SDL", "Couldn't create surface");
                return false;
            }

            if (!egl.eglMakeCurrent(dpy, surface, surface, ctx)) {
                Log.e("SDL", "Couldn't make context current");
                return false;
            }

            mEGLContext = ctx;
            mEGLDisplay = dpy;
            mEGLSurface = surface;

        } catch(Exception e) {
            Log.v("SDL", e + "");
            for (StackTraceElement s : e.getStackTrace()) {
                Log.v("SDL", s.toString());
            }
        }

        return true;
    }

    // EGL buffer flip
    public void flipEGL() {
        try {
            EGL10 egl = (EGL10)EGLContext.getEGL();

            egl.eglWaitNative(EGL10.EGL_NATIVE_RENDERABLE, null);

            // drawing here

            egl.eglWaitGL();

            egl.eglSwapBuffers(mEGLDisplay, mEGLSurface);

            
        } catch(Exception e) {
            Log.v("SDL", "flipEGL(): " + e);
            for (StackTraceElement s : e.getStackTrace()) {
                Log.v("SDL", s.toString());
            }
        }
    }

    // Key events
    public boolean onKey(View  v, int keyCode, KeyEvent event) {

        if (event.getAction() == KeyEvent.ACTION_DOWN) {
            //Log.v("SDL", "key down: " + keyCode);
            // capture the back button so we can quit
        	switch ( keyCode ) {
        	case KeyEvent.KEYCODE_BACK:
        		SDLActivity.nativeQuit();
        		break;
        	case KeyEvent.KEYCODE_VOLUME_DOWN:
        	case KeyEvent.KEYCODE_VOLUME_UP:
        		return false;
        	default:
        		SDLActivity.onNativeKeyDown(keyCode);
        		break;
        	}
            return true;
        }
        else if (event.getAction() == KeyEvent.ACTION_UP) {
            //Log.v("SDL", "key up: " + keyCode);
        	switch ( keyCode ) {
        	case KeyEvent.KEYCODE_VOLUME_DOWN:
        	case KeyEvent.KEYCODE_VOLUME_UP:
        		return false;
        	default:
        		SDLActivity.onNativeKeyUp(keyCode);
        		break;
        	}
            return true;
        }
        
        return false;
    }

    // Touch events
    public boolean onTouch(View v, MotionEvent event) {
    
        int action = event.getAction();
        float x = event.getX();
        float y = event.getY();
        float p = event.getPressure();

        // TODO: Anything else we need to pass?        
        SDLActivity.onNativeTouch(action, x, y, p);
        return true;
    }

    // Sensor events
    public void enableSensor(int sensortype, boolean enabled) {
        // TODO: This uses getDefaultSensor - what if we have >1 accels?
        if (enabled) {
            mSensorManager.registerListener(this, 
                            mSensorManager.getDefaultSensor(sensortype), 
                            SensorManager.SENSOR_DELAY_GAME, null);
        } else {
            mSensorManager.unregisterListener(this, 
                            mSensorManager.getDefaultSensor(sensortype));
        }
    }
    
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO
    }

    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            SDLActivity.onNativeAccel(event.values[0],
                                      event.values[1],
                                      event.values[2]);
        }
    }
}

