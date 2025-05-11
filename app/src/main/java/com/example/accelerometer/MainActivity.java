package com.example.accelerometer;
import androidx.appcompat.app.AppCompatActivity;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    TextView acceleration_x;//x方向的加速度
    TextView acceleration_y;//y方向的加速度
    TextView acceleration_z;//z方向的加速度
    TextView acceleration_total;//显示总加速度
    //显示运动情况
    TextView ifmove;
    TextView step_count; // 显示步数的文本视图
    Button reset_button; // 清零按钮
    
    // 步数相关变量
    private int stepCounter = 0;
    private float lastMagnitude = 0;
    private boolean isStep = false;
    private static final float STEP_THRESHOLD = 12.0f; // 步数阈值，可根据需要调整
    SensorManager mySensorManager;//SensorManager对象引用
    //SensorManagerSimulator mySensorManager;//声明SensorManagerSimulator对象,调试时用
    @Override
    public void onCreate(Bundle savedInstanceState) {//重写onCreate方法
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);//设置当前的用户界面
        acceleration_x = (TextView) findViewById(R.id.acceleration_x);//得到acceleration_x的引用
        acceleration_y = (TextView) findViewById(R.id.acceleration_y);//得到acceleration_y的引用
        acceleration_z = (TextView) findViewById(R.id.acceleration_z);//得到acceleration_z的引用
        acceleration_total = (TextView) findViewById(R.id.acceleration_total);//得到acceleration_total的引用

        //设置一个用于判断是否运动的控件
        ifmove = (TextView) findViewById(R.id.ifmove);//得到ifmove的引用
        
        // 初始化步数显示和清零按钮
        step_count = (TextView) findViewById(R.id.step_count);
        reset_button = (Button) findViewById(R.id.reset_button);
        
        // 设置清零按钮点击事件
        reset_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stepCounter = 0;
                step_count.setText(String.valueOf(stepCounter));
            }
        });
        
        mySensorManager = (SensorManager)getSystemService(SENSOR_SERVICE);//获得SensorManager
    }
    private SensorEventListener mySensorListener = new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent sensorEvent) {
            if (sensorEvent.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                float[] values = sensorEvent.values;
                //通过开平方和得到总加速度
                float total_acceleration = (float) Math.sqrt(sensorEvent.values[0] * sensorEvent.values[0]
                        + sensorEvent.values[1] * sensorEvent.values[1]
                        + sensorEvent.values[2] * sensorEvent.values[2]);

                //设置加速度的显示情况
                acceleration_x.setText(String.valueOf(sensorEvent.values[0]));
                acceleration_y.setText(String.valueOf(sensorEvent.values[1]));
                acceleration_z.setText(String.valueOf(sensorEvent.values[2]));
                acceleration_total.setText(String.valueOf(total_acceleration));

                // 步数检测算法
                detectStep(total_acceleration);

                //通过与本地9.8左右的加速度进行比较从而判断手机是否运动
                //因为实际本地加速度会在9.8-9.9之间浮动，通过物理知识可知小于9.8是在上升，大于9.9是在下降
                if(total_acceleration < 9.9 && total_acceleration > 9.8){
                    ifmove.setText("At rest" );
                }
                else if(total_acceleration >= 9.9){
                    ifmove.setText("In motion,downing" );
                }
                else if(total_acceleration <= 9.8){
                    ifmove.setText("In motion,uping" );
                }
            }
        }
        @Override
        public void onAccuracyChanged(Sensor sensor, int i) {
        }
    };
    @Override
    protected void onResume() {//重写的onResume方法
        mySensorManager.registerListener(//注册监听
                mySensorListener, //监听器SensorListener对象
                mySensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),//传感器的类型为加速度
                SensorManager.SENSOR_DELAY_UI//传感器事件传递的频度
        );
        super.onResume();
    }
    @Override
    protected void onPause() {//重写onPause方法
        mySensorManager.unregisterListener(mySensorListener);//取消注册监听器
        super.onPause();
    }
    
    // 添加步数检测方法
    private void detectStep(float magnitude) {
        // 简单的阈值检测算法
        if (magnitude > STEP_THRESHOLD && !isStep && magnitude > lastMagnitude) {
            isStep = true;
            stepCounter++;
            step_count.setText(String.valueOf(stepCounter));
        } else if (magnitude < STEP_THRESHOLD - 2) {
            isStep = false;
        }
        
        lastMagnitude = magnitude;
    }
}