package lk.ac.mrt.cse.cs4262.server;

import com.google.gson.Gson;
import lk.ac.mrt.cse.cs4262.server.clienthandler.ClientMessageHandler;
import lk.ac.mrt.cse.cs4262.server.model.Server;
import lk.ac.mrt.cse.cs4262.server.model.request.*;
import lk.ac.mrt.cse.cs4262.server.model.response.LeaderAskAllRoomsRes;
import lk.ac.mrt.cse.cs4262.server.model.response.LeaderAskClientRes;
import lk.ac.mrt.cse.cs4262.server.model.response.LeaderAskRoomRes;
import lk.ac.mrt.cse.cs4262.server.model.response.LeaderAskServerRoomRes;
import lk.ac.mrt.cse.cs4262.server.serverhandler.ServerConnectionHandler;
import lk.ac.mrt.cse.cs4262.server.serverhandler.ServerMessageHandler;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class FastBullyService extends Thread{
    private HashMap<String, Server> servers;
    private ArrayList<String> view;
    ServerMessageHandler messageHandler;
    ClientMessageHandler clientMessageHandler;
    private final Gson gson;
    public static String leader;

    public final int ask_wait_time_T1 = 5000;
    public boolean waiting_for_ask = false;
    public final int view_wait_time_T2 = 10000;
    public boolean waiting_for_view = false;
    public final int answer_wait_time_T2 = 10000;
    public boolean waiting_for_answer = false;
    ArrayList<String> answeredServers;
    public final int coordinator_wait_time_T3 = 15000;
    public boolean waiting_for_coordinator = false;
    public boolean coordinator_for_nomination = false;
    public final int nomination_or_coordinator_wait_time_T4 = 20000;
    public boolean waiting_for_nomination_or_coordinator = false;

    private Logger logger =  Logger.getLogger(FastBullyService.class);


//    ArrayList<String> activeServers;

    public FastBullyService(){
        this.servers = ChatServer.servers;
        gson = new Gson();
        messageHandler = ServerMessageHandler.getInstance();
        clientMessageHandler = ClientMessageHandler.getInstance();
        messageHandler.setFastBullyService(this);
        clientMessageHandler.setFastBullyService(this);
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
//                    e.printStackTrace();
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

    public Object askLeader(String request){
        Server l = servers.get(leader);
        Socket socket = null;
        try {
            socket = new Socket();
            logger.info("Waiting for leader response T1 time %s".formatted(ask_wait_time_T1));
            socket.connect(new InetSocketAddress(l.getAddress(),l.getCoordinationPort()),ask_wait_time_T1);
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            JSONParser parser = new JSONParser();
            waiting_for_ask = true;

            out.write((request + "\n").getBytes("UTF-8"));
            out.flush();
            logger.info("Asking request sent to leader");

            String s = in.readLine();
            JSONObject message = (JSONObject) parser.parse(s);
            String type = (String) message.get("type");
            logger.info("Ask response received from leader");

            socket.close();
            waiting_for_ask = false;
            switch (type){
                case Constant.TYPE_ASKCLIENTRES -> {
                    LeaderAskClientRes leaderAskClientRes = gson.fromJson(message.toJSONString(),LeaderAskClientRes.class);
                    return leaderAskClientRes.isIn();
                }
                case Constant.TYPE_ASKROOMRES -> {
                    LeaderAskRoomRes leaderAskRoomRes = gson.fromJson(message.toJSONString(),LeaderAskRoomRes.class);
                    return leaderAskRoomRes.isIn();
                }
                case Constant.TYPE_ASKALLROOMRES -> {
                    LeaderAskAllRoomsRes leaderAskAllRoomsRes = gson.fromJson(message.toJSONString(),LeaderAskAllRoomsRes.class);
                    return leaderAskAllRoomsRes.getAllrooms();
                }
                case Constant.TYPE_ASKSERVERROOMRES -> {
                    LeaderAskServerRoomRes leaderAskAllRoomsRes = gson.fromJson(message.toJSONString(),LeaderAskServerRoomRes.class);
                    return ChatServer.servers.get(leaderAskAllRoomsRes.getServerId());
                }
            }

        } catch (IOException e) {
            logger.info("No response for %s. Starting election".formatted(ask_wait_time_T1));
            heldElection();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return true;
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
                                try{
                                    Socket socket = new Socket(s.getAddress(),s.getCoordinationPort());
                                    ServerConnectionHandler connectionHandler = new ServerConnectionHandler(socket);

                                    NominationReq nominationReq = new NominationReq(ChatServer.serverId);
                                    waiting_for_coordinator = true;
                                    logger.info("Sending nomination msg to %s".formatted(s.getServerId()));
                                    connectionHandler.send(gson.toJson(nominationReq));
                                    connectionHandler.closeConnection();
                                } catch (IOException e) {
                                    logger.info("Failed to send nomination msg to %s".formatted(s.getServerId()));
                                }
//                                Thread t2 = new Thread(){
//                                    @Override
//                                    public void run() {
                                        try {
                                            logger.info("Waiting for coordinator responses T3 time %s ms".formatted(coordinator_wait_time_T3));
                                            sleep(coordinator_wait_time_T3);
                                            logger.info("Waiting for coordinator is over");
                                            if(!waiting_for_coordinator){ //TODO their is a issue
//                                                setLeader(s);
                                                coordinator_for_nomination[0] = true;
                                            }else{
                                                logger.info("Coordinator msg didn't received from %s. trying with next one".formatted(s.getServerId()));
                                                answeredServers.remove(s.getServerId());
                                                if(answeredServers.isEmpty()){
                                                    coordinator_for_nomination[0] = true;
                                                    logger.info("Answered server list is over. Restarting election");
                                                    heldElection();
                                                }
                                            }
                                        } catch (InterruptedException e) {
                                            e.printStackTrace();
                                        }
//                                    }
//                                };
//                                t2.start();
                            }

                        }
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            };
            t.start();
        }

//        answeredServers = null;

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

            logger.info("Sending election answer to %s".formatted(serverId));

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
                    logger.info("Waiting fot coordination or nomination msg for T4 time %s".formatted(nomination_or_coordinator_wait_time_T4));
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
        logger.info("Sending coordinator msg to all lower priority servers");
        HashMap<String,Server> lowerPriorityServers = getAllLowerServers(serverId);
        setLeader(ChatServer.thisServer);
        leaderBreadcast(lowerPriorityServers);
    }
}
