package lk.ac.mrt.cse.cs4262.server.gossiphandler;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.management.Notification;
import javax.management.NotificationListener;

public class GossipHandler implements NotificationListener {

    private ArrayList<Member> memberList;

    private ArrayList<Member> deadList;

    private int t_gossip; //in ms

    public int t_cleanup; //in ms

    private Random random;

    private DatagramSocket server;

    private Socket socket;

    private String myAddress;

    private Member me;

    private String myServerId;
    private String myServerAddress;
    private Integer myClientsPort;
    private Integer myCoordinationPort;
    /**
     * Setup the client's lists, gossiping parameters, and parse the startup config file.
     * @throws SocketException
     * @throws InterruptedException
     * @throws UnknownHostException
     */
    public GossipHandler(String ServerId, String ServerAddress, Integer ClientsPort, Integer CoordinationPort) throws SocketException, InterruptedException, UnknownHostException {
        myServerId = ServerId;
        myServerAddress = ServerAddress;
        myClientsPort = ClientsPort;
        myCoordinationPort = CoordinationPort;

        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            public void run() {
                System.out.println("Goodbye my friends...");
            }
        }));

        memberList = new ArrayList<Member>();

        deadList = new ArrayList<Member>();

        t_gossip = 100; // 1 second TODO: make configurable

        t_cleanup = 10000; // 10 seconds TODO: make configurable

        random = new Random();

        int port = 0;

//        String myIpAddress = InetAddress.getLocalHost().getHostAddress();
//        InetAddress myIpAddress = InetAddress.getByName(myServerAddress);
        this.myAddress = myServerId + " " + myServerAddress + " " + myClientsPort + " " + myCoordinationPort;
        System.out.println(this.myAddress);
        ArrayList<String> startupHostsList = parseStartupMembers();

        // loop over the initial hosts, and find ourselves
        for (String host : startupHostsList) {

            Member member = new Member(host, 0, this, t_cleanup);

            if(host.contains(myServerId)) {
                // save our own Member class so we can increment our heartbeat later
                me = member;
                port = myCoordinationPort;
//                this.myAddress = myIpAddress + ":" + port;
                System.out.println("I am " + me);
            }
            memberList.add(member);
        }

        System.out.println("Original Member List");
        System.out.println("---------------------");
        for (Member member : memberList) {
            System.out.println(member);
        }

        if(port != 0) {
            // TODO: starting the server could probably be moved to the constructor
            // of the receiver thread.
            server = new DatagramSocket(port);
        }
        else {
            // This is bad, so no need proceeding on
            System.err.println("Could not find myself in startup list");
            System.exit(-1);
        }
    }

    /**
     * In order to have some membership lists at startup, we read the IP addresses
     * and port at a newline delimited config file.
     * @return List of <IP address:port> Strings
     */
    private ArrayList<String> parseStartupMembers() {
        ArrayList<String> startupHostsList = new ArrayList<String>();
        File startupConfig = new File("src/Main/lk/ac/mrt/cse/cs4262/server/config/server_conf.txt");

        try {
            BufferedReader br = new BufferedReader(new FileReader(startupConfig));
            String line;
            while((line = br.readLine()) != null) {
                startupHostsList.add(line.trim());
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return startupHostsList;
    }

    /**
     * Performs the sending of the membership list, after we have
     * incremented our own heartbeat.
     */
    private void sendMembershipList() {

        this.me.setHeartbeat(me.getHeartbeat() + 1);

        synchronized (this.memberList) {
            try {
                Member member = getRandomMember();

                if(member != null) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    ObjectOutputStream oos = new ObjectOutputStream(baos);
                    oos.writeObject(this.memberList);
                    byte[] buf = baos.toByteArray();

                    String address = member.getAddress();
                    System.out.println("address " + address);
                    String host = address.split(" ")[1];
                    System.out.println("Host " + host);
                    int port = Integer.parseInt(address.split(" ")[3]);
                    System.out.println("Port " + port);

                    InetAddress dest;
                    dest = InetAddress.getByName(host);
//                    String dest = host;
                    System.out.println("Sending to " + dest);
                    System.out.println("---------------------");
                    for (Member m : memberList) {
                        System.out.println(m);
                    }
                    System.out.println("---------------------");

                    //simulate some packet loss ~25%
                    int percentToSend = random.nextInt(100);
                    if(percentToSend > 25) {
                        DatagramSocket socket = new DatagramSocket();
                        DatagramPacket datagramPacket = new DatagramPacket(buf, buf.length, dest, port);
                        socket.send(datagramPacket);
                        socket.close();
                    }
                }

            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }

    /**
     * Find a random peer from the local membership list.
     * Ensure that we do not select ourselves, and keep
     * trying 10 times if we do.  Therefore, in the case
     * where this client is the only member in the list,
     * this method will return null
     * @return Member random member if list is greater than 1, null otherwise
     */
    private Member getRandomMember() {
        Member member = null;

        if(this.memberList.size() > 1) {
            int tries = 10;
            do {
                int randomNeighborIndex = random.nextInt(this.memberList.size());
                member = this.memberList.get(randomNeighborIndex);
                if(--tries <= 0) {
                    member = null;
                    break;
                }
            } while(member.getAddress().equals(this.myAddress));
        }
        else {
            System.out.println("I am alone in this world.");
        }

        return member;
    }

    /**
     * The class handles gossiping the membership list.
     * This information is important to maintaining a common
     * state among all the nodes, and is important for detecting
     * failures.
     */
    private class MembershipGossiper implements Runnable {

        private AtomicBoolean keepRunning;

        public MembershipGossiper() {
            this.keepRunning = new AtomicBoolean(true);
        }

        @Override
        public void run() {
            while(this.keepRunning.get()) {
                try {
                    TimeUnit.MILLISECONDS.sleep(t_gossip);
                    sendMembershipList();
                } catch (InterruptedException e) {
                    // TODO: handle exception
                    // This membership thread was interrupted externally, shutdown
                    e.printStackTrace();
                    keepRunning.set(false);
                }
            }

            this.keepRunning = null;
        }

    }

    /**
     * This class handles the passive cycle, where this client
     * has received an incoming message.  For now, this message
     * is always the membership list, but if you choose to gossip
     * additional information, you will need some logic to determine
     * the incoming message.
     */
    private class AsychronousReceiver implements Runnable {

        private AtomicBoolean keepRunning;

        public AsychronousReceiver() {
            keepRunning = new AtomicBoolean(true);
        }

        @SuppressWarnings("unchecked")
        @Override
        public void run() {
            while(keepRunning.get()) {
                try {
                    //XXX: be mindful of this array size for later
                    byte[] buf = new byte[256];
                    DatagramPacket p = new DatagramPacket(buf, buf.length);
                    server.receive(p);

                    // extract the member arraylist out of the packet
                    // TODO: maybe abstract this out to pass just the bytes needed
                    ByteArrayInputStream bais = new ByteArrayInputStream(p.getData());
                    ObjectInputStream ois = new ObjectInputStream(bais);

                    Object readObject = ois. readObject();
                    if(readObject instanceof ArrayList<?>) {
                        ArrayList<Member> list = (ArrayList<Member>) readObject;

                        System.out.println("Received member list:");
                        for (Member member : list) {
                            System.out.println(member);
                        }
                        // Merge our list with the one we just received
                        mergeLists(list);
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                    keepRunning.set(false);
                } catch (ClassNotFoundException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        }

        /**
         * Merge remote list (received from peer), and our local member list.
         * Simply, we must update the heartbeats that the remote list has with
         * our list.  Also, some additional logic is needed to make sure we have
         * not timed out a member and then immediately received a list with that
         * member.
         * @param remoteList
         */
        private void   mergeLists(ArrayList<Member> remoteList) {

            synchronized (GossipHandler.this.deadList) {

                synchronized (GossipHandler.this.memberList) {

                    for (Member remoteMember : remoteList) {
                        if(GossipHandler.this.memberList.contains(remoteMember)) {
                            Member localMember = GossipHandler.this.memberList.get(GossipHandler.this.memberList.indexOf(remoteMember));

                            if(remoteMember.getHeartbeat() > localMember.getHeartbeat()) {
                                // update local list with latest heartbeat
                                localMember.setHeartbeat(remoteMember.getHeartbeat());
                                // and reset the timeout of that member
                                localMember.resetTimeoutTimer();
                            }
                        }
                        else {
                            // the local list does not contain the remote member

                            // the remote member is either brand new, or a previously declared dead member
                            // if its dead, check the heartbeat because it may have come back from the dead

                            if(GossipHandler.this.deadList.contains(remoteMember)) {
                                Member localDeadMember = GossipHandler.this.deadList.get(GossipHandler.this.deadList.indexOf(remoteMember));
                                if(remoteMember.getHeartbeat() > localDeadMember.getHeartbeat()) {
                                    // it's baa-aack
                                    GossipHandler.this.deadList.remove(localDeadMember);
                                    Member newLocalMember = new Member(remoteMember.getAddress(), remoteMember.getHeartbeat(), GossipHandler.this, t_cleanup);
                                    GossipHandler.this.memberList.add(newLocalMember);
                                    newLocalMember.startTimeoutTimer();
                                } // else ignore
                            }
                            else {
                                // brand spanking new member - welcome
                                Member newLocalMember = new Member(remoteMember.getAddress(), remoteMember.getHeartbeat(), GossipHandler.this, t_cleanup);
                                GossipHandler.this.memberList.add(newLocalMember);
                                newLocalMember.startTimeoutTimer();
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Starts the client.  Specifically, start the various cycles for this protocol.
     * Start the gossip thread and start the receiver thread.
     * @throws InterruptedException
     */
    public void start() throws InterruptedException {

        // Start all timers except for me
        for (Member member : memberList) {
            if(member != me) {
                member.startTimeoutTimer();
            }
        }

        // Start the two worker threads
        ExecutorService executor = Executors.newCachedThreadPool();
        //  The receiver thread is a passive player that handles
        //  merging incoming membership lists from other neighbors.
        executor.execute(new AsychronousReceiver());
        //  The gossiper thread is an active player that
        //  selects a neighbor to share its membership list
        executor.execute(new MembershipGossiper());

        // Potentially, you could kick off more threads here
        //  that could perform additional data synching

        // keep the main thread around
        while(true) {
            TimeUnit.SECONDS.sleep(10);
        }
    }

//    public static void main(String[] args) throws InterruptedException, SocketException, UnknownHostException {
//
//        GossipHandler gossipHandler = new GossipHandler();
//        gossipHandler.start();
//    }

    /**
     * All timers associated with a member will trigger this method when it goes
     * off.  The timer will go off if we have not heard from this member in
     * <code> t_cleanup </code> time.
     */
    @Override
    public void handleNotification(Notification notification, Object handback) {

        Member deadMember = (Member) notification.getUserData();

        System.out.println("Dead member detected: " + deadMember);

        synchronized (this.memberList) {
            this.memberList.remove(deadMember);
        }

        synchronized (this.deadList) {
            this.deadList.add(deadMember);
        }

    }
}