package feta.objectmodel;

import javax.xml.parsers.*;
import org.xml.sax.*;
import org.w3c.dom.*;
import rgc.xmlparse.*;
import feta.*;

// Class for an inner model element proportional to node degree
public class DegreeModelElement extends ObjectModelElement {
    
    public DegreeModelElement()
    {
    }
    
    public boolean useInDegree()
    {
        return true;
    }
    
    /** Get items required for tracking*/
    
    public int [] getTrackingRequirements()
    {
        int []i= new int[1];
        i[0]= ObjectModelElement.TRACK_DEGREE_DIST;
        return i;
    }
    
    /**Calculate the normalisation constant */
    public void calcNormalisation(Network net) {

        normalise_=1.0/net.noLinks_;
    }
    
        /**Calculate the normalisation constant */
    public void calcNormalisationFrom(Network net, int [] from) {
        int noLinks= net.noLinks_;
        for (int i= 0; i < from.length; i++) {
            noLinks-= net.inLinks_.get(from[i]).length;
        }
        if (noLinks <= 0) {
            System.err.println("No links");
            normalise_= 0.0;
        } else {
            normalise_=1.0/noLinks;
        }
    }
    
    
    public void setNormalisation(double nc) {
        normalise_= nc;
    }
    
    public void parseXML(Node node) throws SAXException
    {
    }
    
    public double calcProbability(int nodeNo, Network net)
    {
        int degree= net.inLinks_.get(nodeNo).length;
        return (double)degree*normalise_;
    }
}
