/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Johan Boye, 2010
 *   Second version: Johan Boye, 2012
 */  

package ir;

import java.io.Serializable;
import java.util.LinkedList;

public class PostingsEntry implements Comparable<PostingsEntry>, Serializable {
    
    public int docID;
    public float score;
    // Added this LinkedList to keep all offsets for current word and avoid duplicate documents
    private LinkedList<Integer> offsets = new LinkedList<Integer>();
    // Added to make this entry as document, with many tokens and amount of their occurances
    private LinkedList<Integer> tokenOccurances = new LinkedList<Integer>();

    /* Constructor */
    public PostingsEntry(int docID, int offset) {
        this.docID = docID;
        this.score = 0.0f;
        addOffset(offset);
    }

    /**
     *  PostingsEntries are compared by their score (only relevant 
     *  in ranked retrieval).
     *
     *  The comparison is defined so that entries will be put in 
     *  descending order.
     */
    public int compareTo( PostingsEntry other ) { return Float.compare( other.score, score ); }

    // Returns docID
    public int getDocID() { return docID; }

    public int sizeOfOffsets() { return offsets.size(); }

    public void setScore(float f) { this.score = f; }
    public float getScore() { return score; }

    // TODO
    public void addOccurance(int n) { tokenOccurances.add(n); }
    public int getPositionOccurance(int i) { return tokenOccurances.get(i); }
    public int sizeOfOccurance() { return tokenOccurances.size(); }
    public void clearOccurances() { tokenOccurances.clear(); }
    public LinkedList<Integer> getOccurances() { return tokenOccurances; }

    // Adds offset last in the LinkedList
    public void addOffset(int offset) { offsets.add(offset); }

    // Returns the ith posting of offsets
    public int getOffset(int i) { return offsets.get( i ); }

    public float getWeight() {
        if(offsets.size() > 0) {
            return 1.0f+(float)Math.log((float)offsets.size());
        }
        else {
            return 0.0f;
        }
    }

}
