#!/bin/bash

bagrow=experiments/netsci18/BAgrow.xml
bameasure=experiments/netsci18/BA_analyse.xml
badegrees=experiments/netsci18/BAdegrees.dat
res1=experiments/netsci18/basnap.dat

rankgrow=experiments/netsci18/rankgrow.xml
rankmeasure=experiments/netsci18/rank_analyse.xml
rankdegrees=experiments/netsci18/rankdegrees.dat
res2=experiments/netsci18/ranksnap.dat



#rm -f $res1 $res2 $badegrees $rankdegrees

#echo 'Building BA network'
#java -jar feta2-1.0.0.jar $bagrow
#echo 'Measuring BA network'
#java -jar feta2-1.0.0.jar $bameasure > experiments/netsci18/BAresults.dat
#echo 'Growing RP network'
#java -jar feta2-1.0.0.jar $rankgrow
#echo 'Measuring RP network'
#java -jar feta2-1.0.0.jar $rankmeasure > experiments/netsci18/rankresults.dat

awk '/./{line=$0} END{print line}' $badegrees > $res1
awk '/./{line=$0} END{print line}' $rankdegrees > $res2
awk '/./{line=$0} END{print line}' $rankdegrees > $res2

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
}' experiments/netsci18/BAsnap.dat > experiments/netsci18/BAtransp.dat

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
}' experiments/netsci18/ranksnap.dat > experiments/netsci18/ranktransp.dat
