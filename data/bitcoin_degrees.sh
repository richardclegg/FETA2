#!/bin/bash

bitdegrees=data/bitcoin/bitcoindegrees2009-2010.txt
res1=data/bitcoin/bitsnap.dat

awk '/./{line=$0} END{print line}' $bitdegrees > $res1
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
}' $res1 > data/bitcoin/bitcointransp.dat