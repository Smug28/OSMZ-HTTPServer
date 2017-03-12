package com.kru13.httpserver;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.camera2.CameraManager;
import android.os.*;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;

public class HttpServerActivity extends AppCompatActivity implements OnClickListener {

    private static final String TAG = "HTTP_SERVER_ACTIVITY";
    private static final String[] NEEDED_PERMISSIONS = new String[]{ Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA };
    private SocketServer s;
    private TextView log;
    private ScrollView scrollView;
    private boolean safeToTakePicture = false;
    private Camera mCamera;
    private CameraPreview mPreview;
    private TextInputEditText maxThreads;
    private CameraHandler mCameraHandler;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if ("camera".equals(((com.kru13.httpserver.Message) msg.obj).file)){
                mCameraHandler.registerListener(new MyCameraListener((com.kru13.httpserver.Message) msg.obj));
            }
            else {
                String txt = log.getText() != null ? log.getText().toString() : "";
                log.setText(txt + ((com.kru13.httpserver.Message) msg.obj).toString() + "\n");
                scrollView.fullScroll(View.FOCUS_DOWN);
            }
        }
    };

    private Camera.PictureCallback mPicture = new Camera.PictureCallback() {

        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            try {
                camera.startPreview();
                File pictureFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/camera_feed.jpg");
                try {
                    if (!pictureFile.exists()) {
                        if (!pictureFile.createNewFile()) {
                            Log.d(TAG, "File already created");
                        }
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return;
                }

                try {
                    FileOutputStream fos = new FileOutputStream(pictureFile);
                    fos.write(data);
                    fos.close();
                } catch (FileNotFoundException e) {
                    Log.d(TAG, "File not found: " + e.getMessage());
                } catch (IOException e) {
                    Log.d(TAG, "Error accessing file: " + e.getMessage());
                }
            }
            catch (Exception e){
                e.printStackTrace();
            }
            finally {
                safeToTakePicture = true;
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_http_server);

        Button btn1 = (Button)findViewById(R.id.button1);
        Button btn2 = (Button)findViewById(R.id.button2);

        maxThreads = (TextInputEditText) findViewById(R.id.max_threads);

        btn1.setOnClickListener(this);
        btn2.setOnClickListener(this);

        log = (TextView) findViewById(R.id.log);
        scrollView = (ScrollView) findViewById(R.id.scroll_view);

        /*
        mCamera = CameraHandler.getCameraInstance();
        mPreview = new CameraPreview(this, mCamera);
        ((FrameLayout) findViewById(R.id.camera_preview)).addView(mPreview);
        safeToTakePicture = true;
        */
        mCameraHandler = new CameraHandler(this);
        mCameraHandler.open(0);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mCamera != null) {
            mCamera.release();
            mCamera = null;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.http_server, menu);
        return true;
    }

	@Override
    @SuppressWarnings("NewApi")
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v.getId() == R.id.button1) {
            if (!hasAllPesmissions()){
                requestPermissions(NEEDED_PERMISSIONS, 88);
                return;
            }
            if (s != null){
                Toast.makeText(this, "Server already running", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                s = new SocketServer(handler, Integer.valueOf(maxThreads.getText().toString()));
                s.start();
                Toast.makeText(this, "Server running", Toast.LENGTH_SHORT).show();
            } catch (Exception e){
                Toast.makeText(this, "Could not start server", Toast.LENGTH_SHORT).show();
            }
		}
		if (v.getId() == R.id.button2) {
            try {
                s.close();
                try {
                    s.join();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Toast.makeText(this, "Server stopped", Toast.LENGTH_SHORT).show();
                s = null;
            } catch (Exception e){
                Toast.makeText(this, "Server already stopped", Toast.LENGTH_SHORT).show();
            }
		}
	}

    private boolean hasAllPesmissions(){
        if (Build.VERSION.SDK_INT < 23)
            return true;
        for (String permission : NEEDED_PERMISSIONS){
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED)
                return false;
        }
        return true;
    }

    @TargetApi(23)
    private boolean handlePermissionResult(@NonNull String[] permissions, @NonNull int[] grantResults){
        ArrayList<String> notGranted = new ArrayList<>();
        for (int i = 0; i < permissions.length; i++){
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED)
                notGranted.add(permissions[i]);
        }
        if (notGranted.size() == 0)
            return true;
        String[] array = new String[notGranted.size()];
        notGranted.toArray(array);
        requestPermissions(array, 88);
        return false;
    }

    @Override
    @TargetApi(23)
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 88 && handlePermissionResult(permissions, grantResults)){
            try {
                s = new SocketServer(handler, Integer.valueOf(maxThreads.getText().toString()));
            } catch (Exception e){
                Toast.makeText(this, "Could not start server", Toast.LENGTH_SHORT).show();
                return;
            }
            s.start();
            Toast.makeText(this, "Server running", Toast.LENGTH_SHORT).show();
        }
    }

    class MyCameraListener implements CameraListener {
        com.kru13.httpserver.Message message;
        public MyCameraListener(com.kru13.httpserver.Message msg){
            message = msg;
        }

        @Override
        public void onNewImage(ByteBuffer image) {
            synchronized (message) {
                message.setBuffer(ClientThread.getActiveArray(image));
                message.notifyAll();
            }
        }
    }
}
