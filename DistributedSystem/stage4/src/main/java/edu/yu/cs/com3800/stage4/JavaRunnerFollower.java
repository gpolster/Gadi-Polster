package edu.yu.cs.com3800.stage4;

import edu.yu.cs.com3800.JavaRunner;
import edu.yu.cs.com3800.LoggingServer;
import edu.yu.cs.com3800.Message;
import edu.yu.cs.com3800.Util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.AbstractMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

public class JavaRunnerFollower extends Thread implements LoggingServer {
    private int tcpPort;
    private InetSocketAddress myAddress;
    private LinkedBlockingQueue<Message> outgoingMessages;
    private LinkedBlockingQueue<Message> incomingMessages;
    private Logger logger;

    //In each peer server, the port passed to the constructor will continue to be used for UDP, and you will calculate the TCP port
    //of any server by adding 2 to the UDP port.
    public JavaRunnerFollower(int tcpPort, InetSocketAddress myAddress, LinkedBlockingQueue<Message> outgoingMessages, LinkedBlockingQueue<Message> incomingMessages) throws IOException {
        this.setDaemon(true);
        this.tcpPort = tcpPort;
        this.myAddress = myAddress;
        this.outgoingMessages = outgoingMessages;
        this.incomingMessages = incomingMessages;
        this.logger = initializeLogging( "JavaRunnerFollower-on-port-" + this.tcpPort);
        this.logger.log(Level.FINE,"initiating logger for JavaRunnerFollower with server address: " + myAddress);
    }
    //When the leader assigns this node some work to do, this class uses a JavaRunner to do the work, and returns the results back to the leader.
    @Override
    public void run(){
        ServerSocket servSock = null;
        try {
            servSock = new ServerSocket(tcpPort);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        while (!this.isInterrupted()) {
            try {
                if(this.logger == null){
                    this.logger = initializeLogging(JavaRunnerFollower.class.getCanonicalName() + "-on-server-with-udpPort-" + this.tcpPort);
                }
                Socket clntSock = servSock.accept(); // Get client connection
                //SocketAddress clientAddress = clntSock.getRemoteSocketAddress();
                InputStream in = clntSock.getInputStream();
                OutputStream out = clntSock.getOutputStream();
                Message m = new Message(Util.readAllBytesFromNetwork(in));
                String rs;
                JavaRunner jr = new JavaRunner();
                try {
                    rs =jr.compileAndRun(new ByteArrayInputStream(m.getMessageContents()));
                    Message completed = new Message(Message.MessageType.COMPLETED_WORK,rs.getBytes(), m.getReceiverHost(), m.getReceiverPort(), m.getSenderHost(), m.getSenderPort(), m.getRequestID());
                    out.write(completed.getNetworkPayload());
                    out.close();
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
        this.logger.log(Level.SEVERE,"Exiting JavaRunnerFollower.run()");

    }
    public void shutdown() {
        interrupt();
    }
}
