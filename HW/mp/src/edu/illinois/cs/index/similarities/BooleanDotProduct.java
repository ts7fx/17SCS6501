package edu.illinois.cs.index.similarities;

import org.apache.lucene.search.similarities.BasicStats;
import org.apache.lucene.search.similarities.SimilarityBase;

public class BooleanDotProduct extends SimilarityBase {
    /**
     * Returns a score for a single term in the document.
     *
     * @param stats
     *            Provides access to corpus-level statistics
     * @param termFreq
     * @param docLength
     */
    @Override
    protected float score(BasicStats stats, float termFreq, float docLength) { 
    	// this function is called once per word in the query for each document where that word occurs.
    	
    	// Once all the documents are scored, Lucene outputs a list of documents ranked by their score.
    	
    	// where and how is this function used?
        return 1;
    }

    @Override
    public String toString() {
        return "Boolean Dot Product";
    }
}
