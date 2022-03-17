package lk.ac.mrt.cse.cs4262.server.chatroom;

import java.util.ArrayList;

// singleton class to maintain only one ChatroomHandler for the system

public class ChatroomHandler {

    // static self reference to guarantee that chatroomHandler instance is per class
    public static ChatroomHandler chatroomHandler;

    private final ArrayList<String> chatroomList; // <chatroom id>

    //private constructor to ensure that objects cannot be created externally
    private ChatroomHandler(){
        chatroomList = new ArrayList <>();
    }


    public static ChatroomHandler getInstance(){
        if (chatroomHandler==null){
            synchronized (ChatroomHandler.class){
                if (chatroomHandler==null){
                    chatroomHandler = new ChatroomHandler();
                }
            }

        }
        return chatroomHandler;
    }

    public synchronized Boolean createRoom(int serverID,String chatRoomID, String ownerID) {

        //check if a room exists in the same name
        if (chatroomList.contains(chatRoomID)){
            return false;
        }
        else{
            Chatroom newRoom = new Chatroom(chatRoomID, serverID, ownerID); // create new chatroom
            chatroomList.add(chatRoomID); // add the new chatroom to the chatroom list
            return true;
        }

    }

    public ArrayList<String> getChatroomList() {
        return chatroomList;
    }
}
