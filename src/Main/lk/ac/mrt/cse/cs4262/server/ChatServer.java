package lk.ac.mrt.cse.cs4262.server;


import lk.ac.mrt.cse.cs4262.server.chatroom.ChatroomHandler;
import lk.ac.mrt.cse.cs4262.server.clienthandler.ClientConnectionHandler;
import lk.ac.mrt.cse.cs4262.server.clienthandler.ClientSocket;
import lk.ac.mrt.cse.cs4262.server.gossiphandler.GossipHandler;
import lk.ac.mrt.cse.cs4262.server.model.Server;
import lk.ac.mrt.cse.cs4262.server.serverhandler.ServerSocket;
import org.apache.log4j.Logger;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;

import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;

public class ChatServer {
    public static String serverId;
    public static Server thisServer;
    public static HashMap<String, Server> servers;
    public volatile static boolean electionStatus;

    public static void main(String[] args) {
        ServerArgs serverArgs = new ServerArgs();
        CmdLineParser parser = new CmdLineParser(serverArgs);
        ChatroomHandler chatroomHandler = ChatroomHandler.getInstance();
        try{
            parser.parseArgument(args);
            serverId = serverArgs.getServerId();
            Config config = new Config(serverArgs.getServerConf());
            servers = config.getServers();
            thisServer = config.getServers().get(serverId);
            config.getServers().remove(serverId);
            thisServer.setMe(true);
            chatroomHandler.addChatroom(thisServer.getChatroom());

            FastBullyService fastBullyService = new FastBullyService();
//            fastBullyService.imUp();

            ServerSocket serverSocket = new ServerSocket(thisServer.getAddress(),thisServer.getCoordinationPort(),fastBullyService);
//            serverSocket.start();
            ClientSocket clientSocket = new ClientSocket(thisServer.getAddress(),thisServer.getClientsPort());
            clientSocket.start();


            GossipHandler gossipHandler = new GossipHandler(thisServer,config.getServers());
            gossipHandler.start();
        }catch (IOException | CmdLineException | InterruptedException e){
            e.printStackTrace();
        }
//        }catch (IOException | CmdLineException e){
//            e.printStackTrace();
//        }
    }

}


