package feta.objectmodel;

import java.util.*;
import javax.xml.parsers.*;
import org.xml.sax.*;
import org.w3c.dom.*;
import rgc.xmlparse.*;
import feta.*;

// Class for an inner model element proportional to node degree
public class PFPModelElement extends ObjectModelElement {
    
    private double d_= 2.0;

    
    public PFPModelElement()
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
        double total= 0.0;
        if (net.tns_ == null) {
            System.err.println("Tracking must be on in net for normalistion");
            System.err.println("Exiting DegreePowModelElement.calcNormalisation");
            System.exit(-1);
        }
        int degSum= 0;
        for (int i= 1; i <= net.maxInDegree_; i++) {
            degSum+= i*net.inDegreeDistrib_[i];
            int d= net.inDegreeDistrib_[i];
            if (d > 0) {
                total+= d*pfpfact(i);
            }
        }
       // System.err.println("DegSum "+degSum+" no links "+net.noLinks_);
       if (total == 0) {
           normalise_= 0.0;
       } else {
            normalise_=1.0/total;
        }
    }
    
    public double pfpfact(int degree)
    {
        return Math.pow(degree,(1.0+d_*(Math.log10(degree))));
    }
    
        /**Calculate the normalisation constant */
    public void calcNormalisationFrom(Network net, int [] from) {
        double total= 0.0;
        if (net.tns_ == null) {
            System.err.println("Tracking must be on in net for normalistion");
            System.err.println("Exiting DegreePowModelElement.calcNormalisation");
            System.exit(-1);
        }
        int degSum= 0;
        for (int i= 1; i <= net.maxInDegree_; i++) {
            degSum+= i*net.inDegreeDistrib_[i];
            int d= net.inDegreeDistrib_[i];
            if (d > 0) {
                total+= d*pfpfact(i);
            }
        }
        for (int i= 0; i < from.length; i++) {
            total-= pfpfact(net.inLinks_.get(from[i]).length);
        }
       // System.err.println("DegSum "+degSum+" no links "+net.noLinks_);
       if (total == 0) {
           normalise_= 0.0;
       } else {
            normalise_=1.0/total;
        }
    }
    
    public void setNormalisation(double nc) {
        normalise_= nc;
    }
    
    public void parseXML(Node node) throws SAXException
    {
        try {
            d_= ReadXMLUtils.parseSingleDouble(node, "d", 
                "Element", false);
            ReadXMLUtils.removeNode(node,"d","Element");
        } catch (SAXException e) {
            throw e;
        } catch (XMLNoTagException e) {
        }
    }
    
    public double calcProbability(int nodeNo, Network net)
    {
        if (normalise_ == 0.0) {
            System.out.println("Random");
            return 1.0/net.noNodes_;
        }
        int degree= net.inLinks_.get(nodeNo).length;
        return pfpfact(degree)*normalise_;
    }
}
