package edu.yu.cs.com3800.stage5;

import edu.yu.cs.com3800.Vote;
import edu.yu.cs.com3800.ZooKeeperPeerServer;
import org.junit.jupiter.api.Test;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class Stage5Test {
    private String validClass = "package edu.yu.cs.fall2019.com3800.stage1;\n\npublic class HelloWorld\n{\n    public String run()\n    {\n        return \"Hello world!\";\n    }\n}\n";
    private Process[] processes = new Process[8];
    private ClientImpl[] clients= new ClientImpl[9];

    @Test
    public void Stage5Demo() throws Exception {
        //step 1: create sender & sending queue

        //step 2: create servers and clients
        createServers();

        //step2.1: wait for servers to get started
        pause(15000);
        System.out.println("before clients are created");
        // Wait until the election has completed before sending any requests to the Gateway
        // In order to do this, you must add another http based service to the Gateway which can be called to ask if it has a leader or not
//        String leaderUrl = "http://localhost:9900/isLeader";
//        while (true) {
//            HttpURLConnection connection = (HttpURLConnection) new URL(leaderUrl).openConnection();
//            connection.setRequestMethod("GET");
//            int responseCode = connection.getResponseCode();
//            if (responseCode == 200) {
//                // If the Gateway has a leader, it should respond with the full list of nodes and their roles (follower vs leader)
//                // Script should print out the list of server IDs and their roles
//                BufferedReader in = new BufferedReader(new InputStreamReader(connection.getInputStream()));
//                String inputLine;
//                StringBuilder response = new StringBuilder();
//                while ((inputLine = in.readLine()) != null) {
//                    response.append(inputLine);
//                }
//                in.close();
//                System.out.println(response.toString());
//                break;
//            }
//            Thread.sleep(1000);
//        }
//        ClientImpl c = new ClientImpl("localhost", 9900);
//        while(true){
//            HttpURLConnection connect = c.sendLeaderRequest("http://localhost:9900/isLeader");
//            int responseCode = connect.getResponseCode();
//            if(responseCode == 200){
//                // If the Gateway has a leader, it should respond with the full list of nodes and their roles (follower vs leader)
//                // Script should print out the list of server IDs and their roles
//                BufferedReader in = new BufferedReader(new InputStreamReader(connect.getInputStream()));
//                String inputLine;
//                StringBuilder response = new StringBuilder();
//                while ((inputLine = in.readLine()) != null) {
//                    response.append(inputLine);
//                }
//                in.close();
//                System.out.println(response);
//                break;
//            }
//            pause(1000);
//        }

//
//        ClientImpl.Response re = c.getResponse();
//        String resp = re.getBody();
//        System.out.println("response code = " + re.getCode());
//        System.out.println(resp);
        createClients();
        System.out.println("after clients are created");
        pause(2000);

        //this.leaderPort = theWatcher.getPeerByID(theWatcher.getCurrentLeader().getProposedLeaderID()).getPort();
        //printLeaders();
        System.out.println("leaders printed");
        //step 3: since we know who will win the election, send requests to the leader, this.leaderPort
        for (int i = 0; i < 9; i++) {
            System.out.println("before message sent");
            String code = this.validClass.replace("world!", "world! from code version " + i);
            sendMessage(code, i);
            System.out.println("after message sent");
        }
        System.out.println("messages sent");
        //step 4: validate responses from leader

        printResponses();
        //stage 5 stuff now
        //ZooKeeperPeerServer rip = servers.get(2);
        System.out.println("RIP Server on port 8030 whose ID is 2");
        processes[2].destroyForcibly();
        pause(45000);
        System.out.println("oh captain my captain!!");
        processes[6].destroyForcibly();
        pause(1000);
        for (int i = 0; i < 9; i++) {
            System.out.println("before message sent");
            String code = this.validClass.replace("world!", "world! from code version " + i);
            sendMessage(code, i);
            System.out.println("after message sent");
        }
        pause(20000);
        printResponses();
        pause(1000);
        String code = this.validClass.replace("world!", "world! from code version " + 1);
        sendMessage(code, 1);
        ClientImpl.Response r = clients[1].getResponse();
        String completeResponse = "";
        completeResponse += "Response to request " + 1 + ":\n" + r.getBody() + "\n\n";
        System.out.println("response code = " + r.getCode());
        int version = r.getBody().charAt(r.getBody().length()-1)-48;
        assertEquals(1,version);
        System.out.println(completeResponse);
        //step 5: stop servers
        System.out.println("stopping servers");
        stopServers();
    }

    private void createClients() throws MalformedURLException {
        for (int i = 0; i < clients.length; i++) {
            clients[i] = new ClientImpl("localhost", 9900);
        }

    }

    private void pause(int millis){
        try {
            Thread.sleep(millis);
        }
        catch (InterruptedException e) {
            System.out.println("interrupted exception in test");
        }
    }
//    private void printLeaders() {
//        for (ZooKeeperPeerServer server : this.servers) {
//            Vote leader = server.getCurrentLeader();
//            if (leader != null) {
//                System.out.println("Server on port " + server.getAddress().getPort() + " whose ID is " + server.getServerId() + " has the following ID as its leader: " + leader.getProposedLeaderID() + " and its state is " + server.getPeerState().name());
//            } else {
//                System.out.println("Server on port " + server.getAddress().getPort() + " whose ID is " + server.getServerId() + " is a piece of shit");
//            }
//        }
//    }

    private void stopServers() {
        for (Process server : this.processes) {
            server.destroyForcibly();
        }
    }

    private void printResponses() throws Exception {
        String completeResponse = "";
        for (int i = 0; i < 9; i++) {
            ClientImpl.Response r = clients[i].getResponse();
            completeResponse += "Response to request " + i + ":\n" + r.getBody() + "\n\n";
            System.out.println("response code = " + r.getCode());
            int version = r.getBody().charAt(r.getBody().length()-1)-48;
            assertEquals(i,version);
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
//        HashMap<Long, InetSocketAddress> peerIDtoAddress = new HashMap<>(8);
//        for (int i = 0; i < this.udpPorts.length; i++) {
//            peerIDtoAddress.put(Integer.valueOf(i).longValue(), new InetSocketAddress("localhost", this.udpPorts[i]));
//        }
        //create servers
        for (int i = 0; i < 8; i++) {
            ProcessBuilder pb = new ProcessBuilder("javac", "-cp", "src/main/java:src/test/java", "src/test/java/ServerRunner.java");
            Process p = pb.start();
            p.waitFor();
            if (p.exitValue() != 0) {
                // Process failed, read error output
                BufferedReader reader = new BufferedReader(new InputStreamReader(p.getErrorStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    System.err.println(line);
                }
            } else {
                System.out.println("compiled?");
            }
            pb = new ProcessBuilder("java", "-cp","target/classes:target/test-classes","ServerRunner",((Integer)i).toString());
            pb.inheritIO();
            p = pb.start();
            processes[i] = p;
            int finalI = i;
            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                processes[finalI].destroy();
            }));
        }
    }
}
/*
NOTES:
    send messages using threads so they all happen at the same time?
    double check in election leader to make sure there isnt anyone with a higher ID that joined late
    - attempted and works on first attempt
    figure out this socket in use shit - lol kinda
    why are all my servers dying?/need to pause/reset gossiper better
    check how i deal with no new work on new leader getting last workDemo

 */









