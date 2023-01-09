package edu.yu.cs.com3800.stage3;

import edu.yu.cs.com3800.*;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

/*
Round Robin Task Assignment, Asynchronous Results
The leader/master assigns tasks to the workers on a round-robin basis.
What we mean by round-robin is simple: when a client request comes in,
the master will simply assign the client request to the next worker in the list of workers.
When the master gets to the end of the list of workers,
it goes back to the beginning of the list and proceeds the same exact way.
You should think of the list of workers as a circularly linked list which the master is looping through forever –
the master assigns a request to the current worker and then advances to the next worker in the list.
The master thread MUST NOT synchronously wait (a.k.a. block) for a worker to complete a task;
all work in the cluster is done asynchronously (otherwise, we would gain nothing by having multiple servers.)
The master sends work to a worker via a UDP message and receives results via a UDP message;
there is no synchronous connection or blocking.
Implication of Moving Work Around The Cluster: Request IDs
Each client request has to be assigned a request ID by the master so that as requests / results are passed between master and worker we have a way of knowing what request a given message is relevant to.

 */

public class RoundRobinLeader extends Thread implements LoggingServer {
    private final InetSocketAddress myAddress;
    private final int myPort;
    private final HashMap<Long, InetSocketAddress> peerIDtoAddress;
    private ZooKeeperPeerServer.ServerState state;
    private volatile boolean shutdown;
    private LinkedBlockingQueue<Message> outgoingMessages;
    private LinkedBlockingQueue<Message> incomingMessages;
    private LinkedBlockingQueue<InetSocketAddress> servers;
    private Map<Long,Client> requestToClient;
    private long requestID;
    private long id;
    private Logger logger;
    public RoundRobinLeader(int port,long id, InetSocketAddress address, LinkedBlockingQueue<Message> outgoingMessages, LinkedBlockingQueue<Message> incomingMessages, Map<Long,InetSocketAddress> peerIDtoAddress) throws IOException {
        this.setDaemon(true);
        this.outgoingMessages = outgoingMessages;
        this.incomingMessages = incomingMessages;
        this.myPort = port;
        this.myAddress = address;
        this.id = id;
        this.peerIDtoAddress = new HashMap<>(peerIDtoAddress);
        this.servers = new LinkedBlockingQueue<>();
        this.requestID = 0;
        this.logger = initializeLogging(RoundRobinLeader.class.getCanonicalName() + "-on-port-" + this.myPort);
        this.logger.fine("initiating logger for round robin with server id: " + id);
        this.requestToClient = new HashMap<>();
        for(InetSocketAddress address1 : this.peerIDtoAddress.values()){
            if(!address1.equals(myAddress)){
                this.servers.offer(address1);
            }
        }

    }
    public class Client{
        private int port;
        private String host;
        public Client(int port, String host){
            this.port = port;
            this.host = host;
        }
        public int getPort(){
            return this.port;
        }
        public String getHost(){
            return this.host;
        }
        @Override
        public String toString(){
            return "server at port: " + this.port + " and host: " + this.host;
        }
    }
    @Override
    public void run(){
        while (!this.isInterrupted()) {
            try {
                if(this.logger == null){
                    this.logger = initializeLogging(JavaRunnerFollower.class.getCanonicalName() + "-on-server-with-udpPort-" + this.myPort);
                }
                Message received = this.incomingMessages.poll();
                if (received != null) {
                    if(received.getMessageType() == Message.MessageType.WORK)
                        sendToDoWork(received);
                    else if(received.getMessageType() == Message.MessageType.COMPLETED_WORK){
                        receiveWork(received);
                    }
                }
            }
            catch (IOException e) {
                this.logger.log(Level.WARNING,"failed to send packet", e);
            }
        }
        this.logger.log(Level.SEVERE,"Exiting JavaRunnerFollower.run()");

    }
    //It assigns work it to followers on a round-robin basis
    //when a client request comes in, the master will simply assign the client request to the next worker in the list of workers
    //pop off, send to do work, push back on
    //sends using UDPmessage
    //Each client request has to be assigned a request ID by the master so that as requests /
    // results are passed between master and worker we have a way of knowing what request a given message is relevant to.
    public void sendToDoWork(Message msg){
        InetSocketAddress adr = this.servers.poll();
        //Message(Message.MessageType type, byte[] contents, String senderHost, int senderPort, String receiverHost, int receiverPort, long requestID)
        Message toSend = new Message(Message.MessageType.WORK, msg.getMessageContents(), this.myAddress.getHostString(),this.myPort, adr.getHostString(), adr.getPort(),requestID);
        this.outgoingMessages.offer(toSend);
        this.requestToClient.put(requestID,new Client(msg.getSenderPort(), msg.getSenderHost()));
        requestID++;
        this.servers.offer(adr);
        this.logger.fine("sending work request: " + msg.getRequestID() + " to server: " + adr);
    }
    //gets the results back from the followers, and sends the responses to the one who requested the work, i.e. the client.
    //receives using UDPmessage
    //Each client request has to be assigned a request ID by the master so that as requests /
    // results are passed between master and worker we have a way of knowing what request a given message is relevant to.
    public void receiveWork(Message msg){
        Client client = requestToClient.get(msg.getRequestID());
        Message toSend = new Message(Message.MessageType.COMPLETED_WORK,msg.getMessageContents(),this.myAddress.getHostString(),this.myPort, client.getHost(), client.getPort(),msg.getRequestID());
        requestToClient.remove(msg.getRequestID());
        this.outgoingMessages.offer(toSend);
        this.logger.fine("receiving work request: " + msg.getRequestID() + " from server: " + client);
    }
    public void shutdown() {
        interrupt();
    }
}
