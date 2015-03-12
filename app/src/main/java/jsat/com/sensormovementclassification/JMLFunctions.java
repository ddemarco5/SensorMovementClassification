package jsat.com.sensormovementclassification;

import android.os.Environment;
import android.provider.ContactsContract;
import android.util.Log;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.List;


import jsat.ARFFLoader;
import jsat.DataSet;
import jsat.classifiers.CategoricalData;
import jsat.classifiers.CategoricalResults;
import jsat.classifiers.ClassificationDataSet;
import jsat.classifiers.DataPoint;
import jsat.classifiers.trees.RandomForest;
import jsat.linear.DenseVector;
import jsat.linear.Vec;


/**
 * Created by Dominic on 2/1/2015.
 */
public class JMLFunctions {
    TextView textview;
    DataSet traindata;
    File datafile;
    RandomForest test;

    ClassificationDataSet mergeddataset;



    File DATA_PATH = Environment.getExternalStorageDirectory();
    String TRAIN_DATA_PATH = "/SensorExperiment/TrainData";
    String TRAIN_FILE_NAME = "/train2.arff";


    public JMLFunctions(TextView textview){
        this.textview = textview;
        //readTrainingData();
    }

    /** Takes xyz data and returns the classification **/
    //public String classify(double x1, double y1, double z1, double x2, double y2, double z2, double a){
    public String classify(List<Double> values){
        DataPoint pointtoclassify = new DataPoint(new DenseVector(values));
        //check for a NaN value
        if(values.contains(Double.NaN)){
            Log.w("Problem Child: ", pointtoclassify.toString());
            return "NaN found...";
        }
        CategoricalResults results = test.classify(pointtoclassify);
        return results.toString();
    }

    public void readTrainingData(){

        textview.append("Attempting to load all training data files.\n");
        List<DataSet> datasetslist = new ArrayList<DataSet>();


        File f = new File(DATA_PATH + TRAIN_DATA_PATH);

        try {
            for(File file : f.listFiles()){
                datasetslist.add(ARFFLoader.loadArffFile(file));
            }
        } catch (Exception e) {
            textview.append("Error loading datasets!\n" + e.toString() + "\n");
            return;
        }
        textview.append("Data sets loaded, now attempting merge.\n");


        int num_classifications = datasetslist.size();
        int num_attributes = datasetslist.get(0).getNumNumericalVars();
        mergeddataset = new ClassificationDataSet(num_attributes, new CategoricalData[0], new CategoricalData(num_classifications));
        int classification=0;
        for(DataSet d : datasetslist){
            for(int i=0; i<d.getSampleSize(); i++){
                mergeddataset.addDataPoint(d.getDataPoint(i).getNumericalValues(), classification);
                //textview.append("DEBUG: " + d.getDataPoint(i).getNumericalValues() + "\n");
            }
            classification++;
        }
        //ClassificationDataSet mergeddataset = new ClassificationDataSet(attribs, new CategoricalData[0], decidingdata);

        textview.append("Successfully created merged dataset!...\n");
        textview.append("Dataset contains " + mergeddataset.getSampleSize() + " instances!\n");
        textview.append("Dataset contains " + mergeddataset.getNumCategoricalVars() + " categories!\n");
        for(int i = 0; i <  mergeddataset.getNumCategoricalVars(); i++)
            textview.append("\t" + mergeddataset.getCategoryName(i) + "\n");
        //textview.append("DEBUG " + mergeddataset.getNumCategoricalVars() + "\n");


        /*
        textview.append("Checking for base files...\n");
        //trainfile = new File(DATA_PATH + TRAIN_DATA_PATH, "train.csv");
        //textview.append(trainfile.toString() + "\n");

        textview.append("Loading file...\n");
        try {
            //source = new DataSource(DATA_PATH + TRAIN_DATA_PATH + "/train.csv");
            datafile = new File(DATA_PATH + TRAIN_DATA_PATH + TRAIN_FILE_NAME);
            traindata = ARFFLoader.loadArffFile(datafile);
        } catch (Exception e) {
            textview.append("Error loading dataset!\n" + e.toString() + "\n");
            return;
        }
        if(traindata.getSampleSize() > 0){
            //Some nice debug info
            textview.append("Dataset contains " + traindata.getSampleSize() + " instances!\n");
            textview.append("Dataset contains " + traindata.getNumCategoricalVars() + " categories!\n");
            for(int i = 0; i <  traindata.getNumCategoricalVars(); i++)
                textview.append("\t" + traindata.getCategoryName(i) + "\n");

        }
        */

        //subject to change.
        test = new RandomForest(num_attributes);


        //ClassificationDataSet testthing = new ClassificationDataSet(datasetslist.get(0), 0);


        textview.append("Creating classifier...\n");
        try {
            test.trainC(mergeddataset);
        } catch (Exception e) {
            textview.append("Error creating classifier!\n" + e.toString() + "\n");
            return;
        }
        textview.append("Classifier created...\n");

        textview.append("Testing classifier...\n");
        //this.classify(100.0, 122.0, 99.0, 100.0);

    }

    /* Checks if external storage is available for read and write */
    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }

}
