package feta.objectmodel;



public class NodeProperties {
    
    public int inDegree_;  //
    public int outDegree_;
    public int recent_;
    public int triangles_;
    public int rank_;
    
    NodeProperties(int i, int o, int r, int t, int ra) {
        inDegree_= i;
        outDegree_= o;
        recent_= r;
        triangles_= t;
        rank_ = ra;
    }
    
    public String toString() {
        String str=inDegree_+" "+outDegree_+" "+recent_+" "+triangles_+" "+rank_;
        return str;
    }
    @Override
    public int hashCode() {
        return (((inDegree_*10)+outDegree_)*10+recent_)*10+triangles_;
    }
    
    @Override
    public boolean equals(Object o)
    {
        if (o == null)
            return false;
        if (o.getClass() != getClass())
            return false;
        
        NodeProperties n= (NodeProperties)o;
        if (n.inDegree_ != inDegree_)
            return false;
        if (n.outDegree_ != outDegree_)
            return false;
        if (n.recent_ != recent_)
            return false;
        if (n.triangles_ != triangles_)
            return false;
        if (n.rank_ != rank_)
            return false;
        return true;
    }
    
}
