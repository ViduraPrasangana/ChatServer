package lk.ac.mrt.cse.cs4262.server.chatroom;

import lk.ac.mrt.cse.cs4262.server.model.Chatroom;
import lk.ac.mrt.cse.cs4262.server.model.Client;
import lk.ac.mrt.cse.cs4262.server.model.Server;

import java.util.ArrayList;
import java.util.HashMap;

// singleton class to maintain only one ChatroomHandler for the system

public class ChatroomHandler {

    // static self reference to guarantee that chatroomHandler instance is per class
    public static ChatroomHandler chatroomHandler;

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
        //TODO: client management should implemented
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
