package com.kru13.httpserver;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.ImageFormat;
import android.hardware.Camera;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureFailure;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.TotalCaptureResult;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.media.Image;
import android.media.ImageReader;
import android.os.Environment;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.util.Log;
import android.util.Size;
import android.view.Surface;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

/**
 * Created by smuggler on 07.03.17.
 */

public class CameraHandler {
    private static String TAG = "CAMERA_HANDLER";
    private Context mApplicationContext;
    private CameraManager mCameraManager;
    private ImageReader mImageReader;
    private Size mImageSize;
    private boolean opened = false;

    public CameraHandler(Context context) {
        mApplicationContext = context.getApplicationContext();
        mCameraManager = (CameraManager) context.getSystemService(Context.CAMERA_SERVICE);
    }

    public void open(int id) {
        try {
            String[] cameras = mCameraManager.getCameraIdList();
            if (id < 0 || id >= cameras.length)
                id = 0;
            if (ActivityCompat.checkSelfPermission(mApplicationContext, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED)
                return;
            CameraCharacteristics characteristics = mCameraManager.getCameraCharacteristics(cameras[id]);
            StreamConfigurationMap map = characteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            Size[] sizes = map.getHighSpeedVideoSizes();
            mImageSize = sizes[sizes.length - 1];

            mCameraManager.openCamera(cameras[id], new CameraDevice.StateCallback() {
                @Override
                public void onOpened(@NonNull final CameraDevice cameraDevice) {
                    opened = true;
                    Log.d(TAG, "onOpened");
                    mImageReader = ImageReader.newInstance(mImageSize.getWidth(), mImageSize.getHeight(), ImageFormat.JPEG, 1);
                    mImageReader.setOnImageAvailableListener(new ImageReader.OnImageAvailableListener() {
                        @Override
                        public void onImageAvailable(ImageReader imageReader) {
                            Image image = imageReader.acquireNextImage();
                            Log.d(TAG, String.format("Image captured %dx%d, Format: %s", image.getWidth(), image.getHeight()));
                            image.close();
                        }
                    }, null);
                    ArrayList<Surface> surfaces = new ArrayList<Surface>();
                    surfaces.add(mImageReader.getSurface());
                    try {
                        cameraDevice.createCaptureSession(surfaces, new CameraCaptureSession.StateCallback() {
                            @Override
                            public void onConfigured(@NonNull CameraCaptureSession cameraCaptureSession) {
                                Log.d(TAG, "onConfigured");
                                try {
                                    CaptureRequest.Builder builder = cameraDevice.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
                                    builder.addTarget(mImageReader.getSurface());
                                    CaptureRequest request = builder.build();
                                    cameraCaptureSession.setRepeatingRequest(request, new CameraCaptureSession.CaptureCallback() {
                                        @Override
                                        public void onCaptureFailed(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull CaptureFailure failure) {
                                            Log.e(TAG, "onCaptureFailed");
                                            Log.e(TAG, String.valueOf(failure.getReason()));
                                        }

                                        @Override
                                        public void onCaptureSequenceAborted(@NonNull CameraCaptureSession session, int sequenceId) {
                                            Log.e(TAG, "onCaptureSequenceAborted");
                                        }

                                        @Override
                                        public void onCaptureCompleted(@NonNull CameraCaptureSession session, @NonNull CaptureRequest request, @NonNull TotalCaptureResult result) {
                                            //Log.d(TAG, "IMAGE_CAPTURED!!!!!!");
                                        }

                                        @Override
                                        public void onCaptureSequenceCompleted(@NonNull CameraCaptureSession session, int sequenceId, long frameNumber) {
                                            Log.d(TAG, "IMAGE_SEQUENCE_CAPTURED");
                                        }
                                    }, null);
                                } catch (CameraAccessException e) {
                                    e.printStackTrace();
                                }
                            }

                            @Override
                            public void onConfigureFailed(@NonNull CameraCaptureSession cameraCaptureSession) {
                                Log.e(TAG, "onConfigureFailed");
                            }
                        }, null);
                    } catch (CameraAccessException e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onDisconnected(@NonNull CameraDevice cameraDevice) {
                    opened = false;
                    Log.e(TAG, "onDisconnected");
                }

                @Override
                public void onError(@NonNull CameraDevice cameraDevice, int i) {
                    opened = false;
                    Log.e(TAG, "onError");
                }
            }, null);

        } catch (CameraAccessException e) {
            e.printStackTrace();
        }
    }
}
