package com.kru13.httpserver;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;

import android.os.Build;
import android.os.Environment;
import android.util.Log;

public class SocketServer extends Thread {
	
	ServerSocket serverSocket;
	public final int port = 12345;
	boolean bRunning;
	
	public void close() {
		try {
			serverSocket.close();
		} catch (IOException e) {
			Log.d("SERVER", "Error, probably interrupted in accept(), see log");
			e.printStackTrace();
		}
		bRunning = false;
	}
	
	public void run() {
        try {
        	Log.d("SERVER", "Creating Socket");
            serverSocket = new ServerSocket(port);
            bRunning = true;
            while (bRunning) {
            	Log.d("SERVER", "Socket Waiting for connection");
                Socket s = serverSocket.accept(); 
                Log.d("SERVER", "Socket Accepted");
                
                OutputStream o = s.getOutputStream();
	        	BufferedWriter out = new BufferedWriter(new OutputStreamWriter(o));
	        	BufferedReader in = new BufferedReader(new InputStreamReader(s.getInputStream()));

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
                s.close();
                Log.d("SERVER", "Socket Closed");
            }
        } 
        catch (IOException e) {
            if (serverSocket != null && serverSocket.isClosed())
            	Log.d("SERVER", "Normal exit");
            else {
            	Log.d("SERVER", "Error");
            	e.printStackTrace();
            }
        }
        finally {
        	serverSocket = null;
        	bRunning = false;
        }
    }

}
