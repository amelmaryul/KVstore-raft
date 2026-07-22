package client;
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import storage.StorageEngine;

public class ClientServer {
    private int clients = 1;
    int port;
    List<Integer> listOfPorts = new ArrayList<>(Arrays.asList(5051,5052));

    public ClientServer(int port){
        this.port = port;
    }


    public void start() throws IOException {
        StorageEngine storageEngine = StorageEngine.getInstance();

        ServerSocket serverSocket = new ServerSocket(port);
        System.out.printf("Port %d is open \n", port);
        //storageEngine.readFile("../data.txt");

        while (true){

            Socket socket = serverSocket.accept();
            System.out.println("Client " + socket.getInetAddress() + " has connected.");
            new Thread(new ClientThread(this, socket, clients++)).start();
        }

    }

    public static void main(String[] args) {
        try {
            // Create a new instance of the TCPServer to start the server
            ClientServer server = new ClientServer(Integer.valueOf(args[0]));
            StorageEngine storageEngine = StorageEngine.getInstance();
            Thread thread = new Thread(() -> {
                System.out.println("Before shutdown");
                storageEngine.dumpToFile("../data.txt");
                System.out.println("Successfully written to file");
            });
            Runtime.getRuntime().addShutdownHook(thread);
            server.start();

        } catch (Exception e) {
            // Print the stack trace in case of an exception
            e.printStackTrace();
        }
    }
}