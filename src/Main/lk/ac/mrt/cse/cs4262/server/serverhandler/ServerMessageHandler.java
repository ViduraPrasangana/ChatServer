package lk.ac.mrt.cse.cs4262.server.serverhandler;

import com.google.gson.Gson;
import lk.ac.mrt.cse.cs4262.server.ChatServer;
import lk.ac.mrt.cse.cs4262.server.Constant;
import lk.ac.mrt.cse.cs4262.server.FastBullyService;
import lk.ac.mrt.cse.cs4262.server.clienthandler.ClientConnectionHandler;
import lk.ac.mrt.cse.cs4262.server.gossiphandler.GossipHandler;
import lk.ac.mrt.cse.cs4262.server.model.Client;
import lk.ac.mrt.cse.cs4262.server.model.Server;
import lk.ac.mrt.cse.cs4262.server.model.request.*;
import lk.ac.mrt.cse.cs4262.server.model.response.GossipDataRes;
import lk.ac.mrt.cse.cs4262.server.model.response.NewIdentityRes;
import lk.ac.mrt.cse.cs4262.server.model.response.RoomChange;
import org.json.simple.JSONObject;

import javax.swing.text.View;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class ServerMessageHandler {
    public static ServerMessageHandler instance;
    private final Gson gson;
    private GossipHandler gossipHandler;

    private ServerMessageHandler(){
        gson = new Gson();
    }

    public static synchronized ServerMessageHandler getInstance(){
        if(instance == null){
            instance = new ServerMessageHandler();
        }
        return instance;
    }

    public void handleMessage(JSONObject message, ServerConnectionHandler connectionHandler) {
        String type = (String) message.get("type");

        switch (type){
            case Constant.TYPE_IMUP -> {
                try{
                    ImUpReq imUpReq = gson.fromJson(message.toJSONString(),ImUpReq.class);
                    ChatServer.servers.get(imUpReq.getServerId()).setAlive(true);
                    ArrayList<String> activeServers = getActiveServers();
                    ViewReq viewReq = new ViewReq(activeServers);
                    String request = gson.toJson(viewReq);
                    connectionHandler.send(request);
                    connectionHandler.closeConnection();
                } catch (IOException e){
                    e.printStackTrace();
                }

            }
            case Constant.TYPE_VIEW -> {
                ViewReq viewReq = gson.fromJson(message.toJSONString(), ViewReq.class);
                for (String serverId:viewReq.getActiveServers()) {
                    ChatServer.servers.get(serverId).setAlive(true);
                }
            }
            case Constant.TYPE_COORDINATOR -> {
                CoordinatorReq leader = gson.fromJson(message.toJSONString(),CoordinatorReq.class);
                FastBullyService.leader = leader.getServerId();
            }
            case Constant.TYPE_GOSSIPINGREQ -> {
                System.out.println("gossip req");
                System.out.println(message.toJSONString());
                GossipDataReq gossipDataReq = gson.fromJson(message.toJSONString(),GossipDataReq.class);
                gossipHandler.handleGossipReq(gossipDataReq,connectionHandler);
            }
            case Constant.TYPE_GOSSIPINGRES -> {
                System.out.println("gossip res");
                System.out.println(message.toJSONString());
                GossipDataRes gossipDataRes = gson.fromJson(message.toJSONString(),GossipDataRes.class);
                gossipHandler.handleGossipRes(gossipDataRes,connectionHandler);
                connectionHandler.closeConnection();
            }

        }
    }
    public ArrayList<String> getActiveServers(){
        ArrayList<String> activeServers = new ArrayList<>();
        ChatServer.servers.forEach((s, server) -> {
            if(server.isAlive()){
                activeServers.add(server.getServerId());
            }
        });
        return activeServers;
    }

    public void setGossipHandler(GossipHandler gossipHandler) {
        this.gossipHandler = gossipHandler;
    }
}
