package com.example.haotian.haotianalp;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public class ALPActivity extends Activity implements SensorEventListener{
    protected LockPatternView mPatternView;
    protected PatternGenerator mGenerator;
    protected Button mGenerateButton;
    protected Button mDesigner;
    protected ToggleButton mPracticeToggle;
    private List<Point> mEasterEggPattern;
    protected SharedPreferences mPreferences;
    protected int mGridLength=0;
    protected int mPatternMin=0;
    protected int mPatternMax=0;
    protected String mHighlightMode;
    protected boolean mTactileFeedback;
    // Sensor data storage
    protected float accelerometer_x = 0;
    protected float accelerometer_y = 0;
    protected float accelerometer_z = 0;
    protected float magnetic_field_x = 0;
    protected float magnetic_field_y = 0;
    protected float magnetic_field_z = 0;
    protected float gyroscope_x = 0;
    protected float gyroscope_y = 0;
    protected float gyroscope_z = 0;
    protected float rotation_vector_x = 0;
    protected float rotation_vector_y = 0;
    protected float rotation_vector_z = 0;
    protected float linear_acceleration_x = 0;
    protected float linear_acceleration_y = 0;
    protected float linear_acceleration_z = 0;
    protected float gravity_x = 0;
    protected float gravity_y = 0;
    protected float gravity_z = 0;

    private static final String TAG = "SensorActivity";
    private static final String TAGmotion = "motionEvent";
    private SensorManager mSensorManager = null;

    public List<Sensor> deviceSensors;
    private  Sensor mAccelerometer, mMagnetometer, mGyroscope, mRotation, mGravity, myLinearAcc;

    private File file;
    public static String[] mLine;
    public BufferedWriter bufferedWriter;
    private VelocityTracker mVelocityTracker = null;
    private int control = 0;
    DateFormat mDateFormat;
    String mTimestamp;
    private int counter=0;
    private String myStr = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPreferences = PreferenceManager.getDefaultSharedPreferences(this);

        mSensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mMagnetometer = mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
        mGyroscope = mSensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        mRotation = mSensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        mGravity = mSensorManager.getDefaultSensor(Sensor.TYPE_GRAVITY);
        myLinearAcc = mSensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);

        mGenerator = new PatternGenerator();
        setContentView(R.layout.activity_alp);
        mPatternView = (LockPatternView) findViewById(R.id.pattern_view);
        mGenerateButton = (Button) findViewById(R.id.generate_button);
        mPracticeToggle = (ToggleButton) findViewById(R.id.practice_toggle);

        // create file for which to save motion data
        String root = Environment.getExternalStorageDirectory().toString();
        file = new File(root + "/DCIM", "motiondata" + System.currentTimeMillis() + ".csv");

        // create BufferedWriter
        try {
            bufferedWriter = new BufferedWriter(new FileWriter(file));
            bufferedWriter.write("TYPE_ACCELEROMETER_X,TYPE_ACCELEROMETER_Y," +
                    "TYPE_ACCELEROMETER_Z,TYPE_MAGNETIC_FIELD_X,TYPE_MAGNETIC_FIELD_Y," +
                    "TYPE_MAGNETIC_FIELD_Z,TYPE_GYROSCOPE_X,TYPE_GYROSCOPE_Y,TYPE_GYROSCOPE_Z," +
                    "TYPE_ROTATION_VECTOR_X,TYPE_ROTATION_VECTOR_Y,TYPE_ROTATION_VECTOR_Z," +
                    "TYPE_LINEAR_ACCELERATION_X,TYPE_LINEAR_ACCELERATION_Y,TYPE_LINEAR_ACCELERATION_Z," +
                    "TYPE_GRAVITY_X,TYPE_GRAVITY_Y,TYPE_GRAVITY_Z,position_X,position_Y,velocity_X,velocity_Y,pressure,size,mCurrentPattern,counter\n");
            bufferedWriter.flush();
        }
        catch(Exception e){
            System.out.println(e.toString());
            System.out.println("bw error");
        }

        mGenerateButton.setOnClickListener(new Button.OnClickListener(){

            @Override
            public void onClick(View view) {
                mPatternView.setPattern(mGenerator.getPattern());
                mPatternView.invalidate();
            }

        });

        mGenerateButton.setOnLongClickListener(new Button.OnLongClickListener(){

            public boolean onLongClick(View v){
                int d = 90;
                for (int i = 1; i<20; i ++){
                    new android.os.Handler().postDelayed(
                            new Runnable() {
                                public void run() {
                                    mPatternView.setPattern(mGenerator.getPattern());
                                    mPatternView.invalidate();;
                                }
                            },
                            (d = (int)(d* 1.2)));}
                return true;
            }
        });

        mPracticeToggle.setOnCheckedChangeListener(
                new ToggleButton.OnCheckedChangeListener() {
                    public void onCheckedChanged(CompoundButton buttonView,
                                                 boolean isChecked) {
                        if(isChecked){
                            mGenerateButton.setEnabled(false);
                            mPatternView.setPracticeMode(true);
                        }
                        else{
                            mGenerateButton.setEnabled(true);
                            mPatternView.setPracticeMode(false);
                        }
                    }
                });
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        mSensorManager.registerListener(this, mAccelerometer, mSensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mGravity, mSensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mGyroscope, mSensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mMagnetometer, mSensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, mRotation, mSensorManager.SENSOR_DELAY_NORMAL);
        mSensorManager.registerListener(this, myLinearAcc, mSensorManager.SENSOR_DELAY_NORMAL);
        updateFromPrefs();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_al, menu);
        return true;
    }

    @Override
    protected void onPause() {

        super.onPause();
        mSensorManager.unregisterListener(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        try {
            bufferedWriter.close();
        }
        catch (java.io.IOException e){
            System.out.println(e.toString());
            System.out.println("closing error");
        }
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    // test file saving method
    public void saveMotionData(ArrayList<String> data)
    {
        for(int i = 0; i < data.size(); i++){
            try {
                bufferedWriter.write(data.get(i));
            }
            catch(Exception e){
                System.out.println(e.toString());
            }
        }

        try{
            bufferedWriter.flush();
        }
        catch(Exception e){
            System.out.println(e.toString());
        }
    }

    private void updateFromPrefs()
    {
        int gridLength =
                mPreferences.getInt("grid_length", Defaults.GRID_LENGTH);
        int patternMin =
                mPreferences.getInt("pattern_min", Defaults.PATTERN_MIN);
        int patternMax =
                mPreferences.getInt("pattern_max", Defaults.PATTERN_MAX);
        String highlightMode =
                mPreferences.getString("highlight_mode", Defaults.HIGHLIGHT_MODE);
        boolean tactileFeedback = mPreferences.getBoolean("tactile_feedback",
                Defaults.TACTILE_FEEDBACK);

        // sanity checking
        if(gridLength < 1)
        {
            gridLength = 1;
        }
        if(patternMin < 1)
        {
            patternMin = 1;
        }
        if(patternMax < 1)
        {
            patternMax = 1;
        }
        int nodeCount = (int) Math.pow(gridLength, 2);
        if(patternMin > nodeCount)
        {
            patternMin = nodeCount;
        }
        if(patternMax > nodeCount)
        {
            patternMax = nodeCount;
        }
        if(patternMin > patternMax)
        {
            patternMin = patternMax;
        }

        // only update values that differ
        if(gridLength != mGridLength)
        {
            setGridLength(gridLength);
        }
        if(patternMax != mPatternMax)
        {
            setPatternMax(patternMax);
        }
        if(patternMin != mPatternMin)
        {
            setPatternMin(patternMin);
        }
        if(!highlightMode.equals(mHighlightMode))
        {
            setHighlightMode(highlightMode);
        }
        if(tactileFeedback ^ mTactileFeedback)
        {
            setTactileFeedback(tactileFeedback);
        }
    }

    private void setGridLength(int length)
    {
        mGridLength = length;
        mGenerator.setGridLength(length);
        mPatternView.setGridLength(length);
    }
    private void setPatternMin(int nodes)
    {
        mPatternMin = nodes;
        mGenerator.setMinNodes(nodes);
    }
    private void setPatternMax(int nodes)
    {
        mPatternMax = nodes;
        mGenerator.setMaxNodes(nodes);
    }
    private void setHighlightMode(String mode)
    {
        if("no".equals(mode))
        {
            mPatternView.setHighlightMode(new LockPatternView.NoHighlight());
        }
        else if("first".equals(mode))
        {
            mPatternView.setHighlightMode(new LockPatternView.FirstHighlight());
        }
        else if("rainbow".equals(mode))
        {
            mPatternView.setHighlightMode(
                    new LockPatternView.RainbowHighlight());
        }

        mHighlightMode = mode;
    }
    private void setTactileFeedback(boolean enabled)
    {
        mTactileFeedback = enabled;
        mPatternView.setTactileFeedbackEnabled(enabled);
    }

    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if(sensorEvent.sensor.equals(mAccelerometer)){
            accelerometer_x = sensorEvent.values[0];
            accelerometer_y = sensorEvent.values[1];
            accelerometer_z = sensorEvent.values[2];
        }
        if(sensorEvent.sensor.equals(mGravity)){
            gravity_x = sensorEvent.values[0];
            gravity_y = sensorEvent.values[1];
            gravity_z = sensorEvent.values[2];
        }
        if(sensorEvent.sensor.equals(mGyroscope)){
            gyroscope_x = sensorEvent.values[0];
            gyroscope_y = sensorEvent.values[1];
            gyroscope_z = sensorEvent.values[2];
        }
        if(sensorEvent.sensor.equals(mMagnetometer)){
            magnetic_field_x = sensorEvent.values[0];
            magnetic_field_y = sensorEvent.values[1];
            magnetic_field_z = sensorEvent.values[2];
        }
        if(sensorEvent.sensor.equals(mRotation)){
            rotation_vector_x = sensorEvent.values[0];
            rotation_vector_y = sensorEvent.values[1];
            rotation_vector_z = sensorEvent.values[2];
        }
        if(sensorEvent.sensor.equals(myLinearAcc)){
            linear_acceleration_x = sensorEvent.values[0];
            linear_acceleration_y = sensorEvent.values[1];
            linear_acceleration_z = sensorEvent.values[2];
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {
        // Do something if sensor accuracy changes
    }
}
