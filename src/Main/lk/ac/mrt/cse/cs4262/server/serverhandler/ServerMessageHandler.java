package lk.ac.mrt.cse.cs4262.server.serverhandler;

import lk.ac.mrt.cse.cs4262.server.Constant;
import lk.ac.mrt.cse.cs4262.server.clienthandler.ClientConnectionHandler;
import lk.ac.mrt.cse.cs4262.server.model.Client;
import lk.ac.mrt.cse.cs4262.server.model.request.NewIdentityReq;
import lk.ac.mrt.cse.cs4262.server.model.response.NewIdentityRes;
import lk.ac.mrt.cse.cs4262.server.model.response.RoomChange;
import org.json.simple.JSONObject;

import java.util.ArrayList;

public class ServerMessageHandler {
    public static ServerMessageHandler instance;

    private ServerMessageHandler(){

    }

    public static synchronized ServerMessageHandler getInstance(){
        if(instance == null){
            instance = new ServerMessageHandler();
        }
        return instance;
    }

    public void handleMessage(JSONObject message, ServerConnectionHandler connectionHandler) {
        String type = (String) message.get("type");

        switch (type){
            case Constant.TYPE_NEWIDENTITY -> {

            }
            case Constant.TYPE_CREATEROOM -> {

            }
        }
    }
}
