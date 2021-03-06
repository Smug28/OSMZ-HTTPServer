package com.kru13.httpserver;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by smuggler on 07.02.17.
 */

public class HttpRequest {
    private String method = null;
    private String uri = null;
    private String version = null;
    private HashMap<String, String> headers = new HashMap<>();
    private int lines = 0;
    private String originalRequest = "";

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

    public HashMap<String, String> getUriParams(){
        HashMap<String, String> params = new HashMap<>();
        if (uri == null)
            return params;
        if (!uri.contains("?"))
            return params;
        String[] p = uri.split("\\?");
        if (p.length != 2)
            return params;
        String[] p2 = p[1].split("&");
        for (String s : p2){
            try {
                String[] kv = s.split("=");
                params.put(kv[0], kv[1]);
            } catch (Exception e){
                e.printStackTrace();
            }
        }
        return params;
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

    public String getUriDecoded(){
        return decodeString(uri);
    }

    private String decodeString(String s){
        try {
            return URLDecoder.decode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
            return s;
        }
    }

    @Override
    public String toString() {
        Log.d("SERVER_REQUEST", originalRequest);
        return originalRequest;
    }
}
