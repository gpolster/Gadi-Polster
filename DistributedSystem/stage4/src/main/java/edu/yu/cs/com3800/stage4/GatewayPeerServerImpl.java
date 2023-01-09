package edu.yu.cs.com3800.stage4;

import java.net.InetSocketAddress;
import java.util.Map;

public class GatewayPeerServerImpl extends ZooKeeperPeerServerImpl{
    public GatewayPeerServerImpl(int udpPort, long peerEpoch, Long serverID, Map<Long, InetSocketAddress> peerIDtoAddress, Long gatewayID, int numberOfObservers) {
        super(udpPort,peerEpoch, serverID, peerIDtoAddress, gatewayID, numberOfObservers);
        super.setPeerState(ServerState.OBSERVER);
    }
    /*
        ...is a peer in the ZooKeeper cluster, but as an OBSERVER only – it does not get a vote in leader elections, rather it merely
        observes and watches for a winner so it knows who the leader is to which it should send client requests.
        o An OBSERVER must never change its state to any other ServerState
        o Other nodes must not count an OBSERVER when determining how many votes are needed for a quorum, and
        must not count any votes sent by an OBSERVER when determining if there is a quorum voting for a given server o Think though carefully what changes this will require to ZooKeeperLeaderElection!
     */
    @Override
    public void setPeerState(ServerState newState) {}
}
