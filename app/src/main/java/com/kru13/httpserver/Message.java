package com.kru13.httpserver;

/**
 * Created by smuggler on 21.02.17.
 */

public class Message {
    public final String file;
    public final long size;
    public Message(String file, long size){
        this.file = file;
        this.size = size;
    }

    @Override
    public String toString() {
        return "Message(file=" + file + ", size=" + size + " B)";
    }
}