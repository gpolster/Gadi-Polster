package edu.yu.cs.com3800;

import edu.yu.cs.com3800.stage4.ZooKeeperPeerServerImpl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static edu.yu.cs.com3800.ZooKeeperPeerServer.ServerState.*;
import static edu.yu.cs.com3800.ZooKeeperPeerServer.ServerState.LOOKING;
import static java.lang.Thread.sleep;

public class ZooKeeperLeaderElection {
    /**
     * time to wait once we believe we've reached the end of leader election.
     */
    private final static int finalizeWait = 200;

    /**
     * Upper bound on the amount of time between two consecutive notification checks.
     * This impacts the amount of time to get the system up again after long partitions. Currently 60 seconds.
     */
    private final static int maxNotificationInterval = 60000;
    private final long gatewayID;
    private LinkedBlockingQueue<Message> incomingMessages;
    private final ZooKeeperPeerServer myPeerServer;
    private final long peerID;
    private long peerEpoch;
    private long proposedLeader;
    private long proposedEpoch;
    private ElectionNotification myVote;
//    private int electionMessages = 0;
//    private int workMessages = 0;
//    private int completedWorkMessages = 0;
//    private int gossipMessages = 0 ;
//    private int newLeaderMessages = 0;



    public ZooKeeperLeaderElection(ZooKeeperPeerServer server, LinkedBlockingQueue<Message> incomingMessages) {
        this.incomingMessages = incomingMessages;
        this.myPeerServer = server;
        this.proposedEpoch = server.getPeerEpoch();
        this.peerEpoch = server.getPeerEpoch();
        this.peerID = server.getServerId();
        this.proposedLeader = server.getServerId();
        this.gatewayID = ((ZooKeeperPeerServerImpl)this.myPeerServer).getGatewayID();
    }

    //supposed to convert a notification to a message
    //Messages only taken a certain parameter for message contents, which is a byte array,
    // so you have to figure out how to convert whatever you’re passing into the message into a byte array.
    //Message.getNetworkPayload? or maybe something very similar
    public static byte[] buildMsgContent(ElectionNotification notification) {
        //current idea is
        //(long proposedLeaderID, ZooKeeperPeerServer.ServerState state, long senderID, long peerEpoch)
        //1 long (propesedLeaderID) = 8 bytes
        //1 char (msg type) = 2 bytes
        //1 long (senderID) = 8 bytes
        //1 long (peerEpoch) = 8 bytes
        ByteBuffer buffer = ByteBuffer.allocate(26);
        buffer.clear();
        buffer.putLong(notification.getProposedLeaderID());
        buffer.putChar(notification.getState().getChar());
        buffer.putLong(notification.getSenderID());
        buffer.putLong(notification.getPeerEpoch());
        buffer.flip();
        return buffer.array();
    }

    public static ElectionNotification getNotificationFromMessage(Message received) {
        //(long proposedLeaderID, ZooKeeperPeerServer.ServerState state, long senderID, long peerEpoch)
        ByteBuffer msgBytes = ByteBuffer.wrap(received.getMessageContents());
        long leader = msgBytes.getLong();
        char stateChar = msgBytes.getChar();
        long senderID = msgBytes.getLong();
        long peerEpoch = msgBytes.getLong();
        ElectionNotification hadoop = new ElectionNotification(leader, getServerState(stateChar),senderID,peerEpoch);
        return hadoop;
    }

    private synchronized Vote getCurrentVote() {
        //not yet initialized, presumably will come up in lookForLeader
        return notificationToVote(this.myVote);
    }

    public synchronized Vote lookForLeader() {
        double retryInterval = 2.0;
        Map<Long, ElectionNotification> votes = new HashMap<>();
        myVote = new ElectionNotification(this.proposedLeader,this.myPeerServer.getPeerState(),this.myPeerServer.getServerId(),this.myPeerServer.getPeerEpoch());
        //send initial notifications to other peers to get things started
        sendNotifications();
        //Loop, exchanging notifications with other servers until we find a leader
        while (this.myPeerServer.getPeerState() == LOOKING||this.myPeerServer.getPeerState() == OBSERVER) {
            //Remove next notification from queue, timing out after 2 times the termination time
            Message m;
            while(true) {
                try {
                    m = this.incomingMessages.poll(200, TimeUnit.MILLISECONDS);
                } catch (InterruptedException e) {
                    System.out.println("runtime exception in look for leader");
                    throw new RuntimeException(e);
                }
                //if no notifications received..
                if (m == null) {
                    //..resend notifications to prompt a reply from others..
                    try {
                        sleep((long) retryInterval * 1000);
                    } catch (InterruptedException e) {
                        System.out.println("runtime exception in look for leader");
                        throw new RuntimeException(e);
                    }
                    sendNotifications();
                    retryInterval = retryInterval * (Math.random() + .5);
                    continue;
                    //.and implement exponential back-off when notifications not received..
                } else {
                    break;
                }

            }
            //if/when we get a message and it's from a valid server and for a valid server..
            ElectionNotification hadoop = getNotificationFromMessage(m);
            //switch on the state of the sender:
            switch (hadoop.getState()) {
                case LOOKING: //if the sender is also looking
                    //System.out.println("looking");
                    //System.out.println(getCurrentVote());
                    //if the received message has a vote for a leader which supersedes mine, change my vote and tell all my peers what my new vote is.
                    if (supersedesCurrentVote(hadoop.getProposedLeaderID(),hadoop.getPeerEpoch())){
                        changeVote(hadoop);
                        sendNotifications();
                    }
                    //keep track of the votes I received and who I received them from.
                    //maybe add my vote too???

                   // votes.put(this.peerID, this.myVote);
                    votes.put(hadoop.getSenderID(),hadoop);


                    ////if I have enough votes to declare my currently proposed leader as the leader:
                    if (haveEnoughVotes(votes, notificationToVote(this.myVote))){
                        //first check if there are any new votes for a higher ranked possible leader before I declare a leader. If so, continue in my election loop
                        //loop through the queue
                        if(!isHigherLeader()){
                            return acceptElectionWinner(this.myVote);
                        }

                    }
                    break;
                case FOLLOWING: case LEADING:
                    votes.put(hadoop.getSenderID(),hadoop);
                    //if the sender is following a leader already or thinks it is the leader
                    //IF: see if the sender's vote allows me to reach a conclusion based on the election epoch that I'm in,
                    // i.e. it gives the majority to the vote of the FOLLOWING or LEADING peer whose vote I just received.
                    if(haveEnoughVotes(votes,hadoop)){
                        //if so, accept the election winner.
                        changeVote(hadoop);
                        return acceptElectionWinner(hadoop);
                        //As, once someone declares a winner, we are done. We are not worried about / accounting for misbehaving peers.
                    } else {
                        //ELSE: if n is from a LATER election epoch
                        if(hadoop.getPeerEpoch() > this.peerEpoch){
                            //IF a quorum from that epoch are voting for the same peer as the vote of the FOLLOWING or LEADING peer whose vote I just received.
                            if(this.proposedLeader == hadoop.getProposedLeaderID()){
                                //THEN accept their leader, and update my epoch to be their epoch
                                this.peerEpoch = hadoop.getPeerEpoch();
                            }
                        }
                    }
                    break;
                case OBSERVER:
                    break;

            }
        }
        return this.myVote;
    }
    private boolean lastCheck(){
        try {
            sleep(finalizeWait*10);
        } catch (InterruptedException e) {
            System.out.println("runtime exception in look for leader");
            throw new RuntimeException(e);
        }
        return isHigherLeader();
    }
    //first check if there are any new votes for a higher ranked possible leader before I declare a leader. If so, continue in my election loop
    //waiting for clarification on piazza
    private boolean isHigherLeader() {
        for (Message msg : this.incomingMessages) {
            if (msg.getMessageType() == Message.MessageType.ELECTION){
                ElectionNotification en = getNotificationFromMessage(msg);
                if(this.myVote.getProposedLeaderID() < en.getProposedLeaderID()){
                    return true;
                }
            }
        }
        return false;
    }

    private Vote notificationToVote(ElectionNotification hadoop){
        return new Vote(hadoop.getProposedLeaderID(), hadoop.getPeerEpoch());
    }

    private void addVote(ElectionNotification hadoop, Map<Long, ElectionNotification> votes) {
        //not using right now, but may change that in future stages/ if i clean up code
        votes.put(hadoop.getProposedLeaderID(), hadoop);
    }

    //created myself, unsure if it should return something or what it should do ;)
    //send initial notifications to other peers to get things started
    private void sendNotifications() {
        this.myPeerServer.sendBroadcast(Message.MessageType.ELECTION, buildMsgContent(myVote));
    }

    private Vote acceptElectionWinner(ElectionNotification n) {
        //set my state to either LEADING or FOLLOWING
        //clear out the incoming queue before returning
        Vote v;
        try {
            v = new Vote(n.getProposedLeaderID(),n.getPeerEpoch());
            this.myPeerServer.setCurrentLeader(v);
        } catch (IOException e) {
            System.out.println("IO exception in look for leader");
            throw new RuntimeException(e);
        }
        //this.incomingMessages.clear();
        //this.peerEpoch++;
        return v;
    }
    private void changeVote(ElectionNotification hadoop){
        proposedLeader = hadoop.getProposedLeaderID();
        this.proposedEpoch = hadoop.getPeerEpoch();
        this.myVote = new ElectionNotification(hadoop.getProposedLeaderID(), this.myPeerServer.getPeerState(), this.peerID,this.peerEpoch);
    }

    /*
     * We return true if one of the following three cases hold:
     * 1- New epoch is higher
     * 2- New epoch is the same as current epoch, but server id is higher.
     */
    protected boolean supersedesCurrentVote(long newId, long newEpoch) {
       if (this.proposedLeader==gatewayID){
           return true;
        }
        return (newEpoch > this.proposedEpoch) || ((newEpoch == this.proposedEpoch) && (newId > this.proposedLeader));
    }

    /**
     * Termination predicate. Given a set of votes, determines if have sufficient support for the proposal to declare the end of the election round.
     * Who voted for who isn't relevant, we only care that each server has one current vote
     */
    protected boolean haveEnoughVotes(Map<Long, ElectionNotification> votes, Vote proposal) {
        //is the number of votes for the proposal > the size of my peer server’s quorum?
        int counter = 0;
        Set<Long> voters = new HashSet<>();
        for (Long id : votes.keySet()){
            if(voters.add(id)){
                //if(votes.get(id).getProposedLeaderID() == proposal.getProposedLeaderID()){
                if(votes.get(id).getProposedLeaderID() == proposal.getProposedLeaderID() && votes.get(id).getPeerEpoch() == proposal.getPeerEpoch()){
                    counter++;
                }
            }
        }
        if(counter >= this.myPeerServer.getQuorumSize()){
            return true;
        } else {
            return false;
        }
    }
}











