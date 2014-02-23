package feta.objectmodel;

import javax.xml.parsers.*;
import org.xml.sax.*;
import org.w3c.dom.*;
import rgc.xmlparse.*;
import feta.*;

// Class for an inner model element proportional to node degree
public class HotModelElement extends ObjectModelElement {
    
    public int hotLength_= 10;
    private double random_= 1.0;

    public HotModelElement()
    {
    }
        
    /** Get items required for tracking*/
    
    public int [] getTrackingRequirements()
    {
        int []i= new int[0];
        return i;
    }
    
    /**Calculate the normalisation constant */
    public void calcNormalisation(Network net) {
        int noLinks= 0;
        for (int i= 1; i <= hotLength_; i++) {
            int p= (net.maxHotNodes_+net.hotPos_-i)%net.maxHotNodes_;
            if (net.hotNodes_[p] >= 0) {
                noLinks++;
            }
        }
        if (noLinks == 0) {
            normalise_= 0.0;
            random_= 1.0/net.noNodes_;
        } else {
            normalise_=1.0/noLinks;
        }
    }
    
        /**Calculate the normalisation constant */
    public void calcNormalisationFrom(Network net, int [] from) {
        int noLinks= 0;
        for (int i= 1; i <= hotLength_; i++) {
            int p= (net.maxHotNodes_+net.hotPos_-i)%net.maxHotNodes_;
            if (net.hotNodes_[p] >= 0) {
                noLinks++;
            }
        }
        for (int i= 0; i < from.length; i++) {
            int recent= net.getRecent(from[i]);
            if (recent > 0 && recent <=hotLength_)
                noLinks--;
        }
        
        
        if (noLinks == 0) {
            normalise_= 0.0;
            random_= 1.0/(net.noNodes_ - from.length);
        } else {
            normalise_=1.0/noLinks;
        }
    }
    
    public int useRecent() {
        return hotLength_;
    }
    
    public void setNormalisation(double nc) {
        normalise_= nc;
    }
    
    public void parseXML(Node node) throws SAXException
    {
        try {
            hotLength_= ReadXMLUtils.parseSingleInt(node, "HotLength",
                "Element", true);
            ReadXMLUtils.removeNode(node,"HotLength","Element");
        } catch (SAXException e) {
            throw new SAXException("Cannot construct Integer from HotLength tag");
        } catch (XMLNoTagException e) {
        }
    }
    
    public double calcProbability(int nodeNo, Network net)
    {
        if (normalise_ == 0.0) {
            return random_;
        }
        int recent=net.getRecent(nodeNo);
        if (recent > 0 && recent <= hotLength_) {
            return 1.0*normalise_;
        } else {
            return 0.0;
        }
    }
}
