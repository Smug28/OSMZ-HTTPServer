package com.kru13.httpserver.pages;

import android.os.Build;
import android.os.Handler;

import com.kru13.httpserver.HttpRequest;
import com.kru13.httpserver.HttpResponse;

/**
 * Created by smug2 on 07.03.2017.
 */

public class DefaultPage extends WebPage {
    public DefaultPage(HttpRequest request, Handler handler) {
        super(request, handler);
    }

    @Override
    public HttpResponse getResponse() {
        return new HttpResponse(String.format("<html><head><title>%s %s</title></head><body><h1>The request was:</h1><p>", Build.MANUFACTURER, Build.MODEL) + request.toString().replace("\n", "<br>") + "</p></body></html>");
    }
}
