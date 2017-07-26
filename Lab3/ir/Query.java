/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Hedvig Kjellström, 2012
 */  

package ir;

import java.util.LinkedList;
import java.util.StringTokenizer;
import java.nio.charset.StandardCharsets;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.Iterator;

public class Query {
    
    public LinkedList<String> terms = new LinkedList<String>();
    public LinkedList<Double> weights = new LinkedList<Double>();

    /**
     *  Creates a new empty Query 
     */
    public Query() {
    }
	
    /**
     *  Creates a new Query from a string of words
     */
    public Query( String queryString  ) {
    	StringTokenizer tok = new StringTokenizer( queryString );
    	while ( tok.hasMoreTokens() ) {
            String s = tok.nextToken();
            if(!terms.contains(s)) {
    	       terms.add( s );
    	       weights.add( new Double(1) );
            }
    	}
    }
    
    /**
     *  Returns the number of terms
     */
    public int size() {
	   return terms.size();
    }

    public int sizeWeights() {
       return weights.size();
    }
    
    /**
     *  Returns a shallow copy of the Query
     */
    public Query copy() {
    	Query queryCopy = new Query();
    	queryCopy.terms = (LinkedList<String>) terms.clone();
    	queryCopy.weights = (LinkedList<Double>) weights.clone();
    	return queryCopy;
    }
    
    /**
     *  Expands the Query using Relevance Feedback
     */
    public void relevanceFeedback( PostingsList results, boolean[] docIsRelevant, Indexer indexer ) {
	// results contain the ranked list from the current search
	// docIsRelevant contains the users feedback on which of the 10 first hits are relevant
        long start = System.nanoTime(); 
    	double alpha = 1.0;
        double beta = 0.75;
        int n = indexer.index.docIDs.size();
        int relevantDocs = 0;
        // New query
        HashMap<String, Double> relDocVec = new HashMap<String, Double>();

        // Calculate d_j for every relevant doc
        for(int i = 0; i < docIsRelevant.length; i++) {
            if(docIsRelevant[i]) {
                relevantDocs++;
                try {
                    int docID = results.get(i).getDocID();
                    String fileName = indexer.index.docIDs.get(Integer.toString(docID));
                    File f = new File("/Users/Aleksandar/Desktop/IrGroupProject16/Lab3/davisWiki/" + fileName);
                    Reader reader = new InputStreamReader( new FileInputStream(f), StandardCharsets.UTF_8 );
                    Tokenizer tok = new Tokenizer( reader, true, false, true, indexer.patterns_file );
                    int lenD = Index.docLengths.get(Integer.toString(docID));
                    while ( tok.hasMoreTokens() ) {
                        String token = tok.nextToken();
                        if(relDocVec.containsKey(token)) {
                            relDocVec.put(token, relDocVec.get(token) + 1.0/(double)Math.sqrt(lenD));
                            //relDocVec.put(token, relDocVec.get(token) + 1.0/(double) lenD);
                        }
                        else {
                            relDocVec.put(token, 1.0/(double)Math.sqrt(lenD));
                            //relDocVec.put(token, 1.0/(double)lenD);
                        }
                    }
                    reader.close();
                }
                catch(Exception e) {
                    e.printStackTrace();
                }
            }
        }

        // qm = beta*1.0/numOfRelDocs*sum of d_j
        for (String token : relDocVec.keySet()) {
            PostingsList tmp = indexer.index.getPostings(token);
            double idf = (Double) Math.log10((double)n/(double)tmp.size());
            relDocVec.put(token, (beta*(relDocVec.get(token)*idf)/relevantDocs));
        }

        // q0 = alpha*weight and add that to beta part
        for(int i = 0; i < terms.size(); i++) {
            if(relDocVec.containsKey(terms.get(i))) {
                relDocVec.put(terms.get(i), relDocVec.get(terms.get(i)) + (alpha*weights.get(i)));
            }
            else {
                relDocVec.put(terms.get(i), (alpha*weights.get(i)));
            }
        }

        this.terms = new LinkedList(relDocVec.keySet());
        this.weights = new LinkedList(relDocVec.values());

        long end = System.nanoTime(); 
        System.out.println("relevanceFeedback took: "+(end-start)+ " ns");
    }

    public String getTerm(int i) {
        return terms.get( i );
    }

    public double getWeight(int i) {
        return weights.get( i );
    }
}
    
