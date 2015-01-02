package org.wilds.quadcontroller.app.communication.packet;

/**
 * Created by Wilds on 28/12/2014.
 */
public class AltitudeTargetPacket extends Packet {

    protected int value;
    protected boolean mod = false;

    public AltitudeTargetPacket(int value) {
        super();
        this.setType(TYPE_ALTITUDE_TARGET);
        setValue(value);
    }

    public AltitudeTargetPacket(boolean mod, int value) {
        super();
        this.setType(TYPE_ALTITUDE_TARGET);
        this.mod = mod;
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
        if (!mod && value < 0)
            return super.toString() + " 0";
        return super.toString() + " " + (mod && value >=0 ? "+" : "") + value;
    }
}
