package lk.ac.mrt.cse.cs4262.server.model.response;

import lk.ac.mrt.cse.cs4262.server.Constant;
import lk.ac.mrt.cse.cs4262.server.model.Type;

public class CreateRoomRes extends Type {
    private String roomid;
    private String approved;

    public CreateRoomRes(String roomid, boolean approved) {
        super(Constant.TYPE_CREATEROOM);
        this.roomid = roomid;
        this.approved = Boolean.toString(approved);
    }
}
