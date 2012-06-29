package com.scheffsblend.smw;

import com.scheffsblend.smw.Preferences.SMWSettings;
import com.scheffsblend.util.Utility;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.media.AudioManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.KeyEvent;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.AsyncTask;

public class MainMenuActivity extends Activity {

	private Button mLaunchButton;
	private Button mInstructionsButton;
	private Button mSettingsButton;
	private AnimatedImageView mLogo;
    public static final int DIALOG_DOWNLOAD_PROGRESS = 0;
    public static final int DIALOG_UNZIP_PROGRESS = 1;
    private ProgressDialog mProgressDialog;
    private ProgressDialog mUnzipProgressDialog;
	
    protected void onCreate( Bundle savedInstanceState ) {
        super.onCreate( savedInstanceState );

        // get rid of the title bar and go fullscreen
        requestWindowFeature( Window.FEATURE_NO_TITLE );
		getWindow().setFlags( WindowManager.LayoutParams.FLAG_FULLSCREEN,
				WindowManager.LayoutParams.FLAG_FULLSCREEN );

        setContentView( R.layout.main_menu );
        
        mLogo = (AnimatedImageView)findViewById(R.id.smwLogo);
        mLaunchButton = (Button)findViewById( R.id.launchGame );
        mInstructionsButton = (Button)findViewById( R.id.instructions );
        mSettingsButton = (Button)findViewById( R.id.settings );
		Typeface type = Typeface.createFromAsset( this.getAssets(), "fonts/jellybelly.ttf" );
		mLaunchButton.setTypeface( type );
		mInstructionsButton.setTypeface( type );
		mSettingsButton.setTypeface( type );
		mLaunchButton.setEnabled( false );
        mLaunchButton.setOnClickListener( new OnClickListener() {

			@Override
			public void onClick(View v) {
				mLogo.stopAnimating();
				Intent intent = new Intent(getApplicationContext(), SDLActivity.class);
				startActivity( intent );
			}
        	
        });
        
        mInstructionsButton.setOnClickListener( new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(), InstructionsActivity.class);
				startActivity( intent );
			}
        });
        
        mSettingsButton.setOnClickListener( new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent(getApplicationContext(), SMWSettings.class);
				startActivity( intent );
			}
        });
        
        if( Utility.gameDataInstalled() == false ) {
        	if ( Utility.gameDataFolderExists() == false )
        		Utility.createGameDataFolder();
        	startDownload();
        } else
        	mLaunchButton.setEnabled( true );
    }

    // Events
    protected void onPause() {
        super.onPause();
    }

    protected void onResume() {
        super.onResume();
    }

    private void startDownload() {
        String url = Utility.GAME_DATA_URL + Utility.GAME_DATA_FILE;
        new DownloadFileAsync().execute(url);
    }
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case DIALOG_DOWNLOAD_PROGRESS:
                mProgressDialog = new ProgressDialog( this );
                mProgressDialog.setIcon( R.drawable.icon );
                mProgressDialog.setMessage( "Downloading game data.." );
                mProgressDialog.setProgressStyle( ProgressDialog.STYLE_HORIZONTAL );
                mProgressDialog.setCancelable( false );
                mProgressDialog.show();
                return mProgressDialog;
            case DIALOG_UNZIP_PROGRESS:
                mUnzipProgressDialog = new ProgressDialog( this );
                mUnzipProgressDialog.setIcon( R.drawable.icon );
                mUnzipProgressDialog.setMessage( "Extracting game data.." );
                mUnzipProgressDialog.setProgressStyle( ProgressDialog.STYLE_SPINNER );
                mUnzipProgressDialog.setCancelable( false );
                mUnzipProgressDialog.show();
                return mUnzipProgressDialog;
            default:
                return null;
        }
    }
    
    class DownloadFileAsync extends AsyncTask<String, String, String> {
        
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showDialog( DIALOG_DOWNLOAD_PROGRESS );
        }

        @Override
        protected String doInBackground(String... aurl) {
            int count;

            try {
                URL url = new URL(aurl[0]);
                URLConnection conexion = url.openConnection();
                conexion.connect();

                int lenghtOfFile = conexion.getContentLength();
                Log.d( "ANDRO_ASYNC", "Lenght of file: " + lenghtOfFile );
                mProgressDialog.setMax( lenghtOfFile );

                InputStream input = new BufferedInputStream( url.openStream() );
                OutputStream output = new FileOutputStream( Utility.getGameDataFolder() + 
                		"/" + Utility.GAME_DATA_FILE );

                byte data[] = new byte[1024];

                long total = 0;

                while ((count = input.read( data )) != -1) {
                    total += count;
                    publishProgress( ""+total );
                    output.write( data, 0, count );
                }

                output.flush();
                output.close();
                input.close();
            } catch (Exception e) {}
            return null;

        }
        protected void onProgressUpdate(String... progress) {
             mProgressDialog.setProgress(Integer.parseInt(progress[0]));
        }

        @Override
        protected void onPostExecute(String unused) {
            //mProgressDialog.setMessage( "Extracting files..." );
            //extractGameData();
        	dismissDialog( DIALOG_DOWNLOAD_PROGRESS );
            new UnzipFileAsync().execute();
        }
    }

    class UnzipFileAsync extends AsyncTask<String, String, String> {
        
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showDialog( DIALOG_UNZIP_PROGRESS );
        }

        @Override
        protected String doInBackground(String... params) {
        	String dataPath = Utility.getGameDataFolder();
        	String zipFileName = dataPath + "/" + Utility.GAME_DATA_FILE;
            try {
    			/*
    			 * Extract entries while creating required
    			 * sub-directories
    			 */
    			File fSourceZip = new File(zipFileName);
    			ZipFile zipFile = new ZipFile(fSourceZip);
    			Enumeration e = zipFile.entries();
     
    			while(e.hasMoreElements())
    			{
    				ZipEntry entry = (ZipEntry)e.nextElement();
    				File destinationFilePath = new File(Utility.getExternalDataFolder(),entry.getName());
     
    				//create directories if required.
    				destinationFilePath.getParentFile().mkdirs();
     
    				//if the entry is directory, leave it. Otherwise extract it.
    				if(entry.isDirectory())
    				{
    					continue;
    				}
    				else
    				{
    					final String msg = "Extracting " + destinationFilePath;
    					Log.i("UNZIP", "Extracting " + destinationFilePath);

    					/*
    					 * Get the InputStream for current entry
    					 * of the zip file using
    					 * 
    					 * InputStream getInputStream(Entry entry) method.
    					 */
    					BufferedInputStream bis = new BufferedInputStream(zipFile
    															.getInputStream(entry));
     
    					int b;
    					byte buffer[] = new byte[1024];
     
    					/*
    					 * read the current entry from the zip file, extract it
    					 * and write the extracted file.
    					 */
    					FileOutputStream fos = new FileOutputStream(destinationFilePath);
    					BufferedOutputStream bos = new BufferedOutputStream(fos,
    									1024);
     
    					while ((b = bis.read(buffer, 0, 1024)) != -1) {
    							bos.write(buffer, 0, b);
    					}
     
    					//flush the output stream and close it.
    					bos.flush();
    					bos.close();
     
    					//close the input stream.
    					bis.close();
    				}
    			}
            } catch (Exception e) {
            	
            }
			return null;
        }
        protected void onProgressUpdate(String... progress) {
             mUnzipProgressDialog.setProgress(Integer.parseInt(progress[0]));
        }

        @Override
        protected void onPostExecute(String unused) {
        	dismissDialog( DIALOG_UNZIP_PROGRESS );
        	
        	String dataPath = Utility.getGameDataFolder();
        	String zipFileName = dataPath + "/" + Utility.GAME_DATA_FILE;
        	// delete the zip file
        	File zipFile = new File( zipFileName );
        	zipFile.delete();
        	
        	// create .nomedia and .swmdata files
        	try {
            	File f = new File(dataPath + "/" + Utility.NO_MEDIA_SCANNER_FILE);
				f.createNewFile();
				f = new File(dataPath + "/" + Utility.DATA_INSTALLED_FILE);
				f.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			// now enable the launch game button so the player can get on with it
			mLaunchButton.setEnabled(true);
        }
    }

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		super.onKeyDown(keyCode, event);
		if(event.getAction() == KeyEvent.ACTION_DOWN) {
			switch(event.getKeyCode()) {
			case KeyEvent.KEYCODE_MENU:
				Intent intent = new Intent(getApplicationContext(), SMWSettings.class);
				startActivity( intent );
				return true;
			}
		}
		return true;
	}
}
