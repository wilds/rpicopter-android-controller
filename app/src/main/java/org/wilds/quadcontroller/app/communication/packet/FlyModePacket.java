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
public class FlyModePacket extends Packet {

    public enum FlyMode {
        stable,
        acro
    }

    protected FlyMode flymode;

    public FlyModePacket(FlyMode flymode) {
        super();
        this.setType(TYPE_FLYMODE);
        setFlyMode(flymode);
    }

    public FlyMode getFlyMode() {
        return flymode;
    }

    protected void setFlyMode(FlyMode flymode) {
        this.flymode = flymode;
    }

    @Override
    public boolean waitReply() {
        return true;
    }

    @Override
    public String toString() {
        return super.toString() + " " + flymode.name();
    }
}
