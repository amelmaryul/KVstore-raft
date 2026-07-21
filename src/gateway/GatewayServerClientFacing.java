package gateway;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

import util.Parsing;

public class GatewayServerClientFacing {
    ServerSocket server;
    Gateway gateway;

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
                BufferedReader clientReader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                PrintWriter pwClient = new PrintWriter(new OutputStreamWriter(clientSocket.getOutputStream()), true);
                Socket clusterSocket = new Socket(gateway.getLeaderId(), 5050); 
                BufferedReader clusterReader = new BufferedReader(new InputStreamReader(clusterSocket.getInputStream()));
                PrintWriter pwCluster = new PrintWriter(new OutputStreamWriter(clusterSocket.getOutputStream()), true);

                System.out.println("Client Connected");
                String message = clusterReader.readLine();
                pwClient.println(message);

                while (true){
                    String[] command = Parsing.parseRequest(clientReader);
                    String commandString = Parsing.buildRespString(command);
                    pwCluster.println(commandString);
                    message = clusterReader.readLine();
                    pwClient.println(message);
                    if (message.equals("Closing Connection")) break;

                }

                clientSocket.close();
                clusterSocket.close();


            } catch (Exception e) {
                e.printStackTrace();
            }

            
        }
    }

    
}
