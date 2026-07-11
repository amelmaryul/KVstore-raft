import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class RaftMessaging {

    public Message sendRequest(int port, Message req){
        try (Socket socket = new Socket("localhost", port)) {
            
            socket.setSoTimeout(500);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
            out.writeObject(req);
            out.flush();

            return (Message) in.readObject();
            

        } catch (Exception e){
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
