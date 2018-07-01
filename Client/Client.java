package com.example.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Client {

    private static DataInputStream dataInputStream;
    private static DataOutputStream dataOutputStream;
    private static Socket clientSocket;
    private static String IP_ADDRESS = "192.168.1.4";
    private static int PORT_NUMBER = 8080;
    private static String msg, userMail, userPass;
    private static HashMap<String, String> p_Users;
    private static boolean isLoggedIn = false;


    public static void main(String[] args) {

        p_Users = new HashMap<>();
        fillMap();

        while (true) {
            try {
                System.out.println("ESTABLISHING CONNECTION..");

                clientSocket = new Socket(IP_ADDRESS, PORT_NUMBER);


                dataOutputStream = new DataOutputStream(clientSocket.getOutputStream());
                dataInputStream = new DataInputStream(clientSocket.getInputStream());

                System.out.println("LISTENING FOR YOUR MESSAGES..");

                msg = dataInputStream.readUTF();
                System.out.println("YOUR MESSAGE: " + msg);

                splitMSG(msg);

                System.out.println("E-Mail: " + userMail);
                System.out.println("Password: " + userPass);



            } catch (IOException e) {
                e.printStackTrace();
            }

            if (p_Users.containsKey(userMail)) {
                if (userPass.equals(p_Users.get(userMail))) {
                    isLoggedIn = true;
                    System.out.println("System: " + "SUCCESSFULLY LOGGED IN!");
                }else {
                    isLoggedIn = false;
                    System.out.println("System: " + "INCORRECT E-MAIL OR PASSWORD!");
                }

            } else {
                isLoggedIn = false;
                System.out.println("System: " + "INCORRECT E-MAIL OR PASSWORD!");
            }

            try {
                System.out.println("I'm sending user flag..");
                System.out.println("=======================");
                dataOutputStream.writeBoolean(isLoggedIn);
                dataOutputStream.flush();
            } catch (IOException e) {
                System.out.println("PAIRED DEVICE DISCONNECTED!");
            }


        }


    }

    private static void fillMap() {
        p_Users.put("bahaasco@gmail.com", "123456");
        p_Users.put("aliya@gmail.com", "246810");
        p_Users.put("ahmed@yahoo.com", "159753");
        p_Users.put("mohamed@hotmail.com", "357951");
        p_Users.put("michael@gmail.com", "741369852");
        p_Users.put("ebtisam@yahoo.com", "963147852");
        p_Users.put("kariman@gmail.com", "123987456");
    }

    private static void splitMSG(String msg) {
        Pattern pattern = Pattern.compile("/ *");
        Matcher matcher = pattern.matcher(msg);
        if (matcher.find()) {
            userMail = msg.substring(0, matcher.start());
            userPass = msg.substring(matcher.end());
        }
    }
}
