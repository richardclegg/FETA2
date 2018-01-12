package feta;

public class LLComponent
{
    int noSteps_= 0;
    boolean fail_= false;
    double logLike_= 0.0;
    double randLogLike_= 0.0;
    
    public LLComponent()
    {
    }
    
    /** Add two components together */
    public LLComponent add(LLComponent o)
    {
        LLComponent newcomp= new LLComponent();
        newcomp.fail_= (o.fail_ & fail_);
        newcomp.logLike_= o.logLike_+logLike_;
        newcomp.randLogLike_= o.randLogLike_+randLogLike_;
        newcomp.noSteps_= o.noSteps_+ noSteps_;
        return newcomp;
    }
    
    public void setFail()
    {
        fail_=true;
    }
    
    /** Add probabilities */
    public void addLogProbability(double logProb, double logRandProb, int noSteps)
    {
        if (fail_)
            return;
        logLike_+= logProb;
        randLogLike_+= logRandProb;
        noSteps_+= noSteps;
    }
    
    public double getC0()
    {
        if (fail_ || noSteps_ == 0)
            return 1.0;
        return Math.exp((logLike_-randLogLike_)/noSteps_);
    }
    
    /** Print results */
    public String toString()
    {
        String str;
        if (fail_ ) {
            str= "- - - ";
        } else {
            str= logLike_+" "+noSteps_+" "+getC0();
        }
        return str;
    }
    
    public String prettyPrint(String header)
    {
        String str;
        if (fail_) {
            str= header+": LL - St - C0 - ";
        } else {
            str= header+": LL "+logLike_+" St "+noSteps_+" C0 "+getC0();
        }
        return str;
    }
    
    
}
