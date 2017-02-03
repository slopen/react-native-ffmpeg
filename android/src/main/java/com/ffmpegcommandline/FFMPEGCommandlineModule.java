package com.ffmpegcommandline;

import android.content.Context;
import android.support.annotation.Nullable;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.Promise;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.io.StringWriter;
import java.io.PrintWriter;

import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Arrays;
import java.util.Objects;
import java.util.ArrayList;

import com.ffmpegcommandline.FFMPEGWrapper;
import com.ffmpegcommandline.ShellCallback;


public class FFMPEGCommandlineModule extends ReactContextBaseJavaModule {
    private Context context;
    private FFMPEGWrapper ffmpeg;

    public FFMPEGCommandlineModule (ReactApplicationContext reactContext) {
        super (reactContext);

        this.context = reactContext;
    }

    /**
     * @return the name of this module. This will be the name used to {@code require()} this module
     * from javascript.
     */
    @Override
    public String getName () {
        return "FFMPEGCommandline";
    }

    @Override
    public @Nullable Map<String, Object> getConstants() {
        HashMap<String, Object> constants = new HashMap<String, Object>();

        constants.put ("FILES_DIR_PATH", context.getFilesDir().getPath ());
        constants.put ("CACHE_DIR_PATH", context.getExternalCacheDir ().getPath ());

        return constants;
    }

    @Override
    public void initialize () {
        super.initialize ();

        try {
            ffmpeg = new FFMPEGWrapper (context);
            sendEvent ("ffmpeg:init", "true");
        } catch (Exception e) {
            sendEvent ("ffmpeg:init", printExeption (e));
        }
    }


    @ReactMethod
    public void runCommand (
        ReadableArray command,
        final Promise promise
    ) {
        try {

            ShellCallback shellCallback = new ShellCallback () {
                @Override
                public void shellOut (char[] shellout) {
                    String line = new String (shellout);

                    if (Objects.equals (line, "FFMPEG_ERROR")) {
                        promise.reject (line);
                    } else if (Objects.equals (line, "FFMPEG_DONE")) {
                        promise.resolve (line);
                    } else {
                        sendEvent ("ffmpeg:message", line);
                    }
                }
            };

            ffmpeg.processVideo (parseCommand (command), shellCallback);

        } catch (Exception e) {
            promise.reject (printExeption (e));
        }
    }

    private String[] parseCommand (
        ReadableArray command
    ) {
        List<String> list = new ArrayList<String>();
        for (int i = 0; i < command.size (); i++){
            list.add (command.getString (i));
        }

        return list.toArray (new String [list.size ()]);
    }

    private String printExeption (Exception e) {
        StringWriter sw = new StringWriter ();
        e.printStackTrace (new PrintWriter (sw));

        return sw.toString ();
    }

    private void sendEvent (String eventName, String line) {
        getReactApplicationContext ()
            .getJSModule (DeviceEventManagerModule.RCTDeviceEventEmitter.class)
            .emit (eventName, line);
    }
}