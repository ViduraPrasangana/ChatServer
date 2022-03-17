package lk.ac.mrt.cse.cs4262.server.client;

import lk.ac.mrt.cse.cs4262.server.chatroom.ChatroomHandler;

import java.util.List;

public class Client {

    private final String clientID;
    private int serverID;
    private final ChatroomHandler chatroomHandler;

    public Client (String clientID, int serverID){
        this.clientID = clientID;
        this.serverID = serverID;
        this.chatroomHandler = ChatroomHandler.getInstance();

    }


    // #list - ask for the list of chat rooms in the system
    public List<String> getListofChatrooms(){
        return chatroomHandler.getChatroomList();
    }

    // #who - ask for the list of clients in the current chat room
//    public List<String> getListofClients(){
//        return
//
//    }

    // #createroom roomid - create a chat room
    public Boolean createRoom(String roomID){
        // check if the client is already an owner of a chatroom
        Boolean result = chatroomHandler.createRoom(serverID,roomID,clientID);
        return result;
    }

    // #joinroom roomid - join other rooms if he/she is not the owner of the current chat room
    public void joinRoom(String roomID){

    }

    // #deleteroom roomid - delete the room

    // #quit - quit messaging

    // pass message











    //setters and getters
    public void setServerID(int serverID){
        this.serverID = serverID;
    }

    public String getClientID(){
        return this.clientID;
    }
}
