package feta;

import java.util.*;

/** Class represents the addition of a link at a given time */
public class LinkTimeElement implements Comparable<LinkTimeElement> {
    public String node1_;
    public String node2_;
    public long time_;

    /** Blank constructor */
    public LinkTimeElement() 
    {
    }
    
    public LinkTimeElement(String a, String b, long t)
    {
        node1_= a;
        node2_= b;
        time_= t;
    }
    
    public int compareTo(LinkTimeElement o2) {
        if (time_ < o2.time_)
            return -1;
        if (time_ > o2.time_)
            return 1;
        return 0;
    }

    public String toString() {
        String str= node1_+" -> "+node2_+" "+time_;
        return str;
    }
    
}
