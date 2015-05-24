# Android Smooth Bluetooth

[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-Android%20Smooth%20Bluetooth-brightgreen.svg?style=flat)](http://android-arsenal.com/details/1/1859)

Smooth communication via bluetooth with other android devices or microcontrollers such as Arduino.

## Getting Started

Add Gradle dependency:

```gradle
dependencies {
   compile 'io.palaima:smoothbluetooth:0.1.0'
}
```

* Or
[Download from Maven](https://oss.sonatype.org/content/repositories/releases/io/palaima/smoothbluetooth/0.1.0/smoothbluetooth-0.1.0.aar)

You can try the SNAPSHOT version:

```gradle
dependencies {
   compile 'io.palaima:smoothbluetooth:0.2.0-SNAPSHOT'
}
```
Make sure to add the snapshot repository:

```gradle
repositories {
    maven {
        url "https://oss.sonatype.org/content/repositories/snapshots"
    }
}
```

## Usage

### 1. Declare bluetooth permissions in AndroidManifest.xml
```xml
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
```
### 2. Define SmoothBluetooth instance
```java
private SmoothBluetooth mSmoothBluetooth;

@Override
protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mSmoothBluetooth = new SmoothBluetooth(Context context);
}
```
there are possible overrides:
```java
mSmoothBluetooth = new SmoothBluetooth(Context context, SmoothBluetooth.Listener listener);
```
or
```java
mSmoothBluetooth = new SmoothBluetooth(Context context, ConnectionTo connectionTo, Connection connection, SmoothBluetooth.Listener listener);
```

`ConnectionTo` defines to what type of device to connect with (by default it is `ConnectionTo.OTHER_DEVICE` which means microcontrollers like Arduino)

```java
ConnectionTo.ANDROID_DEVICE
ConnectionTo.OTHER_DEVICE
```

`Connection` defines what type of connection will be (be default it is `Connection.SECURE`)

```java
Connection.SECURE
Connection.INSECURE
```
### 3. Define SmoothBluetooth.Listener
After that you must define `SmoothBluetooth.Listener` which catches all bluetooth related events and pass it to `SmoothBluetooth` constructor when defining its instance or if you already have `SmoothBluetooth` instance you can pass listener via setter `setListener(SmoothBluetooth.Listener listener)`

```java
private SmoothBluetooth.Listener mListener = new SmoothBluetooth.Listener() {
    @Override
    public void onBluetoothNotSupported() {
        //device does not support bluetooth
    }

    @Override
    public void onBluetoothNotEnabled() {
        //bluetooth is disabled, probably call Intent request to enable bluetooth
    }

    @Override
    public void onConnecting(Device device) {
        //called when connecting to particular device
    }

    @Override
    public void onConnected(Device device) {
       //called when connected to particular device
    }

    @Override
    public void onDisconnected() {
        //called when disconnected from device
    }

    @Override
    public void onConnectionFailed(Device device) {
        //called when connection failed to particular device
    }

    @Override
    public void onDiscoveryStarted() {
        //called when discovery is started
    }

    @Override
    public void onDiscoveryFinished() {
        //called when discovery is finished
    }

    @Override
    public void onNoDevicesFound() {
        //called when no devices found
    }

    @Override
    public void onDevicesFound(final List<Device> deviceList,
            final BluetoothHelper.ConnectionCallback connectionCallback) {
        //receives discovered devices list and connection callback
        //you can filter devices list and connect to specific one
        //connectionCallback.connectTo(deviceList.get(position));
    }

    @Override
    public void onDataReceived(int data) {
        //receives all bytes
    }
};
```

### 4. Try to connect
After everything is set up and all is left to do is try to connect

```java
mSmoothBluetooth.tryConnection();
```
`tryConnection()` is linked with `SmoothBluetooth.Listener` so all connection events will be passed to listener.
By default if everything is ok, immediately returns all paired devices to `SmoothBluetooth.Listener`'s `onDevicesFound`

### 5. Discovering
```java
mSmoothBluetooth.doDiscovery();
```
Call `doDiscovery()` method which search for unpaired devices and returns them to `SmoothBluetooth.Listener`'s `onDevicesFound`

### 6. Sending data
```java
mSmoothBluetooth.send(byte[] data, boolean CRLF);
```
or
```java
mSmoothBluetooth.send(String data, boolean CRLF);
```
`boolean CRLF` indicates if data is need to be send with ending by LF and CR or not.
if you do not need CRLF at the end there are some overrides with `CRLF = false`
```java
mSmoothBluetooth.send(byte[] data);
mSmoothBluetooth.send(String data);
```

### 6. Disconnect

```java
mSmoothBluetooth.disconnect();
```

### 7. Do not forget to stop
For instance in your activity where `SmoothBluetooth` is defined you must call `stop()`
```java
@Override
protected void onDestroy() {
    super.onDestroy();
    mSmoothBluetooth.stop();
}
```

## Sample

You can clone the project and compile it yourself (it includes a sample).
[MainActivity](https://github.com/palaima/AndroidSmoothBluetooth/blob/master/app/src/main/java/io/palaima/smoothbluetooth/app/MainActivity.java)

## Contributing
Want to contribute? You are welcome!
Note that all pull request should go to `dev` branch.

Developed By
------------

* Mantas Palaima - <palaima.mantas@gmail.com>

Credits
------------

Credit to Aidan Follestad's [Material Dialogs](https://github.com/afollestad/material-dialogs) library.

License
--------

    Copyright 2015 Mantas Palaima.

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.
