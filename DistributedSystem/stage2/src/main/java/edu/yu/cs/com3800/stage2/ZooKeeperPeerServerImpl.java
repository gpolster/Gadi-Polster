package edu.yu.cs.com3800.stage2;

import edu.yu.cs.com3800.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

public class ZooKeeperPeerServerImpl extends Thread implements ZooKeeperPeerServer{
    private final InetSocketAddress myAddress;
    private final int myPort;
    private ServerState state;
    private volatile boolean shutdown;
    private LinkedBlockingQueue<Message> outgoingMessages;
    private LinkedBlockingQueue<Message> incomingMessages;
    private Long id;
    private long peerEpoch;
    private volatile Vote currentLeader;
    private Map<Long,InetSocketAddress> peerIDtoAddress;
    private ZooKeeperLeaderElection pollster;
    private UDPMessageSender senderWorker;
    private UDPMessageReceiver receiverWorker;
    private int quorumSize;
    private Logger logger;

    public ZooKeeperPeerServerImpl(int myPort, long peerEpoch, Long id, Map<Long,InetSocketAddress> peerIDtoAddress){
        this.outgoingMessages = new LinkedBlockingQueue<>();
        this.incomingMessages = new LinkedBlockingQueue<>();
        this.myPort = myPort;
        this.peerEpoch = peerEpoch;
        this.id = id;
        this.peerIDtoAddress = new HashMap<>(peerIDtoAddress);
        this.myAddress = new InetSocketAddress(myPort);
        this.state = ServerState.LOOKING;
        this.peerEpoch = 0;
        this.pollster = new ZooKeeperLeaderElection(this, this.incomingMessages);
        this.quorumSize = this.peerIDtoAddress.size()/2+1;
        try {
            this.logger = initializeLogging(ZooKeeperPeerServerImpl.class.getCanonicalName() + "-on-port-" + this.myPort);
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
    }
    @Override
    public void run(){
        //System.out.println("running with the T-dawg");
        try{
            logger.fine("creating UDP connection");
            //step 1: create and run thread that sends broadcast messages
            this.senderWorker = new UDPMessageSender(this.outgoingMessages,this.myPort);
            senderWorker.start();
            //step 2: create and run thread that listens for messages sent to this server
            this.receiverWorker = new UDPMessageReceiver(this.incomingMessages,this.myAddress,this.myPort,this);
            receiverWorker.start();
        }catch(IOException e){
            logger.severe("UDP exception thrown");
            e.printStackTrace();
            return;
        }
        //step 3: main server loop
        try{
            logger.fine("finding leader");
            while (!this.shutdown){
                switch (getPeerState()){
//                    When a server enters the LOOKING state, it sends a batch of notification messages,
//                    one to each of the other servers in the ensemble.
//                    The message contains its current vote, which consists of the server’s identifier (sid)
                    //this is done at the very beginning of lookForLeader()
                    case LOOKING:
                        //start leader election, set leader to the election winner
                        this.currentLeader = pollster.lookForLeader();
                        logger.fine("found leader");
                        this.peerEpoch++;
                        break;
                }
            }
        }
        catch (Exception e) {
            //code...
            logger.severe("exception thrown while looking for leader");
            e.printStackTrace();
        }
    }
    @Override
    public void setCurrentLeader(Vote v) throws IOException {
        this.currentLeader = v;
        if(v.equals(new Vote(this.id,this.peerEpoch))){
            this.state = ServerState.LEADING;
        } else {
            this.state = ServerState.FOLLOWING;
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
        Message msg = new Message(Message.MessageType.WORK, messageContents,this.myAddress.getHostString(),this.myPort,target.getHostString(),target.getPort());
        this.outgoingMessages.offer(msg);
    }

    //sends a message to every server
    @Override
    public void sendBroadcast(Message.MessageType type, byte[] messageContents) {
        for(InetSocketAddress peer : peerIDtoAddress.values())
        {
            Message msg = new Message(type, messageContents,this.myAddress.getHostString(),this.myPort,peer.getHostString(),peer.getPort());
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
        return this.id;
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
        return this.myPort;
    }

    @Override
    public InetSocketAddress getPeerByID(long peerId) {
        return this.myAddress;
    }

    //getQuromSize is how many servers have to agree in order for a leader to be elected, i.e. (cluster size / 2 )+1
    @Override
    public int getQuorumSize() {
        return quorumSize;
    }
}
