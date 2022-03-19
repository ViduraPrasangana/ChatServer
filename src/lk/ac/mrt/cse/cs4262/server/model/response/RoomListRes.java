package lk.ac.mrt.cse.cs4262.server.model.response;

import lk.ac.mrt.cse.cs4262.server.Constant;
import lk.ac.mrt.cse.cs4262.server.model.Request;

public class RoomListRes extends Request {
    private String[] rooms;

    public RoomListRes(String[] rooms) {
        super(Constant.TYPE_ROOMLIST);
        this.rooms = rooms;
    }
}
