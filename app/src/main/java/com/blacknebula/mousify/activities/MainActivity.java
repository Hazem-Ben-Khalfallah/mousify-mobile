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
import android.widget.TextView;
import android.widget.Toast;

import com.blacknebula.mousify.R;
import com.blacknebula.mousify.services.ScanNetIntentService;
import com.blacknebula.mousify.util.Logger;
import com.blacknebula.mousify.util.MousifyApplication;

import org.parceler.Parcels;

import java.io.OutputStream;
import java.net.InetAddress;
import java.util.List;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;


public class MainActivity extends Activity {

    private static final int SCAN_NET_REQUEST_CODE = 0;
    @InjectView(R.id.send)
    Button sendButton;

    @InjectView(R.id.connect)
    Button connectButton;

    @InjectView(R.id.devices)
    TextView devicesText;

    private OutputStream outputStream;

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

    @OnClick(R.id.connect)
    public void connect(View view) {
        final InetAddress broadcastAddress = getBroadcastAddress();
        final String subNet = retrieveSubNet(broadcastAddress.getHostAddress());
        PendingIntent pendingResult = createPendingResult(SCAN_NET_REQUEST_CODE, new Intent(), 0);
        Intent intent = new Intent(getApplicationContext(), ScanNetIntentService.class);
        intent.putExtra(ScanNetIntentService.URL_EXTRA, subNet);
        intent.putExtra(ScanNetIntentService.PENDING_RESULT_EXTRA, pendingResult);
        startService(intent);
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

    private void handleSuccess(Intent data) {
        Parcelable result = data.getParcelableExtra(ScanNetIntentService.HOSTS_EXTRA);
        List<String> hosts = Parcels.unwrap(result);
        devicesText.setText(from(hosts));
    }

    private void handleError(Intent data) {
        Toast.makeText(this, "Error while scanning the net", Toast.LENGTH_SHORT).show();
    }

    private String retrieveSubNet(String hostAddress) {
        return hostAddress.substring(0, hostAddress.lastIndexOf("."));
    }


    @OnClick(R.id.send)
    public void send(View view) {
        final InetAddress broadcastAddress = getBroadcastAddress();
        devicesText.setText(broadcastAddress == null ? "fail" : broadcastAddress.getHostAddress());
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

    InetAddress getBroadcastAddress() {
        try {
            WifiManager wifi = (WifiManager) MousifyApplication.getAppContext().getSystemService(Context.WIFI_SERVICE);
            DhcpInfo dhcp = wifi.getDhcpInfo();
            // handle null somehow

            int broadcast = (dhcp.ipAddress & dhcp.netmask) | ~dhcp.netmask;
            byte[] quads = new byte[4];
            for (int k = 0; k < 4; k++)
                quads[k] = (byte) ((broadcast >> k * 8) & 0xFF);

            return InetAddress.getByAddress(quads);
        } catch (Exception e) {
            Logger.error(Logger.Type.MOUSIFY, e);
        }
        return null;
    }

}
