package com.example.baolinfeng.amaptest;

import android.content.Context;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageButton;

public class DirectionBar extends FrameLayout  {

    private ImageButton touch_area;
    private ImageButton rocker;
    private static float touch_area_center_x;
    private static float touch_area_center_y;
    private float touch_area_radius;
    private Context context;
    private final String TAG = "mydebug";
    private float touchX;
    private float touchY;
    private float touchTocenter2;
    private double angle;
    private float rocker_radius;

    public DirectionBar(Context context) {
        super(context);
        Log.i(TAG, "tp4 ");

        Log.i(TAG, "tp6 ");
//        touch_area.setOnTouchListener(new View.OnTouchListener(){
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//             Log.i(TAG,"x= "+event.getX()+"  y="+event.getY());
//                return false;
//            }
//        });
        Log.i(TAG, "tp7 ");

    }

    public void initialize(ImageButton touch_area, ImageButton rocker,View.OnTouchListener listener) {

        this.touch_area = touch_area;
        touch_area.setOnTouchListener(listener);
        this.rocker = rocker;
        rocker.setVisibility(INVISIBLE);

        touch_area_center_x = (touch_area.getLeft() + touch_area.getRight()) / 2;
        touch_area_center_y = (touch_area.getTop() + touch_area.getBottom()) / 2;

        Log.i(TAG, "Center x=" + touch_area_center_x + " y=" + touch_area_center_y);


    }

    public void setRocker(ImageButton rocker) {


    }


    public void touchOnActionDown()
    {
        rocker.setVisibility(VISIBLE);
        Log.i(TAG, "area left = " + touch_area.getLeft() + "  Y= " + touch_area.getRight());
        touch_area_center_x = (touch_area.getLeft() + touch_area.getRight()) / 2;
        touch_area_center_y = (touch_area.getTop() + touch_area.getBottom()) / 2;
        touch_area_radius = touch_area_center_x;
        rocker_radius = 0.5f * (rocker.getBottom() - rocker.getTop());
        Log.i(TAG, "Center x=" + touch_area_center_x + " y=" + touch_area_center_y);
        Log.e(TAG, "Process.myUid() = " + android.os.Process.myTid());
    }
    public void touchOnActionUp()
    {
        rocker.setVisibility(INVISIBLE);
        //圆点回归中心
//        rocker.setX(touch_area_center_x);
//        rocker.setY(touch_area_center_y);
    }
    public void touchOnActionMove( MotionEvent event)
    {
        touchX = event.getX();
        touchY = event.getY();
//        Log.i(TAG, "touch x=" + touchX + " y=" + touchY);
        angle = calculateAngle(touchX, touchY, touch_area_center_x, touch_area_center_y);
//        Log.i(TAG, "angle=" + angle);
//        Log.i(TAG, "rocker:" + 0.5 * (rocker.getRight() - rocker.getLeft()));
        touchTocenter2 = (touchX - touch_area_center_x) * (touchX - touch_area_center_x) +
                (touchY - touch_area_center_y) * (touchY - touch_area_center_y);

        if (touchTocenter2 > (touch_area_center_x - rocker_radius) * (touch_area_center_x - rocker_radius)) {

            Log.e(TAG, "超出触摸区域" + angle);

            rocker.setX((float) (touch_area_radius + (touch_area_radius - rocker_radius) * Math.cos(angle * Math.PI / 180)
                    - rocker_radius));
            rocker.setY((float) (touch_area_radius - (touch_area_radius - rocker_radius) * Math.sin(angle * Math.PI / 180)
                    - rocker_radius));
        } else {
            rocker.setX(event.getX() - rocker_radius);
            rocker.setY(event.getY() - rocker_radius);

        }

    }


    public float getTouchTocenter2() {
        return touchTocenter2;
    }

    public double getAngle() {
        return angle;
    }

    private double calculateAngle(float x, float y, float centerx, float centery) {
        double angle;
        float tanavlue;//正切
        if (x == centerx) {
            if (y < centery)
                angle = 90;
            else
                angle = 270;
        } else if (x < centerx) {
            tanavlue = -(y - centery) / (x - centerx);//第二、三象限
            angle = Math.atan(tanavlue) * 180 / Math.PI + 180;
        } else if (y > centery) {
            tanavlue = -(y - centery) / (x - centerx);//第四象限
            angle = Math.atan(tanavlue) * 180 / Math.PI + 360;
        } else {
            tanavlue = -(y - centery) / (x - centerx);
            angle = Math.atan(tanavlue) * 180 / Math.PI;
        }

        return angle;
    }

}
