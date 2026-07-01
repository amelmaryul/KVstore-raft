import java.util.List;
import java.util.Scanner;
import java.util.Arrays;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Map;
import java.util.HashMap;

public class NodeServer {
    int port;
    static List<Integer> listOfPorts = new ArrayList<>(Arrays.asList(5051,5052,5053,5054));
    static List<Socket> sockets = new ArrayList<>();
    int leader = 5055;

    public NodeServer(int port){
        this.port = port;
    }


    public void start() throws IOException {
        ServerSocket serverSocket = new ServerSocket(port);
        System.out.printf("Port %d is open \n", port);
        Thread thread = new Thread(new ReplicationThread(sockets, listOfPorts, leader, port));
        thread.start();
        

        while (true) {
            Socket socket = serverSocket.accept();
            System.out.println("[NodeServer] connected to node server with port: " + String.valueOf(socket.getPort()));
            new Thread(new NodeListenerThread(socket)).start();

        }
    }


    public static void main(String[] args){
        NodeServer server = new NodeServer(Integer.valueOf(args[1]));

        try{
            server.start();
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
