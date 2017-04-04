package com.kru13.httpserver;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.camera2.CameraManager;
import android.os.*;
import android.os.Message;
import android.preference.PreferenceManager;
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
    private TextInputEditText maxThreads;
    private MyBroadcastReceiver receiver = new MyBroadcastReceiver();

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
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(receiver, new IntentFilter(HttpService.ACTION_LOG));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(receiver);
    }

    @Override
    protected void onStop() {
        super.onStop();
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
            if (HttpService.isRunnting){
                Toast.makeText(this, "Server already running", Toast.LENGTH_SHORT).show();
                return;
            }
            try {
                startService();
                Toast.makeText(this, "Server running", Toast.LENGTH_SHORT).show();
            } catch (Exception e){
                Toast.makeText(this, "Could not start server", Toast.LENGTH_SHORT).show();
            }
		}
		if (v.getId() == R.id.button2) {
            try {
                stopService(new Intent(getApplicationContext(), HttpService.class));
                Toast.makeText(this, "Server stopped", Toast.LENGTH_SHORT).show();
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
                startService();
            } catch (Exception e){
                Toast.makeText(this, "Could not start server", Toast.LENGTH_SHORT).show();
                return;
            }
            Toast.makeText(this, "Server running", Toast.LENGTH_SHORT).show();
        }
    }

    private void startService(){
        Intent i = new Intent(getApplicationContext(), HttpService.class);
        PreferenceManager.getDefaultSharedPreferences(this).edit().putInt("threads", Integer.valueOf(maxThreads.getText().toString())).commit();
        startService(i);
    }

    private class MyBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if (HttpService.ACTION_LOG.equals(intent.getAction())){
                log.setText(log.getText().toString() + intent.getStringExtra("log"));
                scrollView.fullScroll(View.FOCUS_DOWN);
            }
        }
    }
}
