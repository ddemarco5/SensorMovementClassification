package org.com.sensormovementclassification;

import android.os.Environment;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;

import net.sf.javaml.classification.Classifier;
import net.sf.javaml.classification.KNearestNeighbors;
import net.sf.javaml.classification.NearestMeanClassifier;
import net.sf.javaml.core.Dataset;
import net.sf.javaml.core.DenseInstance;
import net.sf.javaml.tools.data.FileHandler;
import net.sf.javaml.core.Instance;

/**
 * Created by Dominic on 2/1/2015.
 */
public class JMLFunctions {
    TextView textview;
    File trainfile;
    Dataset traindata;

    int DATASIZE=1;

    Classifier classifier = new NearestMeanClassifier();

    File testfile;

    File DATA_PATH = Environment.getExternalStorageDirectory();
    String TRAIN_DATA_PATH = "/SensorExperiment/TrainData";
    String TRAIN_FILE_NAME = "train.csv";

    public JMLFunctions(TextView textview){
        this.textview = textview;
        readTrainingData();
    }

    /** Takes xyz data and returns the classification **/
    public String classifyXYZ(double x, double y, double z){
        double[] values = new double[]{x,y,z};
        Instance tempinst = new DenseInstance(values);
        return classifier.classify(tempinst).toString();

    }

    //TODO: This is just for demo, change me back.
    /** Takes xyz data and returns the classification **/
    public String classifyY(double y){
        double[] values = new double[]{y};
        Instance tempinst = new DenseInstance(values);
        return classifier.classify(tempinst).toString();

    }

    private void readTrainingData(){
        textview.append("Checking for base files...\n");
        trainfile = new File(DATA_PATH + TRAIN_DATA_PATH, "train.csv");
        textview.append(trainfile.toString() + "\n");

        if(!trainfile.exists()){
            textview.append("Training csv file does not exist! Don't continue.\n");
        }
        else textview.append("Training file found.\n");

        textview.append("Loading file...\n");
        try {
            traindata = FileHandler.loadDataset(trainfile, DATASIZE, ",");
        } catch (Exception e) {
            textview.append("Error loading dataset!\n" + e.toString() + "\n");
            return;
        }
        if(!traindata.isEmpty()){
            textview.append("Dataset loaded successfully! (Meaning it is not empty)\n");
        }

        textview.append("Creating classifier...\n");
        try {
            classifier.buildClassifier(traindata);
        } catch (Exception e){
            textview.append("Error building classifier.\n" + e.toString() + "\n");
            return;
        }
        textview.append("Classifier created (KNearestNeighbor)...\n");
        textview.append("Testing Classifier...\n");

        //This bit below is just to test the classifier. Nothing special.
        Dataset dataForClassification = null;
        try {
            dataForClassification = FileHandler.loadDataset(new File(DATA_PATH + TRAIN_DATA_PATH, "train.csv"), DATASIZE, ",");
        } catch (Exception e) {
            textview.append("Error building classifier.\n" + e.toString() + "\n");
            return;
        }
        /* Counters for correct and wrong predictions. */
        int correct = 0, wrong = 0;
        /* Classify all instances and check with the correct class values */
        for (Instance inst : dataForClassification) {
            Object predictedClassValue = classifier.classify(inst);
            Object realClassValue = inst.classValue();
            if (predictedClassValue.equals(realClassValue))
                correct++;
            else
                wrong++;
        }

        textview.append("Correct: " + correct + " Wrong: " + wrong + "\n");

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
