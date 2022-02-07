package sample;

import java.io.*;
import java.util.*;


public class TrainingFile{
	
	private Map<String, Double> wordCounts;     // temporary map to sort out repeated words in same file
    private Map<String, Double> trainHamFreq;   // training ham frequency map; number of files containing unique word
    private Map<String, Double> trainSpamFreq;  // training spam frequency map; number of files containing unique word
    private Map<String, Double> pSW;            // the probability that a file is spam, given that it contains the word W​_i​
    private Map<String, Double> pWS;            // the probability that the word W_i appears in a spam file
    private Map<String, Double> pWH;            // the probability that the word W​_i ​​appears in a ham file.
    private int hamSize = 0;                     // amount of files in train/ham directory
    private int spamSize = 0;                    // amount of files in train/spam directory
	
	public TrainingFile(){
		wordCounts = new TreeMap<>();
        trainHamFreq = new TreeMap<>();
		trainSpamFreq = new TreeMap<>();
        pSW = new TreeMap<>();
        pWS = new TreeMap<>();
		pWH = new TreeMap<>();
	}
	
	public void parseFile(File file) throws IOException{
        // Captures amount of files in said directory (ham or spam)
        String path = file.getParentFile().getName();
        if(path.equals("ham")){
            hamSize = file.getParentFile().listFiles().length;
        }
        if(path.equals("spam")){
            spamSize = file.getParentFile().listFiles().length;
        }

		if(file.isDirectory()){
			//parse each file inside the directory
			File[] content = file.listFiles();
			for(File current: content){
				parseFile(current);
			}
		}else{
			Scanner scanner = new Scanner(file);
			// scanning token by token
			while (scanner.hasNext()){
				String  token = scanner.next();
				if (isValidWord(token)){
                    // adds (ignored case) word from same file to temporary map; sets value to 1 where unique word is counted once per file
					wordCounts.put(token.toLowerCase(), 1.0);
				}
			}

            // if in train/ham directory, merge temporary map (amount of files containing unique word) to trainHamFreq
            if(path.equals("ham")){
                System.out.println("In directory: "+file.getParent()+ ". Adding " + file.getName() + " count to trainHamFreq");
			    wordCounts.forEach((key, value) -> trainHamFreq.merge(key, value, (v1, v2) -> v1 + v2));
            }

            // if in train/spam directory, merge temporary map (amount of files containing unique word) to trainSpamFreq
            if(path.equals("spam")){
                System.out.println("In directory: "+file.getParent()+ ". Adding " + file.getName() + " count to trainSpamFreq");
                wordCounts.forEach((key, value) -> trainSpamFreq.merge(key, value, (v1, v2) -> v1 + v2));
            }

			wordCounts.clear();			// deletes words from temporary map to not carry over to next file												 	   
		}
		calcProb(hamSize,spamSize);
	}

    // calculates the probabilty of pWS and pWH to calculate pSW
    public void calcProb(int ham, int spam){
        for (Map.Entry<String, Double> entry : trainSpamFreq.entrySet()) {
            Double prob = entry.getValue() / Double.valueOf(spam);
            pWS.put(entry.getKey(), prob);
        }
        for (Map.Entry<String, Double> entry : trainHamFreq.entrySet()) {
            Double prob = entry.getValue() / Double.valueOf(ham);
            pWH.put(entry.getKey(), prob);
        }

        // calculates probabilty and if key (word) is not found in either will be ignore and move on to next key (word)
        for (Map.Entry<String, Double> entry : trainSpamFreq.entrySet()) {
            // tries to add word and if pWS and PWH return null will skip
            try{
            Double prob = pWS.get(entry.getKey()) / ( pWS.get(entry.getKey()) + pWH.get(entry.getKey()) );
            pSW.put(entry.getKey(), prob);
            } catch(Exception e){
                
            }
       }
    }
	
	private boolean isValidWord(String word){
		String allLetters = "^[a-zA-Z]+$";
		// returns true if the word is composed by only letters otherwise returns false;
		return word.matches(allLetters);
			
	}
	
	public void outputWordCount() throws IOException{
		System.out.println("Total words HamFreq:" + trainHamFreq.size() + " | Total words HamProb:" + pWH.size()
        + " | Size of ham Directory:" + hamSize);
        System.out.println("Total words SpamFreq:" + trainSpamFreq.size() + " | Total words HamProb:" + pWS.size()
        + " | Size of spam Directory:" + spamSize);

        File hamFreq = new File("trainHamFreq.txt");
        hamFreq.createNewFile();
        if (hamFreq.canWrite()) {
            PrintWriter fileOutput = new PrintWriter(hamFreq);
            Set<String> keys = trainHamFreq.keySet();
            Iterator<String> keyIterator = keys.iterator();
            trainHamFreq.entrySet().forEach(entry -> {
                fileOutput.println(entry.getKey() + ":" + entry.getValue());
            });
            fileOutput.close();
        }

        File spamFreq = new File("trainSpamFreq.txt");
        spamFreq.createNewFile();
        if (spamFreq.canWrite()) {
            PrintWriter fileOutput = new PrintWriter(spamFreq);
            Set<String> keys = trainSpamFreq.keySet();
            Iterator<String> keyIterator = keys.iterator();
            trainSpamFreq.entrySet().forEach(entry -> {
                fileOutput.println(entry.getKey() + ":" + entry.getValue());
            });
            fileOutput.close();
        }

        File hamProb = new File("PrW_iH.txt");
        hamProb.createNewFile();
        if (hamProb.canWrite()) {
            PrintWriter fileOutput = new PrintWriter(hamProb);
            Set<String> keys = pWH.keySet();
            Iterator<String> keyIterator = keys.iterator();
            pWH.entrySet().forEach(entry -> {
                fileOutput.println(entry.getKey() + ":" + entry.getValue());
            });
            fileOutput.close();
        }

        File spamProb = new File("PrW_iS.txt");
        spamProb.createNewFile();
        if (spamProb.canWrite()) {
            PrintWriter fileOutput = new PrintWriter(spamProb);
            Set<String> keys = pWS.keySet();
            Iterator<String> keyIterator = keys.iterator();
            pWS.entrySet().forEach(entry -> {
                fileOutput.println(entry.getKey() + ":" + entry.getValue());
            });
            fileOutput.close();
        }
        File totProb = new File("PrSW_i.txt");
        totProb.createNewFile();
        if (totProb.canWrite()) {
            PrintWriter fileOutput = new PrintWriter(totProb);
            Set<String> keys = pSW.keySet();
            Iterator<String> keyIterator = keys.iterator();
            pSW.entrySet().forEach(entry -> {
                fileOutput.println(entry.getKey() + ":" + entry.getValue());
            });
            fileOutput.close();
        }
	}
	
	//main method
	public static void main(String[] args) {
		
		if(args.length < 2){
			System.err.println("Usage: java WordCounter <inputDir> <outfile>");
			System.exit(0);
		}
		
		File dataDir1 = new File(args[0]);  // either train/ham or train/spam
		File dataDir2 = new File(args[1]);	// either train/ham or train/spam
		
		TrainingFile wordCounter = new TrainingFile();

		try{
			wordCounter.parseFile(dataDir1);
            wordCounter.parseFile(dataDir2);
			wordCounter.outputWordCount();

		}catch(FileNotFoundException e){
			System.err.println("Invalid input dir: " + dataDir1.getAbsolutePath() + " or " + dataDir2.getAbsolutePath());
			e.printStackTrace();
		}catch(IOException e){
			e.printStackTrace();
		}
	}
}