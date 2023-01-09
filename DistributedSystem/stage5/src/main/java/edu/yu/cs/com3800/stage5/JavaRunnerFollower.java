package edu.yu.cs.com3800.stage5;

import edu.yu.cs.com3800.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.PrivateKey;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JavaRunnerFollower extends Thread implements LoggingServer {
    private int tcpPort;
    private InetSocketAddress myAddress;
    private LinkedBlockingQueue<Message> outgoingMessages;
    private LinkedBlockingQueue<Message> incomingMessages;
    private LinkedBlockingQueue<WorkDetails> queuedWork;
    private Logger logger;
    private ZooKeeperPeerServer myServer;
    private ServerSocket serverSocket;

    //In each peer server, the port passed to the constructor will continue to be used for UDP, and you will calculate the TCP port
    //of any server by adding 2 to the UDP port.
    public JavaRunnerFollower(int tcpPort, InetSocketAddress myAddress, LinkedBlockingQueue<Message> outgoingMessages, LinkedBlockingQueue<Message> incomingMessages, ZooKeeperPeerServer myServer) throws IOException {
        this.setDaemon(true);
        this.tcpPort = tcpPort;
        this.myAddress = myAddress;
        this.outgoingMessages = outgoingMessages;
        this.incomingMessages = incomingMessages;
        this.logger = initializeLogging( "JavaRunnerFollower-on-port-" + this.tcpPort);
        this.logger.log(Level.FINE,"initiating logger for JavaRunnerFollower with server address: " + myAddress);
        this.myServer = myServer;
        this.queuedWork = new LinkedBlockingQueue<>();
        try {
            this.serverSocket = new ServerSocket(tcpPort);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    class WorkDetails{
        private final String rs;
        private final long requestID;
        public WorkDetails(String rs, long requestID){
            this.rs = rs;
            this.requestID = requestID;
        }
        public long getRequestID() {return requestID;}
        public String getRs() {return rs;}
    }
    //When the leader assigns this node some work to do, this class uses a JavaRunner to do the work, and returns the results back to the leader.
    @Override
    public void run(){
//        ServerSocket servSock = null;

        while (!this.isInterrupted()) {
            try {
                if(this.logger == null){
                    this.logger = initializeLogging(JavaRunnerFollower.class.getCanonicalName() + "-on-server-with-udpPort-" + this.tcpPort);
                }
                Socket clntSock = null;
                try {
                    clntSock = serverSocket.accept(); // Get client connection
                } catch (Exception e){
                    System.out.println("this is the server throwing the issue " + tcpPort);
                    return;
                }
//                if(clntSock == null){
//                    System.out.println("is there an issue now????? " + tcpPort);
//                    clntSock = serverSocket.accept();
//                }
                //SocketAddress clientAddress = clntSock.getRemoteSocketAddress();
                InputStream in = clntSock.getInputStream();
                OutputStream out = clntSock.getOutputStream();
                Message m = new Message(Util.readAllBytesFromNetwork(in));
                //check if leader is dead, if so ignore
                String rs = "noWork";
                JavaRunner jr = new JavaRunner();
                long requestID = m.getRequestID();
                String leaderHost = m.getSenderHost();
                int leaderPort = m.getSenderPort();
                try {
                    //rs =jr.compileAndRun(new ByteArrayInputStream(m.getMessageContents()));
                    if(m.getMessageType() != Message.MessageType.NEW_LEADER_GETTING_LAST_WORK){
                        rs =jr.compileAndRun(new ByteArrayInputStream(m.getMessageContents()));
                    }
                    if(this.myServer.getPeerState()== ZooKeeperPeerServer.ServerState.LOOKING){
                        this.queuedWork.offer(new WorkDetails(rs,m.getRequestID()));
                    } else {
                        if(m.getMessageType() == Message.MessageType.NEW_LEADER_GETTING_LAST_WORK){
                            if(!queuedWork.isEmpty()) {
                                WorkDetails finePrint = queuedWork.take();
                                rs = finePrint.getRs();
                                requestID = finePrint.getRequestID();
                            } else {
                                requestID = -1;
                            }
                        } else {
                            rs =jr.compileAndRun(new ByteArrayInputStream(m.getMessageContents()));
                        }
                        Message completed = new Message(Message.MessageType.COMPLETED_WORK,rs.getBytes(), "localhost", tcpPort, leaderHost, leaderPort, requestID);
                        out.write(completed.getNetworkPayload());
                        out.close();
                    }
                } catch (Exception e) {
                    rs = e.getMessage();
                    rs +="\n";
                    Message error = new Message(Message.MessageType.COMPLETED_WORK,rs.getBytes(), m.getReceiverHost(), m.getReceiverPort(), m.getSenderHost(), m.getSenderPort(), m.getRequestID(),true);
                    out.write(error.getNetworkPayload());
                    out.close();
                    logger.severe("Exception thrown while compiling and running");
                    return;
                }

            }
            catch (IOException e) {
                this.logger.log(Level.WARNING,"failed to send packet", e);
            }
        }
//        try {
//            servSock.close();
//        } catch (IOException e) {
//            System.out.println("bottom thrown");
//            throw new RuntimeException(e);
//        }
        this.logger.log(Level.SEVERE,"Exiting JavaRunnerFollower.run()");

    }
    public void shutdown() {
        try {
            this.serverSocket.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        interrupt();
    }
}
