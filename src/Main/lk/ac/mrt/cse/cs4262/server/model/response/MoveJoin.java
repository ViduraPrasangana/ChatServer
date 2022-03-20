package lk.ac.mrt.cse.cs4262.server.model.response;

import lk.ac.mrt.cse.cs4262.server.Constant;
import lk.ac.mrt.cse.cs4262.server.model.Request;

public class MoveJoin extends Request {
    private String former;
    private String roomid;
    private String identity;

    public MoveJoin(String former, String roomid, String identity) {
        super(Constant.TYPE_MOVEJOIN);
        this.former = former;
        this.roomid = roomid;
        this.identity = identity;
    }
}
