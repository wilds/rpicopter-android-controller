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
