# Android Bluetooth Helper

Easy communication with other android devices or microcontrollers such as Arduino via bluetooth.

## Usage

### 1. Declare bluetooth permissions in AndroidManifest.xml
```xml
<uses-permission android:name="android.permission.BLUETOOTH" />
<uses-permission android:name="android.permission.BLUETOOTH_ADMIN" />
```
### 2. Define BluetoothHelper instance
```java
BluetoothHelper bluetoothHelper = new BluetoothHelper(Context context);
```
there are possible overwrites:
```java
BluetoothHelper bluetoothHelper = new BluetoothHelper(Context context, BluetoothHelper.Listener listener);
```
or
```java
BluetoothHelper bluetoothHelper = new BluetoothHelper(Context context, ConnectionTo connectionTo, Connection connection, BluetoothHelper.Listener listener);
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

After that you must define BluetoothHelper.Listener which catches all bluetooth related events and pass it to BluetoothHelper constructor when defining its instance or if you already have BluetoothHelper instance you can pass listener via setter `setListener(BluetoothHelperListener listener)`

```java
private BluetoothHelper.Listener mListener = new BluetoothHelper.Listener() {
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

After everything is set up and all is left to do is try to connect

```java
mBluetoothHelper.tryConnection();
```
`tryConnection()` is linked with BluetoothHelper.Listener so all connection events will be passed to listener.

## Getting Started

Add Gradle dependency:

```gradle
dependencies {
   compile 'io.palaima:bluetoothhelper-0.1'
}
```

* Or
[Download from Maven](https://oss.sonatype.org/content/repositories/releases/io/palaima/bluetoothhelper/0.1/bluetoothhelper-0.1.aar)

You can try the SNAPSHOT version:

```gradle
dependencies {
   compile 'io.palaima:bluetoothhelper-0.2-SNAPSHOT'
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

## Contributing
Want to contribute? You are welcome!
Note that all pull request should go to `dev` branch.

Developed By
------------

* Mantas Palaima - <palaima.mantas@gmail.com>

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
