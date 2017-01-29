package com.blacknebula.mousify.mobile.services;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Parcelable;

import com.blacknebula.mousify.mobile.BuildConfig;
import com.blacknebula.mousify.mobile.MousifyApplication;
import com.blacknebula.mousify.mobile.dto.MotionHistory;
import com.blacknebula.mousify.mobile.event.ClickEvent;
import com.blacknebula.mousify.mobile.event.MotionEvent;
import com.blacknebula.mousify.mobile.event.ScrollEvent;
import com.blacknebula.mousify.mobile.util.Logger;
import com.blacknebula.mousify.mobile.util.ViewUtils;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;

import org.parceler.Parcels;

import java.io.IOException;
import java.net.InetAddress;
import java.util.List;


public class RemoteMousifyIntentService extends IntentService {

    //Actions
    public static final String DISCOVER_ACTION = "discover";
    public static final String CONNECT_ACTION = "connect";
    public static final String DISCONNECT_ACTION = "disconnect";
    public static final String SEND_MOTION_ACTION = "sendMotion";
    public static final String SEND_CLICK_ACTION = "sendClick";
    public static final String SCROLL_ACTION = "sendScroll";
    // Extras
    public static final String IP_EXTRA = "ip";
    public static final String PENDING_RESULT_EXTRA = "pending_result";
    public static final String REPLY_EXTRA = "reply";
    public static final String MOTION_EXTRA = "motion";
    public static final String CLICK_EXTRA = "click";
    public static final String SCROLL_EXTRA = "scroll";
    // intent codes
    public static final int RESULT_CODE = 0;
    public static final int DISCOVER_REQUEST_CODE = 1;
    public static final int CONNECT_REQUEST_CODE = 2;

    private static final String TAG = RemoteMousifyIntentService.class.getSimpleName();

    public static Client client;

    public RemoteMousifyIntentService() {
        super(TAG);
    }

    public static boolean isCoonected() {
        return client != null && client.isConnected();
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
                PendingIntent reply = intent.getParcelableExtra(PENDING_RESULT_EXTRA);
                connect(ip, reply);
            } else if (SEND_MOTION_ACTION.equals(action)) {
                final Parcelable parcelable = intent.getParcelableExtra(MOTION_EXTRA);
                final MotionEvent motionEvent = Parcels.unwrap(parcelable);
                sendMotion(motionEvent);
            } else if (SEND_CLICK_ACTION.equals(action)) {
                final Parcelable parcelable = intent.getParcelableExtra(CLICK_EXTRA);
                final ClickEvent clickEvent = Parcels.unwrap(parcelable);
                sendClick(clickEvent);
            } else if (SCROLL_ACTION.equals(action)) {
                final Parcelable parcelable = intent.getParcelableExtra(SCROLL_EXTRA);
                final ScrollEvent scrollEvent = Parcels.unwrap(parcelable);
                sendScroll(scrollEvent);
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
            sendReply(reply, "");
        } else {
            for (InetAddress inetAddress : inetAddresses) {
                sendReply(reply, inetAddress.getHostName());
            }
        }
    }

    private void connect(String ip, PendingIntent reply) {
        ViewUtils.showToast(MousifyApplication.getAppContext(), "Connecting");
        try {
            if (client == null) {
                client = new Client();
            }
            if (!client.isConnected()) {
                final Kryo kryo = client.getKryo();
                kryo.register(MotionEvent.class);
                kryo.register(ClickEvent.class);
                kryo.register(ScrollEvent.class);
                client.start();
                client.connect(5000, InetAddress.getByName(ip), BuildConfig.TCP_PORT, BuildConfig.UDP_PORT);
                ViewUtils.showToast(MousifyApplication.getAppContext(), "connected to " + ip);
            } else {
                ViewUtils.showToast(MousifyApplication.getAppContext(), "Client already connected");
            }
            sendReply(reply, "Connection successful");
        } catch (IOException e) {
            Logger.error(Logger.Type.MOUSIFY, e, "Could not connect client");
            sendReply(reply, "Connection failed");
        }
    }

    private void disconnect() {
        if (client.isConnected()) {
            client.stop();
            ViewUtils.showToast(MousifyApplication.getAppContext(), "Disconnected");
        } else {
            ViewUtils.showToast(MousifyApplication.getAppContext(), "Already disconnected");
        }
    }

    private void sendMotion(MotionEvent motionEvent) throws IOException {
        if (MotionHistory.shouldIgnoreMove(motionEvent.getDx(), motionEvent.getDy())) {
            Logger.warn(Logger.Type.MOUSIFY, "Ignore %s, %s", motionEvent.getDx(), motionEvent.getDy());
            return;
        }
        Logger.info(Logger.Type.MOUSIFY, "----> sending %s, %s", motionEvent.getDx(), motionEvent.getDy());

        if (client == null) {
            ViewUtils.showToast(MousifyApplication.getAppContext(), "client should not be null");
            return;
        }

        if (!client.isConnected()) {
            client.reconnect();
        }
        client.sendTCP(motionEvent);

        //update history
        MotionHistory.getInstance().resetStartToCurrentPosition();
    }

    private void sendClick(ClickEvent clickEvent) throws IOException {
        if (client == null) {
            ViewUtils.showToast(MousifyApplication.getAppContext(), "client should not be null");
            return;
        }

        if (!client.isConnected()) {
            client.reconnect();
        }
        client.sendTCP(clickEvent);
    }

    private void sendScroll(ScrollEvent scrollEvent) throws IOException {
        if (MotionHistory.shouldIgnoreScroll(scrollEvent.getAmount())) {
            Logger.warn(Logger.Type.MOUSIFY, "Ignore scroll %s", scrollEvent.getAmount());
            return;
        }
        Logger.info(Logger.Type.MOUSIFY, "----> sending scroll %s", scrollEvent.getAmount());

        if (client == null) {
            ViewUtils.showToast(MousifyApplication.getAppContext(), "client should not be null");
            return;
        }

        if (!client.isConnected()) {
            client.reconnect();
        }
        client.sendTCP(scrollEvent);
    }

    private <T> void sendReply(PendingIntent replyIntent, T replyMessage) {
        final Intent result = new Intent();
        result.putExtra(REPLY_EXTRA, Parcels.wrap(replyMessage));
        try {
            replyIntent.send(this, RESULT_CODE, result);
        } catch (PendingIntent.CanceledException e) {
            Logger.error(Logger.Type.MOUSIFY, e, "Could not send back reply %s", replyMessage);
        }
    }

}