package lk.ac.mrt.cse.cs4262.server.model.response;

import lk.ac.mrt.cse.cs4262.server.Constant;
import lk.ac.mrt.cse.cs4262.server.model.Type;

public class ServerChange extends Type {
    private String approved;
    private String serverid;

    public ServerChange(String approved, String serverid) {
        super(Constant.TYPE_SERVERCHANGE);
        this.approved = approved;
        this.serverid = serverid;
    }
}
