package org.wilds.quadcontroller.app.communication.packet;

/**
 * Created by Wilds on 28/12/2014.
 */
public class VideoPacket extends Packet {

    public enum Command {
        record,
        stop,
        pause
    }

    protected Command command;

    public VideoPacket(Command command) {
        super();
        this.setType(TYPE_VIDEO);
        setCommand(command);
    }

    public Command getCommand() {
        return command;
    }

    protected void setCommand(Command command) {
        this.command = command;
    }

    @Override
    public boolean waitReply() {
        return true;
    }

    @Override
    public String toString() {
        return super.toString() + " " + command.name();
    }
}
