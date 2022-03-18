package lk.ac.mrt.cse.cs4262.server.chatroom;

import lk.ac.mrt.cse.cs4262.server.client.Client;
import lk.ac.mrt.cse.cs4262.server.model.Server;

import java.util.ArrayList;
import java.util.HashMap;

// singleton class to maintain only one ChatroomHandler for the system

public class ChatroomHandler {

    // static self reference to guarantee that chatroomHandler instance is per class
    public static ChatroomHandler chatroomHandler;

    private HashMap<String,Chatroom> chatRooms; // <chatroom id>

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
        Chatroom newRoom = new Chatroom(chatRoomID, server.getServerId(), client.getClientID()); // create new chatroom
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

    public HashMap<String,Chatroom> getChatroomList() {
        return chatRooms;
    }

    public void deleteRoom(String roomID){
        synchronized(chatRooms){
            chatRooms.remove(roomID);
        }
    }


    public void changeRoom(Chatroom chatroom) {
        //get all members of the room
        ArrayList<Client> clients = chatroom.getClientList();
        //for each member: join mainhall
        clients.forEach((client) -> client.joinMainhall());
        //roomchange msg broadcast to all the clients in the old room + clients in the mainhall (= all the clients currently in the mainhall)
        clients.forEach((client) -> client.getServer().broadcastRoomChangeClients(chatroom.getChatroomID(),client.getChatroom().getChatroomID(),client.getClientID(),client.getChatroom().getClientList()));




    }
}
