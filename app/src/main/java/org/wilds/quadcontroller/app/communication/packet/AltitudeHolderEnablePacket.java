package org.wilds.quadcontroller.app.communication.packet;

/**
 * Created by Wilds on 28/12/2014.
 */
public class AltitudeHolderEnablePacket extends Packet {

    protected boolean enabled = true;

    public AltitudeHolderEnablePacket(boolean enabled) {
        super();
        this.setType(TYPE_ALTITUDE_HOLDER_ENABLE);
        setEnabled(enabled);
    }

    public boolean getEnabled() {
        return enabled;
    }

    protected void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean waitReply() {
        return true;
    }

    @Override
    public String toString() {
        return super.toString() + " " + enabled;
    }
}
