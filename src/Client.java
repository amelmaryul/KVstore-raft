import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {

    // Constructor that establishes a connection with the server
    Client(int port) throws IOException {

        // Create a socket to connect to the server running on localhost and port 2020
        Socket socket = new Socket("localhost", port);
        System.out.println("Successfully connected to the server.");

        // Input stream to receive messages from the server
        BufferedReader in_socket = new BufferedReader(new InputStreamReader(socket.getInputStream()));

        // Output stream to send messages to the server
        PrintWriter out_socket = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()), true);

        // Read the message from the server and print it to the console
        String message = in_socket.readLine();
        System.out.println("Server says: " + message);

        
        Scanner scanner = new Scanner(System.in);
        while (true){
            System.out.println("Enter what you want to send to server");
            String message2 = scanner.nextLine();
            String respString = Parsing.parseCommandString(message2);
            // Send a message back to the server to acknowledge the message
            out_socket.println(respString);

            message = in_socket.readLine();

            System.out.println("Server says: " + message);
            if (message.equals("Closing Connection")) break;
        }   

        // Close the socket connection after communication
        socket.close();
        System.out.println("Connection Closed");
        scanner.close();

    }

    public static void main(String[] args) {
        try {
            // Create a new instance of TCPClient to establish connection with the server
            new Client(Integer.valueOf(args[0]));
        } catch (Exception e) {
            // Print the exception if any occurs
            e.printStackTrace();
        }
    }
}
