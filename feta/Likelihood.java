package feta;

import java.lang.*;
import java.util.*;
import feta.objectmodel.*;
import feta.operationmodel.*;

public class Likelihood {
    public double opLL_= 0.0;
    public boolean opLLFail_= false;
    public double xvProp_;
    public double calProp_;
    public double lastOpProb_= 0.0;
    public double lastObProb_= 0.0;
    
    private int seed_;
    private Random rng_;
    
    public static final int XV_SET= 0;
    public static final int CAL_SET= 1;
    public static final int TEST_SET= 2;
    public static final int NO_SETS= 3;
    
    public  LLComponent []llComponents_= new LLComponent[NO_SETS];
    
    public Likelihood(int seed, double xvProp, double calProp) {
        seed_= seed;
        xvProp_= xvProp;
        calProp_= calProp;
        rng_= new Random();
        for (int i= 0; i < NO_SETS;i++) {
            llComponents_[i]= new LLComponent();
        }
    }
    
    /** Calculate log likelihood from a given Operation and Object Model
    of a given feta element*/
    public void calcLL (FetaElement fe, Network net, ObjectModel obm,
        OperationModel opm) 
    {
        calcOpLL(fe, net, opm);
        calcObLL(fe, net, obm);
    }
    
    /** Calculate log likelihood from a given Operation and Object Model
    of a given feta element*/
    public void calcOpLL (FetaElement fe, Network net, OperationModel opm) 
    {
        if (opLLFail_)
            return;
        if (opm == null) 
            return;
        double likelihood= opm.calcProb(net,fe);
        if (likelihood <= 0.0) {
            System.out.println("Operation Likelihood zero with "+fe);
            opLLFail_= true;
            return;
        }
        opLL_+= Math.log(likelihood);
        lastOpProb_= likelihood;
    }
    
    /** Calculate log likelihood from a given Operation and Object Model
    of a given feta element*/
    public void calcObLL (FetaElement fe, Network net, ObjectModel obm) 
    {
        if (fe.noOldNodes_ == 0)
            return;
        int set= whichSet(fe.time_);
        double logProb=0.0;
        double logRandProb= 0.0;
        obm.normalise(net);
        int []nodes= new int[fe.noOldNodes_];
        for (int i= 0; i < fe.noOldNodes_; i++) {
            nodes[i]= net.nameToNumber_.get(fe.oldNodes_[i]);
        }
        //System.err.println("Tot prob "+totProb);
        logProb= obm.calcLogProbabilitySet(net, fe, nodes);
        logRandProb= Math.log(1.0/net.noNodes_)*fe.noOldNodes_;

        llComponents_[set].addLogProbability(logProb,logRandProb,fe.noOldNodes_);
        lastObProb_= Math.exp(logProb);
    }
    
    public String prettyPrint()
    {
        String str="Op:";
        if (opLLFail_) {
           str+="- Ob:";
        } else {
            str+= opLL_+" Ob:";
        }
        str+= llComponents_[0].prettyPrint("XV")
            + llComponents_[1].prettyPrint("Cal")
            + llComponents_[2].prettyPrint("Test")
            + totComp().prettyPrint("Tot");
        return str;
    }
    
    /** print results as string */
    public String toString()
    {
        String str= "";
        if (opLLFail_) {
            str+="- ";
        } else {
            str+=opLL_+" ";
        }
        str+= llComponents_[0].toString()+ " "
            + llComponents_[1].toString()+ " "
            + llComponents_[2].toString()+ " "
            + totComp().toString();
        return str;
    }
    
    private LLComponent totComp()
    {
        return llComponents_[0].add(llComponents_[1].add(llComponents_[2]));
    }
    /** Function ensures that a time is mapped to either XV_SET CAL_SET
     * or TEST_SET consistently*/
    public int whichSet (long time)
    {
        //System.err.println("Time "+time+" seed "+seed_);
        //rng_.setSeed((time*10000+seed_));
        float r= rng_.nextFloat();
        //System.err.println("r = "+r+" xvP "+xvProp_+" calProp_ "+ calProp_);
        if (r < xvProp_)
            return XV_SET;
        if (r < xvProp_+calProp_) 
            return CAL_SET;
        return TEST_SET;
    }
    
}
