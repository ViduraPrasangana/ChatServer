package lk.ac.mrt.cse.cs4262.server;


import org.json.simple.parser.ParseException;
import java.io.IOException;

public class ChatServer {
    public static void main(String[] args) throws IOException, ParseException {

        try{
            ServerSocket serverSocket = new ServerSocket("0.0.0.0",5555);
            serverSocket.start();
            ClientSocket clientSocket = new ClientSocket("0.0.0.0",4444);
            clientSocket.start();
        }catch (IOException e){
            System.out.println("Error occured"+e);
        }
    }
}


