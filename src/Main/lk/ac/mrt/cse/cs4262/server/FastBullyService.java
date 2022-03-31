package lk.ac.mrt.cse.cs4262.server;

import com.google.gson.Gson;
import lk.ac.mrt.cse.cs4262.server.model.Server;
import lk.ac.mrt.cse.cs4262.server.model.request.*;
import lk.ac.mrt.cse.cs4262.server.serverhandler.ServerConnectionHandler;
import lk.ac.mrt.cse.cs4262.server.serverhandler.ServerMessageHandler;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class FastBullyService extends Thread{
    private HashMap<String, Server> servers;
    private ArrayList<String> view;
    ServerMessageHandler messageHandler;
    private final Gson gson;
    public static String leader;

    public final int ask_wait_time_T1 = 5000;
    public boolean waiting_for_ask = false;
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

    private Logger logger =  Logger.getLogger(FastBullyService.class);


//    ArrayList<String> activeServers;

    public FastBullyService(){
        this.servers = ChatServer.servers;
        gson = new Gson();
        messageHandler = ServerMessageHandler.getInstance();
        messageHandler.setFastBullyService(this);
        view = new ArrayList<>();
    }

    public void imUp(){
        ImUpReq imUpReq = new ImUpReq(ChatServer.thisServer.getServerId());
        String message = gson.toJson(imUpReq);
        int total_servers = ChatServer.servers.size();
        AtomicInteger active_server_count = new AtomicInteger();
        logger.info("Sending I'm up message to %s servers".formatted(servers.size()));
        servers.forEach((s, server) -> {
            if(!Objects.equals(server.getServerId(), ChatServer.thisServer.getServerId())){
                try{
                    Socket socket = new Socket(server.getAddress(),server.getCoordinationPort());
                    ServerConnectionHandler connectionHandler = new ServerConnectionHandler(socket);
                    //server.setAlive(true);

                    // Send Imup Requests
                    connectionHandler.send(message);
                    connectionHandler.closeConnection();
                    logger.info("Sent ImUp to %s".formatted(s));
                    active_server_count.set(active_server_count.get() + 1);
                } catch (IOException e) {
                    server.setAlive(false);
                    logger.warn("Failed to send ImUp to %s".formatted(s));
//                    System.out.println("Server "+server.getServerId()+" is not activated.");
                    //e.printStackTrace();
                }
            }
        });
        waiting_for_view = true;

        Thread t = new Thread(){
            @Override
            public void run() {
                try {
                    logger.info("Waiting for view responses T2 time %s ms".formatted(view_wait_time_T2));
                    sleep(view_wait_time_T2);
                    waiting_for_view = false;
                    logger.info("Waiting is over for view responses");
                    if(view.isEmpty()){ //TODO: add this view to list
                        logger.info("No view response received. I'm the leader");
                        setLeader(ChatServer.thisServer);
                    }else{
                        HashMap<String, Server> higherPriorityServers = getAllHigherServers(ChatServer.serverId);
                        HashMap<String, Server> lowerPriorityServers = getAllLowerServers(ChatServer.serverId);
                        if (higherPriorityServers.size() == 0 && lowerPriorityServers.size() !=0){
                            logger.info("I'm the highest priority view. I'm the leader");
                            setLeader(ChatServer.thisServer);
                            leaderBreadcast(lowerPriorityServers);
                        }else {
                            logger.info("Highest priority server and leader is %s ms".formatted(getHighestServer().getServerId()));
                            setLeader(getHighestServer());
                        }
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        t.start();



//        if (active_server_count.get() == 0){
//            setLeader(ChatServer.thisServer);
//            System.out.println("There are no any servers alive.");
//            System.out.println("I'm the Leader");
//        }

    }

    public ArrayList<String> getView() {
        return view;
    }

    public ArrayList<String> getAnsweredServers() {
        return answeredServers;
    }

    public void askLeader(String request){
        Server l = servers.get(leader);
        Socket socket = null;
        try {
            socket = new Socket(l.getAddress(),l.getCoordinationPort());
            ServerConnectionHandler connectionHandler = new ServerConnectionHandler(socket);

            connectionHandler.send(request);
            connectionHandler.closeConnection();
            waiting_for_ask = true;
        } catch (IOException e) {
            logger.info("Connection to leader %s failed".formatted(leader));
        }

        Thread t = new Thread(){
            @Override
            public void run() {
                try {
                    sleep(ask_wait_time_T1);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                if(waiting_for_ask){
                    waiting_for_ask = false;
                    heldElection();
                }
            }
        };

    }

    public synchronized void updateView(ArrayList<String> view){
        logger.info("Current view %s".formatted(List.of(this.view).toArray()));
        logger.info("Incoming view %s".formatted(List.of(view).toArray()));
        for (String v :
                view) {
            if(!this.view.contains(v) && !Objects.equals(ChatServer.serverId, v)){
                this.view.add(v);
            }
        }

        Iterator<String> i = this.view.iterator();
        while (i.hasNext()) {
            String s = i.next(); // must be called before you can call i.remove()
            boolean in = false;
            for (String v : view) {
                if (v.equals(s)){
                    in = true;
                }
            }
            if(!in){
                i.remove();
            }

        }
        logger.info("Updated view %s".formatted(List.of(this.view).toArray()));
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
        logger.info("Broadcasting coordinator message");
        view.forEach((s) -> {
            Server server = this.servers.get(s);
            try{
                Socket socket = new Socket(server.getAddress(),server.getCoordinationPort());
                ServerConnectionHandler connectionHandler = new ServerConnectionHandler(socket);
                CoordinatorReq leader = new CoordinatorReq(ChatServer.thisServer.getServerId());

                connectionHandler.send(gson.toJson(leader));
                connectionHandler.closeConnection();

                logger.info("Coordinator msg sent to %s".formatted(server.getServerId()));
            } catch (IOException e) {
                logger.warn("Failed to send coordinator msg to %s".formatted(server.getServerId()));
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
        logger.info("Election started");
        ChatServer.electionStatus = true;
        HashMap<String, Server> higherPriorityServers = getAllHigherServers(ChatServer.thisServer.getServerId());
        if(higherPriorityServers.isEmpty()){
            logger.info("I'm the highest priority server. I'm the leader");
            setLeader(ChatServer.thisServer);
        }else{
            waiting_for_answer = true;
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
                        logger.info("election msg sent to %s".formatted(server.getServerId()));
                    } catch (IOException e) {
                        server.setAlive(false);
                        view.remove(server.getServerId());//TODO verify this
                        logger.info("Failed sending election msg to %s".formatted(server.getServerId()));
                        //e.printStackTrace();
                    }
                }
            });

            Thread t = new Thread(){
                @Override
                public void run() {
                    try {
                        logger.info("Waiting fot T2 %s for answer".formatted(answer_wait_time_T2));
                        sleep(answer_wait_time_T2);
                        waiting_for_answer = false;
                        if(answeredServers.isEmpty()){
                            logger.info("No answer is received. I'm the leader");
                            HashMap<String,Server> lowerPriorityServers = getAllLowerServers(ChatServer.serverId);
                            setLeader(ChatServer.thisServer);
                            leaderBreadcast(lowerPriorityServers);
                        }else{
                            final boolean[] coordinator_for_nomination = {false};
                            while (!coordinator_for_nomination[0]){
                                Server s = getHighestServer(answeredServers);
                                Socket socket = new Socket(s.getAddress(),s.getCoordinationPort());
                                ServerConnectionHandler connectionHandler = new ServerConnectionHandler(socket);

                                NominationReq nominationReq = new NominationReq(ChatServer.serverId);
                                waiting_for_coordinator = true;
                                connectionHandler.send(gson.toJson(nominationReq));
                                connectionHandler.closeConnection();
                                Thread t2 = new Thread(){
                                    @Override
                                    public void run() {
                                        try {
                                            sleep(coordinator_wait_time_T3);
                                            if(!waiting_for_coordinator){ //TODO their is a issue
//                                                setLeader(s);
                                                coordinator_for_nomination[0] = true;
                                            }else{
                                                logger.info("Coordinator msg didn't received from %s. trying with next one".formatted(s.getServerId()));
                                                answeredServers.remove(s);
                                                if(answeredServers.isEmpty()){
                                                    coordinator_for_nomination[0] = true;
                                                    logger.info("Answered server list is over. Restarting election");
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

        answeredServers = null;

    }

    public void notifyLeader(){
        servers.forEach((s, server) -> {

        });
    }

    public HashMap<String, Server> getAllLowerServers(String serverId){
        int currentPriority = getPriorityNumber(serverId);
        HashMap<String, Server> lowerPriorityServers = new HashMap<>();
        view.forEach((s) -> {
            int serverPriority = getPriorityNumber(s);
            if (serverPriority < currentPriority){
                lowerPriorityServers.put(s,this.servers.get(s));
            }
        });
        return lowerPriorityServers;
    }

    public Server getHighestServer(){
        int currentPriority = Integer.MIN_VALUE;
        AtomicReference<Server> server1 = new AtomicReference<>(ChatServer.thisServer);
        view.forEach((s) -> {
            int serverPriority = getPriorityNumber(s);
            if (serverPriority > currentPriority){
                server1.set(servers.get(s));
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
        view.forEach((s) -> {
            int serverPriority = getPriorityNumber(s);
            if (serverPriority > currentPriority){
                higherPriorityServers.put(s,this.servers.get(s));
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
