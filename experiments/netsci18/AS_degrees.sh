script2=experiments/netsci18/AS_analyse.xml
script3=experiments/netsci18/ASdegrees.dat
script4=experiments/netsci18/tmp.dat

java -jar feta2-1.0.0.jar $script2 > experiments/netsci18/ASmeasurements.dat

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

gnuplot experiments/netsci18/ASplot.gnu > experiments/netsci18/ASheatmap.eps
epstopdf experiments/netsci18/ASheatmap.eps