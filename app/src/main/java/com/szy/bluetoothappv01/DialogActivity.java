package com.szy.bluetoothappv01;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import java.util.Set;

public class DialogActivity extends Activity implements AdapterView.OnItemClickListener {

    private BluetoothAdapter mBluetoothAdapter;
    private ArrayAdapter<String> mPairedDevicesArrayAdapter; //已配对的蓝牙设备
    private ArrayAdapter<String> mNewDevicesArrayAdapter;   //新的蓝牙设备

    ListView mListView1 = (ListView) findViewById(R.id.mydialog_list1); //已配对的蓝牙设备列表
    ListView mListView2 = (ListView) findViewById(R.id.mydialog_list2); //新的蓝牙设备列表

    TextView mTextView1 = (TextView) findViewById(R.id.mydialog_tv1);

    Button button = (Button) findViewById(R.id.mydialog_btn);

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mPairedDevicesArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);
        mNewDevicesArrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1);

        // 把已配对设备放到 ListView1
        mListView1.setAdapter(mPairedDevicesArrayAdapter);
        mListView1.setOnItemClickListener(this);

        // 把新发现的设备放到 ListView2
        mListView2.setAdapter(mNewDevicesArrayAdapter);
        mListView2.setOnItemClickListener(this);

        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        this.registerReceiver(mReceiver, filter);

        filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        this.registerReceiver(mReceiver, filter);

        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
        if (pairedDevices.size() > 0) {
            for (BluetoothDevice device : pairedDevices) {
                mPairedDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
            }
        }
        // 搜寻设备按钮
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doDiscovery();
            }
        });
    }

    private void doDiscovery() {
        setProgressBarIndeterminateVisibility(true);
        mTextView1.setText("搜索中...");
        if (mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }
        mBluetoothAdapter.startDiscovery();
    }


    //销毁函数
    @Override
    protected void onDestroy() {
        if (mBluetoothAdapter != null && mBluetoothAdapter.isDiscovering()) {
            mBluetoothAdapter.cancelDiscovery();
        }
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    // BroadcastReceiver监听设备，当设备发现后
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            // 当发现设备后
            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                // 如果已经配对的
                if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
                    mNewDevicesArrayAdapter.add(device.getName() + "\n" + device.getAddress());
                }
                // 完成搜索，设置Title为“完成搜索”
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                setProgressBarIndeterminateVisibility(false);
                mTextView1.setText("完成搜索");
                if (mNewDevicesArrayAdapter.getCount() == 0) {
                    String noDevices = "未发现任何设备";
                    mNewDevicesArrayAdapter.add(noDevices);
                }
            }
        }
    };

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        // TODO Auto-generated method stub
        // 取消搜索，准备选择设备进行连接
        mBluetoothAdapter.cancelDiscovery();
        // 获取设备的MAC地址
        String info = ((TextView) view).getText().toString();
        String address = info.substring(info.length() - 17);
        // 把MAC address用Intent回传到主Activity
        Intent intent = new Intent();
        intent.putExtra("设备地址", address);
        // 结束当前Activity并回传MAC
        setResult(Activity.RESULT_OK, intent);
        finish();
    }
}
