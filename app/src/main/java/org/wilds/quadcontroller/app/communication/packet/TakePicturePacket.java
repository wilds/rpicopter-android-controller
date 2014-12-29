package org.wilds.quadcontroller.app.communication.packet;

/**
 * Created by Wilds on 28/12/2014.
 */
public class TakePicturePacket extends Packet {

    public TakePicturePacket(String from) {
        super();
        this.setType(TYPE_TAKE_PICTURE);
    }

    @Override
    public boolean waitReply() {
        return true;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
