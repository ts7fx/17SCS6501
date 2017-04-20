package edu.illinois.cs.index.similarities;

import org.apache.lucene.search.similarities.BasicStats;
import org.apache.lucene.search.similarities.LMSimilarity;

public class DirichletPrior extends LMSimilarity {

    private LMSimilarity.DefaultCollectionModel model; // this would be your reference model
    private float queryLength = 0; // will be set at query time automatically
    private float docLen=0;
    public DirichletPrior() {
        model = new LMSimilarity.DefaultCollectionModel();
    }

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
    	docLen = docLength;
    	double mu = 3000;
    	//double mu = 1645; // 1645
    	double alphad = mu / (mu+docLength);
    	double a,b;
    	double pwc = model.computeProbability(stats);
    	a = (termFreq + mu * pwc)/(mu + docLength);
    	b = alphad * pwc;
    	double result = Math.log(a/b);
        return (float)result;
    }

    @Override
    public String getName() {
        return "Dirichlet Prior";
    }
    public float getDocLen() {
        return docLen;
    }

    @Override
    public String toString() {
        return getName();
    }

    public void setQueryLength(float length) {
        queryLength = length;
    }
}
