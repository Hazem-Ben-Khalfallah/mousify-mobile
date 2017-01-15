package com.blacknebula.mousify.services;

import android.app.IntentService;
import android.content.Intent;
import android.widget.Toast;

import com.blacknebula.mousify.dto.SomeRequest;
import com.blacknebula.mousify.util.Logger;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;

import java.io.IOException;
import java.net.InetAddress;

public class RemoteMousifyIntentService extends IntentService {

    public static final String PORT_EXTRA = "port";
    public static final String IP_EXTRA = "ip";
    public static final String CONNECT_ACTION = "connect";
    public static final String SEND_ACTION = "send";
    public static final String MESSAGE_EXTRA = "message";
    private static final int DEFAULT_PORT = 18080;
    private static final String TAG = RemoteMousifyIntentService.class.getSimpleName();

    public static Client client;

    public RemoteMousifyIntentService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            final String action = intent.getAction();
            if (CONNECT_ACTION.equals(action)) {
                final String ip = intent.getStringExtra(IP_EXTRA);
                final int port = intent.getIntExtra(PORT_EXTRA, DEFAULT_PORT);
                connect(ip, port);
            } else if (SEND_ACTION.equals(action)) {
                final String msg = intent.getStringExtra(MESSAGE_EXTRA);
                send(msg);
            }


        } catch (Exception exc) {
            // could do better by treating the different sax/xml exceptions individually
            Logger.error(Logger.Type.MOUSIFY, "Error handling action with remote server", exc);
        }
    }

    private void connect(String ip, int port) {
        try {
            if (client == null) {
                client = new Client();
                final Kryo kryo = client.getKryo();
                kryo.register(SomeRequest.class);
                client.start();
                client.connect(5000, InetAddress.getByName(ip), port);
            } else {
                if (!client.isConnected()) {
                    client.reconnect();
                } else {
                    Toast.makeText(getApplicationContext(), "Client already connected", Toast.LENGTH_LONG).show();
                }
            }
        } catch (IOException e) {
            Logger.error(Logger.Type.MOUSIFY, "Could not connect client");
        }
    }

    private void send(String message) {
        if (client == null) {
            Logger.error(Logger.Type.MOUSIFY, "client should not be null");
            return;
        }

        if (!client.isConnected()) {
            Logger.error(Logger.Type.MOUSIFY, "client should not be connected");
            return;
        }
        SomeRequest request = new SomeRequest();
        request.text = message;
        client.sendTCP(request);
    }

}