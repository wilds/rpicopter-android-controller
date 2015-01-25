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
public class MotionPacket extends Packet {

    protected int thr;
    protected int yaw;
    protected int pitch;
    protected int roll;

    public MotionPacket() {
        this.setType(TYPE_MOTION);
    }

    public MotionPacket(int throttle, int yaw, int pitch, int roll) {
        super();
        this.setType(TYPE_MOTION);
        setThrottle(throttle);
        setYaw(yaw);
        setPitch(pitch);
        setRoll(roll);
    }

    public int getThrottle() {
        return thr;
    }

    protected void setThrottle(int throttle) {
        this.thr = throttle;
    }


    public int getYaw() {
        return yaw;
    }

    protected void setYaw(int yaw) {
        this.yaw = yaw;
    }

    public int getPitch() {
        return pitch;
    }

    public void setPitch(int pitch) {
        this.pitch = pitch;
    }

    public int getRoll() {
        return roll;
    }

    public void setRoll(int roll) {
        this.roll = roll;
    }

    @Override
    public String toString() {
        return super.toString() + " " + thr + " " + yaw + " " + pitch + " " + roll;
    }
}
