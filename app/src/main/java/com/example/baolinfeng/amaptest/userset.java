package com.example.baolinfeng.amaptest;

import android.content.pm.ActivityInfo;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class userset extends AppCompatActivity {

    private final String TAG="mydebug";
    private List<Map<String,Object>> dataList;
    private ListView listView;
    private Button buttonOk;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.i(TAG,"userset view1");
//        if(getRequestedOrientation()!=ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE){
//            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
//        }
        Log.i(TAG,"userset view2");
        setContentView(R.layout.userset);
//        setTheme(R.style.myTransparent);
        Log.i(TAG,"userset view3");
        setDataInitialize();
        initListView();
        buttonOk=findViewById(R.id.buttonOK);
        buttonOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }
    private void setDataInitialize()
    {
        dataList=new ArrayList<>();
        Log.i(TAG,"tp5");
        Map<String,Object> map;
        for(int i=0;i<15;i++)
        {
            map=new HashMap<>();
            map.put("set_item","test string"+i);
            map.put("image",R.drawable.set_icon);
            dataList.add(map);
        }
        Log.i(TAG,"tp6");
    }
    private void initListView()
    {
        Log.i(TAG,"tp6");
        listView=findViewById(R.id.set_list);
        Log.i(TAG,"tp4");
        SetupAdapter setupAdapter=new SetupAdapter(this,dataList);
        listView.setAdapter(setupAdapter);
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i(TAG,"uonDestroy");
    }
}
