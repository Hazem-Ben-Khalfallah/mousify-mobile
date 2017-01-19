package com.blacknebula.mousify.services;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Parcelable;

import com.blacknebula.mousify.BuildConfig;
import com.blacknebula.mousify.MousifyApplication;
import com.blacknebula.mousify.dto.MotionHistory;
import com.blacknebula.mousify.dto.MotionRequest;
import com.blacknebula.mousify.util.Logger;
import com.blacknebula.mousify.util.ViewUtils;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;

import org.parceler.Parcels;

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;

import static com.blacknebula.mousify.services.ScanNetIntentService.HOSTS_EXTRA;
import static com.blacknebula.mousify.services.ScanNetIntentService.PENDING_RESULT_EXTRA;
import static com.blacknebula.mousify.services.ScanNetIntentService.RESULT_CODE;

public class RemoteMousifyIntentService extends IntentService {

    public static final String IP_EXTRA = "ip";
    public static final String DISCOVER_ACTION = "discover";
    public static final String CONNECT_ACTION = "connect";
    public static final String DISCONNECT_ACTION = "disconnect";
    public static final String SEND_ACTION = "send";
    public static final String COORDINATES_EXTRA = "coordinates";
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
                connect(ip);
            } else if (SEND_ACTION.equals(action)) {
                final Parcelable parcelable = intent.getParcelableExtra(COORDINATES_EXTRA);
                final MotionRequest motionRequest = Parcels.unwrap(parcelable);
                send(motionRequest);
            } else if (DISCONNECT_ACTION.equals(action)) {
                disconnect();
            } else if (DISCOVER_ACTION.equals(action)) {
                PendingIntent reply = intent.getParcelableExtra(PENDING_RESULT_EXTRA);
                discoverHosts(reply);
            }


        } catch (Exception ex) {
            // could do better by treating the different sax/xml exceptions individually
            Logger.error(Logger.Type.MOUSIFY, ex, "Error handling action with remote server");
        }
    }

    private void discoverHosts(PendingIntent reply) throws PendingIntent.CanceledException {
        final Client client = new Client();
        final List<InetAddress> inetAddresses = client.discoverHosts(BuildConfig.UDP_PORT, 5000);

        if (inetAddresses.isEmpty()) {
            sendHost(reply, "nothing found!");
        } else {
            for (InetAddress inetAddress : inetAddresses) {
                sendHost(reply, inetAddress.getHostName());
            }
        }
    }

    private void sendHost(PendingIntent replyIntent, String host) throws PendingIntent.CanceledException {
        final Intent result = new Intent();
        result.putExtra(HOSTS_EXTRA, Parcels.wrap(host));
        replyIntent.send(this, RESULT_CODE, result);
    }

    private void disconnect() {
        if (client.isConnected()) {
            client.stop();
            ViewUtils.showToast(MousifyApplication.getAppContext(), "Disconnected");
        } else {
            ViewUtils.showToast(MousifyApplication.getAppContext(), "Already disconnected");
        }
    }

    private void connect(String ip) {
        ViewUtils.showToast(MousifyApplication.getAppContext(), "Connecting");
        try {
            if (client == null) {
                client = new Client();
            }
            if (!client.isConnected()) {
                final Kryo kryo = client.getKryo();
                kryo.register(MotionRequest.class);
                client.start();
                client.connect(5000, InetAddress.getByName(ip), BuildConfig.TCP_PORT, BuildConfig.UDP_PORT);
                ViewUtils.showToast(MousifyApplication.getAppContext(), "connected to " + ip);
            } else {
                ViewUtils.showToast(MousifyApplication.getAppContext(), "Client already connected");
            }
        } catch (IOException e) {
            Logger.error(Logger.Type.MOUSIFY, e, "Could not connect client");
            ViewUtils.showToast(MousifyApplication.getAppContext(), "Connection failed");
        }
    }

    private void send(MotionRequest motionRequest) throws IOException {
        if (MotionHistory.shouldIgnoreMove(motionRequest.getDx(), motionRequest.getDy())) {
            Logger.warn(Logger.Type.MOUSIFY, "Ignore %s, %s", motionRequest.getDx(), motionRequest.getDy());
            return;
        }
        Logger.info(Logger.Type.MOUSIFY, "----> sending %s, %s", motionRequest.getDx(), motionRequest.getDy());

        if (client == null) {
            ViewUtils.showToast(MousifyApplication.getAppContext(), "client should not be null");
            return;
        }

        if (!client.isConnected()) {
            client.reconnect();
        }
        client.sendTCP(motionRequest);

        //update history
        MotionHistory.getInstance().resetStartToCurrentPosition();
    }

}