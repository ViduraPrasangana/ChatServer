package lk.ac.mrt.cse.cs4262.server.model.response;

import lk.ac.mrt.cse.cs4262.server.Constant;
import lk.ac.mrt.cse.cs4262.server.model.Request;

public class LeaderAskClientRes extends Request {
    private String clientId;
    private boolean isIn;
    public LeaderAskClientRes(boolean isIn) {
        super(Constant.TYPE_ASKCLIENTRES);
        this.isIn = isIn;
    }

    public String getClientId() {
        return clientId;
    }

    public void setClientId(String clientId) {
        this.clientId = clientId;
    }

    public boolean isIn() {
        return isIn;
    }

    public void setIn(boolean in) {
        isIn = in;
    }
}
