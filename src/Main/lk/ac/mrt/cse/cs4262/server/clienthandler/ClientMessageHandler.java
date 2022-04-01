package lk.ac.mrt.cse.cs4262.server.clienthandler;

import com.google.gson.Gson;
import lk.ac.mrt.cse.cs4262.server.ChatServer;
import lk.ac.mrt.cse.cs4262.server.Constant;
import lk.ac.mrt.cse.cs4262.server.FastBullyService;
import lk.ac.mrt.cse.cs4262.server.gossiphandler.GossipHandler;
import lk.ac.mrt.cse.cs4262.server.model.Request;
import lk.ac.mrt.cse.cs4262.server.serverhandler.ServerMessageHandler;
import lk.ac.mrt.cse.cs4262.server.model.Chatroom;
import lk.ac.mrt.cse.cs4262.server.chatroom.ChatroomHandler;
import lk.ac.mrt.cse.cs4262.server.model.Client;
import lk.ac.mrt.cse.cs4262.server.model.Server;
import lk.ac.mrt.cse.cs4262.server.model.request.*;
import lk.ac.mrt.cse.cs4262.server.model.response.*;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class ClientMessageHandler {
    private static ClientMessageHandler instance;
    private ChatroomHandler chatroomHandler;
    private ServerMessageHandler serverMessageHandler;
    private final Gson gson;
    private HashMap<String,Client> clientsOnServer;
    private GossipHandler gossipHandler;
    private FastBullyService fastBullyService;
    Logger logger = Logger.getLogger(ClientMessageHandler.class);

    public void setFastBullyService(FastBullyService fastBullyService) {
        this.fastBullyService = fastBullyService;
    }

    private ClientMessageHandler(){
        gson = new Gson();
        chatroomHandler = ChatroomHandler.getInstance();
        serverMessageHandler = ServerMessageHandler.getInstance();
        clientsOnServer = new HashMap<>();
    }

    public static synchronized ClientMessageHandler getInstance(){
        if(instance==null){
            instance = new ClientMessageHandler();
        }
        return instance;
    }

    public HashMap<String, Client> getClientsOnServer() {
        return clientsOnServer;
    }

    public void setGossipHandler(GossipHandler gossipHandler) {
        this.gossipHandler = gossipHandler;
    }

    public void setClientsOnServer(HashMap<String, Client> clientsOnServer) {
        this.clientsOnServer = clientsOnServer;
    }
    public void addClientToServer(Client client){
        clientsOnServer.put(client.getClientID(),client);
        gossipHandler.updateClients();
    }

    public void handleMessage(JSONObject message, ClientConnectionHandler connectionHandler) {
        String type = (String) message.get("type");
        switch (type){
            case Constant.TYPE_NEWIDENTITY -> {
                NewIdentityReq newIdentityReq = gson.fromJson(message.toJSONString(),NewIdentityReq.class);
                logger.info("New incoming identity request - %s".formatted(newIdentityReq.getIdentity()));
                /** Identity Validation **/
                String regex = "[a-zA-Z][a-zA-Z0-9]{2,15}";
                Pattern p = Pattern.compile(regex);
                Matcher m = p.matcher(newIdentityReq.getIdentity());
                NewIdentityRes newIdentityRes;

                boolean isIn;
                if(FastBullyService.leader.equals(ChatServer.serverId)){
                    logger.info("i'm the leader. Checking duplicate identity");
                    isIn = gossipHandler.isInClient(newIdentityReq.getIdentity());
                }else{
                    logger.info("I'm not the leader. Asking from leader");
                    LeaderAskClientReq leaderAskClientReq = new LeaderAskClientReq(newIdentityReq.getIdentity());
                    isIn = (boolean) fastBullyService.askLeader(gson.toJson(leaderAskClientReq));
                }

                if(m.matches() && !isIn){
                    logger.info("Identity does not exists in any server - %s".formatted(newIdentityReq.getIdentity()));
                    newIdentityRes = new NewIdentityRes("true");
                    Client client = new Client(newIdentityReq.getIdentity(), ChatServer.thisServer,connectionHandler);
                    connectionHandler.setClient(client);
                    addClientToServer(client);

                    Chatroom chatroom = ChatServer.thisServer.getChatroom();
                    chatroomHandler.addClientToChatRoom(client,chatroom);
                    RoomChange roomChange = new RoomChange(client.getClientID(),"",chatroom.getChatroomID());
                    broadcastToClients(gson.toJson(roomChange),chatroom.getClientList());

                    //TODO: Inform other servers about new identity : Gossiping
                }else{
                    logger.info("Identity exists in  server or minimum requirements does not match - %s".formatted(newIdentityReq.getIdentity()));
                    newIdentityRes = new NewIdentityRes("false");
                }
                String response = gson.toJson(newIdentityRes);
                connectionHandler.send(response);
                logger.info("Sending identity response to client");

            }
            case Constant.TYPE_LIST -> {
                logger.info("List request incoming from - %s".formatted(connectionHandler.getClient().getClientID()));
                String[] rooms;
                if(FastBullyService.leader.equals(ChatServer.serverId)){
                    logger.info("i'm the leader. getting all rooms details");
                    rooms = gossipHandler.getGlobalChatrooms();
                }else{
                    logger.info("I'm not the leader. Asking from leader");
                    LeaderAskAllRoomsReq leaderAskAllRoomsReq = new LeaderAskAllRoomsReq();
                    rooms = (String[]) fastBullyService.askLeader(gson.toJson(leaderAskAllRoomsReq));
                }

                RoomListRes roomListRes = new RoomListRes(rooms);
                connectionHandler.send(gson.toJson(roomListRes));
            }
            case Constant.TYPE_WHO -> {
                Chatroom chatroom = connectionHandler.getClient().getChatroom();

                RoomContentsRes roomContentsRes = new RoomContentsRes(
                        chatroom.getChatroomID(),
                        chatroom.getClientList().stream().map(Client::getClientID).toArray(String[]::new),
                        connectionHandler.getClient().getClientID()
                );
                connectionHandler.send(gson.toJson(roomContentsRes));
            }
            case Constant.TYPE_CREATEROOM -> {
                CreateRoomReq createRoomReq = gson.fromJson(message.toJSONString(),CreateRoomReq.class);
                Client client = connectionHandler.getClient();
                logger.info("New incoming create room request - %s".formatted(createRoomReq.getRoomid()));

                /** RoomId Validation **/
                String regex = "[a-zA-Z][a-zA-Z0-9]{2,15}";
                Pattern p = Pattern.compile(regex);
                Matcher m = p.matcher(createRoomReq.getRoomid());

                CreateRoomRes createRoomRes = new CreateRoomRes(createRoomReq.getRoomid(),false);
                RoomChange roomChange;

                boolean isIn;
                if(FastBullyService.leader.equals(ChatServer.serverId)){
                    logger.info("i'm the leader. Checking duplicate room name");
                    isIn = gossipHandler.isInRoom(createRoomReq.getRoomid());
                }else{
                    logger.info("I'm not the leader. Asking from leader");
                    LeaderAskRoomReq leaderAskRoomReq = new LeaderAskRoomReq(createRoomReq.getRoomid());
                    isIn = (boolean) fastBullyService.askLeader(gson.toJson(leaderAskRoomReq));
                }
                logger.info("Room create response sent to client");

                if (m.matches() && !client.isOwner() && !isIn){
                    Chatroom oldRoom = client.getChatroom();
                    boolean result = chatroomHandler.createRoom(ChatServer.thisServer,createRoomReq.getRoomid(),client);
                    if (result){
                        createRoomRes = new CreateRoomRes(createRoomReq.getRoomid(),true);
                        roomChange = new RoomChange(client.getClientID(),oldRoom.getChatroomID(), createRoomReq.getRoomid());
                        connectionHandler.send(gson.toJson(createRoomRes));
                        connectionHandler.send(gson.toJson(roomChange));

                        logger.info("Room change broadcast to everyone in old room");
                        broadcastToClients(gson.toJson(roomChange),oldRoom.getClientList());
                    }else{
                        connectionHandler.send(gson.toJson(createRoomRes));
                    }
                }else{
                    connectionHandler.send(gson.toJson(createRoomRes));
                }
            }

            case Constant.TYPE_JOINROOM -> {
                JoinRoomReq joinRoomReq = gson.fromJson(message.toJSONString(),JoinRoomReq.class);
                Client client = connectionHandler.getClient();
                logger.info("New incoming join room request to %s".formatted(joinRoomReq.getRoomid()));


                //TODO: verify from **leader** chatroom exists and chatroom id
                Server serverOfRoom = gossipHandler.getServerOfRoom(joinRoomReq.getRoomid());

                if(connectionHandler.getClient().isOwner() || serverOfRoom == null){

                    RoomChange roomChange = new RoomChange(client.getClientID(), client.getChatroom().getChatroomID(),  client.getChatroom().getChatroomID());
                    connectionHandler.send(gson.toJson(roomChange));

                }else if(serverOfRoom.getServerId().equals(ChatServer.thisServer.getServerId())){
                    Chatroom formerRoom = client.getChatroom();

                    chatroomHandler.addClientToChatRoom(client,joinRoomReq.getRoomid());
                    RoomChange roomChange = new RoomChange(client.getClientID(),formerRoom.getChatroomID(), client.getChatroom().getChatroomID());

                    broadcastToClients(gson.toJson(roomChange), formerRoom.getClientList());
                    broadcastToClients(gson.toJson(roomChange), client.getChatroom().getClientList());
                }else {
                    Chatroom formerRoom = client.getChatroom();
                    client.getChatroom().removeClient(client.getClientID());
                    RouteRes routeRes = new RouteRes(joinRoomReq.getRoomid(), serverOfRoom.getAddress(),String.valueOf(serverOfRoom.getClientsPort()));
                    RoomChange roomChange = new RoomChange(client.getClientID(),formerRoom.getChatroomID(), joinRoomReq.getRoomid());
                    connectionHandler.send(gson.toJson(routeRes));
                    broadcastToClients(gson.toJson(roomChange),formerRoom.getClientList());
                    clientsOnServer.remove(client.getClientID());
                    gossipHandler.updateClients();
                }

            }
            case Constant.TYPE_MOVEJOIN -> {
                MoveJoinReq moveJoinReq = gson.fromJson(message.toJSONString(),MoveJoinReq.class);
                logger.info("New incoming movejoin room request to %s".formatted(moveJoinReq.getRoomid()));
                Client client = new Client(moveJoinReq.getIdentity(),ChatServer.thisServer,connectionHandler);
                connectionHandler.setClient(client);
                if(chatroomHandler.isRoomExists(moveJoinReq.getRoomid())){
                    addClientToServer(client);
                    chatroomHandler.addClientToChatRoom(connectionHandler.getClient(),moveJoinReq.getRoomid());
                    RoomChange roomChange = new RoomChange(moveJoinReq.getIdentity(), moveJoinReq.getFormer(), moveJoinReq.getRoomid());
                    ServerChange serverChange = new ServerChange(true,ChatServer.thisServer.getServerId());

                    broadcastToClients(gson.toJson(roomChange),chatroomHandler.getChatroom(moveJoinReq.getRoomid()).getClientList());
                    connectionHandler.send(gson.toJson(serverChange));
                }else{
                    Chatroom newRoom = ChatServer.thisServer.getChatroom();
                    chatroomHandler.addClientToChatRoom(connectionHandler.getClient(),newRoom);
                    RoomChange roomChange = new RoomChange(moveJoinReq.getIdentity(), moveJoinReq.getFormer(), newRoom.getChatroomID());

                    broadcastToClients(gson.toJson(roomChange),newRoom.getClientList());
                }
            }
            case Constant.TYPE_MESSAGE -> {
                MessageReq messageReq = gson.fromJson(message.toJSONString(),MessageReq.class);
                MessageRes messageRes = new MessageRes(connectionHandler.getClient().getClientID(),messageReq.getContent());

                broadcastToClients(gson.toJson(messageRes), (ArrayList<Client>) connectionHandler.getClient().getChatroom().getClientList().stream().filter(client -> {
                    return !client.getClientID().equals(connectionHandler.getClient().getClientID());
                }).collect(Collectors.toList()));
            }
            case Constant.TYPE_DELETEROOM -> {
                DeleteRoomReq deleteRoomReq = gson.fromJson(message.toJSONString(),DeleteRoomReq.class);
                Client client = connectionHandler.getClient();
                DeleteRoomRes deleteRoomRes;
                Chatroom chatroom = chatroomHandler.getChatroom(deleteRoomReq.getRoomid());

                if(client.getChatroom() != null && client.isOwner() && chatroom.getOwner().getClientID().equals(client.getClientID())){
                    chatroom.getClientList().forEach(client1 -> {
                        RoomChange roomChange = new RoomChange(client1.getClientID(),client1.getChatroom().getRoomID(),ChatServer.thisServer.getChatroom().getRoomID());
                        broadcastToClients(gson.toJson(roomChange),chatroom.getClientList());
                    });
                    chatroomHandler.deleteRoom(deleteRoomReq.getRoomid());
                    client.setOwner(false);
                    deleteRoomRes = new DeleteRoomRes(deleteRoomReq.getRoomid(), true);
                }else{
                    deleteRoomRes = new DeleteRoomRes(deleteRoomReq.getRoomid(), false);
                }
                connectionHandler.send(gson.toJson(deleteRoomRes));
            }
            case Constant.TYPE_QUIT -> {
                Client client = connectionHandler.getClient();
                RoomChange roomChange = new RoomChange(client.getClientID(),client.getChatroom().getRoomID(),"");
                //TODO: Do we need to broadcast room change to everyone?
                broadcastToClients(gson.toJson(roomChange),client.getChatroom().getClientList());
//                connectionHandler.send(gson.toJson(roomChange));

                connectionHandler.closeConnection();
                if(client.isOwner()){
                    DeleteRoomReq deleteRoomReq = new DeleteRoomReq(client.getChatroom().getRoomID());
                    manualRequest(deleteRoomReq,connectionHandler);
                }
                clientsOnServer.remove(connectionHandler.getClient().getClientID());
                gossipHandler.updateClients();
            }
        }
    }

    public void manualRequest(Request request,ClientConnectionHandler connectionHandler){
        JSONParser parser = new JSONParser();
        try {
            JSONObject jsonObject = (JSONObject) parser.parse(gson.toJson(request));
            handleMessage(jsonObject,connectionHandler);

        }catch (ParseException e){
            e.printStackTrace();
        }
    }

    public void broadcastToClients(String massage, ArrayList<Client> clients){
        clients.forEach(client -> {
            client.getConnectionHandler().send(massage);
        });
    }

}
