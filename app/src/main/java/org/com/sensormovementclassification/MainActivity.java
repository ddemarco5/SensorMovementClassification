package org.com.sensormovementclassification;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ToggleButton;
import android.widget.TextView;


public class MainActivity extends ActionBarActivity {

    //declare our sensorlog class as global
    SensorLog sensorLog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
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

    public void onToggleClicked(View view) {
        // Is the toggle on?
        boolean on = ((ToggleButton) view).isChecked();
        TextView logtext = (TextView)findViewById(R.id.textView);
        logtext.setMovementMethod(new ScrollingMovementMethod());

        if (on) {
            //create our sensorlog activity
            sensorLog = new SensorLog(logtext, this);
            sensorLog.startService();
            logtext.append("Started.\n");
        } else {
            //logtext.append("Stopped.\n");
            sensorLog.stopService();
            //logtext.clearComposingText();
            logtext.append("Stopped.\n");
        }
    }

}
