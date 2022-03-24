package lk.ac.mrt.cse.cs4262.server.gossiphandler;

import com.google.gson.Gson;
import lk.ac.mrt.cse.cs4262.server.Connectable;
import lk.ac.mrt.cse.cs4262.server.Constant;
import lk.ac.mrt.cse.cs4262.server.chatroom.ChatroomHandler;
import lk.ac.mrt.cse.cs4262.server.clienthandler.ClientMessageHandler;
import lk.ac.mrt.cse.cs4262.server.model.Server;
import lk.ac.mrt.cse.cs4262.server.model.request.GossipDataReq;
import lk.ac.mrt.cse.cs4262.server.model.response.GossipDataRes;
import lk.ac.mrt.cse.cs4262.server.serverhandler.ServerConnectionHandler;
import lk.ac.mrt.cse.cs4262.server.serverhandler.ServerMessageHandler;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.management.Notification;
import javax.management.NotificationListener;

public class GossipHandler {

    private HashMap<String,ServerState> serverData;
    private final ArrayList<Server> servers;
    private Server thisServer;
    private Gson gson;
    private int gossipStateVersion = 1;
    private ChatroomHandler chatroomHandler;
    private ClientMessageHandler messageHandler;
    private ScheduledThreadPoolExecutor gossipExecutor = new ScheduledThreadPoolExecutor(1);
    private long gossipIntervalMs = 1000;
    private ScheduledFuture<?> taskFuture;
    private ServerMessageHandler serverMessageHandler;
    private Random random = new Random();
    private ServerConnectionHandler connectionHandler;
    private int gossipFanout;
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
        this.servers = new ArrayList<>(servers.values());
        serverData = new HashMap<>();
        serverMessageHandler = ServerMessageHandler.getInstance();
        serverMessageHandler.setGossipHandler(this);
        gossipFanout = servers.size() - 1;

        String[] a = new String[chatroomHandler.getChatroomList().size()];
        String[] chatrooms = chatroomHandler.getChatroomList().keySet().toArray(a);
        setLocalServerState(Constant.GOSSIPDATA_ROOMS,chatrooms);

        String[] b = new String[messageHandler.getClientsOnServer().size()];
        String[] clients = messageHandler.getClientsOnServer().keySet().toArray(b);
        setLocalServerState(Constant.GOSSIPDATA_CLIENTS,clients);
        setLocalServerState(Constant.GOSSIPDATA_ADDRESS,server.getAddress());
        setLocalServerState(Constant.GOSSIPDATA_CLIENTS,server.getCoordinationPort());

        gson = new Gson();


        random = new Random();
    }

    public void start() {
//        socketServer.start();
        taskFuture = gossipExecutor.scheduleAtFixedRate(()-> doGossip(),
                gossipIntervalMs,
                gossipIntervalMs,
                TimeUnit.MILLISECONDS);
    }
    public void doGossip() {
//        ArrayList<Server> knownClusterNodes = liveNodes();
        if (serverData.isEmpty()) {
            sendGossip(new ArrayList<>(servers), gossipFanout);
        } else {
            sendGossip(new ArrayList<>(serverData.values()), gossipFanout);
        }
    }
    private void sendGossip(ArrayList<Connectable> knownClusterNodes, int gossipFanout) {
        if (knownClusterNodes.isEmpty()) {
            return;
        }

        for (int i = 0; i < gossipFanout; i++) {
            Connectable server = pickRandomNode(knownClusterNodes);
            sendGossipTo(server);
        }
    }
    private Connectable pickRandomNode(ArrayList<Connectable> knownClusterNodes) {
        int randomNodeIndex = random.nextInt(knownClusterNodes.size());
        return knownClusterNodes.get(randomNodeIndex);
    }
    private void sendGossipTo(Connectable server) {
        try {
            Socket socket = new Socket(server.getAddress(),server.getCoordinationPort());
            connectionHandler = new ServerConnectionHandler(socket);
            connectionHandler.start();
            GossipDataReq gossipDataReq = new GossipDataReq(serverData);
            connectionHandler.send(gson.toJson(gossipDataReq));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void handleGossipReq(GossipDataReq gossipDataReq){
        HashMap<String, ServerState> incomingServerData = gossipDataReq.getServerData();
        merge(incomingServerData);

        HashMap<String, ServerState> diff = delta(this.serverData, incomingServerData);
        GossipDataRes gossipDataRes = new GossipDataRes(diff);
        if(connectionHandler !=null){
            try {
                connectionHandler.send(gson.toJson(gossipDataRes));
            } catch (IOException e){
                e.printStackTrace();
            }

        }
    }
    private void handleGossipRes(GossipDataReq gossipDataRes){
        HashMap<String, ServerState> incomingServerData = gossipDataRes.getServerData();
        merge(incomingServerData);
    }
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

}
