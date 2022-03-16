package lk.ac.mrt.cse.cs4262.server.model.response;

import lk.ac.mrt.cse.cs4262.server.Constant;
import lk.ac.mrt.cse.cs4262.server.model.Type;

public class NewIdentity extends Type {
    private String approved;

    public NewIdentity(String approved) {
        super(Constant.TYPE_NEWIDENTITY);
        this.approved = approved;
    }

    public String getApproved() {
        return approved;
    }

    public void setApproved(String approved) {
        this.approved = approved;
    }
}
