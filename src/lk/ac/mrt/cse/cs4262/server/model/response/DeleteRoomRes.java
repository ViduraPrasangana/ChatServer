package lk.ac.mrt.cse.cs4262.server.model.response;

import lk.ac.mrt.cse.cs4262.server.Constant;
import lk.ac.mrt.cse.cs4262.server.model.Type;

public class DeleteRoomRes extends Type {
    private String roomid;
    private String approved;

    public DeleteRoomRes(String roomid, boolean approved) {
        super(Constant.TYPE_DELETEROOM);
        this.roomid = roomid;
        this.approved = Boolean.toString(approved);
    }
}
