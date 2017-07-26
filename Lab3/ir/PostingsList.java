/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Johan Boye, 2010
 *   Second version: Johan Boye, 2012
 */  

package ir;

import java.util.LinkedList;
import java.io.Serializable;

/**
 *   A list of postings for a given word.
 */
public class PostingsList implements Serializable {
    
    /** The postings list as a linked list. */
    private LinkedList<PostingsEntry> list = new LinkedList<PostingsEntry>();

    /**  Number of postings in this list  */
    public int size() { return list.size(); }

    /**  Returns the ith posting */
    public PostingsEntry get( int i ) { return list.get( i ); }

    // Add method that adds an PostingsEntry to LinkedList list
    public void add (PostingsEntry p) { list.add(p); }

    public LinkedList<PostingsEntry> getList() { return list; }

    // Add method with two parameters 
    // If the token we want to add has no elements (offset) or the lastly added 
    // PostingsEntry has different docID from the current one, we add a new PostingsEntry.
    // else add the offset to the lastly added PostingsEntry to avoid duplicate documents
    public void add (int docID, int offset) {
        if(list.size() == 0 ||list.peekLast().getDocID() != docID) {
            PostingsEntry p = new PostingsEntry(docID, offset);
            list.add(p);
        }
        else {
            list.getLast().addOffset(offset);
        }
    }

}
			   
