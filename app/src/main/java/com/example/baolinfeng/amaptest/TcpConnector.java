package com.example.baolinfeng.amaptest;

import android.os.Message;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;


class MsgType{
  final  static int CONNTCT=0x00;
    final static int BATTARY=0x11;
    final  static int MODE=0x33;
    final static int CTRL=0x44;//控制信息 速度 角速度等
    final static  int TEST=0xff;
}
class NAVMODE{
    final  static int AUTO=0x00;
    final  static int CTRL=0x11;
}
class ConncetState{
}
public class TcpConnector {
    private String TAG = "mydebug";
    private Socket socket;
    private BufferedReader bufferedReader;
    private OutputStream outputStream;
    private String ipAddress;
    private static int port = 0000;
    private boolean connected = false;
    private int DATA_BYTES_SIZE = 13;
    private byte[] databuff;
    private MainActivity.ActivityHandler activityHandler;


    public TcpConnector(String ip, int port, MainActivity.ActivityHandler handler) {
        this.ipAddress = ip;
        this.port = port;
        this.activityHandler = handler;
    }

    boolean connect2server() {
        connected = false;
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    socket = new Socket(ipAddress, port);
                    bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
                    outputStream = socket.getOutputStream();
                    connected = true;
                    send2ui(MsgType.CONNTCT,isConnected());
                    Log.i(TAG, "连接成功");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
        return connected;
    }

    void sendBytes(final byte[] bytes) {

        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    outputStream.write(bytes);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    void receiveStart() {
        databuff = new byte[DATA_BYTES_SIZE];

        new Thread() {
            @Override
            public void run() {
                String datastring;
                while (true) {//改进
                    if(connected) {
                        try {
                            datastring = bufferedReader.readLine();
                            if (datastring != null) {
                                databuff = datastring.getBytes();
                                send2ui(MsgType.BATTARY, datastring);
//                                Log.i(TAG, "" + datastring);
                         }
                        } catch (Exception e) {
                            e.printStackTrace();
//                            Log.i(TAG, "receive failed");
//                        break;
                        }
                    }
                }
            }
        }.start();
    }

    public void disconnect() {
        new Thread() {
            @Override
            public void run() {
                super.run();
                try {
                    socket.close();
                    socket=null;
                    connected = false;
                    send2ui(MsgType.CONNTCT,isConnected());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }.start();

    }

    public void reconnect() {
        connect2server();

    }

    public boolean isConnected() {
        return connected;
    }

    private void send2ui(int what,Object s) {
        Message msg = new Message();
        msg.what =what;
        msg.obj = s;
        activityHandler.sendMessage(msg);
    }
}
