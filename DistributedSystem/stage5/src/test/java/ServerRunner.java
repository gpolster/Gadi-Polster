


import edu.yu.cs.com3800.stage5.GatewayServer;
import edu.yu.cs.com3800.stage5.ZooKeeperPeerServerImpl;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

import static java.lang.Thread.sleep;

public class ServerRunner {

//    private static final int[] udpPorts = {8010, 8020, 8030, 8040, 8050, 8060, 8070, 8090};;
//    private static long peerEpoch = 0L;
//    private static long serverID;
//    private static Map<Long, InetSocketAddress> peerIDtoAddress;
//    private ZooKeeperPeerServerImpl peerServer;
//    private GatewayServer gateway;
//    public ServerRunner(){
//        System.out.println("creating serverrunner");
//        ServerRunner.peerIDtoAddress = new HashMap<>(8);
//        for (int i = 0; i < udpPorts.length; i++) {
//            peerIDtoAddress.put(Integer.valueOf(i).longValue(), new InetSocketAddress("localhost", udpPorts[i]));
//        }
//    }
    public static void main(String[] args) throws IOException, InterruptedException {
        int[] udpPorts = {8010, 8020, 8030, 8040, 8050, 8060, 8070, 8090};
//        System.out.println("creating serverrunner");
        Map<Long, InetSocketAddress> peerIDtoAddress = new HashMap<>(8);
        for (int i = 0; i < udpPorts.length; i++) {
            peerIDtoAddress.put(Integer.valueOf(i).longValue(), new InetSocketAddress("localhost", udpPorts[i]));
        }
//        System.out.println("Hello, World!");
        int id = Integer.parseInt(args[0]);
        long serverID = id;
//        System.out.println("hello " + id);
        long gatewayID = 7L;
        int numberOfObservers = 1;
//        HashMap<Long, InetSocketAddress> map = (HashMap<Long, InetSocketAddress>) ((HashMap<Long, InetSocketAddress>) peerIDtoAddress).clone();
        if(gatewayID == serverID){
//            System.out.println("should create gateway");
            peerIDtoAddress.remove(serverID);
            GatewayServer gateway = new GatewayServer(9900,udpPorts[id],0L, serverID, peerIDtoAddress, gatewayID, numberOfObservers);
            gateway.start();
//            System.out.println("gateway created");
        } else {
            peerIDtoAddress.remove(serverID);
            ZooKeeperPeerServerImpl peerServer = new ZooKeeperPeerServerImpl(udpPorts[id],0L, serverID,peerIDtoAddress, gatewayID, numberOfObservers);
            sleep(4200- (id*600));
            peerServer.start();

//            System.out.println("peerServer created");
        }
        return;

    }
}
