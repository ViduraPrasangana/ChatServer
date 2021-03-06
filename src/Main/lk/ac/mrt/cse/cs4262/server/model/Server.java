package lk.ac.mrt.cse.cs4262.server.model;

import lk.ac.mrt.cse.cs4262.server.Connectable;
import lk.ac.mrt.cse.cs4262.server.Constant;
import lk.ac.mrt.cse.cs4262.server.chatroom.ChatroomOwnerable;
import lk.ac.mrt.cse.cs4262.server.gossiphandler.GossipHandler;
import lk.ac.mrt.cse.cs4262.server.serverhandler.ServerConnectionHandler;

import java.io.IOException;
import java.net.Socket;

public class Server implements ChatroomOwnerable, Connectable {
    private String serverId;
    private final String ownerId = "";
    private String address;
    private int clientsPort;
    private int coordinationPort;
    private final Chatroom mainhall;
    private Socket socket;
    private ServerConnectionHandler connectionHandler;
    private boolean isAlive = false;
    private boolean me;
    private int heartbeat;

    public Server(String serverId, String address, int clientsPort, int coordinationPort) throws IOException {
        this(serverId,address,clientsPort,coordinationPort,false);
    }
    public Server(String serverId, String address, int clientsPort, int coordinationPort,boolean me) throws IOException {
        this.serverId = serverId;
        this.address = address;
        this.clientsPort = clientsPort;
        this.coordinationPort = coordinationPort;
        this.mainhall = new Chatroom(Constant.MAINHALL_PREFIX+serverId, this, this);
        this.me = me;
    }

    public boolean isMe() {
        return me;
    }

    public void setMe(boolean me) {
        this.me = me;
    }

    public int getHeartbeat() {
        return heartbeat;
    }

    public void setHeartbeat(int heartbeat) {
        this.heartbeat = heartbeat;
    }


    public boolean isAlive() {
        return isAlive;
    }

    public void setAlive(boolean alive) {
        isAlive = alive;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }

    public ServerConnectionHandler getConnectionHandler() {
        return connectionHandler;
    }

    public void setConnectionHandler(ServerConnectionHandler connectionHandler) {
        this.connectionHandler = connectionHandler;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public int getClientsPort() {
        return clientsPort;
    }

    public void setClientsPort(int clientsPort) {
        this.clientsPort = clientsPort;
    }

    public int getCoordinationPort() {
        return coordinationPort;
    }

    public void setCoordinationPort(int coordinationPort) {
        this.coordinationPort = coordinationPort;
    }

    public Chatroom getChatroom() {
        return mainhall;
    }

    @Override
    public String getClientID() {
        return ownerId;
    }

}
