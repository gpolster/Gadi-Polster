package edu.yu.cs.com3800.stage4;

import edu.yu.cs.com3800.LoggingServer;
import edu.yu.cs.com3800.Message;
import edu.yu.cs.com3800.ZooKeeperPeerServer;
import edu.yu.cs.com3800.stage4.JavaRunnerFollower;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.Executors.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.Runtime.getRuntime;

public class RoundRobinLeader extends Thread implements LoggingServer {
    /*
        - Any data structures (maps, lists, queues, etc.) that will be accessed by multiple threads in a server
            should be instances of the Concurrent Collections found in java.util.concurrent
        - Any individual values (e.g. a long) that will be accessed by multiple threads should be instances of the thread-safe classes in java.util.concurrent.atomic
     */
    private final InetSocketAddress myAddress;
    private final int tcpPort;
    private final HashMap<Long, InetSocketAddress> peerIDtoAddress;
    private ZooKeeperPeerServer.ServerState state;
    private volatile boolean shutdown;
    private LinkedBlockingQueue<Map.Entry<Message, Socket>> messages;
    private LinkedBlockingQueue<InetSocketAddress> servers;
    private Map<Long,Client> requestToClient;
    private long requestID;
    private TCPServer tcpServer;
    private long gatewayID;
    private Logger logger;

    //In each peer server, the port passed to the constructor will continue to be used for UDP, and you will calculate the TCP port
    //of any server by adding 2 to the UDP port.
    public RoundRobinLeader(int tcpPort,InetSocketAddress address, Map<Long,InetSocketAddress> peerIDtoAddress,long gatewayID) throws IOException {
        this.setDaemon(true);
        this.gatewayID = gatewayID;
        this.messages = new LinkedBlockingQueue<>();
        this.tcpPort = tcpPort;
        this.myAddress = address;
        this.peerIDtoAddress = new HashMap<>(peerIDtoAddress);
        this.servers = new LinkedBlockingQueue<>();
        this.requestID = 0;
        this.tcpServer = new TCPServer(this.tcpPort,this.messages);
        this.logger = initializeLogging("RoundRobinLeader" + "-on-port-" + this.tcpPort,true);
        //this.logger.fine("initiating logger for round robin with server id: " + id);
        this.requestToClient = new HashMap<>();
        for(Long l : this.peerIDtoAddress.keySet()){
            if(!myAddress.equals(this.peerIDtoAddress.get(l))&& !l.equals(gatewayID)) {
                this.servers.offer(this.peerIDtoAddress.get(l));
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
        this.logger.info("round robin started with port " + this.tcpPort);
        this.tcpServer.start();
        ExecutorService threadPool = Executors.newFixedThreadPool(getRuntime().availableProcessors()*2);
        while (!this.isInterrupted()) {
            try {
                if(this.logger == null){
                    this.logger = initializeLogging(JavaRunnerFollower.class.getCanonicalName() + "-on-server-with-udpPort-" + this.tcpPort);
                }
                Map.Entry<Message, Socket> received = this.messages.poll();
                if (received != null) {
                    Message message = received.getKey();
                    InetSocketAddress adr = this.servers.poll();
                    this.logger.info("sending message with request ID: " + requestID + " to port " + adr.getPort()+2);
                    Message newMessage = new Message(message.getMessageType(), message.getMessageContents(), this.myAddress.getHostName(),this.tcpPort,adr.getHostString(), adr.getPort()+2,requestID);
                    requestID++;
                    this.servers.offer(adr);
                    threadPool.execute(new TCPClient(new AbstractMap.SimpleEntry<>(newMessage,received.getValue()), logger));
                }
            }
            catch (IOException e) {
                this.logger.log(Level.WARNING,"failed to send packet", e);
            }
        }
        this.logger.log(Level.SEVERE,"Exiting JavaRunnerFollower.run()");

    }

    public void sendToDoWork(Message msg){
        InetSocketAddress adr = this.servers.poll();
        //Message(Message.MessageType type, byte[] contents, String senderHost, int senderPort, String receiverHost, int receiverPort, long requestID)
        //Message toSend = new Message(Message.MessageType.WORK, msg.getMessageContents(), this.myAddress.getHostString(),this.myPort, adr.getHostString(), adr.getPort(),requestID);
        //this.outgoingMessages.offer(toSend);
        this.requestToClient.put(requestID,new RoundRobinLeader.Client(msg.getSenderPort(), msg.getSenderHost()));
        requestID++;
        this.servers.offer(adr);
        this.logger.fine("sending work request: " + msg.getRequestID() + " to server: " + adr);
    }


    public void receiveWork(Message msg){
        RoundRobinLeader.Client client = requestToClient.get(msg.getRequestID());
        //Message toSend = new Message(Message.MessageType.COMPLETED_WORK,msg.getMessageContents(),this.myAddress.getHostString(),this.myPort, client.getHost(), client.getPort(),msg.getRequestID());
        requestToClient.remove(msg.getRequestID());
        //this.outgoingMessages.offer(toSend);
        this.logger.fine("receiving work request: " + msg.getRequestID() + " from server: " + client);
    }
    //looks like i will need to combine these two methods to adhere to the synchronous TCP usage
    //will create a thread/draw from threadpool for every client connection
    //will communicate synchronously with the worker and then send the workers response back to the gateway
    //I strongly recommend that you use a thread pool to run and manage these threads,
    // with the size of the pool being some very small multiple of the number of cores you have in your machine
    public void shutdown() {
        this.tcpServer.shutdown();
        interrupt();
    }
}
