package com.kru13.httpserver;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * Created by smuggler on 07.02.17.
 */

public class HttpRequest {
    private String method = null;
    private String uri = null;
    private String version = null;
    private HashMap<String, String> headers = new HashMap<>();
    private int lines = 0;
    private String originalRequest = null;

    public HttpRequest(){

    }

    public HttpRequest(BufferedReader in) throws IOException {
        String line;
        while (!(line = in.readLine()).isEmpty()) {
            append(line);
            originalRequest += line + "\r\n";
        }
    }

    public HttpRequest(String lines) throws IOException {
        originalRequest = lines;
        for (String s : lines.split("\n"))
            append(s);
    }

    public HttpRequest(List<String> lines) throws IOException {
        for (String s : lines) {
            append(s);
            originalRequest += s + "\r\n";
        }
    }

    public void append(String line) throws IOException {
        if (lines == 0){
            String[] l = line.split(" ");
            if (l.length != 3)
                throw new IOException("Not a valid HTTP request");
            method = l[0];
            uri = l[1];
            version = l[2];
        }
        else {
            String[] l = line.split(": ");
            headers.put(l[0], l[1]);
        }
        lines++;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public HashMap<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(HashMap<String, String> headers) {
        this.headers = headers;
    }

    @Override
    public String toString() {
        Log.d("SERVER_REQUEST", originalRequest);
        return originalRequest;
    }
}
