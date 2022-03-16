package lk.ac.mrt.cse.cs4262.server.model.response;

import lk.ac.mrt.cse.cs4262.server.Constant;
import lk.ac.mrt.cse.cs4262.server.model.Type;

public class RoomList extends Type {
    private String[] rooms;

    public RoomList(String[] rooms) {
        super(Constant.TYPE_ROOMLIST);
        this.rooms = rooms;
    }
}
