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

package org.wilds.quadcontroller.app;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import android.widget.VideoView;

import org.wilds.quadcontroller.app.communication.OnReceiveListener;
import org.wilds.quadcontroller.app.communication.Protocol;
import org.wilds.quadcontroller.app.communication.UDPProtocol;
import org.wilds.quadcontroller.app.communication.packet.AltitudeHolderEnablePacket;
import org.wilds.quadcontroller.app.communication.packet.AltitudeTargetPacket;
import org.wilds.quadcontroller.app.communication.packet.FlyModePacket;
import org.wilds.quadcontroller.app.communication.packet.MotionPacket;
import org.wilds.quadcontroller.app.communication.packet.Packet;
import org.wilds.quadcontroller.app.communication.packet.TakePicturePacket;
import org.wilds.quadcontroller.app.communication.packet.VideoPacket;
import org.wilds.quadcontroller.app.joystick.DualJoystickView;
import org.wilds.quadcontroller.app.joystick.JoystickMovedListener;

import java.net.InetAddress;

/**
 * Created by Wilds on 13/04/2014.
 */
public class QuadControllerActivity extends Activity implements SharedPreferences.OnSharedPreferenceChangeListener {

    protected static Protocol protocol;
    protected int udpPort = 1032;

    protected DualJoystickView joystick;
    protected OverlayView overlayView;

    protected int throttle = 0;
    protected int yaw = 0;
    protected int roll = 0;
    protected int pitch = 0;

    protected boolean altitudeholderEnabled = false;
    protected VideoPacket.Command recordVideoStatus = VideoPacket.Command.stop;

    protected int flymode = 0;

    protected Menu menu;

    protected long lastSend = 0;
    protected final static int MIN_MOTION_PACKET_TIME = 80;

    protected Handler mHandler = new Handler();

    protected boolean debugHUD = false;
    protected int debugAltTarget = 0;

    protected LinkQuality linkQuality;
    protected boolean linkQualitySignalEnabled;
    protected boolean linkQualitySpeedEnabled;
    protected boolean linkQualityLagEnabled;
    protected boolean linkQualityRunning = false;
    protected Runnable updateLinkQuality = new Runnable() {
        @Override
        public void run() {
            linkQuality.update();
            overlayView.setWiFiData(linkQualitySignalEnabled ? linkQuality.getSignalLevel(6) : -1, linkQualitySpeedEnabled ? linkQuality.getLinkSpeed() : -1, linkQualityLagEnabled ? protocol.getLatency() : -1, true);
            //Toast.makeText(QuadControllerActivity.this, linkQualitySignalEnabled + "  " +linkQuality.getSignalLevel(6) + " " +linkQuality.getSignal() + " " + linkQuality.getLinkSpeed(), Toast.LENGTH_LONG).show();
            mHandler.postDelayed(updateLinkQuality, 5000);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dualjoystick);

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        sharedPreferences.registerOnSharedPreferenceChangeListener(this);

        udpPort = Integer.parseInt(sharedPreferences.getString("udp_port", "1032"));
        if (protocol == null) {
            protocol = new UDPProtocol(udpPort /*, 58100*/);
            Toast.makeText(this.getApplicationContext(), R.string.info_connect, Toast.LENGTH_LONG).show();
        }
        debugHUD = sharedPreferences.getBoolean("debug_hud", false);

        VideoView v = (VideoView) findViewById(R.id.surface_view);
        // TEST!!!
        /*
        v.setVideoPath("rtsp://192.168.11.110:8554/test.sdp");
        //v.setVideoPath("https://r2---sn-4g57kuel.googlevideo.com/videoplayback?ratebypass=yes&ip=93.55.50.131&requiressl=yes&fexp=3300103%2C3300103%2C3300133%2C3300133%2C3300137%2C3300137%2C3300164%2C3300164%2C3310366%2C3310366%2C3310704%2C3310704%2C900225%2C900718%2C912141%2C916645%2C927622%2C932404%2C9405766%2C9405883%2C941004%2C943917%2C947209%2C947218%2C948124%2C948532%2C952302%2C952605%2C952901%2C954807%2C955301%2C957103%2C957105%2C957201%2C959701&id=o-AH5n8wbfF9eElQzfJYaHSsArRwZI1jfDyvwFD_Y17yVF&mime=video%2Fmp4&expire=1419838507&ipbits=0&key=cms1&itag=22&signature=1E52F3763A5AFF497AD7D4EB032C6A3EA090C173.1C38498DA5EE2C09F7432E21143BA22780196074&upn=w0mkMLADuB0&dur=226.139&sver=3&source=youtube&sparams=dur,expire,id,initcwndbps,ip,ipbits,itag,mime,mm,ms,mv,ratebypass,requiressl,source,upn&title=Quadcopter%20Airborne%20Video%20Test.mp4&cpn=9m9s6o5_RSLG2XHx&redirect_counter=1&req_id=6a328ee249dca3ee&cms_redirect=yes&mm=26&ms=tsu&mt=1419816911&mv=m");
        v.start();
        v.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mediaPlayer) {
                mediaPlayer.seekTo(1);
                mediaPlayer.start();
            }
        });
        */
        // END TEST

        overlayView = (OverlayView) findViewById(R.id.overlay_view);

        joystick = (DualJoystickView) findViewById(R.id.dualjoystickView);

        joystick.setOnJostickMovedListener(_listenerLeft, _listenerRight);
        joystick.getLeftStick().setDisableAutoReturnToCenterY(true);
        joystick.getLeftStick().setYAxisInverted(false);
        joystick.setMovementRange(100, 100);

        linkQuality = new LinkQuality(this);
        linkQualitySignalEnabled = sharedPreferences.getBoolean("wifi_signal_enabled", true);
        linkQualitySpeedEnabled = sharedPreferences.getBoolean("wifi_speed_enabled", false);
        linkQualityLagEnabled = sharedPreferences.getBoolean("wifi_latency_enabled", false);
        if (linkQualitySignalEnabled || linkQualityLagEnabled) {
            startLinkQuality();
        }

        protocol.setOnReceiveListener(new OnReceiveListener() {
            @Override
            public void onReceive(String type, Object message) {
                if (type == Packet.TYPE_HELLO && message instanceof InetAddress) {
                    // TODO show list and choose
                    InetAddress quadcopter = (InetAddress) message;
                    Log.d("QuadController", quadcopter.getHostAddress());
                    protocol.connectToQuadcopter(quadcopter.getHostAddress());
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(QuadControllerActivity.this, R.string.connected, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
                else if (type == Packet.TYPE_QUERY_STATUS) {
                    String values = (String) message;
                    final String data[] = values.split(" ");
                    if (!data[0].equals("status")) {
                        System.err.printf("Invalid response from quadcopter: %s", data[0]);
                    } else {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                // TODO send record time is secs shifted whit record status
                                int recordVideoData = Integer.parseInt(data[7]);
                                recordVideoStatus = recordVideoData == 1 ? VideoPacket.Command.record : recordVideoData == 2 ? VideoPacket.Command.pause :  VideoPacket.Command.stop;
                                overlayView.setData(throttle, Float.parseFloat(data[2]), Float.parseFloat(data[3]), Float.parseFloat(data[4]), Integer.parseInt(data[6]), Integer.parseInt(data[5]), recordVideoData);
                                updateMenu();
                            }
                        });
                    }
                } else if (type == Packet.TYPE_TAKE_PICTURE) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(QuadControllerActivity.this, R.string.picture_saved, Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });

        /*
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        PowerManager.WakeLock wl = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "My Tag");
        wl.acquire();
        ..screen will stay on during this section..
        wl.release();
        */
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        if (udpPort != Integer.parseInt(sharedPreferences.getString("udp_port", "1032")))
            Toast.makeText(this, R.string.need_restart, Toast.LENGTH_LONG).show();
        debugHUD = sharedPreferences.getBoolean("debug_hud", false);

        linkQualitySignalEnabled = sharedPreferences.getBoolean("wifi_signal_enabled", true);
        linkQualitySpeedEnabled = sharedPreferences.getBoolean("wifi_speed_enabled", false);
        linkQualityLagEnabled = sharedPreferences.getBoolean("wifi_latency_enabled", false);
        if (linkQualitySignalEnabled || linkQualityLagEnabled) {
            startLinkQuality();
        }
        overlayView.setWiFiData(linkQualitySignalEnabled ? linkQuality.getSignalLevel(6) : -1, linkQualitySpeedEnabled ? linkQuality.getLinkSpeed() : -1, linkQualityLagEnabled ? protocol.getLatency() : -1, true);

    }

    private JoystickMovedListener _listenerLeft = new JoystickMovedListener() {

        @Override
        public void OnMoved(int pan, int tilt) {
            throttle = tilt / 2 + 50;
            yaw = pan;
            sendMotionPacket();
        }

        @Override
        public void OnReleased() {

        }

        public void OnReturnedToCenter() {
            yaw = 0;
            sendMotionPacket();
        }
    };

    private JoystickMovedListener _listenerRight = new JoystickMovedListener() {

        @Override
        public void OnMoved(int pan, int tilt) {
            pitch = tilt;
            roll = pan;
            sendMotionPacket();
        }

        @Override
        public void OnReleased() {

        }

        public void OnReturnedToCenter() {
            pitch = 0;
            roll = 0;
            sendMotionPacket();
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        //protocol.close();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.dual_joystick_view, menu);
        this.menu = menu;
        updateMenu();
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_connect:
                protocol.searchForQuadcopter();
                return true;

            case R.id.action_flymode:
                flymode = 1 - flymode;
                protocol.sendPacket(new FlyModePacket(flymode == 0 ? FlyModePacket.FlyMode.stable : FlyModePacket.FlyMode.acro));
                item.setTitle(flymode == 0 ? R.string.action_action_flymode_stable : R.string.action_action_flymode_acro);
                return true;

            case R.id.action_altitude_holder:
                altitudeholderEnabled = !altitudeholderEnabled;
                protocol.sendPacket(new AltitudeHolderEnablePacket(altitudeholderEnabled));
                item.setTitle(altitudeholderEnabled ? R.string.action_altitude_holder_disable : R.string.action_altitude_holder_enable);
                if (debugHUD)
                    debugAltTarget = throttle * 10;
                return true;

            case R.id.action_take_picture:
                protocol.sendPacket(new TakePicturePacket());
                return true;

            case R.id.action_snap_video_record:
                if (recordVideoStatus == VideoPacket.Command.stop || recordVideoStatus == VideoPacket.Command.pause) {
                    protocol.sendPacket(new VideoPacket(VideoPacket.Command.record));
                    if (debugHUD) {
                        recordVideoStatus = VideoPacket.Command.record;
                        debugUpdateHUD();
                        updateMenu();
                    }
                }
                return true;
            case R.id.action_snap_video_pause:
                if (recordVideoStatus == VideoPacket.Command.record) {
                    protocol.sendPacket(new VideoPacket(VideoPacket.Command.pause));
                    if (debugHUD) {
                        recordVideoStatus = VideoPacket.Command.pause;
                        debugUpdateHUD();
                        updateMenu();
                    }
                }
                return true;

            case R.id.action_snap_video_stop:
                if (recordVideoStatus == VideoPacket.Command.record || recordVideoStatus == VideoPacket.Command.pause) {
                    protocol.sendPacket(new VideoPacket(VideoPacket.Command.stop));
                    if (debugHUD) {
                        recordVideoStatus = VideoPacket.Command.stop;
                        debugUpdateHUD();
                        updateMenu();
                    }
                }
                return true;

            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void sendMotionPacket() {
        mHandler.removeCallbacks(mHandlerTask);
        if (System.currentTimeMillis() - lastSend > MIN_MOTION_PACKET_TIME) {
            protocol.sendPacket(new MotionPacket(throttle, yaw, pitch, roll));
            lastSend = System.currentTimeMillis();
            if (debugHUD)
                debugUpdateHUD();
        } else {
            mHandler.postDelayed(mHandlerTask, MIN_MOTION_PACKET_TIME);
        }
    }

    Runnable mHandlerTask = new Runnable()
    {
        @Override
        public void run() {
            sendMotionPacket();
        }
    };

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event)
    {
        if (altitudeholderEnabled) {
            if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
                protocol.sendPacket(new AltitudeTargetPacket(true, -10));   //10cm
                if (debugHUD && debugAltTarget >= 10) {
                    debugAltTarget -= 10;
                    debugUpdateHUD();
                }
                return true;
            } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
                protocol.sendPacket(new AltitudeTargetPacket(true, 10));
                if (debugHUD) {
                    debugAltTarget += 10;
                    debugUpdateHUD();
                }
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    protected void updateMenu() {
        switch (recordVideoStatus) {
            case stop:
                menu.findItem(R.id.action_take_picture).setVisible(true);
                menu.findItem(R.id.action_snap_video_record).setVisible(true);
                menu.findItem(R.id.action_snap_video_stop).setVisible(false);
                menu.findItem(R.id.action_snap_video_pause).setVisible(false);
                break;
            case pause:
                menu.findItem(R.id.action_take_picture).setVisible(false);
                menu.findItem(R.id.action_snap_video_record).setVisible(true);
                menu.findItem(R.id.action_snap_video_stop).setVisible(true);
                menu.findItem(R.id.action_snap_video_pause).setVisible(false);
                break;
            case record:
                menu.findItem(R.id.action_take_picture).setVisible(false);
                menu.findItem(R.id.action_snap_video_record).setVisible(false);
                menu.findItem(R.id.action_snap_video_stop).setVisible(true);
                menu.findItem(R.id.action_snap_video_pause).setVisible(true);
                break;
        }
    }

    protected void debugUpdateHUD() {
        overlayView.setData(throttle, yaw, pitch, roll, throttle * 10 + (int)(Math.random() * 10), debugAltTarget, recordVideoStatus.ordinal());
    }

    protected void startLinkQuality() {
        if (!linkQualityRunning) {
            mHandler.post(updateLinkQuality);
            linkQualityRunning = true;
        }
    }

    protected void stopLinkQuality() {
        if (linkQualityRunning) {
            mHandler.removeCallbacks(updateLinkQuality);
            linkQualityRunning = false;
        }
    }
}
