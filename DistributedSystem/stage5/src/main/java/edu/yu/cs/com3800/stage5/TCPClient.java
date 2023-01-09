package edu.yu.cs.com3800.stage5;

import edu.yu.cs.com3800.Message;
import edu.yu.cs.com3800.Util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.AbstractMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Logger;

//no clue if im actually gonna use this or if its just gonna make things harder
public class TCPClient extends Thread {
    private final Socket bigSocket;
    private final Message message;
    private final Logger logger;
    private final InetSocketAddress adr;
    private final InetSocketAddress leaderAdr;
    private final ConcurrentMap<Long,Map.Entry<Message, Socket>> requestToMessage;
    private final ConcurrentMap<Long, Set<Long>> serverToRequests;
    private final long id;
    public TCPClient(Map.Entry<Message,Socket> entry, Logger logger, ConcurrentMap<Long,Map.Entry<Message, Socket>> requestToMessage, ConcurrentMap<Long, Set<Long>> serverToRequests, long id){
        this.message = entry.getKey();
        this.bigSocket = entry.getValue();
        this.logger = logger;
        this.requestToMessage = requestToMessage;
        this.adr = null;
        this.leaderAdr = null;
        this.serverToRequests = serverToRequests;
        this.id = id;
    }
    public TCPClient(InetSocketAddress adr, InetSocketAddress leaderAdr, Logger logger, ConcurrentMap<Long,Map.Entry<Message, Socket>> requestToMessage, ConcurrentMap<Long, Set<Long>> serverToRequests, long id){
        this.message = null;
        this.logger = logger;
        this.requestToMessage = requestToMessage;
        this.bigSocket = null;
        this.adr = adr;
        this.leaderAdr = leaderAdr;
        this.serverToRequests = serverToRequests;
        this.id = id;
    }
    @Override
    public void run(){
              // Server name or IP address
        // Convert argument String to bytes using the default character encoding

        // Create socket that is connected to server on specified port
        Socket socket = null;
        try {
            if(this.bigSocket == null){
                socket = new Socket(this.adr.getHostName(), this.adr.getPort()+2);
                InputStream in = socket.getInputStream();
                OutputStream out = socket.getOutputStream();
                Message m = new Message(Message.MessageType.NEW_LEADER_GETTING_LAST_WORK,"null".getBytes(),leaderAdr.getHostName(),leaderAdr.getPort(),adr.getHostName(),adr.getPort()+2);
                out.write(m.getNetworkPayload());
                byte[] bytes = Util.readAllBytesFromNetwork(in);
                Message mess = new Message(bytes);
                if(mess.getRequestID() != -1)
                    this.requestToMessage.put(mess.getRequestID(),new AbstractMap.SimpleEntry<>(mess,socket));
            } else {
                socket = new Socket(message.getReceiverHost(), message.getReceiverPort());
                InputStream in = socket.getInputStream();
                OutputStream out = socket.getOutputStream();
                byte[] sendBytes = message.getNetworkPayload();
                out.write(sendBytes);  // Send the encoded string to the server
                byte[] bytes = Util.readAllBytesFromNetwork(in);
                Message m = new Message(bytes);
                OutputStream out2 = this.bigSocket.getOutputStream();
                out2.write(m.getNetworkPayload());
                this.bigSocket.close();
                socket.close();  // Close the socket and its streams
                this.logger.info("sent out message with ID " + m.getRequestID());
                this.requestToMessage.remove(m.getRequestID());
                Set<Long> requests = this.serverToRequests.get(this.id);
                if(requests.size() <=1){
                    serverToRequests.remove(this.id);
                } else {
                    requests.remove(m.getRequestID());
                    serverToRequests.put(this.id,requests);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
