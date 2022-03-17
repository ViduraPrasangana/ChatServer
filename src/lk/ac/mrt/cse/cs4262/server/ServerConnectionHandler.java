package lk.ac.mrt.cse.cs4262.server;

import com.google.gson.Gson;
import lk.ac.mrt.cse.cs4262.server.model.request.NewIdentityReq;
import lk.ac.mrt.cse.cs4262.server.model.response.NewIdentityRes;
import lk.ac.mrt.cse.cs4262.server.model.response.RoomChange;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class ServerConnectionHandler extends Thread {
    private Socket socket;
    private Gson gson;
    private DataOutputStream out;
    private BufferedReader in;
    private JSONParser parser;

    public ServerConnectionHandler(Socket socket) throws IOException {
        this.socket = socket;
        gson = new Gson();
        parser = new JSONParser();
        in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
        out = new DataOutputStream(socket.getOutputStream());
    }

    @Override
    public void run() {
        while (true){
            try {
                JSONObject message = (JSONObject) parser.parse(in.readLine());
                ReadRequest (socket, message);
            } catch (IOException | ParseException e) {
                e.printStackTrace();
            }
        }
    }

    private void ReadRequest(Socket socket, JSONObject message) throws IOException {
        String type = (String) message.get("type");
        System.out.println("socket "+socket.toString());
        System.out.println(Constant.TYPE_NEWIDENTITY);
        switch (type){
            case Constant.TYPE_NEWIDENTITY -> {
                NewIdentityReq newIdentityReq = gson.fromJson(message.toJSONString(),NewIdentityReq.class);
                System.out.println(newIdentityReq.getIdentity());

                String response = gson.toJson(new NewIdentityRes("true"));
                String response2 = gson.toJson(new RoomChange(newIdentityReq.getIdentity(), "","MainHall-s1"));
                send(response);
                send(response2);

            }
            case Constant.TYPE_CREATEROOM -> {

            }
        }

    }
    public void send(String res) throws IOException {
        out.write((res + "\n").getBytes("UTF-8"));
        out.flush();
    }
}
