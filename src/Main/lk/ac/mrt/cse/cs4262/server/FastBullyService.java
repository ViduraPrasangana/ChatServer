package lk.ac.mrt.cse.cs4262.server;

import com.google.gson.Gson;
import lk.ac.mrt.cse.cs4262.server.model.Server;
import lk.ac.mrt.cse.cs4262.server.model.View;
import lk.ac.mrt.cse.cs4262.server.model.request.*;
import lk.ac.mrt.cse.cs4262.server.serverhandler.ServerConnectionHandler;
import lk.ac.mrt.cse.cs4262.server.serverhandler.ServerMessageHandler;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class FastBullyService extends Thread{
    private HashMap<String, Server> servers;
    private HashMap<String,View> views;
    ServerMessageHandler messageHandler;
    private final Gson gson;
    public static String leader;

    public final int view_wait_time_T2 = 5000;
    public boolean waiting_for_view = false;
    public final int answer_wait_time_T2 = 5000;
    public boolean waiting_for_answer = false;
    ArrayList<String> answeredServers;
    public final int coordinator_wait_time_T3 = 5000;
    public boolean waiting_for_coordinator = false;
    public boolean coordinator_for_nomination = false;
    public final int nomination_or_coordinator_wait_time_T4 = 5000;
    public boolean waiting_for_nomination_or_coordinator = false;


//    ArrayList<String> activeServers;

    public FastBullyService(){
        this.servers = ChatServer.servers;
        gson = new Gson();
        messageHandler = ServerMessageHandler.getInstance();
        messageHandler.setFastBullyService(this);
        views = new HashMap<>();
    }

    public void imUp(){
        ImUpReq imUpReq = new ImUpReq(ChatServer.thisServer.getServerId());
        String message = gson.toJson(imUpReq);
        int total_servers = ChatServer.servers.size();
        AtomicInteger active_server_count = new AtomicInteger();

        servers.forEach((s, server) -> {
            if(!Objects.equals(server.getServerId(), ChatServer.thisServer.getServerId())){
                ServerConnectionHandler connectionHandler = server.getConnectionHandler();
                try{
                    if(connectionHandler == null){
                        Socket socket = new Socket(server.getAddress(),server.getCoordinationPort());
                        connectionHandler = new ServerConnectionHandler(socket);
                        //server.setAlive(true);
                    }

                    // Send Imup Requests
                    connectionHandler.send(message);
                    connectionHandler.closeConnection();
                    waiting_for_view = true;
                    active_server_count.set(active_server_count.get() + 1);
                } catch (IOException e) {
                    server.setAlive(false);
                    System.out.println("Server "+server.getServerId()+" is not activated.");
                    //e.printStackTrace();
                }
            }
        });

        Thread t = new Thread(){
            @Override
            public void run() {
                try {
                    sleep(view_wait_time_T2);
                    waiting_for_view = false;
                    if(views.isEmpty()){
                        setLeader(ChatServer.thisServer);
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        t.start();



        if (active_server_count.get() == 0){
            setLeader(ChatServer.thisServer);
            System.out.println("There are no any servers alive.");
            System.out.println("I'm the Leader");
        }

        HashMap<String, Server> higherPriorityServers = getAllHigherServers(ChatServer.serverId);
        HashMap<String, Server> lowerPriorityServers = getAllLowerServers(ChatServer.serverId);
        if (higherPriorityServers.size() == 0 && lowerPriorityServers.size() !=0){
            setLeader(ChatServer.thisServer);
            leaderBreadcast(lowerPriorityServers);
        }else {
            setLeader(getHighestServer());
        }
    }

    public HashMap<String, View> getViews() {
        return views;
    }

    public void updateView(ArrayList<View> views){
        for (View view :
                views) {
            if(this.views.containsKey(view.getServerId())){
                if(!view.equals(this.views.get(view.getServerId()))){
                    this.views.put(view.getServerId(),view);
                }
            }else{
                this.views.put(view.getServerId(),view);
            }
        }

        Iterator<String> i = this.views.keySet().iterator();
        while (i.hasNext()) {
            String s = i.next(); // must be called before you can call i.remove()
            boolean in = false;
            for (View view :
                    views) {
                if (view.getServerId().equals(s)){
                    in = true;
                }
            }
            if(!in){
                i.remove();
            }

        }

    }

//    public View getHighestPriorityView(){
//        AtomicReference<View> v = new AtomicReference<>(new View());
//        v.get().setPriority(Integer.MIN_VALUE);
//        this.views.forEach((s, view) -> {
//            if(view.getPriority()> v.get().getPriority()){
//                v.set(view);
//            }
//        });
//        return v.get();
//    }

    public void setLeader(Server server){
        leader = server.getServerId();
    }


    public void leaderBreadcast(HashMap<String, Server> servers){
        servers.forEach((s, server) -> {
            if (server.isAlive()){
                try{
                    Socket socket = new Socket(server.getAddress(),server.getCoordinationPort());
                    ServerConnectionHandler connectionHandler = new ServerConnectionHandler(socket);

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



    public void heldElection(){
        answeredServers = new ArrayList<>();
        System.out.println("Leader Election is Happening...");
        ChatServer.electionStatus = true;
        HashMap<String, Server> higherPriorityServers = getAllHigherServers(ChatServer.thisServer.getServerId());
        if(higherPriorityServers.isEmpty()){
            setLeader(ChatServer.thisServer);
        }else{
            higherPriorityServers.forEach((s, server) -> {
                if(!Objects.equals(server.getServerId(), ChatServer.thisServer.getServerId())){
                    try{
                        Socket socket = new Socket(server.getAddress(),server.getCoordinationPort());
                        ServerConnectionHandler connectionHandler = new ServerConnectionHandler(socket);
                        //server.setAlive(true);
                        ElectionReq electionReq = new ElectionReq(ChatServer.serverId);
                        // Send Imup Requests
                        connectionHandler.send(gson.toJson(electionReq));
                        connectionHandler.closeConnection();
                        waiting_for_answer = true;
                    } catch (IOException e) {
                        server.setAlive(false);
                        System.out.println("Server "+server.getServerId()+" is not activated.");
                        //e.printStackTrace();
                    }
                }
            });
        }

        Thread t = new Thread(){
            @Override
            public void run() {
                try {
                    sleep(answer_wait_time_T2);
                    waiting_for_answer = false;
                    if(answeredServers.isEmpty()){
                        HashMap<String,Server> lowerPriorityServers = getAllLowerServers(ChatServer.serverId);
                        setLeader(ChatServer.thisServer);
                        leaderBreadcast(lowerPriorityServers);
                    }else{
                        coordinator_for_nomination = false;
                        while (!coordinator_for_nomination){
                            Server s = getHighestServer(answeredServers);
                            Socket socket = new Socket(s.getAddress(),s.getCoordinationPort());
                            ServerConnectionHandler connectionHandler = new ServerConnectionHandler(socket);

                            NominationReq nominationReq = new NominationReq(ChatServer.serverId);
                            connectionHandler.send(gson.toJson(nominationReq));
                            connectionHandler.closeConnection();
                            waiting_for_coordinator = true;
                            Thread t2 = new Thread(){
                                @Override
                                public void run() {
                                    try {
                                        sleep(coordinator_wait_time_T3);
                                        waiting_for_coordinator = false;
                                        if(coordinator_for_nomination){
                                            setLeader(s);
                                        }else{
                                            answeredServers.remove(s);
                                            if(answeredServers.isEmpty()){
                                                coordinator_for_nomination = true;
                                                heldElection();
                                            }
                                        }
                                    } catch (InterruptedException e) {
                                        e.printStackTrace();
                                    }
                                }
                            };
                            t2.start();
                        }

                    }
                } catch (InterruptedException | IOException e) {
                    e.printStackTrace();
                }
            }
        };
        t.start();
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

    public Server getHighestServer(){
        int currentPriority = Integer.MIN_VALUE;
        AtomicReference<Server> server1 = new AtomicReference<>(ChatServer.thisServer);
        servers.forEach((s, server) -> {
            int serverPriority = getPriorityNumber(server.getServerId());
            if (serverPriority > currentPriority){
                server1.set(server);
            }
        });
        return server1.get();
    }

    public Server getHighestServer(ArrayList<String> servers){
        int currentPriority = Integer.MIN_VALUE;
        AtomicReference<String> server1 = new AtomicReference<>(null);
        servers.forEach(s -> {
            int serverPriority = getPriorityNumber(s);
            if (serverPriority > currentPriority){
                server1.set(s);
            }
        });
        return this.servers.get(server1.get());
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

    public void sendAnswer(String serverId) {
        Server server = servers.get(serverId);
        try {
            Socket socket = new Socket(server.getAddress(),server.getCoordinationPort());
            ServerConnectionHandler connectionHandler = new ServerConnectionHandler(socket);

            ElectionAnswerReq electionAnswerReq = new ElectionAnswerReq(ChatServer.serverId);
            connectionHandler.send(gson.toJson(electionAnswerReq));
            connectionHandler.closeConnection();

            waiting_for_nomination_or_coordinator = true;
        } catch (IOException e) {
            e.printStackTrace();
        }

        Thread t = new Thread(){
            @Override
            public void run() {
                try {
                    sleep(nomination_or_coordinator_wait_time_T4);
                    if(waiting_for_nomination_or_coordinator){
                        waiting_for_nomination_or_coordinator = false;
                        heldElection();
                    }else{

                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        t.start();
    }

    public void sendCoordinatorToLower(String serverId) {
        HashMap<String,Server> lowerPriorityServers = getAllLowerServers(serverId);
        setLeader(ChatServer.thisServer);
        leaderBreadcast(lowerPriorityServers);
    }
}
