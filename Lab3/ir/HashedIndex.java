/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Johan Boye, 2010
 *   Second version: Johan Boye, 2012
 *   Additions: Hedvig Kjellström, 2012-14
 */  


package ir;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Collections;


/**
 *   Implements an inverted index as a Hashtable from words to PostingsLists.
 */
public class HashedIndex implements Index {

    /** The index as a hashtable. */
    private HashMap<String,PostingsList> index = new HashMap<String,PostingsList>();

    /**
     *  Inserts this token in the index.
     *  If the token doesn't exist create new PostingsList() and add to index
     */
    public void insert( String token, int docID, int offset ) {
        PostingsList list = index.get(token);
        if(list == null) {
            list = new PostingsList();   
        }
        list.add(docID, offset);
        index.put(token, list);   

    }


    /**
     *  Returns all the words in the index.
     */
    public Iterator<String> getDictionary() {
        return index.keySet().iterator();
    }


    /**
     *  Returns the postings for a specific term, or empty postingsList
     *  if the term is not in the index.
     */
    public PostingsList getPostings( String token ) {
        if(index.containsKey(token)) return index.get(token);
    	return new PostingsList();
    }


    /**
     *  Searches the index for postings matching the query.
     */
    public PostingsList search( Query query, int queryType, int rankingType, int structureType ) {

        // Intersect query
        if(queryType == Index.INTERSECTION_QUERY && query.size() > 0) {
            PostingsList answer = getPostings(query.getTerm(0));
            for(int i = 1; i < query.size(); i++) {
                if(index.containsKey(query.getTerm(i))) { answer = intersect(answer, index.get(query.getTerm(i))); }
                else { answer = new PostingsList(); }
            }
            return answer;
        }

        // Phrase query
        else if(queryType == Index.PHRASE_QUERY && query.size() > 0) {
            PostingsList answer = getPostings(query.getTerm(0));
            for(int i = 1; i < query.size(); i++) {
                if(index.containsKey(query.getTerm(i))) { 
                    answer = positionalIntersect(answer, index.get(query.getTerm(i)));
                }
                else { answer = new PostingsList(); }
            }
            return answer;
        }
        else if(queryType == Index.RANKED_QUERY && query.size() > 0) {
            return fastCosineScore(query);
        }
        return null;
    }


    /*
    *   Intersect method
    */
    public PostingsList intersect(PostingsList p1, PostingsList p2) {
        PostingsList answer = new PostingsList();
        int posP1 = 0;
        int posP2 = 0;
        while(posP1 < p1.size() && posP2 < p2.size()) {
            if(p1.get(posP1).getDocID() == p2.get(posP2).getDocID()) {
                answer.add(p1.get(posP1));
                posP1++;
                posP2++;
            }
            else if(p1.get(posP1).getDocID() < p2.get(posP2).getDocID()) {
                posP1++;
            }
            else {
                posP2++;
            }
        }
        return answer;
    }


    /*
    *   Phrase method
    */
    public PostingsList positionalIntersect(PostingsList list1, PostingsList list2) {
        PostingsList temp = new PostingsList();
        int offset1 = 0;
        int offset2 = 0;
        int p1 = 0;
        int p2 = 0;
        while(p1 < list1.size() && p2 < list2.size()) {
            PostingsEntry e1 = list1.get(p1);
            PostingsEntry e2 = list2.get(p2);
            if(e1.getDocID() == e2.getDocID()) {
                int firstOffset = 0;
                int secondOffset = 0;
                int firstOffsetSize = e1.sizeOfOffsets();
                int secondOffsetSize = e2.sizeOfOffsets();

                while(firstOffset != firstOffsetSize && secondOffset != secondOffsetSize) {
                    offset1 = e1.getOffset(firstOffset);
                    offset2 = e2.getOffset(secondOffset);
                    if(offset1+1 == offset2) {
                        temp.add(e2.getDocID(), offset2);
                        break;
                    }
                    else if(offset1+1 < offset2) {
                        firstOffset++;
                    }
                    else {
                        secondOffset++;
                    }
                }
                p1++;
                p2++;
            }
            else if(e1.getDocID() < e2.getDocID()) {
                p1++;
            }
            else {
                p2++;
            }
        }
        return temp;
    }

    public PostingsList fastCosineScore(Query query) {
        long start = System.nanoTime(); 

        PostingsList answer;
        int n = Index.docIDs.size();
        float[] scores = new float[n];
        int df = 0;
        float idf = 0.0f;
        for(int i = 0; i < query.size(); i++) {
            String s = query.getTerm(i);
            idf = 0.0f;
            if(index.containsKey(s)) {
                answer = getPostings(s);
                df = answer.size();
                idf = (float) Math.log10((float)n/(float)df);
                // 1) Index Elimination – use only high-idf terms at query-time (you do not have to make it work for phrase retrieval, 
                // i.e., no need to keep track of the relative word positions in the original query).
                // idf > 0.5 BRA SPEEDUP !
                if(idf > 1.0) {
                    for(int j = 0; j < df; j++) {
                        float tf = answer.get(j).sizeOfOffsets();
                        scores[answer.get(j).getDocID()] += query.getWeight(i)*tf*idf;
                    }
                }
            }
        }
        answer = new PostingsList();
        for(int i = 0; i < n; i++) {
            if(scores[i] > 0.0f) {
                int lenD = Index.docLengths.get(Integer.toString(i));
                scores[i] = scores[i]/ (float) Math.sqrt(lenD);
                PostingsEntry e = new PostingsEntry(i, 1);
                e.setScore(scores[i]);
                answer.add(e);
            }
        }

        Collections.sort(answer.getList());
        long end = System.nanoTime(); 
        System.out.println("FastCosineSimilarity took: "+(end-start)+" ns");
        return answer;
    }


    /**
     *  No need for cleanup in a HashedIndex.
     */
    public void cleanup() {}
}
