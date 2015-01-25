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
public class HelloPacket extends Packet {

    protected String from;

    public HelloPacket(String from) {
        super();
        this.setType(TYPE_HELLO);
        setFrom(from);
    }

    public String getFrom() {
        return from;
    }

    protected void setFrom(String from) {
        this.from = from;
    }

    @Override
    public boolean waitReply() {
        return true;
    }

    @Override
    public String toString() {
        return super.toString() + " " + from;
    }
}
