package edu.yu.cs.com3800.stage5;

import edu.yu.cs.com3800.LoggingServer;
import edu.yu.cs.com3800.Message;
import edu.yu.cs.com3800.ZooKeeperPeerServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.*;
import java.util.concurrent.LinkedBlockingQueue;

//Seems like the simplest way to do gossiping would be for the ZookeeperPeerServerImpl to take charge
public class Gossiper extends Thread implements LoggingServer {
    static final int GOSSIP = 2500;
    static final int FAIL = GOSSIP * 10;
    static final int CLEANUP = FAIL * 2;
    private final long id;
    private LinkedBlockingQueue<Message> outgoingMessages;
    private LinkedBlockingQueue<Message> incomingMessages;
    private Set<Tidbit> failed;
    private Map<Long,Tidbit> myTable;
    private List<Long> serverIds;
    private String host;
    private int udpPort;
    private int heartbeatCounter = 0;
    private ZooKeeperPeerServerImpl myServer;
    private Random random;

    public Gossiper(long id, LinkedBlockingQueue<Message> incomingMessages, LinkedBlockingQueue<Message> outgoingMessages, Set<Long> serverSet, ZooKeeperPeerServer myServer){
        this.id =id;
        this.incomingMessages = incomingMessages;
        this.outgoingMessages = outgoingMessages;
        this.myTable = new HashMap<>();
        this.myServer = (ZooKeeperPeerServerImpl) myServer;
        this.failed =new HashSet<>();
        this.random = new Random();
        this.host = myServer.getAddress().getHostName();
        this.udpPort = myServer.getUdpPort();
        this.serverIds = new ArrayList<>(serverSet);
        for (long server : serverSet){
            this.myTable.put(server,new Tidbit(server));
        }
    }

    /*
    on each iteration of your gossiper's run loop it should:
        1) merge in to its records all new heartbeats/ gossip info that the UDP receiver has
        2) check for failures, using the records it has
        3) clean up old failures that have reached cleanup time
        4) gossip to a random peer
        5) sleep for the heartbeat/gossip interval
     */
    //Every Tgossip seconds, every node increments its own
    //heartbeat counter and gossips its heartbeat data, i.e.
    //pick a random node and send it the data I have about all the nodes
    //When a node’s entry times out, member is marked as failed
    //If on node N1 at time t the heartbeat for node x
    //has not increased for more than Tfail seconds, x is marked failed.
    @Override
    public void run() {
        while (!this.isInterrupted()) {
            if(this.myServer.getCurrentLeader()==null){
                while (this.myServer.getCurrentLeader()==null){
                    Thread.onSpinWait();
                }
                long currentTime = System.currentTimeMillis();
                for(Tidbit t : myTable.values()){
                    t.setTimeStamp(currentTime);
                    myTable.put(t.getId(),t);
                }
            }
            heartbeatCounter++;
            long currentTime = System.currentTimeMillis();
            mergeLocalData(currentTime);
            checkForFailures(currentTime);
            cleanupOldFailures(currentTime);
            gossip();
            try {
                sleep(GOSSIP);
            } catch (InterruptedException e) {
                interrupt();
                break;
//            throw new RuntimeException(e);
            }
        }
//        System.out.println("end of gossiper for id " + id);
    }

    public void pause(int time) {
        try {
            sleep(time);
        } catch (InterruptedException e) {
            interrupt();
//            throw new RuntimeException(e);
        }
    }

    private void gossip() {
        int num = random.nextInt(serverIds.size());
        long id = serverIds.get(num);
        this.myTable.put(this.id,new Tidbit(this.id,heartbeatCounter,System.currentTimeMillis()));
        try {
            ByteArrayOutputStream byteOutputStream = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(byteOutputStream);
            out.writeObject(new HashMap<>(this.myTable));
            byte[] contents = byteOutputStream.toByteArray();
            InetSocketAddress other = myServer.getPeerByID(id);
            Message send = new Message(Message.MessageType.GOSSIP,contents,host,udpPort, other.getHostName(), other.getPort());
            this.outgoingMessages.offer(send);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void cleanupOldFailures(long currentTime) {
        Set<Tidbit> cleanup = new HashSet<>();
        for(Tidbit myBit : this.failed){
            if(currentTime - myBit.getTimeStamp() >= CLEANUP){
                cleanup.add(myBit);
            }

        }
        if(!cleanup.isEmpty()) {
            for (Tidbit bit : cleanup) {
                this.failed.remove(bit);
                myTable.remove(bit.getId());
            }
        }
    }

    private void checkForFailures(long currentTime) {
        for(long server : this.myTable.keySet()){
            Tidbit myBit = myTable.get(server);
            if(currentTime - myBit.getTimeStamp() >= FAIL && !failed.contains(myBit)){
//                System.out.println("Server on port " + udpPort + " whose ID is " + id + " just lost server with ID: " + server);
                myBit.setFailed(true);
                myTable.put(server,myBit);
                serverIds.remove(server);
                failed.add(myBit);
                myServer.reportFailedPeer(server);

            }
        }
    }

    private void mergeLocalData(long currentTime){
        //On receipt, it is merged with local data; lower values
        //replaced with higher values, a-la vector clocks
        Set<Message> oldGossip = new HashSet<>();
        for(Message m : this.incomingMessages){
            if(m.getMessageType() == Message.MessageType.GOSSIP){
                oldGossip.add(m);
                ByteArrayInputStream byteInputStream = new ByteArrayInputStream(m.getMessageContents());
                ObjectInputStream in = null;
                Map<Long,Tidbit> otherMap;
                try {
                    in = new ObjectInputStream(byteInputStream);
                    otherMap = (Map<Long, Tidbit>) in.readObject();
                } catch (IOException | ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
                Set<Long> toRemove = new HashSet<>();
                for(long server : this.myTable.keySet()){
                    Tidbit myBit = myTable.get(server);
                    Tidbit hisBit = otherMap.get(server);
                    if(hisBit==null){
                        toRemove.add(server);
                    }else if(hisBit.getHeartbeat() > myBit.getHeartbeat()){
                        myBit.setHeartbeat(hisBit.getHeartbeat());
                        myBit.setTimeStamp(currentTime);
                        this.myTable.put(server,myBit);
                        if(hisBit.getId()==2L){
//                            System.out.println("my ID is " + this.id + " heartbeat: "+ myBit.getHeartbeat() + " timestamp: " + myBit.getTimeStamp());
                        }
                    }
                }
                for (Long l : toRemove){
                    this.myTable.remove(l);
                }
            }
        }
        this.incomingMessages.removeAll(oldGossip);
    }
    public void shutdown() {
        interrupt();
    }
}
