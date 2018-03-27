package feta.objectmodel;

import feta.*;

// import javax.xml.soap.Node;
import java.util.*;
import java.io.*;
import java.lang.*;

/** Class represents object model for choosing nodes */
public class ObjectModel {
    
    // Components which make up object model
    public ArrayList <ObjectModelElement> components_;
    // Multiplicative or additive
    public boolean multiply_= false;

    private double weightSum_= 0.0;

    // Bypass clever normalisation using nodesets if object model doesn't have any nice
    public boolean lazyNormalise_ = false;
    
    FetaOptions options_;
    
    public ObjectModel(FetaOptions opt)
    {
        components_= new ArrayList<ObjectModelElement>();
        options_= opt;
    }

    private int [] getRemove(Network net, int from)
    {
        if (from < 0) 
            return new int[0];
        if (net.complexNetwork_) {
            int [] n=new int[1];
            n[0]= from;
            return n;
        }
        int []outLinks= net.outLinks_.get(from);
        int []remove= new int[outLinks.length+1];
        System.arraycopy(outLinks,0,remove,0,outLinks.length);
        remove[outLinks.length]= from;
        return remove;
    }
    
    private void tempRemove (Network net, int [] remove) 
    {
        if (remove.length > 0) {
            normaliseFrom(net,remove);
        } else {
            normalise(net);
        }
        

        // remove each member in remove (temporarily)
        for (int i= 0; i < remove.length; i++) {
            NodeSet ns= net.tns_.nodeMap_.get((Integer)remove[i]);
            if (ns.members_.remove((Integer)remove[i]) == false) {
                System.err.println("Problem removing in getNodes");
            }
        }

    }
    
    /** return up to noNodes nodes in network according to model*/
    public int [] getNodes(FetaElement fe, int from, int noNodes, Network net)
    {
        int availableNodeCount= 0;
        double totWeight= 0.0;
        
        // Get nodes to remove because we have links
        int []remove= getRemove(net,from);

        if(!lazyNormalise_) {

            tempRemove(net,remove);
            for (NodeSet ns : net.tns_.nodeSets_) {
                //System.out.println(ns.members_);
                if (ns.members_.size() == 0)
                    continue;
                Iterator itr = ns.members_.iterator();
                int node= (Integer)itr.next(); // get typical member
                ns.probability_= calcProbability(node, net, false);
                if (ns.probability_ > 0.0) {
                    availableNodeCount+= ns.members_.size();
                    totWeight+= ns.probability_*ns.members_.size();
                }
                //System.err.println("Nodes "+ns.noNodes_+" prob "+ns.probability_);
            }
            // Add them back

            if (availableNodeCount == 0) {
                return new int[0];
            }
            //System.err.println("Getting "+noNodes+" available "+availableNodeCount);
            //System.out.println("Calc probability");
            if (Math.abs(1.0 - totWeight) > 0.000001) {
                System.err.println("Object model, weight does not total to 1 "+totWeight
                    +" "+from+" "+noNodes);

                System.exit(-1);
            }
            if (availableNodeCount < noNodes) {
                noNodes= availableNodeCount;
            }


        int []n= getTheNodes(fe,net, noNodes);
		
        // ADd back removed members
        for (int i= 0; i < remove.length; i++) {
            NodeSet ns= net.tns_.nodeMap_.get((Integer)remove[i]);
            ns.members_.add((Integer)remove[i]);
        }
        return n;
        }
        return getNodesLazy(fe, from, noNodes, net);

    }
    
    private int []getTheNodes(FetaElement fe,Network net, int noNodes)
    {
        int []nodes= new int[noNodes];
        int n;
        double totProb=1.0;
        double probUsed=0.0;
        for (int i= 0; i < noNodes; i++) {
            while(true) {
                n= getReadiedNode(net);
//                int j;
//                for (j= 0; j < i; j++) {
//                    if (n == nodes[j]) {
//                        break;
//                    }
//                }
//                if (j == i) {
                    nodes[i]= n;
                    break;
                //}
           }
           double prob= calcProbability(n,net,false);
           totProb*= prob/(1.0-probUsed);
           probUsed+= prob;
        }
        fe.multObProb(totProb);
        return nodes;
    }
    
    private int getReadiedNode(Network net)
    {
        if(lazyNormalise_) {
            return getReadiedNodeLazy(net);
        }
        double weightSoFar= 0.0;
        NodeSet lns= null;
        double r= Math.random();
        //System.err.println("r ="+r);
        for (NodeSet ns : net.tns_.nodeSets_) {
            int noNodes= ns.members_.size();
            double p= ns.probability_;
            if (p == 0.0 || noNodes == 0)
                continue;
            lns= ns;
            weightSoFar+= (p*noNodes);
            if (weightSoFar > r)
                break;
        }
        
        int n= (int)(Math.random()*lns.members_.size());
        return (Integer)lns.members_.toArray()[n];
    }

    /** getReadiedNode but lazier */
    public int getReadiedNodeLazy(Network net)
    {
        double weightSoFar = 0.0;
        double r = Math.random();
        int i;
        for( i = 0; i < net.noNodes_; i++) {
            double p = calcProbability(i, net, false);
            if(p == 0)
                continue;
            weightSoFar+= p;
            if(weightSoFar > r)
                break;
        }
        return i;
    }

    /** Alternative routine for lazily getting nodes without nodesets */

    public int [] getNodesLazy(FetaElement fe, int from, int noNodes, Network net)
    {
        // Are there actually any nodes available to attach to?
        int availableNodeCount= 0;
        double totWeight= 0.0;

        for (int i=0; i< net.noNodes_; i++) {
            double probability_ = calcProbability(i, net, true);
            if(probability_ > 0.0) {
                availableNodeCount++;
                totWeight += probability_;
            }
        }
        if (Math.abs(totWeight - 1.0) > 0.00001) {
            System.err.println("Object model, weight " + totWeight + " does not total to 1 "+net.noNodes_);
            System.exit(-1);
        }
        if (availableNodeCount < noNodes) {
            noNodes = availableNodeCount;
        }
        int[] n = getTheNodes(fe, net, noNodes);
        return n;
    }
    
    /** Normalise all elements */
    public void normalise(Network net) 
    {
        for (ObjectModelElement ome: components_) {
            ome.calcNormalisation(net); 
        }
    }
    
    /** Normalise all elements */
    public void normaliseFrom(Network net, int remove[])
    {

        for (ObjectModelElement ome: components_) {
            ome.calcNormalisationFrom(net,remove); 
        }
    }
    
    /** Calc probability of set of nodes on list */
    public double calcLogProbabilitySet(Network net, FetaElement fe, int nodes[]){
        double prob;
        double totLogProb= 0.0;
        double probUsed= 0.0;
        ArrayList <Integer> usedNodes = new ArrayList<Integer>();
        for (int n: nodes) {
            prob= calcProbability(n, net, false);
            if (prob <= 0) {
                System.out.println("Object returned zero prob"+fe);
                System.exit(0);
            }
            //int []outL= net.outLinks_.get(n);
            totLogProb+= Math.log(prob) - Math.log(1.0-probUsed);
            probUsed+= prob;
            //for (Integer m: outL) {
                //if (!usedNodes.contains(m)) {
                    //usedNodes.add(m);
                    //probUsed+= calcProbability(m,net,false);
                //}
            //}
            
        }
        //System.out.println("Tot prob "+totProb);
        return totLogProb;
    }
    
    /** Calculate probability for a specific node */
    public double calcProbability(int nodeNo, Network net, 
            boolean normalise) {
        if (multiply_) {
            return calcProbabilityMultiplicative(nodeNo,net, normalise);
        }
        return calcProbabilityAdditive(nodeNo, net, normalise);
    }
    
    /** This is normalised if classes have normalisation constants set*/
    private double calcProbabilityAdditive(int nodeNo, Network net,
            boolean normalise) {
        if (weightSum_ == 0.0) {
            for (ObjectModelElement ome: components_) {
                weightSum_+= ome.weight_;
            }
        }
        double prob= 0.0;
        for (ObjectModelElement ome: components_) {
            if (normalise) {
                ome.calcNormalisation(net);
            }
            prob+= ome.weight_*ome.calcProbability(nodeNo, net);
        }
        return prob/weightSum_;
    }

    /** This is unnormalised */
    private double calcProbabilityMultiplicative(int nodeNo, Network net,
        boolean normalise) {
        double prob= 1.0;
        System.err.println("CalcProbMult not yet implemented correctly");
        for (ObjectModelElement ome: components_) {
            if (normalise) {
                ome.calcNormalisation(net);
            }
            prob*= Math.pow(ome.calcProbability(nodeNo,net),ome.weight_);
        }
        return prob/weightSum_;
    }
    
}
