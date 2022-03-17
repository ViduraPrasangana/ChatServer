package lk.ac.mrt.cse.cs4262.server.chatroom;

import lk.ac.mrt.cse.cs4262.server.client.Client;

import java.util.ArrayList;


public class Chatroom {

    private final String roomID;
    private final int serverID;

    //private final ArrayList <Client> clientList = new ArrayList <Client>();
    private final ArrayList<String> clientList = new ArrayList <String>(); // <client id>

    //TODO : check sync keyword
    public Chatroom(String roomID, int serverID) {
        this.roomID = roomID;
        this.serverID = serverID;
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

    public synchronized int getServerID() {
        return serverID;
    }

    public synchronized ArrayList <String> getClientList() {
        return clientList;
    }
}
