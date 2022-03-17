package lk.ac.mrt.cse.cs4262.server;

import com.google.gson.Gson;
import lk.ac.mrt.cse.cs4262.server.model.request.NewIdentityReq;
import lk.ac.mrt.cse.cs4262.server.model.response.NewIdentity;
import lk.ac.mrt.cse.cs4262.server.model.response.RoomChange;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;

public class ClientSocket implements Runnable{

    private ServerSocket socket;
    private boolean run = true;


    public ClientSocket(String address, int port ) throws IOException {
        socket = new ServerSocket();
        SocketAddress inetSocketAddress = new InetSocketAddress(address,port);
        socket.bind(inetSocketAddress);
    }


    public void run() {
        try {
            while (run) {
                System.out.println("socket hold");
                Socket s = socket.accept();
                ClientConnectionHandler connectionHandler = new ClientConnectionHandler(s);
                connectionHandler.start();
            }
        } catch (IOException e) {
            System.out.println("Communication Error: " + e.getMessage());
            System.exit(1);
        }
    }


}
