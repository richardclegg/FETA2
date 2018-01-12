package feta.operationmodel;

import feta.*;
import java.util.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import org.xml.sax.*;
import rgc.xmlparse.*;

/** Abstract class for the operation model which 
 * controls which operations manipulate the network
 */
public class FromFile extends OperationModel {
    
    private int pos_=0;
    private ArrayList<FetaElement> feta_= null;
    String file_;
    int format_= FetaOptions.FETA_FILE_RAW;
    
    public FromFile() 
    {
        pos_=0;
    }
   
    /** Calculate the probability of a given FetaElement operation*/ 
    public double calcProb(Network net, FetaElement fe)
    {
        if (feta_==null) {
            try {
                feta_= FetaNetwork.readFile(file_,format_);
            } catch (Exception e) {
                System.err.println(e);
                System.err.println("Cannot read file "+file_);
                System.exit(-1);
            }
        }
        if (pos_ >= feta_.size())
            return 0.0;
        double prob=0.0; 
        if (feta_.get(pos_).typeEquals(fe))
            prob=1.0;
        pos_++;
        return prob;
    }
    
    /** Generate a potential FetaElement using this Operation model
     * The contents of the objectnodes will be dummy*/
    public FetaElement nextElement(Network net) {
        if (feta_ == null) { 
            try {
                feta_= FetaNetwork.readFile(file_,format_);
            } catch (Exception e) {
                System.err.println(e);
                System.err.println("Cannot read file "+file_);
                return null;
            }
        }
        if (pos_ >= feta_.size())
            return null;
        FetaElement fe= feta_.get(pos_);
        pos_++;
        return fe;
    }
    
    public boolean providesStop()
    {
        return true;
    }
    
    /** Parse passed in XML */
    public void parseXML(Node node) throws SAXException {
        try {
            file_= ReadXMLUtils.parseSingleString(node, "FileName", "OperationModel", false);
            ReadXMLUtils.removeNode(node,"FileName","OperationModel");
        } catch (SAXException e) {
            throw e;
        } catch (XMLNoTagException e) {
        }
                try {
        String str= ReadXMLUtils.parseSingleString(node, "FetaFormat", 
            "OperationModel", true);
        ReadXMLUtils.removeNode(node,"FetaFormat","OperationModel");
        format_= FetaOptions.processFetaFormat(str);
        } catch (SAXException e) {
            throw e;
        } catch (XMLNoTagException e) {
        }
        
        super.parseXML(node);
    }
    
    
    
}
