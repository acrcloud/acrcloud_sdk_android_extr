package com.acrcloud.demo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.acrcloud.utils.ACRCloudExtrTool;
import com.acrcloud.utils.ACRCloudRecognizer;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    private TextView mResult;

    private String path = "";
    private static final String TAG = "ACRCloud";


    private Handler mHandler = new Handler(Looper.getMainLooper()) {

        public void handleMessage(android.os.Message msg) {
            switch (msg.what) {
                case 1:
                    String res = (String) msg.obj;
                    mResult.setText(res);
                    break;

                default:
                    break;
            }
        };
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.verifyPermissions();

        ACRCloudExtrTool.setDebug();

        path = Environment.getExternalStorageDirectory().toString()
                + "/acrcloud/model";

        Log.e(TAG, path);

        File file = new File(path);
        if(!file.exists()){
            file.mkdirs();
        }

        mResult = (TextView) findViewById(R.id.result);

        Button recBtn = (Button) findViewById(R.id.rec);
        recBtn.setText(getResources().getString(R.string.rec));

        findViewById(R.id.rec).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View arg0) {
                rec();
            }
        });
    }

    class RecThread extends Thread {

        public void run() {
            Map<String, Object> config = new HashMap<String, Object>();
            // Replace "xxxxxxxx" below with your project's access_key and access_secret.
            config.put("access_key", "");
            config.put("access_secret", "");
            config.put("host", "");
            config.put("timeout", 5);

            ACRCloudRecognizer re = new ACRCloudRecognizer(config);
            String filePath = path + "/test.mp3";
            File file = new File(filePath);
            if (file.canRead()) {
                Log.e(TAG, "can read");
            } else {
                Log.e(TAG, "can not read");
                return;
            }
            String result = re.recognizeByFile(filePath, 10);
            Log.e(TAG, result);

            /*File file = new File(path + "/test.mp3");
            byte[] buffer = new byte[3 * 1024 * 1024];
            if (!file.exists()) {
                return;
            }
            FileInputStream fin = null;
            int bufferLen = 0;
            try {
                fin = new FileInputStream(file);
                bufferLen = fin.read(buffer, 0, buffer.length);
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                try {
                    if (fin != null) {
                        fin.close();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            System.out.println("bufferLen=" + bufferLen);

            if (bufferLen <= 0)
                return;

            String result = re.recognizeByFileBuffer(buffer, bufferLen, 0);*/

            try {
                Message msg = new Message();
                msg.obj = result;

                msg.what = 1;
                mHandler.sendMessage(msg);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void rec() {
        new RecThread().start();
    }

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS = {
            Manifest.permission.ACCESS_NETWORK_STATE,
            Manifest.permission.ACCESS_WIFI_STATE,
            Manifest.permission.INTERNET,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_EXTERNAL_STORAGE
    };
    public void verifyPermissions() {
        for (int i=0; i<PERMISSIONS.length; i++) {
            int permission = ActivityCompat.checkSelfPermission(this, PERMISSIONS[i]);
            if (permission != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, PERMISSIONS,
                        REQUEST_EXTERNAL_STORAGE);
                break;
            }
        }
    }
}