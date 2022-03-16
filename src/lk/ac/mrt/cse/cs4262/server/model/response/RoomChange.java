package lk.ac.mrt.cse.cs4262.server.model.response;

import lk.ac.mrt.cse.cs4262.server.Constant;
import lk.ac.mrt.cse.cs4262.server.model.Type;

public class RoomChange extends Type {
    private String identity;
    private String former;
    private String roomid;

    public RoomChange(String identity, String former, String roomid) {
        super(Constant.TYPE_ROOMCHANGE);
        this.identity = identity;
        this.former = former;
        this.roomid = roomid;
    }
}
