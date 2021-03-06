package lk.ac.mrt.cse.cs4262.server.serverhandler;

import com.google.gson.Gson;
import lk.ac.mrt.cse.cs4262.server.ChatServer;
import lk.ac.mrt.cse.cs4262.server.Constant;
import lk.ac.mrt.cse.cs4262.server.FastBullyService;
import lk.ac.mrt.cse.cs4262.server.gossiphandler.GossipHandler;
import lk.ac.mrt.cse.cs4262.server.model.Server;
import lk.ac.mrt.cse.cs4262.server.model.request.*;
import lk.ac.mrt.cse.cs4262.server.model.response.*;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ServerMessageHandler {
    public static ServerMessageHandler instance;
    private final Gson gson;
    private GossipHandler gossipHandler;
    private FastBullyService fastBullyService;
    private Logger logger =  Logger.getLogger(ServerMessageHandler.class);

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

    public synchronized void handleMessage(JSONObject message, ServerConnectionHandler connectionHandler) {
        String type = (String) message.get("type");

        switch (type){
            case Constant.TYPE_ASKCLIENT -> {
                LeaderAskClientReq leaderAskClientReq = gson.fromJson(message.toJSONString(),LeaderAskClientReq.class);
                logger.info("Ask for 'is client in any server' msg received. client - %s".formatted(leaderAskClientReq.getClientId()));

                boolean isIn = gossipHandler.isInClient(leaderAskClientReq.getClientId());
                LeaderAskClientRes leaderAskClientRes = new LeaderAskClientRes(isIn);

                logger.info("Client is in one of servers - %s".formatted(isIn));

                try {
                    logger.info("Sending response to server".formatted(isIn));
                    connectionHandler.send((gson.toJson(leaderAskClientRes)));
                    connectionHandler.closeConnection();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            case Constant.TYPE_ASKALLROOM -> {
                LeaderAskAllRoomsReq leaderAskAllRoomsReq = gson.fromJson(message.toJSONString(),LeaderAskAllRoomsReq.class);
                logger.info("Ask for 'all rooms' msg received.");

                String[] rooms = gossipHandler.getGlobalChatrooms();
                LeaderAskAllRoomsRes leaderAskAllRoomsRes = new LeaderAskAllRoomsRes(rooms);

                try {
                    logger.info("Sending response to server");
                    connectionHandler.send((gson.toJson(leaderAskAllRoomsRes)));
                    connectionHandler.closeConnection();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            case Constant.TYPE_ASKROOM -> {
                LeaderAskRoomReq leaderAskRoomReq = gson.fromJson(message.toJSONString(),LeaderAskRoomReq.class);
                logger.info("Ask for 'is room in any server' msg received. room - %s".formatted(leaderAskRoomReq.getRoomId()));

                boolean isIn = gossipHandler.isInRoom(leaderAskRoomReq.getRoomId());
                LeaderAskRoomRes leaderAskRoomRes = new LeaderAskRoomRes(isIn);

                logger.info("Room is in one of servers - %s".formatted(isIn));

                try {
                    logger.info("Sending response to server");
                    connectionHandler.send((gson.toJson(leaderAskRoomRes)));
                    connectionHandler.closeConnection();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            case Constant.TYPE_ASKSERVERROOM -> {
                LeaderAskServerRoomReq leaderAskRoomReq = gson.fromJson(message.toJSONString(),LeaderAskServerRoomReq.class);
                logger.info("Ask for 'what is the server of room' msg received. room - %s".formatted(leaderAskRoomReq.getRoomId()));

                Server s = gossipHandler.getServerOfRoom(leaderAskRoomReq.getRoomId());
                LeaderAskServerRoomRes leaderAskRoomRes = new LeaderAskServerRoomRes(s.getServerId());


                try {
                    logger.info("Sending response to server");
                    connectionHandler.send((gson.toJson(leaderAskRoomRes)));
                    connectionHandler.closeConnection();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            case Constant.TYPE_IMUP -> {
                try{
                    ImUpReq imUpReq = gson.fromJson(message.toJSONString(),ImUpReq.class);
                    logger.info("ImUp msg received from %s".formatted(imUpReq.getServerId()));

                    ArrayList<String> view = new ArrayList<>(fastBullyService.getView());
                    view.add(ChatServer.serverId);
                    ViewReq viewReq = new ViewReq(ChatServer.thisServer.getServerId(),view);
                    String request = gson.toJson(viewReq);
                    connectionHandler.closeConnection();

                    Socket socket = new Socket(ChatServer.servers.get(imUpReq.getServerId()).getAddress(),ChatServer.servers.get(imUpReq.getServerId()).getCoordinationPort());
                    ServerConnectionHandler connectionHandler2 = new ServerConnectionHandler(socket);

                    connectionHandler2.send(request);
                    connectionHandler2.closeConnection();
                    logger.info("Sent view msg to %s , %s".formatted(imUpReq.getServerId(), Arrays.toString(view.toArray())));

                    if(!fastBullyService.getView().contains(imUpReq.getServerId())){
                        fastBullyService.getView().add(imUpReq.getServerId());
                    }
                } catch (IOException e){
                    e.printStackTrace();
                }

            }
            case Constant.TYPE_VIEW -> {
                ViewReq viewReq = gson.fromJson(message.toJSONString(), ViewReq.class);
//                System.out.println("View Message from server "+viewReq.getServerId());
                if(fastBullyService.waiting_for_view){
                    logger.info("View message received from %s".formatted(viewReq.getServerId()));
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
                logger.info("Election msg received from %s".formatted(electionReq.getServerId()));
                if(fastBullyService.getPriorityNumber(ChatServer.serverId)
                                >fastBullyService.getPriorityNumber(electionReq.getServerId())){
                    fastBullyService.sendAnswer(electionReq.getServerId());
                }
            }

            case Constant.TYPE_NOMINATION -> {
                NominationReq nominationReq = gson.fromJson(message.toJSONString(),NominationReq.class);
                logger.info("Nomination msg received from %s".formatted(nominationReq.getServerId()));
                if(fastBullyService.getPriorityNumber(ChatServer.serverId)
                        >fastBullyService.getPriorityNumber(nominationReq.getServerId())){
                    fastBullyService.waiting_for_nomination_or_coordinator = false;
                    fastBullyService.sendCoordinatorToLower(nominationReq.getServerId());
                }
            }

            case Constant.TYPE_COORDINATOR -> {
                CoordinatorReq leader = gson.fromJson(message.toJSONString(),CoordinatorReq.class);
                logger.info("Coordinator message received from %s".formatted(leader.getServerId()));

//                if(fastBullyService.waiting_for_coordinator || fastBullyService.waiting_for_nomination_or_coordinator){
                    logger.info("Leader is %s".formatted(leader.getServerId()));
                    FastBullyService.leader = leader.getServerId();
                    fastBullyService.waiting_for_nomination_or_coordinator = false;
                    fastBullyService.waiting_for_coordinator = false;
                    //TODO: cancel election
//                }
                //TODO: cancel election
            }
            case Constant.TYPE_ELECTIONANSWER -> {
                ElectionAnswerReq electionAnswerReq = gson.fromJson(message.toJSONString(),ElectionAnswerReq.class);
                logger.info("Election answer received from %s".formatted(electionAnswerReq.getServerId()));
                if(fastBullyService.waiting_for_answer){
                    fastBullyService.getAnsweredServers().add(electionAnswerReq.getServerId());
                }
            }
            case Constant.TYPE_GOSSIPINGREQ -> {
//                System.out.println("gossip req");
//                System.out.println(message.toJSONString());
                GossipDataReq gossipDataReq = gson.fromJson(message.toJSONString(),GossipDataReq.class);
                gossipHandler.handleGossipReq(gossipDataReq,connectionHandler);
            }
            case Constant.TYPE_GOSSIPINGRES -> {
//                System.out.println("gossip res");
//                System.out.println(message.toJSONString());
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
