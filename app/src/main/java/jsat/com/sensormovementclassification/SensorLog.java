package jsat.com.sensormovementclassification;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Environment;
import android.widget.TextView;

import android.os.Handler;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import jsat.ARFFLoader;
import jsat.classifiers.CategoricalData;
import jsat.classifiers.ClassificationDataSet;
import jsat.linear.DenseVector;
import jsat.linear.Vec;


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
    Vector<xyzData> accelDataVec = new Vector<xyzData>();
    Vector<Float> lightDataVec = new Vector<>();

    public TextView textview;
    private SensorManager sensorManager;
    private SensorEventListener sensorListener;
    private Sensor senGyroscope;
    private Sensor senProximity;
    private Sensor senLight;

    //are we logging the data? TODO: fix me up. This is sloppy.
    boolean log;
    String filename;

    //Dataset for when we log data. 7 attributes, 7 attribute informations, 1 target class(predicting)
    ClassificationDataSet datatolog = new ClassificationDataSet(7, new CategoricalData[7], new CategoricalData(1));


    //this are cyclePrintData's params. It is needed for postDelayed loop.
    Handler h = new Handler();
    long delay = 1000; //milliseconds


    private int cycle=0;

    //Calendar c = Calendar.getInstance();
    //private int prevCycleTime = c.get(Calendar.SECOND);

    private xyzData avgXYZ(Vector<xyzData> datatoavg){
        xyzData avgData = new xyzData();
        //TODO: Update this average calculation to the sliding window average method.
        int vecSize= datatoavg.size();
        for(int i=0; i<vecSize; i++){
            xyzData temp = datatoavg.get(i);
            avgData.set(temp.getX()+avgData.getX(), temp.getY()+avgData.getY(), temp.getZ()+avgData.getZ());
        }
        avgData.set(avgData.getX()/vecSize, avgData.getY()/vecSize, avgData.getZ()/vecSize);
        return avgData;
    }

    private float avgFloat(Vector<Float> datatoavg){
        float returnval = 0;
        int vecSize = datatoavg.size();
        for(int i=0; i<vecSize; i++){
            returnval += datatoavg.get(i);
        }
        return returnval;
    }

    //This method will calculate the average of the data, print the data, and clear the vector
    private void cyclePrintData(TextView viewtoprint, JMLFunctions jmlfunc){

        //Skip this function if there is no data to calculate.
        if(gyroDataVec.size() == 0) return;

        //calculate averages.
        xyzData gyroavg = avgXYZ(gyroDataVec);
        xyzData accelavg = avgXYZ(accelDataVec);
        Float lightavg = avgFloat(lightDataVec);

        cycle++;
        //TODO: Print the other sensors in this loop.
        viewtoprint.append("gyro - " + "X:" + Float.toString(gyroavg.getX()) + " Y:" + Float.toString(gyroavg.getY()) + " Z:" + Float.toString(gyroavg.getZ()) + "\n");
        String classification = jmlfunc.classifyXYZ(gyroavg.getX(), gyroavg.getY(), gyroavg.getZ());
        viewtoprint.append(classification + "\n");

        int scrollAmount = viewtoprint.getLayout().getLineTop(viewtoprint.getLineCount()) - viewtoprint.getHeight();
        //automatically scroll to lowest line
        if (scrollAmount > 0) viewtoprint.scrollTo(0,scrollAmount);
        else viewtoprint.scrollTo(0,0);


        //clear the vector to obtain new sensor information
        gyroDataVec.clear();
        accelDataVec.clear();
        lightDataVec.clear();
    }

    private void cycleRecordData(JMLFunctions jmlfunc, int classification){

        //Skip this function if there is no data to calculate.
        if(gyroDataVec.size() == 0) return;

        //calculate averages.
        xyzData gyroavg = avgXYZ(gyroDataVec);
        xyzData accelavg = avgXYZ(accelDataVec);
        Float lightavg = avgFloat(lightDataVec);

        //This is where we will create an instance and add it to the dataset.
        List<Double> tmplist = new ArrayList<Double>();
        tmplist.add((double)gyroavg.getX()); tmplist.add((double)gyroavg.getY()); tmplist.add((double)gyroavg.getZ());
        tmplist.add((double)accelavg.getX()); tmplist.add((double)accelavg.getY()); tmplist.add((double)accelavg.getZ());
        tmplist.add((double)lightavg);
        Vec vectoadd = new DenseVector(tmplist);
        datatolog.addDataPoint(vectoadd, classification);

        //clear the vector to obtain new sensor information
        gyroDataVec.clear();
        accelDataVec.clear();
        lightDataVec.clear();
    }


    //@Override
    protected void startService(final boolean log, final int classification) {

        filename = Integer.toString(classification);

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
                } else if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                    xyzData tempdata = new xyzData();
                    tempdata.set(event.values[0], event.values[1], event.values[2]);
                    accelDataVec.add(tempdata);
                }else if (sensor.getType() == Sensor.TYPE_LIGHT) {
                    lightDataVec.add(event.values[0]);
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
        senLight = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        sensorManager.registerListener(sensorListener, senGyroscope, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(sensorListener, senProximity, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(sensorListener, senLight, SensorManager.SENSOR_DELAY_NORMAL);

        //This will run the cyclePrintData once the set delay is up.
        if(!log) { //We aren't logging data
            h.postDelayed(new Runnable() {
                public void run() {
                    cyclePrintData(textview, jmlfuncs);
                    h.postDelayed(this, delay);
                }
            }, delay);
        }
        else{//we're recording the data
            h.postDelayed(new Runnable() {
                public void run() {
                    cycleRecordData(jmlfuncs, classification);
                    h.postDelayed(this, delay);
                }
            }, delay);
        }
    }

    protected void stopService(){
        h.removeCallbacksAndMessages(null);
        sensorManager.unregisterListener(sensorListener);

        String OUTPUT_FILE = Environment.getExternalStorageDirectory() + "/SensorExperiment/TrainData/" + filename;

        FileOutputStream fout = null;
        try {
            fout = new FileOutputStream(OUTPUT_FILE);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        if(log){
            ARFFLoader.writeArffFile(datatolog,fout);
        }

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
