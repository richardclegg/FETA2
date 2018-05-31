package feta.objectmodel;

import java.util.*;
import javax.xml.parsers.*;
import org.xml.sax.*;
import org.w3c.dom.*;
import rgc.xmlparse.*;
import feta.*;


/** Integral approximation to rank preference model */

public class RankApproxElement extends ObjectModelElement {

    private double power = 2.0;

    public RankApproxElement()
    {
    }

    public boolean useInDegree() {
        return false;
    }

    public boolean useRank() {
        return false;
    }

    public int [] getTrackingRequirements()
    {
        int []i= new int[0];

        return i;
    }

    public void calcNormalisation(Network net) {
        double integral;
        double N = net.noNodes_;
        if(power == 1.0) {
            integral = Math.log(N+1);
        } else {
            integral = (1/(1- power))*(Math.pow(N+1, 1-power) - 1);
        }

        if (integral == 0.0) {
            normalise_ = 0.0;
        } else {
            normalise_ = 1.0/integral;
        }
    }

    public void calcNormalisationFrom(Network net, int [] from) {
        double integral;
        double N = net.noNodes_;
        if (power == 1.0) {
            integral = Math.log(N+1);
        } else {
            integral = (1/(1- power))*(Math.pow(N+1, 1-power) - 1);
        }
        if (power == 1.0) {
            for (int j = 0; j < from.length; j++) {
                integral -= Math.log((from[j] + 2) /(from[j] + 1));
            }
        } else {
            for (int j = 0; j < from.length; j++) {
                integral -= (1/(1 - power))*(Math.pow(from[j]+2, 1 - power) - Math.pow(from[j]+1, 1 - power));
            }
        }

        if (integral == 0.0) {
            normalise_ = 0.0;
        } else {
            normalise_ = 1.0/integral;
        }
    }

    public void setNormalisation(double nc) {
        normalise_= nc;
    }

    public void parseXML(Node node) throws SAXException
    { try {
        power = ReadXMLUtils.parseSingleDouble(node, "Power", "Element", false);
        ReadXMLUtils.removeNode(node,"Power","Element");
    }
    catch (SAXException e){
        throw e;
    } catch (XMLNoTagException e) {
    }
    }

    public double calcProbability(int nodeNo, Network net) {
        double prob = 0.0;
        if (normalise_ == 0.0) {
            System.out.println("Random");
            prob = 1.0/net.noNodes_;
        } else {
            if (power == 1.0) {
                prob = Math.log((nodeNo + 2)/(nodeNo + 1))*normalise_;
            } else {
                double num = (1/(1 - power))*(Math.pow(nodeNo+2, 1 - power) - Math.pow(nodeNo+1, 1 - power));
                prob = num*normalise_;
            }
        }
        return prob;
    }


}
