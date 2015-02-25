package jsat.com.sensormovementclassification;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import org.com.sensormovementclassification.R;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

public class ManageTrainingData extends ActionBarActivity {

    final Context context = this;

    String path = Environment.getExternalStorageDirectory() + "/SensorExperiment/TrainData";

    ArrayList<String> trainDataList=new ArrayList<String>();
    ArrayAdapter<String> arrayAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_manage_training_data);

        //trainDataList = new ArrayList<String>();
        arrayAdapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, trainDataList);
        refreshTrainData();

        final ListView dataList = (ListView) findViewById(R.id.listView);

        dataList.setAdapter(arrayAdapter);
        dataList.setChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        dataList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Toast.makeText(getApplicationContext(), "Something clicked", Toast.LENGTH_LONG).show();

                AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(context);
                alertDialogBuilder.setTitle("Delete Data Set?");
                alertDialogBuilder
                        .setMessage("Deletion is irreversible.")
                        .setCancelable(false)
                        .setPositiveButton("Delete",new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                // if this button is clicked, close
                                // current activity
                                //ManageTrainingData.this.finish();
                                deleteTrainData(dataList.getAdapter().getItem(dataList.getCheckedItemPosition()).toString());
                            }
                        })
                        .setNegativeButton("Cancel",new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,int id) {
                                // if this button is clicked, just close
                                // the dialog box and do nothing
                                dialog.cancel();
                            }
                        });

                AlertDialog alertDialog = alertDialogBuilder.create();

                alertDialog.show();

            }
        });



    }

    private void deleteTrainData(String name){
        File f = new File(path + "/" + name);
        f.delete();
        //Toast.makeText(getApplicationContext(), "Deleted " + name, Toast.LENGTH_LONG).show();
        refreshTrainData();
    }

    private void refreshTrainData(){
        trainDataList.clear();
        File f = new File(path);
        for(File file : f.listFiles()){
            trainDataList.add(file.getName());
        }
        arrayAdapter.notifyDataSetChanged();
    }



    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_manage_training_data, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_new) {
               Intent myIntent = new Intent(ManageTrainingData.this, SensorRecord.class);
               ManageTrainingData.this.startActivity(myIntent);

            /*File file = new File(path + "/delete.me");
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
            refreshTrainData();
            return true;*/
        }

        return super.onOptionsItemSelected(item);
    }
}
