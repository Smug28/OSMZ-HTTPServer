package com.kru13.httpserver.pages;

import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import com.kru13.httpserver.ClientThread;
import com.kru13.httpserver.HttpRequest;
import com.kru13.httpserver.HttpResponse;

import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by smug2 on 07.03.2017.
 */

public class CameraStreamPage extends WebPage {
    public CameraStreamPage(HttpRequest request, Handler handler) {
        super(request, handler);
    }

    @Override
    public HttpResponse getResponse() {
        sendMessage(new com.kru13.httpserver.Message("camera", 0));
        return new HttpResponse(String.format("<html><head><title>%s %s</title><meta http-equiv=\"refresh\" content=\"1;url=http://127.0.0.1:12345/camera_sd\"></head><body><h1>Camera feed SD:</h1><img width=\"60%%\" src=\"%s/camera_feed.jpg\"></body></html>", Build.MANUFACTURER, Build.MODEL, Environment.getExternalStorageDirectory().getAbsolutePath()));
    }

    public HttpResponse getResponseMultipart(){
        int id = 0;
        HashMap<String, String> params = request.getUriParams();
        if (params != null && params.containsKey("id")){
            try {
                id = Integer.valueOf(params.get("id"));
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
        com.kru13.httpserver.Message message = new com.kru13.httpserver.Message("camera", id);
        sendMessage(message);
        synchronized (message){
            try {
                message.wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        HttpResponse response = new HttpResponse();
        if (message.getBuffer() != null){
            response.setBody(ByteBuffer.wrap(message.getBuffer()));
            response.setContentType("image/jpeg");
            response.setContentLength(message.getBuffer().length);
        }
        return response;
    }
}
