package client;

import java.net.Socket;
import java.util.Arrays;
import java.io.*;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;

import storage.StorageEngine;
import util.Parsing;

public class ClientThread implements Runnable {
    ClientServer server;
    Socket socket;
    int id;

    public ClientThread(ClientServer server, Socket socket, int id){
        this.server = server;
        this.socket = socket;
        this.id = id;
    }




    @Override
    public void run(){
        try{
            StorageEngine storageEngine = StorageEngine.getInstance();

            System.out.println("Client connected");
            // Input stream for receiving data from the client
            InputStream inputStream = socket.getInputStream();
            InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
            BufferedReader in_socket = new BufferedReader(inputStreamReader);

            // Output stream for sending data to the client
            OutputStream outputStream = socket.getOutputStream();
            OutputStreamWriter outputStreamWriter = new OutputStreamWriter(outputStream);
            PrintWriter out_socket = new PrintWriter(outputStreamWriter, true);

            // Send a welcome message to the client
            out_socket.println("You've connected to socket: " + this.id);
            while (true){
                // Read a message sent by the client
                String[] command = Parsing.parseRequest(in_socket); 
                if (command[1].equals("turn") && command[2].equals("off")){ // tacky error handling can be done better imo
                    out_socket.println("Closing Connection");
                    break;
                }
                if (command[0].equals("set")){
                    // i dont store here i shoudl just send it to the queue only. 
                    out_socket.println("We'll try to update the map in a bit!");
                    storageEngine.queue.offer(command);
                    System.out.println("[ClientThread] added set command to the queue.");
                }
                else if (command[0].equals("get")){
                    out_socket.println(storageEngine.get(command[1]));
                }
                // this is to delete but it does nothing yet
                else if (command[0].equals("delete")){
                    out_socket.println("We'll try to update the map in a bit!");
                    storageEngine.queue.offer(command);
                    System.out.println("[ClientThread] added delete command to the queue.");
                }
                else{
                    out_socket.println("You sent a message with an incorrect format. Try again. ////////////// Message: " + Arrays.toString(command));
                }
                
                System.out.println("Client says " + Arrays.toString(command));
            }
            // Close the socket connection
            socket.close();
            System.out.println("Socket: " + this.id + " terminated connection");
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    
}
