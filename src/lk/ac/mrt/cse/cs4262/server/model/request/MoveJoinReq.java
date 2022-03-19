package lk.ac.mrt.cse.cs4262.server.model.request;

import lk.ac.mrt.cse.cs4262.server.Constant;
import lk.ac.mrt.cse.cs4262.server.model.Request;

public class MoveJoinReq extends Request {
    private String former;
    private String roomid;
    private String identity;

    public MoveJoinReq(String roomid) {
        super(Constant.TYPE_MOVEJOIN);
        this.roomid = roomid;
    }

    public String getFormer() {
        return former;
    }

    public void setFormer(String former) {
        this.former = former;
    }

    public String getIdentity() {
        return identity;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }

    public String getRoomid() {
        return roomid;
    }

    public void setRoomid(String roomid) {
        this.roomid = roomid;
    }
}
