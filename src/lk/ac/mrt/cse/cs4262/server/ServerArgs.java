package lk.ac.mrt.cse.cs4262.server;

import org.kohsuke.args4j.Option;

public class ServerArgs {
    @Option(required = true, name = "-i", usage = "Server Id")
    private String serverId = "s1";

    @Option(required = true, name = "-c", usage = "Server Configuration File")
    private String serverConf = "./config/server_conf.txt";

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public String getServerConf() {
        return serverConf;
    }

    public void setServerConf(String serverConf) {
        this.serverConf = serverConf;
    }
}
