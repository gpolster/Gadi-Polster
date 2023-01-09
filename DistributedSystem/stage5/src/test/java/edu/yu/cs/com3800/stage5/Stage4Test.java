package edu.yu.cs.com3800.stage5;

import edu.yu.cs.com3800.Vote;
import edu.yu.cs.com3800.ZooKeeperPeerServer;
import edu.yu.cs.com3800.stage5.GatewayPeerServerImpl;
import edu.yu.cs.com3800.stage5.GatewayServer;
import edu.yu.cs.com3800.stage5.ZooKeeperPeerServerImpl;
import edu.yu.cs.com3800.stage5.ClientImpl;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Stage4Test {
    private String validClass = "package edu.yu.cs.fall2019.com3800.stage1;\n\npublic class HelloWorld\n{\n    public String run()\n    {\n        return \"Hello world!\";\n    }\n}\n";

    private int[] udpPorts = {8010, 8020, 8030, 8040, 8050, 8060, 8070, 8080, 8090};
    private int[] tcpPorts = {8012, 8022, 8032, 8042, 8052, 8062, 8072, 8082, 8092};
    private ClientImpl[] clients= new ClientImpl[8];
    private GatewayPeerServerImpl theWatcher;
    private GatewayServer gatewayServer;
    private int leaderPort;
    private int myPort = 9999;
    private InetSocketAddress myAddress = new InetSocketAddress("localhost", this.myPort);
    private ArrayList<ZooKeeperPeerServer> servers;

    @Test
    public void Stage4Demo() throws Exception {
        //step 1: create sender & sending queue

        //step 2: create servers and clients
        createServers();
        createClients();
        //step2.1: wait for servers to get started
        try {
            Thread.sleep(5000);
        }
        catch (InterruptedException e) {
            System.out.println("interrupted exception in test");
        }
        //this.leaderPort = theWatcher.getPeerByID(theWatcher.getCurrentLeader().getProposedLeaderID()).getPort();
        printLeaders();
        System.out.println("leaders printed");
        //step 3: since we know who will win the election, send requests to the leader, this.leaderPort
        for (int i = 0; i < this.udpPorts.length-1; i++) {
            String code = this.validClass.replace("world!", "world! from code version " + i);
            sendMessage(code, i);
        }
        System.out.println("messages sent");
        //step 4: validate responses from leader

        printResponses();

        //step 5: stop servers
        stopServers();
    }

    private void createClients() throws MalformedURLException {
        for (int i = 0; i < clients.length; i++) {
            clients[i] = new ClientImpl("localhost", 9000);
        }

    }

    private void printLeaders() {
        for (ZooKeeperPeerServer server : this.servers) {
            Vote leader = server.getCurrentLeader();
            if (leader != null) {
                System.out.println("Server on port " + server.getAddress().getPort() + " whose ID is " + server.getServerId() + " has the following ID as its leader: " + leader.getProposedLeaderID() + " and its state is " + server.getPeerState().name());
            } else {
                System.out.println("Server on port " + server.getAddress().getPort() + " whose ID is " + server.getServerId() + " is a piece of shit");
            }
        }
    }

    private void stopServers() {
        for (ZooKeeperPeerServer server : this.servers) {
            server.shutdown();
        }
    }

    private void printResponses() throws Exception {
        String completeResponse = "";
        for (int i = 0; i < udpPorts.length-1; i++) {
            ClientImpl.Response r = clients[i].getResponse();
            completeResponse += "Response to request " + i + ":\n" + r.getBody() + "\n\n";
            int version = r.getBody().charAt(r.getBody().length()-1)-48;
            assertEquals(version,i);
        }
        System.out.println(completeResponse);
    }

    private void sendMessage(String code, int i) throws IOException {
        //Message msg = new Message(Message.MessageType.WORK, code.getBytes(), this.myAddress.getHostString(), this.myPort, "localhost", this.leaderPort);
        ClientImpl client = clients[i];
        client.sendCompileAndRunRequest(code);
    }
    private void createServers() throws IOException, InterruptedException {
        //create IDs and addresses
        HashMap<Long, InetSocketAddress> peerIDtoAddress = new HashMap<>(8);
        for (int i = 0; i < this.udpPorts.length; i++) {
            peerIDtoAddress.put(Integer.valueOf(i).longValue(), new InetSocketAddress("localhost", this.udpPorts[i]));
        }
        //create servers
        this.servers = new ArrayList<>(3);
        for (Map.Entry<Long, InetSocketAddress> entry : peerIDtoAddress.entrySet()) {
            HashMap<Long, InetSocketAddress> map = (HashMap<Long, InetSocketAddress>) peerIDtoAddress.clone();
            //map.remove(entry.getKey());
            if(Objects.equals(entry.getKey(), Integer.valueOf(8).longValue())){
//                Thread.sleep(5000);
                this.gatewayServer = new GatewayServer(9000,entry.getValue().getPort(), 0L, entry.getKey(), map, entry.getKey(),1);
                this.gatewayServer.start();
                this.theWatcher = gatewayServer.getGatewayPeerServer();
                this.servers.add(theWatcher);

            } else {
                ZooKeeperPeerServerImpl server = new ZooKeeperPeerServerImpl(entry.getValue().getPort(), 0L, entry.getKey(), map, Integer.valueOf(8).longValue(),1);
                this.servers.add(server);
                server.start();
            }
        }
    }

}
