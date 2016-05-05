package com.pili.rnpili;

import android.hardware.Camera;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.facebook.react.bridge.LifecycleEventListener;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.pili.pldroid.streaming.CameraStreamingManager;
import com.pili.pldroid.streaming.CameraStreamingSetting;
import com.pili.pldroid.streaming.MicrophoneStreamingSetting;
import com.pili.pldroid.streaming.StreamStatusCallback;

import com.pili.pldroid.streaming.StreamingPreviewCallback;
import com.pili.pldroid.streaming.StreamingProfile;
import com.pili.pldroid.streaming.SurfaceTextureCallback;
import com.pili.pldroid.streaming.widget.AspectFrameLayout;
import com.qiniu.android.dns.DnsManager;
import com.qiniu.android.dns.IResolver;
import com.qiniu.android.dns.NetworkInfo;
import com.qiniu.android.dns.http.DnspodFree;
import com.qiniu.android.dns.local.AndroidDnsServer;
import com.qiniu.android.dns.local.Resolver;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;

import javax.annotation.Nullable;

/**
 * Created by buhe on 16/4/29.
 */
public class PiliStreamingViewManager extends SimpleViewManager<AspectFrameLayout>
        implements
        CameraPreviewFrameView.Listener,
        CameraStreamingManager.StreamingSessionListener,
        CameraStreamingManager.StreamingStateListener,
        View.OnLayoutChangeListener,
        StreamStatusCallback,
        StreamingPreviewCallback,
        SurfaceTextureCallback,
        LifecycleEventListener


{
    private static final String TAG = "StreamingBaseActivity";
    protected static final int MSG_START_STREAMING = 0;
    protected static final int MSG_STOP_STREAMING = 1;
    private static final int MSG_SET_ZOOM = 2;
    private static final int MSG_MUTE = 3;
    private static final int ZOOM_MINIMUM_WAIT_MILLIS = 33; //ms

    protected CameraStreamingManager mCameraStreamingManager;
    protected boolean mIsReady = false;

    private int mCurrentZoom = 0;
    private int mMaxZoom = 0;
    private StreamingProfile mProfile;
    private CameraStreamingSetting setting;
    private MicrophoneStreamingSetting microphoneSetting;
    private ThemedReactContext context;


    private void initializeStreamingSessionIfNeeded(AspectFrameLayout afl, CameraPreviewFrameView previewFrameView) {
        if (mCameraStreamingManager == null) {
            mCameraStreamingManager = new CameraStreamingManager(
                    context,
                    afl,
                    previewFrameView,
                    CameraStreamingManager.EncodingType.SW_VIDEO_WITH_SW_AUDIO_CODEC);  // soft codec
            mProfile = new StreamingProfile();
            StreamingProfile.AudioProfile aProfile = new StreamingProfile.AudioProfile(44100, 96 * 1024);
            StreamingProfile.VideoProfile vProfile = new StreamingProfile.VideoProfile(30, 1000 * 1024, 48);
            StreamingProfile.AVProfile avProfile = new StreamingProfile.AVProfile(vProfile, aProfile);
            mProfile.setVideoQuality(StreamingProfile.VIDEO_QUALITY_HIGH3)
                    .setAudioQuality(StreamingProfile.AUDIO_QUALITY_MEDIUM2)
//                .setPreferredVideoEncodingSize(960, 544)
                    .setEncodingSizeLevel(Config.ENCODING_LEVEL)
                    .setEncoderRCMode(StreamingProfile.EncoderRCModes.QUALITY_PRIORITY)
//                    .setStream(stream)   //set Stream
                    .setAVProfile(avProfile)
                    .setDnsManager(getMyDnsManager())
                    .setStreamStatusConfig(new StreamingProfile.StreamStatusConfig(3))
//                .setEncodingOrientation(StreamingProfile.ENCODING_ORIENTATION.PORT)
                    .setSendingBufferProfile(new StreamingProfile.SendingBufferProfile(0.2f, 0.8f, 3.0f, 20 * 1000));

            setting = new CameraStreamingSetting();
            setting.setCameraId(Camera.CameraInfo.CAMERA_FACING_BACK)
                    .setContinuousFocusModeEnabled(true)
                    .setRecordingHint(false)
                    .setResetTouchFocusDelayInMs(3000)
                    .setFocusMode(CameraStreamingSetting.FOCUS_MODE_CONTINUOUS_PICTURE)
                    .setCameraPrvSizeLevel(CameraStreamingSetting.PREVIEW_SIZE_LEVEL.MEDIUM)
                    .setCameraPrvSizeRatio(CameraStreamingSetting.PREVIEW_SIZE_RATIO.RATIO_16_9);

            microphoneSetting = new MicrophoneStreamingSetting();
            microphoneSetting.setBluetoothSCOEnabled(false);

            boolean result = mCameraStreamingManager.prepare(setting, microphoneSetting, mProfile);

            mCameraStreamingManager.setStreamingStateListener(this);
            mCameraStreamingManager.setStreamingPreviewCallback(this);
            mCameraStreamingManager.setSurfaceTextureCallback(this);
            mCameraStreamingManager.setStreamingSessionListener(this);
            mCameraStreamingManager.setStreamStatusCallback(this);
            context.addLifecycleEventListener(this);

        }
    }

    @Override
    public AspectFrameLayout createViewInstance(ThemedReactContext context) {
        this.context = context;

        AspectFrameLayout afl = new AspectFrameLayout(context);

        afl.setShowMode(AspectFrameLayout.SHOW_MODE.REAL);

        CameraPreviewFrameView previewFrameView = new CameraPreviewFrameView(context);
        previewFrameView.setListener(this);
        previewFrameView.setLayoutParams(new FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        afl.addView(previewFrameView);
        initializeStreamingSessionIfNeeded(afl, previewFrameView);

        afl.addOnAttachStateChangeListener(new View.OnAttachStateChangeListener() {
            @Override
            public void onViewAttachedToWindow(View v) {
                mCameraStreamingManager.resume();
            }

            @Override
            public void onViewDetachedFromWindow(View v) {
                mCameraStreamingManager.destroy();
            }
        });

        return afl;
    }

    @Override
    /**
     * <Streaming />
     */
    public String getName() {
        return "RCTStreaming";
    }

    protected Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_START_STREAMING:
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            // disable the shutter button before startStreaming
//                            setShutterButtonEnabled(false);
                            boolean res = mCameraStreamingManager.startStreaming();
//                            mShutterButtonPressed = true;
                            Log.i(TAG, "res:" + res);
//                            if (!res) {
//                                mShutterButtonPressed = false;
//                                setShutterButtonEnabled(true);
//                            }
//                            setShutterButtonPressed(mShutterButtonPressed);
                        }
                    }).start();
                    break;
                case MSG_STOP_STREAMING:
                    // disable the shutter button before stopStreaming
//                    setShutterButtonEnabled(false);
                    boolean res = mCameraStreamingManager.stopStreaming();
//                    if (!res) {
//                        mShutterButtonPressed = true;
//                        setShutterButtonEnabled(true);
//                    }
//                    setShutterButtonPressed(mShutterButtonPressed);
                    break;
                case MSG_SET_ZOOM:
                    mCameraStreamingManager.setZoomValue(mCurrentZoom);
                    break;
                case MSG_MUTE:  //外部设置,不需要提供了
//                    mIsNeedMute = !mIsNeedMute;
//                    mCameraStreamingManager.mute(mIsNeedMute);
//                    updateMuteButtonText();
                    break;
                default:
                    Log.e(TAG, "Invalid message");
            }
        }
    };

    protected void startStreaming() {
//        if (!streaming) {
        mHandler.removeCallbacksAndMessages(null);
        mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_START_STREAMING), 50);
//            streaming = true;
//        }
    }

    protected void stopStreaming() {
//        if (streaming) {
        mHandler.removeCallbacksAndMessages(null);
        mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_STOP_STREAMING), 50);
//            streaming = false;
//        }

    }

    public static DnsManager getMyDnsManager() {
        IResolver r0 = new DnspodFree();
        IResolver r1 = AndroidDnsServer.defaultResolver();
        IResolver r2 = null;
        try {
            r2 = new Resolver(InetAddress.getByName("119.29.29.29"));
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return new DnsManager(NetworkInfo.normal, new IResolver[]{r0, r1, r2});
    }

    @ReactProp(name = "stream")
    public void setStream(AspectFrameLayout view, @Nullable ReadableMap stream) {  //FIXME 这里后续用json
        mProfile.setStream(new StreamingProfile.Stream(Jsons.readableMapToJson(stream)));
        mCameraStreamingManager.setStreamingProfile(mProfile);
    }

    @ReactProp(name = "muted")
    public void setMuted(AspectFrameLayout view, boolean muted) {
        mCameraStreamingManager.mute(muted);
    }

    @ReactProp(name = "zoom")
    //0 ~ 1
    public void setZoom(AspectFrameLayout view, float factor) {
        compute(factor);
        mCameraStreamingManager.setZoomValue(mCurrentZoom);
    }


    @Override
    public void onStateChanged(int state, Object extra) {
        switch (state) {
            case CameraStreamingManager.STATE.PREPARING:
                break;
            case CameraStreamingManager.STATE.READY:
                mIsReady = true;
                mMaxZoom = mCameraStreamingManager.getMaxZoom();
                startStreaming();
                break;
            case CameraStreamingManager.STATE.CONNECTING:
                break;
            case CameraStreamingManager.STATE.STREAMING:
                break;
            case CameraStreamingManager.STATE.SHUTDOWN:
                break;
            case CameraStreamingManager.STATE.IOERROR:
                break;
            case CameraStreamingManager.STATE.UNKNOWN:
                break;
            case CameraStreamingManager.STATE.SENDING_BUFFER_EMPTY:
                break;
            case CameraStreamingManager.STATE.SENDING_BUFFER_FULL:
                break;
            case CameraStreamingManager.STATE.AUDIO_RECORDING_FAIL:
                break;
            case CameraStreamingManager.STATE.OPEN_CAMERA_FAIL:
//                Log.e(TAG, "Open Camera Fail. id:" + extra);
                break;
            case CameraStreamingManager.STATE.DISCONNECTED:
//                mLogContent += "DISCONNECTED\n";
                break;
            case CameraStreamingManager.STATE.CAMERA_SWITCHED:
//                mShutterButtonPressed = false;
                if (extra != null) {
                    Log.i(TAG, "current camera id:" + (Integer) extra);
                }
                Log.i(TAG, "camera switched");
                break;
            case CameraStreamingManager.STATE.TORCH_INFO:
                if (extra != null) {
                    final boolean isSupportedTorch = (Boolean) extra;
                    Log.i(TAG, "isSupportedTorch=" + isSupportedTorch);
//                    this.runOnUiThread(new Runnable() {
//                        @Override
//                        public void run() {
//                            if (isSupportedTorch) {
//                                mTorchBtn.setVisibility(View.VISIBLE);
//                            } else {
//                                mTorchBtn.setVisibility(View.GONE);
//                            }
//                        }
//                    });
                }
                break;
        }
    }


    @Override
    public boolean onRecordAudioFailedHandled(int err) {
        mCameraStreamingManager.updateEncodingType(CameraStreamingManager.EncodingType.SW_VIDEO_CODEC);
        mCameraStreamingManager.startStreaming();
        return true;
    }

    @Override
    public boolean onRestartStreamingHandled(int err) {
        Log.i(TAG, "onRestartStreamingHandled");
        return mCameraStreamingManager.startStreaming();
    }

    @Override
    public Camera.Size onPreviewSizeSelected(List<Camera.Size> list) {
        Camera.Size size = null;
//        if (list != null) {
//            for (Camera.Size s : list) {
//                Log.i(TAG, "w:" + s.width + ", h:" + s.height);
//            }
//        }
//        Log.e(TAG, "selected size :" + size.width + "x" + size.height);
        return size;
    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        Log.i(TAG, "onSingleTapUp X:" + e.getX() + ",Y:" + e.getY());

        if (mIsReady) {
//            setFocusAreaIndicator();
            mCameraStreamingManager.doSingleTapUp((int) e.getX(), (int) e.getY());
            return true;
        }
        return false;
    }

    @Override
    public boolean onZoomValueChanged(float factor) {
        if (mIsReady && mCameraStreamingManager.isZoomSupported()) {
            compute(factor);
            Log.d(TAG, "zoom ongoing, scale: " + mCurrentZoom + ",factor:" + factor + ",maxZoom:" + mMaxZoom);
            if (!mHandler.hasMessages(MSG_SET_ZOOM)) {
                mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_SET_ZOOM), ZOOM_MINIMUM_WAIT_MILLIS);
                return true;
            }
        }
        Log.d(TAG, "zoom ongoing, scale: " + mCurrentZoom + ",factor:" + factor + ",maxZoom:" + mMaxZoom);
        return false;
    }

    private void compute(float factor) {
        mCurrentZoom = (int) (mMaxZoom * factor);
        mCurrentZoom = Math.min(mCurrentZoom, mMaxZoom);
        mCurrentZoom = Math.max(0, mCurrentZoom);
    }

    @Override
    public boolean onStateHandled(final int state, Object extra) {
        switch (state) {
            case CameraStreamingManager.STATE.SENDING_BUFFER_HAS_FEW_ITEMS:
                return false;
            case CameraStreamingManager.STATE.SENDING_BUFFER_HAS_MANY_ITEMS:
                return false;
        }
        return false;
    }

    @Override
    public void onLayoutChange(View v, int left, int top, int right, int bottom, int oldLeft, int oldTop, int oldRight, int oldBottom) {
        Log.i(TAG, "view!!!!:" + v);
    }

    @Override
    public void onPreviewFrame(byte[] bytes, Camera camera) {
        System.out.println();
    }

    @Override
    public boolean onPreviewFrame(byte[] bytes, int width, int height) {
//        deal with the yuv data.
//        long start = System.currentTimeMillis();
//        for (int i = 0; i < bytes.length; i++) {
//            bytes[i] = 0x00;
//        }
//        Log.i(TAG, "old onPreviewFrame cost :" + (System.currentTimeMillis() - start));
        return true;
    }

    @Override
    public void onSurfaceCreated() {
        Log.i(TAG, "onSurfaceCreated");
//        mFBO.initialize(this);
    }

    @Override
    public void onSurfaceChanged(int width, int height) {
        Log.i(TAG, "onSurfaceChanged width:" + width + ",height:" + height);
//        mFBO.updateSurfaceSize(width, height);
    }

    @Override
    public void onSurfaceDestroyed() {
        Log.i(TAG, "onSurfaceDestroyed");
//        mFBO.release();
    }

    @Override
    public int onDrawFrame(int texId, int texWidth, int texHeight) {
        // newTexId should not equal with texId. texId is from the SurfaceTexture.
        // Otherwise, there is no filter effect.
//        int newTexId = mFBO.drawFrame(texId, texWidth, texHeight);
        Log.i(TAG, "onDrawFrame texId:" + texId + ",texWidth:" + texWidth + ",texHeight:" + texHeight);
//        return newTexId;
        return texId;
    }

    @Override
    public void notifyStreamStatusChanged(final StreamingProfile.StreamStatus streamStatus) {
//        runOnUiThread(new Runnable() {
//            @Override
//            public void run() {
//                mStreamStatus.setText("bitrate:" + streamStatus.totalAVBitrate / 1024 + " kbps"
//                        + "\naudio:" + streamStatus.audioFps + " fps"
//                        + "\nvideo:" + streamStatus.videoFps + " fps");
//            }
//        });
        Log.i(TAG, "notifyStreamStatusChanged");
    }

    @Override
    public void onHostResume() {
        mCameraStreamingManager.resume();
    }

    @Override
    public void onHostPause() {
        mCameraStreamingManager.pause();
    }

    @Override
    public void onHostDestroy() {
        mCameraStreamingManager.destroy();
    }
}
