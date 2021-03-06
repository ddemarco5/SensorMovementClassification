package jsat.io;

import java.io.*;
import java.util.*;
import jsat.classifiers.CategoricalData;
import jsat.classifiers.ClassificationDataSet;
import jsat.linear.SparseVector;
import static java.lang.Integer.*;
import static java.lang.Double.*;
import jsat.DataSet;
import jsat.datatransform.DenseSparceTransform;
import jsat.linear.IndexValue;
import jsat.linear.Vec;
import jsat.regression.RegressionDataSet;
import jsat.utils.DoubleList;

/**
 * Loads a LIBSVM data file into a {@link DataSet}. LIVSM files do not indicate 
 * whether or not the target variable is supposed to be numerical or 
 * categorical, so two different loading methods are provided. For a LIBSVM file
 * to be loaded correctly, it must match the LIBSVM spec without extensions. 
 * <br><br>
 * Each line should begin with a numeric value. This is either a regression 
 * target or a class label. <br>
 * Then, for each non zero value in the data set, a space should precede an 
 * integer value index starting from 1 followed by a colon ":" followed by a 
 * numeric feature value. <br> The single space at the beginning should be the 
 * only space. There should be no double spaces in the file. 
 * <br><br>
 * LIBSVM files do not explicitly specify the length of data vectors. This can 
 * be problematic if loading a testing and training data set, if the data sets 
 * do not include the same highest index as a non-zero value, the data sets will
 * have incompatible vector lengths. To resolve this issue, use the loading 
 * methods that include the optional {@code vectorLength} parameter to specify 
 * the length before hand. 
 * 
 * @author Edward Raff
 */
public class LIBSVMLoader
{

    private LIBSVMLoader()
    {
    }
    
    /*
     * LIBSVM format is sparse
     * <VAL> <1 based Index>:<Value>
     * 
     */
    
    /**
     * Loads a new regression data set from a LIBSVM file, assuming the label is
     * a numeric target value to predict
     * 
     * @param file the file to load
     * @return a regression data set
     * @throws FileNotFoundException if the file was not found
     * @throws IOException if an error occurred reading the input stream
     */
    public static RegressionDataSet loadR(File file) throws FileNotFoundException, IOException
    {
        return loadR(file, 0.5);
    }
    
    /**
     * Loads a new regression data set from a LIBSVM file, assuming the label is
     * a numeric target value to predict
     * 
     * @param file the file to load
     * @param sparseRatio the fraction of non zero values to qualify a data 
     * point as sparse
     * @return a regression data set
     * @throws FileNotFoundException if the file was not found
     * @throws IOException if an error occurred reading the input stream
     */
    public static RegressionDataSet loadR(File file, double sparseRatio) throws FileNotFoundException, IOException
    {
        return loadR(file, sparseRatio, -1);
    }
    
    /**
     * Loads a new regression data set from a LIBSVM file, assuming the label is
     * a numeric target value to predict
     * 
     * @param file the file to load
     * @param sparseRatio the fraction of non zero values to qualify a data 
     * point as sparse
     * @param vectorLength the pre-determined length of each vector. If given a 
     * negative value, the largest non-zero index observed in the data will be 
     * used as the length. 
     * @return a regression data set
     * @throws FileNotFoundException if the file was not found
     * @throws IOException if an error occurred reading the input stream
     */
    public static RegressionDataSet loadR(File file, double sparseRatio, int vectorLength) throws FileNotFoundException, IOException
    {
        return loadR(new FileReader(file), sparseRatio, vectorLength);
    }
    
    /**
     * Loads a new regression data set from a LIBSVM file, assuming the label is
     * a numeric target value to predict
     * 
     * @param isr the input stream for the file to load
     * @param sparseRatio the fraction of non zero values to qualify a data 
     * point as sparse
     * @return a regression data set
     * @throws IOException if an error occurred reading the input stream
     */
    public static RegressionDataSet loadR(InputStreamReader isr, double sparseRatio) throws IOException
    {
        return loadR(isr, sparseRatio, -1);
    }
    
    /**
     * Loads a new regression data set from a LIBSVM file, assuming the label is
     * a numeric target value to predict.
     * 
     * @param isr the input stream for the file to load
     * @param sparseRatio the fraction of non zero values to qualify a data 
     * point as sparse
     * @param vectorLength the pre-determined length of each vector. If given a 
     * negative value, the largest non-zero index observed in the data will be 
     * used as the length. 
     * @return a regression data set
     * @throws IOException 
     */
    public static RegressionDataSet loadR(InputStreamReader isr, double sparseRatio, int vectorLength) throws IOException
    {
        BufferedReader br = new BufferedReader(isr);
        List<SparseVector> sparseVecs = new ArrayList<SparseVector>();
        List<Double> targets = new DoubleList();
        int maxLen=1;
        
        
        String line;
        
        while((line = br.readLine()) != null)
        {
            int firstSpace = line.indexOf(' ');
            targets.add(parseDouble(line.substring(0, firstSpace)));
            
            maxLen = loadSparseVec(line, maxLen, sparseVecs, firstSpace+1);
        }
        
        if(vectorLength > 0)
            maxLen = vectorLength;
        
        RegressionDataSet rds = new RegressionDataSet(maxLen, new CategoricalData[0]);
        for(int i = 0; i < sparseVecs.size(); i++)
        {
            SparseVector sv = sparseVecs.get(i);
            sv.setLength(maxLen);
            rds.addDataPoint(sv, new int[0], targets.get(i));
        }
        
        rds.applyTransform(new DenseSparceTransform(sparseRatio));
        
        return rds;
    }
    
    /**
     * Loads a new classification data set from a LIBSVM file, assuming the 
     * label is a nominal target value
     * 
     * @param file the file to load
     * @return a classification data set
     * @throws FileNotFoundException if the file was not found
     * @throws IOException if an error occurred reading the input stream
     */
    public static ClassificationDataSet loadC(File file) throws FileNotFoundException, IOException
    {
        return loadC(new FileReader(file), 0.5);
    }
    
    /**
     * Loads a new classification data set from a LIBSVM file, assuming the 
     * label is a nominal target value
     * 
     * @param file the file to load
     * @param sparseRatio the fraction of non zero values to qualify a data 
     * point as sparse
     * @return a classification data set
     * @throws FileNotFoundException if the file was not found
     * @throws IOException if an error occurred reading the input stream
     */
    public static ClassificationDataSet loadC(File file, double sparseRatio) throws FileNotFoundException, IOException
    {
        return loadC(file, sparseRatio, -1);
    }
    
    /**
     * Loads a new classification data set from a LIBSVM file, assuming the 
     * label is a nominal target value
     * 
     * @param file the file to load
     * @param sparseRatio the fraction of non zero values to qualify a data 
     * point as sparse
     * @param vectorLength the pre-determined length of each vector. If given a 
     * negative value, the largest non-zero index observed in the data will be 
     * used as the length. 
     * @return a classification data set
     * @throws FileNotFoundException if the file was not found
     * @throws IOException if an error occurred reading the input stream
     */
    public static ClassificationDataSet loadC(File file, double sparseRatio, int vectorLength) throws FileNotFoundException, IOException
    {
        return loadC(new FileReader(file), sparseRatio, vectorLength);
    }
    
    /**
     * Loads a new classification data set from a LIBSVM file, assuming the 
     * label is a nominal target value 
     * 
     * @param isr the input stream for the file to load
     * @param sparseRatio the fraction of non zero values to qualify a data 
     * point as sparse
     * @return a classification data set
     * @throws IOException if an error occurred reading the input stream
     */
    public static ClassificationDataSet loadC(InputStreamReader isr, double sparseRatio) throws IOException
    {
        return loadC(isr, sparseRatio, -1);
    }
    
    /**
     * Loads a new classification data set from a LIBSVM file, assuming the 
     * label is a nominal target value 
     * 
     * @param isr the input stream for the file to load
     * @param sparseRatio the fraction of non zero values to qualify a data 
     * point as sparse
     * @param vectorLength the pre-determined length of each vector. If given a 
     * negative value, the largest non-zero index observed in the data will be 
     * used as the length. 
     * @return a classification data set
     * @throws IOException if an error occurred reading the input stream
     */
    public static ClassificationDataSet loadC(InputStreamReader isr, double sparseRatio, int vectorLength) throws IOException
    {
        BufferedReader br = new BufferedReader(isr);
        List<SparseVector> sparceVecs = new ArrayList<SparseVector>();
        List<Double> cats = new ArrayList<Double>();
        Map<Double, Integer> possibleCats = new HashMap<Double, Integer>();
        int maxLen=1;
        
        
        String line;
        
        while((line = br.readLine()) != null)
        {
            int firstSpace = line.indexOf(' ');
            double cat = Double.parseDouble(line.substring(0, firstSpace));
            if(!possibleCats.containsKey(cat))
                possibleCats.put(cat, possibleCats.size());
            cats.add(cat);
            
            maxLen = loadSparseVec(line, maxLen, sparceVecs, firstSpace+1);
        }
        
        CategoricalData predicting = new CategoricalData(possibleCats.size());
        
        if(vectorLength > 0)
            maxLen = vectorLength;
        
        //Give categories a unique ordering to avoid loading issues based on the order categories are presented
        List<Double> allCatKeys = new DoubleList(possibleCats.keySet());
        Collections.sort(allCatKeys);
        for(int i = 0; i < allCatKeys.size(); i++)
            possibleCats.put(allCatKeys.get(i), i);
        
        ClassificationDataSet cds = new ClassificationDataSet(maxLen, new CategoricalData[0], predicting);
        for(int i = 0; i < cats.size(); i++)
        {
            SparseVector vec = sparceVecs.get(i);
            vec.setLength(maxLen);
            cds.addDataPoint(vec, new int[0], possibleCats.get(cats.get(i)));
        }
        
        cds.applyTransform(new DenseSparceTransform(sparseRatio));
        
        return cds;
    }
    
    /**
     * Writes out the given classification data set as a LIBSVM data file
     * @param data the data set to write to a file
     * @param os the output stream to write to. The stream will not be closed or
     * flushed by this method
     */
    public static void write(ClassificationDataSet data, OutputStream os)
    {
        PrintWriter writer = new PrintWriter(os);
        for(int i = 0; i < data.getSampleSize(); i++)
        {
            int pred = data.getDataPointCategory(i);
            Vec vals = data.getDataPoint(i).getNumericalValues();
            writer.write(pred + " ");
            for(IndexValue iv : vals)
                writer.write((iv.getIndex()+1) + ":" + iv.getValue() + " ");//+1 b/c 1 based indexing
            writer.write("\n");
        }
    }
    
    /**
     * Writes out the given regression data set as a LIBSVM data file
     * @param data the data set to write to a file
     * @param os the output stream to write to. The stream will not be closed or
     * flushed by this method
     */
    public static void write(RegressionDataSet data, OutputStream os)
    {
        PrintWriter writer = new PrintWriter(os);
        for(int i = 0; i < data.getSampleSize(); i++)
        {
            double pred = data.getTargetValue(i);
            Vec vals = data.getDataPoint(i).getNumericalValues();
            writer.write(pred + " ");
            for(IndexValue iv : vals)
                writer.write((iv.getIndex()+1) + ":" + iv.getValue() + " ");//+1 b/c 1 based indexing
            writer.write("\n");
        }
    }
    
    /**
     * Use thread local of sparse vectors to initialize construction. This way 
     * we avoid unnecessary object allocation - one base vec will increase to 
     * the needed size. Then a copy with only the needed space is added instead
     * of the thread local vector. 
     */
    private static final ThreadLocal<SparseVector> tempSparseVecs = new ThreadLocal<SparseVector>()
    {

        @Override
        protected SparseVector initialValue()
        {
            return new SparseVector(1);
        }
        
    };
    
    private static int loadSparseVec(String line, int maxLen, List<SparseVector> sparceVecs, int pos) 
    {
        SparseVector sv = tempSparseVecs.get();
        sv.zeroOut();
        if(maxLen > sv.length())//we might be used on a new problem, so we need to reduce our length
            sv.setLength(Math.max(maxLen, 1));
        
        while(true)
        {
            int colnPos = line.indexOf(':', pos+1);
            //if -1 then there are no values for this line
            if(colnPos < 0)
                break;
            int valPos = line.indexOf(' ', colnPos+1);
            boolean breakOut = valPos < 0;//we reached the end of the line?
            if(breakOut)
                valPos = line.length();
            int index = parseInt(line.substring(pos, colnPos))-1;
            double val = parseDouble(line.substring(colnPos+1, valPos));
            maxLen = Math.max(maxLen, index+1);
            sv.setLength(maxLen);
            sv.set(index, val);
            pos = valPos+1;
            if(breakOut || pos == line.length())
                break;
        }
        sparceVecs.add(new SparseVector(sv));//copy of the sv since sv will be reused. Copy will allocate just enough to store the values present
        return maxLen;
    }
}
