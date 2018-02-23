#!/bin/bash
SCRIPT1=experiments/RankBALike.xml
if [ ! -d experiments/tmp ]; then mkdir experiments/tmp; fi
RESULT1=experiments/tmp/ranktmp.tmp
TMP=experiments/tmp/rankBAliketmp.tmp
FINALRES=experiments/tmp/rankBAlike.out
rm -f $FINALRES

for i in `seq 1 1`;
do
    echo $i
    java -jar feta2-1.0.0.jar experiments/RankTest.xml
    rm -f $RESULT1
    for a in `seq 0.0 0.01 1.0`;
    do
        b=`echo "1.0 - $a" | bc -l`
        echo -n $a " " $b " " >> $RESULT1
        sed -e 's/XXX/'$a'/g' -e 's/YYY/'$b'/g' $SCRIPT1 > $TMP
        java -jar feta2-1.0.0.jar $TMP >> $RESULT1
    done
    awk '{print $15, $1, $2}' $RESULT1| sort -nr | head -1 >> $FINALRES
done