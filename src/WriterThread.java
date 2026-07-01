import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;

public class WriterThread implements Runnable {
    LinkedBlockingQueue<String[]> queue;
    Socket socket;
    
    public WriterThread(LinkedBlockingQueue<String[]> queue, Socket socket){
        this.queue = queue;
        this.socket = socket;
    }

    public void run(){

        try{
        PrintWriter out_socket = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);

            while (true){

                String[] command = queue.take();
                String message = Parsing.buildRespString(command);
                out_socket.println(message);
                System.out.printf("[Leader WriterThread] %s command send to follower node at port: %d \n", command[0], socket.getPort());


            }



        } catch (Exception e ){
            e.printStackTrace();
        }

    }
    
}
