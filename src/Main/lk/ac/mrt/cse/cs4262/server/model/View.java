package lk.ac.mrt.cse.cs4262.server.model;

import java.util.Objects;

public class View {
    private String serverId;

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    @Override
    public boolean equals(Object obj) {
        View o = (View) obj;
        return  Objects.equals(getServerId(), o.getServerId());
    }
}
