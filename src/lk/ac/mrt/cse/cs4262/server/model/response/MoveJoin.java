package lk.ac.mrt.cse.cs4262.server.model.response;

import lk.ac.mrt.cse.cs4262.server.Constant;
import lk.ac.mrt.cse.cs4262.server.model.Type;

public class MoveJoin extends Type {
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
