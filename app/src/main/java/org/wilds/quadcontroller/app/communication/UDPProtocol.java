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

import android.os.Handler;
import android.util.Log;

import org.wilds.quadcontroller.app.communication.packet.HeartBeatPacket;
import org.wilds.quadcontroller.app.communication.packet.HelloPacket;
import org.wilds.quadcontroller.app.communication.packet.Packet;
import org.wilds.quadcontroller.app.communication.packet.QueryStatusPacket;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by Wilds on 14/04/2014.
 */
public class UDPProtocol implements Protocol {

    protected Handler mHandler = new Handler();
    private final static int INTERVAL_HEARTBEAT = 1000 * 2;
    private final static int INTERVAL_QUERY_STATUS = 200;
    //
    protected DatagramSocket sendSocket;
    protected int sendPort = 58000;
    protected InetAddress sendTo;

    protected int lag;

    private final BlockingQueue<Runnable> mQueue = new LinkedBlockingQueue<Runnable>();
    protected ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(5, 10, 1, TimeUnit.SECONDS, mQueue);

    protected Map<Long, Packet> waitingForResponse = new ConcurrentHashMap<>();

    protected Runnable mHandlerTaskHeartBeat = new Runnable() {
        @Override
        public void run() {
            sendPacket(new HeartBeatPacket());
        }
    };

    protected Runnable mHandlerTaskQueryStatus = new Runnable() {
        @Override
        public void run() {
            sendPacket(new QueryStatusPacket());
            mHandler.postDelayed(mHandlerTaskQueryStatus, INTERVAL_QUERY_STATUS);
        }
    };

    protected OnReceiveListener onReceiveListener;

    public UDPProtocol(int sendPort /*, int listenPort*/) {
        this.sendPort = sendPort;
        try {
            sendSocket = new DatagramSocket(sendPort);
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setOnReceiveListener(OnReceiveListener listener) {
        this.onReceiveListener = listener;
    }

    protected void startListening() {
        Thread listenThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (isConnected()) {
                    byte[] message = new byte[1024];
                    DatagramPacket p = new DatagramPacket(message, message.length);
                    try {
                        sendSocket.receive(p);

                        if (isFakePacket(p)) {
                            Log.d("LOGGER", "STOP listening");
                            return;
                        }

                        String text = new String(message, 0, p.getLength());
                        String par[] = text.split(" ");

                        if (par.length < 2) {
                            Log.e("UDPProtocol", "invalid response " + text);
                            continue;
                        }

                        Packet packet = waitingForResponse.remove(Long.parseLong(par[1]));

                        if (packet == null) {
                            Log.e("UDPProtocol", "not waiting for response");
                            continue;
                        }

                        lag = (int) ((System.nanoTime() - packet.getId()) / 1000000);

                        //TODO handle quadcopter response
                        if (onReceiveListener != null)
                            onReceiveListener.onReceive(packet.getType(), text);
                    } catch (IOException ex) {

                    }
                }
            }
        });
        listenThread.start();
    }

    @Override
    public boolean sendPacket(final Packet packet) {
        if (!isConnected())
            return false;

        poolExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {

                    if (packet.waitReply()) {
                        waitingForResponse.put(packet.getId(), packet);
                    }

                    stopHeartBeat();
                    sendSocket.send(new DatagramPacket(packet.getByte(), packet.getLength(), sendTo, sendPort));
                    startHeartBeat();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        return true;
    }


    @Override
    public void startHeartBeat() {
        //mHandlerTask.run();
        mHandler.postDelayed(mHandlerTaskHeartBeat, INTERVAL_HEARTBEAT);
    }

    @Override
    public void stopHeartBeat() {
        mHandler.removeCallbacks(mHandlerTaskHeartBeat);
    }


    @Override
    public void startQueryStatus() {
        mHandlerTaskQueryStatus.run();
    }

    @Override
    public void stopQueryStatus() {
        mHandler.removeCallbacks(mHandlerTaskQueryStatus);
    }

    @Override
    public boolean searchForQuadcopter() {
        if (isConnected())
            close();
        poolExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Packet packet = new HelloPacket(InetAddress.getLocalHost().getHostAddress());
                    DatagramPacket broadcast = new DatagramPacket(packet.getByte(), packet.getLength(), InetAddress.getByName(Utils.getBroadcast()), sendPort);
                    sendSocket.setBroadcast(true);
                    sendSocket.send(broadcast);

                    long t = System.currentTimeMillis();
                    while (System.currentTimeMillis() - t < 5000) {
                        byte[] message = new byte[1024];
                        DatagramPacket p = new DatagramPacket(message, message.length);

                        sendSocket.receive(p);
                        String text = new String(message, 0, p.getLength());
                        Log.d("LOGGER", "RECEIVED: " + text);
                        // TODO
                        if (text.contains("OK")) {
                            if (onReceiveListener != null)
                                onReceiveListener.onReceive(packet.getType(), p.getAddress());
                        } else if (text.contains("hello")) {
                        } else if (isFakePacket(p)) {
                            Log.d("LOGGER", "STOP");
                            onConnected();
                            return;
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        return true;
    }

    @Override
    public boolean connectToQuadcopter(String id) {
        try {
            sendFakePacket();
            this.sendTo = InetAddress.getByName(id);
            return true;
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return false;
        }
    }

    protected void onConnected() {
        startHeartBeat();
        startQueryStatus();
        startListening();
    }

    public void close() {
        sendTo = null;
        sendFakePacket();   //interrupt receive
        stopHeartBeat();
        stopQueryStatus();
        //sendSocket.close();
    }

    @Override
    public boolean isConnected() {
        return sendTo != null;
    }

    /* this method is used to send fake packet to self and interrupt receive;*/
    private void sendFakePacket() {
        poolExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    sendSocket.send(new DatagramPacket("".getBytes(), 0, InetAddress.getLocalHost(), sendPort));
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        });
    }

    private boolean isFakePacket(DatagramPacket p) {
        try {
            return p.getLength() == 0 && p.getAddress().equals(InetAddress.getLocalHost());
        } catch (UnknownHostException e) {
            return false;
        }
    }

    public int getLatency() {
        return lag;
    }
}
