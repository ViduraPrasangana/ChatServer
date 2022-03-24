package lk.ac.mrt.cse.cs4262.server;

import com.google.gson.Gson;
import lk.ac.mrt.cse.cs4262.server.model.Server;
import lk.ac.mrt.cse.cs4262.server.model.request.CoordinatorReq;
import lk.ac.mrt.cse.cs4262.server.model.request.ImUpReq;
import lk.ac.mrt.cse.cs4262.server.serverhandler.ServerConnectionHandler;

import java.io.IOException;
import java.lang.reflect.Array;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

public class FastBullyService {
    private HashMap<String, Server> servers;
    private final Gson gson;
    public static String leader;
    ArrayList<String> activeServers;

    public FastBullyService(){
        this.servers = ChatServer.servers;
        gson = new Gson();

    }

    public void imUp(){
        ImUpReq imUpReq = new ImUpReq(ChatServer.thisServer.getServerId());
        broadcast(gson.toJson(imUpReq));
    }
    public void leaderBreadcast(){
        for (String serverId: activeServers) {
            Server server = ChatServer.servers.get(serverId);
            ServerConnectionHandler connectionHandler = server.getConnectionHandler();
            try{
                if(connectionHandler == null){
                    Socket socket = new Socket(server.getAddress(),server.getCoordinationPort());
                    connectionHandler = new ServerConnectionHandler(socket,this);
                    server.setSocket(socket);
                    server.setConnectionHandler(connectionHandler);
                    activeServers.add(server.getServerId());
                    //server.setAlive(true);
                }

                CoordinatorReq leader = new CoordinatorReq(ChatServer.thisServer.getServerId());

                connectionHandler.send(gson.toJson(leader));
                connectionHandler.closeConnection();
            } catch (IOException e) {
                server.setAlive(false);
                e.printStackTrace();
            }
        }
    }

    public Server isConnected(String address, int port){
        AtomicReference<Server> server = new AtomicReference<>();
        servers.forEach((s, server1) -> {
            if(server1.getAddress().equals(address) && server1.getCoordinationPort() == port) server.set(server1);
        });

        return server.get();
    }

    public void broadcast(String message){
        activeServers = new ArrayList<>();
        servers.forEach((s, server) -> {
            if(server.getServerId() != ChatServer.thisServer.getServerId()){
                ServerConnectionHandler connectionHandler = server.getConnectionHandler();
                try{
                    if(connectionHandler == null){
                        Socket socket = new Socket(server.getAddress(),server.getCoordinationPort());
                        connectionHandler = new ServerConnectionHandler(socket,this);
                        server.setSocket(socket);
                        server.setConnectionHandler(connectionHandler);
                        activeServers.add(server.getServerId());
                        //server.setAlive(true);
                    }

                    // Send Imup Requests
                    connectionHandler.send(message);
                    connectionHandler.closeConnection();
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
