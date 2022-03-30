package lk.ac.mrt.cse.cs4262.server.model;

import java.util.Objects;

public class View {
    private String serverId;
    private boolean isLeader;

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public boolean isLeader() {
        return isLeader;
    }

    public void setLeader(boolean leader) {
        isLeader = leader;
    }

    @Override
    public boolean equals(Object obj) {
        View o = (View) obj;
        return o.isLeader()==isLeader() && Objects.equals(getServerId(), o.getServerId());
    }
}
