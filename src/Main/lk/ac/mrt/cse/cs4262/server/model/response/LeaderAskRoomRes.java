package lk.ac.mrt.cse.cs4262.server.model.response;

import lk.ac.mrt.cse.cs4262.server.Constant;
import lk.ac.mrt.cse.cs4262.server.model.Request;

public class LeaderAskRoomRes extends Request {
    private String roomId;
    private boolean isIn;
    public LeaderAskRoomRes(boolean isIn) {
        super(Constant.TYPE_ASKROOMRES);
        this.isIn = isIn;
    }

    public String getRoomId() {
        return roomId;
    }

    public void setRoomId(String roomId) {
        this.roomId = roomId;
    }

    public boolean isIn() {
        return isIn;
    }

    public void setIn(boolean in) {
        isIn = in;
    }
}
