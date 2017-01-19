package com.blacknebula.mousify.activities;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.DhcpInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.blacknebula.mousify.MousifyApplication;
import com.blacknebula.mousify.R;
import com.blacknebula.mousify.dto.ConnectionInfo;
import com.blacknebula.mousify.event.MotionEvent;
import com.blacknebula.mousify.services.RemoteMousifyIntentService;
import com.blacknebula.mousify.services.ScanNetIntentService;
import com.blacknebula.mousify.util.Logger;
import com.google.common.base.Strings;

import org.parceler.Parcels;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteOrder;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


public class DebugActivity extends Activity {

    private static final int SCAN_NET_REQUEST_CODE = 0;
    private static final int DISCOVER_REQUEST_CODE = 1;

    @InjectView(R.id.networkInfoButton)
    Button networkInfoButton;

    @InjectView(R.id.mousePadButton)
    Button mousePadButton;

    @InjectView(R.id.scanButton)
    Button scanButton;

    @InjectView(R.id.connectButton)
    Button connectButton;

    @InjectView(R.id.disconnectButton)
    Button disconnectButton;

    @InjectView(R.id.discoverButton)
    Button discoverButton;

    @InjectView(R.id.sendButton)
    Button sendButton;

    @InjectView(R.id.testingIpText)
    TextView testingIpText;


    @InjectView(R.id.detectedDevicesText)
    TextView detectedDevicesText;

    @InjectView(R.id.networkInfoText)
    TextView networkInfoText;

    @InjectView(R.id.targetIpAddressEditText)
    EditText targetIpAddressEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.debug);
        ButterKnife.inject(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == SCAN_NET_REQUEST_CODE) {
            switch (resultCode) {
                case ScanNetIntentService.ERROR_CODE:
                    handleError(data);
                    break;
                case ScanNetIntentService.RESULT_CODE:
                    handleSuccess(data);
                    break;
                case ScanNetIntentService.TRY_CODE:
                    handleTry(data);
                    break;
                case ScanNetIntentService.END_CODE:
                    handleEnd(data);
                    break;
            }
        } else if (requestCode == DISCOVER_REQUEST_CODE) {
            switch (resultCode) {
                case ScanNetIntentService.ERROR_CODE:
                    handleError(data);
                    break;
                case ScanNetIntentService.RESULT_CODE:
                    handleSuccess(data);
                    break;
                case ScanNetIntentService.TRY_CODE:
                    handleTry(data);
                    break;
                case ScanNetIntentService.END_CODE:
                    handleEnd(data);
                    break;
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @OnClick(R.id.networkInfoButton)
    public void detectBroadcastAddress(View view) {
        final ConnectionInfo connectionInfo = getNetworkInfo();
        if (connectionInfo != null) {
            networkInfoText.clearComposingText();
            final StringBuilder sb = new StringBuilder();
            sb.append("SSID: ").append(connectionInfo.getSSID()).append("\n")
                    .append("Ip: ").append("" + connectionInfo.getIpAddress()).append("\n")
                    .append("Broadcast Address: ").append(connectionInfo.getBroadcastAddress());
            networkInfoText.setText(sb.toString());
        } else {
            networkInfoText.setText("fail");
        }
    }

    @OnClick(R.id.scanButton)
    public void scanCode(View view) {
        detectedDevicesText.setText("pending ...");
        final String targetIpAddress = targetIpAddressEditText.getText().toString();
        final ConnectionInfo connectionInfo = getNetworkInfo();
        if (connectionInfo == null) {
            Toast.makeText(this, "Connection info cannot be null", Toast.LENGTH_SHORT).show();
            return;
        }
        final String subNet = retrieveSubNet(connectionInfo.getBroadcastAddress());
        final PendingIntent pendingResult = createPendingResult(SCAN_NET_REQUEST_CODE, new Intent(), 0);
        final Intent intent = new Intent(this, ScanNetIntentService.class);
        intent.putExtra(ScanNetIntentService.URL_EXTRA, subNet);
        intent.putExtra(ScanNetIntentService.IP_EXTRA, targetIpAddress);
        intent.putExtra(ScanNetIntentService.PENDING_RESULT_EXTRA, pendingResult);
        startService(intent);
    }

    @OnClick(R.id.discoverButton)
    public void discover(View view) {
        detectedDevicesText.setText("pending ...");
        final PendingIntent pendingResult = createPendingResult(DISCOVER_REQUEST_CODE, new Intent(), 0);

        final Intent intent = new Intent(this, RemoteMousifyIntentService.class);
        intent.setAction(RemoteMousifyIntentService.DISCOVER_ACTION);
        intent.putExtra(ScanNetIntentService.PENDING_RESULT_EXTRA, pendingResult);
        startService(intent);
    }

    @OnClick(R.id.mousePadButton)
    public void openMousePad(View view) {
        final Intent intent = new Intent(this, MousePadActivity.class);
        startActivity(intent);
    }

    @OnClick(R.id.connectButton)
    public void connect(View view) {
        if (Strings.isNullOrEmpty(targetIpAddressEditText.getText().toString())) {
            Toast.makeText(this, "Should set ip address", Toast.LENGTH_SHORT).show();
            return;
        }

        final String ip = targetIpAddressEditText.getText().toString();

        final Intent intent = new Intent(this, RemoteMousifyIntentService.class);
        intent.setAction(RemoteMousifyIntentService.CONNECT_ACTION);
        intent.putExtra(RemoteMousifyIntentService.IP_EXTRA, ip);
        startService(intent);
    }

    @OnClick(R.id.disconnectButton)
    public void disconnect(View view) {
        final Intent intent = new Intent(this, RemoteMousifyIntentService.class);
        intent.setAction(RemoteMousifyIntentService.DISCONNECT_ACTION);
        startService(intent);
    }

    @OnClick(R.id.sendButton)
    public void send(View view) {
        final Intent intent = new Intent(this, RemoteMousifyIntentService.class);
        intent.setAction(RemoteMousifyIntentService.SEND_MOTION_ACTION);
        Parcelable parcelable = Parcels.wrap(new MotionEvent(30, 30));
        intent.putExtra(RemoteMousifyIntentService.MOTION_EXTRA, parcelable);
        startService(intent);
    }

    private void handleSuccess(Intent data) {
        if (detectedDevicesText.getText().toString().contains("pending")) {
            detectedDevicesText.setText("");
        }
        final Parcelable result = data.getParcelableExtra(ScanNetIntentService.HOSTS_EXTRA);
        final String host = Parcels.unwrap(result);
        final String value = detectedDevicesText.getText().toString().isEmpty() ? host : detectedDevicesText.getText() + "\n" + host;
        detectedDevicesText.setText(value);
        targetIpAddressEditText.setText(value);
    }

    private void handleEnd(Intent data) {
        if (detectedDevicesText.getText().toString().contains("pending")) {
            detectedDevicesText.setText("");
        }
        final String finishedMessage = "Finished";
        final String value = detectedDevicesText.getText().toString().isEmpty() ? finishedMessage : detectedDevicesText.getText() + "\n" + finishedMessage;
        detectedDevicesText.setText(value);
    }

    private void handleError(Intent data) {
        Toast.makeText(this, "Error while scanning the net", Toast.LENGTH_SHORT).show();
    }

    private void handleTry(Intent data) {
        final Parcelable result = data.getParcelableExtra(ScanNetIntentService.HOSTS_EXTRA);
        final String host = Parcels.unwrap(result);
        testingIpText.setText("Trying: " + host);
    }


    private String retrieveSubNet(String hostAddress) {
        return hostAddress.substring(0, hostAddress.lastIndexOf("."));
    }

    private ConnectionInfo getNetworkInfo() {
        try {
            final ConnectionInfo connectionInfo = new ConnectionInfo();
            WifiManager wifi = (WifiManager) MousifyApplication.getAppContext().getSystemService(Context.WIFI_SERVICE);
            connectionInfo.setSSID(wifi.getConnectionInfo().getSSID());
            String ipAddress = wifiIpAddress(MousifyApplication.getAppContext());
            connectionInfo.setIpAddress(ipAddress);
            DhcpInfo dhcp = wifi.getDhcpInfo();

            int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
            byte[] quads = new byte[4];
            for (int k = 0; k < 4; k++)
                quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);

            connectionInfo.setBroadcastAddress(InetAddress.getByAddress(quads).getHostAddress());

            return connectionInfo;
        } catch (Exception e) {
            Logger.error(Logger.Type.MOUSIFY, e);
        }
        return null;
    }

    private String wifiIpAddress(Context context) {
        WifiManager wifiManager = (WifiManager) context.getSystemService(WIFI_SERVICE);
        int ipAddress = wifiManager.getConnectionInfo().getIpAddress();

        // Convert little-endian to big-endian if needed
        if (ByteOrder.nativeOrder().equals(ByteOrder.LITTLE_ENDIAN)) {
            ipAddress = Integer.reverseBytes(ipAddress);
        }

        byte[] ipByteArray = BigInteger.valueOf(ipAddress).toByteArray();

        String ipAddressString;
        try {
            ipAddressString = InetAddress.getByAddress(ipByteArray).getHostAddress();
        } catch (UnknownHostException ex) {
            Logger.error(Logger.Type.MOUSIFY, ex);
            ipAddressString = null;
        }

        return ipAddressString;
    }


}
