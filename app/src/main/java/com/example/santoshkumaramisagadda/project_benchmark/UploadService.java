package com.example.santoshkumaramisagadda.project_benchmark;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Button;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

//Major idea and source code from
// http://androidexample.com/Upload_File_To_Server_-_Android_Example/index.php?view=article_discription&aid=83

public class UploadService {
    String uploadUrl;
    String uploadFilePath="";
    String uploadFileName="";
    Context context;
    Activity activity;
    Button btn_upload;

    public UploadService(Activity act, Context ctx, String uploadFilePath,String uploadFileName,Button btn_upload, int i)
    {
        if(i==0)
            uploadUrl="http://10.0.2.2:8888/Upload/UploadToServer.php";
        //else if(i==1)
        //    uploadUrl="http://10.0.2.2:8888/Upload/TrainAtServer.php";
        this.activity=act;
        this.uploadFilePath=uploadFilePath;
        this.uploadFileName=uploadFileName;
        this.context=ctx;
        this.btn_upload=btn_upload;
    }
    public int startUpload()
    {
        final int[] result = {0};
        new Thread(new Runnable() {
            @Override
            public void run() {
                result[0] =uploadFile(uploadFilePath , uploadFileName);
            }
        }).start();

        return result[0];
    }
    public int uploadFile(String sourceFileUri,String sourceFileName)
    {
        String fileName = sourceFileName;
        int serverResponseCode = 0;

        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        File sourceFile = new File(sourceFileUri);

        if (!sourceFile.isFile()) {
            Toast.makeText(context,"Source File Doesn't Exist", Toast.LENGTH_LONG).show();
            return 0;
        }
        else
        {
            try {
                FileInputStream fileInputStream = new FileInputStream(sourceFile);

                //installing trustmanager to allow uploading to unprotected Impact lab server
                //https://stackoverflow.com/a/8694377

                /*final TrustManager[] trustAllCerts = new TrustManager[]{
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
                    return 0;
                }
                */
                String data= URLEncoder.encode("name", "UTF-8")
                        + "=" + URLEncoder.encode("Hello", "UTF-8");
                URL url = new URL(uploadUrl);

                // Open a HTTP  connection to  the URL
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true);
                conn.setDoOutput(true);
                conn.setUseCaches(false);
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                conn.setRequestProperty("uploaded_file", fileName);
                conn.setRequestProperty("Classifier","1");


                dos = new DataOutputStream(conn.getOutputStream());

                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""
                        + fileName + "\"" + lineEnd);
                dos.writeBytes(lineEnd);

                bytesAvailable = fileInputStream.available();

                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                while (bytesRead > 0) {

                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                }

                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                //for parameter
                dos.writeBytes("Content-Disposition: form-data; name=\"Classifier\"" + lineEnd);
                dos.writeBytes(lineEnd);
                dos.writeBytes("1");//your parameter value
                dos.writeBytes(lineEnd); //to add multiple parameters write Content-Disposition: form-data; name=\"your parameter name\"" + crlf again and keep repeating till here :)
                dos.writeBytes(twoHyphens + boundary + twoHyphens);
                //

                OutputStreamWriter wr = new OutputStreamWriter(conn.getOutputStream());
                wr.write( data );
                wr.flush();

                serverResponseCode = conn.getResponseCode();
                String serverResponseMessage = conn.getResponseMessage();

                Log.i("uploadFile", "HTTP Response is : "
                        + serverResponseMessage + ": " + serverResponseCode);

                if(serverResponseCode == 200){

                    activity.runOnUiThread(new Runnable() {
                        public void run() {
                            btn_upload.setEnabled(true);
                        }
                    });
                }
                fileInputStream.close();
                dos.flush();
                dos.close();

            } catch (Exception ex) {

                ex.printStackTrace();
                activity.runOnUiThread(new Runnable() {
                    public void run() {
                        btn_upload.setEnabled(true);
                        //btn_upload.setText("UPLOAD");
                        Toast.makeText(context, "File Upload Failed.",
                                Toast.LENGTH_SHORT).show();
                    }
                });
            }
            return serverResponseCode;

        }
    }
}
