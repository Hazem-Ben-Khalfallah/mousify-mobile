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

import com.blacknebula.mousify.R;
import com.blacknebula.mousify.dto.ConnectionInfo;
import com.blacknebula.mousify.services.ScanNetIntentService;
import com.blacknebula.mousify.util.Logger;
import com.blacknebula.mousify.util.MousifyApplication;

import org.parceler.Parcels;

import java.math.BigInteger;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.ByteOrder;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


public class MainActivity extends Activity {

    private static final int SCAN_NET_REQUEST_CODE = 0;

    @InjectView(R.id.networkInfoButton)
    Button networkInfoButton;

    @InjectView(R.id.scanButton)
    Button scanButton;

    @InjectView(R.id.detectedDevicesText)
    TextView detectedDevicesText;

    @InjectView(R.id.networkInfoText)
    TextView networkInfoText;

    @InjectView(R.id.portEditText)
    EditText portEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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
        if (!portEditText.getFreezesText()) {
            portEditText.setText("80");
        }
        final String portNumber = portEditText.getText().toString();
        final ConnectionInfo connectionInfo = getNetworkInfo();
        if (connectionInfo == null) {
            Toast.makeText(this, "Connection info cannot be null", Toast.LENGTH_SHORT).show();
            return;
        }
        final String subNet = retrieveSubNet(connectionInfo.getBroadcastAddress());
        PendingIntent pendingResult = createPendingResult(SCAN_NET_REQUEST_CODE, new Intent(), 0);
        Intent intent = new Intent(getApplicationContext(), ScanNetIntentService.class);
        intent.putExtra(ScanNetIntentService.URL_EXTRA, subNet);
        intent.putExtra(ScanNetIntentService.PORT_EXTRA, portNumber);
        intent.putExtra(ScanNetIntentService.PENDING_RESULT_EXTRA, pendingResult);
        startService(intent);
    }

    private void handleSuccess(Intent data) {
        Parcelable result = data.getParcelableExtra(ScanNetIntentService.HOSTS_EXTRA);
        List<String> hosts = Parcels.unwrap(result);
        detectedDevicesText.setText(from(hosts));
    }

    private void handleError(Intent data) {
        Toast.makeText(this, "Error while scanning the net", Toast.LENGTH_SHORT).show();
    }


    private String retrieveSubNet(String hostAddress) {
        return hostAddress.substring(0, hostAddress.lastIndexOf("."));
    }

    private String from(List<String> data) {
        if (data.isEmpty()) {
            return "no host found";
        }
        final StringBuilder stringBuilder = new StringBuilder();
        for (String s : data) {
            stringBuilder.append(s).append("\n");
        }
        return stringBuilder.toString();
    }

    private ConnectionInfo getNetworkInfo() {
        try {
            final ConnectionInfo connectionInfo = new ConnectionInfo();
            WifiManager wifi = (WifiManager) MousifyApplication.getAppContext().getSystemService(Context.WIFI_SERVICE);
            connectionInfo.setSSID(wifi.getConnectionInfo().getSSID());
            String ipAddress = wifiIpAddress(MousifyApplication.getAppContext());
            connectionInfo.setIpAddress(ipAddress);
            DhcpInfo dhcp = wifi.getDhcpInfo();
            // handle null somehow

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

        // Convert little-endian to big-endianif needed
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
