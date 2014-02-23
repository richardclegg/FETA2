package feta.objectmodel;

import javax.xml.parsers.*;
import org.xml.sax.*;
import org.w3c.dom.*;
import rgc.xmlparse.*;
import feta.*;

// Class for inner model element which is simply random
public class RandomModelElement extends ObjectModelElement {
    
    public RandomModelElement()
    {
    }
    
    /**Calculate the normalisation constant */
    public void calcNormalisation(Network net) {
        normalise_=1.0/net.noNodes_;
    }
        /**Calculate the normalisation constant */
    public void calcNormalisationFrom(Network net, int []from) {
        
        if (net.noNodes_ <= from.length)
            normalise_ = 0.0;
        else {
            normalise_=1.0/(net.noNodes_-from.length);
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
        return normalise_;
    }
}
