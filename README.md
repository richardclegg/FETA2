FETA2
=====

FETA2 java based graph analysis software (FETA 3 currently under development)

To build and run either:

ant jar
java -jar feta2-1.00.jar [script.xml]

or

ant build
java feta.FetaCLI [script.xml]

in the latter case you will need to set up CLASSPATH for java as shown below setting FETAHOME to whereever you placed the source.  After you do this source ~/.bashrc

FETAHOME=${HOME}/code/FETA2
export CLASSPATH=${CLASSPATH}:${FETAHOME}:${FETAHOME}/libs/*




FetaCLI is controlled by a single XML document. This .XML controls what FETA is doing.

<FetaOptions>
    <DataFile>  #Options related to graph data
        <Name>scripts/simple_test_graph.dat</Name>  #File name
        <Format>NNT</Format>   # Format
        <Directed>true</Directed>       #Graph is directed
        <Complex>false</Complex>        #Multiple links A->B allowed
    </DataFile>
# Allowable data file formats
# NNT -- node-id(string) node-id(string) time (epoch)    


    <Action>  # What FETA will actually do
        <Type>Measure</Type>
    </Action>
</FetaOptions>
