package lk.ac.mrt.cse.cs4262.server.chatroom;

import lk.ac.mrt.cse.cs4262.server.ChatServer;
import lk.ac.mrt.cse.cs4262.server.gossiphandler.GossipHandler;
import lk.ac.mrt.cse.cs4262.server.model.Chatroom;
import lk.ac.mrt.cse.cs4262.server.model.Client;
import lk.ac.mrt.cse.cs4262.server.model.Server;

import java.util.ArrayList;
import java.util.HashMap;

// singleton class to maintain only one ChatroomHandler for the system

public class ChatroomHandler {

    // static self reference to guarantee that chatroomHandler instance is per class
    public static ChatroomHandler chatroomHandler;
    private GossipHandler gossipHandler;


    private final HashMap<String, Chatroom> chatRooms; // <chatroom id>

    //private constructor to ensure that objects cannot be created externally
    private ChatroomHandler(){
        chatRooms = new HashMap<>();
    }


    public static ChatroomHandler getInstance(){
        if (chatroomHandler==null){
            synchronized (ChatroomHandler.class){
                if (chatroomHandler==null){
                    chatroomHandler = new ChatroomHandler();
                }
            }

        }
        return chatroomHandler;
    }

    public synchronized Boolean createRoom(Server server, String chatRoomID, Client client) {

        //check if a room exists in the same name
        //TODO: leader should check
        if (chatRooms.containsKey(chatRoomID)){
            return false;
        }
        Chatroom newRoom = new Chatroom(chatRoomID, server, client); // create new chatroom
        addClientToChatRoom(client,newRoom); // When the client successfully creates a room, it automatically joins the room
        chatRooms.put(chatRoomID,newRoom); // add the new chatroom to the chatroom list
        client.setOwner(true);
        return true;
    }
    public void addClientToChatRoom(Client client,Chatroom chatroom){
        if(client.getChatroom() !=null){
            client.getChatroom().removeClient(client.getClientID());
        }
        client.setChatroom(chatroom);
        chatroom.addClient(client);
    }
    public void addClientToChatRoom(Client client,String chatroomId){
        addClientToChatRoom(client,chatRooms.get(chatroomId));
    }
    public HashMap<String,Chatroom> getChatroomList() {
        return chatRooms;
    }

    public void addChatroom(Chatroom chatroom){
        chatRooms.put(chatroom.getChatroomID(),chatroom);
    }

    public boolean isRoomExists(String roomId){
        return chatRooms.containsKey(roomId);
    }
    public Chatroom getChatroom(String roomId){
        return chatRooms.get(roomId);
    }

    public void deleteRoom(String roomID){
        Chatroom chatroom = getChatroom(roomID);
        chatroom.getClientList().forEach(client -> {
            client.setChatroom(ChatServer.thisServer.getChatroom());
        });
        chatRooms.remove(roomID);
    }

    public void setGossipHandler(GossipHandler gossipHandler) {
        this.gossipHandler = gossipHandler;
    }
}
