package edu.yu.cs.com3800;



import edu.yu.cs.com3800.stage5.ZooKeeperPeerServerImpl;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import static edu.yu.cs.com3800.ZooKeeperPeerServer.ServerState.*;
import static edu.yu.cs.com3800.ZooKeeperPeerServer.ServerState.LOOKING;
import static java.lang.Thread.sleep;

public class ZooKeeperLeaderElection {

    private final static int finalizeWait = 200;
    private final static int maxNotificationInterval = 60000;
    private final long gatewayID;
    private LinkedBlockingQueue<Message> incomingMessages;
    private Set<Long> deadServers;
    private final ZooKeeperPeerServer myPeerServer;
    private final long peerID;
    private long peerEpoch;
    private long proposedLeader;
    private long proposedEpoch;
    private ElectionNotification myVote;

    public ZooKeeperLeaderElection(ZooKeeperPeerServer server, LinkedBlockingQueue<Message> incomingMessages) {
        this.incomingMessages = incomingMessages;
        this.myPeerServer = server;
        this.proposedEpoch = server.getPeerEpoch();
        this.peerEpoch = server.getPeerEpoch();
        this.peerID = server.getServerId();
        this.proposedLeader = server.getServerId();
        this.gatewayID = ((ZooKeeperPeerServerImpl)this.myPeerServer).getGatewayID();
        this.deadServers = new HashSet<>();
    }

    public static byte[] buildMsgContent(ElectionNotification notification) {
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
        ByteBuffer msgBytes = ByteBuffer.wrap(received.getMessageContents());
        long leader = msgBytes.getLong();
        char stateChar = msgBytes.getChar();
        long senderID = msgBytes.getLong();
        long peerEpoch = msgBytes.getLong();
        return new ElectionNotification(leader, getServerState(stateChar),senderID,peerEpoch);
    }

    private synchronized Vote getCurrentVote() {
        return notificationToVote(this.myVote);
    }

    public synchronized Vote lookForLeader() {
        double retryInterval = 2.0;
        Map<Long, ElectionNotification> votes = new HashMap<>();
        myVote = new ElectionNotification(this.proposedLeader,this.myPeerServer.getPeerState(),this.myPeerServer.getServerId(),this.myPeerServer.getPeerEpoch());
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
                if (m == null || m.getMessageType() != Message.MessageType.ELECTION) {
                    if(m!= null){
                        if(m.getMessageType() != Message.MessageType.GOSSIP){
                            this.incomingMessages.offer(m);
                        }
                    }
                    //..resend notifications to prompt a reply from others..
                    try {
                        sleep((long) retryInterval * 1000);
                    } catch (InterruptedException e) {
                        System.out.println("runtime exception in look for leader");
                        throw new RuntimeException(e);
                    }
                    sendNotifications();
                    retryInterval = retryInterval * (Math.random() + .5);
                    //.and implement exponential back-off when notifications not received..
                } else {
                    break;
                }

            }
            //if/when we get a message and it's from a valid server and for a valid server..
            ElectionNotification hadoop = getNotificationFromMessage(m);
            if(this.myPeerServer.isPeerDead(hadoop.getSenderID())){
                System.out.println(hadoop.getSenderID() + " should be dead");
                break;
            }
            //switch on the state of the sender:
            switch (hadoop.getState()) {

                case LOOKING: //if the sender is also looking
                    if(this.myPeerServer.isPeerDead(hadoop.getSenderID())){
                        System.out.println(hadoop.getSenderID() + " should be dead");
                        break;
                    }
                    //if the received message has a vote for a leader which supersedes mine, change my vote and tell all my peers what my new vote is.
                    if (supersedesCurrentVote(hadoop.getProposedLeaderID(),hadoop.getPeerEpoch())){
                        changeVote(hadoop);
                        sendNotifications();
                    }
                   // votes.put(this.peerID, this.myVote);
                    votes.put(hadoop.getSenderID(),hadoop);
                    ////if I have enough votes to declare my currently proposed leader as the leader:
                    if (haveEnoughVotes(votes, notificationToVote(this.myVote))){
                        if(!lastCheck(votes)){
                            return acceptElectionWinner(this.myVote);
                        }

                    }
                    break;
                case FOLLOWING: case LEADING:
                    if(this.myPeerServer.isPeerDead(hadoop.getSenderID())){
                        System.out.println(hadoop.getSenderID() + " should be dead");
                        break;
                    }
                    if(hadoop.getPeerEpoch() < this.peerEpoch){
                        break;
                    }
                    votes.put(hadoop.getSenderID(),hadoop);
                    //if the sender is following a leader already or thinks it is the leader
                    if(haveEnoughVotes(votes,hadoop)){
                        if(!lastCheck(votes)) {
                            //if so, accept the election winner.
                            changeVote(hadoop);
                            return acceptElectionWinner(hadoop);
                        }
                    } else {
                        if(hadoop.getPeerEpoch() > this.peerEpoch){
                            if(this.proposedLeader == hadoop.getProposedLeaderID()){
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
    private boolean lastCheck(Map<Long, ElectionNotification> votes){
        try {
//            if(votes.size() < ((ZooKeeperPeerServerImpl)this.myPeerServer).size()){
//                sleep(finalizeWait*10);
//            }
            sleep(finalizeWait*10);
        } catch (InterruptedException e) {
            System.out.println("runtime exception in look for leader");
            throw new RuntimeException(e);
        }
        return isHigherLeader();
    }

    private boolean isHigherLeader() {
        for (Message msg : this.incomingMessages) {
            if (msg.getMessageType() == Message.MessageType.ELECTION){
                ElectionNotification en = getNotificationFromMessage(msg);
                if(this.myVote.getProposedLeaderID() < en.getProposedLeaderID() && this.myVote.getPeerEpoch() <= en.getPeerEpoch()){
                    return true;
                }
            }
        }
        return false;
    }

    private Vote notificationToVote(ElectionNotification hadoop){
        return new Vote(hadoop.getProposedLeaderID(), hadoop.getPeerEpoch());
    }
    public void addDeadServer(long id){
        this.deadServers.add(id);
    }

    private void addVote(ElectionNotification hadoop, Map<Long, ElectionNotification> votes) {
        votes.put(hadoop.getProposedLeaderID(), hadoop);
    }

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
        //should i be changing this to proposed epoch????
        this.myVote = new ElectionNotification(hadoop.getProposedLeaderID(), this.myPeerServer.getPeerState(), this.peerID,this.peerEpoch);
    }

    protected boolean supersedesCurrentVote(long newId, long newEpoch) {
       if (this.proposedLeader==gatewayID){
           return true;
       } else if(newEpoch < this.proposedEpoch){
           return false;
       } else if(this.myPeerServer.isPeerDead(newId)){
           return false;
       }
        return (newEpoch > this.proposedEpoch) || ((newEpoch == this.proposedEpoch) && (newId > this.proposedLeader));
    }

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











