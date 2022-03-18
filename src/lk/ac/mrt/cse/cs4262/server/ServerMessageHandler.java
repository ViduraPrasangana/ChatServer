package lk.ac.mrt.cse.cs4262.server;

import lk.ac.mrt.cse.cs4262.server.client.Client;

import java.util.ArrayList;

public class ServerMessageHandler {
    public static ServerMessageHandler instance;

    private ServerMessageHandler(){

    }

    public static synchronized ServerMessageHandler getInstance(){
        if(instance == null){
            instance = new ServerMessageHandler();
        }
        return instance;
    }

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
    public void informCreation(String ClientID, String roomID, Boolean approved){
        //{"type" : "createroom", "roomid" : "jokes", "approved" : "true"}

    }

    public void informRoomChange(String clientID, String chatroomID, String roomID) {
        //{"type" : "roomchange", "identity" : "Maria", "former" : "MainHall-s1", "roomid" :
        //"jokes"}
    }
}
