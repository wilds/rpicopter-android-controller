package org.wilds.quadcontroller.app;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import android.widget.VideoView;

import org.wilds.quadcontroller.app.communication.OnReceiveListener;
import org.wilds.quadcontroller.app.communication.Protocol;
import org.wilds.quadcontroller.app.communication.UDPProtocol;
import org.wilds.quadcontroller.app.communication.packet.MotionPacket;
import org.wilds.quadcontroller.app.communication.packet.Packet;
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

    protected int throttle = 6;
    protected int yaw = 0;
    protected int roll = 0;
    protected int pitch = 0;

    protected long lastSend = 0;

    protected Handler mHandler = new Handler();

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

        VideoView v = (VideoView) findViewById(R.id.surface_view);
        // TEST!!!
        /*
        v.setVideoPath("http://daily3gp.com/vids/747.3gp");
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

        /*
        Button connect = (Button) findViewById(R.id.buttonConnect);
        connect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                protocol.searchForQuadcopter();
            }
        });


        Button test0 = (Button) findViewById(R.id.buttonTestMotor0);
        test0.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                protocol.sendPacket(new TestMotorPacket(2500, 0, 0, 0));
            }
        });
        */

        protocol.setOnReceiveListener(new OnReceiveListener() {
            @Override
            public void onReceive(String type, Object message) {
                if (type == Packet.TYPE_HELLO && message instanceof InetAddress) {
                    // TODO show list and choose
                    InetAddress quadcopter = (InetAddress) message;
                    Log.d("QuadController", quadcopter.getHostAddress());
                    protocol.connectToQuadcopter(quadcopter.getHostAddress());
                }
                else if (type == Packet.TYPE_QUERY_STATUS) {
                    String values = (String) message;
                    final String data[] = values.split(" ");
                    if (!data[0].equals("status")) {
                        System.err.printf("Invalid response from quadcopter: %s", data[0]);
                    } else {
                        mHandler.post(new Runnable() {
                            @Override
                            public void run() {
                                overlayView.setData(throttle, Float.parseFloat(data[1]), Float.parseFloat(data[2]), Float.parseFloat(data[3]), Integer.parseInt(data[5]), Integer.parseInt(data[4]));
                            }
                        });
                    }
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
    }

    private JoystickMovedListener _listenerLeft = new JoystickMovedListener() {

        @Override
        public void OnMoved(int pan, int tilt) {
            throttle = tilt + 50;
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
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()) {
            case R.id.action_connect:
                protocol.searchForQuadcopter();
                return true;
            case R.id.action_settings:
                Intent intent = new Intent(this, SettingsActivity.class);
                startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    protected void sendMotionPacket() {
        mHandler.removeCallbacks(mHandlerTask);
        if (System.currentTimeMillis() - lastSend > 80) {
            protocol.sendPacket(new MotionPacket(throttle, yaw, pitch, roll));
            lastSend = System.currentTimeMillis();
        } else {
            mHandler.postDelayed(mHandlerTask, 80);
        }
    }

    Runnable mHandlerTask = new Runnable()
    {
        @Override
        public void run() {
            sendMotionPacket();
        }
    };

}
