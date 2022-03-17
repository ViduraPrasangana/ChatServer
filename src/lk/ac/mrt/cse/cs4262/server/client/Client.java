package lk.ac.mrt.cse.cs4262.server.client;

import lk.ac.mrt.cse.cs4262.server.chatroom.ChatroomHandler;

import java.util.List;

public class Client {

    private final String clientID;
    private int serverID;
    private final ChatroomHandler chatroomHandler;
    private boolean isOwner;
    private String chatroomID;

    public Client (String clientID, int serverID){
        this.clientID = clientID;
        this.serverID = serverID;
        this.chatroomHandler = ChatroomHandler.getInstance();
        this.isOwner = false;
        //this.chatroomID = //mainhall of the server;

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
    // TODO: check sync
    public Boolean createRoom(String roomID){
        // check if the client is already an owner of a chatroom
        if (!isOwner){
            Boolean result = chatroomHandler.createRoom(serverID,roomID,clientID);
            // When the client successfully creates a room, it automatically joins the room
            if (result){
                this.isOwner = true;
               // this.joinRoom(roomID);
                return true;
            }
            else{
                return false;
            }
        }
        else{
            return false;
        }

    }

    // #joinroom roomid - join other rooms
//    public Boolean joinRoom(String roomID){
//        // TODO: validate roomID
//        //check if he/she is not the owner of the current chat room
//        if (){
//            this.chatroomID = roomID;
//            // remove it from the current chatroom's client list
//            //add it to the new chatrrom's client list
//        }
//        else{
//            return false;
//        }
//
//    }

    // #deleteroom roomid - delete the room
//    public Boolean deleteRoom(String roomID){
//        // TODO: validate roomID
//        //check if he/she is the owner
//        if (){
//            //delete room, send messages to the other servers
//            //move users to mainhall, roomchange msg to all the members and all in the mainhalls
//            // success msg to the client
//           chatroomHandler.deleteRoom(roomID);
//        }
//
//
//    }


    // pass message











    //setters and getters
    public void setServerID(int serverID){
        this.serverID = serverID;
    }

    public String getClientID(){
        return this.clientID;
    }
}
