package com.example.bahaa.detectorclient;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import org.joda.time.DateTime;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;


public class MainActivity extends Activity {

    private TextView detailsTextView, dateTextView;
    private ImageView clientImage;

    String detailsStr, imgUrlStr, dateStr;
    private TextView localIP, portNumber, bytesDecoded, responseTime;
    private Button signoutBtn;
    /////////////////////Network Portion/////////////////

    private Socket clientSocket;
    private DataInputStream dataInputStream;
    private String IP_ADDRESS = "192.168.1.4";
    private int PORT_NUMBER = 13267;
    private String base64Code;
    private byte[] decodedString;
    private Bitmap bitmap;
    private long INITIAL_TIME = 0;
    private long ELAPSED_TIME = 0;
    private Handler handler;
    private Runnable runnable;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dateTextView = (TextView) findViewById(R.id.text_date);
        clientImage = (ImageView) findViewById(R.id.img);

        localIP = (TextView) findViewById(R.id.ip_tv);
        portNumber = (TextView) findViewById(R.id.port_tv);
        bytesDecoded = (TextView) findViewById(R.id.bytes_tv);
        responseTime = (TextView) findViewById(R.id.time_tv);
        signoutBtn = (Button)findViewById(R.id.signout_btn);

        signoutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        INITIAL_TIME = System.currentTimeMillis();

        callAsynchronousTask();


    }


    Callback picassoCallback = new Callback() {
        @Override
        public void onSuccess() {
            showData();

            pushNotification("Alert", "Check it out NOW!");
        }

        @Override
        public void onError() {
            Toast.makeText(MainActivity.this, "An error has occurred during loading image", Toast.LENGTH_SHORT).show();
        }
    };

    private void showData() {
        String year, month, day, hour, min, sec, fullDateTime;
        DateTime dateTime = new DateTime();


        year = String.valueOf(dateTime.getYear());
        month = String.valueOf(dateTime.getMonthOfYear());
        day = String.valueOf(dateTime.getDayOfMonth());
        hour = String.valueOf(dateTime.getHourOfDay());
        min = String.valueOf(dateTime.getMinuteOfHour());
        sec = String.valueOf(dateTime.getSecondOfMinute());

        fullDateTime = year + "-" + month + "-" + day + " " + hour + ":" + min + ":" + sec;

        Log.i("Date: ", fullDateTime);

        dateTextView.setText(fullDateTime);


    }


    private void pushNotification(String title, String msg) {
        Notification notification = createNotification(title, msg);
        NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (notificationManager != null)
            notificationManager.notify(435, notification);
    }

    private PendingIntent createPendingIntent() {
        Intent intent = new Intent(this, MainActivity.class);
        return PendingIntent.getActivity(this, 678, intent, 0);
    }

    private Notification createNotification(String title, String msg) {

        PendingIntent pendingIntent = createPendingIntent();

        return new Notification.Builder(this)
                .setContentTitle(getString(R.string.notification_msg))
                .setSubText(title)
                .setContentText(msg)
                .setSmallIcon(R.drawable.ic_eye)
                .setDefaults(Notification.DEFAULT_SOUND)
                .setContentIntent(pendingIntent)
                .build();


    }

    //this method for check if internet is available or not
    public static boolean ConnectionAvailable(Context ctx) {
        ConnectivityManager connectivityManager = (ConnectivityManager) ctx.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }


    //Receiving Data in Background..
    class ReceiverTask extends AsyncTask<Void, Integer, String> {
        @Override
        protected String doInBackground(Void... params) {


            try {

                Log.i("Client", "I'm going to connect..");
                clientSocket = new Socket(IP_ADDRESS, PORT_NUMBER);

                Log.i("Client", "I'm Loading streams");
                dataInputStream = new DataInputStream(clientSocket.getInputStream());

                base64Code = dataInputStream.readUTF();

                Log.i("Client", base64Code);

                //NO_WRAP:: Removes All line breakers from received String to avoid Decoding Corruption
                decodedString = Base64.decode(base64Code, Base64.NO_WRAP);

                //Converting the Decoded String back into image. Bitmap First!
                bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);


            } catch (IOException e) {
                e.printStackTrace();
            }


            return "Task Completed";
        }


        @Override
        protected void onPostExecute(String result) {
            //Rescale Bitmap to fit the imageView
            bitmap = Bitmap.createScaledBitmap(bitmap, 1000, 1000, true);
            Uri bitmapURI = getImageUri(MainActivity.this, bitmap);

            if (ConnectionAvailable(MainActivity.this)) {
                Picasso.with(MainActivity.this).load(bitmapURI).fit().centerInside().placeholder(R.drawable.load_icon)
                        .error(R.drawable.cloud_error).into(clientImage, picassoCallback);
            } else {
                Toast.makeText(MainActivity.this, "No Internet Connection", Toast.LENGTH_SHORT).show();
            }


            clientImage.setImageBitmap(bitmap);

            localIP.setText(IP_ADDRESS);
            portNumber.setText(String.valueOf(PORT_NUMBER));
            bytesDecoded.setText(String.valueOf(base64Code.length()) + " Bytes");

            ELAPSED_TIME = System.currentTimeMillis();

            responseTime.setText(String.valueOf(ELAPSED_TIME - INITIAL_TIME) + "ms");

            INITIAL_TIME = ELAPSED_TIME;


            Log.i("Client", "Leaving PostExecute..");

        }

        @Override
        protected void onProgressUpdate(Integer... values) {


        }


    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Detector Image", null);
        return Uri.parse(path);
    }

    public void callAsynchronousTask() {
        handler = new Handler();
        Timer timer = new Timer();

        TimerTask doAsynchronousTask = new TimerTask() {
            @Override
            public void run() {
              runnable = new Runnable() {
                  @Override
                  public void run() {
                      new ReceiverTask().execute();
                  }
              };
              handler.post(runnable);
            }
        };
        timer.schedule(doAsynchronousTask, 0, 3100); //execute in every 3 sec + 100 ms safety delay
    }

    @Override
    protected void onStop() {
        super.onStop();
        handler.removeCallbacks(runnable);

    }
}
