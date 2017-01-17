package com.blacknebula.mousify.services;

import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;

import com.blacknebula.mousify.BuildConfig;
import com.blacknebula.mousify.dto.SomeRequest;
import com.blacknebula.mousify.util.Logger;
import com.blacknebula.mousify.util.MousifyApplication;
import com.blacknebula.mousify.util.ViewUtils;
import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.FrameworkMessage;

import org.parceler.Parcels;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketTimeoutException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Collections;
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
    public static final String MESSAGE_EXTRA = "message";
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
                final String msg = intent.getStringExtra(MESSAGE_EXTRA);
                send(msg);
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

    /**
     * Broadcasts a UDP message on the LAN to discover any running servers.
     *
     * @param udpPort       The UDP port of the server.
     * @param timeoutMillis The number of milliseconds to wait for a response.
     */
    public List<InetAddress> discoverHosts(int udpPort, int timeoutMillis) {
        List<InetAddress> hosts = new ArrayList<>();
        DatagramSocket socket = null;
        try {
            socket = new DatagramSocket(udpPort);
            broadcast(udpPort, socket);
            socket.setSoTimeout(timeoutMillis);
            while (true) {
                DatagramPacket packet = new DatagramPacket(new byte[0], 0);
                try {
                    socket.receive(packet);
                } catch (SocketTimeoutException ex) {
                    Logger.info(Logger.Type.KRYONET, "Host discovery timed out.");
                    return hosts;
                }
                Logger.info(Logger.Type.KRYONET, "Discovered server: %s", packet.getAddress());
                hosts.add(packet.getAddress());
            }
        } catch (IOException ex) {
            Logger.error(Logger.Type.KRYONET, ex, "Host discovery failed.");
        } finally {
            if (socket != null) socket.close();
        }
        return hosts;
    }

    private void broadcast(int udpPort, DatagramSocket socket) throws IOException {
        ByteBuffer dataBuffer = ByteBuffer.allocate(64);
        client.getSerialization().write(null, dataBuffer, new FrameworkMessage.DiscoverHost());
        dataBuffer.flip();
        byte[] data = new byte[dataBuffer.limit()];
        dataBuffer.get(data);
        for (NetworkInterface iface : Collections.list(NetworkInterface.getNetworkInterfaces())) {
            for (InetAddress address : Collections.list(iface.getInetAddresses())) {
                try {
                    // Java 1.5 doesn't support getting the subnet mask, so try the two most common.
                    byte[] ip = address.getAddress();
                    Logger.info(Logger.Type.KRYONET, "-> %s", address.getHostAddress());
                    ip[3] = -1; // 255.255.255.0
                    socket.setBroadcast(true);
                    DatagramPacket packet = new DatagramPacket(data, data.length, getBroadcastAddress(), udpPort);
                    socket.send(packet);
                } catch (Exception e) {
                    Logger.error(Logger.Type.KRYONET, e, "Error  in network interface %s", iface.getName());
                }
            }
        }
        Logger.info(Logger.Type.KRYONET, "Broadcasted host discovery on port: %s", udpPort);
    }

    InetAddress getBroadcastAddress() throws IOException {
        WifiManager wifi = (WifiManager) MousifyApplication.getAppContext().getSystemService(Context.WIFI_SERVICE);
        DhcpInfo dhcp = wifi.getDhcpInfo();
        // handle null somehow

        int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
        byte[] quads = new byte[4];
        for (int k = 0; k < 4; k++)
            quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);
        return InetAddress.getByAddress(quads);
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
                kryo.register(SomeRequest.class);
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

    private void send(String message) throws IOException {
        if (client == null) {
            ViewUtils.showToast(MousifyApplication.getAppContext(), "client should not be null");
            return;
        }

        if (!client.isConnected()) {
            client.reconnect();
        }
        SomeRequest request = new SomeRequest();
        request.text = message;
        client.sendTCP(request);
        ViewUtils.showToast(MousifyApplication.getAppContext(), "Message sent");
    }

}