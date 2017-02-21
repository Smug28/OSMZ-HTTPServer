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
import android.os.Handler;
import android.util.Log;

public class SocketServer extends Thread {
	
	ServerSocket serverSocket;
	public final int port = 12345;
	boolean bRunning;
	private Handler handler;

	public SocketServer(Handler handler){
		this.handler = handler;
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
                new ClientThread(s, handler).start();
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
