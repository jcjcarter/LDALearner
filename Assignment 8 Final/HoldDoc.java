import java.util.*;
// this silly little class is just a wrapper for a DocSignature name, DocSignature pair 
class HoldDoc{
  
  String docName;
  DocSignature docSig;
  IDoubleMatrix topicProbsMatrix;
  public HoldDoc(String docNameIn, DocSignature docSigIn, IDoubleMatrix topicProbsMatrixIn){
    
    docName = docNameIn;
    docSig = docSigIn;
    topicProbsMatrix = topicProbsMatrixIn;
  }
  
  public DocSignature getDocSig(){
    return docSig;
  }
  
  public String getDocName(){
    return docName;
  }
  
  public IDoubleMatrix getTopicProbsMatrix(){
  return topicProbsMatrix;
  }
}