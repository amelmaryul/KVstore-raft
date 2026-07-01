import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.Socket;



public class NodeListenerThread implements Runnable {

    Socket socket;
    StorageEngine storageEngine = StorageEngine.getInstance();

    public NodeListenerThread(Socket socket){
        this.socket = socket;
    }

   public void run() {
        try{
            while (true){
                BufferedReader in_socket = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                String[] command = Parsing.parseRequest(in_socket);
                System.out.println("[NodeListenerThread] I have recieved a message.");
                // some handling here maybe?
                if (command[0].equals("set")){
                    storageEngine.set(command[1], command[2]);
                    System.out.println("NodeListenerThread] updated key: "+ command[1] + " to value: " + storageEngine.get(command[1]));
                    System.out.println("[NodeListenerThread] updated kv store!");
                }
            }

        } catch (Exception e){
            e.printStackTrace();
        }
   }
}
