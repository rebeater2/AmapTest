package com.example.baolinfeng.amaptest;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.location.Location;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.Toast;

import com.amap.api.maps.AMap;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.MapView;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.BitmapDescriptorFactory;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity implements View.OnTouchListener {

    //global vars
    MapView mapView = null;
    static final String TAG = "mydebug";
    private AMap aMap = null;//地图控制器类
    private UiSettings uiSettings = null;//地图UI设置
    private ActionBar actionBar;//动作栏
    private ImageButton setButton;//设置按钮
    private Marker robotmarker;//用来标识机器人位置的图标
    private Robot robot;//记录机器人的速度、位置、方向
    private MyLocation myLocation;//手机位置

    //按键手柄
    GridLayout gridLayout_direction_keys;
    private ImageButton buttonup;
    private ImageButton buttondown;
    private ImageButton buttonleft;
    private ImageButton buttonright;

    //速度控制
    private ImageButton buttonspeedup;
    private ImageButton buttonspeeddown;

    //虚拟方向手柄
    private ImageButton button_rocker;
    private ImageButton button_touch_direction_bar;
    DirectionBar directionBar;
    private FrameLayout frameLayout_direction_bar;
    private List<MyLocation> track;

    //actionbar控件
    Switch aSwitch;
    MenuItem menuItem_setAddress; //IP设置item
    MenuItem menuItem_signal_wireless;//信号状态控件
    MenuItem menuItem_battery;//电池控件

    //悬浮功能按键
    ImageButton button_location;
    ImageButton button_set_track;

    String IPAddress = "192.168.1.115";
    int Port = 9999;
    TcpConnector tcpConnector;
    ActivityHandler activityHandler;//线程消息处理
    //
    private BitmapDescriptor robotbitmapDescriptor;//机器人图标

    private final double pi = 3.1415926535897932;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        /*
        设置为强制横屏，禁止旋转
         */
        if (getRequestedOrientation() != ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        }
        /*
        隐藏状态栏
         */
//        getSupportActionBar().hide();
        setContentView(R.layout.activity_main);

        mapView = findViewById(R.id.amapview);
        myLocation = new MyLocation(30.523533220, 114.362200);
        buttonup = findViewById(R.id.buttonUP);
        buttonup.setOnTouchListener(this);
        buttondown = findViewById(R.id.buttonDOWN);
        buttondown.setOnTouchListener(this);
        buttonleft = findViewById(R.id.buttonLEFT);
        buttonleft.setOnTouchListener(this);
        buttonright = findViewById(R.id.buttonRIGHT);
        buttonright.setOnTouchListener(this);
        buttonspeedup = findViewById(R.id.buttonSpeedUP);
        buttonspeedup.setOnTouchListener(this);
        buttonspeeddown = findViewById(R.id.buttonSpeedDOWN);
        buttonspeeddown.setOnTouchListener(this);
        gridLayout_direction_keys = findViewById(R.id.grid_direction_keys);


        button_touch_direction_bar = findViewById(R.id.directionbar_background);
        button_rocker = findViewById(R.id.rocker);
        directionBar = new DirectionBar(this);
        directionBar.initialize(button_touch_direction_bar, button_rocker, this);
        button_rocker.setOnTouchListener(this);
        frameLayout_direction_bar = findViewById(R.id.framelayout_directionbar);
        frameLayout_direction_bar.setOnTouchListener(this);

        button_location = findViewById(R.id.tool_button1);
        button_location.setOnTouchListener(this);
        track = new ArrayList<>();

        activityHandler = new ActivityHandler(this);
        Log.i(TAG, "tp main");
        mapView.onCreate(savedInstanceState);
        if (aMap == null)
            aMap = mapView.getMap();
        //初始化地图、actionBar
        uiInitialize();
        markerInitialize();

        //初始化robot 必须在markerinitialize之后执行
        robot = new Robot(new LatLng(0, 0), 0, 0.000001, robotmarker);
        Log.e(TAG, " Main activity Process.myUid() = " + android.os.Process.myTid());
    }

    private void uiInitialize() {
    /*
    amap
     */
        uiSettings = aMap.getUiSettings();

        uiSettings.setZoomControlsEnabled(true);
        aMap.setMapType(AMap.MAP_TYPE_NORMAL);// 设置卫星地图模式，aMap是地图控制器对象。
//        aMap.animateCamera(CameraUpdateFactory.zoomTo(20f));

        uiSettings.setCompassEnabled(false);//显示指南针按钮
        uiSettings.setMyLocationButtonEnabled(false);//显示定位按钮
        uiSettings.setScaleControlsEnabled(true);//控制比例尺控件是否显示
        uiSettings.setRotateGesturesEnabled(false);//禁用旋转手势，因为会导致角度异常

        // 初始化定位蓝点样式类
        MyLocationStyle myLocationStyle;
        myLocationStyle = new MyLocationStyle();
        myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_SHOW);
        myLocationStyle.strokeColor(0xffffff);
        myLocationStyle.radiusFillColor(0x000000);//设置定位蓝点精度圆圈的填充颜色的方法。
        myLocationStyle.showMyLocation(true);

        BitmapDescriptor bitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.me);

        myLocationStyle.myLocationIcon(bitmapDescriptor);
        aMap.setMyLocationStyle(myLocationStyle);//设置定位蓝点的Style

        aMap.setMyLocationEnabled(true);// 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。
        //初始位置
        aMap.setOnMyLocationChangeListener(new AMap.OnMyLocationChangeListener() {
            @Override
            public void onMyLocationChange(Location location) {
                Log.e(TAG, "位置是 " + location.getLatitude() + location.getLongitude() + "");
                aMap.moveCamera(CameraUpdateFactory.newLatLng(new LatLng(location.getLatitude(), location.getLongitude())));
                aMap.moveCamera(CameraUpdateFactory.zoomTo(19));
                myLocation.setLatitude(location.getLatitude());
                myLocation.setLongitude(location.getLongitude());
                robot.setLocation(new LatLng(location.getAltitude(), location.getLatitude()));
                setMarkerOnMap(location.getLatitude(), location.getLongitude());
            }
        });

    /*
    action bar
     */
        actionBar = getSupportActionBar();
        //actionBar.setDisplayShowTitleEnabled(true);
        actionBar.setTitle("遥控车");
        actionBar.setIcon(R.drawable.ic_top_eg1);
        //显示图标
        actionBar.setDisplayShowHomeEnabled(true);

    }

    private void markerInitialize() {
        LatLng latLng = new LatLng(30.525434114, 114.362200);
        robotbitmapDescriptor = BitmapDescriptorFactory.fromResource(R.drawable.location_marker);
        robotmarker = aMap.addMarker(new MarkerOptions().position(latLng).title("robot").icon(robotbitmapDescriptor));

    }

    public void setMarkerOnMap(double latitude, double longitude) {
        LatLng latLng = new LatLng(latitude, longitude);
        robotmarker.setPosition(latLng);
        robot.setLocation(latLng);
        Log.i(TAG, "" + robot.getLocation().latitude);
//        aMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
//        aMap.moveCamera(CameraUpdateFactory.zoomTo(18));
    }

    private boolean isIPAddress(String ip) {
        // 正则表达式
        String iprule = "(25[0-5]|2[0-4]\\d|1\\d{2}|[1-9]\\d|[1-9])\\." + "(25[0-5]|2[0-4]\\d|1\\d{1," +
                "" + "2}|\\d{2}|\\d)\\." + "(25[0-5]|2[0-4]\\d|1\\d{1,2}|\\d{2}|\\d)\\." + "" + "" +
                "(25[0-5]|2[0-4]\\d|1\\d{1,2}|\\d{2}|\\d)";
        Pattern pattern = Pattern.compile(iprule);
        Matcher matcher = pattern.matcher(ip);

        return matcher.matches();
    }

    private void goToSetActivity() {

        Intent intent = new Intent();
        intent.setClass(getBaseContext(), userset.class);
        startActivity(intent);
        Log.i(TAG, "go to set");
    }

    @Override
    public boolean onTouch(View v, MotionEvent m) {
        switch (v.getId()) {
            /******************direction button *********************/
            case R.id.buttonUP:
                if (m.getAction() == MotionEvent.ACTION_DOWN) {
                    buttonup.setImageResource(R.drawable.up_pressed);
                    robot.moveOn();//内部已经开启计时器，务必调用stop函数关闭计时器，ondestory中销毁timer
                } else if (m.getAction() == MotionEvent.ACTION_UP) {
                    buttonup.setImageResource(R.drawable.up);
                    robot.stop();
                }
                break;
            case R.id.buttonDOWN:
                if (m.getAction() == MotionEvent.ACTION_DOWN) {
                    buttondown.setImageResource(R.drawable.down_pressed);
                    robot.moveBack();
                } else if (m.getAction() == MotionEvent.ACTION_UP) {
                    buttondown.setImageResource(R.drawable.down);
                    robot.stop();
                }
                break;
            case R.id.buttonLEFT:
                if (m.getAction() == MotionEvent.ACTION_DOWN) {
                    buttonleft.setImageResource(R.drawable.left_pressed);

                    robot.turnLeft(2);

                } else if (m.getAction() == MotionEvent.ACTION_UP) {
                    buttonleft.setImageResource(R.drawable.left);
                    robot.stop();
                }
                break;
            case R.id.buttonRIGHT:
                if (m.getAction() == MotionEvent.ACTION_DOWN) {
                    buttonright.setImageResource(R.drawable.right_pressed);
                    robot.turnRight(2);
                } else if (m.getAction() == MotionEvent.ACTION_UP) {
                    buttonright.setImageResource(R.drawable.right);
                    robot.stop();
                }
                break;
            case R.id.buttonSpeedUP:
                if (m.getAction() == MotionEvent.ACTION_DOWN) {
                    buttonspeedup.setImageResource(R.drawable.speedup_pressed);
                    robot.setVelocity(robot.getVelocity() + 0.000005);
                    Log.i(TAG, "speed up" + robot.getVelocity());
                } else if (m.getAction() == MotionEvent.ACTION_UP) {
                    buttonspeedup.setImageResource(R.drawable.speedup);
                }
                break;
            case R.id.buttonSpeedDOWN:
                if (m.getAction() == MotionEvent.ACTION_DOWN) {
                    buttonspeeddown.setImageResource(R.drawable.speeddown_pressed);
                    Log.i(TAG, "speed down pressed");
                    robot.setVelocity(robot.getVelocity() - 0.000005);
                } else if (m.getAction() == MotionEvent.ACTION_UP) {
                    buttonspeeddown.setImageResource(R.drawable.speeddown);
                }
                break;

            /*******************************direction bar*******************/
            case R.id.directionbar_background:
                if (m.getAction() == MotionEvent.ACTION_DOWN) {
                    directionBar.touchOnActionDown();//此方法只改变directionbar的图标，不移动robot
                    robot.timerForDirectionBarStart();//开启计时器
                } else if (m.getAction() == MotionEvent.ACTION_UP) {
                    directionBar.touchOnActionUp();
                    robot.timerForDirectionBarStop();//关闭计时器
                } else {
                    //移动
                    directionBar.touchOnActionMove(m);
                    double angle = directionBar.getAngle();
                    robot.updateAngleFromActionBar(angle);//更新角度
                }
                break;

            default:
                break;
        }
        return true;
    }


    /* Handler处理方式 */
    static class ActivityHandler extends Handler {
        private final WeakReference<MainActivity> mainactivity;

        ActivityHandler(MainActivity acticity) {
            mainactivity = new WeakReference<>(acticity);
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            MainActivity acticity = mainactivity.get();

            if (acticity != null) {
                switch (msg.what) {
                    case MsgType.BATTARY:
                        String s = (String) msg.obj;

                        acticity.menuItem_battery.setTitle(s.substring(1,3)+"%");
                        Log.i(TAG, s);
                        break;
                    case MsgType.CONNTCT:

                        if((boolean)msg.obj)
                           acticity. menuItem_signal_wireless.setIcon(R.drawable.signal_wireless_full);//设置图标变绿
                        else
                            acticity. menuItem_signal_wireless.setIcon(R.drawable.signal_wireless);//设置图标变灰色
                        break;
                    default:
                        break;
                }
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Log.i(TAG, "onContextItemSelected");
        switch (item.getItemId()) {
            case R.id.set_button:
                Log.i(TAG, "set_button");
//                goToSetActivity();
                break;
            case R.id.about:
                AlertDialog.Builder builder = new AlertDialog.Builder
                        (this);
                builder.setTitle("关于");
                builder.setMessage("\t\t\t我还没想好这里写什么\n\t\t\t版本V1.0");
                builder.create().show();
                break;
            case R.id.exit:
                finish();
                break;
            case R.id.set_address:
                final EditText editText = new EditText(this);
                editText.setText("192.168.1.115");//设置默认IP地址 减少手动输入

                final Dialog dialog = new AlertDialog.Builder(this)
                        .setTitle("输入IP地址")
                        .setIcon(R.drawable.location)
                        .setView(editText)
                        .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Log.i(TAG, editText.getText().toString());

                                String string = editText.getText().toString();
                                if (isIPAddress(string)) {
//                                    IPAddress=string;//设置地址
                                    menuItem_setAddress.setTitle(string);
                                    Log.i(TAG, editText.getText().toString() + "is valid IP address");
                                    tcpConnector=new TcpConnector(IPAddress,Port,activityHandler);
                                    tcpConnector.connect2server();//连接到服务器
                                    tcpConnector.receiveStart();//开启接收数据监听

                                    Log.i(TAG,"connected");
                                } else {
                                    Toast.makeText(getBaseContext(), "错误的IP地址", Toast.LENGTH_LONG).show();
                                    Log.e(TAG, editText.getText().toString() + "is not valid IP address");

                                }

                            }
                        }).setNegativeButton("取消", null)
                        .show();
                Log.i(TAG, editText.getText().toString());
                break;
            case R.id.disconnect:
                if (tcpConnector != null) {//防止用户瞎搞
                    if (tcpConnector.isConnected())
                    {
                        tcpConnector.disconnect();
                    }
                }
                break;
            case R.id.reconnect:
                if (tcpConnector != null) {
                    if (!tcpConnector.isConnected()) {
                        tcpConnector.reconnect();
                    }
                }
                break;
            case R.id.conncetiontest:
                if (tcpConnector != null) {
                    if (tcpConnector.isConnected()) {
                        byte[] bytes=new byte[2];
                        bytes[0]=0xf;
                        bytes[1]='\n';
                        Log.i(TAG,"send test data");
                        tcpConnector.sendBytes(bytes);//发送测试数据
                    }
                }
                break;
            default:
                break;
        }
        return super.onContextItemSelected(item);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu1, menu);
        Log.i(TAG, "tp on checked changed");
        aSwitch = (Switch) menu.findItem(R.id.app_bar_switch).getActionView().findViewById(R.id.switch_for_actionbar);

        aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

                if (isChecked) {
                    gridLayout_direction_keys.setVisibility(View.INVISIBLE);
                    frameLayout_direction_bar.setVisibility(View.VISIBLE);
                } else {
                    gridLayout_direction_keys.setVisibility(View.VISIBLE);
                    frameLayout_direction_bar.setVisibility(View.INVISIBLE);
                }
            }
        });
        Log.i(TAG, "tp on checked changed");
        menuItem_setAddress = menu.findItem(R.id.set_address);
        menuItem_battery=menu.findItem(R.id.battery);
        menuItem_signal_wireless=menu.findItem(R.id.signal_ctrl);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
        robot.robotDestroy();

    }

    @Override
    protected void onPostResume() {
        super.onPostResume();
        mapView.onResume();
        Log.i(TAG, "On Resume");
    }

    @Override
    protected void onPause() {
        super.onPause();
        mapView.onPause();
        Log.i(TAG, "On Pause");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
        mapView.onSaveInstanceState(outState);
        Log.i(TAG, "On SaveInstanceState");
    }
}
