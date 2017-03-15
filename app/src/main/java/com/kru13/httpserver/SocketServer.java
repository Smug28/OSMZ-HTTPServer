package com.kru13.httpserver;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.Semaphore;

import android.os.Handler;
import android.util.Log;

public class SocketServer extends Thread {
	
	private ServerSocket serverSocket;
	public final int port = 12345;
	private static boolean bRunning = false;
	private Handler handler;
	private Semaphore semaphore;

	public SocketServer(Handler handler, int maxThreads){
		this.handler = handler;
		this.semaphore = new Semaphore(maxThreads, true);
		Log.d("SERVER", String.format("Started with max %d threads", maxThreads));
	}
	
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
                new ClientThread(s, handler, semaphore).start();
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

    public static boolean isRunning(){
		return bRunning;
	}
}
