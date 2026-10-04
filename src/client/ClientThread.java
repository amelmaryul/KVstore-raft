package client;

import java.net.Socket;
import java.util.Arrays;
import java.io.*;

import storage.StorageEngine;
import util.Parsing;
import util.RespBuffer;

public class ClientThread implements Runnable {
    ClientServer server;
    Socket socket;
    int id;
    Parsing parsing = new Parsing();

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
            BufferedInputStream in = new BufferedInputStream(socket.getInputStream());

            // Output stream for sending data to the client
            BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream()); 

            String msg = parsing.bulkStringToResp("Connected Successfully");
            out.write(msg.getBytes());
            out.flush();
            RespBuffer buffer = new RespBuffer(1024);
            while (true){
                String[] command = (String[]) parsing.parseRespValue(in, buffer);
                System.out.println("String command received");
                if (command[0].equals("set")){
                    storageEngine.queue.offer(command);
                    msg = parsing.bulkStringToResp("Added Set command");
                    out.write(msg.getBytes());
                    out.flush();
                }
                else if (command[0].equals("get")){
                    msg = storageEngine.get(command[1]);
                    if (msg == null){
                        msg = parsing.bulkStringToResp("Error: Key does not exist");
                    }
                    else msg = parsing.bulkStringToResp(msg);
                    out.write(msg.getBytes());
                    out.flush();
                }
                else if (command[0].equals("delete")){
                    //out.pr("We'll try to update the map in a bit!");
                    msg = storageEngine.get(command[1]);
                    if (msg == null){
                        msg = parsing.bulkStringToResp("Error: Key does not exist");
                    }
                    else msg = parsing.bulkStringToResp(msg);
                    out.write(msg.getBytes());
                    out.flush();
                    storageEngine.queue.offer(command);
                }
                else {
                    msg = parsing.bulkStringToResp("You sent a message with an incorrect format. Try again. \r\n Message: " + Arrays.toString(command));
                    out.write(msg.getBytes());
                    out.flush();
                }
                System.out.println("Client says " + Arrays.toString(command));

            }

            

            // Close the socket connection
        } catch (Exception e){
            try {
                socket.close();
            } catch (IOException e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
            System.out.println("Socket: " + this.id + " terminated connection");
            e.printStackTrace();
        }
    }

    
}
