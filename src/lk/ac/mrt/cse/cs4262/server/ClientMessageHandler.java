package lk.ac.mrt.cse.cs4262.server;

import com.google.gson.Gson;
import lk.ac.mrt.cse.cs4262.server.model.request.NewIdentityReq;
import lk.ac.mrt.cse.cs4262.server.model.response.NewIdentity;
import lk.ac.mrt.cse.cs4262.server.model.response.RoomChange;
import org.json.simple.JSONObject;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ClientMessageHandler {
    private static ClientMessageHandler instance;
    private Gson gson;

    private void ClientConnectionHandler(){
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
                String regex = "[a-zA-Z][a-zA-Z0-9-_]{2,15}";
                Pattern p = Pattern.compile(regex);
                Matcher m = p.matcher(newIdentityReq.getIdentity());
                if(m.matches()){

                }

                String response = gson.toJson(new NewIdentity("true"));
                String response2 = gson.toJson(new RoomChange(newIdentityReq.getIdentity(), "","MainHall-s1"));
                connectionHandler.send(response);
                connectionHandler.send(response2);

            }
            case Constant.TYPE_CREATEROOM -> {

            }
        }
    }

}
