package com.szy.bluetoothappv01;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.support.annotation.NonNull;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;
import java.util.ArrayList;
import java.util.Set;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";

    private static String mAddress = "A0:2F:D5:38:9E:32"; // <==要连接的目标蓝牙设备MAC地址 MiaoWuLabs

    private ViewPager viewPager;
    private ArrayList<View> pageView;
    private TextView weightLayout, controlLayout, waveLayout, pidLayout;
    private MyDialog myDialog;

    // 滚动条图片
    private ImageView scrollBar;
    // 滚动条初始偏移量
    private int offSet = 0;
    // 当前页编号
    private int currIndex = 0;
    // 滚动条宽度
    private int sbWidth;
    //一倍滚动量
    private int one;

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.toolbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.bluetooth:
                break;
            case R.id.menu:
                break;
            default:
                break;
        }
        return true;
    }

    /**
     * onCreate
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        viewPager = (ViewPager) findViewById(R.id.view_pager);

        /**
         * Dialog
         */
        myDialog = new MyDialog(MainActivity.this);

        toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {

                switch (menuItem.getItemId()) {
                    case R.id.bluetooth:
                        myDialog.show();
                        Button button = (Button) myDialog.findViewById(R.id.mydialog_btn);
                        final BluetoothAdapter mBluetoothAdapter;
                        final ListView mListView1 = (ListView) myDialog.findViewById(R.id.mydialog_list1);//已配对的蓝牙设备
                        final ListView mListView2 = (ListView) myDialog.findViewById(R.id.mydialog_list2);//新的蓝牙设备
                        final TextView mTextView1 = (TextView) myDialog.findViewById(R.id.mydialog_tv1);
                        final ArrayAdapter<String> mPairedDevicesArrayAdapter = new ArrayAdapter<String>(MainActivity.this, R.layout.arrayadapter);
                        final ArrayAdapter<String> mNewDevicesArrayAdapter = new ArrayAdapter<String>(MainActivity.this, R.layout.arrayadapter);

                        mListView1.setAdapter(mPairedDevicesArrayAdapter);
                        mListView2.setAdapter(mNewDevicesArrayAdapter);

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
                                setProgressBarIndeterminateVisibility(true);
                                mTextView1.setText("搜索中...");
                                if (mBluetoothAdapter.isDiscovering()) {
                                    mBluetoothAdapter.cancelDiscovery();
                                }
                                mBluetoothAdapter.startDiscovery();

                            }
                        });
                        // BroadcastReceiver监听设备，当设备发现后
                        final BroadcastReceiver mReceiver = new BroadcastReceiver() {
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
                                    // 完成搜索，设置TextView为“完成搜索”
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
                        IntentFilter intentFilter1 = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                        registerReceiver(mReceiver, intentFilter1);

                        intentFilter1 = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
                        registerReceiver(mReceiver, intentFilter1);

                        mListView1.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                switch (position) {
                                    case 0:
                                        Toast.makeText(MainActivity.this, "你选择了第" + position + "项", Toast.LENGTH_SHORT).show();
                                        break;
                                    case 1:
                                        Toast.makeText(MainActivity.this, "你选择了第" + position + "项", Toast.LENGTH_SHORT).show();

                                        break;
                                    case 2:
                                        Toast.makeText(MainActivity.this, "你选择了第" + position + "项", Toast.LENGTH_SHORT).show();
                                        break;
                                    case 3:
                                        Toast.makeText(MainActivity.this, "你选择了第" + position + "项", Toast.LENGTH_SHORT).show();
                                        break;
                                    default:
                                        break;
                                }
                            }
                        });

                        mListView2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                            @Override
                            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                                switch (position) {
                                    case 0:
                                        Toast.makeText(MainActivity.this, "你选择了第" + position + "项", Toast.LENGTH_SHORT).show();
                                        break;
                                    case 1:
                                        Toast.makeText(MainActivity.this, "你选择了第" + position + "项", Toast.LENGTH_SHORT).show();
                                        break;
                                    case 2:
                                        Toast.makeText(MainActivity.this, "你选择了第" + position + "项", Toast.LENGTH_SHORT).show();
                                        break;
                                    case 3:
                                        Toast.makeText(MainActivity.this, "你选择了第" + position + "项", Toast.LENGTH_SHORT).show();
                                        break;
                                    default:
                                        break;

                                }
                            }
                        });


                        break;
                    case R.id.menu:
                        break;
                    default:
                        break;
                }

                return false;
            }
        });


        // 查找布局文件
        LayoutInflater inflater = getLayoutInflater();
        View weight_view = inflater.inflate(R.layout.weight_layout, null);
        View control_view = inflater.inflate(R.layout.control_layout, null);
        View wave_view = inflater.inflate(R.layout.wave_layout, null);
        View pid_view = inflater.inflate(R.layout.pid_layout, null);

        weightLayout = (TextView) findViewById(R.id.weight);
        controlLayout = (TextView) findViewById(R.id.control);
        waveLayout = (TextView) findViewById(R.id.wave);
        pidLayout = (TextView) findViewById(R.id.PID);


        weightLayout.setOnClickListener(this);
        controlLayout.setOnClickListener(this);
        waveLayout.setOnClickListener(this);
        pidLayout.setOnClickListener(this);

        scrollBar = (ImageView) findViewById(R.id.scrollbar);

        pageView = new ArrayList<View>();
        // 添加想要切换的界面
        pageView.add(weight_view); //0
        pageView.add(control_view); //1
        pageView.add(wave_view); //2
        pageView.add(pid_view); //3

        // 数据适配器
        PagerAdapter mPagerAdapter = new PagerAdapter() {
            @Override
            //获取当前窗体界面数
            public int getCount() {
                return pageView.size();
            }

            @Override
            //判断是否由对象生成界面
            public boolean isViewFromObject(@NonNull View arg0, @NonNull Object arg1) {
                return arg0 == arg1;
            }

            //使从ViewGroup中移出当前View
            public void destroyItem(View arg0, int arg1, Object arg2) {
                ((ViewPager) arg0).removeView(pageView.get(arg1));
            }

            //返回一个对象，这个对象表明了PagerAdapter适配器选择哪个对象放在当前的ViewPager中
            public Object instantiateItem(View arg0, int arg1) {
                ((ViewPager) arg0).addView(pageView.get(arg1));
                switch (arg1) {
                    case 0:

                        break;
                    case 1:
                        Button btn_start = (Button) findViewById(R.id.start_control);
                        Button btn_stop = (Button) findViewById(R.id.stop_control);
                        ImageView up = (ImageView) findViewById(R.id.control_up);
                        ImageView left = (ImageView) findViewById(R.id.control_left);
                        ImageView right = (ImageView) findViewById(R.id.control_right);
                        ImageView down = (ImageView) findViewById(R.id.control_down);

                        btn_start.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Toast.makeText(MainActivity.this, "开始控制", Toast.LENGTH_SHORT).show();
                            }
                        });

                        btn_stop.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Toast.makeText(MainActivity.this, "停止控制", Toast.LENGTH_SHORT).show();
                            }
                        });

                        left.setOnTouchListener(new View.OnTouchListener() {
                            @Override
                            public boolean onTouch(View view, MotionEvent motionEvent) {
                                switch (motionEvent.getAction()) {
                                    case MotionEvent.ACTION_DOWN:
                                        Toast.makeText(MainActivity.this, "左拐中", Toast.LENGTH_SHORT).show();
                                        break;
                                    case MotionEvent.ACTION_UP:
                                        Toast.makeText(MainActivity.this, "停止左拐", Toast.LENGTH_SHORT).show();
                                        break;
                                    default:
                                        break;
                                }
                                return true;
                            }
                        });

                        up.setOnTouchListener(new View.OnTouchListener() {
                            @Override
                            public boolean onTouch(View view, MotionEvent motionEvent) {
                                switch (motionEvent.getAction()) {
                                    case MotionEvent.ACTION_DOWN:
                                        Toast.makeText(MainActivity.this, "前进中", Toast.LENGTH_SHORT).show();
                                        break;
                                    case MotionEvent.ACTION_UP:
                                        Toast.makeText(MainActivity.this, "停止前进", Toast.LENGTH_SHORT).show();
                                        break;
                                    default:
                                        break;
                                }
                                return true;
                            }
                        });

                        down.setOnTouchListener(new View.OnTouchListener() {
                            @Override
                            public boolean onTouch(View view, MotionEvent motionEvent) {
                                switch (motionEvent.getAction()) {
                                    case MotionEvent.ACTION_DOWN:
                                        Toast.makeText(MainActivity.this, "后退中", Toast.LENGTH_SHORT).show();
                                        break;
                                    case MotionEvent.ACTION_UP:
                                        Toast.makeText(MainActivity.this, "停止后退", Toast.LENGTH_SHORT).show();
                                        break;
                                    default:
                                        break;
                                }
                                return true;
                            }
                        });

                        right.setOnTouchListener(new View.OnTouchListener() {
                            @Override
                            public boolean onTouch(View view, MotionEvent motionEvent) {
                                switch (motionEvent.getAction()) {
                                    case MotionEvent.ACTION_DOWN:
                                        Toast.makeText(MainActivity.this, "右拐中", Toast.LENGTH_SHORT).show();
                                        break;
                                    case MotionEvent.ACTION_UP:
                                        Toast.makeText(MainActivity.this, "停止右拐", Toast.LENGTH_SHORT).show();
                                        break;
                                    default:
                                        break;
                                }
                                return true;
                            }
                        });
                        break;
                    case 2:

                        break;
                    case 3:
                        final TextView tv_p = (TextView) findViewById(R.id.pid_p_adjust);
                        final TextView tv_i = (TextView) findViewById(R.id.pid_i_adjust);
                        final TextView tv_d = (TextView) findViewById(R.id.pid_d_adjust);
                        SeekBar sb_p = (SeekBar) findViewById(R.id.seekbar_p);
                        SeekBar sb_i = (SeekBar) findViewById(R.id.seekbar_i);
                        SeekBar sb_d = (SeekBar) findViewById(R.id.seekbar_d);

                        sb_p.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                            @Override
                            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                                tv_p.setText("P=" + i / 100f);
                            }

                            @Override
                            public void onStartTrackingTouch(SeekBar seekBar) {

                            }

                            @Override
                            public void onStopTrackingTouch(SeekBar seekBar) {

                            }
                        });

                        sb_i.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                            @Override
                            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                                tv_i.setText("I=" + i / 100f);
                            }

                            @Override
                            public void onStartTrackingTouch(SeekBar seekBar) {

                            }

                            @Override
                            public void onStopTrackingTouch(SeekBar seekBar) {

                            }
                        });

                        sb_d.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                            @Override
                            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                                tv_d.setText("D=" + i / 100f);
                            }

                            @Override
                            public void onStartTrackingTouch(SeekBar seekBar) {

                            }

                            @Override
                            public void onStopTrackingTouch(SeekBar seekBar) {

                            }
                        });

                        break;
                    default:
                        break;
                }
                return pageView.get(arg1);
            }
        };
        //绑定适配器
        viewPager.setAdapter(mPagerAdapter);
        //设置viewPager的初始界面为第一个界面
        viewPager.setCurrentItem(0);
        //添加切换界面的监听器
        viewPager.addOnPageChangeListener(new MyOnPageChangeListener());
        // 获取滚动条的宽度
        sbWidth = BitmapFactory.decodeResource(getResources(), R.drawable.scrollbar).getWidth();
        //为了获取屏幕宽度，新建一个DisplayMetrics对象
        DisplayMetrics displayMetrics = new DisplayMetrics();
        //将当前窗口的一些信息放在DisplayMetrics类中
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        //得到屏幕的宽度
        int screenW = displayMetrics.widthPixels;
        //计算出滚动条初始的偏移量
        offSet = 0;
        //计算出切换一个界面时，滚动条的位移量
        one = screenW >> 2;
        Matrix matrix = new Matrix();
        matrix.postTranslate(offSet, 0);
        //将滚动条的初始位置设置成与左边界间隔一个offset
        scrollBar.setImageMatrix(matrix);
    }


//
//    public void onDestroy(){
//
//        super.onDestroy();
//        //解除注册
//        unregisterReceiver(mReceiver);
//        Log.e("destory","解除注册");
//    }
//
//    //定义广播接收
//    private BroadcastReceiver mReceiver=new BroadcastReceiver(){
//
//        @Override
//        public void onReceive(Context context, Intent intent) {
//
//            String action = intent.getAction();
//            Log.e("ywq", action);
//            if (action.equals(BluetoothDevice.ACTION_FOUND)) {
//                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
//                if (device.getBondState() == BluetoothDevice.BOND_BONDED) {    //显示已配对设备
//                    list.add(device.getName()+"\n"+device.getAddress()+"\n");
//                } else if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
//                    list1.add(device.getName()+"\n"+device.getAddress()+"\n");
//                }
//            } else if (action.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)) {
//
//            }
//        }
//    };
//
//    ArrayAdapter<String> mArrayAdapter = new ArrayAdapter<String>(MainActivity.this,android.R.layout.simple_list_item_1,list);


    public class MyOnPageChangeListener implements ViewPager.OnPageChangeListener {
        @Override
        public void onPageSelected(int arg0) {
            Animation animation = null;
            switch (arg0) {
                /**
                 * TranslateAnimation的四个属性分别为
                 * float fromXDelta 动画开始的点离当前View X坐标上的差值
                 * float toXDelta 动画结束的点离当前View X坐标上的差值
                 * float fromYDelta 动画开始的点离当前View Y坐标上的差值
                 * float toYDelta 动画开始的点离当前View Y坐标上的差值
                 **/
                case 0:
                    animation = new TranslateAnimation(0, 0, 0, 0);
                    break;
                case 1:
                    animation = new TranslateAnimation(one, one, 0, 0);
                    break;
                case 2:
                    animation = new TranslateAnimation(one + one, one + one, 0, 0);
                    break;
                case 3:
                    animation = new TranslateAnimation(one + one + one, one + one + one, 0, 0);
                    break;
            }
            //arg0为切换到的页的编码
            currIndex = arg0;
            // 将此属性设置为true可以使得图片停在动画结束时的位置
            animation.setFillAfter(true);
            //动画持续时间，单位为毫秒
            animation.setDuration(200);
            //滚动条开始动画
            scrollBar.startAnimation(animation);


        }

        @Override
        public void onPageScrolled(int arg0, float arg1, int arg2) {
        }

        @Override
        public void onPageScrollStateChanged(int arg0) {
        }
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.weight:
                //点击“重力感应”时切换到第一页
                viewPager.setCurrentItem(0);
                break;
            case R.id.control:
                //点击“手动控制”时切换到第一页
                viewPager.setCurrentItem(1);
                break;
            case R.id.wave:
                //点击“波形显示”时切换到第一页
                viewPager.setCurrentItem(2);
                break;
            case R.id.PID:
                //点击“PID”时切换到第一页
                viewPager.setCurrentItem(3);
                break;
        }
    }
//    @Override
//    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//        switch (seekBar.getId()){
//            case(R.id.pid_sb_p):
//                text_p.setText("P:"+progress);
//                break;
//            case (R.id.pid_sb_i):
//                text_i.setText("I:"+progress);
//                break;
//            case(R.id.pid_sb_d):
//                text_d.setText("D:"+progress);
//                break;
//        }
//    }
//    @Override
//    public void onStartTrackingTouch(SeekBar seekBar) {
//    }
//    @Override
//    public void onStopTrackingTouch(SeekBar seekBar) {
//    }

//    SeekBar seekBar_p = (SeekBar) findViewById(R.id.pid_sb_p);
//    SeekBar seekBar_i = (SeekBar) findViewById(R.id.pid_sb_i);
//    SeekBar seekBar_d = (SeekBar) findViewById(R.id.pid_sb_d);
//    TextView text_p = (TextView) findViewById(R.id.pid_p_adjust);
//    TextView text_i = (TextView) findViewById(R.id.pid_i_adjust);
//    TextView text_d = (TextView) findViewById(R.id.pid_d_adjust);
//
//    public class seekBarAction implements SeekBar.OnSeekBarChangeListener {
//
//        @Override
//        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
//            switch (seekBar.getId()) {
//                case (R.id.pid_sb_p):
//                    text_p.setText("P:" + progress);
//                    break;
//                case (R.id.pid_sb_i):
//                    text_i.setText("I:" + progress);
//                    break;
//                case (R.id.pid_sb_d):
//                    text_d.setText("D:" + progress);
//                    break;
//            }
//        }
//
//        @Override
//        public void onStartTrackingTouch(SeekBar seekBar) {
//
//        }
//
//        @Override
//        public void onStopTrackingTouch(SeekBar seekBar) {
//
//        }
//    }
}

