package org.wilds.quadcontroller.app.communication.packet;

/**
 * Created by Wilds on 27/12/2014.
 */
public class QueryStatusPacket extends Packet {

    public QueryStatusPacket() {
        super();
        this.setType(TYPE_QUERY_STATUS);
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
