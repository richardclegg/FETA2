package feta.operationmodel;

import feta.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import org.xml.sax.*;
import rgc.xmlparse.*;

/** Abstract class for the operation model which 
 * controls which operations manipulate the network
 */
public abstract class OperationModel {
    
    public int maxLinks_=Integer.MAX_VALUE;
    public int maxNodes_=Integer.MAX_VALUE;
    public long time_=0;
    public long interval_=1;
    
    public OperationModel() 
    {
    }
    
    public boolean providesStop() {
        return false;
    }
    
    /** Set initial bits for Operation model*/
    public void initialise(long start, long interval)
    {   
        time_= start;
        interval_= interval;
    }
   
    /** Calculate the probability of a given FetaElement operation*/ 
    abstract public double calcProb(Network net, FetaElement fe);
    
    /** Generate a potential FetaElement using this Operation model
     * The contents of the objectnodes will be dummy */
    abstract public FetaElement nextElement(Network net);
    
    
    /** Parse passed in XML */
    public void parseXML(Node node) throws SAXException {
        
    }
    
    
    
}
