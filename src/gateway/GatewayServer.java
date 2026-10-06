package gateway;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class GatewayServer {

    ServerSocket server;
    Gateway gateway;

    public GatewayServer(Gateway gateway){
        this.gateway = gateway;
    }
    

    public void start(){
        try {
            server = new ServerSocket(5050);

            while (true){
                Socket socket = server.accept();
                BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                gateway.setLeaderId(reader.readLine());
                System.err.println("Current Leader: " + gateway.getLeaderId());
                socket.close();
            }


        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
