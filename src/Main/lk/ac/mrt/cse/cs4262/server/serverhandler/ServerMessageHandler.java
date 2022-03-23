package lk.ac.mrt.cse.cs4262.server.serverhandler;

import com.google.gson.Gson;
import lk.ac.mrt.cse.cs4262.server.Constant;
import lk.ac.mrt.cse.cs4262.server.clienthandler.ClientConnectionHandler;
import lk.ac.mrt.cse.cs4262.server.model.Client;
import lk.ac.mrt.cse.cs4262.server.model.request.ImUpReq;
import lk.ac.mrt.cse.cs4262.server.model.request.NewIdentityReq;
import lk.ac.mrt.cse.cs4262.server.model.response.NewIdentityRes;
import lk.ac.mrt.cse.cs4262.server.model.response.RoomChange;
import org.json.simple.JSONObject;

import java.util.ArrayList;

public class ServerMessageHandler {
    public static ServerMessageHandler instance;
    private final Gson gson;

    private ServerMessageHandler(){
        gson = new Gson();
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
            case Constant.TYPE_IMUP -> {
                System.out.println("Imupped  "+message);
                ImUpReq imUpReq = gson.fromJson(message.toJSONString(),ImUpReq.class);

            }

        }
    }
}
