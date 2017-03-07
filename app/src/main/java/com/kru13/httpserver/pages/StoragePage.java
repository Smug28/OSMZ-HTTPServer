package com.kru13.httpserver.pages;

import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import com.kru13.httpserver.HttpRequest;
import com.kru13.httpserver.HttpResponse;

import java.io.File;

/**
 * Created by smug2 on 07.03.2017.
 */

public class StoragePage extends WebPage {

    public StoragePage(HttpRequest request, Handler handler) {
        super(request, handler);
    }

    @Override
    public HttpResponse getResponse() {
        HttpResponse response;
        StringBuilder content = new StringBuilder();
        File root = Environment.getExternalStorageDirectory();
        if (!request.getUri().equals("/storage") && !request.getUri().equals("/storage/")){
            root = new File(request.getUriDecoded());
        }
        if (root != null && root.isFile()){
            response = new HttpResponse();
            response.setBody(root);
        }
        else if (root != null && root.listFiles() != null) {
            for (File f : root.listFiles()) {
                content.append(String.format("<li><a href=\"%s\">%s</a></li>", f.getPath(), f.getName()));
            }
            response = new HttpResponse(String.format("<html><head><title>%s %s</title></head><body><h1>External storage:</h1><a href=\"%s\">&lt;&lt; %s</a><ul>", Build.MANUFACTURER, Build.MODEL, root.getParentFile().getPath(), root.getParentFile().getName()) + content.toString() + "</ul></body></html>");
        }
        else
            response = new HttpResponse(404, "Not Found", "<html><head><title>%s %s</title></head><body><h1>File not found</h1><a href=\"/storage\">Back to storage</a></body></html>");
        sendMessage(new com.kru13.httpserver.Message(String.valueOf(root != null ? root.getAbsolutePath() : null), root != null ? root.length() : 0));
        return response;
    }
}
