package com.example.varunnagaraj.bluchat;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private ListView chatWindow;
    private EditText chatText;
    private Button sendButton;
    private Button scanButton;
    private Button sendListButton;

    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;

    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    private static final int REQUEST_CONNECT_DEVICE_INSECURE = 2;
    private static final int REQUEST_ENABLE_BT = 3;

    private Devices devices;
    private Devices devices1;

    private String names;

    private String connectedDeviceName = null;
    private ArrayAdapter<String> chatArrayAdapter;

    private StringBuffer outStringBuffer;
    private BluetoothAdapter bluetoothAdapter = null;
    private ChatService chatService = null;

    private DeviceDBHandler dbHandler;

    private Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {
            switch (msg.what) {
                case MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case ChatService.STATE_CONNECTED:
                            setStatus(R.string.title_connected_to +
                                    connectedDeviceName);
//                            chatArrayAdapter.clear();
                            break;
                        case ChatService.STATE_CONNECTING:
                            setStatus(R.string.title_connecting);
                            break;
                        case ChatService.STATE_LISTEN:
                        case ChatService.STATE_NONE:
                            setStatus(R.string.title_not_connected);
                            break;
                    }
                    break;
                case MESSAGE_WRITE:
                    byte[] writeBuf = (byte[]) msg.obj;

                    String writeMessage = new String(writeBuf);
                    chatArrayAdapter.add("Me:  " + writeMessage);
                    break;
                case MESSAGE_READ:
                    byte[] readBuf = (byte[]) msg.obj;

                    String readMessage = new String(readBuf, 0, msg.arg1);
                    chatArrayAdapter.add(connectedDeviceName + ":  " + readMessage);
                    break;
                case MESSAGE_DEVICE_NAME:

                    connectedDeviceName = msg.getData().getString(DEVICE_NAME);
                    Toast.makeText(getApplicationContext(),
                            "Connected to " + connectedDeviceName,
                            Toast.LENGTH_SHORT).show();
                    break;
                case MESSAGE_TOAST:
                    Toast.makeText(getApplicationContext(),
                            msg.getData().getString(TOAST), Toast.LENGTH_SHORT)
                            .show();
                    break;
            }
            return false;
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//        dbHandler = new DeviceDBHandler(this,null,null,1);
        if (bluetoothAdapter == null) {
            Toast.makeText(this, "Bluetooth is not available",
                    Toast.LENGTH_LONG).show();
            finish();
            return;
        }


        chatWindow = (ListView) findViewById(R.id.chatWindow);
        chatText = (EditText) findViewById(R.id.chatText);
        scanButton = (Button) findViewById(R.id.scanButton);
        sendButton = (Button) findViewById(R.id.sendButton);
        sendListButton = (Button) findViewById(R.id.sendListButton);

        scanButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        startScan(v);
                    }
                }
        );

        sendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                String message = chatText.getText().toString();
                sendMessage(message);
            }
        });
        ensureDiscoverable();

        sendListButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
//                        sendMessage(names);
                        showToast();
                    }
                }
        );

//        dbHandler = new DeviceDBHandler(this);
//        String hello = "testing Database";
//        String hello2 = "testing Database2";
//        devices = new Devices(hello);
//        devices1 = new Devices(hello2);
//        dbHandler.addDevice(devices);
//        dbHandler.addDevice(devices1);
    }

    public void showToast(){
        String dbString [] = dbHandler.deviceAt(2);
        Toast.makeText(this,"Device Name - "+ dbString[0]+"\n" +"Device Address - "+ dbString[1]+"\n"+"Device RSSI - "+dbString[2],Toast.LENGTH_LONG).show();
    }
    private final void setStatus(int resId) {
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setSubtitle(resId);
    }

    private final void setStatus(CharSequence subTitle) {
        final ActionBar actionBar = getSupportActionBar();
        actionBar.setSubtitle(subTitle);
    }
    private void startScan(View view){
        Intent serverIntent = new Intent(this, ScanDevices.class);
        startActivityForResult(serverIntent, REQUEST_CONNECT_DEVICE_INSECURE);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case REQUEST_CONNECT_DEVICE_INSECURE:
                if (resultCode == Activity.RESULT_OK) {
                    connectDevice(data, false);
                }
                break;
        }
    }

    private void connectDevice(Intent data, boolean secure) {
        String address = data.getExtras().getString(
                ScanDevices.DEVICE_ADDRESS);
        names = data.getExtras().getString(
                ScanDevices.DEVICE_NAMES);
        //create database to store devices
        dbHandler = new DeviceDBHandler(this);
        dbHandler.deletetable();
        dbHandler.createtable();
//        Toast.makeText(this,names,Toast.LENGTH_LONG).show();
//        dbHandler.onUpgrade(<database name .db>); //implement this to update the database when the user starts a new scanning session
        String [] splitNames = names.split("\r?\n");
        int size = splitNames.length;
        for(int i=0;i<size;i=i+3) {
            String a= splitNames[i];
            String b= splitNames[i+1];
            String c= splitNames[i+2];
            devices = new Devices(a,b,c);
            dbHandler.addDevice(devices);
        }
        BluetoothDevice device = bluetoothAdapter.getRemoteDevice(address);
        chatService.connect(device, secure);
    }

    private void ensureDiscoverable() {
        if (bluetoothAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(
                    BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(
                    BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            startActivity(discoverableIntent);
        }
    }

    @Override
    public void onStart() {
        super.onStart();

        if (!bluetoothAdapter.isEnabled()) {
            Intent enableIntent = new Intent(
                    BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableIntent, REQUEST_ENABLE_BT);
            ensureDiscoverable();
        }
        else {
            if (chatService == null)
                setupChat();
        }
    }

    private void sendMessage(String message) {
        if (chatService.getState() != ChatService.STATE_CONNECTED) {
            Toast.makeText(this, R.string.not_connected, Toast.LENGTH_SHORT)
                    .show();
            return;
        }

        if (message.length() > 0) {
            byte[] send = message.getBytes();
            chatService.write(send);

            outStringBuffer.setLength(0);
            chatText.setText(outStringBuffer);
        }
    }

    private TextView.OnEditorActionListener mWriteListener = new TextView.OnEditorActionListener() {
        public boolean onEditorAction(TextView view, int actionId,
                                      KeyEvent event) {
            if (actionId == EditorInfo.IME_NULL
                    && event.getAction() == KeyEvent.ACTION_UP) {
                String message = view.getText().toString();
                sendMessage(message);
            }
            return true;
        }
    };

    private void setupChat() {
        chatArrayAdapter = new ArrayAdapter<String>(this, R.layout.message);
        chatWindow.setAdapter(chatArrayAdapter);

        chatService = new ChatService(this, handler);

        outStringBuffer = new StringBuffer("");
    }



    @Override
    public synchronized void onResume() {
        super.onResume();

        if (chatService != null) {
            if (chatService.getState() == ChatService.STATE_NONE) {
                chatService.start();
            }
        }
    }

    @Override
    public synchronized void onPause() {
        super.onPause();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroy() {
        dbHandler.close();
        super.onDestroy();
        if (chatService != null)
            chatService.stop();
    }
}
