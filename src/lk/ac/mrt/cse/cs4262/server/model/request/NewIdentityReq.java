package lk.ac.mrt.cse.cs4262.server.model.request;

import lk.ac.mrt.cse.cs4262.server.Constant;
import lk.ac.mrt.cse.cs4262.server.model.Request;

public class NewIdentityReq extends Request {
    private String identity;

    public NewIdentityReq(String identity) {
        super(Constant.TYPE_NEWIDENTITY);
        this.identity = identity;
    }

    public String getIdentity() {
        return identity;
    }

    public void setIdentity(String identity) {
        this.identity = identity;
    }
}
