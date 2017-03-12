package com.kru13.httpserver;

import java.nio.ByteBuffer;

/**
 * Created by smuggler on 21.02.17.
 */

public class Message {
    public final String file;
    public final long size;
    public byte[] buffer;
    public Message(String file, long size){
        this.file = file;
        this.size = size;
    }

    public void setBuffer(byte[] buffer){
        this.buffer = buffer;
    }

    public byte[] getBuffer(){
        return buffer;
    }

    @Override
    public String toString() {
        return "Message(file=" + file + ", size=" + size + " B)";
    }
}