package lk.ac.mrt.cse.cs4262.server;

import com.google.gson.Gson;
import lk.ac.mrt.cse.cs4262.server.client.Client;
import lk.ac.mrt.cse.cs4262.server.model.request.NewIdentityReq;
import lk.ac.mrt.cse.cs4262.server.model.response.NewIdentityRes;
import lk.ac.mrt.cse.cs4262.server.model.response.RoomChange;
import lk.ac.mrt.cse.cs4262.server.model.response.RoomListRes;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClientMessageHandler {
    private static ClientMessageHandler instance;
    private final Gson gson;

    private ClientMessageHandler(){
        gson = new Gson();
    }

    public static synchronized ClientMessageHandler getInstance(){
        if(instance==null){
            instance = new ClientMessageHandler();
        }
        return instance;
    }

    public void handleMessage(JSONObject message, ClientConnectionHandler connectionHandler) throws IOException {
        String type = (String) message.get("type");
        switch (type){
            case Constant.TYPE_NEWIDENTITY -> {
                NewIdentityReq newIdentityReq = gson.fromJson(message.toJSONString(),NewIdentityReq.class);

                /** Identity Validation **/
                String regex = "[a-zA-Z][a-zA-Z0-9]{2,15}";
                Pattern p = Pattern.compile(regex);
                Matcher m = p.matcher(newIdentityReq.getIdentity());
                NewIdentityRes newIdentityRes;
                if(m.matches()){
                    //TODO: Cross server search for duplicate identity : Gossiping

                    newIdentityRes = new NewIdentityRes("true");
                    connectionHandler.setClient(new Client(newIdentityReq.getIdentity(),ChatServer.thisServer));
                    //TODO: Inform other servers about new identity : Gossiping
                }else{
                    newIdentityRes = new NewIdentityRes("false");
                }
                String response = gson.toJson(newIdentityRes);
                connectionHandler.send(response);

            }
            case Constant.TYPE_LIST -> {
                //TODO cross server search for all chat rooms

                RoomListRes roomListRes = new RoomListRes(new String[]{"MainHall-s1", "MainHall-s2", "jokes"});

                connectionHandler.send(gson.toJson(roomListRes));
            }
            case Constant.TYPE_WHO -> {

            }
        }
    }

}
