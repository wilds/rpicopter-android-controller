package org.wilds.quadcontroller.app.communication.packet;

/**
 * Created by Wilds on 14/04/2014.
 */
//TODO remove json and send string message

public abstract class Packet {

    public static final String TYPE_HELLO = "hello";
    public static final String TYPE_MOTION = "rcinput";
    public static final String TYPE_HEARTBEAT = "heartbeat";
    public static final String TYPE_TEST_MOTOR = "tm";
    public static final String TYPE_QUERY_STATUS = "querystatus";
    public static final String TYPE_TAKE_PICTURE = "takepicture";
    public static final String TYPE_VIDEO = "vidsnap";
    public static final String TYPE_ALTITUDE_HOLDER_ENABLE = "altitudeholderenabled";
    public static final String TYPE_ALTITUDE_TARGET = "altitudetarget";

    protected long id = -1;
    protected String type;

    public Packet() {
        id = System.nanoTime();
    }

    public String getType() {
        return type;
    }

    protected void setType(String type) {
        this.type = type;
    }

    public long getId() {
        return id;
    }

    public boolean waitReply() {
        return false;
    }

    @Override
    public String toString() {
        return type + " " + id;
    }

    public byte[] getByte() { return this.toString().getBytes(); }

    public int getLength() {
        return this.toString().length();
    }
}
