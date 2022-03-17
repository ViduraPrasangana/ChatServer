package lk.ac.mrt.cse.cs4262.server;


import org.json.simple.parser.ParseException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ChatServer {
    public static void main(String[] args) throws IOException, ParseException {

        try{
            ClientSocket clientSocket = new ClientSocket("0.0.0.0",4444);
            Thread reqThread = new Thread(clientSocket);
            reqThread.start();
        }catch (IOException e){
            System.out.println("Error occured"+e);
        }
    }
}


