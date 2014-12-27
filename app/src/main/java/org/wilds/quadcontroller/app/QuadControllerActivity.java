package org.wilds.quadcontroller.app;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.VideoView;

import org.wilds.quadcontroller.app.communication.OnReceiveListener;
import org.wilds.quadcontroller.app.communication.Protocol;
import org.wilds.quadcontroller.app.communication.UDPProtocol;
import org.wilds.quadcontroller.app.communication.packet.MotionPacket;
import org.wilds.quadcontroller.app.communication.packet.Packet;
import org.wilds.quadcontroller.app.communication.packet.TestMotorPacket;
import org.wilds.quadcontroller.app.joystick.DualJoystickView;
import org.wilds.quadcontroller.app.joystick.JoystickMovedListener;

import java.net.InetAddress;

/**
 * Created by Wilds on 13/04/2014.
 */
public class QuadControllerActivity extends Activity {

    protected static Protocol protocol = new UDPProtocol(1032 /*, 58100*/);

    protected DualJoystickView joystick;
    protected OverlayView overlayView;

    protected int throttle = 6;
    protected int yaw = 0;
    protected int roll = 0;
    protected int pitch = 0;

    protected long lastSend = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dualjoystick);

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

        protocol.setOnReceiveListener(new OnReceiveListener() {
            @Override
            public void onReceive(String type, Object message) {
                if (type == Packet.TYPE_HELLO && message instanceof InetAddress) {
                    // TODO show list and choose
                    InetAddress quadcopter = (InetAddress) message;
                    Log.d("QuadController", quadcopter.getHostAddress());
                    protocol.connectToQuadcopter(quadcopter.getHostAddress());
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

        MenuItem Item = menu.add("PID");
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getTitle() == "PID") {
            Intent intent = new Intent(this, PIDActivity.class);
            startActivity(intent);
        }
        return true;
    }

    protected void sendMotionPacket() {
        mHandler.removeCallbacks(mHandlerTask);
        if (System.currentTimeMillis() - lastSend > 80) {
            protocol.sendPacket(new MotionPacket(throttle, yaw, pitch, roll));
            lastSend = System.currentTimeMillis();
        } else {
            mHandler.postDelayed(mHandlerTask, 80);
        }
        // TEST
        overlayView.setData(throttle, yaw, roll, pitch, throttle*10 + (int) (Math.random()*10));
    }

    Handler mHandler = new Handler();

    Runnable mHandlerTask = new Runnable()
    {
        @Override
        public void run() {
            sendMotionPacket();
        }
    };

}
