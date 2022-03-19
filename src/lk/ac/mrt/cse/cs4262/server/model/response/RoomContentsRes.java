package lk.ac.mrt.cse.cs4262.server.model.response;

import lk.ac.mrt.cse.cs4262.server.Constant;
import lk.ac.mrt.cse.cs4262.server.model.Request;

public class RoomContentsRes extends Request {

    private String roomid;
    private String[] identities;
    private String owner;

    public RoomContentsRes(String roomid, String[] identities, String owner) {
        super(Constant.TYPE_ROOMCONTENTS);
        this.roomid = roomid;
        this.identities = identities;
        this.owner = owner;
    }
}
