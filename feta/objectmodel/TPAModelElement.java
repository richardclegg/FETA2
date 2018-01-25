package feta.objectmodel;

import java.util.*;
import javax.xml.parsers.*;
import org.xml.sax.*;
import org.w3c.dom.*;
import rgc.xmlparse.*;
import feta.*;

//Class for an inner model element exhibiting temporal preferential attachment
public class TPAModelElement extends ObjectModelElement{

    public TPAModelElement()
    {
    }

    public boolean useTimeGroup(){
        return true;
    }

    private double getConnectivityFun(int tgDiff){
        double connectivity_ = Math.pow(2, -1 - (double)tgDiff);
        return connectivity_;
    }

    private int getTimeGroupDiff(int node1, int node2, Network net){
        int tg1 = net.timeGroup(node1);
        int tg2 = net.timeGroup(node2);
        return Math.abs(tg1 - tg2);
    }

/**Calculate the normalisation constant */
    public void calcNormalisation(Network net) {
        int timeGroups = net.noNodes_/net.timeGroupInterval_ + 1;
        double total = 0.0;
        if(net.tns_ == null){
            System.err.println("Tracking must be on in net for normalisation");
            System.err.println("Exiting TPAModelElement.calcNormalisationFrom");
            System.exit(-1);
        }
        for(int i = 1; i < timeGroups; i++){
            total+= net.timeGroupInterval_*getConnectivityFun(timeGroups - i);
        }
        //Number of members in most recent time group
        int remaining = net.noNodes_ - (timeGroups - 1)*net.timeGroupInterval_;
        total+= remaining*getConnectivityFun(0);
        if(total == 0) {
            normalise_ = 0.0;
        } else {
            normalise_ = 1/total;
        }
    }

    /**Calculate normalisation from */

    public void calcNormalisationFrom(Network net, int [] from ){
        double total = 0.0;
        if(net.tns_ == null){
            System.err.println("Tracking must be on in net for normalisation");
            System.err.println("Exiting TPAModelElement.calcNormalisationFrom");
            System.exit(-1);
        }

        int timeGroups = net.noNodes_/net.timeGroupInterval_ + 1;
        for(int i = 1; i < timeGroups; i++){
            total+= net.timeGroupInterval_*getConnectivityFun(timeGroups - i);
        }
        //Number of members in most recent time group
        int remaining = net.noNodes_ - (timeGroups - 1)*net.timeGroupInterval_;
        total+= remaining*timeGroups;

        for(int j = 0; j < from.length; j++){
            total-= getConnectivityFun(timeGroups - net.timeGroup(j));
        }
        if(total == 0){
            normalise_ = 0.0;
        } else {
            normalise_ = 1/total;
        }
    }

    /**Calculate probability */

    public double calcProbability(int nodeNo, Network net){
        if(normalise_== 0.0) {
            System.out.println("Random");
            return 1.0/net.noNodes_;
        }
        int timeGroups = net.noNodes_/net.timeGroupInterval_ + 1;
        int tg = net.timeGroup(nodeNo);
        return getConnectivityFun(timeGroups - tg)*normalise_;
    }

    public void parseXML(Node node) throws SAXException
    {
    }


}
