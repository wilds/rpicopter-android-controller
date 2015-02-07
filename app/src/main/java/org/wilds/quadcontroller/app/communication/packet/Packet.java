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
    public static final String TYPE_FLYMODE = "flymode";
    public static final String TYPE_STREAM_CAMERA = "stream";

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
