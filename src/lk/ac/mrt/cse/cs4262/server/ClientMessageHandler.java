package lk.ac.mrt.cse.cs4262.server;

import com.google.gson.Gson;
import lk.ac.mrt.cse.cs4262.server.chatroom.Chatroom;
import lk.ac.mrt.cse.cs4262.server.chatroom.ChatroomHandler;
import lk.ac.mrt.cse.cs4262.server.client.Client;
import lk.ac.mrt.cse.cs4262.server.model.Server;
import lk.ac.mrt.cse.cs4262.server.model.request.CreateRoomReq;
import lk.ac.mrt.cse.cs4262.server.model.request.JoinRoomReq;
import lk.ac.mrt.cse.cs4262.server.model.request.NewIdentityReq;
import lk.ac.mrt.cse.cs4262.server.model.response.*;
import org.json.simple.JSONObject;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClientMessageHandler {
    private static ClientMessageHandler instance;
    private ChatroomHandler chatroomHandler;
    private ServerMessageHandler serverMessageHandler;
    private final Gson gson;

    private ClientMessageHandler(){
        gson = new Gson();
        chatroomHandler = ChatroomHandler.getInstance();
        serverMessageHandler = ServerMessageHandler.getInstance();
    }

    public static synchronized ClientMessageHandler getInstance(){
        if(instance==null){
            instance = new ClientMessageHandler();
        }
        return instance;
    }

    public void handleMessage(JSONObject message, ClientConnectionHandler connectionHandler) {
        String type = (String) message.get("type");
        switch (type){
            case Constant.TYPE_NEWIDENTITY -> {
                NewIdentityReq newIdentityReq = gson.fromJson(message.toJSONString(),NewIdentityReq.class);

                /** Identity Validation **/
                String regex = "[a-zA-Z][a-zA-Z0-9]{2,15}";
                Pattern p = Pattern.compile(regex);
                Matcher m = p.matcher(newIdentityReq.getIdentity());
                NewIdentityRes newIdentityRes;
                if(m.matches()){
                    //TODO: Cross server search for duplicate identity : Gossiping

                    newIdentityRes = new NewIdentityRes("true");
                    connectionHandler.setClient(new Client(newIdentityReq.getIdentity(),ChatServer.thisServer,connectionHandler));
                    //TODO: Inform other servers about new identity : Gossiping
                }else{
                    newIdentityRes = new NewIdentityRes("false");
                }
                String response = gson.toJson(newIdentityRes);
                connectionHandler.send(response);

            }
            case Constant.TYPE_LIST -> {
                //TODO cross server search for all chat rooms

                RoomListRes roomListRes = new RoomListRes(new String[]{"MainHall-s1", "MainHall-s2", "jokes"});

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

                /** RoomId Validation **/
                String regex = "[a-zA-Z][a-zA-Z0-9]{2,15}";
                Pattern p = Pattern.compile(regex);
                Matcher m = p.matcher(createRoomReq.getRoomid());

                CreateRoomRes createRoomRes = new CreateRoomRes(createRoomReq.getRoomid(),false);;
                RoomChange roomChange;
                if (m.matches() && !client.isOwner()){
                    Chatroom oldRoom = client.getChatroom();
                    boolean result = chatroomHandler.createRoom(ChatServer.thisServer,createRoomReq.getRoomid(),client);
                    if (result){
                        createRoomRes = new CreateRoomRes(createRoomReq.getRoomid(),true);
                        roomChange = new RoomChange(client.getClientID(),oldRoom.getChatroomID(), createRoomReq.getRoomid());
                        connectionHandler.send(gson.toJson(createRoomRes));
                        connectionHandler.send(gson.toJson(roomChange));

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
                //TODO: verify from leader chatroom exists and chatroom id
                Server serverOfRoom = null;
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
                }

            }
            case Constant.TYPE_MOVEJOIN -> {

            }
        }
    }

    public void broadcastToClients(String massage, ArrayList<Client> clients){
        clients.forEach(client -> {
            client.getConnectionHandler().send(massage);
        });
    }

}
