package edu.yu.cs.com3800.stage2;

import static java.lang.Thread.sleep;
import static org.junit.jupiter.api.Assertions.*;

import edu.yu.cs.com3800.*;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.Assertions.*;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.LinkedBlockingQueue;

public class Stage2Test {

    @Test
    public void testBuildMsgContent(){
        ElectionNotification hadoop = new ElectionNotification(2L, ZooKeeperPeerServer.ServerState.LOOKING,1L,1L);
        byte[] answer = ZooKeeperLeaderElection.buildMsgContent(hadoop);
        assertNotNull(answer);
    }
    @Test
    public void testGetNotificationFromMessage(){
        ElectionNotification hadoop = new ElectionNotification(2L, ZooKeeperPeerServer.ServerState.LOOKING,1L,1L);
        byte[] answer = ZooKeeperLeaderElection.buildMsgContent(hadoop);
        Message boop = new Message(Message.MessageType.ELECTION,answer,"localhost",8000, "localhost",8000);
        ElectionNotification en = ZooKeeperLeaderElection.getNotificationFromMessage(boop);
        assertEquals(hadoop,en);
    }
    @Test
    public void ZooKeeperQuorumPeerServerDemo() throws InterruptedException {
        //create IDs and addresses
        for (int i = 0; i < 50; i++) {
            System.out.println(i);
            tradeMessages();
            sleep(300);
        }
    }
    @Test
    public  void tradeMessages() {
        //create IDs and addresses
        HashMap<Long, InetSocketAddress> peerIDtoAddress = new HashMap<>(3);
        peerIDtoAddress.put(1L, new InetSocketAddress("localhost", 8010));
        peerIDtoAddress.put(2L, new InetSocketAddress("localhost", 8020));
        peerIDtoAddress.put(3L, new InetSocketAddress("localhost", 8030));
        peerIDtoAddress.put(4L, new InetSocketAddress("localhost", 8040));
        peerIDtoAddress.put(5L, new InetSocketAddress("localhost", 8050));
        peerIDtoAddress.put(6L, new InetSocketAddress("localhost", 8060));
        peerIDtoAddress.put(7L, new InetSocketAddress("localhost", 8070));
        peerIDtoAddress.put(8L, new InetSocketAddress("localhost", 8080));

        //create servers
        ArrayList<ZooKeeperPeerServer> servers = new ArrayList<>(3);
        for (Map.Entry<Long, InetSocketAddress> entry : peerIDtoAddress.entrySet()) {
            HashMap<Long, InetSocketAddress> map = (HashMap<Long, InetSocketAddress>) peerIDtoAddress.clone();
            map.remove(entry.getKey());
            ZooKeeperPeerServerImpl server = new ZooKeeperPeerServerImpl(entry.getValue().getPort(), 0, entry.getKey(), map);
            servers.add(server);
            new Thread(server, "Server on port " + server.getAddress().getPort()).start();
        }
        //wait for threads to start
        try {
            sleep(1000);
        }
        catch (Exception e) {
        }
        //print out the leaders and shutdown
        for (ZooKeeperPeerServer server : servers) {
            Vote leader = server.getCurrentLeader();
            if (leader != null) {
                System.out.println("Server on port " + server.getAddress().getPort() + " whose ID is " + server.getServerId() + " has the following ID as its leader: " + leader.getProposedLeaderID() + " and its state is " + server.getPeerState().name());
                assertEquals(8,leader.getProposedLeaderID());
                if (server.getServerId() != 8) {
                    assertEquals(ZooKeeperPeerServer.ServerState.FOLLOWING, server.getPeerState());
                } else {
                    assertEquals(ZooKeeperPeerServer.ServerState.LEADING, server.getPeerState());
                }
                server.shutdown();
            } else {
                System.out.println("leader is null");
            }
        }
    }
}
