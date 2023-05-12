package com.example.testusb;

import androidx.appcompat.app.AppCompatActivity;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    private UsbDevice device;
    private UsbManager usbManager;
    PendingIntent permissionIntent;
    private static final String ACTION_USB_PERMISSION = "com.android.usb.USB_PERMISSION";
    public static final String TAG = "USB_Test";

    private final BroadcastReceiver usbReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {
                            // call method to set up device communication
                            Log.e("USB_Test", "插入设备 DeviceName:" + device.getDeviceName() + ">>>DeviceId:" + device.getDeviceId());

                            Toast.makeText(MainActivity.this, "插入设备 DeviceName:" + device.getDeviceName() + ">>>DeviceId:" + device.getDeviceId(), Toast.LENGTH_SHORT).show();
//                            usbManager.requestPermission(device, permissionIntent);
                        }
                    } else {
                        Log.d(TAG, "permission denied for device " + device);
                    }
                }
            }
        }
    };

    /**
     * @description OTG广播注册
     * @author ldm
     * @time 2017/9/1 17:19
     */
    private void registerUDiskReceiver() {
        //监听otg插入 拔出
        IntentFilter usbDeviceStateFilter = new IntentFilter();
        usbDeviceStateFilter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        usbDeviceStateFilter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(mOtgReceiver, usbDeviceStateFilter);
        //注册监听自定义广播
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        registerReceiver(mOtgReceiver, filter);

    }

    private  void unRegisterUDiskReceiver(){

        unregisterReceiver(mOtgReceiver);
    }

    /**
     * @description OTG广播，监听U盘的插入及拔出
     * @author ldm
     * @time 2017/9/1 17:20
     * @param
     */
    private BroadcastReceiver mOtgReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            switch (action) {
                case ACTION_USB_PERMISSION://接受到自定义广播
                    UsbDevice usbDevice = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    //允许权限申请
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (usbDevice != null) {
                            //用户已授权，可以进行读取操作
//                            readDevice(getUsbMass(usbDevice));
                        } else {
                            showToastMsg("没有插入U盘");
                        }
                    } else {
                        showToastMsg("未获取到U盘权限");
                    }
                    break;
                case UsbManager.ACTION_USB_DEVICE_ATTACHED://接收到U盘设备插入广播
                    UsbDevice device_add = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (device_add != null) {
                        //接收到U盘插入广播，尝试读取U盘设备数据
//                        redUDiskDevsList();
                        showToastMsg("插入U盘");
                    }
                    break;
                case UsbManager.ACTION_USB_DEVICE_DETACHED://接收到U盘设设备拔出广播
                    showToastMsg("U盘已拔出");
                    break;
            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
//        initUSB();
        findViewById(R.id.Test).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//            getUsbPermission(device);
                getUsbDeviceList();
                usbManager.requestPermission(device, permissionIntent);
            }
        });

//        registerUsbReceiver();

        registerUDiskReceiver();
    }


    private void registerUsbReceiver() {

        UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        permissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        registerReceiver(usbReceiver, filter);


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        unregisterReceiver(usbReceiver);
        unRegisterUDiskReceiver();
    }

    private void getUsbDeviceList() {
        UsbManager manager = (UsbManager) getSystemService(Context.USB_SERVICE);
// 获取到的是设备名与USB设备的映射
        HashMap<String, UsbDevice> deviceList = manager.getDeviceList();
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        List<UsbDevice> usbDevices = new ArrayList<UsbDevice>();
        while (deviceIterator.hasNext()) {
            UsbDevice device = deviceIterator.next();
            // your code
            usbDevices.add(device);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                Log.e("USB_Test", "DeviceName:" + device.getDeviceName() + ">>>ManufacturerName:" + device.getManufacturerName() + ">>>DeviceId:" + device.getDeviceId());
            }

//            Toast.makeText(this,device.getDeviceId(),Toast.LENGTH_SHORT).show();
        }

    }


        private  void showToastMsg(String msg){

        Toast.makeText(this,msg,Toast.LENGTH_SHORT).show();

        }


//    private void initUSB(){
//
//        HashMap<String, UsbDevice> deviceList = usbManager.get();
//        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
//        List<UsbDevice> usbDevices = new ArrayList<UsbDevice>();
//        while (deviceIterator.hasNext()) {
//            UsbDevice device = deviceIterator.next();
//            usbDevices.add(device);
//            Log.e("USB_Test", "getDeviceList: " + device.getDeviceId());
//        }
//         device = usbDevices.size()>0?usbDevices.get(0):null;
//
//    }

//
//    private void getUsbPermission(UsbDevice mUSBDevice) {
//        PendingIntent pendingIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), 0);
//        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
//        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
//        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
//        registerReceiver(mUsbReceiver, filter);// 广播监听，监听USB设备插拔、权限
//        usbManager.requestPermission(mUSBDevice, pendingIntent); // 该代码执行后，系统弹出一个对话框/等待权限
//    }
//
//    private final BroadcastReceiver mUsbReceiver = new BroadcastReceiver() {
//        public void onReceive(Context context, Intent intent) {
//            String action = intent.getAction();
//
//            if (ACTION_USB_PERMISSION.equals(action)) {
//                synchronized (this) {
//                    unregisterReceiver(mUsbReceiver);
//                    UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
//                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false) && device.equals(device)) {
//                        //授权成功后，进行USB设备操作
//                        initUSB();
//                    } else {
//                        // 拒绝
//                        Toast.makeText(MainActivity.this, "拒绝权限会造成与UKey通信失败！", Toast.LENGTH_SHORT).show();
//                    }
//                }
//            }
//        }
//    };
//
//

//    private void sss (){
//        UsbInterface usbInterface = device.getInterface(0);
//        UsbEndpoint epBulkIn = usbInterface.getEndpoint(0);// 读信息
//        UsbEndpoint epBulkOut = usbInterface.getEndpoint(1);// 写入指令
//
//        if (usbInterface != null) {
//            // open前判断连接权限
//            if (usbManager.hasPermission(device)) {
//                // 打开USB设备，以便向此USB设备发送和接受数据，返回一个关于此USB设备的连接
//                usbDeviceConnection = usbManager.openDevice(device);
//            }
//            if (usbDeviceConnection != null && usbDeviceConnection.claimInterface(usbInterface, true)) {
//                usbDeviceConnection = conn;
//                if (usbDeviceConnection != null) {
//                    // android设备已经连接硬件设备
//                    Log.e("USB_Test", "android设备已经连接硬件设备.");
//                }
//            } else {
//
//            }
//        }

}
