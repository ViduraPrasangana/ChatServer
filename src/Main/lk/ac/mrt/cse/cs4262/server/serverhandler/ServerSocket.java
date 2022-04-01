package lk.ac.mrt.cse.cs4262.server.serverhandler;

import lk.ac.mrt.cse.cs4262.server.ChatServer;
import lk.ac.mrt.cse.cs4262.server.FastBullyService;
import lk.ac.mrt.cse.cs4262.server.model.Server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

public class ServerSocket extends Thread{

    private java.net.ServerSocket socket;
    private boolean run = true;
    private FastBullyService fastBullyService;


    public ServerSocket(String address, int port,FastBullyService fastBullyService) throws IOException {
        socket = new java.net.ServerSocket();
        this.fastBullyService = fastBullyService;
        InetAddress ip = InetAddress.getLocalHost();
        String internal_address = ip.getHostAddress();
        SocketAddress inetSocketAddress = new InetSocketAddress(address,port);
        System.out.println(address+" "+port);
        socket.bind(inetSocketAddress);
    }

    @Override
    public void run() {
        try {
            while (run) {
                Socket s = socket.accept();
                //TODO: Check both hostname and host address
                Server server = fastBullyService.isConnected(s.getInetAddress().getHostAddress(),s.getPort());
                ServerConnectionHandler connectionHandler;
                if(server != null){
                    connectionHandler = server.getConnectionHandler();
                }else{
                    connectionHandler = new ServerConnectionHandler(s);
                    fastBullyService.addConnection(s,connectionHandler);
                }
                connectionHandler.start();
            }
        } catch (IOException e) {
            System.out.println("Communication Error: " + e.getMessage());
            System.exit(1);
        }
    }


}
