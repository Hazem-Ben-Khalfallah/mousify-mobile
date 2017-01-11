package com.blacknebula.mousify;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.blacknebula.mousify.util.Logger;

import java.io.DataOutputStream;
import java.net.Socket;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

public class MainActivity extends Activity {

    @InjectView(R.id.button)
    Button sendButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.inject(this);
    }

    @OnClick(R.id.button)
    public void onClick(View view) {
        try {
            Socket socket = new Socket("192.168.122.1", 1755);
            DataOutputStream DOS = new DataOutputStream(socket.getOutputStream());
            DOS.writeUTF("HELLO_WORLD");
            socket.close();
        } catch (Exception e) {
            Logger.error(Logger.Type.MOUSIFY, e);
            Toast.makeText(this, "Error", Toast.LENGTH_SHORT).show();
        }

    }
}
