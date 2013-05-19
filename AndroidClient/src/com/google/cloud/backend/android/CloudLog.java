package com.google.cloud.backend.android;

import java.io.IOException;

import com.google.api.client.extensions.android.http.AndroidHttp;
import com.google.api.client.json.gson.GsonFactory;
import com.google.api.services.clientlogging.*;

public class CloudLog {
	String android_id; 
	public CloudLog(String android_id) {
		this.android_id = android_id;
 
	}

	public void Log (final String message) {


			new Thread(new Runnable() {
			    public void run() {
			    	
					Clientlogging.Builder builder = new Clientlogging.Builder (
							   AndroidHttp.newCompatibleTransport(), new GsonFactory(), null);
					Clientlogging service = builder.build();
					try {
						String msg = android_id +":"+ message;
						service.logs().log(msg).execute();
						android.util.Log.d("log", msg);

					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						android.util.Log.e("log", e.toString());

					}
			    }
			  }).start();
			
			
			
	
	}

}
