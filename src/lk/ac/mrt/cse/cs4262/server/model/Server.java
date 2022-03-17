package lk.ac.mrt.cse.cs4262.server.model;

import lk.ac.mrt.cse.cs4262.server.Constant;
import lk.ac.mrt.cse.cs4262.server.chatroom.Chatroom;

public class Server {
    private String serverId;
    private String address;
    private int clientsPort;
    private int coordinationPort;
    private final Chatroom mainhall;

    public Server(String serverId, String address, int clientsPort, int coordinationPort) {
        this.serverId = serverId;
        this.address = address;
        this.clientsPort = clientsPort;
        this.coordinationPort = coordinationPort;
        this.mainhall = new Chatroom(Constant.MAINHALL_PREFIX+serverId, this.serverId, "");
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

    public Chatroom getMainhall() {
        return mainhall;
    }

    public void broadcastDeletion(String roomID) {
        //TODO: broadcast to all the servers that this room is deleted
    }
}
