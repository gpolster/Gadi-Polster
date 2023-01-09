package edu.yu.cs.com3800.stage4;

import edu.yu.cs.com3800.LoggingServer;
import edu.yu.cs.com3800.Message;
import edu.yu.cs.com3800.Util;
import java.util.logging.Logger;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

//no clue if im actually gonna use this or if its just gonna make things harder
public class TCPClient extends Thread {
    private final Socket bigSocket;
    private final Message message;
    private final Logger logger;
    public TCPClient(Map.Entry<Message,Socket> entry, Logger logger){
        this.message = entry.getKey();
        this.bigSocket = entry.getValue();
        this.logger = logger;
    }
    @Override
    public void run(){
              // Server name or IP address
        // Convert argument String to bytes using the default character encoding

        // Create socket that is connected to server on specified port
        Socket socket = null;
        try {
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
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }
}
