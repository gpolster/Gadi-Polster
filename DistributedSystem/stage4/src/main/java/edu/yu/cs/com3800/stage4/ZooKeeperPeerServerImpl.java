package edu.yu.cs.com3800.stage4;

import edu.yu.cs.com3800.*;
import edu.yu.cs.com3800.stage4.JavaRunnerFollower;
import edu.yu.cs.com3800.stage4.RoundRobinLeader;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

public class ZooKeeperPeerServerImpl extends Thread implements ZooKeeperPeerServer {
    private final InetSocketAddress myAddress;
    private final int udpPort;
    private final int tcpPort;
    private final Long gatewayID;
    private ServerState state;
    private volatile boolean shutdown;
    private LinkedBlockingQueue<Message> outgoingMessages;
    private LinkedBlockingQueue<Message> incomingMessages;
    private Long serverID;
    private long peerEpoch;
    private volatile Vote currentLeader;
    private Map<Long,InetSocketAddress> peerIDtoAddress;
    private ZooKeeperLeaderElection pollster;
    private UDPMessageSender senderWorker;
    private UDPMessageReceiver receiverWorker;
    private int quorumSize;
    private Logger logger;
    private JavaRunnerFollower jrf;
    private boolean notStarted;
    private RoundRobinLeader robinHood;
    private InetSocketAddress leaderAddress;

    //In each peer server, the port passed to the constructor will continue to be used for UDP, and you will calculate the TCP port
    //of any server by adding 2 to the UDP port.
    public ZooKeeperPeerServerImpl(int udpPort, long peerEpoch, Long serverID, Map<Long, InetSocketAddress> peerIDtoAddress, Long gatewayID, int numberOfObservers){
        this.outgoingMessages = new LinkedBlockingQueue<>();
        this.incomingMessages = new LinkedBlockingQueue<>();
        this.udpPort = udpPort;
        this.tcpPort = udpPort+2;
        this.peerEpoch = peerEpoch;
        this.serverID= serverID;
        this.gatewayID = gatewayID;
        this.peerIDtoAddress = new HashMap<>(peerIDtoAddress);
        this.myAddress = new InetSocketAddress(udpPort);
        this.state = ServerState.LOOKING;
        this.peerEpoch = 0;
        this.pollster = new ZooKeeperLeaderElection(this, this.incomingMessages);
        this.quorumSize = (this.peerIDtoAddress.size()-numberOfObservers)/2+1;
        this.notStarted = true;
        try {
            this.logger = initializeLogging("ZooKeeperPeerServerImpl" + "-on-port-" + this.udpPort, true);
            //this.jrf = new JavaRunnerFollower(tcpPort,myAddress,outgoingMessages,incomingMessages);
            //this.robinHood = new RoundRobinLeader(tcpPort,myAddress,peerIDtoAddress,gatewayID);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        //code here...
    }

    @Override
    public void shutdown(){
        this.shutdown = true;
        this.senderWorker.shutdown();
        this.receiverWorker.shutdown();
        if(this.robinHood != null)
            this.robinHood.shutdown();
        else if(this.jrf != null)
            this.jrf.shutdown();
    }
    @Override
    public void run(){
        //System.out.println("running with the T-dawg");
        try{
            logger.fine("creating UDP connection");
            //step 1: create and run thread that sends broadcast messages
            this.senderWorker = new UDPMessageSender(this.outgoingMessages,this.udpPort);
            senderWorker.start();
            //step 2: create and run thread that listens for messages sent to this server
            this.receiverWorker = new UDPMessageReceiver(this.incomingMessages,this.myAddress,this.udpPort,this);
            receiverWorker.start();
        }catch(IOException e){
            logger.severe("UDP exception thrown");
            e.printStackTrace();
            return;
        }
        //step 3: main server loop
        try{
            while (!this.shutdown){
                switch (getPeerState()){
                    case OBSERVER:
                        if(this.currentLeader == null){
                            this.logger.info("OBSERVING");
                            this.currentLeader = pollster.lookForLeader();
                            this.leaderAddress = peerIDtoAddress.get(this.currentLeader.getProposedLeaderID());
                        }
                        break;
//                    When a server enters the LOOKING state, it sends a batch of notification messages,
//                    one to each of the other servers in the ensemble.
//                    The message contains its current vote, which consists of the server’s identifier (sid)
                    //this is done at the very beginning of lookForLeader()
                    case LOOKING:
                        logger.fine("finding leader");
                        //start leader election, set leader to the election winner
                        this.currentLeader = pollster.lookForLeader();
                        logger.fine("found leader");
                        this.peerEpoch++;
                        this.leaderAddress = peerIDtoAddress.get(this.currentLeader.getProposedLeaderID());
                        //updateQueue();
                        break;
                    case FOLLOWING:
                        if(notStarted){
                            this.logger.info("FOLLOWING");
                            if(jrf == null){
                                this.jrf = new JavaRunnerFollower(tcpPort,myAddress,outgoingMessages,incomingMessages);
                            }
                            jrf.start();
                            notStarted = false;
                        }
                        break;
                    case LEADING:
                        // Creates A thread using TCPServer that uses ServerSocket.accept() to accept TCP connections from the GatewayServer, reads a Message from
                        //the connection’s InputStream, and hands that message to the RoundRobinLeader to deal with.

                        if(notStarted){
                            this.logger.info("LEADING");
                            if(robinHood==null){
                                this.robinHood = new RoundRobinLeader(tcpPort,myAddress,peerIDtoAddress,gatewayID);
                            }
                            robinHood.start();
                            notStarted = false;
                        }
                        break;
                }
            }
        }
        catch (Exception e) {
            //code...
            logger.severe("exception thrown while looking for leader");
            e.printStackTrace();
        }
        this.logger.severe("exiting zookeeperpeerserverimpl");
    }

    private void updateQueue() throws InterruptedException {
        if(this.incomingMessages.isEmpty()){
            //System.out.println("empty");
            return;
        } else {
            int size = this.incomingMessages.size();
            for(int i = 0; i < size; i++){
                Message oldMsg = this.incomingMessages.take();
                System.out.println(oldMsg);
                Message msg = new Message(oldMsg.getMessageType(), oldMsg.getMessageContents(), oldMsg.getSenderHost(), oldMsg.getSenderPort(), this.leaderAddress.getHostString(), this.leaderAddress.getPort());
                this.incomingMessages.put(msg);
//                oldMsg = this.outgoingMessages.take();
//                msg = new Message(oldMsg.getMessageType(), oldMsg.getMessageContents(), oldMsg.getSenderHost(), oldMsg.getSenderPort(), this.leaderAddress.getHostString(), this.leaderAddress.getPort());
//                this.outgoingMessages.put(msg);
            }
        }

    }

    @Override
    public void setCurrentLeader(Vote v) throws IOException {
        this.currentLeader = v;
        if(v.equals(new Vote(this.serverID,this.peerEpoch))){
            this.state = ServerState.LEADING;
        } else {
            this.setPeerState(ServerState.FOLLOWING);
        }
    }

    @Override
    public Vote getCurrentLeader() {
        return this.currentLeader;
    }

    //send a single message to a specific server
    @Override
    public void sendMessage(Message.MessageType type, byte[] messageContents, InetSocketAddress target) throws IllegalArgumentException {
        //very much not done, need to coordinate with udp message sender (and reciever?)
        //look deeper into the reciever and sender code when you start back up, then implement the necessary stuff
        //refer to piazza post @56 for slight guidance
        Message msg = new Message(type, messageContents,this.myAddress.getHostString(),this.udpPort,target.getHostString(),target.getPort());
        this.outgoingMessages.offer(msg);
    }

    //sends a message to every server
    @Override
    public void sendBroadcast(Message.MessageType type, byte[] messageContents) {
        for(InetSocketAddress peer : peerIDtoAddress.values())
        {
            Message msg = new Message(type, messageContents,this.myAddress.getHostString(),this.udpPort,peer.getHostString(),peer.getPort());
            this.outgoingMessages.offer(msg);
        }
    }

    @Override
    public ServerState getPeerState() {
        return this.state;
    }

    @Override
    public void setPeerState(ServerState newState) {
        this.state = newState;
    }

    @Override
    public Long getServerId() {
        return this.serverID;
    }

    public Long getGatewayID() {
        return this.gatewayID;
    }

    @Override
    public long getPeerEpoch() {
        return this.peerEpoch;
    }

    @Override
    public InetSocketAddress getAddress() {
        return this.myAddress;
    }

    @Override
    public int getUdpPort() {
        return this.udpPort;
    }

    @Override
    public InetSocketAddress getPeerByID(long peerId) {
        return this.peerIDtoAddress.get(peerId);
    }

    //getQuromSize is how many servers have to agree in order for a leader to be elected, i.e. (cluster size / 2 )+1
    @Override
    public int getQuorumSize() {
        return quorumSize;
    }
}
