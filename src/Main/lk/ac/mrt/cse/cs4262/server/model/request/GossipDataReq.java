package lk.ac.mrt.cse.cs4262.server.model.request;

import lk.ac.mrt.cse.cs4262.server.Constant;
import lk.ac.mrt.cse.cs4262.server.gossiphandler.ServerState;
import lk.ac.mrt.cse.cs4262.server.model.Request;

import java.util.HashMap;

public class GossipDataReq extends Request {
    private HashMap<String, ServerState> serverData;
    public GossipDataReq(HashMap<String, ServerState> serverData) {
        super(Constant.TYPE_GOSSIPINGREQ);
        this.serverData = serverData;
    }

    public HashMap<String, ServerState> getServerData() {
        return serverData;
    }

    public void setServerData(HashMap<String, ServerState> serverData) {
        this.serverData = serverData;
    }
}
