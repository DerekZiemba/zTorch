package com.derekziemba.misc;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;

import android.content.Context;
import android.util.Log;

public class FileTools {
	

	public static int doesFileExist(Context context, String file){
		try{
			new Scanner(new File(file));
			return 1;
		}catch (FileNotFoundException e){
			return 0;
		}
	}
			
	public static String getFileString(Context context, String file) {
		String logfuncID = "getFileString()";
		String path = context.getFilesDir().getAbsolutePath();
		file = path+"/"+file;
		Log.i(logfuncID,"Loading File: " + file);
		File f = new File(file);
		int length = (int) f.length();
		byte[] bytes = new byte[length];
		FileInputStream in ;
		try {
			in = new FileInputStream(f);
			try {
			    in.read(bytes);
			} catch (IOException e) {
				Log.e(logfuncID,"IO READ EXCEPTION " + file);
			} finally {
			    try {
					in.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} catch (FileNotFoundException e) {
			Log.e(logfuncID,"FILE NOT FOUND EXCEPTION: " + file);
		}
		
		return new String(bytes);
		
		/*
		String[] arr = getFileData(context, file);
		StringBuilder sb = new StringBuilder();
		for(int i =0 ; i < arr.length ; i++) {
			sb.append(arr[i] + "\n") ;
		}
		return sb.toString();
		*/
	}
	
	public static String[] getFileData(Context context, String file){
		String logfuncID = "getFileData()";
		String path = context.getFilesDir().getAbsolutePath();
		String[] data = {""};
		file = path+file;
		Log.i(logfuncID,"Loading File: " + file + "\n");
		
		if( doesFileExist(context, file) == 1 ) {
			data = retrieveFile(context,file);
			Log.i(logfuncID,"Successfully Loaded File: " + file +"\n" );
		}
		else if( doesFileExist(context,file) == 0 ) {
			Log.w(logfuncID,"File Does Not Exist: " + file );
			
		}		
		return data;		
	}

	@SuppressWarnings("resource")
	private static String[] retrieveFile(Context context, String file) {
		String logfuncID = "retrieveFile()";
		String path = context.getFilesDir().getAbsolutePath();
		file = path+file;
		String[] data = {""};
		int i = 0;

		Scanner S;
		try {
			S = new Scanner(new File(file));
			data[i]= S.nextLine();	

			while( S.hasNextLine() == true)	{
				Log.v(logfuncID,data[i]);
				i++;
				data = Arrays.copyOf( data , i+1);
				data[i] = S.nextLine();
			}
			Log.v(logfuncID,data[i]);
			
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}	
		
		return data;
	}
	
	public static void writeFile(Context context, String file, String data){
		String logfuncID = "writeFile()";
		String path = context.getFilesDir().getAbsolutePath();
		file = path+"/"+file;
		Log.i(logfuncID,"Creating File: " + file);
		File f = new File(file);
		FileOutputStream stream;
		try {
			stream = new FileOutputStream(f);
			stream.write(data.getBytes());
			stream.close();
		} catch (FileNotFoundException e2) {
			// TODO Auto-generated catch block
			e2.printStackTrace();
			Log.e(logfuncID,"Failure Creating File: "+file+ "\n");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.e(logfuncID,"Failure Creating File: "+file+ "\n");
		} 

	}		

}








