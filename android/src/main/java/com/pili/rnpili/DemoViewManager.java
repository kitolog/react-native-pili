//package com.pili.rnpili;
//
//import android.support.annotation.Nullable;
//import android.view.View;
//
//import com.facebook.react.uimanager.annotations.ReactProp;
//import com.facebook.react.uimanager.SimpleViewManager;
//import com.facebook.react.uimanager.ThemedReactContext;
//
///**
// * Created by buhe on 16/5/3.
// */
//public class DemoViewManager extends SimpleViewManager<View> {
//
//    public static final String REACT_CLASS = "DEMO";
//    @Override
//    public String getName() {
//        return REACT_CLASS;
//    }
//
//    @Override
//    protected View createViewInstance(ThemedReactContext reactContext) {
//        return new View(reactContext);
//    }
//
//    @ReactProp(name = "str")
//    public void setStr(final View view,@Nullable final String str){
//        System.out.println(str);
//    }
//}
