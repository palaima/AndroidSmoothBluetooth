/*
 * Copyright (C) 2014 Mantas Palaima
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.palaima.smoothbluetooth;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class SmoothBluetooth {

    public enum Connection {
        SECURE,
        INSECURE
    }

    public enum ConnectionTo {
        ANDROID_DEVICE,
        OTHER_DEVICE
    }

    public interface ConnectionCallback {
        void connectTo(Device device);
    }

    public interface Listener {
        void onBluetoothNotSupported();
        void onBluetoothNotEnabled();
        void onConnecting(Device device);
        void onConnected(Device device);
        void onDisconnected();
        void onConnectionFailed(Device device);
        void onDiscoveryStarted();
        void onDiscoveryFinished();
        void onNoDevicesFound();
        void onDevicesFound(List<Device> deviceList, ConnectionCallback connectionCallback);
        void onDataReceived(int data);
    }

    private static final String TAG = "BluetoothManager";

    private BluetoothAdapter mBluetoothAdapter;

    private BluetoothService mBluetoothService;

    private boolean isServiceRunning;

    private boolean mIsAndroid;

    private boolean mIsSecure;

    private boolean isConnected;

    private boolean isConnecting;

    private final Context mContext;

    private Listener mListener;

    private ArrayList<Device> mDevices = new ArrayList<>();

    private Device mCurrentDevice;

    public SmoothBluetooth(Context context) {
        this(context, ConnectionTo.OTHER_DEVICE, Connection.SECURE, null);
    }

    public SmoothBluetooth(Context context, Listener listener) {
        this(context, ConnectionTo.OTHER_DEVICE, Connection.SECURE, listener);
    }

    public SmoothBluetooth(Context context, ConnectionTo connectionTo, Connection connection,
            Listener listener) {
        mContext = context;
        mListener = listener;
        mIsAndroid = connectionTo == ConnectionTo.ANDROID_DEVICE;
        mIsSecure = connection == Connection.SECURE;
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    private boolean checkBluetooth() {
        if (!isBluetoothAvailable()) {
            if (mListener != null) {
                mListener.onBluetoothNotSupported();
            }
            return false;
        }

        if (!isBluetoothEnabled()) {
            if (mListener != null) {
                mListener.onBluetoothNotEnabled();
            }
            return false;
        }
        return true;
    }

    public void tryConnection() {
        if (!checkBluetooth()) {
            return;
        }

        mDevices.clear();

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                String name = device.getName();
                String address = device.getAddress();
                if(name != null && address != null) {
                    mDevices.add(new Device(name, address, true));
                }
            }
        }

        Log.d(TAG, "Paired devices: " + mDevices.size());
        if (!mDevices.isEmpty()) {
            mListener.onDevicesFound(mDevices, new ConnectionCallback() {
                @Override
                public void connectTo(Device device) {
                    if (device != null) {
                        connect(device, mIsAndroid, mIsSecure);
                    }
                }
            });
        } else {
            doDiscovery();
        }
    }

    public boolean isBluetoothAvailable() {
        try {
            if (mBluetoothAdapter == null || mBluetoothAdapter.getAddress().equals(null))
                return false;
        } catch (NullPointerException e) {
            return false;
        }
        return true;
    }

    public boolean isBluetoothEnabled() {
        return mBluetoothAdapter.isEnabled();
    }

    public boolean isServiceAvailable() {
        return mBluetoothService != null;
    }

    public boolean startDiscovery() {
        return mBluetoothAdapter.startDiscovery();
    }

    public boolean isDiscovery() {
        return mBluetoothAdapter.isDiscovering();
    }

    public boolean cancelDiscovery() {
        return mBluetoothAdapter.cancelDiscovery();
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    private void connect(Device device, boolean android, boolean secure) {
        mCurrentDevice = device;
        if (mListener != null) {
            mListener.onConnecting(device);
        }
        connect(device.getAddress(), android, secure);
    }

    public boolean isConnected() {
        return mCurrentDevice != null;
    }

    public void doDiscovery() {
        if (!checkBluetooth()) {
            return;
        }
        mCurrentDevice = null;
        mDevices.clear();
        if (mListener != null) {
            mListener.onDiscoveryStarted();
        }
        Log.d(TAG, "doDiscovery()");

        if (isDiscovery()) {
            mContext.unregisterReceiver(mReceiver);
            cancelDiscovery();
        }
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        mContext.registerReceiver(mReceiver, filter);

        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        mContext.registerReceiver(mReceiver, filter);

        startDiscovery();
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            // When discovery finds a device
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                // Get the BluetoothDevice object from the Intent
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // If it's already paired, skip it, because it's been listed already
                Log.d(TAG, "Device found: " + device.getName() + " " + device.getAddress());
                if(!deviceExist(device)) {
                    mDevices.add(new Device(device.getName(), device.getAddress(), false));
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Log.d(TAG, "Discovery finished: " + mDevices.size());
                mContext.unregisterReceiver(mReceiver);
                if (mListener != null) {
                    mListener.onDiscoveryFinished();
                }
                connectAction(mDevices, mIsAndroid, mIsSecure);
            }
        }
    };

    private void connectAction(List<Device> devices, final boolean android, final boolean secure) {
        if (mListener != null) {
            if (devices.isEmpty()) {
                mListener.onNoDevicesFound();
            } else {
                mListener.onDevicesFound(devices, new ConnectionCallback() {
                    @Override
                    public void connectTo(Device device) {
                        if (device != null) {
                            connect(device, android, secure);
                        }
                    }
                });
            }
        }
    }

    private boolean deviceExist(BluetoothDevice device){
        for (Device mDevice : mDevices) {
            if (mDevice.getAddress().contains(device.getAddress())) {
                return true;
            }
        }
        return false;
    }

    public BluetoothAdapter getBluetoothAdapter() {
        return mBluetoothAdapter;
    }

    private void setupService() {
        mBluetoothService = new BluetoothService(mHandler);
    }

    private void startService(boolean isAndroid, boolean secure) {
        if (isServiceAvailable()) {
            if (mBluetoothService.getState() == BluetoothService.STATE_NONE) {
                isServiceRunning = true;
                mBluetoothService.start(isAndroid, secure);
            }
        }
    }

    public void stop() {
        mCurrentDevice = null;
        if (isServiceAvailable()) {
            isServiceRunning = false;
            mBluetoothService.stop();
        }
        new Handler().postDelayed(new Runnable() {
            public void run() {
                if (isServiceAvailable()) {
                    isServiceRunning = false;
                    mBluetoothService.stop();
                }
            }
        }, 500);
    }

    private void connect(String address, boolean android, boolean secure) {
        if (isConnecting) {
            return;
        }
        if (!isServiceAvailable()) {
            setupService();
        }
        startService(android, secure);
        if(BluetoothAdapter.checkBluetoothAddress(address)) {
            BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
            mBluetoothService.connect(device);
        }
    }

    public void disconnect() {
        mCurrentDevice = null;
        if(isServiceAvailable()) {
            isServiceRunning = false;
            mBluetoothService.stop();
            if(mBluetoothService.getState() == BluetoothService.STATE_NONE) {
                isServiceRunning = true;
                mBluetoothService.start(mIsAndroid, mIsSecure);
            }
        }
    }

    public void send(String data) {
        send(data, false);
    }

    public void send(byte[] data) {
        send(data, false);
    }

    public void send(byte[] data, boolean CRLF) {
        if(isServiceAvailable() && mBluetoothService.getState() == BluetoothService.STATE_CONNECTED) {
            if(CRLF) {
                byte[] data2 = new byte[data.length + 2];
                System.arraycopy(data, 0, data2, 0, data.length);
                data2[data2.length] = 0x0A;
                data2[data2.length] = 0x0D;
                mBluetoothService.write(data2);
            } else {
                mBluetoothService.write(data);
            }
        }
    }

    public void send(String data, boolean CRLF) {
        if(isServiceAvailable() && mBluetoothService.getState() == BluetoothService.STATE_CONNECTED) {
            if(CRLF)
                data += "\r\n";
            mBluetoothService.write(data.getBytes());
        }
    }

    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case BluetoothService.MESSAGE_WRITE:
                    break;
                case BluetoothService.MESSAGE_READ:
                    int readBuf = (int) msg.obj;
                    //String readMessage = new String(readBuf);
                    //if(readBuf != null) {
                    if(mListener != null)
                        mListener.onDataReceived(readBuf);
                    // }
                    break;
                case BluetoothService.MESSAGE_DEVICE_NAME:
                    if(mListener != null) {
                        mListener.onConnected(mCurrentDevice);
                    }
                    isConnected = true;
                    break;
                case BluetoothService.MESSAGE_STATE_CHANGE:
                    /*if(mBluetoothStateListener != null)
                        mBluetoothStateListener.onServiceStateChanged(msg.arg1);*/
                    if(isConnected && msg.arg1 != BluetoothService.STATE_CONNECTED) {
                        isConnected = false;
                        if (mListener != null) {
                            mListener.onDisconnected();
                            mCurrentDevice = null;
                        }
                    }
                    if(!isConnecting && msg.arg1 == BluetoothService.STATE_CONNECTING) {
                        isConnecting = true;
                    } else if(isConnecting) {
                        isConnecting = false;
                        if(msg.arg1 != BluetoothService.STATE_CONNECTED) {
                            if (mListener != null) {
                                mListener.onConnectionFailed(mCurrentDevice);
                                mCurrentDevice = null;
                            }
                        }
                    }
                    break;
            }
        }
    };

}
