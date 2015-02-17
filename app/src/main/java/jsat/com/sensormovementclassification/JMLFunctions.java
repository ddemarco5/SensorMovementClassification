package jsat.com.sensormovementclassification;

import android.os.Environment;
import android.widget.TextView;

import java.io.File;


import jsat.ARFFLoader;
import jsat.DataSet;
import jsat.classifiers.CategoricalResults;
import jsat.classifiers.ClassificationDataSet;
import jsat.classifiers.DataPoint;
import jsat.classifiers.trees.RandomForest;
import jsat.linear.DenseVector;


/**
 * Created by Dominic on 2/1/2015.
 */
public class JMLFunctions {
    TextView textview;
    DataSet traindata;
    File datafile;
    RandomForest test;


    File DATA_PATH = Environment.getExternalStorageDirectory();
    String TRAIN_DATA_PATH = "/SensorExperiment/TrainData";
    String TRAIN_FILE_NAME = "/train2.arff";

    public JMLFunctions(TextView textview){
        this.textview = textview;
        readTrainingData();
    }

    /** Takes xyz data and returns the classification **/
    public String classifyXYZ(double x, double y, double z){
        DataPoint pointtoclassify = new DataPoint(DenseVector.toDenseVec(x,y,z));
        CategoricalResults results = test.classify(pointtoclassify);
        return results.toString();
    }

    private void readTrainingData(){
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


        //subject to change.
        test = new RandomForest(10);


        ClassificationDataSet classdata = new ClassificationDataSet(traindata, 0);


        textview.append("Creating classifier...\n");
        try {
            test.trainC(classdata);
        } catch (Exception e) {
            textview.append("Error creating classifier!\n" + e.toString() + "\n");
            return;
        }
        textview.append("Classifier created...\n");

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
