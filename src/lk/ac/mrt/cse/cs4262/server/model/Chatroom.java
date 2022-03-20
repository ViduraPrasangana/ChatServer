package lk.ac.mrt.cse.cs4262.server.model;

import lk.ac.mrt.cse.cs4262.server.chatroom.ChatroomOwnerable;

import java.util.ArrayList;
import java.util.HashMap;


public class Chatroom {

    private final String roomID;
    private final Server server;
    private final ChatroomOwnerable owner; // client id

    private final HashMap<String,Client> clientList;
    //private final ArrayList<String> clientList = new ArrayList <>(); // <client id>

    //TODO : check sync keyword
    public Chatroom(String roomID, Server server, ChatroomOwnerable owner) {
        this.roomID = roomID;
        this.server = server;
        this.owner = owner;
        clientList = new HashMap<>();
    }


    public synchronized void addClient(Client client) {
        this.clientList.put(client.getClientID(),client);
    }

    public synchronized void removeClient(String clientID) {
        this.clientList.remove(clientID);
    }

    //getters
    public synchronized String getChatroomID() {
        return roomID;
    }

    public String getRoomID() {
        return roomID;
    }

    public Server getServer() {
        return server;
    }

    public ChatroomOwnerable getOwner() {
        return owner;
    }

    public synchronized ArrayList <Client> getClientList() {
        return new ArrayList<Client>(clientList.values());
    }
}
