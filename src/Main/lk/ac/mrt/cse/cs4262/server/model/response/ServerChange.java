package lk.ac.mrt.cse.cs4262.server.model.response;

import lk.ac.mrt.cse.cs4262.server.Constant;
import lk.ac.mrt.cse.cs4262.server.model.Request;

public class ServerChange extends Request {
    private String approved;
    private String serverid;

    public ServerChange(boolean approved, String serverid) {
        super(Constant.TYPE_SERVERCHANGE);
        this.approved = Boolean.toString(approved);
        this.serverid = serverid;
    }
}
