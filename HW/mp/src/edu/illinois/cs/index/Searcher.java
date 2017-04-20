package edu.illinois.cs.index;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.InvalidTokenOffsetsException;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.SimpleHTMLFormatter;
import org.apache.lucene.search.similarities.Similarity;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import edu.illinois.cs.index.similarities.DirichletPrior;
import edu.illinois.cs.index.similarities.JelinekMercer;

public class Searcher
{
    private IndexSearcher indexSearcher;
    private SpecialAnalyzer analyzer;
    private static SimpleHTMLFormatter formatter;
    private static final int numFragments = 4;
    private static final String defaultField = "content";

    /**
     * Sets up the Lucene index Searcher with the specified index.
     *
     * @param indexPath
     *            The path to the desired Lucene index.
     */
    public Searcher(String indexPath)
    {
        try
        {
            IndexReader reader = DirectoryReader.open(FSDirectory.open(new File(indexPath)));
            indexSearcher = new IndexSearcher(reader);
            analyzer = new SpecialAnalyzer();
            formatter = new SimpleHTMLFormatter("****", "****");
        }
        catch(IOException exception)
        {
            exception.printStackTrace();
        }
    }

    public void setSimilarity(Similarity sim)
    {
        indexSearcher.setSimilarity(sim);
    }

    /**
     * The main search function.
     * @param searchQuery Set this object's attributes as needed.
     * @return
     */
    public SearchResult search(SearchQuery searchQuery)
    {
        BooleanQuery combinedQuery = new BooleanQuery();
        for(String field: searchQuery.fields())
        {
            QueryParser parser = new QueryParser(Version.LUCENE_46, field, analyzer);
            try
            {
                Query textQuery = parser.parse(searchQuery.queryText());
                combinedQuery.add(textQuery, BooleanClause.Occur.MUST);
            }
            catch(ParseException exception)
            {
                exception.printStackTrace();
            }
        }

        return runSearch(combinedQuery, searchQuery);
    }

    /**
     * The simplest search function. Searches the abstract field and returns a
     * the default number of results.
     *
     * @param queryText
     *            The text to search
     * @return the SearchResult
     */
    public SearchResult search(String queryText)
    {
        return search(new SearchQuery(queryText, defaultField));
    }

    /**
     * Performs the actual Lucene search.
     *
     * @param luceneQuery
     * @param numResults
     * @return the SearchResult
     */
    private SearchResult runSearch(Query luceneQuery, SearchQuery searchQuery)
    {
        try
        {
            System.out.println("\nScoring documents with " + indexSearcher.getSimilarity().toString());
            Similarity sim = indexSearcher.getSimilarity();
            double len = 0;
            // have to do this to figure out query length in the LM scorers
            if(sim instanceof JelinekMercer)
            {
                Set<Term> terms = new HashSet<Term>();
                luceneQuery.extractTerms(terms);
                ((JelinekMercer) sim).setQueryLength(terms.size());
                len = terms.size();
            }
            else if(sim instanceof DirichletPrior)
            {
                Set<Term> terms = new HashSet<Term>();
                luceneQuery.extractTerms(terms);
                ((DirichletPrior) sim).setQueryLength(terms.size());
                len = terms.size();
            }

            TopDocs docs = indexSearcher.search(luceneQuery, searchQuery.fromDoc() + searchQuery.numResults());
            // this is the search..  that return the top 10 docs
            
            ScoreDoc[] hits = docs.scoreDocs;
            if(sim instanceof JelinekMercer)
            {
            	for(ScoreDoc hit : hits)
                	hit.score += len * Math.log(0.1);
                
            }
            else if(sim instanceof DirichletPrior)
            {
                float docL = ((DirichletPrior) sim).getDocLen();
                for(ScoreDoc hit : hits){
                	//System.out.println(hit.score);
                	//double alphad = 1645 / (1645+docL);
                	double alphad = 3000 / (3000+docL);
                	hit.score += len * Math.log(alphad);
                }
            }
            // sort
            boolean swapped = true;
            int j = 0;
            ScoreDoc tmp;
            while (swapped) {
                  swapped = false;
                  j++;
                  for (int i = 0; i < hits.length - j; i++) {                                       
                        if (hits[i].score < hits[i + 1].score) {                          
                              tmp = hits[i];
                              hits[i] = hits[i+1];
                              hits[i+1] = tmp;
                              swapped = true;
                        }
                  }                
            }
            //
            String field = searchQuery.fields().get(0);

            SearchResult searchResult = new SearchResult(searchQuery, docs.totalHits);
            for(ScoreDoc hit : hits)
            {
                Document doc = indexSearcher.doc(hit.doc);
                ResultDoc rdoc = new ResultDoc(hit.doc);
                String highlighted = null;
                try
                {
                    Highlighter highlighter = new Highlighter(formatter, new QueryScorer(luceneQuery));
                    rdoc.title("" + (hit.doc + 1));
                    String contents = doc.getField(field).stringValue();
                    rdoc.content(contents);
                    String[] snippets = highlighter.getBestFragments(analyzer, field, contents, numFragments);
                    highlighted = createOneSnippet(snippets);
                }
                catch(InvalidTokenOffsetsException exception)
                {
                    exception.printStackTrace();
                    highlighted = "(no snippets yet)";
                }

                searchResult.addResult(rdoc);
                searchResult.setSnippet(rdoc, highlighted);
            }

            searchResult.trimResults(searchQuery.fromDoc());
            return searchResult;
        }
        catch(IOException exception)
        {
            exception.printStackTrace();
        }
        return new SearchResult(searchQuery);
    }

    /**
     * Create one string of all the extracted snippets from the highlighter
     * @param snippets
     * @return
     */
    private String createOneSnippet(String[] snippets)
    {
        String result = " ... ";
        for(String s: snippets)
            result += s + " ... ";
        return result;
    }
}
