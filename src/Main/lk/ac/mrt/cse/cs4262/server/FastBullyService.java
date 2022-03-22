package lk.ac.mrt.cse.cs4262.server;

import com.google.gson.Gson;
import lk.ac.mrt.cse.cs4262.server.model.Server;
import lk.ac.mrt.cse.cs4262.server.model.request.ImUpReq;
import lk.ac.mrt.cse.cs4262.server.serverhandler.ServerConnectionHandler;

import java.io.IOException;
import java.net.Socket;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

public class FastBullyService {
    private HashMap<String, Server> servers;
    private final Gson gson;

    public FastBullyService(HashMap<String,Server> servers){
        this.servers = servers;
        gson = new Gson();

    }

    public void imUp(){
        ImUpReq imUpReq = new ImUpReq(ChatServer.thisServer.getServerId());
        broadcast(gson.toJson(imUpReq));
    }

    public Server isConnected(String address, int port){
        AtomicReference<Server> server = new AtomicReference<>();
        servers.forEach((s, server1) -> {
            if(server1.getAddress().equals(address) && server1.getCoordinationPort() == port) server.set(server1);
        });

        return server.get();
    }

    public void broadcast(String message){
        servers.forEach((s, server) -> {
            ServerConnectionHandler connectionHandler = server.getConnectionHandler();
            if(connectionHandler == null){
                try {
                    Socket socket = new Socket(server.getAddress(),server.getCoordinationPort());
                    connectionHandler = new ServerConnectionHandler(socket);
                    server.setSocket(socket);
                    server.setConnectionHandler(connectionHandler);
                    server.setAlive(true);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(connectionHandler == null){
                server.setAlive(false);
            }else{
                try {
                    connectionHandler.send(message);
                } catch (IOException e) {
                    server.setAlive(false);
                    e.printStackTrace();
                }
            }
        });
    }

    public void addConnection(Socket socket, ServerConnectionHandler connectionHandler) {
        servers.forEach((s, server1) -> {
            if(server1.getAddress().equals(socket.getInetAddress().getHostAddress()) && server1.getCoordinationPort() == socket.getPort()) {
                server1.setSocket(socket);
                server1.setConnectionHandler(connectionHandler);
                server1.setAlive(true);
            }
        });
    }
}
