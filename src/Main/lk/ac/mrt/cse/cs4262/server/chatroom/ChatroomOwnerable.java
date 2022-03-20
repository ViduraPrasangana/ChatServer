package lk.ac.mrt.cse.cs4262.server.chatroom;

import lk.ac.mrt.cse.cs4262.server.model.Chatroom;

public interface ChatroomOwnerable {
    final String clientID = null;

    public String getClientID();
    public Chatroom getChatroom();
}
