package lk.ac.mrt.cse.cs4262.server.model.request;

import lk.ac.mrt.cse.cs4262.server.Constant;
import lk.ac.mrt.cse.cs4262.server.model.Type;

public class JoinRoomReq extends Type {
    private String roomid;

    public JoinRoomReq(String roomid) {
        super(Constant.TYPE_JOINROOM);
        this.roomid = roomid;
    }

    public String getRoomid() {
        return roomid;
    }

    public void setRoomid(String roomid) {
        this.roomid = roomid;
    }
}