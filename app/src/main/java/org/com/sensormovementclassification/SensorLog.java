package org.com.sensormovementclassification;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.widget.TextView;

import android.os.Handler;

import java.util.Vector;


/*Dominic created class for logging accelerometer (for the moment) data
TODO: Implement functionality for more sensors.
 */

class xyzData{
    private float x = 0;
    private float y = 0;
    private float z = 0;
    public void set(float x, float y, float z){
        this.x = x;
        this.y = y;
        this.z = z;
    }
    public float getX(){ return x; }
    public float getY(){ return y; }
    public float getZ(){ return z; }

}

public class SensorLog implements SensorEventListener {

    Context mContext;
    JMLFunctions jmlfuncs;

    public SensorLog(TextView textview, Context mContext, JMLFunctions jmlfuncs){
        this.textview = textview;
        //the context passing is so this class can use getSystemService
        this.mContext = mContext;
        this.jmlfuncs = jmlfuncs;
    }

    Vector<xyzData> accelDataVec = new Vector<xyzData>();

    public TextView textview;
    private SensorManager senSensorManager;
    private Sensor senAccelerometer;

    //this are cyclePrintData's params. It is needed for postDelayed loop.
    Handler h = new Handler();
    long delay = 1000; //milliseconds


    private int cycle=0;

    //Calendar c = Calendar.getInstance();
    //private int prevCycleTime = c.get(Calendar.SECOND);

    //This method will calculate the average of the data, print the data, and clear the vector
    private void cyclePrintData(TextView viewtoprint, JMLFunctions jmlfunc){

        //Skip this function if there is no data to calculate.
        if(accelDataVec.size() == 0) return;

        //calculate averages.
        xyzData avgData = new xyzData();
        //TODO: Update this average calculation to the sliding window average method.
        int vecSize=accelDataVec.size();
        for(int i=0; i<vecSize; i++){
            xyzData temp = accelDataVec.get(i);
            avgData.set(temp.getX()+avgData.getX(), temp.getY()+avgData.getY(), temp.getZ()+avgData.getZ());
        }
        avgData.set(avgData.getX()/vecSize, avgData.getY()/vecSize, avgData.getZ()/vecSize);
        cycle++;
        //viewtoprint.append(cycle + " - " + "X:" + Float.toString(avgData.getX()) + " Y:" + Float.toString(avgData.getY()) + " Z:" + Float.toString(avgData.getZ()) + "\n");
        //TODO: CHANGE ME BACK
        //String classification = jmlfunc.classifyXYZ(avgData.getX(),avgData.getY(),avgData.getZ());
        String classification = jmlfunc.classifyY(avgData.getY());
        if(classification.equals("1")){
            viewtoprint.append("Hand!\n");
        }
        else if(classification.equals("2")){
            viewtoprint.append("Pocket!\n");
        }
        else viewtoprint.append("Invalid classification: " + classification + "\n");

        int scrollAmount = viewtoprint.getLayout().getLineTop(viewtoprint.getLineCount()) - viewtoprint.getHeight();
        //automatically scroll to lowest line
        if (scrollAmount > 0) viewtoprint.scrollTo(0,scrollAmount);
        else viewtoprint.scrollTo(0,0);


        //clear the vector to obtain new sensor information
        accelDataVec.clear();
    }


    //@Override
    protected void startService() {

        senSensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);
        senAccelerometer = senSensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION);
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);

        h.postDelayed(new Runnable(){
            public void run(){
                //do something
                cyclePrintData(textview, jmlfuncs);
                h.postDelayed(this, delay);
            }
        }, delay);
    }

    protected void stopService(){
        h.removeCallbacksAndMessages(null);
        senSensorManager.unregisterListener(this);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Sensor accelSensor = event.sensor;
        //Sanity check to make sure we have the accelerometer.
        if (accelSensor.getType() != Sensor.TYPE_ACCELEROMETER){
            //PRINT ERROR HERE
        }
        xyzData tempdata = new xyzData();
        tempdata.set(event.values[0],event.values[1],event.values[2]);
        accelDataVec.add(tempdata);


    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    /*
    protected void onPause() {
        super.onPause();
        senSensorManager.unregisterListener(this);
    }

    protected void onResume() {
        super.onResume();
        senSensorManager.registerListener(this, senAccelerometer, SensorManager.SENSOR_DELAY_NORMAL);
    }
    */
}
