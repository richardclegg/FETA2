package feta.operationmodel;

import feta.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import org.xml.sax.*;
import rgc.xmlparse.*;

/** Abstract class for the operation model which 
 * controls which operations manipulate the network
 */
public class PreferentialAttachment extends OperationModel {
    
    private int addNodes_= 3;
    
    public PreferentialAttachment() 
    {
    }
   
    /** Calculate the probability of a given FetaElement operation*/ 
    public double calcProb(Network net, FetaElement fe)
    {
        if (fe.type_ != FetaElement.OPERATION_ADD_NODE) 
            return 0.0;
        if (fe.noOldNodes_ != addNodes_) {
            return 0.0;
        }
        return 1.0;
    }
    
    /** Generate a potential FetaElement using this Operation model
     * The contents of the objectnodes will be dummy*/
    public FetaElement nextElement(Network net) {
        FetaElement fe= new FetaElement();
        fe.addNode(null, new String[addNodes_],new String[0], time_);
        time_+= interval_;
        return fe;
    }
    
    
    /** Parse passed in XML */
    public void parseXML(Node node) throws SAXException {
        try {
            addNodes_= ReadXMLUtils.parseSingleInt(node, "AddNodes", "OperationModel", true);
            ReadXMLUtils.removeNode(node,"AddNodes","OperationModel");
        } catch (SAXException e) {
            throw e;
        } catch (XMLNoTagException e) {
        }
        super.parseXML(node);
    }
    
    
    
}
