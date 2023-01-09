package edu.yu.cs.com3800.stage5;

import edu.yu.cs.com3800.LoggingServer;
import edu.yu.cs.com3800.Message;
import edu.yu.cs.com3800.Util;

import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.AbstractMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Logger;

public class TCPServer extends Thread implements LoggingServer {
    // A thread that uses ServerSocket.accept() to accept TCP connections from the GatewayServer, reads a Message from
    //the connection’s InputStream, and hands that message to the RoundRobinLeader to deal with.
    // You could do this within the RoundRobinLeader itself, or you could create a separate Thread whose only job is to do this.
    // (I personally took the later approach, creating a class called TCPServer and having it give the Messages to the RoundRobinLeader via a queue,
    // but you are not required to do it this way.)
    private static final int BUFSIZE = 32;   // Size of receive buffer
    private int servPort;
    private LinkedBlockingQueue<Map.Entry<Message,Socket>> messages;
    private Logger logger;
    public TCPServer(int port, LinkedBlockingQueue<Map.Entry<Message,Socket>> messages) throws IOException {
        this.setDaemon(true);
        this.servPort = port;
        this.messages = messages;
        this.logger = initializeLogging( "TCPServer-on-port-" + this.servPort,true);

    }

    @Override
    public void run(){
        // Create a server socket to accept client connection requests
        ServerSocket servSock = null;
        try {
            servSock = new ServerSocket(servPort,0);
        } catch (IOException e) {
            System.out.println("server on port " + servPort);
            throw new RuntimeException(e);
        }

        int recvMsgSize;   // Size of received message
        byte[] receiveBuf = new byte[BUFSIZE];  // Receive buffer

        while (!this.isInterrupted()) { // Run forever, accepting and servicing connections
            try {
                Socket clntSock = servSock.accept(); // Get client connection
                this.logger.fine("accepted tcp connection");
                InputStream in = clntSock.getInputStream();
                Message temp = new Message(Util.readAllBytesFromNetwork(in));
                Message m = new Message(temp.getMessageType(), temp.getMessageContents(), "localhost",this.servPort,temp.getSenderHost(),temp.getSenderPort(),temp.getRequestID());
                messages.offer(new AbstractMap.SimpleEntry<>(m,clntSock));
                this.logger.info("put message from port-" + m.getReceiverPort() + "-on queue");
            } catch (IOException e) {
                throw new RuntimeException(e);
            }


        }
        /* NOT REACHED */
        this.logger.severe("exiting TCPServer");
    }
    public void shutdown() {
        interrupt();
    }
}
