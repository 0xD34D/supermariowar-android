/**
 * 
 */
package com.scheffsblend.smw.Preferences;

import com.scheffsblend.smw.R;
import com.scheffsblend.smw.R.xml;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * @author Clark Scheff
 *
 */
public class SMWSettings extends PreferenceActivity {

	@Override
	protected void onCreate( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );
		addPreferencesFromResource( R.xml.settings );
	}
}
