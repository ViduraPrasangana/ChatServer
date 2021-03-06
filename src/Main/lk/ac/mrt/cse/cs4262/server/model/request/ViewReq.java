package lk.ac.mrt.cse.cs4262.server.model.request;
import lk.ac.mrt.cse.cs4262.server.Constant;
import lk.ac.mrt.cse.cs4262.server.model.Request;
import lk.ac.mrt.cse.cs4262.server.model.View;

import java.util.ArrayList;

public class ViewReq extends Request {
    private String serverId;
    private ArrayList<String> view;
    ArrayList<String> activeServers;
    public ViewReq(String serverId,  ArrayList<String> view) {
        super(Constant.TYPE_VIEW);
        this.serverId = serverId;
        this.view = view;
    }

    public ArrayList<String> getView() {
        return view;
    }

    public void setView(ArrayList<String> view) {
        this.view = view;
    }

    public ArrayList<String> getActiveServers() {
        return activeServers;
    }

    public void setActiveServers(ArrayList<String> activeServers) {
        this.activeServers = activeServers;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }
}
