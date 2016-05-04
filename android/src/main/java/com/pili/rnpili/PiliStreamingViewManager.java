package com.pili.rnpili;

import android.hardware.Camera;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;

import com.facebook.react.uimanager.annotations.ReactProp;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.pili.pldroid.streaming.CameraStreamingManager;
import com.pili.pldroid.streaming.CameraStreamingSetting;
import com.pili.pldroid.streaming.MicrophoneStreamingSetting;
import com.pili.pldroid.streaming.StreamStatusCallback;
import com.pili.pldroid.streaming.StreamingProfile;
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

/**
 * Created by buhe on 16/4/29.
 */
public class PiliStreamingViewManager extends SimpleViewManager<AspectFrameLayout>
        implements CameraStreamingManager.StreamingStateListener {
    private static final String TAG = "StreamingBaseActivity";
    protected static final int MSG_START_STREAMING = 0;
    protected static final int MSG_STOP_STREAMING = 1;
    private static final int MSG_SET_ZOOM = 2;
    private static final int MSG_MUTE = 3;

    protected CameraStreamingManager mCameraStreamingManager;
    protected String mStatusMsgContent;
    protected boolean mIsReady = false;
    private int mCurrentZoom = 0;
    private int mMaxZoom = 0;
    private StreamingProfile mProfile;
    private CameraStreamingSetting setting;
    private MicrophoneStreamingSetting microphoneSetting;
    private ThemedReactContext context;

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


    @Override
    public AspectFrameLayout createViewInstance(ThemedReactContext context) {
        this.context = context;


        AspectFrameLayout afl = new AspectFrameLayout(context);


        afl.setShowMode(AspectFrameLayout.SHOW_MODE.REAL);


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
                case MSG_MUTE:
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
        mHandler.removeCallbacksAndMessages(null);
        mHandler.sendMessageDelayed(mHandler.obtainMessage(MSG_START_STREAMING), 50);
    }

    @ReactProp(name = "streamProfile")
    public void setStreamProfile(AspectFrameLayout view, boolean profile) {
        String streamJsonStrFromServer = "{\n" +
                "  \"id\":\"buhe\",\n" +
                "  \"title\":\"buhe\",\n" +
                "  \"hub\":\"pilitest\",\n" +
                "  \"publishKey\":\"6eeee8a82246636e\",\n" +
                "  \"publishSecurity\":\"static\",\n" +
                "  \"hosts\":{\"publish\":{\"rtmp\":\"pili-publish.pilitest.qiniucdn.com\"}}\n" +
                "}";

        JSONObject mJSONObject = null;
        try {
            mJSONObject = new JSONObject(streamJsonStrFromServer);
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //FIXME
        CameraPreviewFrameView previewFrameView = new CameraPreviewFrameView(context);
        view.addView(previewFrameView);
        StreamingProfile.AudioProfile aProfile = new StreamingProfile.AudioProfile(44100, 96 * 1024);
        StreamingProfile.VideoProfile vProfile = new StreamingProfile.VideoProfile(30, 1000 * 1024, 48);
        StreamingProfile.AVProfile avProfile = new StreamingProfile.AVProfile(vProfile, aProfile);

        StreamingProfile.Stream stream = new StreamingProfile.Stream(mJSONObject);
        mProfile = new StreamingProfile();

        mProfile.setVideoQuality(StreamingProfile.VIDEO_QUALITY_HIGH3)
                .setAudioQuality(StreamingProfile.AUDIO_QUALITY_MEDIUM2)
//                .setPreferredVideoEncodingSize(960, 544)
                .setEncodingSizeLevel(Config.ENCODING_LEVEL)
                .setEncoderRCMode(StreamingProfile.EncoderRCModes.QUALITY_PRIORITY)
                .setStream(stream)
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

        mCameraStreamingManager = new CameraStreamingManager(context, view, previewFrameView, CameraStreamingManager.EncodingType.SW_VIDEO_WITH_SW_AUDIO_CODEC);  // soft codec
        mCameraStreamingManager.setStreamingStateListener(this);
        boolean result = mCameraStreamingManager.prepare(setting, microphoneSetting, mProfile);
        System.out.println(result);
    }


    @Override
    public void onStateChanged(int state, Object extra) {
        switch (state) {
            case CameraStreamingManager.STATE.PREPARING:
//                mStatusMsgContent = getString(R.string.string_state_preparing);
                break;
            case CameraStreamingManager.STATE.READY:
                mIsReady = true;
                mMaxZoom = mCameraStreamingManager.getMaxZoom();
//                mStatusMsgContent = getString(R.string.string_state_ready);
                // start streaming when READY
                startStreaming();
                break;
            case CameraStreamingManager.STATE.CONNECTING:
//                mStatusMsgContent = getString(R.string.string_state_connecting);
                break;
            case CameraStreamingManager.STATE.STREAMING:
//                mStatusMsgContent = getString(R.string.string_state_streaming);
//                setShutterButtonEnabled(true);
//                setShutterButtonPressed(true);
                break;
            case CameraStreamingManager.STATE.SHUTDOWN:
//                mStatusMsgContent = getString(R.string.string_state_ready);
//                setShutterButtonEnabled(true);
//                setShutterButtonPressed(false);
                break;
            case CameraStreamingManager.STATE.IOERROR:
//                mLogContent += "IOERROR\n";
//                mStatusMsgContent = getString(R.string.string_state_ready);
//                setShutterButtonEnabled(true);
                break;
            case CameraStreamingManager.STATE.UNKNOWN:
//                mStatusMsgContent = getString(R.string.string_state_ready);
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
        }
    }

    @Override
    public boolean onStateHandled(int i, Object o) {
        return false;
    }
}
