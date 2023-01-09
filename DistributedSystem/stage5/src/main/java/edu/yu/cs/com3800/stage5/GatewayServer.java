package edu.yu.cs.com3800.stage5;

import com.sun.net.httpserver.Headers;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import edu.yu.cs.com3800.*;

import java.io.*;
import java.net.ConnectException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.lang.Thread.sleep;

public class GatewayServer implements LoggingServer {
    private int gatewayPort;
    private static int tcpPort;
    private InetSocketAddress myTCPAddress;
    private static String host;
    private HttpServer server;
    private Logger logger;
    private static GatewayPeerServerImpl gp;
    private static LinkedBlockingQueue<ClientRequest> waitingRequests;
    private static Map<Long, InetSocketAddress> peerIDtoAddress;
    private static long requestID = 0;

    public GatewayServer(int gatewayPort, int udpPort, long peerEpoch, Long serverID, Map<Long, InetSocketAddress> peerIDtoAddress, Long gatewayID, int numberOfObservers) throws IOException {
        this.server = HttpServer.create(new InetSocketAddress(gatewayPort), 0);
        this.gatewayPort = gatewayPort;
        tcpPort = udpPort +2;
        this.myTCPAddress = new InetSocketAddress(gatewayPort);
        waitingRequests = new LinkedBlockingQueue<>();
        host = "localhost";
        this.logger = initializeLogging("GatewayServer-on-port-" + this.gatewayPort,true);
        server.createContext("/compileandrun", new MyHandler());
//        server.createContext("/isLeader", new LogHandler());
        gp = new GatewayPeerServerImpl(udpPort,peerEpoch, serverID, peerIDtoAddress, gatewayID, numberOfObservers);
        gp.start();

    }
    static class ClientRequest{
        private final byte[] contents;
        private final HttpExchange nyse;
        public ClientRequest(byte [] contents, HttpExchange nyse){
            this.contents = contents;
            this.nyse = nyse;
        }
        public byte[] getContents() {return contents;}
        public HttpExchange getExchange(){ return nyse;}
    }
//    class LogHandler implements HttpHandler {
//        public void handle(HttpExchange t) throws IOException {
//            System.out.println("got request");
//            StringBuilder rs;
//            OutputStream os;
//            if(gp.getCurrentLeader() != null){
//                rs = new StringBuilder();
//                for(long id : peerIDtoAddress.keySet()){
//                    System.out.println("ARE WE CHILLING");
//                    InetSocketAddress add = peerIDtoAddress.get(id);
//                    if(id == gp.getGatewayID()){
//                        rs.append("\nServer on port ").append(add.getPort()).append(" whose ID is ").append(id).append(" has the following ID as its leader: ").append(gp.getCurrentLeader().getProposedLeaderID()).append(" and its state is ").append(ZooKeeperPeerServer.ServerState.OBSERVER);
//                    } else if (id == gp.getCurrentLeader().getProposedLeaderID()){
//                        rs.append("\nServer on port ").append(add.getPort()).append(" whose ID is ").append(id).append(" has the following ID as its leader: ").append(gp.getCurrentLeader().getProposedLeaderID()).append(" and its state is ").append(ZooKeeperPeerServer.ServerState.LEADING);
//                    } else {
//                        rs.append("\nServer on port ").append(add.getPort()).append(" whose ID is ").append(id).append(" has the following ID as its leader: ").append(gp.getCurrentLeader().getProposedLeaderID()).append(" and its state is ").append(ZooKeeperPeerServer.ServerState.FOLLOWING);
//                    }
//                }
//                t.sendResponseHeaders(200, rs.length());
//            }else {
//                rs = new StringBuilder("Gateway does NOT have a leader");
//                t.sendResponseHeaders(404, rs.length());
//            }
//            logger.info("successful server experience, hope you enjoyed!");
//            os = t.getResponseBody();
//            os.write(rs.toString().getBytes());
//            os.close();
//        }
//    }

    class MyHandler implements HttpHandler {

        public void handle(HttpExchange t) throws IOException {
            String rs = "potato";
            Headers headers = t.getRequestHeaders();
            OutputStream os;
            logger.fine("accepted http connection: " + t.getResponseHeaders());
            if(gp.getCurrentLeader()==null) {
                System.out.println("leader died on line 62");
                while (gp.getCurrentLeader() == null) {
                    Thread.onSpinWait();
                }
                System.out.println("new leader ID " + gp.getCurrentLeader().getProposedLeaderID());
            }
            if (!headers.getFirst("Content-Type").contains("text/x-java-source")){
                logger.severe("Content-Type is not of type text/x-java-source");
                t.sendResponseHeaders(400, rs.length());
                os = t.getResponseBody();
                os.write(rs.getBytes());
                os.close();
                return;
            }
            InputStream is = t.getRequestBody();
            byte [] contents = is.readAllBytes();
            ClientRequest request = new ClientRequest(contents,t);
            try {
                //waitingRequests.offer(request);
                Message received = contactLeader(contents);
                if(received == null){
                    while(gp.getCurrentLeader() != null){
                        Thread.onSpinWait();
                    }
                }
                if(gp.getCurrentLeader()==null) {
                    System.out.println("leader died on line 92");
                    while (gp.getCurrentLeader() == null) {
                        Thread.onSpinWait();
                    }
                    sleep(5000);
                    System.out.println("new leader ID " + gp.getCurrentLeader().getProposedLeaderID());
                    received = contactLeader(contents);
                }
                rs = new String(received.getMessageContents());
                logger.fine("recieved message: " + rs);
            } catch (Exception e) {
                e.printStackTrace();
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
            //if(gp.getCurrentLeader()!=null) {
                t.sendResponseHeaders(200, rs.length());
                os = t.getResponseBody();
                os.write(rs.getBytes());
                os.close();
                //waitingRequests.remove(request);
                logger.severe("sent message back to " + t.getResponseHeaders() + " and exiting");
            //}
        }
        private Message contactLeader(byte[] contents){
            try {
                InetSocketAddress leader = gp.getPeerByID(gp.getCurrentLeader().getProposedLeaderID());
                System.out.println("REQUEST ID " + requestID);
                Message toSend = new Message(Message.MessageType.WORK, contents, myTCPAddress.getHostName(), GatewayServer.tcpPort,
                        leader.getHostName(), leader.getPort()+2,requestID);
                requestID++;
                logger.log(Level.FINE, "sending message to leader at port: " + leader.getPort());
                //Message(MessageType type, byte[] contents, String senderHost, int senderPort, String receiverHost, int receiverPort)
                Socket socket = new Socket("localhost", leader.getPort()+2);
                InputStream in = null;
                OutputStream out;
                in = socket.getInputStream();
                out = socket.getOutputStream();
                System.out.println("before send to leader at port " + (leader.getPort()+2));
                out.write(toSend.getNetworkPayload());
                byte[] bytes = Util.readAllBytesFromNetwork(in);
                Message received = new Message(bytes);
                socket.close();
                System.out.println("after message recieved back");
                if(gp.isPeerDead(new InetSocketAddress(received.getSenderPort()-2))){
                    System.out.println("leader died on line 145");
                    while(gp.getCurrentLeader()==null){
                        Thread.onSpinWait();//to deal with possible ead leader response
                    }
                    System.out.println("new leader ID " + gp.getCurrentLeader().getProposedLeaderID());
                    contactLeader(contents);
                }
                return received;
            } catch (ConnectException e){
//                e.printStackTrace();
                return null;
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
              // Send the encoded string to the server
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
