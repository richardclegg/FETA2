package feta.objectmodel;

import java.util.*;
import javax.xml.parsers.*;
import org.xml.sax.*;
import org.w3c.dom.*;
import rgc.xmlparse.*;
import feta.*;

// Class for an inner model element proportional to node degree
public class DegreePowModelElement extends ObjectModelElement {
    
    private double power_= 2.0;

    
    public DegreePowModelElement()
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
                total+= d*Math.pow(i,power_);
            }
        }
       // System.err.println("DegSum "+degSum+" no links "+net.noLinks_);
       if (total == 0) {
           normalise_= 0.0;
       } else {
            normalise_=1.0/total;
        }
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
                total+= d*Math.pow(i,power_);
            }
        }
        for (int i= 0; i < from.length; i++) {
            total-= Math.pow(net.inLinks_.get(from[i]).length,power_);
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
            power_= ReadXMLUtils.parseSingleDouble(node, "Power", 
                "Element", false);
            ReadXMLUtils.removeNode(node,"Power","Element");
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
        return Math.pow(degree,power_)*normalise_;
    }
}
