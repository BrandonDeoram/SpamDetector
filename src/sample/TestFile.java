package sample;

import java.io.*;
import java.util.*;
import java.text.DecimalFormat;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import static javafx.application.Application.launch;

public class TestFile {
    private String filename;
    private double spamProbability;
    private String actualClass;
    private String prob;

    public TestFile(String filename, double spamProbability, String actualClass,String prob) {
        this.filename = filename;
        this.spamProbability = spamProbability;
        this.actualClass = actualClass;
        this.prob = prob;
    }



    public String getFilename() {
        return this.filename;
    }

    public double getSpamProbability() {
        return this.spamProbability;
    }

    public String getSpamProbRounded() {
        DecimalFormat df = new DecimalFormat("0.00000");
        return df.format(this.spamProbability);
    }

    public String getActualClass() {
        return this.actualClass;
    }

    public void setFilename(String value) {
        this.filename = value;
    }

    public void setSpamProbability(double val) {
        this.spamProbability = val;
    }
    public void setProb(String val) {
        this.prob = val;
    }
    public String getProb() {
        return prob;
    }


    public void setActualClass(String value) {
        this.actualClass = value;
    }

    // train dataset
    Map<String, Double> pSW;                      // the probability that a file is spam, given that it contains the word W​_i​ (from prev)
    Map<String, Double> pSF;                      // n will be used to compute a probability that the file F is spam S (pS|F)
    Map<String, String> pSF2;                     // same as above but String value
    List<String> temp;                            // temp list to store repated (ignored cased) words from the same file
    public static double hamSize = 0.0;           // amount of files in train/ham directory
    public static double spamSize = 0.0;          // amount of files in train/spam directory
    public double n = 0.0;                        // used to calculate greek n (eta)
    public static double numTruePos = 0.0;        // # of files from test data that are correctly marked as spam
    public static double numTrueNeg = 0.0;        // # of files from test data that are correctly marked as ham (not spam)
    public static double numFalsePos = 0.0;       // # of files from test data that are incorrectly marked as spam
    public static double accuracy = 0.0;
    public static double precision = 0.0;
    public static ObservableList<TestFile> marks=FXCollections.observableArrayList();;

    public TestFile() {
        pSW = new TreeMap<>();
        pSF = new TreeMap<>();
        pSF2 = new TreeMap<>();
        temp = new ArrayList<>();
//        marks = FXCollections.observableArrayList();
    }

    public static ObservableList<TestFile> getAllMarks() {
        return marks;
    }
    public void setAccuracy(double a) {
        accuracy = a;
    }
    public double getAccuracy() {
        return accuracy;
    }
    public void setPrec(double a){
        precision = a;
    }
    public double getPrec(){
        return precision;
    }



    public void parseFile(File file) throws IOException {
        // Captures amount of files in said directory (ham or spam)
        String path = file.getParentFile().getName();
        if (path.equals("ham")) {
            hamSize = file.getParentFile().listFiles().length;
            setFilename(path);
            setActualClass(path);
        }
        if (path.equals("spam")) {
            setFilename(path);
            setActualClass(path);
            spamSize = file.getParentFile().listFiles().length;
        }

        if (file.isDirectory()) {
            //parse each file inside the directory
            File[] content = file.listFiles();
            for (File current : content) {
                parseFile(current);
            }
        } else {
            Scanner scanner = new Scanner(file);
            // scanning token by token
            while (scanner.hasNext()) {
                String token = scanner.next().toLowerCase();
                // if word in file doesn't already exist then compute eta (n)
                if (isValidWord(token) && !temp.contains(token)) {
                    // tries to add word and if pSW return null will skip
                    try {
                        n += (Math.log(1 - pSW.get(token))) - (Math.log(pSW.get(token)));
                        temp.add(token);
                    } catch (Exception e) {
                        // move on to next word (key) if test/ham or spam doesn't contain word from pSW
                    }
                }
            }
            temp.clear();       // clears out words from temp list for the next file (doesn't overlap)

            double sf = 1 / (1 + Math.pow(Math.E, n));
            setSpamProbability(sf);

            // below used for accuracy and precision
            if (sf > 0.5 && path.equals("ham")) {
                numTrueNeg += 1.0;
            }
            if (sf > 0.5 && path.equals("spam")) {
                numTruePos += 1.0;
            }
            if (sf < 0.5 && path.equals("spam")) {
                numFalsePos += 1.0;
            }

            System.out.println("In directory: " + file.getParent() + ". Adding prSF to " + file.getName());
            pSF2.put(file.getName(), getSpamProbRounded());
            pSF.put(file.getName(), getSpamProbability());
            setProb(getSpamProbRounded());
            marks.add(new TestFile(file.getName(), getSpamProbability(), getActualClass(),getProb()));
            n = 0;                                                                       // deletes old eta for next file
        }
    }

    public Map<String, Double> HashMapFromTextFile(File filePath) {

        Map<String, Double> map = new HashMap<String, Double>();
        BufferedReader br = null;

        try {
            // Used Geeks for Geeks for guidance
            File file = new File(filePath.getPath());
            br = new BufferedReader(new FileReader(file));
            String line = null;

            // read file line by line
            while ((line = br.readLine()) != null) {

                // split the line by :
                String[] parts = line.split(":");

                // first part is name, second is number
                String name = parts[0].trim();
                String number = parts[1].trim();

                // put name, number in HashMap if they are
                // not empty
                if (!name.equals("") && !number.equals(""))
                    map.put(name, Double.parseDouble(number));

            }
            setTotProb(map);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {

            // Always close the BufferedReader
            if (br != null) {
                try {
                    br.close();
                } catch (Exception e) {
                }
                ;
            }
        }
        return map;
    }

    private void setTotProb(Map<String, Double> map) {
        pSW = map;
    }

    public void outputWordCount() throws IOException {
        File totProbF = new File("PrSW_iFORCOMPARISION1.txt");
        totProbF.createNewFile();
        if (totProbF.canWrite()) {
            PrintWriter fileOutput = new PrintWriter(totProbF);
            Set<String> keys = pSW.keySet();
            Iterator<String> keyIterator = keys.iterator();
            pSW.entrySet().forEach(entry -> {
                fileOutput.println(entry.getKey() + ":" + entry.getValue());
            });
            fileOutput.close();
        }
        File sfOUT = new File("PrSFdouble1.txt");
        sfOUT.createNewFile();
        if (sfOUT.canWrite()) {
            PrintWriter fileOutput = new PrintWriter(sfOUT);
            Set<String> keys = pSF.keySet();
            Iterator<String> keyIterator = keys.iterator();
            pSF.entrySet().forEach(entry -> {
                fileOutput.println(entry.getKey() + ":" + entry.getValue());
            });
            fileOutput.close();
        }
        File sfOUT2 = new File("PrSFstring1.txt");
        sfOUT2.createNewFile();
        if (sfOUT.canWrite()) {
            PrintWriter fileOutput = new PrintWriter(sfOUT2);
            Set<String> keys = pSF2.keySet();
            Iterator<String> keyIterator = keys.iterator();
            pSF2.entrySet().forEach(entry -> {
                fileOutput.println(entry.getKey() + ":" + entry.getValue());
            });
            fileOutput.close();
        }
    }

    private boolean isValidWord(String word) {
        String allLetters = "^[a-zA-Z]+$";
        // returns true if the word is composed by only letters otherwise returns false;
        return word.matches(allLetters);

    }



    public static void main(String[] args) {

        // if(args.length < 3){
        // 	System.err.println("Usage: java WordCounter <inputDir> <inputDir> <inputDir>");
        // 	System.exit(0);
        // }

        // training dataset
        File dataDir1 = new File(args[0]);
        // test/ham
        File dataDir2 = new File(args[1]);
        // test/spam
        File dataDir3 = new File(args[2]);

        TestFile wordCounter = new TestFile();

        try {
            wordCounter.HashMapFromTextFile(dataDir1);
            wordCounter.parseFile(dataDir2);
            wordCounter.parseFile(dataDir3);
            wordCounter.outputWordCount();

        } catch (FileNotFoundException e) {
            System.err.println("Invalid input dir: " + dataDir1.getAbsolutePath() + " or " + dataDir2.getAbsolutePath() + " or " + dataDir3.getAbsolutePath());
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        double accuaracy = (numTruePos + numTrueNeg) / (hamSize + spamSize);
        double precision = numTruePos / (numTruePos + numFalsePos);
        wordCounter.setAccuracy(accuaracy);
        wordCounter.setPrec(precision);
        System.out.println("Accuarcy = " + numTruePos + "+" + numTrueNeg + "/" + hamSize + "+" + spamSize + "=" + accuaracy);
        System.out.println("Precision = " + numTruePos + "/" + numFalsePos + "+" + numTrueNeg + "=" + precision);

    }


}
