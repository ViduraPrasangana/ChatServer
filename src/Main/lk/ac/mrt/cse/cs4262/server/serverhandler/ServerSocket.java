package lk.ac.mrt.cse.cs4262.server.serverhandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class ServerSocket extends Thread{

    private java.net.ServerSocket socket;
    private boolean run = true;


    public ServerSocket(String address, int port) throws IOException {
        socket = new java.net.ServerSocket();
        SocketAddress inetSocketAddress = new InetSocketAddress(address,port);
        socket.bind(inetSocketAddress);
    }

    @Override
    public void run() {
        try {
            while (run) {
                Socket s = socket.accept();
                ServerConnectionHandler connectionHandler = new ServerConnectionHandler(s);
                connectionHandler.start();
            }
        } catch (IOException e) {
            System.out.println("Communication Error: " + e.getMessage());
            System.exit(1);
        }
    }


}
