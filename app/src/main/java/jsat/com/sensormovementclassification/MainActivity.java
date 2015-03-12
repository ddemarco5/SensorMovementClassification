package jsat.com.sensormovementclassification;

import android.content.Intent;
import android.os.PowerManager;
import android.os.CountDownTimer;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ToggleButton;
import android.widget.TextView;

import org.com.sensormovementclassification.R;


public class MainActivity extends ActionBarActivity {

    //declare our sensorlog class as global
    SensorLog sensorLog;
    //We have to declare our machine learning class
    JMLFunctions jml;

    TextView logtext;

    //Get the powermanager for the partial wake lock. Needed to collect data when the screen is off
    //TODO:Find a better alternative to this?
    PowerManager pm;
    PowerManager.WakeLock wl;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        //sensorLog = new SensorLog(logtext, this, jml);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        logtext = (TextView)findViewById(R.id.textView);
        logtext.setMovementMethod(new ScrollingMovementMethod());


        jml = new JMLFunctions((TextView)findViewById(R.id.textView));
        jml.readTrainingData();
        pm = (PowerManager) getSystemService(this.POWER_SERVICE);
        wl = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "Log Wakelock");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
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

    public void onToggleClicked(View view){
        // Is the toggle on?
        boolean on = ((ToggleButton) view).isChecked();
        //TextView logtext = (TextView)findViewById(R.id.textView);
        //logtext.setMovementMethod(new ScrollingMovementMethod());



        if (on) {
            //get our wakelock
            wl.acquire();
            //create our sensorlog activity
            sensorLog = new SensorLog(logtext, this, jml);

            sensorLog.startListenerService();
            logtext.append("Started.\n");

        } else {
            //release our wakelock
            wl.release();
            //logtext.append("Stopped.\n");
            sensorLog.stopService();
            //logtext.clearComposingText();
            logtext.append("Stopped.\n");
        }
    }

    public void launchManager(View view){
        Intent myIntent = new Intent(MainActivity.this, ManageTrainingData.class);
        MainActivity.this.startActivity(myIntent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if(sensorLog != null) {
            sensorLog.unregisterListeners();
            sensorLog.registerListeners();
        }
    }
    @Override
    protected void onResume() {
        super.onResume();
        if(sensorLog != null) {
            sensorLog.unregisterListeners();
            sensorLog.registerListeners();
        }
    }

}
