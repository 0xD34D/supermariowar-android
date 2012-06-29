/**
 * 
 */
package com.scheffsblend.util;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import android.app.ProgressDialog;
import android.os.Environment;
import android.util.Log;

/**
 * @author Clark Scheff
 *
 */
public class Utility {
	public static final String GAME_DATA_DIR = "/smw";
	public static final String DATA_INSTALLED_FILE = ".smwdata";
	public static final String NO_MEDIA_SCANNER_FILE = ".nomedia";
	public static final String GAME_DATA_URL = "http://supermariowar-android.googlecode.com/files/";
	public static final String GAME_DATA_FILE = "smw_data.zip";
	
	private static final String TAG = "SMW_UTIL";
	
	/**
	 * Gets the location of the external storage. Not all devices mount to
	 * /sdcard so we need to get the location from the system. 
	 * @return - location of external storage.
	 */
	public static String getExternalDataFolder() {
		String dir = Environment.getExternalStorageDirectory().toString();
		
		return dir;
	}
	
	/**
	 * Gets the location of where the game data will be stored
	 * @return
	 */
	public static String getGameDataFolder() {
		return getExternalDataFolder() + GAME_DATA_DIR;
	}
	
	/**
	 * Checks whether the folder GAME_DATA_DIR exists in external storage
	 * @return - true if the game data folder exists
	 */
	public static boolean gameDataFolderExists() {
		return ( new File( getGameDataFolder() ).exists() );
	}
	
	/**
	 * Determines if the game data has been isntalled on the SD card
	 * @return - true if DATA_INSTALLED_FILE exists.
	 */
	public static boolean gameDataInstalled() {
		if ( gameDataFolderExists() == false )
			return false;
		
		return ( new File(getGameDataFolder()+
				"/" + DATA_INSTALLED_FILE).exists() );
	}
	
	/**
	 * Creates the game data folder
	 * @throws SecurityException
	 */
	public static void createGameDataFolder() throws SecurityException {
		File dataPath = new File( getGameDataFolder() );
		
		dataPath.mkdir();
	}

	/**
	 * Extracts the zipfile strZipFile into the path zipPath
	 * @param zipPath - path to extract files to
	 * @param strZipFile - zip file to extract
	 * @param pd - a progress dialog to update to, pass null to not use
	 */
	public static void unzip(String zipPath, String strZipFile, ProgressDialog pd) {
		 
		try
		{
			/*
			 * Extract entries while creating required
			 * sub-directories
			 */
			File fSourceZip = new File(strZipFile);
			ZipFile zipFile = new ZipFile(fSourceZip);
			Enumeration e = zipFile.entries();
 
			while(e.hasMoreElements())
			{
				ZipEntry entry = (ZipEntry)e.nextElement();
				File destinationFilePath = new File(zipPath,entry.getName());
 
				//create directories if required.
				destinationFilePath.getParentFile().mkdirs();
 
				//if the entry is directory, leave it. Otherwise extract it.
				if(entry.isDirectory())
				{
					continue;
				}
				else
				{
					Log.i(TAG, "Extracting " + destinationFilePath);
					if( pd != null ) {
						pd.setMessage("Extracting " + destinationFilePath );
					}
 
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
		}
		catch(IOException ioe)
		{
			Log.e(TAG, "IOError :" + ioe);
		}
 	}
}
