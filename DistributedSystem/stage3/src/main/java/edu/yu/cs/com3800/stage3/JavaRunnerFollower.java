package edu.yu.cs.com3800.stage3;

import edu.yu.cs.com3800.*;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;
import edu.yu.cs.com3800.*;

public class JavaRunnerFollower extends Thread implements LoggingServer {
    private long myPort;
    private InetSocketAddress myAddress;
    private LinkedBlockingQueue<Message> outgoingMessages;
    private LinkedBlockingQueue<Message> incomingMessages;
    private Logger logger;
    public JavaRunnerFollower(long myPort, InetSocketAddress myAddress, LinkedBlockingQueue<Message> outgoingMessages, LinkedBlockingQueue<Message> incomingMessages) throws IOException {
        this.setDaemon(true);
        this.myPort = myPort;
        this.myAddress = myAddress;
        this.outgoingMessages = outgoingMessages;
        this.incomingMessages = incomingMessages;
        this.logger = initializeLogging(JavaRunnerFollower.class.getCanonicalName() + "-on-port-" + this.myPort);
        this.logger.fine("initiating logger for JavaRunnerFollower with server address: " + myAddress);
    }
    //When the leader assigns this node some work to do, this class uses a JavaRunner to do the work, and returns the results back to the leader.
    @Override
    public void run(){
        while (!this.isInterrupted()) {
            try {
                if(this.logger == null){
                    this.logger = initializeLogging(JavaRunnerFollower.class.getCanonicalName() + "-on-server-with-udpPort-" + this.myPort);
                }
                Message received = this.incomingMessages.poll();
                //Message(Message.MessageType type, byte[] contents, String senderHost, int senderPort, String receiverHost, int receiverPort, long requestID)
                if (received != null && received.getMessageType() == Message.MessageType.WORK) {
                    String rs;
                    JavaRunner jr = new JavaRunner();
                    try {
                        rs =jr.compileAndRun(new ByteArrayInputStream(received.getMessageContents()));
                        Message completed = new Message(Message.MessageType.COMPLETED_WORK,rs.getBytes(), received.getReceiverHost(), received.getReceiverPort(), received.getSenderHost(), received.getSenderPort(), received.getRequestID());
                        this.outgoingMessages.offer(completed);
                        this.logger.fine("completed work with id: " + completed.getRequestID() + "and sending back to leader at port: " + received.getReceiverPort());
                    } catch (Exception e) {
                        rs = e.getMessage();
                        rs +="\n";
                        Message error = new Message(Message.MessageType.COMPLETED_WORK,rs.getBytes(), received.getReceiverHost(), received.getReceiverPort(), received.getSenderHost(), received.getSenderPort(), received.getRequestID(),true);
                        this.outgoingMessages.offer(error);
                        logger.severe("Exception thrown while compiling and running");
                        return;
                    }
                }
            }
            catch (IOException e) {
                this.logger.log(Level.WARNING,"failed to send packet", e);
            }
        }
        this.logger.log(Level.SEVERE,"Exiting JavaRunnerFollower.run()");

    }
    public void shutdown() {
        interrupt();
    }
}
