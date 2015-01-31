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

package org.wilds.gstreamer;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;

import org.freedesktop.gstreamer.GStreamer;

public class GStreamerSurfaceView extends SurfaceView implements SurfaceHolder.Callback {
    private native void nativeInit();                   // Initialize native code, build pipeline, etc
    private native void nativeSetPipeline(String pipeline);
    private native void nativeFinalize();               // Destroy pipeline and shutdown native code
    private native void nativeStart();                  // Constructs PIPELINE
    private native void nativeStop();                   // Destroys PIPELINE
    private native void nativePlay();                   // Set pipeline to PLAYING
    private native void nativePause();                  // Set pipeline to PAUSED
    private native void nativeSurfaceInit(Object surface);
    private native void nativeSurfaceFinalize();
    private native static boolean nativeClassInit();    // Initialize native class: cache Method IDs for callbacks
    private long native_custom_data;                    // Native code will use this to keep private data

    private boolean pipeline_started = false;

    public int media_width = 320;
    public int media_height = 240;

    // Called from native code.
    private void setError(final int type, final String _message) {
        if (type == 1) {
            pipeline_started = false;
            stopPlayback();
        }
        Log.e("GStreamerSurfaceView", _message);
        Toast.makeText(this.getContext(), _message, Toast.LENGTH_LONG).show();  // TODO fire event and run in ui thread
    }

    // Called from native code.
    private void setMessage(final String _message) {
        Log.e("GStreamerSurfaceView", _message);
        Toast.makeText(this.getContext(), _message, Toast.LENGTH_LONG).show();  // TODO fire event and run in ui thread
    }

    // Called from native code.
    private void notifyState(final int _state) {
        Log.d("GStreamerSurfaceView", "STATE " + _state);
        switch (_state) {
            case 0: // PENDING
                pipeline_started = false;
                break;
            case 1: // NULL
                break;
            case 2: // READY
                break;
            case 3: // PAUSE
                pipeline_started = false;
                break;
            case 4: // PLAYING
                pipeline_started = true;
                break;
        }
    }

    // Called from native code. Native code calls this once it has created its pipeline and
    // the main loop is running, so it is ready to accept commands.
    private void onGStreamerInitialized() {
        Log.i("GStreamerSurfaceView", "onGStreamerInitialized");
        nativePlay();
    }

    static {
        System.loadLibrary("gstreamer_android");
        System.loadLibrary("gstreamer_surfaceview");
        nativeClassInit();
    }

    public GStreamerSurfaceView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.getHolder().addCallback(this);
    }

    public GStreamerSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.getHolder().addCallback(this);
    }

    public GStreamerSurfaceView(Context context) {
        super(context);
        this.getHolder().addCallback(this);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        try {
            GStreamer.init(this.getContext());
        } catch (Exception e) {
            Toast.makeText(this.getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
        }
        nativeInit();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        nativeFinalize();
    }

    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d("GStreamerSurfaceView", "Surface changed to format " + format + " width " + width + " height " + height);
        nativeSurfaceInit(holder.getSurface());
    }

    public void surfaceCreated(SurfaceHolder holder) {
        Log.d("GStreamerSurfaceView", "Surface created: " + holder.getSurface());
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d("GStreamerSurfaceView", "Surface destroyed");
        nativeSurfaceFinalize();
    }

    // Called by the layout manager to find out our size and give us some rules.
    // We will try to maximize our size, and preserve the media's aspect ratio if
    // we are given the freedom to do so.
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = 0, height = 0;
        int wmode = MeasureSpec.getMode(widthMeasureSpec);
        int hmode = MeasureSpec.getMode(heightMeasureSpec);
        int wsize = MeasureSpec.getSize(widthMeasureSpec);
        int hsize = MeasureSpec.getSize(heightMeasureSpec);

        Log.i("GStreamerSurfaceView", "onMeasure called with " + media_width + "x" + media_height);
        // Obey width rules
        switch (wmode) {
            case MeasureSpec.AT_MOST:
                if (hmode == MeasureSpec.EXACTLY) {
                    width = Math.min(hsize * media_width / media_height, wsize);
                    break;
                }
            case MeasureSpec.EXACTLY:
                width = wsize;
                break;
            case MeasureSpec.UNSPECIFIED:
                width = media_width;
        }

        // Obey height rules
        switch (hmode) {
            case MeasureSpec.AT_MOST:
                if (wmode == MeasureSpec.EXACTLY) {
                    height = Math.min(wsize * media_height / media_width, hsize);
                    break;
                }
            case MeasureSpec.EXACTLY:
                height = hsize;
                break;
            case MeasureSpec.UNSPECIFIED:
                height = media_height;
        }

        // Finally, calculate best size when both axis are free
        if (hmode == MeasureSpec.AT_MOST && wmode == MeasureSpec.AT_MOST) {
            int correct_height = width * media_height / media_width;
            int correct_width = height * media_width / media_height;

            if (correct_height < height)
                height = correct_height;
            else
                width = correct_width;
        }

        // Obey minimum size
        width = Math.max(getSuggestedMinimumWidth(), width);
        height = Math.max(getSuggestedMinimumHeight(), height);
        setMeasuredDimension(width, height);
    }

    public void setVideoPath(String path) {
        String[] uri = path.split(":");
        String pipeline;
        //if (stream_type==1)
        //    pipeline = "udpsrc address=" + uri[0] + " port=" + uri[1] + " caps=\"application/x-rtp, media=(string)video, clock-rate=(int)90000, encoding-name=(string)H264\" ! rtph264depay  ! avdec_h264 ! tee name=t ! queue ! videomixer name=m sink_0::xpos=0 sink_1::xpos=640 ! videoconvert ! autovideosink sync=false t. ! queue ! m.";
        //else
        pipeline = "udpsrc port=" + uri[1] + " caps=\"application/x-rtp, media=(string)video, clock-rate=(int)90000, encoding-name=(string)H264\" ! rtph264depay  ! avdec_h264 ! videoconvert ! autovideosink sync=false";
        //pipeline = "videotestsrc ! warptv ! autovideosink";
        Log.d("GStreamerSurfaceView", pipeline);
        nativeSetPipeline(pipeline);
    }

    public void start() {
        if (!pipeline_started)
            nativeStart();
    }

    public void pause() {
        if (pipeline_started)
            nativePause();
    }

    public void stopPlayback() {
        if (pipeline_started)
            nativeStop();
    }
}
