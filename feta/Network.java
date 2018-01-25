package feta;
import java.io.*;
import java.util.*;
import feta.objectmodel.*;

/** Class represents a raw network in terms of nodes and links */
public class Network {
    
    public int noNodes_= 0;
    public int noLinks_= 0;
    
    private int growNumber_= 0;
    
    
    private Random rng_ = new Random();

    String grownName_="Artificial-Node-";
    
    // Track the degree distribution
    public boolean trackDegreeDistrib_=false;
    public int[]inDegreeDistrib_;
    public int[]outDegreeDistrib_;
    public int degArraySize_= 1000;
    public int maxInDegree_= 0;
    public int maxOutDegree_= 0;
    
    // Track triangles
    public boolean trackTri_= false;
    public int totTri_= 0;
    public ArrayList<Integer> triCount_;
   
    
    // Names of nodes mapped to place in link Array
    public HashMap<String, Integer> nameToNumber_;
    public ArrayList<String> numberToName_;
     
    // List of links added at times
    public ArrayList<LinkTimeElement> links_;
    
    // Is graph complex (multiple a->b links allowed
    public boolean complexNetwork_;
    // Is graph directed (a->b link does not imply b->a link)
    public boolean directedNetwork_;
    // If there are duplicate links do we just throw them away
    public boolean ignoreDuplicates_;
    // Ignore links to same node
    public boolean ignoreSelfLinks_;
    // Next time to start printing measurements
    public long nextMeasureTime_= -1;
    // Interval between measurements
    private int interval_= 1;
    // Save final degree distribution?
    private boolean finalDegDist_;
    
    // Set of tracking information for calculating probabilities
    public TrackNodeSet tns_= null;
    
    // Time to stop reading graph
    public long stopTime_= Long.MAX_VALUE;
    
    // Variables relating to printing out network measurements
    // First run for printing measurements
    private boolean firstRunPrint_= true;
    // Clustering coefficient
    private double clusterCoeff_= 0.0;
    // Largest node degree (in)
    private int largestInDegree_= 0;
    // Largest node degree (out)
    private int largestOutDegree_= 0;
    // Singleton count (inbound)
    private int singletonInCount_= 0;
    // Doubleton count (inbound)
    private int doubletonInCount_= 0;
    // Singleton count (outbound)
    private int singletonOutCount_= 0;
    // Doubleton count (outbound)
    private int doubletonOutCount_= 0;
    // mean square of out degree
    private double meanOutDegSq_= 0.0;
    // mean square of in degree
    private double meanInDegSq_= 0.0;
    // assortivity
    private double assortIn_;
    private double assortOut_;
    
    // List outlinks and inlinks for ease
    public ArrayList <int []> outLinks_= null;
    public ArrayList <int []> inLinks_= null;
    private ArrayList <long []> outLinksTime_= null;
    private ArrayList <long []> inLinksTime_= null;
    
    // List of links most recently picked
    public int []hotNodes_= null;
    public int hotPos_;  // Implement as ring buffer

    public int timeGroupInterval_ = 20; // size of each time group
    
    public int maxHotNodes_= 0;
    public boolean trackHot_= false;
    
    public Network(FetaOptions opt) {
        init();
        complexNetwork_= opt.complexNetwork_;
        directedNetwork_= opt.directedNetwork_;
        ignoreDuplicates_= opt.ignoreDuplicates_;
        ignoreSelfLinks_= opt.ignoreSelfLinks_;
        if (opt.fetaAction_ == FetaOptions.ACTION_MEASURE) {
            nextMeasureTime_= opt.actionStartTime_;
            interval_= opt.actionInterval_;
            finalDegDist_= opt.finalDegDist_;
            //System.out.println("Measure "+nextMeasureTime_+" "+interval_);
        } else {
            nextMeasureTime_= -1;
        }
    }
    
    
    
    // Common initialisation for all constructors 
    private void init() {
        noNodes_= 0;
        noLinks_= 0;
        growNumber_= 0;
        String grownName_="Artificial-Node-";
        trackDegreeDistrib_=false;
        degArraySize_= 1000;
        maxInDegree_= 0;
        maxOutDegree_= 0;
        trackTri_= false;
        totTri_= 0;
        tns_= null;
        stopTime_= Long.MAX_VALUE;
        firstRunPrint_= true;
        clusterCoeff_= 0.0;
        largestInDegree_= 0;
        largestOutDegree_= 0;
        singletonInCount_= 0;
        doubletonInCount_= 0;
        singletonOutCount_= 0;
        doubletonOutCount_= 0;
        meanOutDegSq_= 0.0;
        meanInDegSq_= 0.0;
        assortIn_= 0.0;
        assortOut_= 0.0;
        nameToNumber_= new HashMap<String,Integer>();
        numberToName_= new ArrayList<String>();
        outLinks_= new ArrayList <int []>();
        inLinks_= new ArrayList <int []>();
        outLinksTime_= new ArrayList <long []>();
        inLinksTime_= new ArrayList <long []>();
        links_= new ArrayList<LinkTimeElement>();
        hotNodes_= new int[0];
        maxHotNodes_= 0;
    }
    
    public void outputGraph(String fname, int format) throws IOException
    {
        outputGraph(fname,format,0,Integer.MAX_VALUE);
    }
    
    /** Write graph either to file or to screen */
    public void outputGraph(String fname, int format, long start, 
        long end) throws IOException {
        OutputStream os;
        if (fname == null) {
            os= System.out;
        } else {
            File f= new File(fname);
            os= new FileOutputStream(f,false);
        }
        if (format == FetaOptions.NODE_NODE_TIME) {
            outputGraphNNT(os,start,end);
        } else if (format == FetaOptions.NODE_NODE) {
            outputGraphNN(os,start,end);
        } else if (format == FetaOptions.INT_INT_TIME) {
            outputGraphIIT(os,start,end);
        }  else if (format == FetaOptions.INT_INT) {
            outputGraphII(os,start,end);
        }else {
            throw new IOException("Unrecognised graph format "+format+
            " in Network.outputGraph");
        }
        if (os != System.out) {
            os.close();
        }
    }
    
    /** Write graph to file or to screen*/
    public void outputGraphNNT(OutputStream os, long start, long end) 
        throws IOException
    {
        PrintStream p= new PrintStream(os,false);
        for (LinkTimeElement lte: links_) {
            p.println(lte.node1_+" "+lte.node2_+" "+lte.time_);
        }
    }
    
        /** Write graph to file or to screen*/
    public void outputGraphNN(OutputStream os, long start, long end) 
        throws IOException
    {
        PrintStream p= new PrintStream(os,false);
        for (LinkTimeElement lte: links_) {
            p.println(lte.node1_+" "+lte.node2_);
        }
    }

        /** Write graph to file or to screen*/
    public void outputGraphII(OutputStream os, long start, long end) 
        throws IOException
    {
        PrintStream p= new PrintStream(os,false);
        for (LinkTimeElement lte: links_) {
            p.println((nameToNumber_.get(lte.node1_))+" "+
		(nameToNumber_.get(lte.node2_)));
        }
    }
    
            /** Write graph to file or to screen*/
    public void outputGraphIIT(OutputStream os, long start, long end) 
        throws IOException
    {
        PrintStream p= new PrintStream(os,false);
        for (LinkTimeElement lte: links_) {
            p.println((nameToNumber_.get(lte.node1_))+" "+
                (nameToNumber_.get(lte.node2_))+" "+
                lte.time_);
        }
    }
    
    /** Read file */
    public ArrayList <LinkTimeElement> read(String file, int format) throws IOException {
        if (format == FetaOptions.NODE_NODE_TIME || format == FetaOptions.INT_INT_TIME) {
            return readNNT(file);
        } else if (format == FetaOptions.NODE_NODE || format == FetaOptions.INT_INT) {
            return readNN(file);
        } 
        throw new IOException("Unknown file format in Network.read "+format);
        
    }
    
    /** Start tracking node properties for calculating probabilities*/
    public void startTracking(FetaOptions o) throws IOException
    {
        startTracking(o,false);
    }
    
    public void startTracking(FetaOptions o, boolean trackAll)
        throws IOException
    {
        

        tns_= new TrackNodeSet(o);
        for (int i= 0; i < o.objectModels_.length; i++) {
            ObjectModel om= o.objectModels_[i];
            if (om == null) 
                continue;
            for (ObjectModelElement ome: om.components_) {
                int recent= ome.useRecent();
                if (recent > maxHotNodes_) {
                    maxHotNodes_= recent;
                }
                int [] tr= ome.getTrackingRequirements();
                for (int j=0; j<tr.length; j++) {
                    if (tr[j] == ObjectModelElement.TRACK_DEGREE_DIST) {
                        trackDegreeDistrib_=true;
                    } else if (tr[j] == ObjectModelElement.TRACK_TRIANGLES) {
                        trackTri_= true;
                    } else {
                        System.err.println("Unknown tracking request");
                        System.exit(-1);
                    }
                }
            }
        }
        if (trackAll) {
            trackDegreeDistrib_= true;
            trackTri_= true;
            maxHotNodes_= 20;
        }
        if (maxHotNodes_ > 0) {
            trackHot_= true;
        }
        
        if (trackHot_) {
            hotNodes_= new int[maxHotNodes_];
            for (int i= 0; i < maxHotNodes_; i++) {
                hotNodes_[i]= -1;
            }
        }
        
        if (trackDegreeDistrib_) {
            inDegreeDistrib_= new int[degArraySize_];
            outDegreeDistrib_= new int[degArraySize_];
            for (int i= 0; i < degArraySize_; i++){
                inDegreeDistrib_[i]= 0;
                outDegreeDistrib_[i]= 0;
            }
            for (int i= 0; i < noNodes_; i++) {
                addNodeDegree(i);
            }
        }
        if (trackTri_) {
            triCount_= new ArrayList<Integer>();
            totTri_= 0;
            for (int i= 0; i < noNodes_; i++) {
                int tri= triCount(i);
                triCount_.add(tri);
                totTri_+= tri;
            }
        }
        tns_.updateNetwork(this);
    } 
    
    /* Return node "hotness"*/
    public int getRecent(int nodeNo)
    {
        for (int i= 1; i <= maxHotNodes_; i++) {
            int pos= (maxHotNodes_+hotPos_-i)%maxHotNodes_;
            if (hotNodes_[pos] == nodeNo) {
                return i;
            }
        }
        return 0;
    }
    
    public int getRecentPos(int nodeNo) 
    {
        for (int i= 0; i < maxHotNodes_; i++) {
            if (hotNodes_[i] == nodeNo) 
                return i+1;
        }
        return 0;
    }
    
    private void addRecentNode(int node) 
    {
        for (int i= 0; i < maxHotNodes_; i++) {
            if (hotNodes_[i] == node)
                return;
        }
        int oldNode= hotNodes_[hotPos_];
        hotNodes_[hotPos_]= node;
        hotPos_++;
        if (hotPos_ == maxHotNodes_)
            hotPos_= 0;
        if (tns_ != null) {
            tns_.changeNode(node,this);
            if (oldNode >= 0) {
                tns_.changeNode(oldNode,this);
            }
        }
    }
    
    private void addNodeDegree(int n)
    {
        int inDeg= inLinks_.get(n).length;
        int outDeg= outLinks_.get(n).length;
        if (inDeg >= degArraySize_ || outDeg >= degArraySize_) {
            int []bigin= new int[degArraySize_*2];
            int []bigout= new int[degArraySize_*2];
            System.arraycopy(inDegreeDistrib_,0,bigin,0,degArraySize_);
            System.arraycopy(outDegreeDistrib_,0,bigout,0,degArraySize_);
            Arrays.fill(bigin,degArraySize_,degArraySize_*2,0);
            Arrays.fill(bigout,degArraySize_,degArraySize_*2,0);
            degArraySize_*=2;
            inDegreeDistrib_= bigin;
            outDegreeDistrib_= bigout;
            addNodeDegree(n);
            return;
        }
        if (inDeg > maxInDegree_)
            maxInDegree_= inDeg;
        if (outDeg > maxOutDegree_)
            maxOutDegree_= outDeg;
        inDegreeDistrib_[inDeg]++;
        outDegreeDistrib_[outDeg]++;
    }
    
    private void delNodeDegree(int n)
    {
        int inDeg= inLinks_.get(n).length;
        int outDeg= outLinks_.get(n).length;
        inDegreeDistrib_[inDeg]--;
        outDegreeDistrib_[outDeg]--;
    }
    
    public ArrayList <LinkTimeElement> getLTE()
    {
		return links_;
	}
    
    /** Read file which is in format Nodename Nodename Time*/
    public ArrayList <LinkTimeElement> readNNT(String file) throws IOException {
        ArrayList<LinkTimeElement> l= new ArrayList<LinkTimeElement>();
        try{
            FileInputStream fstream = new FileInputStream(file);
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader
                (new InputStreamReader(in));
            String strLine;
            while ((strLine= br.readLine()) != null)   {
                strLine= strLine.trim();
                if (strLine.length() == 0)
                    continue;
                String[] parts= strLine.split("\\s+");
                if (parts.length == 0) {
                    continue;
                }
                if (parts.length != 3) {
                    System.out.println(parts.length);
                    System.out.println("\""+parts[0]+"\" "+parts[0].length());
                    throw new IOException("Expected three entries on line reading "
                        +file+"\n"+strLine+"\n");
                }
                
                String node1= parts[0];
                String node2= parts[1];
                if (node1.equals(node2) && ignoreSelfLinks_) {
                    continue;
                }
                long time;
                try {
                    time= Long.parseLong(parts[2]);
                } catch (Exception e) {
                    System.err.println(e);
                    throw new IOException("Could not interpret third entry as int reading "
                        +file+"\n"+strLine+"\n"+e.getMessage());
                }
                l.add(new LinkTimeElement(node1,node2,time));
            }
            in.close();
        }catch (Exception e){
            System.err.println(e);
            throw new IOException(e.getMessage());
        }
        Collections.sort(l);
        return l;
    }
    
    
    /** Read file which is in format Nodename Nodename Time*/
    public ArrayList <LinkTimeElement> readNN(String file) throws IOException {
        ArrayList<LinkTimeElement> l= new ArrayList<LinkTimeElement>();
        long time= 1;
        try{
            FileInputStream fstream = new FileInputStream(file);
            DataInputStream in = new DataInputStream(fstream);
            BufferedReader br = new BufferedReader
                (new InputStreamReader(in));
            String strLine;
            while ((strLine= br.readLine()) != null)   {
                strLine= strLine.trim();
                if (strLine.length() == 0)
                    continue;
                String[] parts= strLine.split("\\s+");
                if (parts.length == 0) {
                    continue;
                }
                if (parts.length != 2) {
                    System.out.println(parts.length);
                    System.out.println("\""+parts[0]+"\" "+parts[0].length());
                    throw new IOException("Expected two entries on line reading "
                        +file+"\n"+strLine+"\n");
                }
                
                String node1= parts[0];
                String node2= parts[1];
                if (node1.equals(node2) && ignoreSelfLinks_) {
                    continue;
                }
                l.add(new LinkTimeElement(node1,node2,time));
                time++;
            }
            in.close();
        }catch (Exception e){
            System.err.println(e);
            throw new IOException(e.getMessage());
        }
        Collections.sort(l);
        return l;
    }
    // Build a network up until time stop time -- return list of remaining elements
    public ArrayList <LinkTimeElement> buildNetwork
        (ArrayList <LinkTimeElement> ltes, long stopTime) throws IOException
    {
        // Add blanks for each node
        noNodes_= 0;
        
        for (int i= 0; i < ltes.size();i++) {
            LinkTimeElement lte= ltes.get(i);
            if (lte.time_ >= stopTime) {
                // If we reached stop time, remove links from list
                // and make list of remaining links to add
                ArrayList <LinkTimeElement> remaining= 
                    new ArrayList <LinkTimeElement>();
                int linkSize= ltes.size();
                for (int j= i; j < linkSize; j++) {
                    remaining.add(ltes.get(j));
                }
                return remaining;
            }
            addLink(lte.node1_,lte.node2_,lte.time_);
        }
        if (nextMeasureTime_ >= 0) {
            printNetworkStatistics(true);
        }
        return new ArrayList <LinkTimeElement>();
    }
    

    
    // Is there a link from a to b
    public boolean isLink(int a, int b) 
    {
        int []outLinks= outLinks_.get(a);
        for (int i= 0; i < outLinks.length; i++) {
            if (outLinks[i] == b)
                return true;
        }
        return false;
    }
    
    public boolean isLink(String as, String bs) 
        
    {
        int a,b;
        try {
            a= nameToNumber_.get(as);
            b= nameToNumber_.get(bs);
        } catch (Exception e) { // ne or both does not exist
            return false;
        }
        return isLink(a,b);
    }
    
    // Number of links from a to b (complex networks)
    public int countLinks(int a, int b) {
        int links= 0;
        int []outLinks= outLinks_.get(a);
        for (int i= 0; i < outLinks.length; i++) {
            if (outLinks[i] == b)
                links++;
        }
        return links;
    }

    
    /** Add node to appropriate data structures */
    public void addLink(String name1, String name2 , long time) 
                throws IOException {
        int node1= addNode(name1);
        int node2= addNode(name2);
        if (nextMeasureTime_ >= 0 && time > nextMeasureTime_) {
            printNetworkStatistics(true);
            nextMeasureTime_+= interval_;
            while (nextMeasureTime_ < time) {
                printNetworkStatistics(false);
                nextMeasureTime_+= interval_;
            }
        }
        
        if (!complexNetwork_) {
            // check for existing links
            if (isLink(node1,node2)) {
                if (ignoreDuplicates_) {
                    System.err.println("Duplicate "+name1+" "+name2);
                    return;
                }
                throw new IOException("Link from "+name1+" to "
                    +name2+" exists twice in non complex network\n");
            }
        }
        addSimpleLink(node1,node2,time);
        LinkTimeElement lte= new LinkTimeElement(name1,name2,time);
        links_.add(lte);
        if (!directedNetwork_) {
            addSimpleLink(node2,node1,time);
        }
    }
    
    public int addNode(String name)
    // Add a new node to all structures -- if necessary
    {
        int nodeNo= getNodeFromName(name);
        if (nodeNo >= 0) 
            return nodeNo;
        nodeNo= noNodes_;
        noNodes_++;
        nameToNumber_.put(name,(Integer)nodeNo);
        numberToName_.add(name);
        outLinks_.add(new int[0]);
        inLinks_.add(new int[0]);
        outLinksTime_.add(new long[0]);
        inLinksTime_.add(new long[0]);
        if (trackTri_) {
            triCount_.add(0);
        }
        return nodeNo;
    }
    
    
    //  get Node number from name of node
    public int getNodeFromName (String name) 
    {
        int nodeno= -1;
        try {
            nodeno= nameToNumber_.get(name);
        } catch (Exception e) {
        }
        return nodeno;
    }
    
    // Manipulate arrays to add a link
    // without adding it to linktime element array
    private void addSimpleLink(int node1, int node2, long time) 
    {   //Change the appropriate data structures for node 1 after the addition of a new link
        int[] oldLinks= outLinks_.get(node1);
        long[] oldTimes= outLinksTime_.get(node1);
        int[] newLinks= new int[oldLinks.length+1];
        long[] newTimes= new long[oldLinks.length+1];
        if (trackDegreeDistrib_) {
            delNodeDegree(node1);
            delNodeDegree(node2);
        }

        System.arraycopy(oldLinks, 0, newLinks,0, oldLinks.length);
        System.arraycopy(oldTimes, 0, newTimes,0, oldTimes.length);
        
        newLinks[oldLinks.length]= node2;
        newTimes[oldLinks.length]= time;
        //Add the new outlinks to node1's outlink array
        outLinks_.set(node1,newLinks);
        outLinksTime_.set(node1,newTimes);

        //Do exactly the same for node2
        oldLinks= inLinks_.get(node2);
        oldTimes= inLinksTime_.get(node2);
        newLinks= new int[oldLinks.length+1];
        newTimes= new long[oldLinks.length+1];
        System.arraycopy(oldLinks, 0, newLinks,0, oldLinks.length);
        System.arraycopy(oldTimes, 0, newTimes,0, oldTimes.length);
        newLinks[oldLinks.length]= node1;
        newTimes[oldLinks.length]= time;
        inLinks_.set(node2,newLinks);
        inLinksTime_.set(node2,newTimes);

        noLinks_++;

        //Node tracking options
        if (tns_ != null) {
            if (trackTri_) {
                while (node1 >= triCount_.size()) {
                    triCount_.add(0);
                }
                while (node2 >= triCount_.size()) {
                    triCount_.add(0);
                }
                int n1Tri= triCount_.get(node1);
                int n2Tri= triCount_.get(node2);
                int newTri= 0;
                int []outLinks= outLinks_.get(node2);
                int []inLinks= inLinks_.get(node1);
                for (int i=0; i < inLinks.length;i++) {
                    if (inLinks[i] == node2)
                        continue;
                    for (int j=0; j < outLinks.length; j++) {
                        if (inLinks[i] == outLinks[j] ) {
                            newTri++;
                            triCount_.set(inLinks[i], triCount_.get(inLinks[i])+1);
                            tns_.changeNode(inLinks[i],this);
                        }
                    }
                }
                if (newTri > 0) {
                    triCount_.set(node1,n1Tri+newTri);
                    triCount_.set(node2,n2Tri+newTri);
                    totTri_+= newTri*3;
                }
            }
            tns_.changeNode(node1,this);
            tns_.changeNode(node2,this);
            if (trackDegreeDistrib_) {
                addNodeDegree(node1);
                addNodeDegree(node2);
                int degSum= 0;
                for (int i= 1; i <= maxInDegree_; i++) {
                    degSum+= i*inDegreeDistrib_[i];
                }
                if (degSum != noLinks_) {
                    System.out.println("Just added "+node1+" "+node2+" "+time+
                        " "+inLinks_.get(node1).length+" "+
                        inLinks_.get(node2).length);
                    System.out.println("Don't ADD "+degSum+" "+noLinks_);
                }
            }
            
        }
    }
  
    /** Return a new unique name for the growing network*/
    private String getNewGrowName()
    {
        String name= null;
        while (true) {
            name= grownName_+growNumber_;
            if (nameToNumber_.get(name) == null)
                break;
            grownName_= "Artificial-node-"+randStr()+"-"+growNumber_;
        }
        growNumber_++;
        return name;
    }
    
    private String randStr()
    {
        return new java.math.BigInteger(32, rng_).toString(6);
    }
    
    
    /** Use feta element to create network */
    public void buildFromFeta(FetaNetwork fn, FetaOptions opt)
    {
        for (FetaElement fe: fn.elements_) {
            if (fe.time_ > opt.actionStopTime_)
                break;
            if (fe.time_ >= opt.actionStartTime_)
                addFetaElement(fe);
        }
    }
        
    /** Grow the network by adding a single feta Element*/    
    public void addFetaElement(FetaElement e)
    {
        if (e.type_ == FetaElement.OPERATION_ADD_NODE) {
            addFetaNode(e);
        } else if (e.type_ == FetaElement.OPERATION_ADD_LINK) {
            addFetaLink(e);
        } else if (e.type_ == FetaElement.OPERATION_ADD_CLIQUE) {
            addFetaClique(e);
        } else {
            System.err.println("Unknown feta element "+e);
            System.exit(-1);
        }
    }
    
  
    /** Grows network by adding new node*/
    public double growByNode(FetaElement fe, ObjectModel model) 
    {
        int []nodes= model.getNodes(fe, -1,fe.noOldNodes_, this);
        int noOldNodes= nodes.length;
        String []oldNodes= new String[noOldNodes];
        for (int i= 0; i < noOldNodes; i++) {
            oldNodes[i]= numberToName_.get(nodes[i]);
        }
        String node= getNewGrowName();
        int noNewNodes= fe.noNewNodes_+fe.noOldNodes_-noOldNodes;
        String []newNodes= new String[noNewNodes];
        for (int i= 0; i < noNewNodes; i++) {
            newNodes[i]= getNewGrowName();
        }
        fe.addNode(node,oldNodes,newNodes,fe.time_);
        addFetaNode(fe);
        return fe.getObProb();
    }
    
    /** Add objects from a feta node to network*/
    public void addFetaNode(FetaElement fe)
    {
        long time= fe.time_;
        String node1,node2;
        if (fe.newNodeName_ != null) {
            node1= fe.newNodeName_;
        } else {
            node1= getNewGrowName();
        }
        
        try {
            for (int i=0; i< fe.noNewNodes_; i++) {
                if (fe.newNodes_.length > i) {
                    node2= fe.newNodes_[i];
                } else {
                    node2= getNewGrowName();
                }
                addLink(node1,node2, time);
            }
            for (int i= 0; i< fe.noOldNodes_; i++) {
                node2= fe.oldNodes_[i];
                addLink(node1,node2, time);
            }
            if (trackHot_) {
                for (int i= 0; i < fe.oldNodes_.length; i++) {
                    addRecentNode(nameToNumber_.get(fe.oldNodes_[i]));
                }
            }
        } catch (Exception e) {
            System.err.println(fe);
            System.err.println("Error"+e.getMessage());
            System.exit(-1);
        }
        
    }
    


    /** Grows network by adding link between existing nodes */
    public double growByLink(FetaElement fe, ObjectModel model) 
    {
        int []nodes1= model.getNodes(fe,-1,1, this);
        int []nodes2= new int[0];
        if (nodes1.length == 1) {
            nodes2= model.getNodes(fe,nodes1[0],1, this);
        }
        String node1;
        String node2;
        // If we could not find enough old nodes add new ones
        if (nodes2.length == 1) { // Two nodes obtained -- connect
            node1=  numberToName_.get(nodes1[0]);
            node2=  numberToName_.get(nodes2[0]);
            fe.addLink(node1,node2,fe.time_);
            addFetaLink(fe);
        } else if (nodes1.length == 1) { // One node obtained
            String []newNodes= new String[0];
            String []oldNodes= new String[1];
            oldNodes[0]= numberToName_.get(nodes1[0]);
            fe.addNode(getNewGrowName(),oldNodes,newNodes,fe.time_);
            addFetaNode(fe);
        } else {
            String []newNodes= new String[2];
            String node= getNewGrowName();
            newNodes[0]= getNewGrowName();
            newNodes[1]= getNewGrowName();
            String []oldNodes= new String[0];
            fe.addNode(node,oldNodes,newNodes,fe.time_);
            addFetaNode(fe);
        } 
        return fe.getObProb();
    }
    
    
    public void addFetaLink(FetaElement e)
    {
        try {
            addLink(e.oldNodes_[0],e.oldNodes_[1],e.time_);
        } catch (Exception ex) {
            System.err.println("Duplicate link error in addFeta Link "+
                e.oldNodes_[0]+" "+e.oldNodes_[1]);
            System.exit(-1);
        }
    }
    
    /** Grows network by adding clique */
    public double growByClique(FetaElement fe, ObjectModel model) 
    {
        int []nodes= model.getNodes(fe,-1,fe.noOldNodes_,this);
        String []newNodes= new String[fe.noNewNodes_];
        int noOldNodes= nodes.length; // number actually returned --
                                // may be not number requested
        String []oldNodes= new String[noOldNodes];
        for (int i= 0; i < fe.noNewNodes_;i++) {
            newNodes[i]= getNewGrowName();
        }
        for (int i= 0; i < noOldNodes; i++) {
            oldNodes[i]= numberToName_.get(nodes[i]);
        }
        fe.addClique(oldNodes, newNodes,fe.time_);
        return fe.getObProb();
    }
    
    /** Grows network by adding clique */
    public void addFetaClique(FetaElement fe) 
    {
        String[] both= new String [fe.oldNodes_.length+fe.newNodes_.length];
        System.arraycopy(fe.oldNodes_, 0, both, 0, fe.oldNodes_.length);
        System.arraycopy(fe.newNodes_, 0, both, fe.oldNodes_.length, fe.newNodes_.length);
        for (int i= 0; i < both.length; i++) {
            for (int j= 0; j < i; j++) {
                try{
                    if (!isLink(both[i],both[j]))
                        addLink(both[i],both[j],fe.time_);
                    if (directedNetwork_ && !isLink(both[j],both[i])) 
                        addLink(both[j],both[i],fe.time_);
                } catch (IOException e) {
                    System.err.println("Cannot add links in addFetaClique");
                    System.exit(-1);
                }
            }
        }
    }
    
    
    
    // print out statistics for network -- if "changed" flag is false
    // then no new links or nodes have been added to the network since
    // last printout
    public void printNetworkStatistics(boolean changed) 
    {
        
        if (firstRunPrint_) {
            System.out.println("#time Nodes links maxInDeg maxOutDeg CC "+
                "SingIn SingOut DoubIn DoubOut InDegSq OutDegSq InAssort OutAssort");
            firstRunPrint_= false;
        }
        if (changed) {
            calcStats();
        }
        System.out.println(nextMeasureTime_+" "+noNodes_+" "+ noLinks_+
            " "+largestInDegree_+" "+largestOutDegree_+" "+ clusterCoeff_+
            " "+singletonInCount_+" "+singletonOutCount_+
            " "+doubletonInCount_+" "+doubletonOutCount_+
            " "+meanInDegSq_+" "+meanOutDegSq_+" "+assortIn_+" "+assortOut_);
    }

    public void printDegDist(String filename) throws IOException {
        if(filename == null){
        throw new java.io.IOException("Must specify filename on which to write degree distribution");}
        try {
            BufferedWriter outputWriter = new BufferedWriter(new FileWriter(filename));
            for(int i = 1; i < inDegreeDistrib_.length; i++){
                outputWriter.write(inDegreeDistrib_[i]+" ");
            }
            outputWriter.flush();
            outputWriter.close();
        } catch (IOException e){
            System.err.println("Problem with writing degree distribution to file");
        }
    }

    //n.b. triangle count of node i is number of pairs of neighbours of i that are themselves neighbours
    private int triCount(int node)
    {
        int []outLinks= outLinks_.get(node);
        int []inLinks= inLinks_.get(node);
        int noOut= outLinks.length;
        int noIn= inLinks.length;
        int triCount= 0;
        for (int j= 0; j < noOut; j++) {
            int outLink= outLinks[j];
            for (int k= 0; k < noIn; k++) {
                int inLink= inLinks[k];
                if (inLink == outLink) {
                    continue;
                }
                if (complexNetwork_) {
                    triCount+= countLinks(outLink,inLink);
                } else if (isLink(outLink,inLink)) {
                    triCount++;
                }
            }
        }
        return triCount;
    }

    /** In which time group is a given node?*/
    public int timeGroup(int node){
        timeGroupInterval_ = 20;
        int tg = node/timeGroupInterval_ + 1;
        return tg;
    }
    
    /** Calculate all network statistics*/
    private void calcStats()
    {
        largestInDegree_= 0;
        largestOutDegree_= 0;
        singletonInCount_= 0;
        singletonOutCount_= 0;
        doubletonInCount_= 0;
        doubletonOutCount_= 0;
        meanOutDegSq_= 0;
        meanInDegSq_= 0;
        clusterCoeff_= 0.0;
        assortIn_= 0.0;
        assortOut_= 0.0;
        
        int clustCount= 0;
        int clustDeg= 0;
        
        for (int i= 0; i < noNodes_; i++) {
            int [] inLinks= inLinks_.get(i);
            int noIn= inLinks.length;
            int [] outLinks= outLinks_.get(i);
            int noOut= outLinks.length;
            if (noIn == 1) {
                singletonInCount_++;
            } else if (noIn == 2) {
                doubletonInCount_++;
            }
            if (noOut == 1) {
                singletonOutCount_++;
            } else if (noOut == 2) {
                doubletonOutCount_++;
            }
            meanInDegSq_+= noIn*noIn;
            meanOutDegSq_+= noOut*noOut;
            if (noIn > largestInDegree_)
                largestInDegree_= noIn;
            if (noOut > largestOutDegree_)
                largestOutDegree_= noOut;
            // Calculate cluster coeff
            
            int minLink= noOut;
            if (noIn < noOut) {
                minLink= noIn;
            }
            int triCount= triCount_.get(i);
            int possTri= 0;
            if (minLink > 1) {
                possTri= minLink*(minLink-1);
            }
            if (possTri > 0) {
                clusterCoeff_+= (double)triCount/possTri;
                clustCount++;
            } 
        }
        if (noLinks_ > 0) {
            clusterCoeff_/= noNodes_;
            meanOutDegSq_/= noLinks_;
            meanInDegSq_/= noLinks_;
            assortIn_= calcAssort(largestInDegree_,true);
            if (directedNetwork_) {
                assortOut_= calcAssort(largestOutDegree_,false);
            } else {
                assortOut_= assortIn_;
            }
        }
    }
    
    
    /** Get assortivity */
    private double calcAssort(int maxDeg, boolean inbound)
    {
        /*int [][]degCounts= new int[maxDeg][maxDeg];
        for (int i= 0; i < maxDeg; i++) {
            for (int j= 0; j < maxDeg; j++) {
                degCounts[i][j]= 0;
            }
        }*/
        int []links1;
        int []links2;
        int assSum= 0;
        int assProd= 0;
        int assSq= 0;
        int srcDeg, dstDeg;
        for (int i= 0; i < noNodes_; i++) {
            
            if (inbound) {
                links1= inLinks_.get(i);
            } else {
                links1= outLinks_.get(i);
            }
            for (int j= 0; j < links1.length; j++) {
                int l= links1[j];
                if (l < i)
                    continue;
                if (inbound) {
                    links2= inLinks_.get(l);
                } else {
                    links2= outLinks_.get(l);
                }
                srcDeg= links1.length;
                dstDeg= links2.length;
                assSum+=srcDeg+dstDeg;
                assProd+= (srcDeg*dstDeg);
                assSq+= (srcDeg*srcDeg)+(dstDeg*dstDeg);
            }
        }
        double assNum= (double)assProd/noLinks_ - ((double)assSum*assSum/(noLinks_*noLinks_*4.0));
        double assDen= (double)assSq/(noLinks_*2.0) - ((double)assSum*assSum/(noLinks_*noLinks_*4.0));
        if (assDen == 0.0)
            return 0.0;
        return assNum/assDen;
    }
}
