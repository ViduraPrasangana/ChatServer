package lk.ac.mrt.cse.cs4262.server.model.response;

import lk.ac.mrt.cse.cs4262.server.Constant;
import lk.ac.mrt.cse.cs4262.server.model.Type;

public class RouteRes extends Type {
    private String roomid;
    private String host;
    private String port;

    public RouteRes(String roomid, String host, String port) {
        super(Constant.TYPE_ROUTE);
        this.roomid = roomid;
        this.host = host;
        this.port = port;
    }
}
