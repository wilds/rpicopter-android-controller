/*
 * Copyright (C) 2014-2015 Danilo & Ivano Selvaggi
 *
 * This program is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License as published by the
 * Free Software Foundation; either version 3 of the License, or (at your
 * option) any later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
 * more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package org.wilds.quadcontroller.app.communication.packet;

/**
 * Created by Wilds on 01/02/2015.
 */
public class StreamCameraPacket extends Packet {

    public enum Command {
        start,
        stop
    }

    protected Command command;
    protected int port;
    protected int width;
    protected int height;

    public StreamCameraPacket(Command command, int port, int width, int height) {
        super();
        this.setType(TYPE_STREAM_CAMERA);
        setCommand(command);
        setPort(port);
        setWidth(width);
        setHeight(height);
    }

    public Command getCommand() {
        return command;
    }

    protected void setCommand(Command command) {
        this.command = command;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    @Override
    public boolean waitReply() {
        return true;
    }

    @Override
    public String toString() {
        return super.toString() + " " + command.name() + " " + getPort() + " " + getWidth() + " " + getHeight();
    }
}
