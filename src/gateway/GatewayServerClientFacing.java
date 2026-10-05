package gateway;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import util.Parsing;
import util.RespBuffer;

public class GatewayServerClientFacing {
    ServerSocket server;
    Gateway gateway;
    Parsing parsing = new Parsing();

    public GatewayServerClientFacing(Gateway gateway){
        this.gateway = gateway;
    }


    public void start() {
        try {
            server = new ServerSocket(5051);
        } catch (Exception e) {
            e.printStackTrace();
        }


        while (true){
            Socket clientSocket = null;
            BufferedInputStream clientIn = null;
            BufferedOutputStream clientOut = null;


            try {
                clientSocket = server.accept();
                clientIn = new BufferedInputStream(clientSocket.getInputStream());
                clientOut = new BufferedOutputStream(clientSocket.getOutputStream());

                Socket clusterSocket = new Socket(gateway.getLeaderId(), 5050); 
                BufferedInputStream clusterIn = new BufferedInputStream(clusterSocket.getInputStream());
                BufferedOutputStream clusterOut = new BufferedOutputStream(clusterSocket.getOutputStream());


                


                RespBuffer clientBuffer = new RespBuffer(1024);
                RespBuffer clusterBuffer = new RespBuffer(1024);
                String msg = (String) parsing.parseRespValue(clusterIn, clusterBuffer);

                msg = parsing.bulkStringToResp(msg);
                clientOut.write(msg.getBytes());
                clientOut.flush();
                boolean closeConnection = false;

                while (!closeConnection){
                    String[] command = (String[]) parsing.parseRespValue(clientIn, clientBuffer);
                    msg = parsing.arrayToRespArray(command);
                    if (command[0].equals("Leader")){
                        clientOut.write(parsing.bulkStringToResp(gateway.getLeaderId()).getBytes());
                        clientOut.flush();
                        continue;
                    }
                    clusterOut.write(msg.getBytes());
                    clusterOut.flush();
                    msg = (String) parsing.parseRespValue(clusterIn, clusterBuffer);
                    if (msg.equals("Closing Connection")) closeConnection = true;
                    msg = parsing.bulkStringToResp(msg);
                    clientOut.write(msg.getBytes());
                    clientOut.flush();
                }

                
                clientSocket.close();
                clusterSocket.close();


            } catch (Exception e) {
                e.printStackTrace();
                RespBuffer clientBuffer = new RespBuffer(1024);
                String msg = parsing.bulkStringToResp("Error: Electing new Leader");
                try {
                    clientOut.write(msg.getBytes());
                    clientOut.flush();
                    String[] command = (String[]) parsing.parseRespValue(clientIn, clientBuffer);
                    msg = parsing.arrayToRespArray(command);
                    if (command[0].equals("Leader")){
                        clientOut.write(parsing.bulkStringToResp(gateway.getLeaderId()).getBytes());
                        clientOut.flush();
                    }
                    else {
                        clientOut.write(parsing.bulkStringToResp("Closing connection").getBytes());
                        clientOut.flush();
                    }
                    clientSocket.close();
                    
                } catch (Exception ee) {
                    // TODO: handle exception
                    ee.printStackTrace();
                }
            }

            
        }
    }

    
}
