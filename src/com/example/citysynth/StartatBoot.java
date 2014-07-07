package com.example.citysynth;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;

public class StartatBoot extends BroadcastReceiver {
	
	@Override
	public void onReceive(final Context context, Intent intent) {
		// TODO Auto-generated method stub
		final Handler handler = new Handler();
		handler.postDelayed(new Runnable(){
			public void run() {
				//Enter code here
				Intent i = new Intent(context, Constants.class);
				i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
				context.startActivity(i);
			}
			},5000);
	}

}