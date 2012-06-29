
package com.scheffsblend.smw;

import java.io.IOException;

import android.media.MediaPlayer;

/**
 * 
 * @author Clark Scheff
 *
 */
public class MusicPlayer {

	public static final int MAX_VOLUME = 128;
	
	private MediaPlayer mPlayer;
	private boolean mIsPlaying;
	private String mFileName;
	private float mVolume;
	
	public MusicPlayer() {
		mPlayer = new MediaPlayer();
		mIsPlaying = false;
		mFileName = "";
		mVolume = 1.0f;
	}
	
	/**
	 * Loads and plays the specified file
	 * @param fname - music file to play
	 * @throws IllegalArgumentException
	 * @throws IllegalStateException
	 * @throws IOException
	 */
	public void playFile(boolean playOnce, String fname) throws 
		IllegalArgumentException, 
		IllegalStateException, 
		IOException {
		if ( mIsPlaying ) {
			mPlayer.stop();
			mPlayer.reset();
		}
		mFileName = fname;
		mPlayer.setDataSource( fname );
		mPlayer.prepare();
		mPlayer.setLooping( !playOnce );
		mPlayer.setVolume(mVolume, mVolume);
		mPlayer.start();
		mIsPlaying = true;
	}
	
	/**
	 * Pauses playback of the music if it is playing
	 * @param pause
	 */
	public void pauseMusic( boolean pause ) {
		if ( mIsPlaying ) {
			if( pause )
				mPlayer.pause();
			else
				mPlayer.start();
		}
	}
	
	/**
	 * Stops playback of music if any is playing
	 */
	public void stop() {
		if ( mIsPlaying ) {
			mPlayer.stop();
			mPlayer.reset();
		}
		mIsPlaying = false;
	}
	
	/**
	 * Sets the volume of the music player
	 * @param level - value of 0 to 128
	 */
	public void setVolume( int level ) {
		if ( level > MAX_VOLUME )
			level = MAX_VOLUME;
		mVolume = (float)level / (float)MAX_VOLUME;
		if ( mIsPlaying ) {
			mPlayer.setVolume( mVolume, mVolume );
		}
	}
}
