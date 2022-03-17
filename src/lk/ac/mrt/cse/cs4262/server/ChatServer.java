package lk.ac.mrt.cse.cs4262.server;


import lk.ac.mrt.cse.cs4262.server.model.Server;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import java.io.IOException;

public class ChatServer {
    public static void main(String[] args) {
        ServerArgs serverArgs = new ServerArgs();
        CmdLineParser parser = new CmdLineParser(serverArgs);

        try{
            parser.parseArgument(args);
            String serverId = serverArgs.getServerId();
            Config config = new Config(serverArgs.getServerConf());
            Server server = config.getServers().get(serverId);

            ServerSocket serverSocket = new ServerSocket(server.getAddress(),server.getCoordinationPort());
            serverSocket.start();
            ClientSocket clientSocket = new ClientSocket(server.getAddress(),server.getClientsPort());
            clientSocket.start();
        }catch (IOException | CmdLineException e){
            e.printStackTrace();
        }
    }

}


