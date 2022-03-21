package lk.ac.mrt.cse.cs4262.server.model.request;

import lk.ac.mrt.cse.cs4262.server.Constant;
import lk.ac.mrt.cse.cs4262.server.model.Request;

public class QuitReq extends Request {
    public QuitReq() {
        super(Constant.TYPE_QUIT);
    }
}
