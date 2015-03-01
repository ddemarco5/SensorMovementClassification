package jsat.com.sensormovementclassification;

import android.os.CountDownTimer;
import android.os.SystemClock;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.ToggleButton;

import org.com.sensormovementclassification.R;

public class SensorRecord extends ActionBarActivity {

    TextView logtext;
    TextView msgtext;
    TextView inputtext;
    JMLFunctions jmlfuncs = new JMLFunctions(logtext);
    SensorLog senslog;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sensor_record);
        logtext = (TextView)findViewById(R.id.textView3);
        msgtext = (TextView)findViewById(R.id.textView2);
        inputtext = (EditText)findViewById(R.id.editText);
        senslog = new SensorLog(logtext,this,jmlfuncs);
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_sensor_record, menu);
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

    private void countdown(final TextView msgtext){
        new CountDownTimer(10000, 1000) {

            public void onTick(long millisUntilFinished) {
                msgtext.setText("Starting in: " + millisUntilFinished / 1000);
            }

            public void onFinish() {
                msgtext.setText("Recording");
                senslog.startService(true, inputtext.getText().toString());
            }
        }.start();

    }

    public void onToggleClicked(View view) {
        // Is the toggle on?
        boolean on = ((ToggleButton) view).isChecked();
        msgtext.setMovementMethod(new ScrollingMovementMethod());

        if (on) {
            //create our sensorlog activity
            //sensorLog = new SensorLog(logtext, this, jml);
            //sensorLog.startService();
            countdown(msgtext);

        } else {
            //logtext.append("Stopped.\n");
            //sensorLog.stopService();
            //logtext.clearComposingText();
            msgtext.setText("Stopped.\n");
            senslog.stopService();
        }
    }

}
