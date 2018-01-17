package feta.objectmodel;

import java.util.*;
import javax.xml.parsers.*;
import org.xml.sax.*;
import org.w3c.dom.*;
import rgc.xmlparse.*;
import feta.*;

public class RankPreferenceElement extends ObjectModelElement {


    private double power = 2.0;

    public RankPreferenceElement()
    {
    }

    public boolean useInDegree(){ return true; }

    public int [] getTrackingRequirements()
    {
        int []i= new int[1];
        i[0]= ObjectModelElement.TRACK_DEGREE_DIST;
        return i;
    }

    /**Calculate the normalisation constant*/
    public void calcNormalisation(Network net) {
        double total = 0.0;
        if (net.tns_ == null) {
            System.err.println("Tracking must be on in net for normalistion");
            System.err.println("Exiting RankPreferenceElement.calcNormalisation");
            System.exit(-1);
        }

        for(int i = 1; i<= net.noNodes_+1; i++){
            total += Math.pow(i,-power);
        }

        if (total == 0.0){
            normalise_ = 0.0;
        }
        else {
            normalise_ = 1.0/total;
        }
    }

    /**Calculate the normalisation constant from*/

    public void calcNormalisationFrom(Network net, int [] from){
        double total = 0.0;
        if (net.tns_ == null) {
            System.err.println("Tracking must be on in net for normalistion");
            System.err.println("Exiting RankPreferenceElement.calcNormalisation");
            System.exit(-1);
        }

        for(int i = 1; i<= net.noNodes_; i++){
            total += Math.pow(i,-power);
        }

        for(int j = 0; j < from.length; j++){
            total -= Math.pow(from[j],-power);
        }

        if (total == 0.0){
            normalise_ = 0.0;
        }
        else {
            normalise_ = 1.0/total;
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

    public double calcProbability(int nodeNo, Network net)
    {
        if (normalise_ == 0.0){
            System.out.println("Random");
            return 1.0/net.noNodes_;
        }
        else {
            return Math.pow(nodeNo + 1, -power)*normalise_;
        }
    }

}
