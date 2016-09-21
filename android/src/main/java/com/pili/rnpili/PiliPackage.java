package com.pili.rnpili;

import android.content.Context;

import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.JavaScriptModule;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ViewManager;
import com.pili.pldroid.streaming.StreamingEnv;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * Created by buhe on 16/4/29.
 */
public class PiliPackage implements ReactPackage {

    private Context context;

    public PiliPackage(Context context) {
        StreamingEnv.init(context);
        this.context = context;
    }

    @Override
    public List<NativeModule> createNativeModules(ReactApplicationContext reactContext) {
        return Collections.emptyList();
    }

    @Override
    public List<Class<? extends JavaScriptModule>> createJSModules() {
        return Collections.emptyList();
    }

    @Override
    public List<ViewManager> createViewManagers(ReactApplicationContext reactContext) {
        return Arrays.<ViewManager>asList(
                new PiliStreamingViewManager(), //TODO PlayerViewManager , HC ,SC ,Audio
                new PiliPlayerViewManager()
        );
    }
}
