package com.example.citysynth;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;

public class Constants extends Activity{
	public final static String DEBUG_TAG = "com.example.imagecapture";
	int fps, zoom, quality, imgsizew,imgsizeh, exec, update, reboot = 0;
	String focus = null;
	String whitebal = null;
	public static int fpm = 30;

	
	/*String dbserv_from = "/mnt/sdcard/cusp/files";
	String dbserv_to = "/home/cusp/mohitsharma44/citysynth/images"; 
	String dbserv_username = "mohitsharma44"; 
	String dbserv_privatekeypath = "/mnt/sdcard/id_rsa"; 
	String dbserv_host = "shell.cusp.nyu.edu";
	*/

	
	/* Setting up the Alarm to wake up the App after 'interval' seconds */
    /* This only happens Once in the entire life cycle of the program */
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
		PowerManager mgr = (PowerManager) getSystemService(Context.POWER_SERVICE);
		WakeLock wakeLock = mgr.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MyWakeLock");
		wakeLock.acquire();
        long t=System.currentTimeMillis();
		long interval=30*1000;
        
      //Intent anIntent=new Intent(this,MainActivity.class);
        //startActivity(anIntent);
        registerAlarmTrigger(t,interval);
    }
    
    
    
    protected void registerAlarmTrigger(long triggerAtTime, long interval){
    	AlarmManager am=(AlarmManager) getSystemService(Context.ALARM_SERVICE);
    	System.out.println("Waking up to Click picture");
    	Intent anIntent=new Intent(this,com.example.citysynth.MainActivity.class);
    	PendingIntent operation=PendingIntent.getActivity(this,0,anIntent,PendingIntent.FLAG_UPDATE_CURRENT);
     	am.setRepeating(AlarmManager.RTC_WAKEUP, triggerAtTime, interval, operation);
    }
}
