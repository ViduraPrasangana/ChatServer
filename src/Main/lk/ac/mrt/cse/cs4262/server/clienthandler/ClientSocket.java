package lk.ac.mrt.cse.cs4262.server.clienthandler;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;

public class ClientSocket extends Thread{

    private ServerSocket socket;
    private boolean run = true;


    public ClientSocket(String address, int port) throws IOException {
        socket = new ServerSocket();
        SocketAddress inetSocketAddress = new InetSocketAddress(address,port);
        socket.bind(inetSocketAddress);
    }

    @Override
    public void run() {
        try {
            while (run) {
                Socket s = socket.accept();
                System.out.println(s.getPort());
                ClientConnectionHandler connectionHandler = new ClientConnectionHandler(s);
                connectionHandler.start();
            }
        } catch (IOException e) {
            System.out.println("Communication Error: " + e.getMessage());
            System.exit(1);
        }
    }


}
