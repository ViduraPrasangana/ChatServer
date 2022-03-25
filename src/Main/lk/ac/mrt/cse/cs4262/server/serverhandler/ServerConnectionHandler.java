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
import java.net.SocketException;

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
    public ServerConnectionHandler(Socket socket) throws IOException {
        this.socket = socket;
        gson = new Gson();
        parser = new JSONParser();
        in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
        out = new DataOutputStream(socket.getOutputStream());
        messageHandler = ServerMessageHandler.getInstance();
    }


    @Override
    public void run() {
        boolean wait = true;
        while (wait){
            if(socket.isClosed()){
                interrupt();
                break;
            }
            try {
                String s = in.readLine();
                if(s !=null){
                    JSONObject message = (JSONObject) parser.parse(s);
                    messageHandler.handleMessage(message, this);
                }

            } catch (IOException | ParseException e) {
                wait = false;
                interrupt();
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
}
