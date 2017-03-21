package com.kru13.httpserver.pages;

import android.annotation.SuppressLint;
import android.os.Build;
import android.os.Handler;

import com.kru13.httpserver.HttpRequest;
import com.kru13.httpserver.HttpResponse;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * Created by smuggler on 21.03.17.
 */

public class RunCommandPage extends WebPage {
    public RunCommandPage(HttpRequest request, Handler handler){
        super(request, handler);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public HttpResponse getResponse() {
        HttpResponse response = new HttpResponse();
        try {
            String command = request.getUriDecoded().replace("/cgi-bin/", "").replace("/cgi-bin", "");
            Process process = Runtime.getRuntime().exec(command);
            BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null){
                response.appendBody(line + "\n");
            }
            int code = process.waitFor();
            if (response.getBody() == null || ((String) response.getBody()).isEmpty())
                response.setBody(String.format("Process exited with code %d", code));
            response.setBody(String.format("<html><head><title>%s %s</title></head><body><pre>%s</pre></body></html>", Build.MANUFACTURER, Build.MODEL, (String) response.getBody()));
        } catch (Exception e) {
            e.printStackTrace();
            response.setStatusCode(500);
            response.setStatusMessage("Server error");
            response.setBody(String.format("<html><head><title>%s %s</title></head><body><h1>Error</h1>%s</body></html>", Build.MANUFACTURER, Build.MODEL, e.getMessage()));
        }
        return response;
    }
}
