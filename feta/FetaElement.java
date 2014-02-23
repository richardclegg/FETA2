package feta;

import java.util.*;
import java.io.*;

public class FetaElement implements Serializable, Comparable<FetaElement> {
    
    public static final int OPERATION_ADD_CLIQUE=0;
    public static final int OPERATION_ADD_NODE=1;
    public static final int OPERATION_ADD_LINK=2;
    
    public static final int NO_OPERATIONS= 3;
    
    public int type_=0;
    public int noOldNodes_= 0;  // Number of old nodes connected
    public int noNewNodes_= 0;  // Number of new nodes connected
    public long time_= 0;
    public String []oldNodes_= null;
    public String []newNodes_= null;
    public String newNodeName_= null;
    
    public FetaElement() {
        oldNodes_= new String[0];
    }
    
    public boolean typeEquals(FetaElement e) 
    {
        if (type_ != e.type_)
            return false;
        if (noOldNodes_ != e.noOldNodes_)
            return false;
        if (noNewNodes_ != e.noNewNodes_)
            return false;
        return true;
    }
    
    public int compareTo(FetaElement o2) {
        if (time_ < o2.time_)
            return -1;
        if (time_ > o2.time_)
            return 1;
        return 0;
    }
    
    /** make element be new link */
    public void addLink(String node1, String node2, long time) {
        oldNodes_= new String[2];
        oldNodes_[0]= node1;
        oldNodes_[1]= node2;
        newNodes_= new String[0];
        type_= OPERATION_ADD_LINK;
        newNodeName_= null;
        noOldNodes_= 2;
        noNewNodes_= 0;
        time_= time;
    }
    
    public void addNode(String nodeName,
        String []oldNodes, String []newNodes,long time) {
        newNodeName_= nodeName;
        oldNodes_= oldNodes;
        noOldNodes_= oldNodes.length;
        newNodes_= newNodes;
        noNewNodes_= newNodes.length;
        type_= OPERATION_ADD_NODE;
        time_= time;
    }
    
    public void addClique(String []oldNodes, String []newNodes,long time) {
        newNodeName_= null;
        oldNodes_= oldNodes;
        noOldNodes_= oldNodes.length;
        newNodes_= newNodes;
        noNewNodes_= newNodes.length;
        type_= OPERATION_ADD_CLIQUE;
        time_= time;
    }
    
    public String toString() {
        String str;
        switch (type_) {
            case (OPERATION_ADD_CLIQUE):
                str= time_+" CLIQUE ";
                break;
            case (OPERATION_ADD_NODE):
                str= time_+" NODE ";
                break;
            case (OPERATION_ADD_LINK):
                str= time_+" LINK ";
                break;
            default:
                str= time_+" NULL ";
                break;
        }
        if (newNodeName_ != null)
            str+=newNodeName_+" ";
        for (int i= 0; i < oldNodes_.length; i++) {
            if (i == 0) {
                str+= "OLD ";
            }
            str+= oldNodes_[i]+" ";
        }
        for (int i= 0; i < newNodes_.length; i++) {
            if (i == 0) {
                str+= "NEW ";
            }
            str+= newNodes_[i]+" ";
        }
        
        return str;
    }
}
