package lk.ac.mrt.cse.cs4262.server;

import lk.ac.mrt.cse.cs4262.server.client.Client;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class ClientConnectionHandler extends Thread {
    private Socket socket;
    private DataOutputStream out;
    private BufferedReader in;
    private JSONParser parser;
    private ClientMessageHandler messageHandler;
    private Client client;

    public ClientConnectionHandler(Socket socket) throws IOException {
        this.socket = socket;
        parser = new JSONParser();
        in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
        out = new DataOutputStream(socket.getOutputStream());
        messageHandler = ClientMessageHandler.getInstance();
    }

    @Override
    public void run() {
        while (true){
            try {
                JSONObject message = (JSONObject) parser.parse(in.readLine());
                messageHandler.handleMessage(message,this);
            } catch (IOException | ParseException e) {
                e.printStackTrace();
            }
        }
    }


    public void send(String res) throws IOException {
        out.write((res + "\n").getBytes("UTF-8"));
        out.flush();
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }
}
