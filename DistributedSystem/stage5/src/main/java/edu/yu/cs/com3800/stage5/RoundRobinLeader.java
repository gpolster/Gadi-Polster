package edu.yu.cs.com3800.stage5;

import edu.yu.cs.com3800.LoggingServer;
import edu.yu.cs.com3800.Message;
import edu.yu.cs.com3800.ZooKeeperPeerServer;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.*;
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
    private final long myID;
    private final int tcpPort;
    private final HashMap<Long, InetSocketAddress> peerIDtoAddress;
    private ZooKeeperPeerServer.ServerState state;
    private volatile boolean shutdown;
    private LinkedBlockingQueue<Map.Entry<Message, Socket>> messages;
    private LinkedBlockingQueue<Long> servers;
    private ConcurrentMap<Long,Map.Entry<Message, Socket>> requestToMessage;
    private ConcurrentMap<Long, Set<Long>> serverToRequests;
    private TCPServer tcpServer;
    private long gatewayID;
    private Logger logger;
    private volatile boolean newDeath;
    private ZooKeeperPeerServer myServer;
    private boolean sentNewLeaderRequests;
    private boolean newLeaderGettingWork;

    //In each peer server, the port passed to the constructor will continue to be used for UDP, and you will calculate the TCP port
    //of any server by adding 2 to the UDP port.
    public RoundRobinLeader(int tcpPort,InetSocketAddress address, Map<Long,InetSocketAddress> peerIDtoAddress,long gatewayID,ZooKeeperPeerServer myServer,boolean newLeaderGettingWork) throws IOException {
        this.setDaemon(true);
        this.gatewayID = gatewayID;
        this.messages = new LinkedBlockingQueue<>();
        this.tcpPort = tcpPort;
        this.myAddress = address;
        this.peerIDtoAddress = new HashMap<>(peerIDtoAddress);
        this.servers = new LinkedBlockingQueue<>();
        this.serverToRequests = new ConcurrentHashMap<>();
        this.tcpServer = new TCPServer(this.tcpPort,this.messages);
        this.myServer = myServer;
        this.myID = myServer.getServerId();
        this.sentNewLeaderRequests = false;
        this.newLeaderGettingWork = newLeaderGettingWork;
        this.logger = initializeLogging("RoundRobinLeader" + "-on-port-" + this.tcpPort,true);
        //this.logger.fine("initiating logger for round robin with server id: " + id);
        this.requestToMessage = new ConcurrentHashMap<>();

        for(Long l : this.peerIDtoAddress.keySet()){
            if(!l.equals(myID)&& !l.equals(gatewayID)) {
                this.servers.offer(l);
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
            if(newLeaderGettingWork && !sentNewLeaderRequests){

                System.out.println("sending new leader requests");
                for(long id : this.servers){
                    InetSocketAddress adr = this.peerIDtoAddress.get(id);
                    threadPool.execute(new TCPClient(adr,this.myAddress, logger,requestToMessage,serverToRequests,id));
                }
                System.out.println("have completed sending all the new leader requests");
                sentNewLeaderRequests = true;
            }
//            while (newDeath) {
//                Thread.onSpinWait();
//            }
            try {
                if(this.logger == null){
                    this.logger = initializeLogging(JavaRunnerFollower.class.getCanonicalName() + "-on-server-with-udpPort-" + this.tcpPort);
                }
                System.out.println("before take");
                Map.Entry<Message, Socket> received = this.messages.take();
                System.out.println("after take");
                if (received != null && !this.myServer.isPeerDead(new InetSocketAddress(received.getKey().getSenderPort()-2))) {
                    Message message = received.getKey();
                    long requestID = message.getRequestID();
                    if(message.getMessageType() == Message.MessageType.WORK && requestToMessage.containsKey(requestID)){
                        if(requestID == -1){
                            break;
                        }
                        Map.Entry<Message, Socket> entry = requestToMessage.get(requestID);
                        Message newMessage = entry.getKey();
                        Message m = new Message(Message.MessageType.COMPLETED_WORK, newMessage.getMessageContents(), this.myAddress.getHostName(),this.tcpPort,newMessage.getSenderHost(),newMessage.getReceiverPort(),requestID);
                        Socket bigSocket = received.getValue();
                        OutputStream out2 = bigSocket.getOutputStream();
                        out2.write(m.getNetworkPayload());
                        bigSocket.close();
                        break;
                    }
                    this.requestToMessage.put(requestID,new AbstractMap.SimpleEntry<>(message,received.getValue()));
                    long tempId = this.servers.poll();
                    addToMap(tempId,requestID);
                    InetSocketAddress adr = peerIDtoAddress.get(tempId);
                    this.logger.info("sending message with request ID: " + requestID + " to port " + adr.getPort()+2);
                    Message newMessage = new Message(message.getMessageType(), message.getMessageContents(), this.myAddress.getHostName(),this.tcpPort,adr.getHostString(), adr.getPort()+2,requestID);
                    this.servers.offer(tempId);
                    threadPool.execute(new TCPClient(new AbstractMap.SimpleEntry<>(newMessage,received.getValue()), logger,requestToMessage,serverToRequests,tempId));
                }
            }
            catch (IOException e) {
                this.logger.log(Level.WARNING,"failed to send packet", e);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        this.logger.log(Level.SEVERE,"Exiting JavaRunnerFollower.run()");

    }

    private void addToMap(long tempId, long requestID) {
        Set<Long> temp = serverToRequests.get(tempId);
        if (temp == null){
            temp = new HashSet<>();
        }
        temp.add(requestID);
        serverToRequests.put(tempId,temp);
    }

    public void reportDeadFollower(long id){
        newDeath = true;
        servers.remove(id);
        if(serverToRequests.get(id)!=null) {
            for (long request : serverToRequests.get(id)) {
                System.out.println("request ID " + request);
                Map.Entry<Message, Socket> m = requestToMessage.get(request);
                System.out.println("entry = " + m);
                //not sure if i need to be redoing this work in a more urgent manner but this should work for now
                messages.offer(m);
                //Message m = requestToMessage.remove(request);
            }
            serverToRequests.remove(id);
        }
        newDeath = false;
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
