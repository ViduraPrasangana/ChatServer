package lk.ac.mrt.cse.cs4262.server.model.response;

import lk.ac.mrt.cse.cs4262.server.Constant;
import lk.ac.mrt.cse.cs4262.server.model.Type;

public class MessageRes extends Type {
    private String identity;
    private String content;

    public MessageRes(String identity, String content) {
        super(Constant.TYPE_MESSAGE);
        this.identity = identity;
        this.content = content;
    }
}
