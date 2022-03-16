package lk.ac.mrt.cse.cs4262.server;

import com.google.gson.Gson;
import lk.ac.mrt.cse.cs4262.server.model.request.NewIdentityReq;
import lk.ac.mrt.cse.cs4262.server.model.response.NewIdentity;
import lk.ac.mrt.cse.cs4262.server.model.response.RoomChange;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.*;
import java.net.Socket;

public class RequestThread implements Runnable{

    private Socket socket;
    private BufferedReader in;
    private DataOutputStream out;
    private JSONParser parser = new JSONParser();
    private boolean run = true;
    private ResponseThread responseThread;
    private Gson gson;


    public RequestThread(Socket socket, ResponseThread responseThread ) throws IOException {
        this.socket = socket;
        this.responseThread = responseThread;
        gson = new Gson();
        out = new DataOutputStream(socket.getOutputStream());
    }


    public void run() {
        try {
            this.in = new BufferedReader(new InputStreamReader(
                    socket.getInputStream(), "UTF-8"
            ));
            JSONObject message;
            while (run) {
                message = (JSONObject) parser.parse(in.readLine());
                ReadRequest (socket, message);
            }
        } catch (ParseException e) {
            System.out.println("Message Error: " + e.getMessage());
            System.exit(1);
        } catch (IOException e) {
            System.out.println("Communication Error: " + e.getMessage());
            System.exit(1);
        }
    }

    private void ReadRequest(Socket socket, JSONObject message) throws IOException {
        String type = (String) message.get("type");
        System.out.println(type);
        System.out.println(Constant.TYPE_NEWIDENTITY);
        switch (type){
            case Constant.TYPE_NEWIDENTITY -> {
                NewIdentityReq newIdentityReq = gson.fromJson(message.toJSONString(),NewIdentityReq.class);
                System.out.println(newIdentityReq.getIdentity());

                String response = gson.toJson(new NewIdentity("true"));
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
