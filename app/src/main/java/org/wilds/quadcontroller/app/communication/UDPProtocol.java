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
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Created by Wilds on 14/04/2014.
 */
public class UDPProtocol implements Protocol {

    protected Handler mHandler = new Handler();
    private final static int INTERVAL_HEARTBEAT = 1000 * 2;
    private final static int INTERVAL_QUERY_STATUS = 100;
    //
    protected DatagramSocket sendSocket;
    protected int sendPort = 58000;
    protected InetAddress sendTo;

    private final BlockingQueue<Runnable> mQueue = new LinkedBlockingQueue<Runnable>();
    protected ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(5, 100,  1, TimeUnit.SECONDS, mQueue);

    // Listening vars
    /*
    protected Thread listenThread;
    protected int listenPort = 57001;
    protected boolean listening = true;
    */

    protected Runnable mHandlerTaskHeartBeat = new Runnable()
    {
        @Override
        public void run() {
            sendPacket(new HeartBeatPacket());
        }
    };

    protected Runnable mHandlerTaskQueryStatus = new Runnable()
    {
        @Override
        public void run() {
            sendPacket(new QueryStatusPacket());
            mHandler.postDelayed(mHandlerTaskQueryStatus, INTERVAL_QUERY_STATUS);
        }
    };

    protected OnReceiveListener onReceiveListener;

    public UDPProtocol(int sendPort /*, int listenPort*/) {
        this.sendPort = sendPort;
        //this.listenPort = listenPort;

        try {
            sendSocket = new DatagramSocket(sendPort);
            //sendSocket.setSoTimeout(5000);
        } catch (SocketException e) {
            e.printStackTrace();
        }

//        startListening();
    }

    @Override
    public void setOnReceiveListener(OnReceiveListener listener) {
        this.onReceiveListener = listener;
    }


    /*
    protected void startListening() {
        listenThread = new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    DatagramSocket sock = new DatagramSocket(listenPort);
                    while (listening) {
                        byte[] message = new byte[1500];
                        DatagramPacket p = new DatagramPacket(message, message.length);
                        sock.receive(p);
                        String text = new String(message, 0, p.getLength());

                        //TODO handle quadcopter response at hello packet

                        onReceiveListener.onReceive(text);
                    }
                    sock.close();

                } catch (SocketException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }

        });
        listenThread.start();
    }
*/

    @Override
    public boolean sendPacket(final Packet packet) { // TODO convertire in async task
        if (!isConnected())
            return false;
        poolExecutor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    stopHeartBeat();
                    sendSocket.send(new DatagramPacket(packet.getByte(), packet.getLength(), sendTo, sendPort));
                    startHeartBeat();

                    if (packet.waitReply()) {
                        byte[] message = new byte[1024];
                        DatagramPacket p = new DatagramPacket(message, message.length);
                        try {
                            sendSocket.setSoTimeout(1000);
                            sendSocket.receive(p);
                            String text = new String(message, 0, p.getLength());

                            //TODO handle quadcopter response
                            if (onReceiveListener != null)
                                onReceiveListener.onReceive(packet.getType(), text);
                        } catch (SocketTimeoutException ex) {
                            System.err.println("TimeOut");
                        }
                    }
                    //return true;
                } catch (IOException e) {
                    e.printStackTrace();
                    //return false;
                }
            }
        });

        return true;
    }

    @Override
    public void startHeartBeat()
    {
        //mHandlerTask.run();
        mHandler.postDelayed(mHandlerTaskHeartBeat, INTERVAL_HEARTBEAT);
    }

    @Override
    public void stopHeartBeat()
    {
        mHandler.removeCallbacks(mHandlerTaskHeartBeat);
    }




    @Override
    public void startQueryStatus()
    {
        mHandlerTaskQueryStatus.run();
    }

    @Override
    public void stopQueryStatus()
    {
        mHandler.removeCallbacks(mHandlerTaskQueryStatus);
    }

    @Override
    public boolean searchForQuadcopter() {  // TODO convertire in async task
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
                        try {
                            sendSocket.setSoTimeout(10000);
                            sendSocket.receive(p);
                            String text = new String(message, 0, p.getLength());
                            Log.d("LOGGER", "RECEIVED: " + text);
                            // TODO
                            if (text.contains("OK")) {
                                if (onReceiveListener != null)
                                    onReceiveListener.onReceive(packet.getType(), p.getAddress());
                            } else if (text.contains("hello")) {
                            } else if (text.isEmpty() && p.getAddress().equals(InetAddress.getLocalHost())) {
                                Log.d("LOGGER", "STOP");
                                return;
                            }
                        } catch (SocketTimeoutException ex) {

                        }
                    }
                    //return true;
                } catch (IOException e) {
                    e.printStackTrace();
                    //return false;
                }
            }
        });

        return true;
    }

    @Override
    public boolean connectToQuadcopter(String id) {
        try {
            try {
                sendSocket.send(new DatagramPacket("".getBytes(), 0, InetAddress.getLocalHost(), sendPort));
            } catch (Exception ex) {
                ex.printStackTrace();
            }
            this.sendTo = InetAddress.getByName(id);
            startHeartBeat();
            startQueryStatus();
            return true;
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return false;
        }
    }

    public void close() {
        //listening = false;
        stopHeartBeat();
        stopQueryStatus();
        sendSocket.close();
    }

    @Override
    public boolean isConnected() {
        return sendTo != null;
    }
}
