package lk.ac.mrt.cse.cs4262.server.model;

import lk.ac.mrt.cse.cs4262.server.Constant;

import java.util.ArrayList;

public class Server {
    private String serverId;
    private String address;
    private int clientsPort;
    private int coordinationPort;
    private final Chatroom mainhall;

    public Server(String serverId, String address, int clientsPort, int coordinationPort) {
        this.serverId = serverId;
        this.address = address;
        this.clientsPort = clientsPort;
        this.coordinationPort = coordinationPort;
        this.mainhall = new Chatroom(Constant.MAINHALL_PREFIX+serverId, this.serverId, "");
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getClientsPort() {
        return clientsPort;
    }

    public void setClientsPort(int clientsPort) {
        this.clientsPort = clientsPort;
    }

    public int getCoordinationPort() {
        return coordinationPort;
    }

    public void setCoordinationPort(int coordinationPort) {
        this.coordinationPort = coordinationPort;
    }

    public Chatroom getMainhall() {
        return mainhall;
    }

    //// Messaging

    public void broadcastDeletion(String roomID) {
        //TODO: broadcast to all the servers that this room is deleted
        // {"type" : "deleteroom", "serverid" : "s1", "roomid" : "jokes"}
    }


    public void broadcastRoomChangeClients(String oldroomID, String newroomID, String clientID, ArrayList<Client> tolist) {
        //TODO: broadcast the  roomchange
        //{"type" : "roomchange", "identity" : "Maria", "former" : "MainHall-s1", "roomid" :
        //"jokes"}
    }

    public void informDeletion(String ClientID, String roomID, Boolean approved){
        //{"type" : "deleteroom", "roomid" : "jokes", "approved" : "true"}
    }
//    public void informCreation(String ClientID, String roomID, Boolean approved){
//        //{"type" : "createroom", "roomid" : "jokes", "approved" : "true"}
//
//    }
//
//    public void informRoomChange(String clientID, String chatroomID, String roomID) {
//        //{"type" : "roomchange", "identity" : "Maria", "former" : "MainHall-s1", "roomid" :
//        //"jokes"}
//    }
}
