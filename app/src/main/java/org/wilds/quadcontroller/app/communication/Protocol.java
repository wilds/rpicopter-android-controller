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

package org.wilds.quadcontroller.app.communication;

import org.wilds.quadcontroller.app.communication.packet.Packet;

/**
 * Created by Wilds on 14/04/2014.
 */
public interface Protocol {
    public boolean isConnected();
    public boolean searchForQuadcopter();  // send broadcast udp packet
    public boolean connectToQuadcopter(String id);   // connect to quadcopter
    public boolean sendPacket(Packet packet);
    public void setOnReceiveListener(OnReceiveListener listener);
    public void close();
    public void startQueryStatus();
    public void stopQueryStatus();
    public void startHeartBeat();
    public void stopHeartBeat();
}
