package ntu.mil.simple_grpc;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.gson.Gson;
import com.interaction.robot.interaction.ConnectReply;
import com.interaction.robot.interaction.ConnectRequest;
import com.interaction.robot.interaction.InteractGrpc;

import androidx.appcompat.app.AppCompatActivity;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class SimpleGrpcActivity extends AppCompatActivity {
    public final static String TAG = SimpleGrpcActivity.class.getSimpleName();
    // gRPC Object
    private InteractGrpc.InteractBlockingStub mInteracter;

    // UI Objects
    private EditText ip_1, ip_2, ip_3, ip_4, host_port;
    private String mIp;
    private int mPort;
    private Button mBtnConnect;
    private TextView mTextState, mLocalIp;
    private String mConnectState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupUI();
    }

    private void setupUI() {
        Log.e(TAG, "Setup UI...");
        ip_1 = (EditText)findViewById(R.id.ip1);
        ip_2 = (EditText)findViewById(R.id.ip2);
        ip_3 = (EditText)findViewById(R.id.ip3);
        ip_4 = (EditText)findViewById(R.id.ip4);
        host_port = (EditText)findViewById(R.id.port);


        mLocalIp = (TextView)findViewById(R.id.text_local_ip);
        mLocalIp.setText("Your Local IP: " + getLocalIpAddress());

        mTextState = (TextView)findViewById(R.id.text_connect_state);
        mBtnConnect = (Button) findViewById(R.id.btn_connect);
        mBtnConnect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                connect();
            }
        });
    }

    public void connect(){
        mIp = ip_1.getText().toString() + "."
                + ip_2.getText().toString() + "."
                + ip_3.getText().toString() + "."
                + ip_4.getText().toString();
        mPort = Integer.parseInt(host_port.getText().toString());

        Log.i(TAG,"Attempting to connect to server with ip " + mIp);

        try {
            ManagedChannel managedChannel = ManagedChannelBuilder.forAddress(mIp, mPort)
                    .usePlaintext(true)
                    .build();
            mInteracter = InteractGrpc.newBlockingStub(managedChannel);

            String jsonHandshake = new Gson().toJson(new ServerCommand("Android APP", mLocalIp.getText().toString()));
            ConnectRequest connectRequest = ConnectRequest.newBuilder().setStatus(jsonHandshake).build();
            ConnectReply connectReply = mInteracter.simpleConnect(connectRequest);

            mConnectState = connectReply.getStatus();
            mTextState.setText(mConnectState);

        } catch (Exception e) {
            Log.e(TAG, "connect() Fail: " + e);
            mTextState.setText("connect() Fail: " + e);
        }
    }

    public String getLocalIpAddress() {
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements();) {
                NetworkInterface intf = en.nextElement();
                for (Enumeration<InetAddress> enumIpAddr = intf.getInetAddresses(); enumIpAddr.hasMoreElements();) {
                    InetAddress inetAddress = enumIpAddr.nextElement();
                    if (!inetAddress.isLoopbackAddress()&& inetAddress instanceof Inet4Address) { return inetAddress.getHostAddress(); }
                }
            }
        } catch (SocketException ex) {
            Log.e(TAG, ex.toString());
        }
        return null;
    }
}
