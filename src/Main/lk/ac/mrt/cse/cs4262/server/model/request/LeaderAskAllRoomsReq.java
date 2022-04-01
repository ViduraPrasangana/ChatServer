package lk.ac.mrt.cse.cs4262.server.model.request;

import lk.ac.mrt.cse.cs4262.server.Constant;
import lk.ac.mrt.cse.cs4262.server.model.Request;

public class LeaderAskAllRoomsReq extends Request {
    public LeaderAskAllRoomsReq() {
        super(Constant.TYPE_ASKALLROOM);
    }
}
