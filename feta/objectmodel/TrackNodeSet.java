package feta.objectmodel;

import java.util.*;
import java.lang.*;
import java.io.*;
import feta.*;


public class TrackNodeSet {
    
    boolean useInDegree_= false;
    boolean useOutDegree_= false;
    boolean useRecent_= false;
    boolean useTri_= false;
    
    HashMap <Integer, NodeSet> nodeMap_;
    HashMap <NodeProperties, NodeSet> propertyMap_;
    ArrayList <NodeSet> nodeSets_;
    
    public TrackNodeSet(FetaOptions opt) throws IOException
    {
        ObjectModel o;
        
        for (int i= 0; i < FetaElement.NO_OPERATIONS; i++) {
            o= opt.objectModels_[i];
            if (o == null || o.components_.size() == 0)
                continue;
            checkObjectModel(o);   
        }
        
    }
    
    
    /** Look at what is used by object model */
    private void checkObjectModel(ObjectModel o) {
        for (ObjectModelElement ome: o.components_) {
            if (ome.useInDegree())
                useInDegree_= true;
            if (ome.useOutDegree())
                useOutDegree_= true;
            if (ome.useRecent() > 0)
                useRecent_= true;
            if (ome.useTri())
                useTri_= true;
        }   
    }
    
    
    /** Add network from start to track node sets */
    public void updateNetwork(Network net)
    {
        nodeMap_= new HashMap <Integer, NodeSet>();
        propertyMap_= new HashMap <NodeProperties, NodeSet>();
        nodeSets_= new ArrayList<NodeSet>();
        for (int i=0; i < net.noNodes_; i++) {
            newNode(i, net);
        }
    }
    
    // Add a completely new node to the system
    public NodeSet newNode(int nodeNo, Network net)
    {
        NodeProperties np= setNodeProperties(nodeNo, net);
        
        // Find NodeSet existing in property map
        NodeSet ns= propertyMap_.get(np);
        if (ns == null) {
            ns= newNodeSet(np);
        }
        addNodeToSet(ns,nodeNo);
        return ns;
    }
    
    // Create a new node set and add it to relevant data structures
    private NodeSet newNodeSet(NodeProperties np)
    {  
        NodeSet ns = new NodeSet(np);
        propertyMap_.put(np,ns);
        nodeSets_.add(ns);
        return ns;
    }
    
    // Add node to set
    private void addNodeToSet (NodeSet ns, int nodeNo)
    {
        ns.add(nodeNo);
        nodeMap_.put((Integer)nodeNo,ns);
    }
    
    public void delNode(int nodeNo)
    {
        NodeSet ns= nodeMap_.get(nodeNo);
        if (ns == null) {
            System.err.println("Expected to find node but missed in TrackNodeSet");
            System.exit(-1);
        }
        ns.delete(nodeNo);
        nodeMap_.remove((Integer)nodeNo);
        
    }
    
    // Suspect a node has changed properties -- may be new node
    public void changeNode(int nodeNo, Network net)
    {
        NodeSet currSet= nodeMap_.get((Integer)nodeNo);
        if (currSet == null) {
            currSet= newNode(nodeNo, net);
            return;
        }
        NodeProperties n= setNodeProperties(nodeNo, net);
        NodeSet newset= propertyMap_.get(n);
        if (newset == null) {
            newset= newNodeSet(n);
        }
        NodeSet currset= nodeMap_.get(nodeNo);
        if (currset == null) {
            System.err.println("Request for currset node not in TrackNodeSet");
            System.exit(-1);
        }
        if (currset == newset) {
            return;  // No change
        }
        currset.delete(nodeNo);
        addNodeToSet(newset,nodeNo);
    }
    
    
    
    private NodeProperties setNodeProperties(int nodeNo, Network net)
    {
        int indegree= 0;
        int outdegree= 0;
        int recent= 0;
        int tri= 0;
        // Only one node at a time has each recent value so reuse
        if (useRecent_) {
            recent= net.getRecentPos(nodeNo);
            if (recent > 0) {
                return new NodeProperties(indegree,outdegree,recent,tri);
            }
        } 
        if (useInDegree_) {
            indegree= net.inLinks_.get(nodeNo).length;
        } 
        if (useOutDegree_) {
            outdegree= net.outLinks_.get(nodeNo).length;
        }
       
        if (useTri_) {
            tri= net.triCount_.get(nodeNo);
        }
        return new NodeProperties(indegree,outdegree,recent,tri);
    }
}
