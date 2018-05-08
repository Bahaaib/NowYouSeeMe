package com.example.server;

import java.net.*;
import java.io.*;
import java.util.Base64;
import java.util.logging.Handler;


public class Server {
    private static DataInputStream dataInputStream;
    private static DataOutputStream dataOutputStream;
    private static ServerSocket serverSocket;
    private static Socket socket;
    private static File mFile;
    private static FileInputStream fileInputStream;
    private static String imageDataToString;
    private static byte[] imageData;
    private static int picIndex = 1;


    //TODO: Implement interrupt FLAG to close socket
    private boolean wasInterrupted;


    public static void main(String[] args) throws IOException {
        // Establishing Connection..
        serverSocket = new ServerSocket(13267);

        while (true) {

            System.out.println("WAITING...");
            socket = serverSocket.accept();

            System.out.println("CONNECTION ADMITTED..");
            System.out.println("CLIENT INFO: " + socket);

            dataInputStream = new DataInputStream(socket.getInputStream());
            dataOutputStream = new DataOutputStream(socket.getOutputStream());


            mFile = new File("D:\\" + picIndex + ".jpg");


            System.out.println("SENDING...");

            try {
                fileInputStream = new FileInputStream(mFile);

            }catch (Exception e){
                System.out.println("No MORE IMAGES CAPTURED!");
            }
            fileInputStream = new FileInputStream(mFile);

            imageData = new byte[(int) mFile.length()];
            fileInputStream.read(imageData);

            //Encoding byteArray into Base64 String..
            imageDataToString = encodeImageToString(imageData);
            System.out.println(imageDataToString.length());

            System.out.println(imageDataToString);
            dataOutputStream.writeUTF(imageDataToString);

            System.out.println(String.valueOf(dataOutputStream.size()));

            dataOutputStream.flush();

            try {
                Thread.sleep(5000);
                picIndex++;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println("Restarting...");
        }


    }

    /* Using Java.Util Base64 Encoder NOT "Apache Common" to avoid dependency problems..
       Android.Util Base64 Decoder can perfectly decode Java.Util Encodings,
       Unlike Apache common, Which uses different "Weired' Algorithm.
       Also: Apache Common Library causes several problems when used on Android API Level > 23
    */
    public static String encodeImageToString(byte[] imageByteArray) {
        return Base64.getEncoder().encodeToString(imageByteArray);
    }
}

