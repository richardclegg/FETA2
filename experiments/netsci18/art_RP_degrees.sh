#!/bin/bash

script1=experiments/netsci18/rankgrow.xml
script2=experiments/netsci18/rank_analyse.xml
script3=experiments/netsci18/rankdegrees.dat
script4=experiments/netsci18/tmp.dat

java -jar feta2-1.0.0.jar $script1
java -jar feta2-1.0.0.jar $script2 > experiments/netsci18/RPmeasurements.dat

cut -d " " -f -999 $script3 > $script4

# Need to transpose the bloody thing
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
}' $script4 > $script3

rm $script4

gnuplot experiments/netsci18/RPplot.gnu > experiments/netsci18/RPheatmap.eps
epstopdf experiments/netsci18/RPheatmap.eps