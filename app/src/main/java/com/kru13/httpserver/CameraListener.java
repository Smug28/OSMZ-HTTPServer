package com.kru13.httpserver;

import java.nio.ByteBuffer;

/**
 * Created by smug2 on 07.03.2017.
 */

public interface CameraListener {
    void onNewImage(ByteBuffer image);
}
