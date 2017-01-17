package com.blacknebula.mousify.services;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.util.Log;

import com.blacknebula.mousify.BuildConfig;
import com.blacknebula.mousify.util.Logger;
import com.google.common.base.Strings;

import org.parceler.Parcels;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class ScanNetIntentService extends IntentService {

    public static final String HOSTS_EXTRA = "hosts";
    public static final String URL_EXTRA = "url";
    public static final String PENDING_RESULT_EXTRA = "pending_result";
    public static final String PORT_EXTRA = "port";
    public static final String IP_EXTRA = "ip";
    public static final int RESULT_CODE = 0;
    public static final int TRY_CODE = 1;
    public static final int ERROR_CODE = 2;
    public static final int END_CODE = 3;
    private static final String TAG = ScanNetIntentService.class.getSimpleName();

    public ScanNetIntentService() {
        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        PendingIntent reply = intent.getParcelableExtra(PENDING_RESULT_EXTRA);

        try {
            try {
                final String url = intent.getStringExtra(URL_EXTRA);
                final String targetIpAddress = intent.getStringExtra(IP_EXTRA);
                scanSubNet(url, targetIpAddress, reply);


            } catch (Exception exc) {
                // could do better by treating the different sax/xml exceptions individually
                reply.send(ERROR_CODE);
            }
        } catch (PendingIntent.CanceledException exc) {
            Log.i(TAG, "reply cancelled", exc);
        }
    }

    private void sendHosts(PendingIntent replyIntent, String host) throws PendingIntent.CanceledException {
        final Intent result = new Intent();
        result.putExtra(HOSTS_EXTRA, Parcels.wrap(host));
        replyIntent.send(this, RESULT_CODE, result);
    }

    private void sendTry(PendingIntent replyIntent, String host) throws PendingIntent.CanceledException {
        final Intent result = new Intent();
        result.putExtra(HOSTS_EXTRA, Parcels.wrap(host));
        replyIntent.send(this, TRY_CODE, result);
    }

    private void sendEnd(PendingIntent replyIntent) throws PendingIntent.CanceledException {
        final Intent result = new Intent();
        replyIntent.send(this, END_CODE, result);
    }

    private void scanSubNet(String subnet, String targetIpAddress, PendingIntent reply) {
        try {
            if (!Strings.isNullOrEmpty(targetIpAddress)) {
                verifyAddress(targetIpAddress, reply);
            } else {
                for (int i = 1; i <= 254; i++) {
                    final String address = subnet + "." + String.valueOf(i);
                    verifyAddress(address, reply);
                }
            }
            sendEnd(reply);
        } catch (Exception e) {
            Logger.error(Logger.Type.MOUSIFY, e);
        }

    }

    private void verifyAddress(String address, PendingIntent reply) throws PendingIntent.CanceledException {
        final int port = BuildConfig.TCP_PORT;
        Logger.info(Logger.Type.MOUSIFY, "Trying: %s:%s", address, port);
        sendTry(reply, String.format("%s:%s", address, port));

        final DetectedHost detectedHost = isReachable(address, port, 1000);
        if (detectedHost.reached) {
            sendHosts(reply, detectedHost.hostName);
            Logger.info(Logger.Type.MOUSIFY, "Spotted: " + detectedHost.hostName);
        }
    }

    private DetectedHost isReachable(String addr, int openPort, int timeOutMillis) {
        // Any Open port on other machine
        // openPort =  80 Linux / 139 windows
        try {
            try (Socket soc = new Socket()) {
                soc.connect(new InetSocketAddress(addr, openPort), timeOutMillis);
            }
            return new DetectedHost(true, addr);
        } catch (IOException ex) {
            return new DetectedHost();
        }
    }

    class DetectedHost {
        public boolean reached;
        public String hostName;

        public DetectedHost() {
            this.reached = false;
        }

        public DetectedHost(boolean reached, String hostName) {
            this.reached = reached;
            this.hostName = hostName;
        }
    }

}