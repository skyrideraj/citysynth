package com.example.citysynth;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.crypto.Cipher;
import javax.crypto.CipherOutputStream;
import javax.crypto.spec.SecretKeySpec;

import com.zehon.exception.FileTransferException;
import com.zehon.sftp.SFTP;

import android.app.Activity;
import android.content.Context;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;


public class PhotoHandler extends Activity implements PictureCallback {
	private final Context context;
	public PhotoHandler(Context context) {
		this.context = context;
	}
	MainActivity mainactivity = new MainActivity();
	FileOutputStream fos = null;
	File sdDir = Environment.getExternalStoragePublicDirectory("cusp/files");
	String name, storagedir, s = null;
	@Override
	public void onPictureTaken(byte[] data, Camera camera) {
		System.out.println("On Picture Taken method");
		File pictureFileDir = getDir();
		if (!pictureFileDir.exists() && !pictureFileDir.mkdirs()) {

			Log.d(Constants.DEBUG_TAG, "Can't create directory to save image.");
			Toast.makeText(context, "Can't create directory to save image.",
					Toast.LENGTH_LONG).show();
			return;

		}

		SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd__HHmmss", Locale.US);
		String date = dateFormat.format(new Date());
		String photoFile = "Picture_" + date + ".jpg";

		String filename = pictureFileDir.getPath() + File.separator + photoFile;

		File pictureFile = new File(filename);

		try{
		    // Here you read the cleartext.
			ByteArrayInputStream bis = new ByteArrayInputStream(data);
		    //FileInputStream fis = new FileInputStream("/mnt/sdcard/Pictures/CUSP/Realtime/Picture_20140412__110342.jpg");
		    // This stream write the encrypted text. This stream will be wrapped by another stream.
		    FileOutputStream fos = new FileOutputStream(pictureFile);

		    // Length is 16 byte
		    System.out.println("key: "+mainactivity.key);
		    SecretKeySpec sks = new SecretKeySpec(mainactivity.key.getBytes(), "AES");
		    System.out.println(sks);
		    // Create cipher
		    Cipher cipher = Cipher.getInstance("AES");
		    cipher.init(Cipher.ENCRYPT_MODE, sks);
		    // Wrap the output stream
		    CipherOutputStream cos = new CipherOutputStream(fos, cipher);
		    // Write bytes
		    int b;
		    byte[] d = new byte[8];
		    System.out.println("BIS: "+bis);
		    while((b = bis.read(d)) != -1) {
		        cos.write(d, 0, b);
		    }
		    // Flush and close streams.
		    cos.flush();
		    cos.close();
		    bis.close();
		    System.out.println("done");
		}catch(Exception e){
			System.out.println("Encryption Failed due to: "+e);
		} finally{
			finish();
		}
		try{
			mainactivity.sshfolder();
		}catch(Exception e){
			System.out.println("Exception running ssh folder: "+e);
		}

	}
	

	public File getDir() {
		return new File(sdDir, mainactivity.savefile());
	}

}