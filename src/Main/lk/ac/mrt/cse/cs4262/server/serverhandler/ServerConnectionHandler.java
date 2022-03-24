package lk.ac.mrt.cse.cs4262.server.serverhandler;

import com.google.gson.Gson;
import lk.ac.mrt.cse.cs4262.server.Constant;
import lk.ac.mrt.cse.cs4262.server.FastBullyService;
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
    private ServerMessageHandler messageHandler;
    private FastBullyService fastBullyService;

    public ServerConnectionHandler(Socket socket, FastBullyService fastBullyService) throws IOException {
        this.socket = socket;
        this.fastBullyService = fastBullyService;
        gson = new Gson();
        parser = new JSONParser();
        in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
        out = new DataOutputStream(socket.getOutputStream());
        messageHandler = ServerMessageHandler.getInstance();
    }

    @Override
    public void run() {
        while (true){
            try {
                JSONObject message = (JSONObject) parser.parse(in.readLine());
                messageHandler.handleMessage(message, this);
            } catch (IOException | ParseException e) {
                e.printStackTrace();
            }
        }
    }


    public void send(String res) throws IOException {
        if(socket.isClosed() || socket.isOutputShutdown()) return;
        out.write((res + "\n").getBytes("UTF-8"));
        out.flush();
    }

    public void closeConnection(){
        try {
            socket.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public Socket getSocket() {
        return socket;
    }
}
