package lk.ac.mrt.cse.cs4262.server;

import com.google.gson.Gson;
import lk.ac.mrt.cse.cs4262.server.model.Server;
import lk.ac.mrt.cse.cs4262.server.model.request.CoordinatorReq;
import lk.ac.mrt.cse.cs4262.server.model.request.ImUpReq;
import lk.ac.mrt.cse.cs4262.server.serverhandler.ServerConnectionHandler;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class FastBullyService {
    private HashMap<String, Server> servers;
    private final Gson gson;
    public static String leader;
//    ArrayList<String> activeServers;

    public FastBullyService(){
        this.servers = ChatServer.servers;
        gson = new Gson();

    }

    public void imUp(){
        ImUpReq imUpReq = new ImUpReq(ChatServer.thisServer.getServerId());
        String message = gson.toJson(imUpReq);
        int total_servers = ChatServer.servers.size();
        AtomicInteger active_server_count = new AtomicInteger();

        servers.forEach((s, server) -> {
            if(server.getServerId() != ChatServer.thisServer.getServerId()){
                ServerConnectionHandler connectionHandler = server.getConnectionHandler();
                try{
                    if(connectionHandler == null){
                        Socket socket = new Socket(server.getAddress(),server.getCoordinationPort());
                        connectionHandler = new ServerConnectionHandler(socket,this);
                        server.setSocket(socket);
                        server.setConnectionHandler(connectionHandler);

                        //server.setAlive(true);
                    }

                    // Send Imup Requests
                    connectionHandler.send(message);
                    active_server_count.set(active_server_count.get() + 1);
                } catch (IOException e) {
                    server.setAlive(false);
                    System.out.println("Server "+server.getServerId()+" is not activated.");
                    //e.printStackTrace();
                }
            }
        });
        if (active_server_count.get() == 0){
            this.leader = ChatServer.serverId;
            System.out.println("There are no any servers alive.");
            System.out.println("I'm the Leader");
        }

        HashMap<String, Server> higherPriorityServers = getAllHigherServers(ChatServer.serverId);
        HashMap<String, Server> lowerPriorityServers = getAllLowerServers(ChatServer.serverId);
        if (higherPriorityServers.size() == 0 && lowerPriorityServers.size() !=0){
            leaderBreadcast(lowerPriorityServers);
        }
    }


    public void leaderBreadcast(HashMap<String, Server> servers){
        servers.forEach((s, server) -> {
            if (server.isAlive()){
                ServerConnectionHandler connectionHandler = server.getConnectionHandler();
                try{
                    if(connectionHandler == null){
                        Socket socket = new Socket(server.getAddress(),server.getCoordinationPort());
                        connectionHandler = new ServerConnectionHandler(socket,this);
                        server.setSocket(socket);
                        server.setConnectionHandler(connectionHandler);
                    }

                    CoordinatorReq leader = new CoordinatorReq(ChatServer.thisServer.getServerId());

                    connectionHandler.send(gson.toJson(leader));
                    connectionHandler.closeConnection();
                } catch (IOException e) {
                    server.setAlive(false);
                    e.printStackTrace();
                }
            }

        });
    }

    public Server isConnected(String address, int port){
        AtomicReference<Server> server = new AtomicReference<>();
        servers.forEach((s, server1) -> {
            if(server1.getAddress().equals(address) && server1.getCoordinationPort() == port) server.set(server1);
        });

        return server.get();
    }

    public void broadcast(String message){

    }

    public void heldElection(){

    }

    public void notifyLeader(){
        servers.forEach((s, server) -> {

        });
    }

    public HashMap<String, Server> getAllLowerServers(String serverId){
        int currentPriority = getPriorityNumber(serverId);
        HashMap<String, Server> lowerPriorityServers = new HashMap<>();
        servers.forEach((s, server) -> {
            int serverPriority = getPriorityNumber(server.getServerId());
            if (serverPriority < currentPriority){
                lowerPriorityServers.put(s,server);
            }
        });
        return lowerPriorityServers;
    }

    public HashMap<String, Server> getAllHigherServers(String serverId){
        int currentPriority = getPriorityNumber(serverId);
        HashMap<String, Server> higherPriorityServers = new HashMap<>();
        servers.forEach((s, server) -> {
            int serverPriority = getPriorityNumber(server.getServerId());
            if (serverPriority > currentPriority){
                higherPriorityServers.put(s,server);
            }
        });
        return higherPriorityServers;
    }

    public Integer getPriorityNumber(String serverId){
        String[] characters = serverId.split("s");
        return Integer.parseInt(characters[1]);
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
