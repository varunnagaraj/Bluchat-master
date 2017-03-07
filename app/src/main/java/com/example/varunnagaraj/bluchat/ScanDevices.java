package com.example.varunnagaraj.bluchat;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;
import java.util.StringTokenizer;

/**
 * Created by Varun Nagaraj on 27-02-2017.
 */

public class ScanDevices  extends Activity{

    private TextView secondHopDeviceTitle, newDeviceTitle;
    private ListView secondHopDeviceList, newDeviceList;
    private Button scanDevicesButton;
    private BluetoothAdapter bluetoothAdapter;
    private ArrayAdapter<String> secondHopDevicesArrayAdapter;
    private ArrayAdapter<String> newDevicesArrayAdapter;

    private TextView progressText;
    private ProgressBar spinner;

    public DeviceDBHandler dbHandler;

//    Handler handler = new Handler(){
//        @Override
//        public void handleMessage(Message msg){
//            updateDB();
//        }
//    };

    private String dbNames2;

    public static String DEVICE_ADDRESS = "deviceAddress";
    public static String DEVICE_2_ADDRESS = "device2Address";
    public static String DEVICE_NAMES = "devicename";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.scan_devices);
        dbHandler = new DeviceDBHandler(this);

        // Quick permission check
        int permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
        permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
        if (permissionCheck != 0) {

            this.requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 1001); //Any number
        }
//        startDiscovery();
        secondHopDeviceTitle = (TextView) findViewById(R.id.secondHopDeviceTitle);
        newDeviceTitle = (TextView) findViewById(R.id.newDeviceTitle);

        secondHopDeviceList = (ListView) findViewById(R.id.secondHopDeviceList);
        newDeviceList= (ListView) findViewById(R.id.newDeviceList);

        scanDevicesButton = (Button) findViewById(R.id.scanDevicesButton);

        scanDevicesButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dbHandler.deletetable1();
                        dbHandler.createtable1();
//                        newDeviceList.clearChoices();
                        startDiscovery();
                        scanDevicesButton.setVisibility(View.GONE);
//                        waitforscanning(v);
                    }
                }
        );



        secondHopDeviceList.setOnItemClickListener(mDeviceClickListener2);
        newDeviceList.setOnItemClickListener(mDeviceClickListener);

        Bundle dbNames1 = getIntent().getExtras();
        dbNames2 = dbNames1.getString("serverIntent");
//        Toast.makeText(this,dbNames2,Toast.LENGTH_LONG).show();

        initializeValues();
    }


//    public void waitforscanning(View view){
//
//        Runnable r = new Runnable() {
//            @Override
//            public void run() {
//                long futureTime = System.currentTimeMillis() + 16000;
//                while(System.currentTimeMillis() < futureTime){
//                    synchronized (this){
//                        try{
//                            wait(futureTime-System.currentTimeMillis());
//                        }catch(Exception e){}
//                    }
//                }
//                handler.sendEmptyMessage(0);
//            }
//        };
//
//        Thread waitThread = new Thread(r);
//        waitThread.start();
//    }

//    public void updateDB(){
//        int count = newDevicesArrayAdapter.getCount();
//        String devicename = null;
//        for(int i=0;i<count;i++){
//            devicename = newDevicesArrayAdapter.getItem(i);
//            Devices devices = new Devices(devicename," "," ");
////            dbHandler.addDevice(devices);
//        }
////        Toast.makeText(this, devicename, Toast.LENGTH_SHORT).show();
//    }
    private void startDiscovery() {
//        setProgressBarIndeterminateVisibility(true);
        setTitle(R.string.scanning);
        spinner = (ProgressBar) findViewById(R.id.progressBar);
        progressText = (TextView) findViewById(R.id.progressText);
        newDeviceTitle.setVisibility(View.VISIBLE);

        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
        }
        spinner.setVisibility(View.VISIBLE);
        progressText.setVisibility(View.VISIBLE);
        bluetoothAdapter.startDiscovery();
    }

    private AdapterView.OnItemClickListener mDeviceClickListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            bluetoothAdapter.cancelDiscovery();
            MainActivity.connectFlag = 1;
            String info = ((TextView) v).getText().toString();
            String devicedata [] = info.split("\r?\n");
//            String address = info.substring(info.length() - 17);
            String address = devicedata[1];
//            int count = newDevicesArrayAdapter.getCount();
//            String devicename = "";
//            for(int i=0;i<count;i++){
//                devicename += newDevicesArrayAdapter.getItem(i) + "\n";
////                Devices devices = new Devices(devicename," "," ");
////            dbHandler.addDevice(devices);
//            }


            Intent intent = new Intent();
            intent.putExtra(DEVICE_ADDRESS, address);
            intent.putExtra(DEVICE_2_ADDRESS, "");
//            intent.putExtra(DEVICE_NAMES, devicename);

            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    };

    private AdapterView.OnItemClickListener mDeviceClickListener2 = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> av, View v, int arg2, long arg3) {
            bluetoothAdapter.cancelDiscovery();
            MainActivity.connectFlag = 2;
            String info = ((TextView) v).getText().toString();
            String devicedata [] = info.split("\r?\n");
//            String address = info.substring(info.length() - 17);
            String address = devicedata[3];
            String addressFinal = devicedata[1];
            int count = newDevicesArrayAdapter.getCount();


            Intent intent = new Intent();
            intent.putExtra(DEVICE_ADDRESS, address);
            intent.putExtra(DEVICE_2_ADDRESS, addressFinal);
//            intent.putExtra(DEVICE_NAMES, devicename);

            setResult(Activity.RESULT_OK, intent);
            finish();
        }
    };

    private void initializeValues() {
        secondHopDevicesArrayAdapter = new ArrayAdapter<String>(this,
                R.layout.device_name);
        newDevicesArrayAdapter = new ArrayAdapter<String>(this,
                R.layout.device_name);

        secondHopDeviceList.setAdapter(secondHopDevicesArrayAdapter);
        newDeviceList.setAdapter(newDevicesArrayAdapter);

        // Register for broadcasts when a device is discovered
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(discoveryFinishReceiver, filter);

        // Register for broadcasts when discovery has finished
        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        registerReceiver(discoveryFinishReceiver, filter);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
//        Set<BluetoothDevice> pairedDevices = bluetoothAdapter
//                .getBondedDevices();

//        // If there are paired devices, add each one to the ArrayAdapter
//        if (pairedDevices.size() > 0) {
//            pairedDeviceTitle.setVisibility(View.VISIBLE);
//            for (BluetoothDevice device : pairedDevices) {
//                pairedDevicesArrayAdapter.add(device.getName() + "\n"
//                        + device.getAddress());
//            }
//        } else {
//            String noDevices = getResources().getText(R.string.none_paired)
//                    .toString();
//            pairedDevicesArrayAdapter.add(noDevices);
//        }
        if(dbNames2.isEmpty()) {
            secondHopDevicesArrayAdapter.add("No Devices in Database");
        }
        else {
            String[] dbDevices = dbNames2.split("\r?\n");
            int count1 = dbDevices.length;
            for (int i = 0; i < count1; i = i + 5) {
                String a = dbDevices[i];
                String b = dbDevices[i + 1];
                String c = dbDevices[i + 2];
                String d = dbDevices[i + 3];
                String e= dbDevices[i + 4];
                secondHopDevicesArrayAdapter.add(a + "\n" + b + "\n" + c+"\n"+d+"\n"+e);
            }
        }
    }

    private final BroadcastReceiver discoveryFinishReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent
                        .getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                int  rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI,Short.MIN_VALUE);
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    if(device.getName()!= null) {
                        newDevicesArrayAdapter.add(device.getName() + "\n"
                                + device.getAddress() + "\n" + Integer.toString(rssi));
                        Devices scanDevices = new Devices(device.getName(),device.getAddress(),Integer.toString(rssi));
                        dbHandler.addDevice1(scanDevices);
                    }
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED
                    .equals(action)) {
//                setProgressBarIndeterminateVisibility(false);
                spinner.setVisibility(View.GONE);
                progressText.setVisibility(View.GONE);
//                scanDevicesButton.setVisibility(View.VISIBLE);
                setTitle(R.string.select_device);
                if (newDevicesArrayAdapter.getCount() == 0) {
                    String noDevices = getResources().getText(
                            R.string.none_found).toString();
                    newDevicesArrayAdapter.add(noDevices);
                }
            }
        }
    };
}
