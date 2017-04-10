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

public class Evaluate { 
	/**
	 * Computes evaluate functions for chosen retrieval models
	 * 
	 * Format for judgements.txt is:
	 * 
	 * line 0: <query 1 text> line 1: <space-delimited list of relevant URLs>
	 * line 2: <query 2 text> line 3: <space-delimited list of relevant URLs>
	 * ...
	 * Please keep all these constants!
	 */
	
	private static final String _judgeFile = "npl-judgements.txt"; // judgement file is the file for queries and rel docs.
																   // serves as the ground truth for evaluation.
	final static String _indexPath = "lucene-npl-index"; // ??
	static Searcher _searcher = null; // what does a searcher do?

	//Please implement P@K, MRR and NDCG accordingly
	public static void main(String[] args) throws IOException { // this is the main method
		String method = "--dp";// specify the ranker you want to test
		
		_searcher = new Searcher(_indexPath);		// initiate a new searcher
		Runner.setSimilarity(_searcher, method);   	// set similarity based on model chosen. bdp (boolean dot product)
		BufferedReader br = new BufferedReader(new FileReader(_judgeFile)); // a bufferedreader to read the document line by line
		String line = null, judgement = null;		// what are these
		int k = 10;									// k is used for p@k and NDCG@k
		double meanAvgPrec = 0.0, p_k = 0.0, mRR = 0.0, nDCG = 0.0; // set params to be zeros initially
		double numQueries = 0.0;					// set params to be zeros initially
		while ((line = br.readLine()) != null) { 	// when there is next line, do the following:
			judgement = br.readLine();				// set judgement to be the currentline
			
			//compute corresponding AP
			meanAvgPrec += AvgPrec(line, judgement); // line is query, where judgement is a list of numbers
			//compute corresponding P@K				
			p_k += Prec(line, judgement, k);		 // the list of numbers refer to relevant documents. 
			//compute corresponding MRR
			mRR += RR(line, judgement);
			//compute corresponding NDCG
			nDCG += NDCG(line, judgement, k);
			
			++numQueries; 							// increment query counter to indicate completion of one line.
		}
		br.close();

		System.out.println("\nMAP: " + meanAvgPrec / numQueries);//this is the final MAP performance of your selected ranker
		System.out.println("\nP@" + k + ": " + p_k / numQueries);//this is the final P@K performance of your selected ranker
		System.out.println("\nMRR: " + mRR / numQueries);//this is the final MRR performance of your selected ranker
		System.out.println("\nNDCG: " + nDCG / numQueries); //this is the final NDCG performance of your selected ranker
	}

	private static double AvgPrec(String query, String docString) {
		ArrayList<ResultDoc> results = _searcher.search(query).getDocs(); // documents that are returned by a query
		System.out.println(results.size()); // why does the search only return 10 documents?
		
		if (results.size() == 0)
			return 0; // no result returned

		HashSet<String> relDocs = new HashSet<String>(Arrays.asList(docString.split(" "))); // a set of relevent docs
																							// this is the ground truth
		int i = 1;	// what is this for?
		double avgp = 0.0;
		double numRel = 0; // number of relevant docs
		System.out.println("\nQuery: " + query); // print out the query
		for (ResultDoc rdoc : results) { // for every document in the returned set of documents
			if (relDocs.contains(rdoc.title())) { // if the document is in the set of relevant documents
				//how to accumulate average precision (avgp) when we encounter a relevant document
				numRel ++;
				avgp += (numRel / i);
				System.out.print("  ");
			} else {
				//how to accumulate average precision (avgp) when we encounter an irrelevant document
				System.out.print("X ");
			}
			System.out.println(i + ". " + rdoc.title());
			++i;
		}
		// compute average precision here
		if (numRel == 0)
			return 0;
		avgp /= numRel;
		System.out.println("Average Precision: " + avgp);
		return avgp;
	}
	
	//precision at K
	private static double Prec(String query, String docString, int k) {
		ArrayList<ResultDoc> results = _searcher.search(query).getDocs(); // get results using current model
		System.out.println(results.size()); // why does the search only return 10 documents?
		if (results.size() == 0) // no results, no precision
			return 0;
		HashSet<String> relDocs = new HashSet<String>(Arrays.asList(docString.split(" "))); // a set of relevent docs
		double p_k = 0;
		double numRel = 0;
		for(int i = 0; i < k; i++){
			if (relDocs.contains(results.get(i).title())){
				System.out.println("blablabla"+results.get(i));
				numRel ++;
			}
		}
		p_k = numRel / k;
		//your code for computing precision at K here
		return p_k;
	}
	
	//Reciprocal Rank
	private static double RR(String query, String docString) {
		ArrayList<ResultDoc> results = _searcher.search(query).getDocs(); // get results using current model
		if (results.size() == 0) 
			return 0;
		HashSet<String> relDocs = new HashSet<String>(Arrays.asList(docString.split(" "))); // a set of relevent docs
		double relPosition = 0;
		double rr = 0;
		for (ResultDoc rdoc : results) { // for every document in the returned set of documents
			if (relDocs.contains(rdoc.title())) { // if the document is in the set of relevant documents
				relPosition = results.indexOf(rdoc) +1; 
				System.out.print("  RRRRR" + relPosition);
				break;
			} 
		}
		if (relPosition == 0)
			return 0;
		rr = 1/relPosition;
		return rr;
	}
	
	//Normalized Discounted Cumulative Gain
	private static double NDCG(String query, String docString, int k) {
		ArrayList<ResultDoc> results = _searcher.search(query).getDocs(); // documents that are returned by a query
		if (results.size() == 0)
			return 0;
		HashSet<String> relDocs = new HashSet<String>(Arrays.asList(docString.split(" "))); // a set of relevent docs
		double numRel = 0;
		double DCG = 0, IDCG=0;
		for(int i = 0; i < k; i++){
			
			if (relDocs.contains(results.get(i).title())){ // if contains the ith result
				DCG += 1/(Math.log(2+i) / Math.log(2));
				numRel ++;
			}
			else {
				DCG += 0/(Math.log(2+i) / Math.log(2));
			}
		}
		// calculate ideal DCG
		for(int i = 0; i<numRel; i++){
			IDCG += 1/(Math.log(2+i) / Math.log(2));
		}
		if (IDCG == 0)
			return 0;
		double ndcg = DCG / IDCG;
		// 1. calculate DCG at k
		// 2. calculate idea DCG at k
		// 3. calculate NDCG at k
		// relevance label..?
		return ndcg;

	}
}