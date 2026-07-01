import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;

public class WriteServer implements Runnable { 
    String[] message;
    int port;

    public WriteServer(int port, String[] message) {
        this.message = message;
        this.port = port;
    }
    
    public void run() {
        try {
            Socket socket = new Socket("localhost", port);
            BufferedReader in_socket = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out_socket = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);

            System.out.println("Message to whoever is reading this. hey buddy this part is for your server writing to another server. thanks");
            System.out.println(in_socket.readLine());

            String temp = String.join(",", message);
            out_socket.println(temp);

            System.out.println("This is temp from your fucking ai they're actually so fucking stupid: " + temp);
            System.out.println("Im gonna send the stuff now so the ohter servers should come and collect the following message: " + Arrays.toString(message));
            System.out.println(in_socket.readLine());

            socket.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
