package jsat.com.sensormovementclassification;

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

public class SensorLog {

    Context mContext;
    JMLFunctions jmlfuncs;

    public SensorLog(TextView textview, Context mContext, JMLFunctions jmlfuncs){
        this.textview = textview;
        //the context passing is so this class can use getSystemService
        this.mContext = mContext;
        this.jmlfuncs = jmlfuncs;
    }

    Vector<xyzData> gyroDataVec = new Vector<xyzData>();

    public TextView textview;
    private SensorManager sensorManager;
    private SensorEventListener sensorListener;
    private Sensor senGyroscope;
    private Sensor senProximity;

    //this are cyclePrintData's params. It is needed for postDelayed loop.
    Handler h = new Handler();
    long delay = 1000; //milliseconds


    private int cycle=0;

    //Calendar c = Calendar.getInstance();
    //private int prevCycleTime = c.get(Calendar.SECOND);

    //This method will calculate the average of the data, print the data, and clear the vector
    private void cyclePrintData(TextView viewtoprint, JMLFunctions jmlfunc){

        //Skip this function if there is no data to calculate.
        if(gyroDataVec.size() == 0) return;

        //calculate averages.
        xyzData avgData = new xyzData();
        //TODO: Update this average calculation to the sliding window average method.
        int vecSize= gyroDataVec.size();
        for(int i=0; i<vecSize; i++){
            xyzData temp = gyroDataVec.get(i);
            avgData.set(temp.getX()+avgData.getX(), temp.getY()+avgData.getY(), temp.getZ()+avgData.getZ());
        }
        avgData.set(avgData.getX()/vecSize, avgData.getY()/vecSize, avgData.getZ()/vecSize);
        cycle++;
        viewtoprint.append(cycle + " - " + "X:" + Float.toString(avgData.getX()) + " Y:" + Float.toString(avgData.getY()) + " Z:" + Float.toString(avgData.getZ()) + "\n");
        String classification = jmlfunc.classifyXYZ(avgData.getX(), avgData.getY(), avgData.getZ());
        viewtoprint.append(classification + "\n");

        int scrollAmount = viewtoprint.getLayout().getLineTop(viewtoprint.getLineCount()) - viewtoprint.getHeight();
        //automatically scroll to lowest line
        if (scrollAmount > 0) viewtoprint.scrollTo(0,scrollAmount);
        else viewtoprint.scrollTo(0,0);


        //clear the vector to obtain new sensor information
        gyroDataVec.clear();
    }


    //@Override
    protected void startService() {

        sensorManager = (SensorManager) mContext.getSystemService(Context.SENSOR_SERVICE);

        //here we create our sensor listener.
        sensorListener = new SensorEventListener() {
            @Override
            public void onSensorChanged (SensorEvent event){
                Sensor sensor = event.sensor;
                //Sanity check to make sure we have the accelerometer.
                if (sensor.getType() == Sensor.TYPE_ORIENTATION) {
                    xyzData tempdata = new xyzData();
                    tempdata.set(event.values[0], event.values[1], event.values[2]);
                    gyroDataVec.add(tempdata);
                } else if (sensor.getType() == Sensor.TYPE_PROXIMITY) {
                    textview.append("Prox: " + event.values[0] + "\n");
                } else {
                    textview.append("No sensor of known type. It's " + sensor.getName() + "\n");
                }

            }

            @Override
            public void onAccuracyChanged (Sensor sensor,int accuracy){

            }
        };

        senGyroscope = sensorManager.getDefaultSensor(Sensor.TYPE_ORIENTATION); //my droid 4 doesn't have TYPE_GYROSCOPE.
        senProximity = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        sensorManager.registerListener(sensorListener, senGyroscope, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(sensorListener, senProximity, SensorManager.SENSOR_DELAY_NORMAL);

        //This will run the cyclePrintData once the set delay is up.
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
        sensorManager.unregisterListener(sensorListener);
    }



    /*
    protected void onPause() {
        super.onPause();
        sensensorManager.unregisterListener(this);
    }

    protected void onResume() {
        super.onResume();
        sensensorManager.registerListener(this, senGyroscope, SensorManager.SENSOR_DELAY_NORMAL);
    }
    */
}
