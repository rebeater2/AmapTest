package com.example.baolinfeng.amaptest;

import android.util.Log;

import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.animation.Animation;
import com.amap.api.maps.model.animation.AnimationSet;
import com.amap.api.maps.model.animation.RotateAnimation;
import com.amap.api.maps.model.animation.TranslateAnimation;

import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import static com.example.baolinfeng.amaptest.MainActivity.TAG;

public class Robot {
    private LatLng location;//位置
    List<LatLng> track;
    private float direction;//方向0~360
    private double velocity;//前进速度
    private Marker marker;
    private TimerTask timerTaskMove;
    private TimerTask timerTaskMoveWhthDireactionbar;
    private double angleFromDirectionBar;
    private Timer timer;

    public Robot(LatLng location, float direction, double velocity, Marker marker) {
        this.location = location;
        timer = new Timer(true);

        if (direction > 360)
            this.direction = direction - 360;
        else if (direction < 0)
            this.direction = direction + 360;
        else
            this.direction = direction;

        if (velocity > 0)
            this.velocity = velocity;
        else
            this.velocity = 0;
        this.marker = marker;
    }

    public void setLocation(LatLng location) {
        this.location = location;
    }

    public void setDirection(float direction) {

        this.direction = direction;
    }

    public void setVelocity(double velocity) {
        if (velocity > 0)
            this.velocity = velocity;
        else
            this.velocity = 0;
    }

    public LatLng getLocation() {
        return location;
    }

    public float getDirection() {
        return direction;
    }

    public double getVelocity() {
        return velocity;
    }

    public void moveOn() {
        timerTaskMove = new TimerTask() {
            @Override
            public void run() {
                LatLng newlocation = new LatLng(
                        location.latitude + velocity * Math.cos(direction * Math.PI / 180),
                        location.longitude - velocity * Math.sin(direction * Math.PI / 180)
                );
                Animation animation = new TranslateAnimation(newlocation);
                animation.setDuration(150);
                marker.setAnimation(animation);
                marker.startAnimation();
                location = newlocation;
                Log.i(TAG, "move On" + "direction:" + direction + "velocity=" + velocity);
                Log.i(TAG, "timer event " + this.toString());
            }
        };
        timer.schedule(timerTaskMove, 0, 200);
    }

    public void stop() {
        timerTaskMove.cancel();
//                    timer.cancel();
        timer.purge();
        Log.i(TAG, "timer cancel");
    }


    public void moveBack() {
        timerTaskMove = new TimerTask() {
            @Override
            public void run() {
                LatLng newlocation = new LatLng(
                        location.latitude - velocity * Math.cos(direction * Math.PI / 180),
                        location.longitude + velocity * Math.sin(direction * Math.PI / 180)
                );
                Animation animation = new TranslateAnimation(newlocation);
                animation.setDuration(80);
                marker.setAnimation(animation);
                marker.startAnimation();
                location = newlocation;
                Log.i(TAG, "move back" + "direction:" + direction + "velocity=" + velocity);
            }
        };
        timer.schedule(timerTaskMove, 0, 100);
    }

    public void turnLeft(float angle) {
        final float angletemp = angle;
        timerTaskMove = new TimerTask() {
            @Override
            public void run() {
                Animation animation = new RotateAnimation(
                        marker.getRotateAngle(),//旋转目标
                        direction,//起始角度
                        direction + angletemp,//结束角度
                        50,
                        50
                );
                animation.setDuration(80);
                marker.setAnimation(animation);
                marker.startAnimation();
                direction = direction + angletemp;
                Log.i(TAG, "turn left");
                Log.i(TAG, "direction:" + direction);
            }
        };
        timer.schedule(timerTaskMove, 0, 100);
    }

    public void turnRight(float angle) {
        final float angletemp = angle;
        timerTaskMove = new TimerTask() {
            @Override
            public void run() {
                Animation animation = new RotateAnimation(
                        marker.getRotateAngle(),//旋转目标
                        direction,//起始角度
                        direction - angletemp,//结束角度
                        50,
                        50
                );
                animation.setDuration(80);
                marker.setAnimation(animation);
                marker.startAnimation();
                direction = direction - angletemp;
                Log.i(TAG, "turn left");
                Log.i(TAG, "direction:" + direction);

            }
        };
        timer.schedule(timerTaskMove, 0, 100);
    }

    public void robotDestroy()//销毁timer对象，必须在MainActivity中调用
    {
        if (timerTaskMove != null) {
            timerTaskMove.cancel();
            timerTaskMove = null;
        }
        if (timerTaskMoveWhthDireactionbar != null) {
            timerTaskMoveWhthDireactionbar.cancel();
            timerTaskMoveWhthDireactionbar = null;
        }
        if (timer != null) {
            timer.cancel();
            timer.purge();
            timer = null;
        }
    }

    /********************************************************************
     * 单次移动，用于方向手柄控制
     *
     */
    private void moveOnOnce() {
        LatLng newlocation = new LatLng(
                location.latitude + velocity * Math.cos(direction * Math.PI / 180),
                location.longitude - velocity * Math.sin(direction * Math.PI / 180)
        );
        Animation animation = new TranslateAnimation(newlocation);
        animation.setDuration(10);
        marker.setAnimation(animation);
        marker.startAnimation();//开始动画
        location = newlocation;
        Log.i(TAG, "move On once " + "direction:" + direction + "velocity=" + velocity);

    }

    private void moveBackOnce() {
        LatLng newlocation = new LatLng(
                location.latitude - velocity * Math.cos(direction * Math.PI / 180),
                location.longitude + velocity * Math.sin(direction * Math.PI / 180)
        );
        Animation animation = new TranslateAnimation(newlocation);
        animation.setDuration(1);
        marker.setAnimation(animation);
        marker.startAnimation();
        location = newlocation;
        Log.i(TAG, "move back once" + "direction:" + direction + "velocity=" + velocity);
    }

    private void turnLeftOnce(float angletemp) {
        Animation animation = new RotateAnimation(
                marker.getRotateAngle(),//旋转目标
                direction,//起始角度
                direction + angletemp,//结束角度
                50,
                50
        );//参数起旋转速度的作用
        animation.setDuration(10);
        marker.setAnimation(animation);
        marker.startAnimation();
        direction = direction + angletemp;
        Log.i(TAG, "turn left once" + "direction:" + direction + "velocity=" + velocity);
    }

    private void turnRightOnce(float angletemp) {
        Animation animation = new RotateAnimation(
                marker.getRotateAngle(),//旋转目标
                direction,//起始角度
                direction - angletemp,//结束角度
                50,
                50
        );
        animation.setDuration(1);
        marker.setAnimation(animation);
        marker.startAnimation();
        direction = direction - angletemp;
        Log.i(TAG, "turn right once" + "direction:" + direction + "velocity=" + velocity);
//        Log.i(TAG, "direction:" + direction);
    }

    public void timerForDirectionBarStart() {
        timerTaskMoveWhthDireactionbar = new TimerTask() {
            @Override
            public void run() {
                moveWithAngeleAndSpeedOnce(angleFromDirectionBar);
                Log.i(TAG, "angleFromDirectionBar=" + angleFromDirectionBar);
            }
        };
        timer.schedule(timerTaskMoveWhthDireactionbar, 0, 100);
        Log.i(TAG, "timerTaskMoveWhthDireactionbar start");
    }

    public void timerForDirectionBarStop() {
        timerTaskMoveWhthDireactionbar.cancel();
//                    timer.cancel();
        timer.purge();
        Log.i(TAG, "timerTaskMoveWhthDireactionbar cancel");
    }

    public void updateAngleFromActionBar(double angleFromDirectionBar) {
        this.angleFromDirectionBar = angleFromDirectionBar;
        Log.i(TAG, "angle updated");
    }

    public void moveWithAngeleAndSpeedOnce(double angleFromDirectionBar) {
        Log.i(TAG, "moveWithAngleAndSpeedOnce! angle=" + angleFromDirectionBar);
        double velocity, omega, scale = 1e6;//按照比例划分速度和角速度 scale 比例
        velocity = this.velocity * Math.cos((angleFromDirectionBar - 90) * Math.PI / 180);
        omega = scale * this.velocity * Math.cos((angleFromDirectionBar - Math.PI / 2) * Math.PI / 180);
        Log.i(TAG, "velocity=" + velocity + "  omega=" + omega);
        //同时进行旋转和平移 组合动画实现
        //旋转动画
        float newdirection;
        if ((angleFromDirectionBar > 90) || (angleFromDirectionBar < 180)) //左转
            newdirection = direction - (float) omega;
        else
            newdirection = direction - (float) omega;
        RotateAnimation rotationAni = new RotateAnimation(
                direction,//旋转目标
                newdirection);//起始角度
        rotationAni.setDuration(10);
        //位移动画
        LatLng newlocation = new LatLng(
                location.latitude + velocity * Math.cos(direction * Math.PI / 180),
                location.longitude - velocity * Math.sin(direction * Math.PI / 180)
        );
        TranslateAnimation tanslateAni = new TranslateAnimation(newlocation);
        tanslateAni.setDuration(10);
        AnimationSet animationSet = new AnimationSet(true);
        //必须要先添加旋转动画，后添加平移动画才可以实现旋转平移同时进行的效果
        animationSet.addAnimation(rotationAni);
        animationSet.addAnimation(tanslateAni);
//        animationSet.setDuration(40);
        marker.setAnimation(animationSet);
        marker.startAnimation();//开始动画
        location = newlocation;
        direction = newdirection;
        Log.i(TAG, "move On once " + "direction:" + direction + "velocity=" + velocity);

    }

    private void moveWithAngleFromDirectionBar(double angleFromDirectionBar) {

        Log.i(TAG, "Move with Direction Bar");
        if (angleFromDirectionBar > 80 && angleFromDirectionBar < 100) {
            moveOnOnce();  //发送一次前进指令
        } else if (angleFromDirectionBar >= 50 && angleFromDirectionBar <= 80) {
            turnRightOnce(1);

        } else if (angleFromDirectionBar >= 20 && angleFromDirectionBar < 50) {
            turnRightOnce(2);

        } else if ((angleFromDirectionBar >= 0 && angleFromDirectionBar <= 20)
                || (angleFromDirectionBar >= 340 && angleFromDirectionBar < 360)) {
            turnRightOnce(3);

        } else if (angleFromDirectionBar >= 280 && angleFromDirectionBar < 340) {
            turnRightOnce(4);

        } else if (angleFromDirectionBar >= 260 && angleFromDirectionBar < 280) {
            moveBackOnce();

        } else if (angleFromDirectionBar >= 200 && angleFromDirectionBar < 260) {
            turnLeftOnce(4);

        } else if (angleFromDirectionBar >= 160 && angleFromDirectionBar < 200) {
            turnLeftOnce(3);

        } else if (angleFromDirectionBar >= 120 && angleFromDirectionBar < 160) {
            turnLeftOnce(2);

        } else if (angleFromDirectionBar >= 100 && angleFromDirectionBar < 120) {
            turnLeftOnce(1);

        } else {
            ;
        }
    }
}
