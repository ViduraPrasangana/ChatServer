package lk.ac.mrt.cse.cs4262.server;


import lk.ac.mrt.cse.cs4262.server.model.Server;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import java.io.IOException;

public class ChatServer {
    public static String serverId;
    public static Server thisServer;
    public static void main(String[] args) {
        ServerArgs serverArgs = new ServerArgs();
        CmdLineParser parser = new CmdLineParser(serverArgs);

        try{
            parser.parseArgument(args);
            serverId = serverArgs.getServerId();
            Config config = new Config(serverArgs.getServerConf());
            thisServer = config.getServers().get(serverId);

            ServerSocket serverSocket = new ServerSocket(thisServer.getAddress(),thisServer.getCoordinationPort());
            serverSocket.start();
            ClientSocket clientSocket = new ClientSocket(thisServer.getAddress(),thisServer.getClientsPort());
            clientSocket.start();
        }catch (IOException | CmdLineException e){
            e.printStackTrace();
        }
    }

}


