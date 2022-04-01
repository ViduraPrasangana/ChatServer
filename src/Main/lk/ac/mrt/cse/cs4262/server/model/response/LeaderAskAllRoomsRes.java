package lk.ac.mrt.cse.cs4262.server.model.response;

import lk.ac.mrt.cse.cs4262.server.Constant;
import lk.ac.mrt.cse.cs4262.server.model.Request;

public class LeaderAskAllRoomsRes extends Request {
    private String[] allrooms;
    public LeaderAskAllRoomsRes(String[] allrooms) {
        super(Constant.TYPE_ASKALLROOMRES);
        this.allrooms = allrooms;
    }

    public String[] getAllrooms() {
        return allrooms;
    }

    public void setAllrooms(String[] allrooms) {
        this.allrooms = allrooms;
    }
}
