import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;

public class RaftMessaging {

    public Message sendRequest(String ip, Message req){
        try (Socket socket = new Socket()) {
            
            socket.setSoTimeout(500);

            SocketAddress address = new InetSocketAddress(ip, 8081);
            socket.connect(address, 250);
            
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            out.writeObject(req);
            out.flush();

            return (Message) in.readObject();
            


        } catch (SocketTimeoutException  e){
            System.out.println(ip + " potentially offline!");
            return null;

        } catch (UnknownHostException e){
            System.out.println(ip + " is an unknown host");
            return null;

        } catch (Exception e){
            e.printStackTrace(); 
            return null;
        }
    }

    public Message sendRequest(Socket socket, Message req){
        try{
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            out.writeObject(req);
            out.flush();

            return (Message) in.readObject();


        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    public Message sendRequest(Socket socket, ObjectOutputStream out, ObjectInputStream in, Message req){
        try{
            out.writeObject(req);
            out.flush();

            return (Message) in.readObject();


        } catch (Exception e){
            return null;
        }
    }

    
}
