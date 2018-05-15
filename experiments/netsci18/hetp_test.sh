#!/usr/bin/env bash

like=experiments/netsci18/hetp_like.xml
tmp=experiments/netsci18/hetp_like.tmp
result=experiments/netsci18/hetp_result_ordered.dat
final=experiments/netsci18/hetp_final_ordered.dat

rm -f $result $final $tmp
for c in `seq 0.0 0.01 1.0`
        do
            d=`echo "1.0 - $c" | bc -l`
            sed -e 's/AAA/'$c'/g' -e 's/BBB/'$d'/g' $like > $tmp
            echo -n $c " " >> $result
            java -jar feta2-1.0.0.jar $tmp >> $result
        done
        awk '{print $14, $1}' $result| sort -nr | head -1