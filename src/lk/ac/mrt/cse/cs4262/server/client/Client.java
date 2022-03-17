package lk.ac.mrt.cse.cs4262.server.client;

public class Client {

    private final String clientID;
    private int serverID;

    public Client (String clientID, int serverID){
        this.clientID = clientID;
        this.serverID = serverID;
    }

    public void setServerID(int serverID){
        this.serverID = serverID;
    }

    public String getClientID(){
        return this.clientID;
    }
}
