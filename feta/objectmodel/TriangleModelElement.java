package feta.objectmodel;

import javax.xml.parsers.*;
import org.xml.sax.*;
import org.w3c.dom.*;
import rgc.xmlparse.*;
import feta.*;

// Class for an inner model element proportional to node degree
public class TriangleModelElement extends ObjectModelElement {
    
    
    public TriangleModelElement()
    {
    }
    
    
    /**Calculate the normalisation constant */
    public void calcNormalisation(Network net) {
        if (net.totTri_ == 0)
            normalise_ = 0.0;
        else {
            normalise_=1.0/(double)net.totTri_;
        }
    }
    
    
    /**Calculate the normalisation constant */
    public void calcNormalisationFrom(Network net, int[] from) {
        int totTri= net.totTri_;
        for (int i= 0; i < from.length; i++) {
            totTri-= net.triCount_.get(from[i])*3;
        }
        if (totTri == 0)
            normalise_ = 0.0;
        else {
            normalise_=1.0/totTri;
        }
    }
    
        /** Get items required for tracking*/
    
    public int [] getTrackingRequirements()
    {
        int []i= new int[1];
        i[0]= ObjectModelElement.TRACK_TRIANGLES;
        return i;
    }
    
    public void setNormalisation(double nc) {
        normalise_= nc;
    }
    
    public void parseXML(Node node) throws SAXException
    {

    }
    
    public double calcProbability(int nodeNo, Network net)
    {
        // If there are no triangles then treat all equally
        if (normalise_ == 0) {
            return 1.0/net.noNodes_;
        } else {
            return normalise_*net.triCount_.get(nodeNo);
        }
    }
    
    public boolean useTri()
    {
        return true;
    }
}
