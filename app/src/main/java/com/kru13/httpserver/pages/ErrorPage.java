package com.kru13.httpserver.pages;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Handler;

import com.kru13.httpserver.HttpRequest;
import com.kru13.httpserver.HttpResponse;

/**
 * Created by smug2 on 07.03.2017.
 */

public class ErrorPage extends WebPage {
    private static final String errorHtml = "<html><head><title>%s %s</title></head><body><h1>Error %d - %s</h1><p>%s</p></body></html>";
    public ErrorPage(HttpRequest request, Handler handler) {
        super(request, handler);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public HttpResponse getResponse() {
        return new HttpResponse(404, "Not Found", String.format(errorHtml, Build.MANUFACTURER, Build.MODEL, 404, "Not Found", "The specified page was not found on this server..."));
    }

    @SuppressLint("DefaultLocale")
    public HttpResponse getErrorResponse(int errorCode, String title, String message){
        return new HttpResponse(errorCode, title, String.format(errorHtml, Build.MANUFACTURER, Build.MODEL, errorCode, title, message));
    }
}
