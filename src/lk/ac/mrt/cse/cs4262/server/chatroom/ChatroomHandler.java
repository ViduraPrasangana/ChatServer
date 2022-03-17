package lk.ac.mrt.cse.cs4262.server.chatroom;

import java.util.ArrayList;

public class ChatroomHandler {

    //private final ArrayList<Chatroom> chatroomList = new ArrayList <Chatroom>();
    private final ArrayList<String> chatroomList = new ArrayList <>(); //<chatroom id>

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

    public void joinRoom() {


    }

    public void deleteRoom(String chatroomID) {


    }

    public void quitRoom( ){

    }


}
