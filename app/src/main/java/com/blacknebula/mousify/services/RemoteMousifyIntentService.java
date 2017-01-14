package com.blacknebula.mousify.services;

import android.app.IntentService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.blacknebula.mousify.dto.SomeRequest;
import com.blacknebula.mousify.util.Logger;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;

import java.io.IOException;
import java.net.InetAddress;

public class RemoteMousifyIntentService extends IntentService {

    public static final String ACTION_EXTRA = "action";
    public static final String PORT_EXTRA = "port";
    public static final String IP_EXTRA = "ip";
    public static final String ACTION_CONNECT = "connect";
    public static final String SEND_ACTION = "com.blacknebula.mousify.services.RemoteMousifyIntentService.SEND";
    public static final String MESSAGE_EXTRA = "message";
    private static final int DEFAULT_PORT = 18080;
    private static final String TAG = RemoteMousifyIntentService.class.getSimpleName();

    Client client;
    RemoteMousifyReceiver remoteMousifyReceiver;

    public RemoteMousifyIntentService() {
        super(TAG);
    }

    @Override
    public void onCreate() {
        remoteMousifyReceiver = new RemoteMousifyReceiver();
        super.onCreate();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        try {
            final String action = intent.getStringExtra(ACTION_EXTRA);
            if (ACTION_CONNECT.equals(action)) {
                final String ip = intent.getStringExtra(IP_EXTRA);
                final int port = intent.getIntExtra(PORT_EXTRA, DEFAULT_PORT);
                connect(ip, port);
            }


        } catch (Exception exc) {
            // could do better by treating the different sax/xml exceptions individually
            Logger.error(Logger.Type.MOUSIFY, "Error handling action with remote server", exc);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Toast.makeText(getApplicationContext(),
                "onStartCommand", Toast.LENGTH_LONG).show();

        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(SEND_ACTION);
        registerReceiver(remoteMousifyReceiver, intentFilter);

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        Toast.makeText(getApplicationContext(),
                "destroy", Toast.LENGTH_LONG).show();
        unregisterReceiver(remoteMousifyReceiver);
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
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

    public class RemoteMousifyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(getApplicationContext(),
                    "receive", Toast.LENGTH_LONG).show();
            final String action = intent.getAction();
            if (SEND_ACTION.equals(action)) {
                final String msg = intent.getStringExtra(MESSAGE_EXTRA);
                send(msg);
            } else if (ACTION_CONNECT.equals(action)) {
                final String ip = intent.getStringExtra(IP_EXTRA);
                final int port = intent.getIntExtra(PORT_EXTRA, DEFAULT_PORT);
                connect(ip, port);
            }
        }
    }


}