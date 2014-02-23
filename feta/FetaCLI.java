package feta;

import java.io.*;

public class FetaCLI {
    
    /** Main entry point for FETA Command Line Interface 
     * 
     *  Only argument is XML control file name
     * 
     */
    
    
    /** Public constructor for class
     * 
     */
     
    public FetaCLI() {
        
    }
    
    /** Entry point for CLI version of FETA
     * 
     */
      
    public static void main(String[] args) {

        if (args.length != 1) {
            System.err.println("Command line must specify "+
                               "XML file to read and nothing else.");
            System.exit(-1);
        }
        FetaModel fm= new FetaModel();
        try {
            fm.readConfig(args[0]);
            
        } catch (IOException e) {
            System.err.println("Could not read XML "+args[0]+"\n"+e.getMessage());
            System.exit(-1);
        }
        fm.executeAction();
            
    }  
    
}
