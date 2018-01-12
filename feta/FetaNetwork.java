package feta ;

import java.util.*;
import java.io.*;

/** Class represents a network translated into FETA operations */
public class FetaNetwork {
    
    // Time ordered list of transform elements in FETA format
    ArrayList <FetaElement> elements_;
    FetaOptions options_= null;
    Network network_= null;
    
    public FetaNetwork(Network net, FetaOptions opt) {
        network_ = net;
        options_ = opt;
        elements_= new ArrayList<FetaElement>();
    }
    
    /** writes a file containing FetaElements */
    public void writeFile(String fname, int format) throws IOException
    {
        writeFileTime(fname,format,0,Long.MAX_VALUE);
    }
    
    /** writes a file containing FetaElements between times */
    public void writeFileTime(String fname, int format, long startTime, 
        long endTime) throws IOException
    {
        OutputStream os;
        if (fname == null) {
            os= System.out;
        } else {
            File f= new File(fname);
            os= new FileOutputStream(f,false);
        }
        if (format == FetaOptions.FETA_FILE_RAW) {
            ObjectOutputStream oos= new ObjectOutputStream(os);
            ArrayList<FetaElement>el= new ArrayList<FetaElement>();
            for (FetaElement e: elements_) {
                if (e.time_ >= startTime && e.time_ <= endTime) {
                    el.add(e);
                }
            }
            oos.writeObject(el);
        } else if (format == FetaOptions.FETA_FILE_ASCII) {
            PrintStream p= new PrintStream(os,false);
            for (FetaElement e: elements_) {
                if (e.time_ < startTime || e.time_ > endTime)
                    continue;
                p.print(e.time_+" "+e.type_+" "+e.noOldNodes_+" "+
                    e.noNewNodes_);
                if (e.newNodeName_ == null) {
                    p.print (" -");
                } else {
                    p.print(" "+e.newNodeName_);
                }
                for (int i=0; i < e.oldNodes_.length; i++) {
                    if (e.oldNodes_[i] == null) {
                        p.print(" -");
                    } else {
                        p.print(" "+e.oldNodes_[i]);
                    }
                }
                for (int i=0; i < e.newNodes_.length; i++) {
                    if (e.newNodes_[i] == null) {
                        p.print(" -");
                    } else {
                        p.print(" "+e.newNodes_[i]);
                    }
                }
                p.println();
            }
            p.close();
        }
        if (fname != null) {
            os.close();
        }
    }
    
    // Read a feta file, return an arraylist of feta elements
    public static ArrayList<FetaElement> readFile(String fname, int format)
        throws IOException
    {
        ArrayList<FetaElement> fe;
        if (format == FetaOptions.FETA_FILE_RAW) {
            fe= readFileRaw(fname);
        } else if (format == FetaOptions.FETA_FILE_ASCII) {
            fe= readFileASCII(fname);
        } else {
            throw new IOException ("Unknown FETA file format number in "+
            "FetaNetwork.readFile");
        }
        Collections.sort(fe);
        return fe;
    }
    
    /** reads a file containing FetaElements in  RAW format*/
    @SuppressWarnings("unchecked")
    private static ArrayList<FetaElement> readFileRaw(String fname) throws IOException
    {
        ArrayList<FetaElement> fe;
        Object o;
           
        File f= new File(fname);
        FileInputStream fs= new FileInputStream(f);
        ObjectInputStream ois= new ObjectInputStream(fs);
        try {
            o = (ArrayList<FetaElement>)ois.readObject();
        } catch (java.lang.ClassNotFoundException e) {
            fs.close();
            throw new IOException("File "+fname+" does not contain FETA file");
        } catch (IOException e) {
            fs.close();
            throw new IOException("File "+fname+" is not in viable FETA format");
        }
        fs.close();
        if (!(o instanceof ArrayList)) {
            throw new IOException("File "+fname+" does not contain FETA file");
        }
        fe= (ArrayList<FetaElement>)o;
        return fe;
    }
    
    /** Reads a file containing FetaElements in ASCII format */
    private static ArrayList<FetaElement> readFileASCII(String fname)
        throws IOException
    {
        ArrayList<FetaElement> fe= new ArrayList<FetaElement>();
        FileInputStream fstream = new FileInputStream(fname);
        DataInputStream in = new DataInputStream(fstream);
        BufferedReader br = new BufferedReader
            (new InputStreamReader(in));
        String strLine;
        FetaElement el;
        int type, p1, p2;
        long time;
        while ((strLine= br.readLine()) != null)   {
            if (strLine == null) {
                break;
            }
            strLine= strLine.trim();
            if (strLine.length() == 0)
                continue;
            String[] parts= strLine.split("\\s+");
            if (parts.length == 0) {
                continue;
            }
            if (parts.length < 5) {
                throw new IOException("Invalid line in FETA file\n"+strLine);
            }
            
            String newnode;
            try {
                time= Long.parseLong(parts[0]);
                type= Integer.parseInt(parts[1]);
                p1= Integer.parseInt(parts[2]);
                p2= Integer.parseInt(parts[3]);
                newnode= parts[4];
                if (newnode.equals("-"))
                    newnode= null;
            } catch (Exception e) {
                throw new IOException("Invalid line in FETA file\n"+strLine);
            }
            // Check expected no ints
            int expectedNodes= 0;
            if (type == FetaElement.OPERATION_ADD_CLIQUE || 
                    type == FetaElement.OPERATION_ADD_NODE ||
                    type == FetaElement.OPERATION_ADD_LINK) {
                expectedNodes= p1+p2;
            } else  {
                throw new IOException("Invalid operation type in FETA file\n"+strLine);
            }
            if (expectedNodes+5 != parts.length) {
                throw new IOException("Wrong number of integers in FETA file line\n"+strLine);
            }
            el= new FetaElement(1.0);
            el.time_= time;
            el.type_= type;
            el.newNodeName_= newnode;
            el.noOldNodes_= p1;
            el.noNewNodes_= p2;
            el.oldNodes_= new String[p1];
            el.newNodes_= new String[p2];
            for (int i= 0; i < p1; i++) {
                if (parts[i+5].equals("-")) {
                    el.oldNodes_[i]= null;
                } else {
                    el.oldNodes_[i]= parts[i+5];
                }
            }
            for (int i= 0; i < p2; i++) {
                if (parts[i+p1+5].equals("-")) {
                    el.newNodes_[i]= null;
                } else {
                    el.newNodes_[i]= parts[i+5];
                }
            }
            fe.add(el);
            
        }
        return fe;
    }
    

    
    /** Parse network in FETA way to get Operation and Objects */
    public void parse(ArrayList<LinkTimeElement> links, Likelihood like) 
            throws IOException {
        int index= 0;
        long time= Long.MIN_VALUE;
        ArrayList<LinkTimeElement> ltset= new ArrayList<LinkTimeElement>();
        LinkTimeElement lte;
        ArrayList<Double> opProb= new ArrayList<Double> ();
        ArrayList<Double> obProb= new ArrayList<Double> ();
        while (true) {
            // If nothing left to process then give up
            if (index >= links.size()) {
                processSet(ltset,like,obProb,opProb);
                return;
            }
            lte= links.get(index);
            // Reached time to stop
            if (lte.time_ >= options_.actionStopTime_) {
                processSet(ltset,like,obProb,opProb);
                return;
            }
            if (lte.time_ > time) {
                processSet(ltset, like,obProb,opProb);
                ltset= new ArrayList<LinkTimeElement>();
            }
            ltset.add(lte);
            time= lte.time_;
            index++;
        }
    }
    
  
    
    /** Process array into FETA format */
    public void processSet
		(ArrayList<LinkTimeElement> lte, Likelihood like, 
			ArrayList <Double>obProb, ArrayList <Double> opProb) throws IOException
    {
        if (lte.size() == 0) {
            return;
        }
        FetaElement fe;
        ArrayList<LinkTimeElement> lteadd;
        if (options_.useCliques_) {
            while(true) {
                fe= findCliques(lte);
                if (fe == null) {
                    break;
                }
                if (like != null) {
                    like.calcLL(fe,network_,options_.objectModels_
                    [FetaElement.OPERATION_ADD_CLIQUE],options_.operationModel_);
					opProb.add(like.lastOpProb_);
					obProb.add(like.lastObProb_);
                }
                elements_.add(fe);
                network_.addFetaElement(fe);
            }
        }
        LinkTimeElement l;
        // Now add all links attached to new nodes
        while (true) {
            fe= findNewNodes(lte);
            if (fe == null) {
                break;
            }
            if (like != null) {
                like.calcLL(fe,network_,options_.objectModels_
                [FetaElement.OPERATION_ADD_NODE],options_.operationModel_);
                opProb.add(like.lastOpProb_);
				obProb.add(like.lastObProb_);
            }
            elements_.add(fe);
            network_.addFetaElement(fe);
            
        }
        while (lte.size() > 0) {
            l= lte.remove(lte.size()-1);
            FetaElement e= new FetaElement(1.0);
            e.addLink(l.node1_,l.node2_,l.time_);
            //System.out.println("Adding "+e);
            if (like != null) {
                like.calcLL(e,network_,options_.objectModels_[FetaElement.OPERATION_ADD_LINK],
                options_.operationModel_); 
            	opProb.add(like.lastOpProb_);
				obProb.add(like.lastObProb_);
			}
            elements_.add(e);
            network_.addFetaElement(e);
            
        }
    }

    /* Find links in the list lte which connect cliques 
     * Return the largest clique we can find which contains at least one
     * new node or null if no clique
     * size 3 or more exists
     */
    private FetaElement findCliques(ArrayList<LinkTimeElement> lte)
    {
        if (network_.directedNetwork_) {
            System.err.println("Cliques do not work correctly in directed networks");
        }
        if (lte.size() == 0)
            return null;
        long time= lte.get(0).time_;
        HashMap <String,ArrayList<String>> nodes= 
            new HashMap<String, ArrayList<String>>();
        ArrayList <String> doubles= new ArrayList<String>();
        // Get a list of new nodes with at least two out links
        for (LinkTimeElement e: lte) {
            ArrayList<String> out;
            if (network_.nameToNumber_.get(e.node1_) == null) {
                out= nodes.get(e.node1_);
                if (out == null) {
                    out= new ArrayList<String>();
                } else {
                    doubles.add(e.node1_);
                }
                out.add(e.node2_);
                nodes.put(e.node1_,out);
            }
            if (network_.nameToNumber_.get(e.node2_) == null) {
                out= nodes.get(e.node2_);
                if (out == null) {
                    out= new ArrayList<String>();
                } else {
                    doubles.add(e.node2_);
                }
                out.add(e.node1_);
                nodes.put(e.node2_,out);
            }
        }
        if (doubles.size() == 0)
            return null;
        ArrayList <ArrayList<String>> cliques= new
            ArrayList <ArrayList<String>>();
        for (String node1: doubles) {
            ArrayList<String> conn= nodes.get(node1);
            for(int i= 1; i < conn.size(); i++) {
                String node2= conn.get(i);
                for (int j= 0; j < i; j++) {
                    String node3= conn.get(j);
                    if (linkDoesOrWillExist(node2,node3,lte)) {
                        ArrayList<String> clique= new ArrayList<String>();
                        clique.add(node1);
                        clique.add(node2);
                        clique.add(node3);
                        if (!cliqueInList(clique, cliques)) {
                            cliques.add(clique);
                        }
                    }
                }
            }
        }
        if (cliques.size() == 0)
            return null;
        while(true) {
            ArrayList <ArrayList<String>> newcliques= 
                growCliques(cliques,lte,nodes);
            if (newcliques.size() == 0)
                break;
            cliques= newcliques;
        }
        //c is our chosen clique -- all the same size so get the first
        ArrayList <String> c= cliques.get(0);
        
        
        // Remove clique
        for (int i= lte.size()-1; i>= 0; i--) {
            LinkTimeElement l= lte.get(i);
            if (c.contains(l.node1_) && c.contains(l.node2_)) {
                lte.remove(i);
            }
        }
        // Create feta element
        FetaElement fe= new FetaElement(1.0);
        ArrayList <String> oldn= new ArrayList<String>();
        ArrayList <String> newn= new ArrayList<String>();
        
        
        for(String s: c) {
            if (network_.nameToNumber_.get(s) == null) {
                newn.add(s);
            } else {
                oldn.add(s);
            }
        }
        
        String []oldNodes= new String[oldn.size()];
        for(int i= 0; i < oldn.size(); i++) {
            oldNodes[i]= oldn.get(i);
        }
        String []newNodes= new String[newn.size()];
        for(int i= 0; i < newn.size(); i++) {
            newNodes[i]= newn.get(i);
        }
        fe.addClique(oldNodes,newNodes,time);
        return fe;
    }
    
    ArrayList <ArrayList<String>> growCliques(
        ArrayList <ArrayList<String>> cliques, 
        ArrayList<LinkTimeElement> lte,
        HashMap <String,ArrayList<String>> nodes) {
        
        ArrayList <ArrayList<String>> newcliques=  new
            ArrayList <ArrayList<String>>();
        for (ArrayList<String> clique: cliques) {
            ArrayList <String> ignore= new ArrayList<String>();
            while(true) {
                String add= growClique(clique, lte,nodes,ignore);
                if (add == null)
                    break;
                ArrayList<String>clique2= new ArrayList<String>();
                for (String s: clique) {
                    clique2.add(s);
                }
                clique2.add(add);
                ignore.add(add);
                newcliques.add(clique2);
            }
        }
        return newcliques;    
    }
    
    
    String growClique(ArrayList<String> clique, 
        ArrayList<LinkTimeElement> lte,
        HashMap <String,ArrayList<String>> nodes,
        ArrayList<String> ignore)
    {
        String node= clique.get(0);
        ArrayList<String> newOut= nodes.get(node);
        for (String s: newOut) {
            if (clique.contains(s))
                continue;
            if (ignore.contains(s))
                continue;
            boolean got= true;
            for (String c: clique) {
                if (!linkDoesOrWillExist(c,s,lte)) {
                    got= false;
                    break;
                }
            }
            if (got) {
                return s;
            }
        }
        return null;
    }
    

    private boolean cliqueInList(ArrayList<String> clique,
        ArrayList <ArrayList<String>> list)
    {
        for (ArrayList<String> clique2: list) {
            if (cliqueIsSame(clique,clique2))
                return true;
        }
        return false;
    }
    
    // Compares cliques which are the same size.
    private boolean cliqueIsSame(ArrayList<String> clique1,
        ArrayList<String> clique2)
    {
        for (String i: clique1) {
            if (!clique2.contains(i))
                return false;
        }
        return true;
    }

    private boolean linkDoesOrWillExist(String as, String bs, 
        ArrayList<LinkTimeElement> lte)
    {
        // Are they both existing nodes with link
        try {
            int a= network_.nameToNumber_.get(as);
            int b= network_.nameToNumber_.get(bs);
            if (network_.isLink(a,b))
                return true;
        } catch (Exception e) {
        }
        for (LinkTimeElement e: lte) {
            if (e.node1_.equals(as) && e.node2_.equals(bs))
                return true;
            if (e.node1_.equals(bs) && e.node2_.equals(as))
                return true;
        }
        return false;
    }

    // Find links in the list lte which connect to new nodes and make
    // these an 
    private FetaElement findNewNodes(ArrayList<LinkTimeElement> lte) 
    {
        ArrayList<Integer> toRemove= new ArrayList<Integer>();
        // Find a new node in the list
        String newNode= null;
        LinkTimeElement l;
        
        for (LinkTimeElement lt: lte) {
            
            if (newNode(lt.node1_)) {
                newNode= lt.node1_;
                break;
            }
            if (newNode(lt.node2_)) {
                newNode= lt.node2_;
                break;
            }
        }
        
        // No new nodes to find
        if (newNode == null) {
            return null;
        }
        //Find all nodes connected to new node
        int newNodes= 0;
        int oldNodes= 0;
        ArrayList <String> oldNodeList= new ArrayList<String>();
        ArrayList <String> newNodeList= new ArrayList<String>();
        // Count number of old nodes and number of new nodes
        //System.out.println("FIND NEW");
        for (int i= 0; i < lte.size(); i++) {
            
            l= lte.get(i);
            //System.out.println(l);
            if (l.node1_.equals(newNode)) {
                toRemove.add(i);
                if (newNode(l.node2_)) {
                    newNodeList.add(l.node2_);
                    newNodes++;
                } else {
                    oldNodeList.add(l.node2_);
                    oldNodes++;
                }
            } else if (l.node2_.equals(newNode)) {
                toRemove.add(i);
                if (newNode(l.node1_)) {
                    newNodeList.add(l.node1_);
                    newNodes++;
                } else {
                    oldNodeList.add(l.node1_);
                    oldNodes++;
                }
            }
        }
        // Now remove them from 
        FetaElement e= new FetaElement(1.0);
        e.type_= FetaElement.OPERATION_ADD_NODE;
        String []oldNames= new String[oldNodes];
        for (int i= 0; i< oldNodes; i++) {
            oldNames[i]= oldNodeList.get(i);
        }
        String []newNames= new String[newNodes];

        for (int i= 0; i < newNodes; i++) {
            newNames[i]= newNodeList.get(i);
        }
        e.addNode(newNode,oldNames,newNames,lte.get(0).time_);
        int index= 0;
        for (int i= toRemove.size()-1; i>=0;i--) {
            int remove= toRemove.get(i);
            l= lte.remove(remove);
            index++;
        }
        //System.out.println("Got "+e);
        return e;
    }

    
    // Is this node a new node 
    private boolean newNode(String name)
    {
        // Node is new if it has no links or if not added to yet
        int node= 0;
        node= network_.getNodeFromName(name);
        if (node == -1)
            return true;
        if (network_.outLinks_.get(node).length == 0 && 
            network_.inLinks_.get(node).length == 0)
            return true;
        return false;
    }
    
}
