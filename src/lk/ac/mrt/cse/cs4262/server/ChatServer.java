package lk.ac.mrt.cse.cs4262.server;


import org.json.simple.parser.ParseException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class ChatServer {
    public static void main(String[] args) throws IOException, ParseException {
        Socket socket = null;

        try{
            ServerSocket ss = new ServerSocket(5000);
            socket =ss.accept();

            ResponseThread responseThread = new ResponseThread(socket);
//            Thread resThread = new Thread(responseThread);
//            resThread.start();

            RequestThread requestThread = new RequestThread(socket,responseThread);
            Thread reqThread = new Thread(requestThread);
            reqThread.start();

        }catch (IOException e){
            System.out.println("Error occured"+e);
        }
    }
}


