package lk.ac.mrt.cse.cs4262.server;

import lk.ac.mrt.cse.cs4262.server.chatroom.ChatroomHandler;
import lk.ac.mrt.cse.cs4262.server.model.Server;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner;

public class Config {
    private HashMap<String,Server> servers;

    public Config(String url) {
        servers = new HashMap<>();

        try {
            File config = new File(url);
            Scanner configScanner = new Scanner(config);
            while (configScanner.hasNextLine()) {
                String data = configScanner.nextLine();
                String[] row = data.split("\\s+");
                Server server = new Server(row[0],row[1],Integer.parseInt(row[2]),Integer.parseInt(row[3]));
                servers.put(row[0],server);
            }
            configScanner.close();
        } catch (FileNotFoundException e) {
            System.out.println("Configuration file not found");
            e.printStackTrace();
        }
    }

    public HashMap<String, Server> getServers() {
        return servers;
    }
}
