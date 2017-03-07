package com.kru13.httpserver.pages;

import android.os.Bundle;
import android.os.Handler;

import com.kru13.httpserver.HttpRequest;
import com.kru13.httpserver.HttpResponse;
import com.kru13.httpserver.Message;

/**
 * Created by smug2 on 07.03.2017.
 */

public abstract class WebPage {
    private Handler messageHandler;
    protected HttpRequest request;

    public WebPage(HttpRequest request, Handler handler){
        this.request = request;
        this.messageHandler = handler;
    }

    protected void sendMessage(Message message){
        android.os.Message msg = messageHandler.obtainMessage();
        msg.obj = message;
        msg.sendToTarget();
    }

    protected void sendMessage(Bundle data){
        android.os.Message msg = messageHandler.obtainMessage();
        msg.obj = null;
        msg.setData(data);
        msg.sendToTarget();
    }

    public abstract HttpResponse getResponse();
}
