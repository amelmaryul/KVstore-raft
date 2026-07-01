import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Arrays;

// i belive this is extra and is never used


public class ServerThread implements Runnable{
    Socket socket;

    public ServerThread(Socket socket){
        this.socket = socket;
    }


    @Override
    public void run(){
        try{
            StorageEngine storageEngine = StorageEngine.getInstance();
            System.out.println("Server Connected");

            BufferedReader in_socket = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter out_socket = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);

            System.out.println("IM ABOUT TO TEXT THE OTHER SERVER SAYING HEY SERVER FRIEND");
            out_socket.println("Hey server my friend how are you doing buddy");

            while (true){
                String[] idk = in_socket.readLine().split(","); // I/O blocker fyi
                System.out.println("Yessss daddy server you did index give me the updated keys thankzzzz");
                System.out.println("This is what the dumbass be sending us ffs: " + Arrays.toString(idk));
                System.out.println("                                                                                                                                       ");
                for(String s : idk){
                    System.out.println(s);
                }
                if (idk[0].equals("set")){
                    storageEngine.set(idk[1], idk[2]);
                    out_socket.println("yeah buddy, love. good set now fuck off");
                    break;
                }
                
                else{
                    out_socket.println("Yeah buddy how about you stop with that stupid shit? yeah thanks though");
                    break;
                }
            }
            socket.close();

        } catch (Exception exception){
            exception.printStackTrace();;
        }
    }
}