package com.example.citysynth;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONObject;

import com.zehon.BatchTransferProgressDefault;
import com.zehon.FileTransferClient;
import com.zehon.FileTransferStatus;
import com.zehon.exception.FileTransferException;
import com.zehon.sftp.SFTP;
import com.zehon.sftp.SFTPClient;

import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.TrafficStats;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.StrictMode;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;
import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
/*REMARKS:
 * 1. /mnt/sdcard = /storage/sdcard0
 * 2. Works with almost all devices except samsung galaxy s3.
*/
public class MainActivity extends Activity {
	Constants constants = new Constants();
	//Step2_var
	String file_loc = null;
	String mainconfigfilename = "main_config.txt";
	String uploadconfigfilename = "upload_config.txt";
	File home = Environment.getExternalStorageDirectory();
	String cntrlserv_host = "shell.cusp.nyu.edu";
	String username = "mohitsharma44";
	String cntrlserv_privateKeyPath = "/storage/sdcard0/id_rsa2";
	String sftpFromFolder = "/home/cusp/mohitsharma44/citysynth/";		
	public static String dbserv_from, dbserv_to, dbserv_username, dbserv_privatekeypath, dbserv_host = null;
	public static int ulupload = 0;
	//Step4_var
	int temp = 0;
	//Step6_var
	int arclen = 0;
	private Camera camera;
	Handler aHandler = new Handler();
	public static String storagedir, savefiles, key = null;
	//Step7_var
	String wb,f = null;
	//Step8_var
	String ip = null;
	String str = null;
	PrintWriter out = null;
	//Step9_var
	//Step10_var
	//Step11_var
	long BeforeTime, TotalRxBeforeTest, TotalTxBeforeTest,TotalRxAfterTest, TotalTxAfterTest, AfterTime = 0;
	double TimeDifference, rxDiff, txDiff, rxBPS, txBPS = 0;
	String speed[];
	File myFile, myarchFile = null;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		System.out.println("Version 0.7");
		try {
			/*
			 * WifiManager wifimanager = (WifiManager)
			 * getSystemService(WIFI_SERVICE); wifimanager.setWifiEnabled(true);
			 * WifiConfiguration wificonfig = new WifiConfiguration(); String
			 * networkSSID = "nyu"; wificonfig.SSID = "\""+networkSSID+"\"";
			 * wificonfig.status = WifiConfiguration.Status.ENABLED;
			 */
			// Step 1: Get Connected to Wifi
			// getconnected();
			// Step 6: Check Wifi and length of archive folder
			savefiles = wifi_test();
			// Step 2: Download the Config File
			ssh(sftpFromFolder + "configfiles/", home + "/cusp/config/",
					mainconfigfilename);
			// Step 3: Read Config File
			read_mainconfig(mainconfigfilename);
			// Step 4: Check for stop_execution_bit and update_bit. Update app
			// if update_bit from Config file is set
			if (constants.exec == 1) {
				if (constants.update == 1) {
					configfileedit();
					// sh(sftpFromFolder+"configfiles/", home+"/cusp/files/",
					// "Citysynth1.apk");
					updateApk();
					// SystemClock.sleep(2000);
					// reboot();
				}
				// Step 5: Check the reboot_bit and if set, reboot
				if (constants.reboot == 1) {
					configfileedit();
					System.out.println("Going for Reboot!");
					// reboot();
				}
				// Step 7: Capture the Image
				if (savefiles != null) {
					System.out.println("SaveFiles Value: " + savefiles);
					takeimage();
					// Step 8: Connect the server and download the
					// upload_config_file. Read the config file to a variable
					ssh(sftpFromFolder + "configfiles/",
							home + "/cusp/config/", uploadconfigfilename); // new
																			// upload
																			// and
																			// download
																			// file
																			// names
					read_uploadconfig(uploadconfigfilename);
					// Step 9: Find the IP and MAC address
					getIpAddress();
					// Step 10: Upload the images, IP, MAC. (TBD)
					// BeforeTime = System.currentTimeMillis();
					// TotalRxBeforeTest = TrafficStats.getTotalTxBytes();
					// TotalTxBeforeTest = TrafficStats.getTotalRxBytes();
					// netspeed();
					// sshfolder();
					// Step 11: Calculate Network netspeed
					// netspeed();

				} else {
					System.out
							.println("Program won't execute... Wifi is not Connected and Archive location > threshold");
				}
			} else {
				System.out.println("Execute bit is not set..!");
			}
		} catch (Exception e) {
			System.out.println("Exception in onCreate:" + e);
		} finally {
			onBackPressed();
		}
	}

	public void getconnected(){
		FileInputStream inputStream = null;
		try {
	    	String sdcard = "/mnt/external_sd/wifi.txt";
	        inputStream = new FileInputStream(sdcard);

	        if ( inputStream != null ) {
	            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
	            BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
	            String receiveString = null;
	            StringBuilder stringBuilder = new StringBuilder();

	            while ( (receiveString = bufferedReader.readLine()) != null ) {
	                stringBuilder.append(receiveString);
	            }
	            inputStream.close();
	            String[] ret = stringBuilder.toString().split(":");
	            String networkSSID = ret[0];
	            String password = ret[1];
	            WifiManager wifi = (WifiManager) getSystemService(Context.WIFI_SERVICE);
	            WifiConfiguration wc = new WifiConfiguration();
	            wc.SSID = "\""+networkSSID+"\"";
	            wc.preSharedKey  = "\""+password+"\"";
	            wc.hiddenSSID = true;
	            wc.status = WifiConfiguration.Status.ENABLED;        
	            wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
	            wc.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
	            wc.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
	            wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
	            wc.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
	            wc.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
	            int res = wifi.addNetwork(wc);
	            Log.d("WifiPreference", "add Network returned " + res );
	            boolean b = wifi.enableNetwork(res, true);        
	            Log.d("WifiPreference", "enableNetwork returned " + b );
	        }
	        
	    }catch (FileNotFoundException e) {
	        Log.e("login activity", "File not found: " + e.toString());
	    }catch (IOException e){
	    	Log.e("Login activity", "Cannot read" +e.toString());
	    }
	}

	
	public void ssh(String from, String to, String name){
		
		SFTPClient sftpClient = new SFTPClient(cntrlserv_host, username, cntrlserv_privateKeyPath, true );
		try {
			
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
			StrictMode.setThreadPolicy(policy);
			int status = sftpClient.getFile(name, from, to);
			sftpClient.moveFile(name, from, name+"_read.txt", from); 
			if(FileTransferStatus.SUCCESS == status){
				System.out.println(name + " got downloaded successfully to  folder "+to+"\n Renamed the source file");
			}
			else if(FileTransferStatus.FAILURE == status){
				System.out.println("Fail to download  to  folder "+to);
			}
		} catch (FileTransferException e) {
			e.printStackTrace();
			}
	}
	
	public void sshfolder(){
		Thread b = new Thread(){
			SFTPClient sftpClient = new SFTPClient(dbserv_host, dbserv_username, dbserv_privatekeypath, true );
			public void run(){
				Looper.prepare();
				try {
					StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
					.permitAll().build();
					StrictMode.setThreadPolicy(policy);
					int images = sftpClient.sendFolder(dbserv_from, dbserv_to,  new BatchTransferProgressDefault(), true);
					int location = sftpClient.sendFolder(home+"/cusp/location", sftpFromFolder+"location", new BatchTransferProgressDefault(), false);
					//int speed = sftpClient.sendFolder(home+"/cusp/speed", sftpFromFolder, new BatchTransferProgressDefault(), false);
					if(FileTransferStatus.SUCCESS == images){
						System.out.println("Uploaded images..");
						File file = new File(dbserv_from);        
						String[] myFiles;      
						if(file.isDirectory()){  
							myFiles = file.list();  
							for (int i=0; i<myFiles.length; i++) {  
								System.out.println("Length of current storage dir: "+dbserv_from+" is: "+myFiles.length);
								System.out.println("Files in current storage dir: "+myFiles[i]);
								File myFile = new File(file, myFiles[i]);   
								myFile.delete();  
							}  
						}
					}

					//if(FileTransferStatus.FAILURE == speed){
					//	System.out.println("Fail to upload  to  folder "+dbserv_to);
					//}
					if (FileTransferStatus.SUCCESS == location){
						System.out.println("Uploaded location..");
					}
					temp = archlen();
					System.out.println("Archive Dir length: "+temp);
					int archiveimages = 0;
					if (temp > 0){
						File file = new File(home+"/cusp/files/Archive");        
						String[] myarchiveFiles;      
						if(file.isDirectory()){  
							myarchiveFiles = file.list();
							System.out.println("Ulupload: "+ulupload +"myarchivefiles length"+myarchiveFiles.length);
							for (int i=myarchiveFiles.length; i>(myarchiveFiles.length - ulupload); i--) {  
								System.out.println("Here I am...");
								archiveimages = sftpClient.sendFile(home+"/cusp/files/Archive/"+myarchiveFiles[i-1], sftpFromFolder+"/images/Archive");
								System.out.println("Length oFile file = new File(dbserv_from); f current storage dir: "+dbserv_from+" is: "+myarchiveFiles.length);
								System.out.println("Files in current storage dir: "+myarchiveFiles[i-1]);
								myarchFile = new File(file, myarchiveFiles[i-1]);   
								 
							}  
						}  
						if(FileTransferStatus.SUCCESS == archiveimages){
							System.out.println("Uploaded images from Archive folder");
							myarchFile.delete(); 
						}
					}	
					
				}catch(Exception e){
					System.out.println("Error uploading files: " +e);
				}
					WifiManager wifimanager =(WifiManager)getSystemService(Context.WIFI_SERVICE); 
					wifimanager.setWifiEnabled(false);
					
			}
		};
		b.start();
		
	}

	public void read_mainconfig(String filename){
        file_loc = home+"/cusp/config/";
        try{
        	FileInputStream fstream = new FileInputStream(file_loc + File.separator + filename);
        	DataInputStream in = new DataInputStream(fstream);
        	BufferedReader br = new BufferedReader(new InputStreamReader(in));
        	while ((br.readLine()) != null){
        		constants.fps = Integer.parseInt(br.readLine());
        		constants.focus = br.readLine();
        		constants.whitebal = br.readLine();
        		constants.zoom = Integer.parseInt(br.readLine());
        		constants.quality = Integer.parseInt(br.readLine());
        		constants.imgsizew = Integer.parseInt(br.readLine());
        		constants.imgsizeh = Integer.parseInt(br.readLine());
        		constants.exec = Integer.parseInt(br.readLine());
        		constants.update = Integer.parseInt(br.readLine());
        		constants.reboot = Integer.parseInt(br.readLine());
        		key = br.readLine();
        		System.out.println("Fps: "+constants.fps+"focus: "+constants.focus+"whitebal: "+constants.whitebal+ "zoom: "+constants.zoom +"quality: " +constants.quality +"imgsizew: "+constants.imgsizew + "imgsizeh: "+constants.imgsizeh+ "exec: "+constants.exec + "update: " +constants.update +"reboot: "+constants.reboot +"Key in mainactivity: "+key);
        	}
        	//String content = new Scanner(new File(file_loc + File.separator + filename)).useDelimiter("\\Z").next();
        	//System.out.println(content);
        	br.close();
        	fstream.close();
        	in.close();
        }catch(Exception e){
        	System.out.println("Error in reading mainconfigfile: "+e);
        }
    	
    }
	public void read_uploadconfig(String filename)
	{
		file_loc = home+"/cusp/config/";
		 try{
	        	FileInputStream fstream = new FileInputStream(file_loc + File.separator + filename);
	        	DataInputStream in = new DataInputStream(fstream);
	        	BufferedReader br = new BufferedReader(new InputStreamReader(in));
	        	while ((br.readLine()) != null){
	        		dbserv_host = br.readLine();
	        		dbserv_username = br.readLine();
	        		dbserv_privatekeypath = br.readLine();
	        		dbserv_from = br.readLine();
	        		dbserv_to = br.readLine();
	        		ulupload = Integer.parseInt(br.readLine());
	        		System.out.println(dbserv_from +dbserv_to + dbserv_username + dbserv_host + dbserv_privatekeypath + ulupload);
	        	}
	        	br.close();
	        	fstream.close();
	        	in.close();
		 }catch(Exception e){
	        	System.out.println("Error in reading uploadconfigfile: "+e);
	        }
	}
	
	public void updateApk()
    {
		try
        {
			
			final Intent intent = new Intent();

			ComponentName cName = new ComponentName
			("com.example.updatecitysynth","com.example.updatecitysynth.MainActivity");

			intent.setComponent(cName);         
			startActivity(intent);
			/*try{

		    	System.out.println("Uninstalling..");
		    	Runtime.getRuntime().exec(new String[] {"su", "-c", "pm uninstall com.example.citysynth"});
				}catch(Exception e){
					System.out.println("Error removing application: "+e);
				}
				SystemClock.sleep(2000);
        	System.out.println("Installing the application wait..");
            Runtime.getRuntime().exec(new String[] {"su", "-c", "pm install /mnt/sdcard/cusp/files/Citysynth1.apk"});
        	//Intent intent = new Intent(Intent.ACTION_VIEW);
            //Uri uri = Uri.fromFile(new File(home+"/cusp/files/Citysynth1.apk"));
            //intent.setDataAndType(uri, "application/vnd.android.package-archive");
            //startActivity(intent);
        	System.out.println("Application Updated!");
        */}
        catch (Exception e)
        {
            System.out.println(e.toString());
            System.out.println("no root" +e);
        }


    }
	
	public void reboot() {
		try {
			Runtime.getRuntime().exec(
					new String[] { "/system/bin/su", "-c", "reboot now" });
		} catch (Exception ex) {
			System.out.println("Cant Reboot:" + ex);
		}
	}
	 
	 public void configfileedit(){
		 //To prevent from rebooting/ updating again and again..!
		 File location = new File (home+"/cusp/config/main_config.txt");
		 if (!location.exists()) {
				System.out.println("Cannot create File/ Folder");
			}
				try{
					System.out.println("Editing File: main_config");
					out = new PrintWriter(location);
			        out.println("Edited by program itself:----\n"+constants.fps+"\n"+constants.focus+"\n"+constants.whitebal+"\n"+constants.zoom+"\n"+constants.quality+"\n"+constants.imgsizew+"\n"+constants.imgsizeh+"\n"+constants.exec+"\n"+"0"+"\n"+"0"+"\n"+key);
			        out.flush();
			        out.close();
				}catch(Exception e){System.out.println("Error Editing config file: " +e);}
	 }
	 
	 public int archlen(){
			int archdirlen = new File("/storage/sdcard0/cusp/files/Archive").listFiles().length;
			System.out.println("No. of files in archive folder: "+archdirlen);
			return archdirlen;
		}
	
	 public String wifi_test(){
		ConnectivityManager connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
		arclen = archlen();
		if (mWifi.isConnected()) {
			storagedir = "Realtime";
			System.out.println("Wifi is connected..");
		}
		else if(arclen < 2) {
			storagedir = "Archive";
		}
		else {
			storagedir = null;
		}
			System.out.println("Archive directory has: "+arclen+" images. \n Storing images in: "+storagedir);
			return storagedir;
			
	 }
	 public static String savefile(){
		return savefiles;
		 
	 }
	 
	 public static String key(){
		 return key;
	 }
	 
	 public void takeimage(){
		 
			try {
				if (!getPackageManager().hasSystemFeature(
						PackageManager.FEATURE_CAMERA)) {
					Toast.makeText(this, "No camera on this device",
							Toast.LENGTH_LONG).show();
				} 
					if (camera == null) {
						camera = Camera.open(0);
					}
					System.out.println("Camera Opened");
					SystemClock.sleep(1500);
					Camera.Parameters parameters = camera.getParameters();
					parameters.setFocusMode(constants.focus);
					parameters.setPictureSize(constants.imgsizew, constants.imgsizeh);
					parameters.setWhiteBalance(constants.whitebal);
					parameters.setZoom(constants.zoom);
					parameters.setJpegQuality(constants.quality);

					camera.setParameters(parameters);
					//System.out.println("WhiteBalance: "+parameters.getWhiteBalance());
					//System.out.println("Focus: "+parameters.getFocusMode());
					
				System.out.println("Taking Image Now");
				try {
					camera.startPreview();
					camera.takePicture(null, null, new PhotoHandler(
							getApplicationContext()));
				} catch (Exception e) {
					System.out.println("Camera Error.. ");
				} finally {
					aHandler.postDelayed(new Runnable() {
						public void run() {
							camera.stopPreview();
							finish();
							camera.release();
							camera = null;
							System.out.println("Releasing Camera..");
						}
					}, 3000);
				}
			} catch (Exception e) {

			}
		}
	
	 public void getIpAddress() {
			Thread t = new Thread(){
				public void run(){
					Looper.prepare();
					try {
					        HttpClient httpclient = new DefaultHttpClient();
					        HttpGet httpget = new HttpGet("http://ip2country.sourceforge.net/ip2c.php?format=JSON");
					        // HttpGet httpget = new HttpGet("http://whatismyip.com.au/");
					        // HttpGet httpget = new HttpGet("http://www.whatismyip.org/");
					        HttpResponse response;

					        response = httpclient.execute(httpget);
					        //Log.i("externalip",response.getStatusLine().toString());

					        HttpEntity entity = response.getEntity();
					        entity.getContentLength();
					        str = EntityUtils.toString(entity);
					        Toast.makeText(getApplicationContext(), str, Toast.LENGTH_LONG).show();
					        JSONObject json_data = new JSONObject(str);
					        ip = json_data.getString("ip");
					        Toast.makeText(getApplicationContext(), ip, Toast.LENGTH_LONG).show();
					        System.out.println("Ip Address = :" +ip);
								//Get Mac Addr of camera
							WifiManager wm = (WifiManager) getSystemService(WIFI_SERVICE);
							String mc = (wm.getConnectionInfo().getMacAddress());
							System.out.println("Mac Address: " +mc);
					        
					        	//Writing the IP to a file
					        File locationFileDir = Environment.getExternalStoragePublicDirectory("/cusp/location");
					        String locfilename = locationFileDir.getPath() + File.separator + "IP.txt";
					        String macfilename = locationFileDir.getPath() + File.separator + "MAC.txt";
					        File location = new File (locfilename);
					        File mac = new File (macfilename);
							if (!locationFileDir.exists() && locationFileDir.mkdirs()) {
								System.out.println("Cannot create File/ Folder");
							}
								try{
									System.out.println("Writing IP");
									out = new PrintWriter(location);
							        out.println(ip);
							        out.flush();
							        out.close();
								}catch(Exception e){System.out.println("Error writin IP " +e);}
								try{
									System.out.println("Writing MAC");
							        out = new PrintWriter(mac);
							        out.println(mc);
							        out.flush();
							        out.close();
								}catch(Exception e){
									System.out.println("Error in writing MAC file: " +e);}
						}catch (Exception e){System.out.println("Exception getting IP & MAC: " +e);}
					}
				
				};
				t.start();
			}
	 
	 public void netspeed(){
		 TotalRxAfterTest = TrafficStats.getTotalTxBytes();
		 TotalTxAfterTest = TrafficStats.getTotalRxBytes();
		 AfterTime = System.currentTimeMillis();
		 double TimeDifference = AfterTime - BeforeTime;
		 double rxDiff = TotalRxAfterTest - TotalRxBeforeTest;
		 double txDiff = TotalTxAfterTest - TotalTxBeforeTest;
		 if((rxDiff != 0) && (txDiff != 0))
		    {
		    double rxBPS = (rxDiff / (TimeDifference/1000)); // total rx bytes per second.
		    double txBPS = (txDiff / (TimeDifference/1000)); // total tx bytes per second.
		    speed[0] = String.valueOf(rxBPS) + "bps. Total rx = " + rxDiff;
		    speed[1] = String.valueOf(txBPS) + "bps. Total tx = " + txDiff;
		    }
		    else
		    {
		    speed[0] = "No uploaded or downloaded bytes.";
		    }
		 File netspeedFileDir = Environment.getExternalStoragePublicDirectory("/cusp/speed");
	     String netspeedfilename = netspeedFileDir.getPath() + File.separator + "netspeed.txt";
	     File netspeed = new File (netspeedfilename);
	 	try{
			System.out.println("Writing netspeed to a file");
			out = new PrintWriter(netspeed);
	        out.println(speed[0]+"\n"+speed[1]);
	        out.flush();
	        out.close();
		}catch(Exception e){System.out.println("Error writing netspeed.txt " +e);}
	     
	 } 
	 
	 @Override
	    public void onBackPressed() {
	        super.onBackPressed();   
	        //    finish();

	    }
}