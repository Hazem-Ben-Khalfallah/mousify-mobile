package com.blacknebula.mousify.services;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Intent;
import android.util.Log;

import com.blacknebula.mousify.util.Logger;

import org.parceler.Parcels;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ScanNetIntentService extends IntentService {

    public static final String HOSTS_EXTRA = "hosts";
    public static final String URL_EXTRA = "url";
    public static final String PENDING_RESULT_EXTRA = "pending_result";
    public static final String PORT_EXTRA = "port";
    public static final int RESULT_CODE = 0;
    public static final int ERROR_CODE = 2;
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
                final String port = intent.getStringExtra(PORT_EXTRA);
                final List<String> hosts = scanSubNet(url, port);
                final Intent result = new Intent();
                result.putExtra(HOSTS_EXTRA, Parcels.wrap(hosts));

                reply.send(this, RESULT_CODE, result);

            } catch (Exception exc) {
                // could do better by treating the different sax/xml exceptions individually
                reply.send(ERROR_CODE);
            }
        } catch (PendingIntent.CanceledException exc) {
            Log.i(TAG, "reply cancelled", exc);
        }
    }

    private List<String> scanSubNet(String subnet, String port) {
        List<String> hosts = new ArrayList<>();


        for (int i = 1; i <= 10; i++) {
            final String address = subnet + "." + String.valueOf(i);
            Logger.info(Logger.Type.MOUSIFY, "Trying: %s:%s", address, port);
            try {
                final DetectedHost detectedHost = isReachable(address, Integer.parseInt(port), 1000);
                if (detectedHost.reached) {
                    hosts.add(detectedHost.hostName);
                    Logger.info(Logger.Type.MOUSIFY, "Spotted: " + detectedHost.hostName);
                }
            } catch (Exception e) {
                Logger.error(Logger.Type.MOUSIFY, e);
            }
        }

        return hosts;
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