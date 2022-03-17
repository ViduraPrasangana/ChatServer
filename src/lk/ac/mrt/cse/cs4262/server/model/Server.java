package lk.ac.mrt.cse.cs4262.server.model;

public class Server {
    private String serverId;
    private String address;
    private int clientsPort;
    private int coordinationPort;

    public Server(String serverId, String address, int clientsPort, int coordinationPort) {
        this.serverId = serverId;
        this.address = address;
        this.clientsPort = clientsPort;
        this.coordinationPort = coordinationPort;
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
}
