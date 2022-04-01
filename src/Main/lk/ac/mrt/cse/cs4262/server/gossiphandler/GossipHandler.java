package lk.ac.mrt.cse.cs4262.server.gossiphandler;

import com.google.gson.Gson;
import lk.ac.mrt.cse.cs4262.server.ChatServer;
import lk.ac.mrt.cse.cs4262.server.Connectable;
import lk.ac.mrt.cse.cs4262.server.Constant;
import lk.ac.mrt.cse.cs4262.server.FastBullyService;
import lk.ac.mrt.cse.cs4262.server.chatroom.ChatroomHandler;
import lk.ac.mrt.cse.cs4262.server.clienthandler.ClientMessageHandler;
import lk.ac.mrt.cse.cs4262.server.model.Server;
import lk.ac.mrt.cse.cs4262.server.model.request.GossipDataReq;
import lk.ac.mrt.cse.cs4262.server.model.response.GossipDataRes;
import lk.ac.mrt.cse.cs4262.server.serverhandler.ServerConnectionHandler;
import lk.ac.mrt.cse.cs4262.server.serverhandler.ServerMessageHandler;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class GossipHandler {

    private HashMap<String,ServerState> serverData;
    private final ArrayList<Server> servers;
    private Server thisServer;
    private Gson gson;
    private int gossipStateVersion = 1;
    private ChatroomHandler chatroomHandler;
    private ClientMessageHandler messageHandler;
    private ScheduledThreadPoolExecutor gossipExecutor = new ScheduledThreadPoolExecutor(1);
    private long gossipIntervalMs = 10000;
    private ScheduledFuture<?> taskFuture;
    private ServerMessageHandler serverMessageHandler;
    private Random random = new Random();
    private int gossipFanout;
    int c = 0;

    private Logger logger =  Logger.getLogger(GossipHandler.class);
    /**
     * Setup the client's lists, gossiping parameters, and parse the startup config file.
     * @throws SocketException
     * @throws InterruptedException
     * @throws UnknownHostException
     */
    public GossipHandler(Server server, HashMap<String,Server> servers) throws SocketException, InterruptedException, UnknownHostException {
        thisServer = server;
        chatroomHandler = ChatroomHandler.getInstance();
        messageHandler = ClientMessageHandler.getInstance();
        messageHandler.setGossipHandler(this);
        this.servers = new ArrayList<>(servers.values());
        serverData = new HashMap<>();
        serverMessageHandler = ServerMessageHandler.getInstance();
        serverMessageHandler.setGossipHandler(this);
        chatroomHandler.setGossipHandler(this);
        gossipFanout = servers.size();

        updateRooms();

        updateClients();
        setLocalServerState(Constant.GOSSIPDATA_ADDRESS,server.getAddress());
        setLocalServerState(Constant.GOSSIPDATA_COORDINATIONPORT,server.getCoordinationPort());

        gson = new Gson();

        random = new Random();
    }

    public void updateClients(){
        String[] b = new String[messageHandler.getClientsOnServer().size()];
        String[] clients = messageHandler.getClientsOnServer().keySet().toArray(b);
        setLocalServerState(Constant.GOSSIPDATA_CLIENTS,clients);
    }

    public void updateRooms(){
        String[] a = new String[chatroomHandler.getChatroomList().size()];
        String[] chatrooms = chatroomHandler.getChatroomList().keySet().toArray(a);
        setLocalServerState(Constant.GOSSIPDATA_ROOMS,chatrooms);
    }

    public void start() {
//        socketServer.start();
        logger.info("Gossipping Started!");
        taskFuture = gossipExecutor.scheduleAtFixedRate(this::doGossip,
                gossipIntervalMs,
                gossipIntervalMs,
                TimeUnit.MILLISECONDS);
    }
    public void doGossip() {
//        if (serverData.isEmpty()) {
//            System.out.println("using servers");
            sendGossip(new ArrayList<>(servers), gossipFanout);
//        } else {
//            System.out.println("using server data");
//            sendGossip(new ArrayList<>(serverData.values()), gossipFanout);
//        }
    }
    private void sendGossip(ArrayList<Connectable> knownClusterNodes, int gossipFanout) {
        if (knownClusterNodes.isEmpty()) {
            logger.info("Cluster is empty.");
//            System.out.println("clusters empty");
            return;
        }
        for (int i = 0; i < knownClusterNodes.size(); i++) {
            logger.info("Cluster Size is %s : %s".formatted(knownClusterNodes.size()));
            Connectable server = knownClusterNodes.get(i);
            if(doSend()){
                logger.info("Sending to node %s : %s".formatted(server.getAddress(),server.getCoordinationPort()));
                sendGossipTo(server);
            }
        }
    }
    private boolean doSend() {
        return random.nextInt(10)<7.5;
    }
    private void sendGossipTo(Connectable server) {
        try {
            Socket socket = new Socket(server.getAddress(),server.getCoordinationPort());
            ServerConnectionHandler connectionHandler = new ServerConnectionHandler(socket);
            connectionHandler.start();
            GossipDataReq gossipDataReq = new GossipDataReq(serverData);
//            System.out.println(gson.toJson(gossipDataReq));
            connectionHandler.send(gson.toJson(gossipDataReq));
            logger.info("send Gossip to %s".formatted(server.getAddress()));
        } catch (IOException e) {
            logger.warn("Failed to send Gossip to %s".formatted(server.getAddress()));
//            e.printStackTrace();
        }
    }
    public void handleGossipReq(GossipDataReq gossipDataReq, ServerConnectionHandler connectionHandler){
//        System.out.println("before gossip req");
//        System.out.println(gson.toJson(serverData));
        HashMap<String, ServerState> incomingServerData = gossipDataReq.getServerData();
        merge(incomingServerData);
//        System.out.println("after merge req");
//        System.out.println(gson.toJson(serverData));
        HashMap<String, ServerState> diff = delta(this.serverData, incomingServerData);
        GossipDataRes gossipDataRes = new GossipDataRes(diff);
        if(connectionHandler !=null){
            try {
                connectionHandler.send(gson.toJson(gossipDataRes));
            } catch (IOException e){
                logger.warn("Failed to handle Gossip Request");
//                e.printStackTrace();
            }

        }
    }
    public void handleGossipRes(GossipDataRes gossipDataRes, ServerConnectionHandler connectionHandler){
//        System.out.println("before gossip res");
//        System.out.println(gson.toJson(serverData));
        HashMap<String, ServerState> incomingServerData = gossipDataRes.getServerData();
        merge(incomingServerData);
        logger.info("Merge the Recieved Data with Existing Data");
//        System.out.println("after merge res");
//        System.out.println(gson.toJson(serverData));
    }

//
    public HashMap<String, ServerState> delta(HashMap<String, ServerState> fromMap, HashMap<String, ServerState> toMap) {
        HashMap<String, ServerState> delta = new HashMap<>();
        for (String key : fromMap.keySet()) {
            if (!toMap.containsKey(key)) {
                delta.put(key, fromMap.get(key));
                continue;
            }
            ServerState fromState = fromMap.get(key);
            ServerState toState = toMap.get(key);
            ServerState diffState = fromState.diff(toState);
            if (!diffState.isEmpty()) {
                delta.put(key, diffState);
            }
        }
        return delta;
    }
    public void merge(HashMap<String, ServerState> otherState) {
        HashMap<String, ServerState> diff = delta(otherState, serverData);
        for (String diffKey : diff.keySet()) {
            if(!serverData.containsKey(diffKey)) {
                serverData.put(diffKey, diff.get(diffKey));
            } else {
                ServerState stateMap = serverData.get(diffKey);
                stateMap.putAll(diff.get(diffKey));
            }
        }
    }
    public void setLocalServerState(String key, Object object){
        ServerState nodeState = serverData.get(thisServer.getServerId());
        if (nodeState == null) {
            nodeState = new ServerState();
            serverData.put(thisServer.getServerId(), nodeState);
        }
        nodeState.putValue(key, new VersionedValue(object, incremenetVersion()));
    }
    private int incremenetVersion() {
        return gossipStateVersion++;
    }

    public boolean isInClient(String client){
        AtomicBoolean isIn = new AtomicBoolean(false);
        serverData.forEach((s, serverState) -> {
            String[] clients = (String[]) serverState.getValues().get(Constant.GOSSIPDATA_CLIENTS).getValue();
            for (String s1 :
                    clients) {
                if (s1.equals(client)) {
                    isIn.set(true);
                    }
                }
        });
        return isIn.get();
    }

    public String[] getGlobalChatrooms() {
        ArrayList<String> chatrooms = new ArrayList<>();

        serverData.forEach((s, serverState) -> {
            String[] rooms = (String[]) serverState.getValues().get(Constant.GOSSIPDATA_ROOMS).getValue();
            chatrooms.addAll(List.of(rooms));
        });
        return chatrooms.toArray(new String[0]);
    }

    public boolean isInRoom(String roomid) {
        AtomicBoolean isIn = new AtomicBoolean(false);
        serverData.forEach((s, serverState) -> {
            String[] rooms = (String[]) serverState.getValues().get(Constant.GOSSIPDATA_ROOMS).getValue();
            for (String r1 :
                    rooms) {
                if (r1.equals(roomid)) {
                    isIn.set(true);
                }
            }
        });
        return isIn.get();
    }

    public Server getServerOfRoom(String roomid) {
        AtomicReference<Server> server = new AtomicReference<>(null);
        serverData.forEach((s, serverState) -> {
            String[] rooms = (String[]) serverState.getValues().get(Constant.GOSSIPDATA_ROOMS).getValue();
            for (String r1 :
                    rooms) {
                if (r1.equals(roomid)) {
                    server.set(ChatServer.servers.get(s));
                }
            }
        });
        return server.get();
    }
}
