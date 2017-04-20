package edu.illinois.cs.index.similarities;

import org.apache.lucene.search.similarities.BasicStats;
import org.apache.lucene.search.similarities.SimilarityBase;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import edu.illinois.cs.index.ResultDoc;
import edu.illinois.cs.index.Runner;
import edu.illinois.cs.index.Searcher;
public class OkapiBM25 extends SimilarityBase {
    /**
     * Returns a score for a single term in the document.
     *
     * @param stats
     *            Provides access to corpus-level statistics
     * @param termFreq
     * @param docLength
     */
	private static final String _judgeFile = "npl-judgements.txt"; 
	final static String _indexPath = "lucene-npl-index"; 
	static Searcher _searcher = null;
	
	//static double k1 = 1.2, k2 = 1000, b = 0.75; // k1∈[1.2,2],k2∈(0,1000],b∈[0.75,1.2]
    @Override
    protected float score(BasicStats stats, float termFreq, float docLength) {
    	double k1=1.2, k2=750, b=0.75;
    	double comp1, comp2, comp3;
    	comp1 = Math.log((stats.getNumberOfDocuments() - stats.getDocFreq() + 0.5) / (stats.getDocFreq() + 0.5));
    	comp2 = ((k1+1) * termFreq) / (k1 * (1 - b + b * docLength/stats.getAvgFieldLength()) + termFreq);
    	comp3 = ((k2 + 1) * 1) / (k2 + 1); // here we assume thatt c(w;q)=1
    	double s = comp1 * comp2 * comp3;
    	return (float)s;
    }

    @Override
    public String toString() {
        return "Okapi BM25";
    }

}
