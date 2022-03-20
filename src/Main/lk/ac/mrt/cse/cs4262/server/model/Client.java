package lk.ac.mrt.cse.cs4262.server.model;

import lk.ac.mrt.cse.cs4262.server.chatroom.ChatroomOwnerable;
import lk.ac.mrt.cse.cs4262.server.clienthandler.ClientConnectionHandler;
import lk.ac.mrt.cse.cs4262.server.chatroom.ChatroomHandler;

import java.util.ArrayList;

public class Client implements ChatroomOwnerable {

    private final String clientID;
    private Server server;
    private final ChatroomHandler chatroomHandler;
    private boolean isOwner;
    private Chatroom chatroom;
    private ArrayList<String> clientIDList;
    private ClientConnectionHandler connectionHandler;

    public Client (String clientID, Server server){
        this.clientID = clientID;
        this.server = server;
        this.chatroomHandler = ChatroomHandler.getInstance();
        this.isOwner = false;
        this.chatroom = this.server.getChatroom();
    }

    public Client(String clientID, Server server,ClientConnectionHandler connectionHandler) {
        this(clientID,server);
        this.connectionHandler = connectionHandler;
    }

    public ClientConnectionHandler getConnectionHandler() {
        return connectionHandler;
    }

    public void setConnectionHandler(ClientConnectionHandler connectionHandler) {
        this.connectionHandler = connectionHandler;
    }

    public void setChatroom(Chatroom chatroom) {
        if(this.chatroom != null){
            this.chatroom.removeClient(this.clientID);
        }
        this.chatroom = chatroom;
    }

    public boolean isOwner() {
        return isOwner;
    }

    public void setOwner(boolean owner) {
        isOwner = owner;
    }

    public void setServer(Server server){
        this.server = server;
    }

    public String getClientID(){
        return this.clientID;
    }

    public Server getServer() {
        return server;
    }

    public Chatroom getChatroom() {
        return chatroom;
    }
}


