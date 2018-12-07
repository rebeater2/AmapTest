package com.example.baolinfeng.amaptest;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;
import java.util.Map;

public class SetupAdapter extends BaseAdapter {
    Context context;
    List<Map<String, Object>> dataList;
    private final String TAG="mydebug";
    public SetupAdapter(Context context, List<Map<String, Object>> dataList) {
        this.context = context;
        this.dataList = dataList;
    }

    @Override
    public int getCount() {
        return dataList.size();
    }

    @Override
    public Object getItem(int position) {
        return dataList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        Log.i(TAG,"tp1");
        if(convertView==null)
            convertView=LayoutInflater.from(context).inflate(R.layout.set_items,null);
        ImageView imageView=convertView.findViewById(R.id.image_item);
        CheckBox checkBox=convertView.findViewById(R.id.checkBox_item);
        TextView textView =convertView.findViewById(R.id.text_item);
        Log.i(TAG,String.valueOf(checkBox.isChecked()));
        Log.i(TAG,"tp2");
        Map<String,Object> map = dataList.get(position);
        Log.i(TAG,"tp2");
        imageView.setImageResource((Integer) map.get("image"));
        textView.setText((String)map.get("set_item"));
        Log.i(TAG,"tp3");

        return convertView;

    }
}
