package feta;

import java.io.*;
import java.util.*;
import java.lang.*;
import feta.objectmodel.*;
import feta.operationmodel.*;

/**
 * Class contains basic information about feta modelling to take
 * place
 */
public class FetaModel {
    
    private FetaOptions options_;  // Options related to class
    private Network network_;       // Netwrok representation
    
    /** Initialiser for options */
    public FetaModel() {
        options_= new FetaOptions();
    }
    
    /** Read the configuration file*/
    public void readConfig (String cfile) throws IOException {
        options_.readConfig(cfile);
    }
    
    /** execute action from options file */
    public void executeAction()
    {
        if (options_.fetaAction_ == FetaOptions.ACTION_MEASURE) {
            try {
                measure();
            } catch (IOException e) {
                System.err.println("Could not measure: "+e.getMessage());
                System.exit(-1);
            }
        } else if (options_.fetaAction_ == FetaOptions.ACTION_LIKELIHOOD) {
            try {                
                getLikelihood();
            } catch (IOException e) {
                System.err.println("Could not get likelihood: "+e.getMessage());
                System.exit(-1);
            }
        } else if (options_.fetaAction_ == FetaOptions.ACTION_GROW) {
            try {
                grow();
            } catch (IOException e) {
                System.err.println("Could not grow network: "+e.getMessage());
                System.exit(-1);
            }
        } else if (options_.fetaAction_ == FetaOptions.ACTION_TRANSLATE) {
            try {
                translate();
            } catch (IOException e) {
                System.err.println("Could not translate network: "+e.getMessage());
                System.exit(-1);
            }
        } else if (options_.fetaAction_ == FetaOptions.ACTION_RFILE) {
            try {
                createRfile();
            } catch (IOException e) {
                System.err.println("Could not create R file: "+e.getMessage());
                System.exit(-1);
            }
        } else if (options_.fetaAction_ == FetaOptions.ACTION_TEST) {
			try {
				testModel();
			} catch (IOException e) {
                System.err.println("Could not test model file: "+e.getMessage());
                System.exit(-1);
            }
		}
    }
    /** Grow a new network */
    public void grow() throws IOException {
        checkModels();
        network_= new Network(options_);
        if (options_.graphFileInput_ != null) {
            ArrayList <LinkTimeElement> links= network_.read
                (options_.graphFileInput_, options_.fileFormatRead_);
            network_.buildNetwork(links, options_.actionStartTime_);
        } 
        
        network_.startTracking(options_);
        if (options_.operationModel_ != null && options_.fetaFileInput_ != null) {
            throw new IOException("Operation model and FETA input file both specified");
        }
        if (options_.operationModel_ != null) {
            growOperationModel();
        } else {
            throw new IOException("Must specify Operation Model for growth");
        }
        network_.outputGraph(options_.graphFileOutput_, options_.fileFormatWrite_);
    }
    
    /** Grow a new network using Operations model */
    private void growOperationModel() throws IOException {
        OperationModel om= options_.operationModel_;
        if ((options_.actionInterval_ == 0 || 
            options_.actionStopTime_== Long.MAX_VALUE)
            && (options_.maxLinks_ == Integer.MAX_VALUE) 
            && (options_.maxNodes_ == Integer.MAX_VALUE)
            && (!om.providesStop())) {
            System.err.println("No stopping criteria for growth model\n");
            System.exit(-1);
        }
        om.initialise(options_.actionStartTime_, 
            options_.actionInterval_);
        FetaElement fe;
        while (true) {
            fe= om.nextElement(network_);
            if (fe == null)
                break;
            if (fe.time_ >= options_.actionStopTime_)
                break;
            growByOperation(fe);
            if (network_.noNodes_ >= options_.maxNodes_)
                break;
            if (network_.noLinks_ >= options_.maxLinks_)
                break;
        }
        
    }
    
    /** Grow a new network using Operations from file */
    private void growFetaFile() throws IOException {
        ArrayList <FetaElement> fe= FetaNetwork.readFile
            (options_.fetaFileInput_,options_.fetaFormatRead_);
        for (FetaElement e: fe) {
            if (e.time_ >= options_.actionStartTime_)
                growByOperation(e);
        }
    }
    
    /** Grow the network according to the operation model */
    private double growByOperation(FetaElement e) throws IOException
    {
        switch (e.type_) {
            case (FetaElement.OPERATION_ADD_CLIQUE):
                return network_.growByClique(e, 
                    options_.objectModels_[e.type_]);
            case (FetaElement.OPERATION_ADD_NODE):
                return network_.growByNode(e,options_.objectModels_[e.type_]);
            case (FetaElement.OPERATION_ADD_LINK):
                return network_.growByLink(e,
                    options_.objectModels_[e.type_]);
            default:
                throw new IOException("Unrecognised outer model type "+
                    e.type_+" in FetaModel.growFetaFile");
        }
    }
    
    /** Creates graph measurements */
    public void measure() throws IOException {
        network_= new Network(options_);
        ArrayList <LinkTimeElement> links= network_.read
            (options_.graphFileInput_, options_.fileFormatRead_);
        network_.startTracking(options_,true);
        links= network_.buildNetwork(links, options_.actionStopTime_);
        network_.printNetworkStatistics(true);
    }
    
    /** Prints graph likelihood */
    public void getLikelihood() throws IOException {
        checkModels();
        
        network_= new Network(options_);
        ArrayList<LinkTimeElement>links;
        links= network_.read(options_.graphFileInput_,options_.fileFormatRead_);
        links= network_.buildNetwork(links,
            options_.actionStartTime_);
        Likelihood l= new Likelihood(options_.likeSeed_,
            options_.xvProp_,options_.calProp_);
        network_.startTracking(options_);
        /*for (LinkTimeElement l2: links) {
            System.out.println(l2.node1_+" "+l2.node2_);
        }*/
        parseRemainingGraph(links, l);
        
        System.out.println(l.toString());
    }
    
    public void checkModels() throws IOException {
        if (options_.objectModels_[FetaElement.OPERATION_ADD_LINK] == null || 
            options_.objectModels_[FetaElement.OPERATION_ADD_NODE] == null ||
            (options_.useCliques_ &&
            options_.objectModels_[FetaElement.OPERATION_ADD_CLIQUE] == null)) {
            throw new IOException("Must specific all models in XML");
        }
    }
    
    /** Parse the remainging graph according to FETA rules */
    public FetaNetwork parseRemainingGraph(ArrayList<LinkTimeElement> links,
        Likelihood like) throws IOException
    {
        FetaNetwork fetaNetwork= new FetaNetwork(network_, options_);
        fetaNetwork.parse(links, like);
        if (options_.fetaFileOutput_ != null || options_.fetaPrint_) {
            fetaNetwork.writeFile(options_.fetaFileOutput_, 
                options_.fetaFormatWrite_); 
        }
        return fetaNetwork;
    }

    /** Translate networks and inputs between formats */
    public void translate() throws IOException {
        FetaNetwork fn= readFromFiles();
        if (options_.graphFileOutput_ != null) {
            network_.outputGraph(options_.graphFileOutput_,
                options_.fileFormatWrite_,options_.actionStartTime_,
                options_.actionStopTime_);
        }
        if (options_.fetaFileOutput_ != null) {
            fn.writeFileTime(options_.fetaFileOutput_,options_.fetaFormatWrite_,
            options_.actionStartTime_,options_.actionStopTime_);
        }
    }
    
    public FetaNetwork readFromFiles() throws IOException
    {
        network_= new Network(options_);
        FetaNetwork fn= new FetaNetwork(network_,options_);
        if (options_.graphFileInput_ != null) {
            ArrayList<LinkTimeElement> links= network_.read
                (options_.graphFileInput_, options_.fileFormatRead_);
            links= network_.buildNetwork(links,options_.actionStartTime_);
            fn.parse(links,null);
        } 
        if (options_.fetaFileInput_ != null) {
            fn.elements_= FetaNetwork.readFile(options_.fetaFileInput_,
                options_.fetaFormatRead_);
            network_.buildFromFeta(fn,options_);
        }
        return fn;
    }

    
    /** Output R file */
    public void createRfile () throws IOException {
        if (options_.RFileOutput_ == null) {
            throw new IOException("No R output file defined");
        }
        int []pos;
        FileOutputStream []os;
        PrintStream []ps;
        
        if (options_.splitRfile_) {
            os= new FileOutputStream[FetaElement.NO_OPERATIONS];
            ps= new PrintStream[FetaElement.NO_OPERATIONS];
            String prefix, suffix;
            String [] parts=options_.RFileOutput_.split("\\.");
            if (parts.length <= 1) {
                prefix=parts[0];
                suffix="";
            } else {
                prefix="";
                for (int i= 0; i<parts.length-1; i++) {
                    if (i > 0) 
                        prefix+=".";
                    prefix+=parts[i];
                }
                suffix=parts[parts.length-1];
            }
            
            pos= new int[FetaElement.NO_OPERATIONS];
            for (int i= 0; i < FetaElement.NO_OPERATIONS; i++) {
                pos[i]= 0;
                String fname=prefix+"_"+i+"."+suffix;
                try {
                    File f= new File(fname);
                    os[i]= new FileOutputStream(fname);
                    ps[i]= new PrintStream(os[i]);
                } catch (Exception e) {
                    throw new IOException("Cannot open "+fname+
                        " for output"+e.getMessage());
                }
            }
        } else {
            pos= new int[1];
            String fname=options_.RFileOutput_;
            os= new FileOutputStream[1];
            ps= new PrintStream[1];
            try {
                File f= new File(fname);
                os[0]= new FileOutputStream(fname);
                ps[0]= new PrintStream(os[0]);
            } catch (Exception e) {
                throw new IOException("Cannot open "+fname+
                        " for output"+e.getMessage());
            }
        }
        network_= new Network(options_);
        if (options_.graphFileInput_ != null && options_.fetaFileInput_ 
            != null) {
            throw new IOException("Specify either input graph or feta file not both");
        }
        FetaNetwork fn= new FetaNetwork(network_,options_);
        ArrayList<LinkTimeElement> links= null;
        if (options_.graphFileInput_ != null) {
            links= network_.read
                (options_.graphFileInput_, options_.fileFormatRead_);
            ArrayList<LinkTimeElement> remaining= 
                network_.buildNetwork(links,options_.actionStartTime_);
            //System.out.println("Built network "+network_.noNodes_);
            ArrayList<LinkTimeElement>links2= new ArrayList<LinkTimeElement>();
            for (LinkTimeElement e: remaining) {
                if (e.time_ >= options_.actionStartTime_ && e.time_ <= 
                    options_.actionStopTime_)
                    links2.add(e);
            }
            
            //System.out.println("Nodes remaining "+links2.size());
            fn.parse(links2,null);
        } else if (options_.fetaFileInput_ != null) {
            fn.elements_= FetaNetwork.readFile(options_.fetaFileInput_,
                options_.fetaFormatRead_);
        }
        
        network_= new Network(options_);
        Likelihood ll= new Likelihood(options_.likeSeed_,options_.xvProp_,
            options_.calProp_);
        network_.startTracking(options_,true);
        if (links != null) {
            network_.buildNetwork(links,options_.actionStartTime_);
        }
        for (FetaElement fe: fn.elements_) {
            if (fe.time_ > options_.actionStopTime_)
                break;
            //System.out.println("Time is "+fe.time_+" nodes "+network_.noNodes_);
            if (fe.time_ >= options_.actionStartTime_ &&
                        ll.whichSet(fe.time_) == Likelihood.CAL_SET) {
                int stream;
                if (options_.splitRfile_) {
                    stream= fe.type_;
                } else {
                    stream= 0;
                }
                //System.out.println("Doing output at time "+fe.time_+" nodes "+network_.noNodes_);
                
                outputRFile(fe,ps[stream], pos[stream]);
                pos[stream]+= fe.oldNodes_.length;
            }
            network_.addFetaElement(fe);
        }
        for (FileOutputStream o: os) {
            o.close();
        }
        
    }
    
    
    
    private void outputRFile (FetaElement fe, PrintStream os, int pos) 
    {
        int nobjs= fe.oldNodes_.length;
        int [] choices= new int[nobjs];
        ObjectModel om= options_.objectModels_[fe.type_];
        PFPModelElement pfpModel= null;
        ArrayList <Integer> remove= new ArrayList<Integer>();

        for (int i= 0; i < nobjs; i++) {
            choices[i]= network_.nameToNumber_.get(fe.oldNodes_[i]);
        }
        for (int i= 0; i < nobjs; i++) {
            /* Do not print already chosen thing*/
            for (int j= 0; j < network_.noNodes_; j++) {
                if (remove.contains(j))
                    continue;
                os.print(j+" "+pos+" ");
                if (choices[i] == j) {
                    os.print("1 ");
                } else {
                    os.print("0 ");
                }
                os.print("1 ");  // batch size ?
                os.print((network_.noNodes_-remove.size())+" "); // no choices
                
                os.print((network_.inLinks_.get(j).length)+" "); // degree
                os.print((network_.triCount_.get(j))+" ");
                os.print(network_.getRecent(j)+"\n"); //recent count
            
            }
            pos++;
            remove.add(choices[i]);
            if (fe.type_ == FetaElement.OPERATION_ADD_LINK) {
                int []outLinks= network_.outLinks_.get(choices[i]);
                for (int j= 0; j < outLinks.length; j++) {
                    remove.add(outLinks[j]);
                }
            }
        }
    }
    
    /** Check model meets expectations */
    private void testModel() throws IOException {
	        checkModels();
        network_= new Network(options_);
        if (options_.graphFileInput_ != null) {
            ArrayList <LinkTimeElement> links= network_.read
                (options_.graphFileInput_, options_.fileFormatRead_);
            network_.buildNetwork(links, options_.actionStartTime_);
        } 
        
        network_.startTracking(options_);
        if (options_.operationModel_ != null && options_.fetaFileInput_ != null) {
            throw new IOException("Operation model and FETA input file both specified");
        }
        if (options_.operationModel_ == null) {
            throw new IOException("Must specify Operation Model for growth");
        }	
                OperationModel om= options_.operationModel_;
        if ((options_.actionInterval_ == 0 || 
            options_.actionStopTime_== Long.MAX_VALUE)
            && (options_.maxLinks_ == Integer.MAX_VALUE) 
            && (options_.maxNodes_ == Integer.MAX_VALUE)
            && (!om.providesStop())) {
            System.err.println("No stopping criteria for growth model\n");
            System.exit(-1);
        }
        om.initialise(options_.actionStartTime_, 
            options_.actionInterval_);
        ArrayList <Double> obs= new ArrayList<Double>();
        ArrayList <Double> ops= new ArrayList<Double>();
        FetaElement fe;
        while (true) {
            fe= om.nextElement(network_);
            obs.add(fe.getObProb());
            ops.add(fe.getOpProb());
            if (fe == null)
                break;
            if (fe.time_ >= options_.actionStopTime_)
                break;
            growByOperation(fe);
            if (network_.noNodes_ >= options_.maxNodes_)
                break;
            if (network_.noLinks_ >= options_.maxLinks_)
                break;
        }
        ArrayList <LinkTimeElement> links=  network_.getLTE();
        network_= new Network(options_);
        links= network_.buildNetwork(links,
			options_.actionStartTime_);

        network_.startTracking(options_);
        /*for (LinkTimeElement l2: links) {
            System.out.println(l2.node1_+" "+l2.node2_);
        }*/
        FetaNetwork fetaNetwork= new FetaNetwork(network_, options_);
        
        int index= 0;
        long time= Long.MIN_VALUE;
        ArrayList<LinkTimeElement> ltset= new ArrayList<LinkTimeElement>();
        LinkTimeElement lte;
        Likelihood like= new Likelihood(0,0.0,0.0);
        ArrayList <Double> newObProb= new ArrayList <Double>();
        ArrayList <Double> newOpProb= new ArrayList <Double>();
        while (true) {
            // If nothing left to process then give up
            if (index >= links.size()) {
                fetaNetwork.processSet(ltset,like,newObProb, newOpProb);
                break;
            }
            lte= links.get(index);
            // Reached time to stop
            if (lte.time_ >= options_.actionStopTime_) {
                fetaNetwork.processSet(ltset,like,newObProb, newOpProb);
                break;
            }
            if (lte.time_ > time) {
                fetaNetwork.processSet(ltset, like,newObProb, newOpProb);
                ltset= new ArrayList<LinkTimeElement>();
            }
            ltset.add(lte);
            time= lte.time_;
            index++;
        }
	}
    
}
