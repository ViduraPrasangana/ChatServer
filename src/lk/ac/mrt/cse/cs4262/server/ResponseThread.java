package lk.ac.mrt.cse.cs4262.server;

import org.json.simple.JSONObject;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ResponseThread implements Runnable {

    private Socket socket;
    private DataOutputStream out;

    public ResponseThread(Socket socket) throws IOException {
        this.socket = socket;
        out = new DataOutputStream(socket.getOutputStream());
    }

    public void run() {
        try{
            String initialMsg = "Hello World";
            SendResponse(socket, initialMsg);
        } catch (IOException e1) {
            e1.printStackTrace();
            System.exit(1);
        }

        while (true) {
            String msg = "My name is Hashan!!! HA HA HA ><";
            try{
                SendResponse(socket,msg);
            } catch (IOException e) {
                System.out.println("Communication Error: " + e.getMessage());
                System.exit(1);
            }
        }
    }

    public void send(JSONObject obj) throws IOException {
        out.write((obj.toJSONString() + "\n").getBytes("UTF-8"));
        out.flush();
    }

    public void SendResponse(Socket socket, String msg) throws IOException {
        JSONObject sendToClient = new JSONObject();
        sendToClient.put("type", "newidentity");
        sendToClient.put("approved", "true");
        send(sendToClient);

    }

//    public void SendResponse(JSONObject obj) throws IOException {
//        send(obj);
//
//    }
}
