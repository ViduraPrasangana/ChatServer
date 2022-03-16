package lk.ac.mrt.cse.cs4262.server.model.response;

import lk.ac.mrt.cse.cs4262.server.Constant;
import lk.ac.mrt.cse.cs4262.server.model.Type;

public class RoomContents extends Type {

    private String roomid;
    private String[] identities;
    private String owner;

    public RoomContents(String roomid, String[] identities, String owner) {
        super(Constant.TYPE_ROOMCONTENTS);
        this.roomid = roomid;
        this.identities = identities;
        this.owner = owner;
    }
}
