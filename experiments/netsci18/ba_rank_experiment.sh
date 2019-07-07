#!/bin/bash

grow=experiments/netsci18/BA_rank_grow.xml
test=experiments/netsci18/BA_rank_test.xml
tmp1=experiments/netsci18/BA_rank_grow.tmp
tmp2=experiments/netsci18/BA_rank_test.tmp
tmp3=experiments/netsci18/rankBAtmp.tmp
result=experiments/netsci18/rankBAresult.tmp
finalres=experiments/netsci18/rankBAfinal.out

rm -f $tmp1 $tmp2 $tmp3 $finalres

for a in `seq 0.0 0.2 1.0`
do
    b=`echo "1.0 - $a" | bc -l`
    sed -e 's/AAA/'$a'/g' -e 's/BBB/'$b'/g' $grow > $tmp1
    for i in `seq 1 10`
    do
        rm -f $tmp3
        java -jar feta2-1.0.0.jar $tmp1
        rm -f $result
        for c in `seq 0.0 0.01 1.0`
        do
            d=`echo "1.0 - $c" | bc -l`
            sed -e 's/CCC/'$c'/g' -e 's/DDD/'$d'/g' $test > $tmp2
            echo -n $a " " $c " " >> $result
            java -jar feta2-1.0.0.jar $tmp2 >> $result
        done
    awk '{print $15, $1, $2}' $result| sort -nr | head -1 >> $finalres
    done
done