package com.example.santoshkumaramisagadda.project_benchmark;

/**
 * Created by santoshkumaramisagadda on 11/13/17.
 */

import android.app.Activity;
import android.content.Context;
import android.os.Environment;
import android.widget.Button;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.net.HttpURLConnection;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

//MajorCode and idea from
//http://www.coderzheaven.com/2011/07/07/how-to-download-an-image-in-android-programatically/
public class DownloadService {
    Context context;
    Activity activity;
    //change tp model location
    String downloadFileURL="http://10.0.2.2:8888/Upload/output.model";

    public DownloadService(Activity act, Context ctx, Button btn_test)
    {
        this.activity=act;
        this.context=ctx;
    }
    public boolean startDownload()
    {
        final boolean[] result = {false};
        new Thread(new Runnable() {
            public void run() {
                result[0] =downloadFile();
            }
        }).start();
        return result[0];
    }

    public boolean downloadFile()
    {
        boolean result=false;
        try{
            File folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Project");
            if (!folder.exists()) {
                folder.mkdir();
            }
            File file = new File(folder,"output.model");

            /*
            //installing trustmanager to allow uploading to unprotected Impact lab server
            //https://stackoverflow.com/a/8694377
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }

                        public void checkClientTrusted(
                                X509Certificate[] certs, String authType) {
                        }

                        public void checkServerTrusted(
                                X509Certificate[] certs, String authType) {
                        }
                    }
            };

            try {
                SSLContext sc = SSLContext.getInstance("SSL");
                sc.init(null, trustAllCerts, new SecureRandom());
                HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            } catch (Exception e) {
                e.printStackTrace();
            }
            */
            URL url = new URL(downloadFileURL);
            HttpURLConnection ucon = (HttpURLConnection) url.openConnection();

            InputStream is = ucon.getInputStream();
            BufferedInputStream bis = new BufferedInputStream(is);
            ByteArrayOutputStream buffer = new ByteArrayOutputStream(50);
            int current = 0;
            while ((current = bis.read()) != -1) {
                buffer.write((byte) current);
            }

            FileOutputStream fos = new FileOutputStream(file);
            fos.write(buffer.toByteArray());
            fos.flush();
            fos.close();

            activity.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(context, "Model generated and downloaded.",
                            Toast.LENGTH_SHORT).show();
                }
            });
            result=true;


        }catch (Exception e)
        {
            result=false;
            activity.runOnUiThread(new Runnable() {
                public void run() {
                    Toast.makeText(context, "Model Download Failed.",
                            Toast.LENGTH_SHORT).show();
                }
            });
            e.printStackTrace();
        }
        return result;
    }
}
