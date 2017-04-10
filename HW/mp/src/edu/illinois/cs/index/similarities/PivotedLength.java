package edu.illinois.cs.index.similarities;

import org.apache.lucene.search.similarities.BasicStats;
import org.apache.lucene.search.similarities.SimilarityBase;

public class PivotedLength extends SimilarityBase {
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
    	double s = 0.75;
    	double comp1, comp2, comp3;
    	comp1 = (1 + Math.log(1 + Math.log(termFreq))) / (1 + s + s * docLength / stats.getAvgFieldLength());
    	comp2 = 1;
    	comp3 = Math.log((stats.getNumberOfDocuments() + 1) / (stats.getDocFreq()));    			
    	double result = comp1 * comp2 * comp3;  	
        return (float)result;
    }

    @Override
    public String toString() {
        return "Pivoted Length Normalization";
    }

}
