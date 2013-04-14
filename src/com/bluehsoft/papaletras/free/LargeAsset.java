package com.bluehsoft.papaletras.free;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

import android.content.Context;
import android.content.res.AssetManager;
import android.util.Log;

public class LargeAsset {

	private static final String TAG = "LargeAsset";

	private static String DB_PATH_PREFIX = "/data/data/";
	private static String DB_PATH_SUFFIX = "/databases/";	
	String m_filename;
	String m_piecename;
	Context m_context;
	
	LargeAsset(Context context, String filename, String piecename) {
		m_context = context;
		m_filename = filename;
		m_piecename = piecename;
	}
	
	public boolean file_exists() {
		File f = new File(m_filename);
		return f.exists();
	}
	
	public static String getDatabasePath(Context aContext, String databaseName) {
		return DB_PATH_PREFIX + aContext.getPackageName() + DB_PATH_SUFFIX
				+ databaseName;
	}		

	public static String getDataPath(Context aContext, String filename) {
		return DB_PATH_PREFIX + aContext.getPackageName() + "/"
				+ filename;
	}		
	
	public void build() throws IOException
	{
		// Path to the just created empty db
		 		
		
		
		// if the path doesn't exist first, create it
		File f = new File(DB_PATH_PREFIX + m_context.getPackageName()
				+ DB_PATH_SUFFIX);
		if (!f.exists())
			f.mkdir();
		
		f = new File(m_filename+".tmp");
		
		// Delete any previously existing tmp file
		if(f.exists())
			f.delete();
		
		// Open the empty db as the output stream
		String outFileName = m_filename+".tmp";
		Log.i(TAG, "Trying to copy local DB to : " + outFileName);
		
		OutputStream myOutput = new FileOutputStream(outFileName);
				
		AssetManager am = m_context.getAssets();
		String []Files = am.list("");
		Arrays.sort(Files);
		
		for (String filename : Files) {
			if(!filename.startsWith(m_piecename))
				continue;
			Log.i(TAG, "Building with file : " + filename);
			InputStream myInput = m_context.getAssets().open(filename);
		
			// transfer bytes from the inputfile to the outputfile
			byte[] buffer = new byte[1024];
			int length;
			while ((length = myInput.read(buffer)) > 0) {
				myOutput.write(buffer, 0, length);
			}
			myInput.close();
		}
		
		// Close the streams
		myOutput.flush();
		myOutput.close();
		f = new File(outFileName);
		f.renameTo(new File(m_filename));
		
		Log.i(TAG, "DB (" + m_filename + ") copied!");
	}
}
