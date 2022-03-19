package lk.ac.mrt.cse.cs4262.server.model.response;

import lk.ac.mrt.cse.cs4262.server.Constant;
import lk.ac.mrt.cse.cs4262.server.model.Request;

public class MessageRes extends Request {
    private String identity;
    private String content;

    public MessageRes(String identity, String content) {
        super(Constant.TYPE_MESSAGE);
        this.identity = identity;
        this.content = content;
    }
}
