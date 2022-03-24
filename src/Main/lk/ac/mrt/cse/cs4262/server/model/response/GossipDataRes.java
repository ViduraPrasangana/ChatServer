package lk.ac.mrt.cse.cs4262.server.model.response;

import lk.ac.mrt.cse.cs4262.server.Constant;
import lk.ac.mrt.cse.cs4262.server.gossiphandler.ServerState;
import lk.ac.mrt.cse.cs4262.server.model.Request;

import java.util.HashMap;

public class GossipDataRes extends Request {
    private HashMap<String, ServerState> serverData;
    public GossipDataRes(HashMap<String, ServerState> serverData) {
        super(Constant.TYPE_GOSSIPINGRES);
        this.serverData = serverData;
    }

    public HashMap<String, ServerState> getServerData() {
        return serverData;
    }

    public void setServerData(HashMap<String, ServerState> serverData) {
        this.serverData = serverData;
    }
}
