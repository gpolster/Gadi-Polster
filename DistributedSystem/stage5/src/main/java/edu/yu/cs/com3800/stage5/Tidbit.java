package edu.yu.cs.com3800.stage5;

import java.io.Serializable;
import java.nio.ByteBuffer;

public class Tidbit implements Serializable {
    private final long id;
    private int heartbeat;
    private long timeStamp;
    private byte[] payload;
    private boolean failed = false;
    public Tidbit(long id){
        this.id = id;
        this.heartbeat = 0;
        this.timeStamp = System.currentTimeMillis();
    }
    public Tidbit(byte [] contents){
        this.payload = contents;
        ByteBuffer buffer = ByteBuffer.wrap(this.payload);
        buffer.clear();
        this.id = buffer.getLong();
        this.heartbeat = buffer.getInt();
        this.timeStamp = buffer.getLong();
        byte[] messageContents = new byte[20];
        buffer.get(messageContents);
        this.payload = messageContents;
    }
    public Tidbit(long id, int heartbeat, long timeStamp){
        this.id = id;
        this.heartbeat = heartbeat;
        this.timeStamp = timeStamp;
    }

    public byte[] getPayload(){
        if (this.payload != null) {
            return this.payload;
        }
        /*
        size of buffer =
        1 long (request ID) = 8 bytes
        1 int for heartbeat = 4 bytes
        1 long for timeStamp = 8 bytes
        */
        ByteBuffer buffer = ByteBuffer.allocate(20);
        buffer.clear();
        buffer.putLong(this.id);
        buffer.putInt(this.heartbeat);
        buffer.putLong(this.timeStamp);
        buffer.flip();
        this.payload = buffer.array();
        return  this.payload;
    }
    public void setHeartbeat(int heartbeat) {
        this.heartbeat = heartbeat;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public int getHeartbeat() {
        return heartbeat;
    }

    public boolean isFailed() {
        return failed;
    }
    public void setFailed(boolean failed){
        this.failed = failed;
    }

    public long getId() {
        return id;
    }

    public long getTimeStamp() {
        return timeStamp;
    }
    @Override
    public String toString(){
        return "server ID: " + id + ", Heartbeat counter: " + heartbeat + ", timestamp: " + timeStamp;
    }
}
