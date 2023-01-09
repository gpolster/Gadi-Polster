package edu.yu.cs.com3800.stage4;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import edu.yu.cs.com3800.*;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.AbstractMap;
import java.util.Map;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

public class GatewayServer implements LoggingServer {
    private int gatewayPort;
    private static int tcpPort;
    private InetSocketAddress myTCPAddress;
    private static String host;
    private HttpServer server;
    private Logger logger;
    private static GatewayPeerServerImpl gp;

    //keeps track of what node is currently the leader and sends it all client requests over a TCP connection
    //is a peer in the ZooKeeper cluster (presumably through gatewayPeerServerImpl), but as an OBSERVER only – it does not get a vote in leader elections,
    // rather it merely observes and watches for a winner so it knows who the leader is to which it should send client requests.
    //will have a number of threads running
    /*
        o an HttpServer to accept client requests
        o a GatewayPeerServerImpl, which is a subclass of ZooKeeperPeerServerImpl which can only be an OBSERVER
        o foreveryclientconnection,the HttpServer creates and runs an HttpHandler (in a thread in a threadpool) which will, in turn,
        synchronously communicate with the master/leader over TCP to submit the client request and get a response that it will then return to the client.
        Be careful to not have any instance variables in your HttpHandler
        – its methods must be thread safe! Only use local variables in your methods.
     */
    //TCP must be used for all messages between servers that have to do with client requests
    //  -This doesn’t make things much more complicated for you in your GatewayServer since the HttpServer uses a thread for each client request anyway
    //HTTP must be used for all communication between clients and the gateway

    //creates an HTTPServer on whatever port number is passed to it in its constructor
    //In each peer server, the port passed to the constructor will continue to be used for UDP, and you will calculate the TCP port
    //of any server by adding 2 to the UDP port.
    public GatewayServer(int gatewayPort, int udpPort, long peerEpoch, Long serverID, Map<Long, InetSocketAddress> peerIDtoAddress, Long gatewayID, int numberOfObservers) throws IOException {
        this.server = HttpServer.create(new InetSocketAddress(gatewayPort), 0);
        this.gatewayPort = gatewayPort;
        this.tcpPort = udpPort +2;
        this.myTCPAddress = new InetSocketAddress(gatewayPort);
        this.host = "localhost";
        this.logger = initializeLogging("GatewayServer-on-port-" + this.gatewayPort,true);
        server.createContext("/compileandrun", new MyHandler());
        this.gp = new GatewayPeerServerImpl(udpPort,peerEpoch, serverID, peerIDtoAddress, gatewayID, numberOfObservers);
        this.gp.start();

        try {
            // This block configure the logger with handler and formatter
//            if (!Files.exists(p.getParent())) {
//                Files.createDirectory(p.getParent());
//            }
//            fh = new FileHandler(p + "MyLog.log", true);
//            logger.addHandler(fh);
//            SimpleFormatter formatter = new SimpleFormatter();
//            fh.setFormatter(formatter);
//            LOGGER.setUseParentHandlers(false);
        } catch (SecurityException e) {
            e.printStackTrace();
        }
    }
    //maybe create TCP server and client classes?
    //Gateway will connect to the master via TCP, and the RoundRobinLeader now must connect to worker nodes over TCP
    //I think gatewayServer would be the client in this case. and roundRobin the server, but then what about javaFollowerRunner and zookeeperPeerServer
    class MyHandler implements HttpHandler {



        public void handle(HttpExchange t) throws IOException {
            String rs = "potato";
            Headers headers = t.getRequestHeaders();
            OutputStream os;
            logger.fine("accepted http connection: " + t.getResponseHeaders());
            while(gp.getCurrentLeader()==null){
            }
            if (!headers.getFirst("Content-Type").contains("text/x-java-source")){
                logger.severe("Content-Type is not of type text/x-java-source");
                t.sendResponseHeaders(400, rs.length());
                os = t.getResponseBody();
                os.write(rs.getBytes());
                os.close();
                return;
            }
            //this is where i would be establishing a tcp connection, presumably as a client connecting to roundRobinLeader (server)
            InputStream is = t.getRequestBody();
            byte [] contents = is.readAllBytes();
            Message toSend;
            InetSocketAddress leader = gp.getPeerByID(gp.getCurrentLeader().getProposedLeaderID());
            try {
                logger.log(Level.FINE, "sending message to leader at port: " + leader.getPort());
                //Message(MessageType type, byte[] contents, String senderHost, int senderPort, String receiverHost, int receiverPort)
                toSend = new Message(Message.MessageType.WORK, contents,GatewayServer.host,GatewayServer.tcpPort,leader.getHostString(), leader.getPort()+2);
                Socket socket = new Socket("localhost", leader.getPort()+2);
                InputStream in = socket.getInputStream();
                OutputStream out = socket.getOutputStream();
                out.write(toSend.getNetworkPayload());  // Send the encoded string to the server
                byte[] bytes = Util.readAllBytesFromNetwork(in);
                Message received = new Message(bytes);
                socket.close();
                rs = new String(received.getMessageContents());
                logger.fine("recieved message: " + rs);
            } catch (Exception e) {
                rs = e.getMessage();
                rs +="\n";
                ByteArrayOutputStream osa = new ByteArrayOutputStream();
                PrintStream s = new PrintStream(osa);
                e.printStackTrace(s);
                rs += osa.toString(StandardCharsets.UTF_8);
                //LOGGER.severe("Exception thrown while compiling and running");
                t.sendResponseHeaders(400, rs.length());
                os = t.getResponseBody();
                os.write(rs.getBytes());
                os.close();
                return;
            }
            //logger.info("successful server experience, hope you enjoyed!");
            t.sendResponseHeaders(200, rs.length());
            os = t.getResponseBody();
            os.write(rs.getBytes());
            os.close();
            logger.severe("sent message back to " + t.getResponseHeaders() + " and exiting");
        }
    }

    public void start() {
        //server.setExecutor(null); // creates a default executor
        server.setExecutor(java.util.concurrent.Executors.newCachedThreadPool());
        //not entirely sure if this is okay/ a good thing to do. Maybe it is but i need to fix the number of threads?
        // https://stackoverflow.com/questions/14729475/can-i-make-a-java-httpserver-threaded-process-requests-in-parallel
        server.start();
    }
    public GatewayPeerServerImpl getGatewayPeerServer(){
        return this.gp;
    }

    public void stop() {
        server.stop(0);
    }
}
