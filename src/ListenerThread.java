import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;

public class ListenerThread implements Runnable{

    Socket socket;
    Coordinator coordinator;

    public ListenerThread(Coordinator coordinator, Socket socket){
        this.coordinator = coordinator;
        this.socket = socket;
    }



    public void run(){
        try{
            BufferedReader in_socket = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            while (true){
                String[] command = Parsing.parseRequest(in_socket);
                handleCommand(command);


            }

        } catch (Exception e){
            e.printStackTrace();
        }



    }

    public void handleCommand(String[] command){
        if (command[0].equals("ack")){
            coordinator.handleAck(command);
        }

        else if (command[0].equals("set")){
            coordinator.addCommandToQueue(command);
             
        }
    }

}