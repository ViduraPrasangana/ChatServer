package lk.ac.mrt.cse.cs4262.server.serverhandler;

import com.google.gson.Gson;
import lk.ac.mrt.cse.cs4262.server.ChatServer;
import lk.ac.mrt.cse.cs4262.server.Constant;
import lk.ac.mrt.cse.cs4262.server.FastBullyService;
import lk.ac.mrt.cse.cs4262.server.gossiphandler.GossipHandler;
import lk.ac.mrt.cse.cs4262.server.model.request.*;
import lk.ac.mrt.cse.cs4262.server.model.response.GossipDataRes;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;

public class ServerMessageHandler {
    public static ServerMessageHandler instance;
    private final Gson gson;
    private GossipHandler gossipHandler;
    private FastBullyService fastBullyService;

    private ServerMessageHandler(){
        gson = new Gson();
    }

    public void setFastBullyService(FastBullyService fastBullyService) {
        this.fastBullyService = fastBullyService;
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
                    System.out.println("Server "+imUpReq.getServerId()+" activated.");
                    ChatServer.servers.get(imUpReq.getServerId()).setAlive(true);
                    ArrayList<String> activeServers = getActiveServers();

                    // Set Alive the imup server
                    ChatServer.servers.get(imUpReq.getServerId()).setAlive(true);


                    ViewReq viewReq = new ViewReq(ChatServer.thisServer.getServerId(),new ArrayList<>(fastBullyService.getViews().values()));
                    String request = gson.toJson(viewReq);
                    connectionHandler.closeConnection();
                    Socket socket = new Socket(ChatServer.servers.get(imUpReq.getServerId()).getAddress(),ChatServer.servers.get(imUpReq.getServerId()).getCoordinationPort());
                    connectionHandler = new ServerConnectionHandler(socket);
//                    if (connectionHandler.getSocket().isClosed()){
//                        Socket socket1 = new Socket(ChatServer.servers.get(imUpReq.getServerId()).getAddress(),ChatServer.servers.get(imUpReq.getServerId()).getCoordinationPort());
//                        connectionHandler = new ServerConnectionHandler(socket1);
//                    }else {
                    connectionHandler.send(request);
                    connectionHandler.closeConnection();
//                    }

                    // connectionHandler.closeConnection();
                } catch (IOException e){
                    e.printStackTrace();
                }

            }
            case Constant.TYPE_VIEW -> {
                ViewReq viewReq = gson.fromJson(message.toJSONString(), ViewReq.class);
                System.out.println("View Message from server "+viewReq.getServerId());
                if(fastBullyService.waiting_for_view){
                    fastBullyService.updateView(viewReq.getView());
                }

//                ArrayList<String> activeServers = viewReq.getActiveServers();
//                if ( activeServers == null || activeServers.size()==0){
//                    System.out.println("No of active servers are 0");
//                }else {
//                    ChatServer.servers.get(viewReq.getServerId()).setAlive(true);
//                    // TODO set all received servers to alive
//
//                }

            }
            case Constant.TYPE_ELECTION -> {
                ElectionReq electionReq = gson.fromJson(message.toJSONString(),ElectionReq.class);
                System.out.println("Election, host by :"+electionReq.getServerId());
            }

            case Constant.TYPE_COORDINATOR -> {
                CoordinatorReq leader = gson.fromJson(message.toJSONString(),CoordinatorReq.class);
                if(fastBullyService.waiting_for_coordinator){
                    FastBullyService.leader = leader.getServerId();
                }
            }
            case Constant.TYPE_ELECTIONANSWER -> {
                ElectionAnswerReq electionAnswerReq = gson.fromJson(message.toJSONString(),ElectionAnswerReq.class);
                if(fastBullyService.waiting_for_answer){

                }
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

    public void printViewMessage(ArrayList<String> activeServers){
        System.out.print(" View: [");
        for (String server: activeServers) {
            System.out.print(" "+ server+" ");
        }
        System.out.println(" ]");
    }
}
