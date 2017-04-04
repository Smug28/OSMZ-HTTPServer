package com.kru13.httpserver;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;

import java.nio.ByteBuffer;

public class HttpService extends Service {
    public static boolean isRunnting = false;
    public static final String ACTION_LOG = "com.kru13.httpserver.ACTION_LOG";
    private CameraHandler mCameraHandler;
    private SocketServer s;
    private SharedPreferences prefs;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(android.os.Message msg) {
            super.handleMessage(msg);
            if ("camera".equals(((com.kru13.httpserver.Message) msg.obj).file)){
                if (!mCameraHandler.isOpened())
                    mCameraHandler.open((int) ((com.kru13.httpserver.Message) msg.obj).size);
                mCameraHandler.registerListener(new HttpService.MyCameraListener((com.kru13.httpserver.Message) msg.obj));
            }
            else {
                Intent i = new Intent(ACTION_LOG);
                i.putExtra("log", ((com.kru13.httpserver.Message) msg.obj).toString() + "\n");
                sendBroadcast(i);
            }
        }
    };

    public HttpService() {

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        prefs = PreferenceManager.getDefaultSharedPreferences(this);
        mCameraHandler = new CameraHandler(this);
        s = new SocketServer(handler, prefs.getInt("threads", 10));
        s.start();
        isRunnting = true;
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        s.close();
        try {
            s.join();
        } catch (InterruptedException e) {
            Log.d("HTTP_SERVICE", "onDestroy: " + e.getMessage());
            e.printStackTrace();
        }
        mCameraHandler.close();
        isRunnting = false;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    class MyCameraListener implements CameraListener {
        com.kru13.httpserver.Message message;
        public MyCameraListener(com.kru13.httpserver.Message msg){
            message = msg;
        }

        @Override
        public void onNewImage(ByteBuffer image) {
            synchronized (message) {
                message.setBuffer(ClientThread.getActiveArray(image));
                message.notifyAll();
            }
        }
    }
}
