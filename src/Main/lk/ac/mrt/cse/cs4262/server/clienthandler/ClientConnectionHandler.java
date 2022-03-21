package lk.ac.mrt.cse.cs4262.server.clienthandler;

import lk.ac.mrt.cse.cs4262.server.model.Client;
import lk.ac.mrt.cse.cs4262.server.model.request.QuitReq;
import org.apache.log4j.Logger;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;
import java.net.SocketException;

public class ClientConnectionHandler extends Thread {
    private Socket socket;
    private DataOutputStream out;
    private BufferedReader in;
    private JSONParser parser;
    private ClientMessageHandler messageHandler;
    private Client client;
    private final Logger logger = Logger.getLogger(ClientConnectionHandler.class);

    public ClientConnectionHandler(Socket socket) throws IOException {
        this.socket = socket;
        parser = new JSONParser();
        in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
        out = new DataOutputStream(socket.getOutputStream());
        messageHandler = ClientMessageHandler.getInstance();
    }

    @Override
    public void run() {
        while (!socket.isClosed()){
            try {
                JSONObject message = (JSONObject) parser.parse(in.readLine());
                messageHandler.handleMessage(message,this);
            } catch (IOException | ParseException e) {
                e.printStackTrace();
                logger.info(client.getClientID()+": Connection issue. Manual Quit request sent" );
                messageHandler.manualRequest(new QuitReq(),this);
            }
        }
    }


    public void send(String res) {
        if(socket.isClosed() || socket.isOutputShutdown()) return;
        try {
            out.write((res + "\n").getBytes("UTF-8"));
            out.flush();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public void closeConnection(){
        try {
            socket.close();
        }catch (IOException e){
            e.printStackTrace();
        }
    }

    public Client getClient() {
        return client;
    }

    public void setClient(Client client) {
        this.client = client;
    }

}
