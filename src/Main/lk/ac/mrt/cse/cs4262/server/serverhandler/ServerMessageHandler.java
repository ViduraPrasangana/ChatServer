package lk.ac.mrt.cse.cs4262.server.serverhandler;

import com.google.gson.Gson;
import lk.ac.mrt.cse.cs4262.server.ChatServer;
import lk.ac.mrt.cse.cs4262.server.Constant;
import lk.ac.mrt.cse.cs4262.server.FastBullyService;
import lk.ac.mrt.cse.cs4262.server.clienthandler.ClientConnectionHandler;
import lk.ac.mrt.cse.cs4262.server.model.Client;
import lk.ac.mrt.cse.cs4262.server.model.Server;
import lk.ac.mrt.cse.cs4262.server.model.request.CoordinatorReq;
import lk.ac.mrt.cse.cs4262.server.model.request.ImUpReq;
import lk.ac.mrt.cse.cs4262.server.model.request.NewIdentityReq;
import lk.ac.mrt.cse.cs4262.server.model.request.ViewReq;
import lk.ac.mrt.cse.cs4262.server.model.response.NewIdentityRes;
import lk.ac.mrt.cse.cs4262.server.model.response.RoomChange;
import org.json.simple.JSONObject;

import javax.swing.text.View;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

public class ServerMessageHandler {
    public static ServerMessageHandler instance;
    private final Gson gson;

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
                    if(connectionHandler.getSocket().isClosed() || connectionHandler.getSocket().isOutputShutdown()){
                        Socket socket = new Socket(ChatServer.thisServer.getAddress(),ChatServer.thisServer.getCoordinationPort());
                        connectionHandler = new ServerConnectionHandler(socket,this);
                    }
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
}
