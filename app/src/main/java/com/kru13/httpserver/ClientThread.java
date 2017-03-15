package com.kru13.httpserver;

import android.os.*;
import android.os.Message;
import android.util.Log;

import com.kru13.httpserver.pages.CameraStreamPage;
import com.kru13.httpserver.pages.DefaultPage;
import com.kru13.httpserver.pages.ErrorPage;
import com.kru13.httpserver.pages.StoragePage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.concurrent.Semaphore;

/**
 * Created by smuggler on 21.02.17.
 */

public class ClientThread extends Thread {
    private final Handler messageHandler;
    private final Socket socket;
    private final Semaphore semaphore;

    public ClientThread(Socket socket, Handler handler, Semaphore semaphore){
        this.messageHandler = handler;
        this.socket = socket;
        this.semaphore = semaphore;
    }

    public static byte[] getActiveArray(ByteBuffer buffer) {
        if (buffer == null)
            return null;
        byte[] ret = new byte[buffer.remaining()];
        if (buffer.hasArray()) {
            byte[] array = buffer.array();
            System.arraycopy(array, buffer.arrayOffset() + buffer.position(), ret, 0, ret.length);
        }
        else {
            buffer.slice().get(ret);
        }
        return ret;
    }

    @Override
    public void run() {
        boolean acquired = semaphore.tryAcquire();
        OutputStream out = null;
        try {
            out = socket.getOutputStream();
            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));

            HttpResponse response;
            if (!acquired){
                response = new ErrorPage(null, messageHandler).getErrorResponse(500, "Server too busy", "Please try again later...");
                out.write(response.toString().getBytes());
                out.flush();
                out.close();
                socket.close();
                Log.d("SERVER", "Socket Closed - server busy");
                return;
            }

            HttpRequest request = new HttpRequest(in);
            if (request.getUri().startsWith("/storage")){
                response = new StoragePage(request, messageHandler).getResponse();
            }
            //else if (request.getUri().startsWith("/camera_sd")){
            //      response = new CameraStreamPage(request, messageHandler).getResponse();
            //}
            else if (request.getUri().startsWith("/camera_stream")){
                response = new CameraStreamPage(request, messageHandler).getResponseMultipart();
                response.setContentLength(HttpResponse.NO_CONTENT_LENGTH);
                response.addHeader("Cache-Control", "no-cache");
                response.setContentType("multipart/x-mixed-replace; boundary=--freevideo");
                out.write(response.toString().getBytes());
                out.write("\r\n".getBytes());
                out.flush();
                while (SocketServer.isRunning()){
                    response = new CameraStreamPage(request, messageHandler).getResponseMultipart();
                    response.setBoundary("--freevideo");
                    response.setContentType("image/jpeg");
                    out.write(response.toString().getBytes());
                    out.write(getActiveArray((ByteBuffer) response.getBody()));
                    out.write("\r\n\r\n".getBytes());
                    out.flush();
                }
                out.close();
                return;
            }
            else
                response = new DefaultPage(request, messageHandler).getResponse();

            out.write(response.toString().getBytes());
            out.flush();
            if (response.getBody() instanceof File) {
                File f = (File) response.getBody();
                FileInputStream r = new FileInputStream(f);
                byte[] bytes = new byte[1024];
                int len = 0;
                while ((len = r.read(bytes, 0, 1024)) > 0)
                    out.write(bytes, 0, len);
                out.flush();
            }
            else if (response.getBody() instanceof ByteBuffer){
                out.write(getActiveArray((ByteBuffer) response.getBody()));
                out.flush();
            }
            out.close();
            socket.close();
            Log.d("SERVER", "Socket Closed");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e){
            e.printStackTrace();
        }
        finally {
            if (acquired)
                semaphore.release();
            if (!socket.isClosed())
                try {
                    socket.close();
                    Log.d("SERVER", "Socket Closed");
                } catch (IOException e) {
                    e.printStackTrace();
                }
        }
    }
}
