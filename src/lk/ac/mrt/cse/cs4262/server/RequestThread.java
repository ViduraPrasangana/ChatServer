package lk.ac.mrt.cse.cs4262.server;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.Socket;

public class RequestThread implements Runnable{

    private Socket socket;
    private BufferedReader in;
    private JSONParser parser = new JSONParser();
    private boolean run = true;
    private ResponseThread responseThread;

    public RequestThread(Socket socket, ResponseThread responseThread ){
        this.socket = socket;
        this.responseThread = responseThread;
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

        if (type.equals("newidentity")){
            System.out.println("bla bla bla");
            JSONObject sendToClient1 = new JSONObject();
            sendToClient1.put("type", "newidentity");
            sendToClient1.put("approved", "true");

            responseThread.send(sendToClient1);

            JSONObject sendToClient2 = new JSONObject();
            sendToClient2.put("type", "roomchange");
            sendToClient2.put("identity", "Adel");
            sendToClient2.put("former", "");
            sendToClient2.put("roomid", "MainHall-s1");
            responseThread.send(sendToClient2);

        }
    }
}
