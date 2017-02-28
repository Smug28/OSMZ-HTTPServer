package com.kru13.httpserver;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.os.*;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.concurrent.Semaphore;

public class HttpServerActivity extends AppCompatActivity implements OnClickListener {

	private SocketServer s;
    private TextView log;
    private ScrollView scrollView;
    private TextInputEditText maxThreads;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            String txt = log.getText() != null ? log.getText().toString() : "";
            log.setText(txt + ((com.kru13.httpserver.Message) msg.obj).toString() + "\n");
            scrollView.fullScroll(View.FOCUS_DOWN);
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
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.http_server, menu);
        return true;
    }

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		if (v.getId() == R.id.button1) {
            if (Build.VERSION.SDK_INT >= 23 && checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 88);
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

    @Override
    @TargetApi(23)
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 88 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
            try {
                s = new SocketServer(handler, Integer.valueOf(maxThreads.getText().toString()));
            } catch (Exception e){
                Toast.makeText(this, "Could not start server", Toast.LENGTH_SHORT).show();
                return;
            }
            s.start();
            Toast.makeText(this, "Server running", Toast.LENGTH_SHORT).show();
        }
        else if (requestCode == 88){
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 88);
        }
    }
}
