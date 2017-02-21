package com.kru13.httpserver;

import android.os.*;
import android.os.Message;
import android.util.Log;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;

/**
 * Created by smuggler on 21.02.17.
 */

public class ClientThread extends Thread {
    private final Handler messageHandler;
    private final Socket socket;

    public ClientThread(Socket socket, Handler handler){
        this.messageHandler = handler;
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            OutputStream o = socket.getOutputStream();
            BufferedWriter out = new BufferedWriter(new OutputStreamWriter(o));
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            HttpRequest request = new HttpRequest(in);
            HttpResponse response;
            if (request.getUri().startsWith("/storage")){
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
                Message msg = messageHandler.obtainMessage();
                msg.obj = new com.kru13.httpserver.Message(String.valueOf(root != null ? root.getAbsolutePath() : null), root != null ? root.length() : 0);
                msg.sendToTarget();
            }
            else
                response = new HttpResponse(String.format("<html><head><title>%s %s</title></head><body><h1>The request was:</h1><p>", Build.MANUFACTURER, Build.MODEL) + request.toString().replace("\n", "<br>") + "</p></body></html>");

            out.write(response.toString());
            out.flush();
            if (response.getBody() instanceof File) {
                File f = (File) response.getBody();
                FileInputStream r = new FileInputStream(f);
                byte[] bytes = new byte[1024];
                int len = 0;
                while ((len = r.read(bytes, 0, 1024)) > 0)
                    o.write(bytes, 0, len);
                o.flush();
            }
            o.close();
            socket.close();
            Log.d("SERVER", "Socket Closed");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
