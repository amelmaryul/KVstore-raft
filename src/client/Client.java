package client;
import java.io.*;
import java.net.Socket;
import java.util.Scanner;

import util.Parsing;
import util.RespBuffer;

public class Client {

    Parsing parsing = new Parsing();


    // Constructor that establishes a connection with the server
    Client(int port) throws IOException {

        try {
            // Create a socket to connect to the server running on localhost and port 2020
            Socket socket = new Socket("localhost", port);
            System.out.println("Successfully connected to the server.");

            BufferedInputStream in = new BufferedInputStream(socket.getInputStream());
            BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream());



            RespBuffer buffer = new RespBuffer(1024);
            String msg = (String) parsing.parseRespValue(in, buffer);
            System.out.println("Server says: " + msg);

            Scanner scanner = new Scanner(System.in);

            while (true){
                System.out.println("Enter what you want to send to the server");
                msg = scanner.nextLine();
                String msg2 = msg;
                msg = parsing.generateRespString(msg);
                out.write(msg.getBytes());
                out.flush();

                msg = (String) parsing.parseRespValue(in, buffer);
                System.out.println("Server says: " + msg);
                if (msg.equals("Closing Connection")) break;
                if (msg2.equals("Leader")) break;
            }

            // Close the socket connection after communication
            socket.close();
            //System.out.println("Connection Closed");
            scanner.close();


        } catch (Exception e) {
            System.out.println("Unexpected error occured");
        }


    }

    public static void main(String[] args) {
        try {
            // Create a new instance of TCPClient to establish connection with the server
            new Client(Integer.valueOf(args[0]));
        } catch (Exception e) {
            // Print the exception if any occurs
            e.printStackTrace();
        }
    }
}
