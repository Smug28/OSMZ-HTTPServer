package com.kru13.httpserver;

import android.text.format.DateFormat;
import android.util.Log;
import android.webkit.MimeTypeMap;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.StringTokenizer;
import java.util.TimeZone;

/**
 * Created by smuggler on 07.02.17.
 */

public class HttpResponse {
    private int statusCode = 200;
    private String statusMessage = "OK";
    private Object body = "";
    private String contentType = "text/html";
    private String server = "Smuggler's Awesome HTTP Server/6.66";
    private HashMap<String, String> headers = new HashMap<>();

    public HttpResponse(){

    }

    public HttpResponse(String body){
        this.body = body;
    }

    public HttpResponse(int code, String message, String body){
        this(body);
        setStatus(code, message);
    }

    public void setStatus(int code, String message){
        statusCode = code;
        statusMessage = message;
    }

    public String getStatus(){
        return String.format(Locale.US, "%d %s", statusCode, statusMessage);
    }

    public String getServerTime() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
        dateFormat.setTimeZone(TimeZone.getTimeZone("GMT"));
        return dateFormat.format(calendar.getTime());
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public String getStatusMessage() {
        return statusMessage;
    }

    public void setStatusMessage(String statusMessage) {
        this.statusMessage = statusMessage;
    }

    public HashMap<String, String> getHeaders() {
        return headers;
    }

    public void setHeaders(HashMap<String, String> headers) {
        this.headers = headers;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Object getBody() {
        return body;
    }

    public void setBody(Object body) {
        this.body = body;
        if (body instanceof File){
            setContentType(getMimeType(((File) body).getAbsolutePath()));
        }
    }

    public static String getMimeType(String url) {
        String type = null;
        String extension = MimeTypeMap.getFileExtensionFromUrl(url);
        if (extension != null) {
            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
        }
        return type;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }

    public void addHeader(String key, String value){
        headers.put(key, value);
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format(Locale.US, "HTTP/1.0 %d %s\r\n", statusCode, statusMessage));
        sb.append("Date: ");
        sb.append(getServerTime());
        sb.append("\r\nContent-Type: ");
        sb.append(contentType);
        sb.append("\r\n");
        sb.append(String.format(Locale.US, "Content-Length: %d\r\n", getContentLength()));
        sb.append(String.format("Server: %s\r\n", server));
        for (String key : headers.keySet()){
            sb.append(String.format(Locale.US, "%s: %s\r\n", key, headers.get(key)));
        }
        sb.append("\r\n");
        if (body instanceof String)
            sb.append(body);
        String result = sb.toString();
        Log.d("SERVER_RESPONSE", result);
        return result;
    }

    private long getContentLength() {
        if (body instanceof String)
            return ((String) body).length();
        if (body instanceof File)
            return ((File) body).length();
        return 0;
    }
}
