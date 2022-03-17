package lk.ac.mrt.cse.cs4262.server.client;

import lk.ac.mrt.cse.cs4262.server.chatroom.Chatroom;
import lk.ac.mrt.cse.cs4262.server.chatroom.ChatroomHandler;
import lk.ac.mrt.cse.cs4262.server.model.Server;

import java.util.ArrayList;

public class Client {

    private final String clientID;
    private Server server;
    private final ChatroomHandler chatroomHandler;
    private boolean isOwner;
    private Chatroom chatroom;
    private ArrayList<String> clientIDList;

    public Client (String clientID, Server server){
        this.clientID = clientID;
        this.server = server;
        this.chatroomHandler = ChatroomHandler.getInstance();
        this.isOwner = false;
        this.chatroom = this.server.getMainhall();

    }

    // --------------------- main requests --------------------------------


    // #list - ask for the list of chat rooms in the system
    public ArrayList<String> getListofChatrooms(){
        return chatroomHandler.getChatroomList();
    }

    // #who - ask for the list of clients in the current chat room
    public ArrayList<String> getListofClients(){
        ArrayList <Client> clientList = chatroom.getClientList();
        clientList.forEach((client -> clientIDList.add(client.getClientID())));
        return clientIDList;

    }

    // #createroom roomid - create a chat room
    // TODO: check sync
    public void createRoom(String roomID){
        // check if the client is already an owner of a chatroom
        if (!isOwner){
            Boolean result = chatroomHandler.createRoom(server.getServerId(),roomID,clientID);
            // When the client successfully creates a room, it automatically joins the room
            if (result){
                this.isOwner = true;
                Chatroom oldroom = this.chatroom;
                //this.joinRoom(roomID);
                this.server.informCreation(this.clientID, roomID,true);
                this.server.informRoomChange(this.clientID, oldroom.getChatroomID(), roomID);
                this.server.broadcastRoomChangeClients(oldroom.getChatroomID(), roomID, this.clientID, oldroom.getClientList() );
            }
            else{
                this.server.informCreation(this.clientID, roomID,false);
            }
        }
        else{
            this.server.informCreation(this.clientID, roomID,false);
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

   //  #deleteroom roomid - delete the room
    public void deleteRoom(String roomID){
        // TODO: validate roomID
        //check if he/she is the owner
        if (this.chatroom.getOwnerID().equals(this.clientID)){
            Chatroom oldroom = this.chatroom;
            ///1.delete room from the chatroom list
            chatroomHandler.deleteRoom(roomID);
            ///2.informs other servers that this room is deleted
            server.broadcastDeletion(roomID);
            ///3.move users to mainhall
            chatroomHandler.changeRoom(oldroom); // here the chatroom is passed bcs the 'if condition' is true
            ///4. success msg to the client
            server.informDeletion(this.clientID, roomID,true);
        }
        else{
            this.server.informDeletion(this.clientID, roomID,false);
        }


    }



    // pass message




    // ----------  supporting fuctions ---------------------------

    public void joinMainhall() {
        this.chatroom = this.server.getMainhall();
    }





    // ----------- setters and getters -------------------------------

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


