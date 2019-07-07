package feta.objectmodel;

import javax.xml.parsers.*;
import org.xml.sax.*;
import org.w3c.dom.*;
import rgc.xmlparse.*;
import feta.*;

/** Abstract class represents part of model*/
public abstract class ObjectModelElement {
    
    public double weight_=1.0;
    public double normalise_= 1.0;
    
    public static final int TRACK_DEGREE_DIST=0;
    public static final int TRACK_TRIANGLES=1;
    public static final int TRACK_RECENT=2;
    
    public ObjectModelElement()
    {
    }
    
    /** Get items required for tracking*/
    
    public int [] getTrackingRequirements()
    {
        return new int[0];
    }
        
    /** parse left over XML from model */
    abstract public void parseXML(Node node) throws SAXException;
    
    /**Calculate the normalisation constant */
    abstract public void calcNormalisation(Network net);
    

        
    /**Calculate the normalisation constant excluding given node */
    abstract public void calcNormalisationFrom(Network net, int [] from);
    
    /** calculate probability */
    abstract public double calcProbability(int nodeNo, Network net);
    
    /** Does this model element require indegree*/
    public boolean useInDegree() 
    {
        return false;
    }
    
    /** Does this model element require outdegree */
    public boolean useOutDegree() 
    {
        return false;
    }
        /** Does this model element require outdegree */
    public int useRecent() 
    {
        return 0;
    }
    
    /** Does this model require rank */
    public boolean useRank() { return false; }
    
    /** Does this model element require triangle counts */
    public boolean useTri()
    {
        return false;
    }

    /** Does this model element require time groups */
    public boolean useTimeGroup() { return false; }

    /** Does this model come with a prescribed normalisation calculation? */
    public boolean usePrescribedNormalisation() { return true; }

}
