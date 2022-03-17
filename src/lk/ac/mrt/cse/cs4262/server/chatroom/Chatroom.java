package lk.ac.mrt.cse.cs4262.server.chatroom;

import lk.ac.mrt.cse.cs4262.server.client.Client;

import java.util.ArrayList;


public class Chatroom {

    private final String roomID;
    private final String serverID;
    private final String ownerID; // client id

    //private final ArrayList <Client> clientList = new ArrayList <Client>();
    private final ArrayList<String> clientList = new ArrayList <>(); // <client id>

    //TODO : check sync keyword
    public Chatroom(String roomID, String serverID, String ownerID) {
        this.roomID = roomID;
        this.serverID = serverID;
        this.ownerID = ownerID;
    }


    public synchronized void addClients(String clientID) {
        this.clientList.add(clientID);
    }

    public synchronized void removeClients(String clientID) {
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


    public synchronized ArrayList <String> getClientList() {
        return clientList;
    }
}
