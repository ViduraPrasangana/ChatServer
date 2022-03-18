package lk.ac.mrt.cse.cs4262.server.model.response;

import lk.ac.mrt.cse.cs4262.server.Constant;
import lk.ac.mrt.cse.cs4262.server.model.Type;

public class RoomListRes extends Type {
    private String[] rooms;

    public RoomListRes(String[] rooms) {
        super(Constant.TYPE_ROOMLIST);
        this.rooms = rooms;
    }
}
