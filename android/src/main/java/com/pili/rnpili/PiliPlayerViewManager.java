package com.pili.rnpili;

import android.util.Log;

import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.uimanager.SimpleViewManager;
import com.facebook.react.uimanager.ThemedReactContext;
import com.facebook.react.uimanager.annotations.ReactProp;
import com.pili.pldroid.player.AVOptions;
import com.pili.pldroid.player.PLMediaPlayer;
import com.pili.pldroid.player.widget.PLVideoView;
import com.pili.rnpili.support.MediaController;

/**
 * Created by buhe on 16/4/29.
 */
public class PiliPlayerViewManager extends SimpleViewManager<PLVideoView> {
    private ThemedReactContext reactContext;
    private static final String TAG = PiliPlayerViewManager.class.getSimpleName();

    @Override
    public String getName() {
        return "RCTPlayer";
    }

    @Override
    protected PLVideoView createViewInstance(ThemedReactContext reactContext) {
        this.reactContext = reactContext;
        PLVideoView mVideoView = new PLVideoView(reactContext);
        // Set some listeners
        // Set some listeners
        mVideoView.setOnPreparedListener(mOnPreparedListener);
        mVideoView.setOnInfoListener(mOnInfoListener);
        mVideoView.setOnVideoSizeChangedListener(mOnVideoSizeChangedListener);
        mVideoView.setOnBufferingUpdateListener(mOnBufferingUpdateListener);
        mVideoView.setOnCompletionListener(mOnCompletionListener);
        mVideoView.setOnSeekCompleteListener(mOnSeekCompleteListener);
        mVideoView.setOnErrorListener(mOnErrorListener);
        return mVideoView;
    }

    private boolean isLiveStreaming(String url) {
        if (url.startsWith("rtmp://")
                || (url.startsWith("http://") && url.endsWith(".m3u8"))
                || (url.startsWith("http://") && url.endsWith(".flv"))) {
            return true;
        }
        return false;
    }

    @ReactProp(name = "source")
    public void setSource(PLVideoView mVideoView, ReadableMap source) {
        AVOptions options = new AVOptions();
        String uri = source.getString("uri");
        boolean mediaController = source.hasKey("controller") && source.getBoolean("controller");
        int avFrameTimeout = source.hasKey("timeout") ? source.getInt("timeout") : -1;        //10 * 1000 ms
        boolean liveStreaming = source.hasKey("live") && source.getBoolean("live");  //1 or 0 // 1 -> live
        boolean codec = source.hasKey("hardCodec") && source.getBoolean("hardCodec");  //1 or 0  // 1 -> hw codec enable, 0 -> disable [recommended]
        // the unit of timeout is ms
        if (avFrameTimeout >= 0) {
            options.setInteger(AVOptions.KEY_GET_AV_FRAME_TIMEOUT, avFrameTimeout);
        }
        // Some optimization with buffering mechanism when be set to 1
        if (liveStreaming) {
            options.setInteger(AVOptions.KEY_LIVE_STREAMING, 1);
        } else {
            options.setInteger(AVOptions.KEY_LIVE_STREAMING, 0);
        }
//        }

        // 1 -> hw codec enable, 0 -> disable [recommended]
        if (codec) {
            options.setInteger(AVOptions.KEY_MEDIACODEC, 1);
        } else {
            options.setInteger(AVOptions.KEY_MEDIACODEC, 0);
        }

        mVideoView.setAVOptions(options);

        // After setVideoPath, the play will start automatically
        // mVideoView.start() is not required

        mVideoView.setVideoPath(uri);

        if (mediaController) {
            // You can also use a custom `MediaController` widget
            MediaController mMediaController = new MediaController(reactContext, false, isLiveStreaming(uri));
            mVideoView.setMediaController(mMediaController);
        }

    }


    private PLMediaPlayer.OnPreparedListener mOnPreparedListener = new PLMediaPlayer.OnPreparedListener() {
        @Override
        public void onPrepared(PLMediaPlayer plMediaPlayer) {
            Log.d(TAG, "onPrepared ! ");
        }
    };

    private PLMediaPlayer.OnInfoListener mOnInfoListener = new PLMediaPlayer.OnInfoListener() {
        @Override
        public boolean onInfo(PLMediaPlayer plMediaPlayer, int what, int extra) {
            Log.d(TAG, "onInfo: " + what + ", " + extra);
            return false;
        }
    };

    private PLMediaPlayer.OnErrorListener mOnErrorListener = new PLMediaPlayer.OnErrorListener() {
        @Override
        public boolean onError(PLMediaPlayer plMediaPlayer, int errorCode) {
            Log.e(TAG, "Error happened, errorCode = " + errorCode);
            switch (errorCode) {
                case PLMediaPlayer.ERROR_CODE_INVALID_URI:
//                    showToastTips("Invalid URL !");
                    break;
                case PLMediaPlayer.ERROR_CODE_404_NOT_FOUND:
//                    showToastTips("404 resource not found !");
                    break;
                case PLMediaPlayer.ERROR_CODE_CONNECTION_REFUSED:
//                    showToastTips("Connection refused !");
                    break;
                case PLMediaPlayer.ERROR_CODE_CONNECTION_TIMEOUT:
//                    showToastTips("Connection timeout !");
                    break;
                case PLMediaPlayer.ERROR_CODE_EMPTY_PLAYLIST:
//                    showToastTips("Empty playlist !");
                    break;
                case PLMediaPlayer.ERROR_CODE_STREAM_DISCONNECTED:
//                    showToastTips("Stream disconnected !");
                    break;
                case PLMediaPlayer.ERROR_CODE_IO_ERROR:
//                    showToastTips("Network IO Error !");
                    break;
                case PLMediaPlayer.MEDIA_ERROR_UNKNOWN:
                default:
//                    showToastTips("unknown error !");
                    break;
            }
            return true;
        }
    };

    private PLMediaPlayer.OnCompletionListener mOnCompletionListener = new PLMediaPlayer.OnCompletionListener() {
        @Override
        public void onCompletion(PLMediaPlayer plMediaPlayer) {
            Log.d(TAG, "Play Completed !");
        }
    };

    private PLMediaPlayer.OnBufferingUpdateListener mOnBufferingUpdateListener = new PLMediaPlayer.OnBufferingUpdateListener() {
        @Override
        public void onBufferingUpdate(PLMediaPlayer plMediaPlayer, int precent) {
            Log.d(TAG, "onBufferingUpdate: " + precent);
        }
    };

    private PLMediaPlayer.OnSeekCompleteListener mOnSeekCompleteListener = new PLMediaPlayer.OnSeekCompleteListener() {
        @Override
        public void onSeekComplete(PLMediaPlayer plMediaPlayer) {
            Log.d(TAG, "onSeekComplete !");
        }

        ;
    };

    private PLMediaPlayer.OnVideoSizeChangedListener mOnVideoSizeChangedListener = new PLMediaPlayer.OnVideoSizeChangedListener() {
        @Override
        public void onVideoSizeChanged(PLMediaPlayer plMediaPlayer, int width, int height) {
            Log.d(TAG, "onVideoSizeChanged: " + width + "," + height);
        }
    };

}
