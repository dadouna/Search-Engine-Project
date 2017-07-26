/*  
 *   This file is part of the computer assignment for the
 *   Information Retrieval course at KTH.
 * 
 *   First version:  Johan Boye, 2012
 */  

import java.util.*;
import java.io.*;

public class PageRank{

    /**  
     *   Maximal number of documents. We're assuming here that we
     *   don't have more docs than we can keep in main memory.
     */
    final static int MAX_NUMBER_OF_DOCS = 2000000;

    /**
     *   Mapping from document names to document numbers.
     */
    Hashtable<String,Integer> docNumber = new Hashtable<String,Integer>();

    /**
     *   Mapping from document numbers to document names
     */
    String[] docName = new String[MAX_NUMBER_OF_DOCS];

    /**  
     *   A memory-efficient representation of the transition matrix.
     *   The outlinks are represented as a Hashtable, whose keys are 
     *   the numbers of the documents linked from.<p>
     *
     *   The value corresponding to key i is a Hashtable whose keys are 
     *   all the numbers of documents j that i links to.<p>
     *
     *   If there are no outlinks from i, then the value corresponding 
     *   key i is null.
     */
    Hashtable<Integer,Hashtable<Integer,Boolean>> link = new Hashtable<Integer,Hashtable<Integer,Boolean>>();

    /**
     *   The number of outlinks from each node.
     */
    int[] out = new int[MAX_NUMBER_OF_DOCS];

    /**
     *   The number of documents with no outlinks.
     */
    int numberOfSinks = 0;

    /**
     *   The probability that the surfer will be bored, stop
     *   following links, and take a random jump somewhere.
     */
    final static double BORED = 0.15;

    /**
     *   Convergence criterion: Transition probabilities do not 
     *   change more that EPSILON from one iteration to another.
     */
    final static double EPSILON = 0.0001;

    /**
     *   Never do more than this number of iterations regardless
     *   of whether the transistion probabilities converge or not.
     */
    final static int MAX_NUMBER_OF_ITERATIONS = 1000;

    
    /* --------------------------------------------- */

    int m = 100;

    public PageRank( String filename ) {
    //long startTime = System.currentTimeMillis();
	int noOfDocs = readDocs( filename );

	ArrayList<Node> pr_exact = computePagerank( noOfDocs );

   /* for(int i = 10; i < 110; i+=10) {
    m = i;
    System.out.println("------ M: " +m+" -------");
    System.out.println(squaredError(pr_exact, randomStartMC( noOfDocs ), 0));
    System.out.println(squaredError(pr_exact, cyclicStartMC( noOfDocs ), 0));
    System.out.println(squaredError(pr_exact, completePathMC( noOfDocs ), 0));
    System.out.println(squaredError(pr_exact, cpDanglingNodesMC( noOfDocs ), 0));
    System.out.println(squaredError(pr_exact, cpRandomStartMC( noOfDocs ), 0));
    System.out.println();
    System.out.println(squaredError(pr_exact, randomStartMC( noOfDocs ), 1));
    System.out.println(squaredError(pr_exact, cyclicStartMC( noOfDocs ), 1));
    System.out.println(squaredError(pr_exact, completePathMC( noOfDocs ), 1));
    System.out.println(squaredError(pr_exact, cpDanglingNodesMC( noOfDocs ), 1));
    System.out.println(squaredError(pr_exact, cpRandomStartMC( noOfDocs ), 1));
    }*/

    /*m = 100;
    System.out.println("M: " +m);
    System.out.println("MC1 HIGHEST comparing to PR "+squaredError(pr_exact, randomStartMC( noOfDocs ), 0));
    System.out.println("MC2 HIGHEST comparing to PR "+squaredError(pr_exact, cyclicStartMC( noOfDocs ), 0));
    System.out.println("MC3 HIGHEST comparing to PR "+squaredError(pr_exact, completePathMC( noOfDocs ), 0));
    System.out.println("MC4 HIGHEST comparing to PR "+squaredError(pr_exact, cpDanglingNodesMC( noOfDocs ), 0));
    System.out.println("MC5 HIGHEST comparing to PR "+squaredError(pr_exact, cpRandomStartMC( noOfDocs ), 0));
    System.out.println();
    System.out.println("MC1 LOWEST comparing to PR "+squaredError(pr_exact, randomStartMC( noOfDocs ), 1));
    System.out.println("MC2 LOWEST comparing to PR "+squaredError(pr_exact, cyclicStartMC( noOfDocs ), 1));
    System.out.println("MC3 LOWEST comparing to PR "+squaredError(pr_exact, completePathMC( noOfDocs ), 1));
    System.out.println("MC4 LOWEST comparing to PR "+squaredError(pr_exact, cpDanglingNodesMC( noOfDocs ), 1));
    System.out.println("MC5 LOWEST comparing to PR "+squaredError(pr_exact, cpRandomStartMC( noOfDocs ), 1)); */

    //completePathMC( noOfDocs );

    /*randomStartMC( noOfDocs );
    cyclicStartMC( noOfDocs );
    completePathMC( noOfDocs );
    cpDanglingNodesMC( noOfDocs );
    cpRandomStartMC( noOfDocs ); */

    //long endTime   = System.currentTimeMillis();
    //long totalTime = endTime - startTime;
    //System.out.println(totalTime/1000);
    }


    public double squaredError(ArrayList<Node> a, ArrayList<Node> b, int n) {
        double error = 0.0;
        int start = 0;
        int end = 30;
        if(n == 1) {
            start = a.size()-30;
            end = a.size();
        }
        for(int i = start; i < end; i++) {
            for(int j = 0; j < b.size(); j++) {
                if(a.get(i).getID().equals(b.get(j).getID())) {
                    error += Math.pow(a.get(i).getPR()-b.get(j).getPR(),2.0);
                    break;
                }
            }
        }
        return error;
    }



    /* --------------------------------------------- */


    /**
     *   Reads the documents and creates the docs table. When this method 
     *   finishes executing then the @code{out} vector of outlinks is 
     *   initialised for each doc, and the @code{p} matrix is filled with
     *   zeroes (that indicate direct links) and NO_LINK (if there is no
     *   direct link. <p>
     *
     *   @return the number of documents read.
     */
    int readDocs( String filename ) {
	int fileIndex = 0;
	try {
	    System.err.print( "Reading file... " );
	    BufferedReader in = new BufferedReader( new FileReader( filename ));
	    String line;
	    while ((line = in.readLine()) != null && fileIndex<MAX_NUMBER_OF_DOCS ) {
		int index = line.indexOf( ";" );
		String title = line.substring( 0, index );
		Integer fromdoc = docNumber.get( title );
		//  Have we seen this document before?
		if ( fromdoc == null ) {	
		    // This is a previously unseen doc, so add it to the table.
		    fromdoc = fileIndex++;
		    docNumber.put( title, fromdoc );
		    docName[fromdoc] = title;
		}
		// Check all outlinks.
		StringTokenizer tok = new StringTokenizer( line.substring(index+1), "," );
		while ( tok.hasMoreTokens() && fileIndex<MAX_NUMBER_OF_DOCS ) {
		    String otherTitle = tok.nextToken();
		    Integer otherDoc = docNumber.get( otherTitle );
		    if ( otherDoc == null ) {
			// This is a previousy unseen doc, so add it to the table.
			otherDoc = fileIndex++;
			docNumber.put( otherTitle, otherDoc );
			docName[otherDoc] = otherTitle;
		    }
		    // Set the probability to 0 for now, to indicate that there is
		    // a link from fromdoc to otherDoc.
		    if ( link.get(fromdoc) == null ) {
			link.put(fromdoc, new Hashtable<Integer,Boolean>());
		    }
		    if ( link.get(fromdoc).get(otherDoc) == null ) {
			link.get(fromdoc).put( otherDoc, true );
			out[fromdoc]++;
		    }
		}
	    }
	    if ( fileIndex >= MAX_NUMBER_OF_DOCS ) {
		System.err.print( "stopped reading since documents table is full. " );
	    }
	    else {
		System.err.print( "done. " );
	    }
	    // Compute the number of sinks.
	    for ( int i=0; i<fileIndex; i++ ) {
		if ( out[i] == 0 )
		    numberOfSinks++;
	    }
	}
	catch ( FileNotFoundException e ) {
	    System.err.println( "File " + filename + " not found!" );
	}
	catch ( IOException e ) {
	    System.err.println( "Error reading file " + filename );
	}
	System.err.println( "Read " + fileIndex + " number of documents" );
	return fileIndex;
    }


    /* --------------------------------------------- */


    /*
     *   Computes the pagerank of each document.
     */
    ArrayList<Node> computePagerank( int numberOfDocs ) {
        double[] init = new double[numberOfDocs];
        double[] g = new double[numberOfDocs];
        Random rand = new Random();
        // Set probability to every document as 1.0/numberOfDocs
        for(int i = 0; i < numberOfDocs; i++) {
            init[i] = 1.0/(double) numberOfDocs;
        }
        int iter = MAX_NUMBER_OF_ITERATIONS;
        // While unitl MAX_NUMBER_OF_ITERATIONS = 1000 reached
        while(iter > 0) {
            iter--;
            for(int i = 0; i < numberOfDocs; i++) {
                g[i] = 0.0;
                for(int j = 0; j < numberOfDocs; j++) {
                    double prob = 0.0;
                    // If the j is a sink and doesn't have any outgoing links, chose random node/document
                    // prob for that document will 1.0/numberOfDocs
                    if(out[j] == 0) {
                        prob = 1.0/(double) numberOfDocs;
                    }
                    // If j has outgoing links but not to i, then
                    // compute only BORED/numberOfDocs because parenthesis in the PR formula is 0
                    else if(link.get(j).get(i) == null) {
                        prob = BORED/(double) numberOfDocs;
                    }
                    // If none above are true, then j must have links to i, use the PR formula
                    else {
                        prob = ((1.0-BORED)/(double) out[j]) + (BORED/(double) numberOfDocs);
                    }
                    // Sum up the init[i] (prevValue) PR(q), probability*prob for all docs in g[i]
                    g[i] += init[j]*prob;
                }
            }
            // See if it converges
            // Sum upp all differences betwwen g[i] and init[i]
            double diff = 0.0;
            for(int i = 0; i < numberOfDocs; i++) {
                diff += Math.abs(g[i]-init[i]);
                init[i] = g[i];
            }
            if(diff < EPSILON) {
                break;
            }
        }

        // Add Nodes/documents to the arrayList so we can sort it according to PR value
        ArrayList<Node> result = new ArrayList<Node>();
        for(int i = 0; i < numberOfDocs; i++) {
            result.add(new Node(docName[i], g[i]));
        }
        // Sort the ArrayList by the PR value
        Collections.sort(result);
        // Print out top 30 documents
        for(int i = 0; i < 30; i++) {
            System.out.println(result.get(i).getID() + ": " + String.format(Locale.US, "%.5f", result.get(i).getPR()));
        }
        return result;

    }

    // Node class for storing PR and docID to be able to sort
    private class Node implements Comparable<Node> {
        private String docID;
        private double pageRank;

        Node(String id, double pr) {
            this.docID = id;
            this.pageRank = pr;
        }

        public String getID() { return docID; }
        public double getPR() { return pageRank; }

        public int compareTo(Node n) {
            if(this.pageRank < n.getPR()) {
                return 1;
            }
            if(this.pageRank > n.getPR()) {
                return -1;
            }
            return 0;
        }
    }

    // Picks a random linking docuemnt from the current document
    // and returns that node's ID as int 
    public int randomLinkingNode(int current) {
        Random rand = new Random();
        int randomNeighbour = rand.nextInt(link.get(current).size());
        Enumeration iter = link.get(current).keys();
        int result = 0;
        while (iter.hasMoreElements() && randomNeighbour > -1) {
            result = (int) iter.nextElement();
            randomNeighbour--;
        }   
        return result;
    }

    // Algorithm 1 MC end-point with random start
    ArrayList<Node> randomStartMC( int numberOfDocs ) {
        double[] pi = new double[numberOfDocs];
        int n = numberOfDocs*m;
        Random rand = new Random();
        for(int i = 0; i < n; i++) {
            // Start from random docuemnt n times
            int nextJump = rand.nextInt(numberOfDocs);
            // While until bored
            while(rand.nextDouble() > BORED) {
                // If no outgoing links, jump to random document
                if((link.get(nextJump) == null)) {
                    nextJump = rand.nextInt(numberOfDocs);
                }
                // If there exist outgoing links and not bored, pick randomly next link (neighbour)
                else {
                    nextJump = randomLinkingNode(nextJump);
                }
            }
            // For every walk end, increment the ending document with 1.0/numOfDocs*m = 1.0/n
            pi[nextJump] += 1.0/(double)n;
        }

        // Add Nodes/documents to the arrayList so we can sort it according to PR value
        ArrayList<Node> result = new ArrayList<Node>();
        for(int i = 0; i < numberOfDocs; i++) {
            result.add(new Node(docName[i], pi[i]));
        }
        // Sort the ArrayList by the PR value
        Collections.sort(result);
        // Print out top 30 documents
        /*for(int i = 0; i < 30; i++) {
            System.out.println(result.get(i).getID() + ": " + String.format(Locale.US, "%.5f", result.get(i).getPR()));
        }*/
        return result;
    }

    // Algorithm 2 MC end-point with cyclic start
    ArrayList<Node> cyclicStartMC( int numberOfDocs ) {
        double[] pi = new double[numberOfDocs];
        int n = numberOfDocs*m;
        Random rand = new Random();
        for(int i = 0; i < n; i++) {
            // Start from first docuemnt when numbOfDocs reached
            // This way it will start from every document m times
            int nextJump = i % numberOfDocs;
            // While unitl bored
            while(rand.nextDouble() > BORED) {
                // If no outgoing links, jump to random document
                if((link.get(nextJump) == null)) {
                    nextJump = rand.nextInt(numberOfDocs);
                }
                // else there exist outgoing links and not bored, pick randomly next link (neighbour)
                else {
                    nextJump = randomLinkingNode(nextJump);
                }
            }
            // For every walk end, increment the ending document with 1.0/numOfDocs*m = 1.0/n
            pi[nextJump] += 1.0/(double)n;
        }

        // Add Nodes/documents to the arrayList so we can sort it according to PR value
        ArrayList<Node> result = new ArrayList<Node>();
        for(int i = 0; i < numberOfDocs; i++) {
            result.add(new Node(docName[i], pi[i]));
        }
        // Sort the ArrayList by the PR value
        Collections.sort(result);
        return result;
        // Print out top 30 documents
        /*for(int i = 0; i < 30; i++) {
            System.out.println(result.get(i).getID() + ": " + String.format(Locale.US, "%.5f", result.get(i).getPR()));
        }*/
    }

    // Algorithm 3 MC complete path
    ArrayList<Node> completePathMC( int numberOfDocs ) {
        double[] pi = new double[numberOfDocs];
        int[] count = new int[numberOfDocs];
        int n = numberOfDocs*m;
        Random rand = new Random();
        for(int i = 0; i < n; i++) {
            // Start from first docuemnt when numbOfDocs reached
            // This way it will start from every document m times
            int nextJump = i % numberOfDocs;
            pi[nextJump]++;
            // While until bored
            while(rand.nextDouble() > BORED) {
                // If no outgoing links, jump to random document
                if((link.get(nextJump) == null)) {
                    nextJump = rand.nextInt(numberOfDocs);
                }
                // else there exist outgoing links and not bored, pick randomly next link (neighbour)
                else {
                    nextJump = randomLinkingNode(nextJump);
                }
                pi[nextJump]++;
            }
        }
        // Add Nodes/documents to the arrayList so we can sort it according to PR value
        ArrayList<Node> result = new ArrayList<Node>();
        for(int i = 0; i < numberOfDocs; i++) {
            result.add(new Node(docName[i], (pi[i]*BORED)/(double)n));
        }
        // Sort the ArrayList by the PR value
        Collections.sort(result);
        // Print out top 30 documents
        /*for(int i = 0; i < 30; i++) {
            System.out.println(result.get(i).getID() + ": " + String.format(Locale.US, "%.5f", result.get(i).getPR()));
            //System.out.println(result.get(i).getID() + ": " + result.get(i).getPR());
        }*/
        return result;
    }

    // Algorithm 4 MC complete path stopping at dangling nodes
    ArrayList<Node> cpDanglingNodesMC( int numberOfDocs ) {
        double counter = 0.0;
        double[] pi = new double[numberOfDocs];
        int n = numberOfDocs*m;
        Random rand = new Random();
        for(int i = 0; i < n; i++) {
            // Start from first docuemnt when numbOfDocs reached
            // This way it will start from every document m times
            int nextJump = i % numberOfDocs;
            while(true) {
                counter++;
                pi[nextJump]++;
                // If bored or no outgoing links, break
                if((link.get(nextJump) == null)||(rand.nextDouble() < BORED)) {
                    break;
                }
                // else there exist outgoing links and not bored, pick randomly next link (neighbour)
                else {
                    nextJump = randomLinkingNode(nextJump);
                }
            }
        }
        // Add Nodes/documents to the arrayList so we can sort it according to PR value
        ArrayList<Node> result = new ArrayList<Node>();
        for(int i = 0; i < numberOfDocs; i++) {
            result.add(new Node(docName[i], pi[i]/(double)counter));
        }
        // Sort the ArrayList by the PR value
        Collections.sort(result);
        // Print out top 30 documents
        /*for(int i = 0; i < 30; i++) {
            System.out.println(result.get(i).getID() + ": " + String.format(Locale.US, "%.5f", result.get(i).getPR()));
        }*/
        return result;
    }

    // Algorithm 5 MC complete path with random start
    ArrayList<Node> cpRandomStartMC( int numberOfDocs ) {
        double counter = 0.0;
        double[] pi = new double[numberOfDocs];
        int n = numberOfDocs*m;
        Random rand = new Random();
        for(int i = 0; i < n; i++) {
            // Start from random docuemnt n times
            int nextJump = rand.nextInt(numberOfDocs);
            while(true) {
                counter++;
                pi[nextJump]++;
                // If bored or no outgoing links, break
                if((link.get(nextJump) == null) ||(rand.nextDouble() < BORED)) {
                    break;
                }
                // else there exist outgoing links and not bored, pick randomly next link (neighbour)
                else {
                    nextJump = randomLinkingNode(nextJump);
                }
            }
        }
        // Add documents to the arrayList so we can sort it according to PR value
        ArrayList<Node> result = new ArrayList<Node>();
        for(int i = 0; i < numberOfDocs; i++) {
            result.add(new Node(docName[i], pi[i]/(double)counter));
        }
        // Sort the ArrayList by the PR value
        Collections.sort(result);
        // Print out top 30 documents
        /*for(int i = 0; i < 30; i++) {
            System.out.println(result.get(i).getID() + ": " + String.format(Locale.US, "%.5f", result.get(i).getPR()));
        }*/
        return result;
    }

    /* --------------------------------------------- */


    public static void main( String[] args ) {
	if ( args.length != 1 ) {
	    System.err.println( "Please give the name of the link file" );
	}
	else {
	    new PageRank( args[0] );
	}
    }
}
