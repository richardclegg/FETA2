package feta;

import java.io.*;
import java.lang.reflect.*;
import org.w3c.dom.*;
import javax.xml.parsers.*;
import org.xml.sax.*;
import rgc.xmlparse.*;
import feta.objectmodel.*;
import feta.operationmodel.*;

/**
 * Class contains basic information about options for FetaModel
 * class
 */
public class FetaOptions {
    
    // Graph file formats understood --- NNT
    // File contains node1, node2, time of arrival    
    public static final int NODE_NODE_TIME= 0;
    public static final int NODE_NODE= 1;
    public static final int INT_INT_TIME= 2;
    public static final int INT_INT= 3;
    

    // FETA file formats understood
    public static final int FETA_FILE_RAW= 0;
    public static final int FETA_FILE_ASCII= 1;
    
    // Actions which can be taken
    public static final int ACTION_MEASURE= 1;
    public static final int ACTION_LIKELIHOOD= 2;
    public static final int ACTION_GROW= 3;
    public static final int ACTION_RFILE= 4;
    public static final int ACTION_TRANSLATE= 5;
    public static final int ACTION_TEST= 6;
    
    

    // Options in datafile tag
    public String graphFileInput_= null;  // Name of file containing network
    public String graphFileOutput_= null;  // Name of file to output network
    public String fetaFileInput_= null;     // Name of file containg FETA elements
    public String fetaFileOutput_= null;    // Name of file to write FETA elements
    public String RFileOutput_= null;           // Name of file to write R output
    public int fileFormatRead_= NODE_NODE_TIME;  // Format of graph file to read
    public int fileFormatWrite_= NODE_NODE_TIME; // Format of graph file to write
    public int fetaFormatRead_= FETA_FILE_RAW;  // Format of FETA file to read
    public int fetaFormatWrite_= FETA_FILE_RAW; // Format of FETA file to write
    public boolean fetaPrint_= false;          // Print out feta format data
    public boolean directedNetwork_= false;  // Is file directed
    public boolean complexNetwork_= false;   // Multiple links between nodes?
    public boolean ignoreDuplicates_= false; // If multiple links disregard first
    public boolean ignoreSelfLinks_= true;
    public boolean splitRfile_= false; // Split R file into many files
    
    // Options in action tag
    public int fetaAction_= ACTION_MEASURE;
    public int actionInterval_= 1;   // Interval for action
    public long actionStartTime_= 0;  // Start time for action
    public long actionStopTime_= Long.MAX_VALUE;   // Stop time for action
    public boolean finalDegDist_= false;
    public String degDistToFile_= null;
    public int maxLinks_= Integer.MAX_VALUE;
    public int maxNodes_= Integer.MAX_VALUE;
    
    // Operation model -- governs probabilities which control which
    // entities are added to the graph
    public OperationModel operationModel_= null;
    public boolean useCliques_= false;
    
    // Object models -- contain probabilities which create graph objects
    // given operation model
    public ObjectModel [] objectModels_;
    
    /** Variables related to likelihood calculations */
    public int likeSeed_= 0;
    public double xvProp_= 0.2;
    public double calProp_= 0.1;
    
    /**Initialiser for options
     * 
     */
    public FetaOptions() {
        objectModels_= new ObjectModel[FetaElement.NO_OPERATIONS];
        for (int i= 0; i < FetaElement.NO_OPERATIONS; i++) {
            objectModels_[i]= null;
        }
        
    }
    
    /** Read the xml specifying what to do */
    public void readConfig(String file) throws IOException {
        try { DocumentBuilderFactory docBuilderFactory = 
            DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document doc = docBuilder.parse (new File(file));

              // normalize text representation
            doc.getDocumentElement ().normalize ();
            String basenode= doc.getDocumentElement().getNodeName();
            if (!basenode.equals("FetaOptions")) {
                throw new SAXException("Base tag should be SimOptions");
            }
            
            // Process subsections in turn
            NodeList subnode= doc.getElementsByTagName("DataFile");
            processDataFile(subnode);
            
            subnode= doc.getElementsByTagName("Action");
            processAction(subnode);
            
            subnode= doc.getElementsByTagName("OperationModel");
            processOperationModel(subnode);
            
            subnode= doc.getElementsByTagName("ObjectModel");
            processObjectModel(subnode);
            
            subnode= doc.getElementsByTagName("Likelihood");
            processLikelihood(subnode);
            
            //  Now check all elements have been processed
            Element el= doc.getDocumentElement();
            NodeList rest= el.getChildNodes();
            for (int i= 0; i < rest.getLength(); i++) {
                Node n= rest.item(i);
                if (n.getNodeType() == Node.ELEMENT_NODE) {
                    throw new SAXException("When reading XML unrecognised tag "+n.getNodeName());
                }
            }
        } catch (java.io.FileNotFoundException e) {
            System.err.println("Cannot find file "+file);
            System.exit(-1);
        } catch (SAXParseException err) {
            System.err.println ("** Parsing error" + ", line "
                                + err.getLineNumber () + ", uri " + err.getSystemId ());
            System.err.println(err.getMessage ());
            System.exit(-1);

        } catch (SAXException e) {
            System.err.println("Exception in SAX XML parser.");
            System.err.println(e.getMessage());
            System.exit(-1);
        } catch (Throwable t) {
            System.err.println("Caught unknown XML exception.");
            t.printStackTrace ();
            System.exit(-1);
        }
    }
    
    /** Process the part of the XML related to the data file to be read*/
    private void processDataFile(NodeList nodelist) throws SAXException {
        if (nodelist.getLength() != 1) {
            throw new SAXException ("Must have exactly one DataFile tag.");
        }
        Node node= nodelist.item(0);

        // graph and Feta inputs and outputs
        try {
            graphFileInput_= ReadXMLUtils.parseSingleString
                (node, "GraphInput","DataFile",true);
            ReadXMLUtils.removeNode(node,"GraphInput","DataFile");
        } catch (SAXException e) {
            throw e;
        } catch (XMLNoTagException e) {
        }
        try {
            graphFileOutput_= ReadXMLUtils.parseSingleString
                (node, "GraphOutput","DataFile",true);
            ReadXMLUtils.removeNode(node,"GraphOutput","DataFile");
        } catch (SAXException e) {
            throw e;
        } catch (XMLNoTagException e) {
        }
        try {
            fetaFileInput_= ReadXMLUtils.parseSingleString
                (node, "FetaInput","DataFile",true);
            ReadXMLUtils.removeNode(node,"FetaInput","DataFile");
        } catch (SAXException e) {
            throw e;
        } catch (XMLNoTagException e) {
        }
        
        try {
            fetaFileOutput_= ReadXMLUtils.parseSingleString
                (node, "FetaOutput","DataFile",true);
            ReadXMLUtils.removeNode(node,"FetaOutput","DataFile");
        } catch (SAXException e) {
            throw e;
        } catch (XMLNoTagException e) {
        }
        
        
        // Should feta file be printed -- to screen if no output file specified
        try {
            fetaPrint_= ReadXMLUtils.parseSingleBool
                (node, "FetaPrint","DataFile",true);
            ReadXMLUtils.removeNode(node,"FetaPrint","DataFile");
        } catch (SAXException e) {
            throw e;
        } catch (XMLNoTagException e) {
        }
        
        // Rfile
        try {
            RFileOutput_= ReadXMLUtils.parseSingleString
                (node,"ROutput","DataFile",true);
            ReadXMLUtils.removeNode(node,"ROutput","DataFile");
        } catch (SAXException e) {
            throw e;
        } catch (XMLNoTagException e) {
        }
        
        
        // Format tag
        try {
            String format= ReadXMLUtils.parseSingleString
                (node, "FormatRead","DataFile",true);
            ReadXMLUtils.removeNode(node,"FormatRead","DataFile");
            fileFormatRead_= processFileFormat(format);
        } catch (SAXException e) {
            throw e;
        } catch (XMLNoTagException e) {
        }
        try {
            String format= ReadXMLUtils.parseSingleString
                (node, "FormatWrite","DataFile",true);
            ReadXMLUtils.removeNode(node,"FormatWrite","DataFile");
            fileFormatWrite_= processFileFormat(format);
        } catch (SAXException e) {
            throw e;
        } catch (XMLNoTagException e) {
        }
        
        // FetaFormat tag
        try {
            String fetaFormatStr= ReadXMLUtils.parseSingleString
                (node, "FetaFormatRead","DataFile",true);
            ReadXMLUtils.removeNode(node,"FetaFormatRead","DataFile");
            fetaFormatRead_= processFetaFormat(fetaFormatStr);
        } catch (SAXException e) {
            throw e;
        } catch (XMLNoTagException e) {
        }        
        try {
            String fetaFormatStr= ReadXMLUtils.parseSingleString
                (node, "FetaFormatWrite","DataFile",true);
            ReadXMLUtils.removeNode(node,"FetaFormatWrite","DataFile");
            fetaFormatWrite_= processFetaFormat(fetaFormatStr);
        } catch (SAXException e) {
            throw e;
        } catch (XMLNoTagException e) {
        } 
        
        // Split R file
        try {
            splitRfile_= ReadXMLUtils.parseSingleBool
                (node, "SplitR","DataFile",true);
            ReadXMLUtils.removeNode(node,"SplitR","DataFile");
        } catch (SAXException e) {
            throw e;
        } catch (XMLNoTagException e) {
        }
        
        // Complex tag
        try {
            complexNetwork_= ReadXMLUtils.parseSingleBool
                (node, "Complex","DataFile",true);
            ReadXMLUtils.removeNode(node,"Complex","DataFile");
        } catch (SAXException e) {
            throw e;
        } catch (XMLNoTagException e) {
        }
        
        // Ignore duplicate tag
        try {
            ignoreDuplicates_= ReadXMLUtils.parseSingleBool
                (node, "IgnoreDuplicates","DataFile",true);
            ReadXMLUtils.removeNode(node,"IgnoreDuplicates","DataFile");
        } catch (SAXException e) {
            throw e;
        } catch (XMLNoTagException e) {
        }
        
        
        // Directed tag
        try {
            directedNetwork_= ReadXMLUtils.parseSingleBool
                (node, "Directed","DataFile",true);
            ReadXMLUtils.removeNode(node,"Directed","DataFile");
        } catch (SAXException e) {
            throw e;
        } catch (XMLNoTagException e) {
        }
        checkUnparsed(node,"DataFile");    
    }
    
    /** Convert a string to a file format number*/
    private int processFileFormat(String format) throws SAXException
    {
        if (format.equals("NNT")) {
            return NODE_NODE_TIME;
        } 
        if (format.equals("NN")) {
            return NODE_NODE;
        } 
        if (format.equals("IIT")) {
            return INT_INT_TIME;
        } 
        if (format.equals("II")) {
            return INT_INT;
        } 
        throw new SAXException("In DataFile, Format, unrecognised format "+
                    format);
    }
    
    /** Convert a string to a feta format number*/
    public static int processFetaFormat(String fetaFormat) throws SAXException
    {
        if (fetaFormat.equals("Raw")) {
            return FETA_FILE_RAW;
        } else if (fetaFormat.equals("ASCII")) {
            return FETA_FILE_ASCII;
        } 
        throw new SAXException("In DataFile, FetaFormat, unrecognised format "+
                    fetaFormat);
    }
    
    /** Process the part of the XML related to the Action to be taken*/
    private void processAction(NodeList nodelist) throws SAXException {
        if (nodelist.getLength() != 1) {
            throw new SAXException ("Must have exactly one Action tag.");
        }
        Node node= nodelist.item(0);
        
        // Type tag
        try {
            String action= ReadXMLUtils.parseSingleString (node, "Type",
                "Action",false);
            if (action.equals("Measure")) {
                fetaAction_= ACTION_MEASURE;
            } else if (action.equals("Likelihood")) {
                fetaAction_= ACTION_LIKELIHOOD;
            } else if (action.equals("Grow")) {
                fetaAction_= ACTION_GROW;
            } else if (action.equals("Translate")) {
                fetaAction_= ACTION_TRANSLATE;
            } else if (action.equals("Rfile")) {
                fetaAction_= ACTION_RFILE;
            } else if (action.equals("Test")) {
				fetaAction_= ACTION_TEST;
            } else {
                throw new SAXException("Unrecognised action type "+action+" in tag Type in tag Action");
            }
            ReadXMLUtils.removeNode(node,"Type","Action");
        }  catch (SAXException e) {
            throw e;
        } catch (XMLNoTagException e) {
        }
        try {
            maxLinks_= ReadXMLUtils.parseSingleInt(node, "MaxLinks", "OperationModel", true);
            ReadXMLUtils.removeNode(node,"MaxLinks","OperationModel");
        } catch (SAXException e) {
            throw e;
        } catch (XMLNoTagException e) {
        }
        try {
            maxNodes_= ReadXMLUtils.parseSingleInt(node, "MaxNodes", "OperationModel", true);
            ReadXMLUtils.removeNode(node,"MaxNodes","OperationModel");
        } catch (SAXException e) {
            throw e;
        } catch (XMLNoTagException e) {
        }
        
        //Interval tag
        try {
            actionInterval_= ReadXMLUtils.parseSingleInt(node, "Interval", "Action", true);
            ReadXMLUtils.removeNode(node,"Interval","Action");
        } catch (SAXException e) {
            throw e;
        } catch (XMLNoTagException e) {
        }
    
        //Start tag
        try {
            actionStartTime_= ReadXMLUtils.parseSingleLong(node, "Start", "Action", true);
            ReadXMLUtils.removeNode(node,"Start","Action");
        } catch (SAXException e) {
            throw e;
        } catch (XMLNoTagException e) {
        }
        //Stop tag
        try {
            actionStopTime_= ReadXMLUtils.parseSingleLong(node, "Stop", "Action", true);
            ReadXMLUtils.removeNode(node,"Stop","Action");
        } catch (SAXException e) {
            throw e;
        } catch (XMLNoTagException e) {
        }

        //Final degree distribution?
        try {
            finalDegDist_= ReadXMLUtils.parseSingleBool(node, "FinalDegreeDistribution", "Action", true);
            ReadXMLUtils.removeNode(node,"FinalDegreeDistribution","Action");
        } catch (SAXException e) {
            throw e;
        } catch (XMLNoTagException e) {
        }

        //Write degree distribution to file

        try {
            degDistToFile_= ReadXMLUtils.parseSingleString(node, "ToFile", "Action", true);
            ReadXMLUtils.removeNode(node,"ToFile","Action");
        } catch (SAXException e) {
            throw e;
        } catch (XMLNoTagException e) {
        }
        
        checkUnparsed(node,"Action");
    }
    
    
    /** Process the part of the XML related to the operation model*/
    private void processOperationModel(NodeList nodelist) throws SAXException {
        if (nodelist.getLength() > 1) {
            throw new SAXException ("Cannot have more than one Operation Model tag.");
        }
        if (nodelist.getLength() == 0) 
            return;
        Node node= nodelist.item(0);
        try {
            useCliques_= ReadXMLUtils.parseSingleBool
                (node, "Cliques","OperationModel",true);
            ReadXMLUtils.removeNode(node,"Cliques","OperationModel");
        } catch (SAXException e) {
            throw e;
        } catch (XMLNoTagException e) {
        }
        
        String type=null;
        Class <? extends OperationModel> cl= null;
        try {
            // Get Type tag
            type= ReadXMLUtils.parseSingleString(node, "Type", "OperationModel", true);
            ReadXMLUtils.removeNode(node,"Type","OperationModel");
            cl =  Class.forName(type).asSubclass(OperationModel.class);
        } catch (SAXException e) {
            throw e;
        } catch (ClassNotFoundException e) {
            throw new SAXException("Cannot find class "+type+
                " to make new OperationModel from XML");
        } catch (XMLNoTagException e) {
        } 
        
        if (type != null) {
            try {
                Constructor <?> c= cl.getConstructor();
                operationModel_= (OperationModel)c.newInstance();
            } catch (Exception e) {
                throw new SAXException("Cannot construct OperationModel from "
                    +type+ " "+e.getMessage());
            }
            operationModel_.parseXML(node);
        }
        try {
            maxLinks_= ReadXMLUtils.parseSingleInt(node, "MaxLinks", "OperationModel", true);
            ReadXMLUtils.removeNode(node,"MaxLinks","OperationModel");
        } catch (SAXException e) {
            throw e;
        } catch (XMLNoTagException e) {
        }
        try {
            maxNodes_= ReadXMLUtils.parseSingleInt(node, "MaxNodes", "OperationModel", true);
            ReadXMLUtils.removeNode(node,"MaxNodes","OperationModel");
        } catch (SAXException e) {
            throw e;
        } catch (XMLNoTagException e) {
        }
        
        checkUnparsed(node,"OperationModel");
    }
    
    /** Process the part of the XML related to the object model*/
    private void processObjectModel(NodeList nodelist) throws SAXException {
        if (nodelist.getLength() == 0) 
            return;
            
        // Reverse order so we can delete at the end
        String entity="";
        for (int i= nodelist.getLength()-1; i >= 0; i--) {
            Node node= nodelist.item(i);
            // For each node work out what object model we have
            try {
                entity= ReadXMLUtils.parseSingleString(node, "Entity", "ObjectModel", false);
                ReadXMLUtils.removeNode(node,"Entity","ObjectModel");
            } catch (SAXException e) {
                throw e;
            } catch (XMLNoTagException e) {
            }
            if (entity.equals("Link")) {
                objectModels_[FetaElement.OPERATION_ADD_LINK]= 
                    parseObjectModel(node);
            } else if (entity.equals("Node")) {
                objectModels_[FetaElement.OPERATION_ADD_NODE]= 
                    parseObjectModel(node);
            } else if (entity.equals("Clique")) {
                objectModels_[FetaElement.OPERATION_ADD_CLIQUE]
                    = parseObjectModel(node);
            } else {
                throw new SAXException("Unrecognised entity type "+entity+
                    " in ObjectModel tag");
            }
            checkUnparsed(node,"ObjectModel");
        }
    }
    
    /** Parse code for an object model*/
    private ObjectModel parseObjectModel(Node node) throws SAXException {
        ObjectModel om= new ObjectModel(this);
        try {
            // Get multiply tag
            om.multiply_= ReadXMLUtils.parseSingleBool(node, "Multiply", "ObjectModel", true);
                ReadXMLUtils.removeNode(node,"Multiply","ObjectModel");
        } catch (SAXException e) {
            throw e;
        } catch (XMLNoTagException e) {
        }
        try {
            om.lazyNormalise_ = ReadXMLUtils.parseSingleBool(node, "LazyNormalise", "ObjectModel", true);
            ReadXMLUtils.removeNode(node, "LazyNormalise", "ObjectModel");
        } catch (SAXException e) {
            throw e;
        } catch (XMLNoTagException e) {
        }
        NodeList nl=((Element)node).getElementsByTagName("Element");
        if (nl.getLength() == 0) {
            throw new SAXException("ObjectModel tags must have at least one Element tag");
        }
        // Get all model elements 
        for (int i= nl.getLength()-1; i >= 0; i--) {
            ObjectModelElement ome= parseObjectModelElement(nl.item(i));
            om.components_.add(ome);
        }
        
        return om;
    }
    
    /** Parse XML for an element of an object model */
    private ObjectModelElement parseObjectModelElement(Node node) throws SAXException {
        ObjectModelElement ome= null;
        String type="";
        Class <? extends ObjectModelElement> el= null;
        try {
            // Get Type tag
            type= ReadXMLUtils.parseSingleString(node, "Type", "Element", false);
            ReadXMLUtils.removeNode(node,"Type","Element");
            el =  Class.forName(type).asSubclass(ObjectModelElement.class);
        } catch (SAXException e) {
            throw e;
        } catch (ClassNotFoundException e) {
            throw new SAXException("Cannot find class "+type+
                " to make new ObjectModelElement from XML");
        } catch (XMLNoTagException e) {
        } 
        
        try {
            Constructor <?> c= el.getConstructor();
            ome= (ObjectModelElement)c.newInstance();
            ome.parseXML(node);
        } catch (Exception e) {
            throw new SAXException("Cannot construct ObjectModelElement "+type+ " "+e.getMessage());
        }
        
        try {
            // Get Weight tag
            ome.weight_= ReadXMLUtils.parseSingleDouble(node, "Weight", "Element", false);
            ReadXMLUtils.removeNode(node,"Weight","Element");
        } catch (SAXException e) {
            throw e;
        } catch (XMLNoTagException e) {
        }
        checkUnparsed(node,"Element");
        return ome;
    }
    
    /** Process the part of the XML related to the operation model*/
    private void processLikelihood(NodeList nodelist) throws SAXException {
        if (nodelist.getLength() > 1) {
            throw new SAXException ("Cannot have more than one Operation Model tag.");
        }
        if (nodelist.getLength() == 0) 
            return;
        Node node= nodelist.item(0);
        /** Parse an int which is the seed */
        try {
            likeSeed_= ReadXMLUtils.parseSingleInt
                (node, "Seed","Likelihood",true);
            ReadXMLUtils.removeNode(node,"Seed","Likelihood");
        } catch (SAXException e) {
            throw e;
        } catch (XMLNoTagException e) {
        }
        /** Parse a double which is the crossvalidation proportion */
        try {
            xvProp_= ReadXMLUtils.parseSingleDouble
                (node, "XVProp","Likelihood",true);
            ReadXMLUtils.removeNode(node,"XVProp","Likelihood");
        } catch (SAXException e) {
            throw e;
        } catch (XMLNoTagException e) {
        }
         /** Parse a double which is the crossvalidation proportion */
        try {
            calProp_= ReadXMLUtils.parseSingleDouble
                (node, "CalibrationProp","Likelihood",true);
            ReadXMLUtils.removeNode(node,"CalibrationProp","Likelihood");
        } catch (SAXException e) {
            throw e;
        } catch (XMLNoTagException e) {
        }
        
        checkUnparsed(node,"Likelihood");
    }
        
    private void checkUnparsed(Node node, String name) throws SAXException
    {
        // Check for other unparsed tags
        NodeList nl= node.getChildNodes();
        for (int i= 0; i < nl.getLength(); i++) {
            Node n= nl.item(i);
            if (n.getNodeType() == Node.ELEMENT_NODE) {
                throw new SAXException("In tag "+name+" XML unrecognised tag "+n.getNodeName());
            }

        }
        node.getParentNode().removeChild(node);
    }
}
