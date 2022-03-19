package lk.ac.mrt.cse.cs4262.server.model;

import lk.ac.mrt.cse.cs4262.server.model.Client;

import java.util.ArrayList;
import java.util.HashMap;


public class Chatroom {

    private final String roomID;
    private final String serverID;
    private final String ownerID; // client id

    private final HashMap<String,Client> clientList;
    //private final ArrayList<String> clientList = new ArrayList <>(); // <client id>

    //TODO : check sync keyword
    public Chatroom(String roomID, String serverID, String ownerID) {
        this.roomID = roomID;
        this.serverID = serverID;
        this.ownerID = ownerID;
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

    public synchronized String getServerID() {
        return serverID;
    }

    public synchronized String getOwnerID() {
        return ownerID;
    }


    public synchronized ArrayList <Client> getClientList() {
        return new ArrayList<Client>(clientList.values());
    }
}
