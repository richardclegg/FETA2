package feta.operationmodel;

import feta.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import org.xml.sax.*;
import rgc.xmlparse.*;

public class GeneralPreferentialAttachment extends OperationModel {

    private int addNodes_ = 3;
    private double nodeProb_ = 0.2;
    private double linkProb_ = 1.0 - nodeProb_;
    private double randomNo;

    public GeneralPreferentialAttachment()
    {
    }

    public double calcProb(Network net, FetaElement fe){
        if(fe.type_ == FetaElement.OPERATION_ADD_NODE){
            if(fe.noOldNodes_ != addNodes_){
                return 0.0;
            }
            return 1.0;
        }
        if(fe.type_ == FetaElement.OPERATION_ADD_LINK){
            return 1.0;
        }
        return 0.0;
    }

    public FetaElement nextElement(Network net) {
        randomNo = Math.random();
        if(0.0 < randomNo && randomNo <nodeProb_) {
            FetaElement fe = new FetaElement(1.0);
            fe.addNode(null, new String[addNodes_], new String[0], time_);
            time_ += interval_;
            return fe;
        }
        else {
            FetaElement fe = new FetaElement(1.0);
            fe.addLink(null, null, time_);
            time_ += interval_;
            return fe;
        }

    }
}
