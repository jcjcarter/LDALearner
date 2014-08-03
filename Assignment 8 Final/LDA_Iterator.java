import java.util.*;

class LDA_Iterator implements Iterator<DocSignature>{

  ArrayList<DocSignature> docsLDA;
  Iterator<DocSignature> iter;
  
  public LDA_Iterator(ArrayList<DocSignature> docsLDAIn){
    docsLDA = docsLDAIn;
    iter = docsLDA.iterator();
  }

  public void remove(){
    return;
  }
  
  public boolean hasNext(){
    return  iter.hasNext();
  }
  public DocSignature next(){
    DocSignature tempDocPrint = iter.next();
    return tempDocPrint;
  }
  
}