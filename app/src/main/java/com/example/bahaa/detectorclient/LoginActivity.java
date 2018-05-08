package com.example.bahaa.detectorclient;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;


public class LoginActivity extends Activity {

    private EditText email;
    private EditText Password;
    private Button Login;
    private int counter = 5;
    private boolean isBadTrial = false;
    private String Email, pass;

    /////////////////////Network Portion/////////////////
    private static DataInputStream dataInputStream;
    private static DataOutputStream dataOutputStream;
    private static ServerSocket serverSocket;
    private static Socket socket;
    private static boolean userStatusOK = false;
    private static boolean networkStatusOK = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        email = (EditText) findViewById(R.id.Name);
        Password = (EditText) findViewById(R.id.Password);
        Login = (Button) findViewById(R.id.Login);


        try {
            serverSocket = new ServerSocket(8080);
        } catch (IOException e) {
            e.printStackTrace();
        }


        Login.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
                Email = email.getText().toString();
                pass = Password.getText().toString();

                if (isValidEmail(Email) && isValidPassword(pass)) {

                    //send to server for validation
                    new TransmitterTask().execute();

                }
                if (!isValidEmail(Email)) {
                    email.setError("Invalid Email");
                    isBadTrial = true;

                }
                if (!isValidPassword(pass)) {
                    Password.setError("Incorrect Password");
                    isBadTrial = true;

                }
                if (isBadTrial) {
                    counter--;
                }
                if (counter == 0) {
                    finish();
                }
            }


        });
    }


    // validating email id
    private boolean isValidEmail(String email) {

        return Patterns.EMAIL_ADDRESS.matcher(email).matches();
    }

    // validating password with retype password
    private boolean isValidPassword(String pass) {
        if (!TextUtils.isEmpty(pass) && pass.length() > 5) {
            return true;
        }
        return false;
    }


    //Receiving Data in Background..
    class TransmitterTask extends AsyncTask<Void, Integer, String> {
        @Override
        protected String doInBackground(Void... params) {

            try {
                Log.i("Login Status", "I'm Waiting..");
                socket = serverSocket.accept();

                Log.i("Login Status", "CONNECTION ADMITTED");
                Log.i("Login Status", "CLIENT INFO: " + socket);

                dataInputStream = new DataInputStream(socket.getInputStream());
                dataOutputStream = new DataOutputStream(socket.getOutputStream());

                String msg = Email + "/" + pass;

                Log.i("Login Status", "I'M SENDING YOUR MESSAGE");
                dataOutputStream.writeUTF(msg);

                dataOutputStream.flush();

                userStatusOK = dataInputStream.readBoolean();
                Log.i("Login Status", String.valueOf(userStatusOK));


            } catch (IOException e) {
                e.printStackTrace();
            }


            return "Task Completed";
        }


        @Override
        protected void onPostExecute(String result) {

            if (userStatusOK) {
                Intent details = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(details);
            } else {
                Toast.makeText(LoginActivity.this, "Incorrect Email or Password!", Toast.LENGTH_LONG).show();

            }


        }

        @Override
        protected void onProgressUpdate(Integer... values) {


        }
    }
}