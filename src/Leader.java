import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.LinkedBlockingQueue;

public class Leader {


    public Leader() throws IOException {
        Coordinator coordinator = new Coordinator();

        ServerSocket serverSocket = new ServerSocket(5055);

        while (true){
            try{
            Socket socket = serverSocket.accept();

            // run 2 threads here one that listens and one that writes to socket
            // give both threads the coordinator object
            LinkedBlockingQueue<String[]> queue = new LinkedBlockingQueue<>();
            new Thread(new WriterThread(queue, socket)).start();
            coordinator.registerQueue(queue);
            new Thread(new ListenerThread(coordinator, socket)).start();


            } catch (Exception e){
                e.printStackTrace();
            }

        }
        
    }
    
}
