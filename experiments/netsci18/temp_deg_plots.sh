#!/usr/bin/env bash

#!/bin/bash

TPAgrow=experiments/netsci18/TPAgrow.xml
TPAmeasure=experiments/netsci18/TPA_analyse.xml
TPAdegrees=experiments/netsci18/TPAdegrees.dat
res1=experiments/netsci18/TPAsnap.dat

degtrigrow=experiments/netsci18/degtri_grow.xml
degtrimeasure=experiments/netsci18/degtri_analyse.xml
degtridegrees=experiments/netsci18/degtridegrees.dat
res2=experiments/netsci18/degtrisnap.dat

#rm -f $res1 $res2 $badegrees $rankdegrees

#echo 'Building TPA network'
#java -jar feta2-1.0.0.jar $TPAgrow
#echo 'Measuring TPA network'
#java -jar feta2-1.0.0.jar $TPAmeasure > experiments/netsci18/TPAresults.dat
#echo 'Growing degtri network'
#java -jar feta2-1.0.0.jar $degtrigrow
#echo 'Measuring degtri network'
java -jar feta2-1.0.0.jar $degtrimeasure > experiments/netsci18/degtriresults.dat

#awk '/./{line=$0} END{print line}' $TPAdegrees > $res1
awk '/./{line=$0} END{print line}' $degtridegrees > $res2

#awk '
#{
#    for (i=1; i<=NF; i++)  {
#        a[NR,i] = $i
#    }
#}
#NF>p { p = NF }
#END {
#    for(j=1; j<=p; j++) {
#        str=a[1,j]
#        for(i=2; i<=NR; i++){
#            str=str" "a[i,j];
#        }
#        print str
#    }
#}' experiments/netsci18/TPAsnap.dat > experiments/netsci18/TPAtransp.dat

awk '
{
    for (i=1; i<=NF; i++)  {
        a[NR,i] = $i
    }
}
NF>p { p = NF }
END {
    for(j=1; j<=p; j++) {
        str=a[1,j]
        for(i=2; i<=NR; i++){
            str=str" "a[i,j];
        }
        print str
    }
}' experiments/netsci18/degtrisnap.dat > experiments/netsci18/degtritransp.dat