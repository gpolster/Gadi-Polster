package edu.yu.cs.com3800.stage5;


import edu.yu.cs.com3800.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

public class ZooKeeperPeerServerImpl extends Thread implements ZooKeeperPeerServer {
    private final InetSocketAddress myAddress;
    private final int udpPort;
    private final int tcpPort;
    private final Long gatewayID;
    private ServerState state;
    private Set<InetSocketAddress> cemetary;
    private Set<Long> idCemetary;
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
    private boolean hadFirstLeader;
    private RoundRobinLeader robinHood;
    private InetSocketAddress leaderAddress;
    private Gossiper gossiper;

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
        this.cemetary = new HashSet<>();
        this.idCemetary = new HashSet<>();
        this.peerIDtoAddress = new HashMap<>(peerIDtoAddress);
        this.myAddress = new InetSocketAddress(udpPort);
        this.state = ServerState.LOOKING;
        this.peerEpoch = 0;
        this.hadFirstLeader = false;
//        this.gossiper = new Gossiper(serverID,incomingMessages,outgoingMessages,peerIDtoAddress.keySet(),this);
        this.pollster = new ZooKeeperLeaderElection(this, this.incomingMessages);
        this.quorumSize = (this.peerIDtoAddress.size()-numberOfObservers);
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
//                            System.out.println("Server on port " + udpPort + " whose ID is " + serverID + " has the following ID as its leader: " + currentLeader.getProposedLeaderID() + " and its state is " + this.getPeerState().name());
                            this.leaderAddress = peerIDtoAddress.get(this.currentLeader.getProposedLeaderID());
                        }
                        if(gossiper == null){
                            this.gossiper = new Gossiper(serverID,incomingMessages,outgoingMessages,peerIDtoAddress.keySet(),this);
                            gossiper.start();
                        }

                        break;
//                    When a server enters the LOOKING state, it sends a batch of notification messages,
//                    one to each of the other servers in the ensemble.
//                    The message contains its current vote, which consists of the server’s identifier (sid)
                    //this is done at the very beginning of lookForLeader()
                    case LOOKING:
                        logger.fine("finding leader");
                        //start leader election, set leader to the election winner
                        System.out.println("looking for leader");
                        this.currentLeader = pollster.lookForLeader();
                        logger.fine("found leader");
                        //this.peerEpoch++; //WAS NOT COMMENTED POSSIBLY COULD CHANGE STUFF SO KEEP AN EYE OUT
                        this.leaderAddress = peerIDtoAddress.get(this.currentLeader.getProposedLeaderID());
//                        System.out.println("Server on port " + udpPort + " whose ID is " + serverID + " has the following ID as its leader: " + currentLeader.getProposedLeaderID() + " and its state is " + this.getPeerState().name());
                        this.gossiper = new Gossiper(serverID,incomingMessages,outgoingMessages,peerIDtoAddress.keySet(),this);
                        this.gossiper.start();//need to change for second election
                        //updateQueue();
                        break;
                    case FOLLOWING:
                        if(jrf == null){
                            this.logger.info("FOLLOWING");
                            this.jrf = new JavaRunnerFollower(tcpPort,myAddress,outgoingMessages,incomingMessages, this);
                            jrf.start();
                            notStarted = false;
                            hadFirstLeader = true;

                        }
                        break;
                    case LEADING:
                        if(jrf != null){
                            jrf.shutdown();
                            sleep(2000);
                            jrf = null;
                        }
                        // Creates A thread using TCPServer that uses ServerSocket.accept() to accept TCP connections from the GatewayServer, reads a Message from
                        //the connection’s InputStream, and hands that message to the RoundRobinLeader to deal with.
                        if(robinHood==null){
                            this.logger.info("LEADING");
                            this.robinHood = new RoundRobinLeader(tcpPort,myAddress,peerIDtoAddress,gatewayID,this,hadFirstLeader);
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
    private void gatherNewWork(){

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
        if(v.getProposedLeaderID() == this.serverID){
            setPeerState(ServerState.LEADING);
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
    public synchronized ServerState getPeerState() {
        return this.state;
    }
    public int size(){
        return this.quorumSize;
    }

    @Override
    public synchronized void setPeerState(ServerState newState) {
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
        //(this.peerIDtoAddress.size()-numberOfObservers)/2+1; judah said quarum size minus one
        //but in theory it should really be cluster size
        return (quorumSize+1)/2;
    }
    @Override
    public synchronized void reportFailedPeer(long peerID){
        quorumSize--;
        this.cemetary.add(peerIDtoAddress.get(peerID));
        this.peerIDtoAddress.remove(peerID);
        this.pollster.addDeadServer(peerID);
        this.idCemetary.add(peerID);
//        System.out.println("this peer just died " + peerID);
        if(peerID == currentLeader.getProposedLeaderID()){
            //gossiper.pause(30000);
            //((GatewayPeerServerImpl)this).reportFailedLeader();
            this.peerEpoch++;
            if(!Objects.equals(this.serverID, gatewayID))
                setPeerState(ServerState.LOOKING);
            this.incomingMessages.clear();
            this.pollster = new ZooKeeperLeaderElection(this, this.incomingMessages);
            this.currentLeader = null;
//            gossiper.shutdown();
            //leader is dead method?
        } else if(serverID == currentLeader.getProposedLeaderID()){
            robinHood.reportDeadFollower(peerID);
        }
    }
    public Set<Long> getDeadPeers(){
        return this.idCemetary;
    }
    @Override
    public boolean isPeerDead(long peerID){
        return this.idCemetary.contains(peerID);
    }
    @Override
    public boolean isPeerDead(InetSocketAddress address){
        return this.cemetary.contains(address);
    }



}
