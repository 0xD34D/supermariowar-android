package com.scheffsblend.smw;

import android.app.Activity;
import android.os.Bundle;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.WebSettings;
import android.webkit.WebView;

public class InstructionsActivity extends Activity {
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );

        // get rid of the title bar and go fullscreen
        requestWindowFeature( Window.FEATURE_NO_TITLE );
		getWindow().setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN );

		WebView wv = new WebView(this);
		WebSettings ws = wv.getSettings();
		ws.setBuiltInZoomControls(true);
        wv.loadUrl( "file:///android_asset/instructions.html" );
        setContentView( wv );
    }

    // Events
    protected void onPause() {
        super.onPause();
    }

    protected void onResume() {
        super.onResume();
    }
}
