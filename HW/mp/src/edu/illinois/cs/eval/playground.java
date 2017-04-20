package edu.illinois.cs.eval;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import edu.illinois.cs.index.ResultDoc;
import edu.illinois.cs.index.Runner;
import edu.illinois.cs.index.Searcher;

public class playground {
	private static final String _judgeFile = "npl-judgements.txt"; 
	final static String _indexPath = "lucene-npl-index"; 
	static Searcher _searcher = null; 

	public static void main(String[] args) throws IOException {
		// TODO Auto-generated method stub
		String method = "--jm";// specify the ranker you want to test
		
		_searcher = new Searcher(_indexPath);		// initiate a new searcher
		Runner.setSimilarity(_searcher, method);   	// set similarity based on model chosen. bdp (boolean dot product)
		BufferedReader br = new BufferedReader(new FileReader(_judgeFile)); // a bufferedreader to read the document line by line
		String line = null, judgement = null;		// what are these
		int k = 10;									// k is used for p@k and NDCG@k
		double meanAvgPrec = 0.0, p_k = 0.0, mRR = 0.0, nDCG = 0.0; // set params to be zeros initially
		double numQueries = 0.0;					// set params to be zeros initially
		while ((line = br.readLine()) != null) { 	// when there is next line, do the following:
			judgement = br.readLine();				// set judgement to be the currentline
			
			meanAvgPrec += AvgPrec(line, judgement); // line is query, where judgement is a list of numbers
			System.out.println(line + " " + AvgPrec(line, judgement));
			
			++numQueries; 							// increment query counter to indicate completion of one line.
		}
		br.close();

		System.out.println("\nMAP: " + meanAvgPrec / numQueries);//this is the final MAP performance of your selected ranker
		
	}

	private static double AvgPrec(String query, String docString) {
		ArrayList<ResultDoc> results = _searcher.search(query).getDocs(); // documents that are returned by a query		
		if (results.size() == 0)
			return 0; // no result returned

		HashSet<String> relDocs = new HashSet<String>(Arrays.asList(docString.split(" "))); // a set of relevent docs
																							// this is the ground truth
		int i = 1;	// what is this for?
		double avgp = 0.0;
		double numRel = 0; // number of relevant docs
		for (ResultDoc rdoc : results) { // for every document in the returned set of documents
			if (relDocs.contains(rdoc.title())) { // if the document is in the set of relevant documents
				//how to accumulate average precision (avgp) when we encounter a relevant document
				numRel ++;
				avgp += (numRel / i);
			} else {
				//how to accumulate average precision (avgp) when we encounter an irrelevant document
			}
			++i;
		}
		// compute average precision here
		if (numRel == 0)
			return 0;
		avgp /= numRel;
		return avgp;
	}
	
	//precision at K

	}


