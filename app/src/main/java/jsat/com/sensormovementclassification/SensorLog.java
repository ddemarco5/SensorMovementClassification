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
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
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
    xyzData prevGyro;
    Vector<xyzData> accelDataVec = new Vector<xyzData>();
    xyzData prevAccel;
    Vector<Float> lightDataVec = new Vector<>();
    Float prevLight;

    public TextView textview;
    private SensorManager sensorManager;
    private SensorEventListener sensorListener;
    private Sensor senGyroscope;
    private Sensor senAccel;
    private Sensor senProximity;
    private Sensor senLight;

    //are we logging the data? TODO: fix me up. This is sloppy.
    boolean log;
    String filename;

    //Dataset for when we log data. 7 attributes, 7 attribute informations, 1 target class(predicting)

    //Change this back when the accellerometer is added back in
    //ClassificationDataSet datatolog = new ClassificationDataSet(7, new CategoricalData[7], new CategoricalData(1));
    ClassificationDataSet datatolog;


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
        //if(gyroDataVec.size() == 0 || accelDataVec.size() == 0 || lightDataVec.size() == 0) return;

        //calculate averages.
        xyzData gyroavg = avgXYZ(gyroDataVec);
        xyzData accelavg = avgXYZ(accelDataVec);
        Float lightavg = avgFloat(lightDataVec);

        cycle++;
        //viewtoprint.append("gyro - " + "X:" + Float.toString(gyroavg.getX()) + " Y:" + Float.toString(gyroavg.getY()) + " Z:" + Float.toString(gyroavg.getZ()) + "\n");
        //TODO: this will have to change when the sensor selection becomes dynamic (not hardcoded)
        List<Double> datalist = Arrays.asList((double) gyroavg.getX(),
                new Double(gyroavg.getY()),
                new Double(gyroavg.getZ()),
                new Double(accelavg.getX()),
                new Double(accelavg.getY()),
                new Double(accelavg.getZ()),
                new Double(lightavg));
        String classification = jmlfunc.classify(datalist);
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

    private void cycleRecordData(JMLFunctions jmlfunc, String classification){

        xyzData gyroavg;
        xyzData accelavg;
        Float lightavg;

        //If there is no data from a sensor, use it's previous value.
        if(gyroDataVec.size() == 0){
            if(prevGyro != null){
                textview.setText("No info from gyro, using previous value.\n");
                gyroavg = prevGyro;
            }
            else {
                textview.setText("No info from gyro, waiting.\n");
                return;
            }
        }
        else{
            gyroavg = avgXYZ(gyroDataVec);
        }

        if(accelDataVec.size() == 0){
            if(prevAccel != null){
                textview.setText("No info from accel, using previous value.\n");
                accelavg = prevAccel;
            }
            else {
                textview.setText("No info from accel, waiting.\n");
                return;
            }
        }
        else{
            accelavg = avgXYZ(accelDataVec);
        }

        if(lightDataVec.size() == 0){
            if(prevLight != 0){
                textview.setText("No info from light, using previous value.\n");
                lightavg = prevLight;
            }
            else {
                textview.setText("No info from light, waiting.\n");
                return;
            }
        }
        else{
            lightavg = avgFloat(lightDataVec);
        }

        //calculate averages.

        prevGyro = gyroavg;
        prevAccel = accelavg;
        prevLight = lightavg;

        //This is where we will create an instance and add it to the dataset.
        List<Double> tmplist = new ArrayList<Double>();
        tmplist.add(new Double(gyroavg.getX()));
        tmplist.add(new Double(gyroavg.getY()));
        tmplist.add(new Double(gyroavg.getZ()));
        tmplist.add(new Double(accelavg.getX()));
        tmplist.add(new Double(accelavg.getY()));
        tmplist.add(new Double(accelavg.getZ()));
        tmplist.add(new Double(lightavg));
        Vec vectoadd = new DenseVector(tmplist);
        //check to make sure we have enough attributes.
        if(vectoadd.length() == 7) //TODO:bad form, hardcoded var. Change me.
            datatolog.addDataPoint(vectoadd, 0);

        textview.append("Data point logged.\n");

        //clear the vector to obtain new sensor information
        gyroDataVec.clear();
        accelDataVec.clear();
        lightDataVec.clear();
    }


    public void startListenerService(){
        startService(false, "dummy");
    }

    public void startRecordingService(String classification){
        startService(true, classification);
    }

    public void unregisterListeners(){
        sensorManager.unregisterListener(sensorListener);
    }

    public void registerListeners(){
        sensorManager.registerListener(sensorListener, senGyroscope, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(sensorListener, senAccel, SensorManager.SENSOR_DELAY_NORMAL);
        //sensorManager.registerListener(sensorListener, senProximity, SensorManager.SENSOR_DELAY_NORMAL);
        sensorManager.registerListener(sensorListener, senLight, SensorManager.SENSOR_DELAY_NORMAL);
    }

    private void startService(final boolean log, final String classification) {


        //Set the global log variable.
        this.log = log;

        CategoricalData decidingdata = new CategoricalData(1);
        decidingdata.setCategoryName("Action");
        decidingdata.setOptionName(classification, 0);


        datatolog = new ClassificationDataSet(7, new CategoricalData[0], decidingdata);
        datatolog.setNumericName("gyroX", 0);
        datatolog.setNumericName("gyroY", 1);
        datatolog.setNumericName("gyroZ", 2);
        datatolog.setNumericName("accelX", 3);
        datatolog.setNumericName("accelY", 4);
        datatolog.setNumericName("accelZ", 5);
        datatolog.setNumericName("light", 6);
        //datatolog.setNumericName("light", 3);


        filename = classification;

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
        senAccel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        //senProximity = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY);
        senLight = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        registerListeners();

        //This will run the cyclePrintData once the set delay is up.
        if(!log) { //We aren't logging data
            textview.append("Running print.\n");
            h.postDelayed(new Runnable() {
                public void run() {
                    cyclePrintData(textview, jmlfuncs);
                    h.postDelayed(this, delay);
                }
            }, delay);
        }
        else{//we're recording the data
            textview.append("Running record.\n");
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


        if(log == true){
            String OUTPUT_FILE = Environment.getExternalStorageDirectory() + "/SensorExperiment/TrainData/" + filename + ".arff";

            FileOutputStream fout = null;
            try {
                fout = new FileOutputStream(OUTPUT_FILE);
            } catch (Exception e) {
                System.err.print(e.toString());
            }
            ARFFLoader.writeArffFile(datatolog,fout);
            try {
                fout.flush();
                fout.close();
            } catch (IOException e) {
                textview.setText("Error writing file.\n");
                return;
            }
            textview.setText("File written with " + datatolog.toString() + ".\n");
        }

    }


/*
    @Override
    protected void onPause() {
        super.onPause();
        sensensorManager.unregisterListener(this);
    }
    @Override
    protected void onResume() {
        super.onResume();
        sensensorManager.registerListener(this, senGyroscope, SensorManager.SENSOR_DELAY_NORMAL);
    }
*/
}
