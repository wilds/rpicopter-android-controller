package org.wilds.quadcontroller.app.communication.packet;

/**
 * Created by Wilds on 28/12/2014.
 */
public class AltitudeTargetPacket extends Packet {

    protected int value;

    public AltitudeTargetPacket(int value) {
        super();
        this.setType(TYPE_ALTITUDE_TARGET);
        setValue(value);
    }

    public int getValue() {
        return value;
    }

    protected void setValue(int value) {
        this.value = value;
    }

    @Override
    public boolean waitReply() {
        return true;
    }

    @Override
    public String toString() {
        return super.toString() + " " + value;
    }
}
