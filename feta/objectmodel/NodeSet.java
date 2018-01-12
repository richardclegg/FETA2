package feta.objectmodel;

import java.lang.*;
import java.util.*;
import feta.*;

/** Class representing a set of nodes with certain properties */
public class NodeSet {
    
    public HashSet <Integer> members_;
    public double probability_= 0.0;  // Probability for individual member
    public int noNodes_= 0;   // Number of members
    NodeProperties properties_; // Properties of nodes in this set
    
    NodeSet(NodeProperties p)
    {   
        members_= new HashSet<Integer>();
        properties_= p;
    }
    
    
    public void add(int node)
    {
        members_.add(node);
        noNodes_++;
    }
    
    public void delete(int node)
    {
        members_.remove(((Integer)node));
        noNodes_--;
    }
    
    public String toString()
    {
        String str=noNodes_+":";
        for (Integer m: members_) {
            str+= " "+m;
        }
        return str;
    }
    
    
}
