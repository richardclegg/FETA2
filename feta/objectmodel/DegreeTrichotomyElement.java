package feta.objectmodel;

import java.util.*;
import javax.xml.parsers.*;
import org.xml.sax.*;
import org.w3c.dom.*;
import rgc.xmlparse.*;
import feta.*;

//Class for an piecewise linear inner model element
public class DegreeTrichotomyElement extends ObjectModelElement {

    private int lowerDegree = 10; //Set the lower degree level
    private int upperDegree = 100; //Set the upper degree level

public DegreeTrichotomyElement()
{
}

public boolean useInDegree(){ return true; }

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
            System.err.println("Tracking must be on in net for normalisation");
            System.err.println("Exiting DegreePowModelElement.calcNormalisation");
            System.exit(-1);
        }
        int degSum= 0;
        for (int i= 1; i <= net.maxInDegree_; i++) {
            degSum+= i*net.inDegreeDistrib_[i];
            int d= net.inDegreeDistrib_[i];
            if (d > 0) {
                total+= d*getDegreeTrichotomy(i);
            }
        }
        // System.err.println("DegSum "+degSum+" no links "+net.noLinks_);
        if (total == 0) {
            normalise_= 0.0;
        } else {
            normalise_=1.0/total;
        }
    }

public int getDegreeTrichotomy(int degree){
    int degTrichotomy;
    if(degree<lowerDegree){
        degTrichotomy = lowerDegree;
    }
    else if(lowerDegree <= degree && degree <= upperDegree){
        degTrichotomy = degree;}
        else{degTrichotomy = upperDegree;}
        return degTrichotomy;
    }
/** Calculate the normalisation constant */
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
                total+= d*getDegreeTrichotomy(i);
            }
        }
        for (int i= 0; i < from.length; i++) {
            total-= getDegreeTrichotomy(net.inLinks_.get(from[i]).length);
        }
        // System.err.println("DegSum "+degSum+" no links "+net.noLinks_);
        if (total == 0) {
            normalise_= 0.0;
        } else {
            normalise_=1.0/total;
        }
    }

    public double calcProbability(int nodeNo, Network net)
    {
        if (normalise_ == 0.0) {
            System.out.println("Random");
            return 1.0/net.noNodes_;
        }
        int degree= net.inLinks_.get(nodeNo).length;
        return getDegreeTrichotomy(degree)*normalise_;
    }

    public void parseXML(Node node) throws SAXException
    {
        try {
            lowerDegree= ReadXMLUtils.parseSingleInt(node, "LowerDegree",
                    "Element", false);
            ReadXMLUtils.removeNode(node,"LowerDegree","Element");
        } catch (SAXException e) {
            throw e;
        } catch (XMLNoTagException e) {
        }
        try {
            upperDegree= ReadXMLUtils.parseSingleInt(node, "UpperDegree",
                    "Element", false);
            ReadXMLUtils.removeNode(node,"UpperDegree","Element");
        } catch (SAXException e) {
            throw e;
        } catch (XMLNoTagException e) {
        }
    }

}


