package lk.ac.mrt.cse.cs4262.server.clienthandler;

import java.io.*;
import java.net.*;

public class ClientSocket extends Thread{

    private ServerSocket socket;
    private boolean run = true;


    public ClientSocket(String address, int port) throws IOException {
        socket = new ServerSocket();
        InetAddress ip = InetAddress.getLocalHost();
//        System.out.print("My external IP address is: " + address);
        String internal_address = ip.getHostAddress();
//        System.out.print("My IP address is: " + internal_address);
        SocketAddress inetSocketAddress = new InetSocketAddress(internal_address,port);
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
