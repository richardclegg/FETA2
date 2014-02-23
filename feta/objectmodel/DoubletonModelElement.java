package feta.objectmodel;

import javax.xml.parsers.*;
import org.xml.sax.*;
import org.w3c.dom.*;
import rgc.xmlparse.*;
import feta.*;

// Class for an inner model element proportional to node degree
public class DoubletonModelElement extends ObjectModelElement {
    
    
    public DoubletonModelElement()
    {
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
        int total= 0;
        if (net.tns_ == null) {
            System.err.println("Tracking must be on in net for normalistion");
            System.err.println("Exiting DegreePowModelElement.calcNormalisation");
            System.exit(-1);
        }
        total= net.inDegreeDistrib_[2];
        if (total == 0) {
            normalise_= 0.0;
        } else {
            normalise_=1.0/(double)total;
        }
    }
    
        /**Calculate the normalisation constant */
    public void calcNormalisationFrom(Network net, int [] from) {
        int total= 0;
        if (net.tns_ == null) {
            System.err.println("Tracking must be on in net for normalistion");
            System.err.println("Exiting DegreePowModelElement.calcNormalisation");
            System.exit(-1);
        }
        total= net.inDegreeDistrib_[2];
        for (int i= 0; i < from.length; i++) {
            if (net.inLinks_.get(from[i]).length == 2) {
                total--;
            }
        }
        if (total == 0) {
            normalise_= 0.0;
        } else {
            normalise_=1.0/(double)total;
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
        // If there are no degree 1 nodes then treat all equally
        if (normalise_ == 0.0) {
            return 1.0/net.noNodes_;
        } else {
            if (net.inLinks_.get(nodeNo).length == 2) {
                return 1.0*normalise_;
            } else {
                return 0.0;
            }
        }
    }
}
