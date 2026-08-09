package gateway;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.Scanner;

import util.Parsing;
import util.RespBuffer;

public class GatewayServerClientFacing {
    ServerSocket server;
    Gateway gateway;
    Parsing parsing = new Parsing();

    public GatewayServerClientFacing(Gateway gateway){
        this.gateway = gateway;
    }


    public void start(){
        try {
            server = new ServerSocket(5051);
        } catch (Exception e) {
            e.printStackTrace();
        }


        while (true){
            try {
                Socket clientSocket = server.accept();
                System.out.println("Connected to client");
                BufferedInputStream clientIn = new BufferedInputStream(clientSocket.getInputStream());
                BufferedOutputStream clientOut = new BufferedOutputStream(clientSocket.getOutputStream());

                Socket clusterSocket = new Socket(gateway.getLeaderId(), 5050); 
                BufferedInputStream clusterIn = new BufferedInputStream(clusterSocket.getInputStream());
                BufferedOutputStream clusterOut = new BufferedOutputStream(clusterSocket.getOutputStream());
                System.out.println("Connected to cluster");


                


                RespBuffer clientBuffer = new RespBuffer(1024);
                RespBuffer clusterBuffer = new RespBuffer(1024);
                String msg = (String) parsing.parseRespValue(clusterIn, clusterBuffer);

                msg = parsing.bulkStringToResp(msg);
                clientOut.write(msg.getBytes());
                clientOut.flush();
                boolean closeConnection = false;

                while (!closeConnection){
                    System.out.println("inside while loop");
                    String[] command = (String[]) parsing.parseRespValue(clientIn, clientBuffer);
                    System.out.println("Command recieved: " +Arrays.toString(command)    );
                    msg = parsing.arrayToRespArray(command);
                    System.out.println("Message recieved from Client: " + msg);
                    System.out.println("Attempting to send msg to cluster.........");
                    clusterOut.write(msg.getBytes());
                    clusterOut.flush();
                    System.out.println("Finished sending message to cluster!");
                    msg = (String) parsing.parseRespValue(clusterIn, clusterBuffer);
                    System.out.println("Im sending the reply back homie");
                    if (msg.equals("Closing Connection")) closeConnection = true;
                    msg = parsing.bulkStringToResp(msg);
                    clientOut.write(msg.getBytes());
                    clientOut.flush();
                }

                
                clientSocket.close();
                clusterSocket.close();


            } catch (Exception e) {
                e.printStackTrace();
            }

            
        }
    }

    
}
