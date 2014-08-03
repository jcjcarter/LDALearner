//class LDALearner extends SCLDALearner {
//  public LDALearner(IDoubleVector a, IDoubleVector b, IPRNG r) {
//    super(a,b,r);
//  }
//}

import java.util.*;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.*;

public class LDALearner implements ILDALearner{
  //alpha and beta setters for constructor
  private IDoubleVector alpha, beta;
  //prng setter for constructor
  private IPRNG prng;
  private Iterator<DocSignature> temp2;
  //holds the vectors for word probability counters
  private ArrayList<IDoubleVector> holdWordProbCounters = new ArrayList<IDoubleVector>(); 
  //holds the word probability vectors in matrix form
  private IDoubleMatrix wordProbsMatrix;
  //for the method updateProduced(), remembers the document position that was retrived
  private int rememberDocPosition, numberOfCols, numberOfRows;
  //matrix that will contain all the computed Word Probs vector
  private IDoubleMatrix computedWordProbs, wordProbsGuess;
  //DocSignature variable is used in the method updateProduce(docName)
  private DocSignature testDoc, temp1;
  //DocSinature variable is used in method updateTopicProbs
  private DocSignature findNewDoc;
  //LDA Iterator type object
  private LDA_Iterator toggle;
  //for the Iterator input constructor
  private ArrayList<DocSignature> inDocs = new ArrayList<DocSignature>();
  private int docPosition;
  private DocSignature newDocSig, oldDocSig;
  private HoldDoc document;
  //create a treeMap
  private TreeMap treeDocs = new TreeMap();
  //string arraylist for docNames
  private ArrayList<String> some;
  //iterator method
  private ArrayList<HoldDoc> valuesOfKeysDoc;
  //iterator with doc signs
  private ArrayList<DocSignature> docSignFromList;
  
  public  LDALearner(IDoubleVector alphaIN, IDoubleVector betaIN, IPRNG objectIN){
    
    alpha = alphaIN;//the word dictionary that is passed in and has a probability for the word
    beta = betaIN;//the topic probs
    prng = objectIN;
    //number of columns for produced matrix
    numberOfCols = alpha.getLength();
    //number of rows for prouded matrix
    numberOfRows = beta.getLength();
    
  }
  
  // Using the specified file, this loads the words_in_doc and topic_probs vectors for 
  // every document, as well as the word_probs vector for every topic.  All of the data
  // in the LDA object before loadFromFile is called should be deleted.  Note that after
  // calling "loadFromFile", the produced matrix for each document does not exist
  public void loadFromFile (String loadFromMe){
    //IXMLParserForLDA thisParser = new SCXMLParserForLDA(loadFromMe);
//    while(thisParser.hasNext){
//    }
    
  }
  
  // this loads a document into the machine learning algorithm... "topicProbs" is the initial
  // version of the topic_probs vector for that document, "wordsInDoc" is the words_in_doc 
  // vector for that document.  Often, topicProbs will be nothing more than a sample from
  // a Dirichlet distribution where the parameter is a vector of ones; this provides a random 
  // initialization
  public void loadUpAnotherDoc (String docName, IDoubleVector wordsInDoc, IDoubleVector topicProbs){ 
    
    //Set up a new document with topic probability and words in the document
    newDocSig = new DocSignature ( docName, topicProbs, wordsInDoc);
    //Add the DocSignature to the data wrapper so that it can be added all the other documents
    document = new HoldDoc (docName, newDocSig, null);
    //add document to the tree map
    treeDocs.put(docName, document);
    
  }
  
  // this loads all of the current topics into the machine learning algorithm... the t^th row
  // of the matrix "loadMe" is the word_probs vector associated with the t^th topic.  Often,
  // wordProbs will initilly be nothing more than a random guess at the true wordProbs matrix,
  // with each row sampled from a Dirichlet () distribution where the parameter is a vector of
  // ones; this provides a random initialization
  public void loadUpWordProbs (IDoubleMatrix loadMe){
    
    //This is a setter for the random guess at the true wordProbs matrix
    computedWordProbs = loadMe; //wordProbsGuess = loadMe;
  }
  
  /**
   * THESE METHODS ARE USED TO UPDATE THE DATA AS REQUIRED BY THE LDA ALGORITHM... SEE
   * SLIDES 46 THROUGH 53 OF THE LECTURE 28 SLIDES
   */
  
  // update/create the produced matrix for a particular document... if the doc does not exist
  // or the word_probs matrix does not exist (it has not been loaded via "loadUpWordProbs" or
  // via "readFromFile") than an exception is thrown
  public void updateProduced (String docName){
    
    try{
      //get the value from the tree
      HoldDoc docHolded2 = (HoldDoc) treeDocs.get(docName);
      //get the docSignature
      DocSignature docSign2 = docHolded2.getDocSig();
      //number of docsignatures in the document holder
      int docTreeSize = treeDocs.size();
      //holds the topic probability vector
      IDoubleVector testTopicProbs = docSign2.getTopicProbs();//yes<<<<<<<<<<
      // create a new produced to update the produced matrix
      IDoubleMatrix newproduced = new ColumnMajorDoubleMatrix(numberOfRows, numberOfCols, 0.0);//yes
      //loop through all the columns in the produced matrix
      for (int col = 0; col < numberOfCols; col++){
        //create a vector probs with the length of the topic probs vectors in the DocSignatures
        IDoubleVector probs = new SparseDoubleVector(numberOfRows, 0.0);//yes
        //loop through all the words in the column wth column
        for (int word = 0; word < numberOfRows; word++){
          double a = computedWordProbs.getEntry(word, col);//wordProbsGuess.getEntry(word, col);//needs to change
          double b = testTopicProbs.getItem(word);//located up top ^^^^^^^
          probs.setItem(word, a*b);//yes
        }
        //normalize probs
        probs.normalize();//yes
        //multinomial operations and wth column in produced[d]
        IRandomGenerationAlgorithm<IDoubleVector> multi = new Multinomial(prng, new MultinomialParam((int)docSign2.getWordsInDoc().getItem(col), probs));
        //Set the column for the matrix here
        newproduced.setColumn(col, multi.getNext());//yes
      }
      //add the final product of the newproduced matrix into the list with the other matrices
      String rememberDocName = docName; //documentHolder.get(rememberDocPosition).getDocName();
      //create a new wrapper that has the updated produce or topic probs matrix
      HoldDoc docSigTopicProbChange = new HoldDoc(docName, docSign2, newproduced);
      //remove the old value if needed?
      treeDocs.remove(docName);
      //update the topic probs matrix for the docsignature in tree
      treeDocs.put(docName, docSigTopicProbChange);
      //System.out.println("_____________here:_____________");
    }catch(OutOfBoundsException E){
      System.out.println("_____________Error in method updatedProduced with string input____________");
    }
  }
  
  // update/create the produced matrix for ALL of the documents... just as above, if the 
  // word_probs matrix does not exist, then an exception is thrown 
  public void updateProduced (){
    //all the docsignature in the tree
    Set<String> allKeys = treeDocs.keySet();
    //iterator over the docsignatues' docnames in the set
    Iterator<String> boo = allKeys.iterator();
    //arraylist containing the docsignatures' docnames as strings
    some = new ArrayList<String>();
    //add all the docnames to the arraylist
    while(boo.hasNext()){
      some.add(boo.next());
    }
    //feed the docs names to the method updateproduced
    for(int ii = 0; ii < some.size(); ii++){
      updateProduced(some.get(ii));
    }
  }
  
  // this updates the topic_probs vector for a particular document... if the doc does not exist,
  // or the produced matrix for that doc does not exist (because "updateProduced" has not been
  // called on this document since it was added or loaded) then an exception is thrown
  public void updateTopicProbs (String docName){
    try{
      
      //get the value from the tree
      HoldDoc docHolded2 = (HoldDoc) treeDocs.get(docName);
      //get the docSignature
      DocSignature docSign2 = docHolded2.getDocSig();
      
      //obtain the produced matrix from the document
      IDoubleMatrix holdProducedMatrix = docHolded2.getTopicProbsMatrix(); //documentHolder.get(docPosition).getTopicProbsMatrix();
      
      IDoubleVector counter = holdProducedMatrix.sumColumns();
      
      //add beta vector to counter vector
      beta.addMyselfToHim(counter);
      //topic_probs ~ Dirchlet(counter + beta)
      IRandomGenerationAlgorithm<IDoubleVector> dirich = new Dirichlet(prng, new DirichletParam(counter, 10e-40, 150));
      //grap the words in doc
      IDoubleVector holdWordsInDoc = docHolded2.getDocSig().getWordsInDoc();
      //grap the produced matrix (Topic Probs Matrix)
      IDoubleMatrix holdProducedMatrixHere = docHolded2.getTopicProbsMatrix(); //documentHolder.get(docPosition).getTopicProbsMatrix();
      //setup a new document with the new topic probability
      DocSignature newDocSig = new DocSignature (docName, dirich.getNext(), holdWordsInDoc);
      //create new wrapper
      HoldDoc newWrap = new HoldDoc(docName, newDocSig, holdProducedMatrixHere);
      //remove the old value if needed?
      treeDocs.remove(docName);
      //update the topic probs matrix for the docsignature in tree
      treeDocs.put(docName, newWrap);
    }catch (OutOfBoundsException E){
      System.out.println("_______________eerrr updateTopicProbs(one parameter) gone array!!!_______________");
    }
  }
  
  // this updates the topic_probs vector for ALL of the documents... just as above, if the 
  // produced matrix does not exist for ANY of the documents in the corpus, then an exception 
  // is thrown   
  public void updateTopicProbs (){
    
    //all the docsignature in the tree
    Set<String> allKeys = treeDocs.keySet();
    //iterator over the docsignatues' docnames in the set
    Iterator<String> boo = allKeys.iterator();
    //arraylist containing the docsignatures' docnames as strings
    some = new ArrayList<String>();
    //add all the docnames to the arraylist
    while(boo.hasNext()){
      some.add(boo.next());
    }
    //feed the docs names to the method updateproduced
    for(int ii = 0; ii < some.size(); ii++){
      updateTopicProbs(some.get(ii));
    }
  }
  
  // this updates the word_probs vector for all of the topics... if the produced matrix does 
  // not exist for ANY of the documents in the corpus, then this will fail
  public void updateWordProbs (){
    try{
      
      //number of documents in documentHolder
      int numDocsHere = treeDocs.size();
      //get any key from the tree
      String thisKeyHere = (String) treeDocs.firstKey();
      //look at this doc for parameters
      HoldDoc checkDocHere = (HoldDoc) treeDocs.get(thisKeyHere);
      //number of topics in document
      int numTopicsHere = checkDocHere.getTopicProbsMatrix().getNumRows(); 
      //setup the parameters for the computed word probability
      computedWordProbs = new RowMajorDoubleMatrix(numTopicsHere, checkDocHere.getTopicProbsMatrix().getNumColumns(),0.0);
      //go through all the topics in the documents
      for (int topic = 0; topic < numTopicsHere; topic++){
        //create a vector called counter
        IDoubleVector counter = new SparseDoubleVector(computedWordProbs.getNumColumns(), 0.0);        
        //all the docsignature in the tree
        Collection allValues = treeDocs.values();
        //iterator over the docsignatues' docnames in the set
        Iterator<HoldDoc> hoo = allValues.iterator();
        //allDocSigsThere = new Set<DocSignature>();
        ////////////valuesOfKeysDoc = new ArrayList<HoldDoc>();
        //add all the docnames to the arraylist
        while(hoo.hasNext()){
          HoldDoc docVectorHold = hoo.next();
          IDoubleVector vectorTopic = docVectorHold.getTopicProbsMatrix().getRow(topic);
          
          //add the topic vector to the counter
          vectorTopic.addMyselfToHim(counter);
        }
        
        
        //add alpha to the counter
        alpha.addMyselfToHim(counter);
        //topic_probs ~ Dirichlet(counter + alpha)
        IRandomGenerationAlgorithm<IDoubleVector> dirich = new Dirichlet(prng, new DirichletParam(counter, 10e-40, 1500));
        //computed word probability that is not a guess
        computedWordProbs.setRow(topic, dirich.getNext());
      }
    }catch(OutOfBoundsException E){
      System.out.println("______________________The words no update in method updateWordProbs______________");
    }
  }
  
  /**
   * THESE METHODS ARE USED TO EXTRACT DATA FROM THE LDA ALGORITHM
   */
  
  // gets the current topic_probs vector and the words_in_doc vector for a particular document... 
  // if the document does not exist, then an exception is thrown
  public DocSignature getSignature (String docName){
    
    HoldDoc docHold33 = (HoldDoc) treeDocs.get(docName);
    DocSignature thisDoc99 = docHold33.getDocSig();
    return thisDoc99;
  }
  
  // gets an iterator that returns the DocSignature for all of the documents
  public Iterator <DocSignature> iterator (){
    //all the docsignature in the tree
    Collection allValues = treeDocs.values();
    //iterator over the docsignatues' docnames in the set
    Iterator<HoldDoc> hoo = allValues.iterator();
    //allDocSigsThere = new Set<DocSignature>();
    valuesOfKeysDoc = new ArrayList<HoldDoc>();
    //add all the docnames to the arraylist
    while(hoo.hasNext()){
      valuesOfKeysDoc.add(hoo.next());
    }
    //create a new arrayList that contains the DocSigs
    docSignFromList = new ArrayList<DocSignature>();
    //number of docs
    int newNumberDocs = valuesOfKeysDoc.size();
    //loop through the arrayList that has the HoldDocs and extract the DocSignatures
    for (int j = 0; j < newNumberDocs; j++){
      docSignFromList.add(valuesOfKeysDoc.get(j).getDocSig());
    }
    //Add DocSignature to LDA iterator
    toggle = new LDA_Iterator(docSignFromList);
    return toggle;
  }
  
  // gets all of the word_probs vectors from the ML algorithm... the t^th row in the matrix 
  // corresponds to the word_probs vector for the t^th topic
  public IDoubleMatrix getWordProbs (){//this may have to come from the XML file
    return computedWordProbs;
  }
  
  // this writes the data maintained by the ML algorithm to a file (specifically, it writes all
  // of the topic_probs vectors, all of the words_in_doc vetors, and all of the word_probs vectors).
  // If one calls "writeToFile", then creates a new LDA object and calls "readFromFile" using that
  // same file, the new LDA object will have exactly the same contents as the old one, except that
  // all of the produced matrices will be lost, because they are not written
  public void writeToFile (String writeToMe){
    try{
      int countDocs = treeDocs.size();
      PrintWriter writer = new PrintWriter(writeToMe, "UTF-8");
      writer.println("<?xml version=\"1.0\" encoding=\"ISO-8859-1\"?>");
      writer.println("");
      writer.println("<LDA_model>");
      writer.println("<word_probs>");
      writer.println("<num_topics>" + numberOfRows +"</num_topics>");
      int ithTopic = 0;
      while(ithTopic < numberOfRows){
        writer.println("<topic><topic_num>" + ithTopic + "</topic_num>");
        writer.println("<dense_vector>");
        writer.println("<length>"+ numberOfCols + "</length>");
        int ithWord1 = 0;
        while(ithWord1 < numberOfCols){
          HoldDoc anotherDoc =(HoldDoc) treeDocs.get(Integer.toString(ithWord1));
          DocSignature someDocSign1 = anotherDoc.getDocSig();
          IDoubleVector someTopicVec1 = someDocSign1.getTopicProbs();
          try{
          writer.println("<entry><dim>" + ithWord1 + "</dim><val>" + someTopicVec1.getItem(ithWord1) +"</val></entry>");
          }catch(OutOfBoundsException E){
          System.out.println("proble at line 342___________");
          }
          ithWord1++;
        }
        writer.println("</dense_vector>");
        writer.println("</topic>");
        ithTopic++;
      }
      writer.println("</word_probs>");
      writer.println("<corpus>");
      writer.println("<num_docs>"+ countDocs +"</num_docs>");
      int itDoc1 = 0;
      while(itDoc1 < countDocs){
        writer.println("<doc_in_corpus>");
        writer.println("<doc_name>"+itDoc1 + "</doc_name>");
        writer.println("<words_in_doc>");
        
        writer.println("<sparse_int_vector>");
        writer.println("<length>"+ numberOfRows +"</length>");
        writer.println("<non_zero_cnt>"+numberOfRows+"</non_zero_cnt>");
        HoldDoc anotherDoc =(HoldDoc) treeDocs.get(Integer.toString(itDoc1));
        DocSignature someDocSign1 = anotherDoc.getDocSig();
        IDoubleVector someWordVec1 = someDocSign1.getWordsInDoc();
        for(int i = 0; i < someWordVec1.getLength(); i++){
          try{
          writer.println("<entry><dim>"+ i +"</dim><val>"+(int)someWordVec1.getItem(i)+"</val></entry>");//line 36??
          }catch(OutOfBoundsException E){
          System.out.println("369_____________");
          }
        }        
        writer.println("</sparse_int_vector>");
        writer.println("</words_in_doc>");
        writer.println("<topic_probs>");
        writer.println("</dense_vector>");
        IDoubleVector finalVeco22 = someDocSign1.getTopicProbs();
        writer.println("<length>"+finalVeco22.getLength()+"</length>");
        for(int ij = 0; ij < finalVeco22.getLength(); ij++){
          try{
          writer.println("<entry><dim>"+ij+"</dim><val>"+ finalVeco22.getItem(ij)+"</val></entry>"); //some loop here
          }catch(OutOfBoundsException E){
          System.out.println("382___________");
          }
        }
        writer.println("<dense_vector>");
        writer.println("<topic_probs>");
        writer.println("<doc_in_corpus>");
        itDoc1++;
      }
      
      writer.println("</corpus>");
      writer.println("<LDA_model>");
      writer.close();
    }catch(IOException i){
      i.printStackTrace ();
    } 
  }
}//end of file

